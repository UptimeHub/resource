package uz.uptimehub.resourceapp.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uz.uptimehub.core.exception.EntityNotFoundException;
import uz.uptimehub.core.exception.InvalidSortRule;
import uz.uptimehub.core.pagination.FilteredSortedPaginatedRequest;
import uz.uptimehub.core.utils.AuthHeaderUtils;
import uz.uptimehub.resource.dto.exception.RequiredSpecificationNotAvailableException;
import uz.uptimehub.resource.dto.kafka.dto.IndexType;
import uz.uptimehub.resource.dto.kafka.dto.ResourceIndexEvent;
import uz.uptimehub.resource.dto.resource.*;
import uz.uptimehub.resource.dto.resourcetype.SpecificationDefinition;
import uz.uptimehub.resourceapp.jpa.entity.resource.Resource;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;
import uz.uptimehub.resourceapp.jpa.repository.ResourceRepository;
import uz.uptimehub.resourceapp.jpa.repository.ResourceTypeRepository;
import uz.uptimehub.resourceapp.kafka.producer.ResourceIndexEventProducer;
import uz.uptimehub.resourceapp.mapper.ResourceMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for business logics of {@link uz.uptimehub.resourceapp.jpa.entity.resource.Resource}
 * entity.
 *
 * <p>Primary responsibilities:
 * <ul>
 *   <li>Resolve and validate resource type references on create.</li>
 *   <li>Enforce required specification values defined by the resource type.</li>
 *   <li>Apply organization-scoped behavior from incoming request headers.</li>
 *   <li>Apply permission-aware status filtering for public read paths.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ResourceService extends CommonService<ResourceCreateRequest, ResourceDto, ResourceFilter>{

    private final ResourceRepository resourceRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ResourceMapper resourceMapper;
    private final ResourceIndexEventProducer resourceIndexEventProducer;
    private final ResourceSearchService resourceSearchService;

    /**
     * Creates and persists a new resource.
     *
     * <p>The method resolves the resource type by {@code resourceTypeId}, validates all required specifications
     * from the resource type definition, then saves a new {@code Resource} entity and returns a detailed DTO.
     *
     * @param request validated creation payload containing type, organization, and specification values
     * @return created resource with extended audit payload
     * @throws EntityNotFoundException when the referenced resource type does not exist
     * @throws RequiredSpecificationNotAvailableException if any required specification defined on the type is missing
     */
    @Override
    public DetailedResourceResponse create(ResourceCreateRequest request) {
        ResourceType resourceType = resourceTypeRepository.findById(request.getResourceTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Resource type not found with id: " + request.getResourceTypeId()));

        List<SpecificationDefinition> specificationDefinitions = resourceType.getSpecificationDefinitions();

        assertRequiredSpecificationsExistence(request, specificationDefinitions);

        Resource resource = resourceRepository.save(resourceMapper.toEntity(request, resourceType));

        resourceIndexEventProducer.publish(new ResourceIndexEvent(resource.getId(), IndexType.CREATED));
        return resourceMapper.toDetailedDto(resource);
    }

    /**
     * Updates an existing resource with non-null values from the incoming DTO.
     *
     * @param resourceData partial resource update payload; null properties are ignored
     * @throws EntityNotFoundException if no resource exists for the given id and organization
     */
    @Override
    public void update(ResourceDto resourceData) {
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceData.getId(), resourceData.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Resource not found with id: " + resourceData.getId()));

        Map<String, Object> specificationValues = resourceData.getSpecificationValues() == null
                ? resource.getSpecificationValues()
                : resourceData.getSpecificationValues();

        assertRequiredSpecificationsExistence(specificationValues, resource.getResourceType().getSpecificationDefinitions());

        resourceRepository.save(resourceMapper.updateFromDto(resourceData, resource));

        resourceIndexEventProducer.publish(new ResourceIndexEvent(resource.getId(), IndexType.UPDATED));
    }

    /**
     * Returns a filtered and paginated list of resources.
     *
     * <p>All filter and pagination details are delegated to {@code ResourceRepository.findAllFiltered(...)}
     * and then mapped to {@link ResourceDto}.
     * </p>
     *
     * @param filter normalized request filter and pageable payload
     * @return page of resource DTOs
     */
    @Override
    public Page<ResourceDto> findAll(FilteredSortedPaginatedRequest<ResourceFilter, InvalidSortRule> filter) {
        if (resourceSearchService.supports(filter.getFilter())) {
            return resourceSearchService.search(filter.getFilter(), filter.getPageable());
        }

        return resourceRepository.findAllFiltered(
                filter.getFilter().getId(),
                filter.getFilter().getOrganizationId(),
                filter.getFilter().getName(),
                filter.getFilter().getResourceTypeId(),
                filter.getFilter().getStatus(),
                filter.getPageable()
        ).map(resourceMapper::toDto);
    }


    public ResourceDto getById(UUID resourceId) {
        return resourceRepository.findById(resourceId)
                .map(resourceMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found with id: " + resourceId));
    }

    /**
     * Overrides organization id on create requests using the authenticated header
     * ({@code X-Organization-Id}).
     *
     * <p>Controllers call this before create when header-based organization resolution is required.
     *
     * @param body resource creation request to update
     * @param request active HTTP request carrying the organization header
     */
    public void organizationIdOverride(ResourceCreateRequest body, HttpServletRequest request) {
        String header = request.getHeader("X-Organization-Id");

        body.setOrganizationId(UUID.fromString(header));
    }

    /**
     * Overrides organization id on update payloads using the authenticated header
     * ({@code X-Organization-Id}).
     *
     * <p>Ensures update calls are executed in a caller-provided organization context.
     *
     * @param body resource update request to update
     * @param request active HTTP request carrying the organization header
     */
    public void organizationIdOverride(ResourceDto body, HttpServletRequest request) {
        String header = request.getHeader("X-Organization-Id");

        body.setOrganizationId(UUID.fromString(header));
    }

    /**
     * Applies status visibility rules to list filters based on caller permissions.
     *
     * <p>Platform managers with both management/view permissions can query across all organizations and
     * statuses. Organization managers are constrained to their authenticated organization while retaining
     * access to non-public statuses. Other callers are constrained to {@link ResourceStatus#PUBLISHED}.
     *
     * @param request the HTTP request containing user permissions in headers
     * @param filter the ResourceFilter whose status may be overridden based on user permissions
     */
    public void statusOverride(HttpServletRequest request, ResourceFilter filter) {
        String[] permissions = AuthHeaderUtils.extractRolesOrPermissions(request.getHeader(permissionsHeader));
        List<String> list = Arrays.asList(permissions);

        if (list.contains("resource:view-all") && list.contains("resource:manage")) {
            return;
        }

        String sessionUserOrgId = request.getHeader("X-Organization-Id");
        if (list.contains("resource:manage") && sessionUserOrgId != null && !sessionUserOrgId.isBlank()) {
            filter.setOrganizationId(UUID.fromString(sessionUserOrgId));
            return;
        }

        filter.setStatus(ResourceStatus.PUBLISHED);
    }

    /**
     * Validates that all required specifications defined for a resource type are present in
     * the creation request.
     *
     * <p>Steps:
     * <ol>
     *   <li>Collect required specification names from the provided type definitions.</li>
     *   <li>Verify each required key exists in {@code request.specificationValues}.</li>
     * </ol>
     *
     * @param request the resource creation request containing incoming specification values
     * @param specificationDefinitions the list of specification definitions for the resource type, including which specifications are required
     * @throws RequiredSpecificationNotAvailableException if any required specification is missing from the request's specification values
     * @see ResourceCreateRequest#getSpecificationValues()
     * @see SpecificationDefinition#getRequired()
     * @see RequiredSpecificationNotAvailableException */
    private void assertRequiredSpecificationsExistence(ResourceCreateRequest request, List<SpecificationDefinition> specificationDefinitions) {

        assertRequiredSpecificationsExistence(request.getSpecificationValues(), specificationDefinitions);
    }

    private void assertRequiredSpecificationsExistence(Map<String, Object> specificationValues, List<SpecificationDefinition> specificationDefinitions) {
        List<SpecificationDefinition> definitions = specificationDefinitions == null
                ? Collections.emptyList()
                : specificationDefinitions;

        Set<String> requiredSpecs = definitions.stream()
                .filter(specificationDefinition -> Boolean.TRUE.equals(specificationDefinition.getRequired()))
                .map(SpecificationDefinition::getName)
                .collect(Collectors.toSet());

        Map<String, Object> values = specificationValues == null ? Collections.emptyMap() : specificationValues;

        requiredSpecs.forEach(spec -> {
            if (!values.containsKey(spec))
                throw new RequiredSpecificationNotAvailableException("Missing required specification: " + spec);
        });

    }

}
