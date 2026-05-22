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
import uz.uptimehub.resource.dto.resource.*;
import uz.uptimehub.resource.dto.resourcetype.SpecificationDefinition;
import uz.uptimehub.resourceapp.jpa.entity.resource.Resource;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;
import uz.uptimehub.resourceapp.jpa.repository.ResourceRepository;
import uz.uptimehub.resourceapp.jpa.repository.ResourceTypeRepository;
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

        return resourceMapper.toDetailedDto(resourceRepository.save(resourceMapper.toEntity(request, resourceType)));
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

        assertRequiredSpecificationsExistence(resourceData, resource.getResourceType().getSpecificationDefinitions());

        resourceRepository.save(resourceMapper.updateFromDto(resourceData, resource));
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
        return resourceRepository.findAllFiltered(
                filter.getFilter().getId(),
                filter.getFilter().getOrganizationId(),
                filter.getFilter().getName(),
                filter.getFilter().getResourceTypeId(),
                filter.getFilter().getStatus(),
                filter.getPageable()
        ).map(resourceMapper::toDto);
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
     * <p>If the request does not contain both management/view permissions, the incoming status filter
     * is constrained to {@link ResourceStatus#PUBLISHED}. The current implementation also allows explicit
     * {@link ResourceStatus#MAINTENANCE} and {@link ResourceStatus#ARCHIVED} values to pass through unchanged.
     *
     * @param request the HTTP request containing user permissions in headers
     * @param filter the ResourceFilter whose status may be overridden based on user permissions
     */
    public void statusOverride(HttpServletRequest request, ResourceFilter filter) {
        ResourceStatus status = filter.getStatus();

        if (status == ResourceStatus.PUBLISHED || status == ResourceStatus.MAINTENANCE || status == ResourceStatus.ARCHIVED) {
            return;
        }

        String[] permissions = AuthHeaderUtils.extractRolesOrPermissions(request.getHeader(permissionsHeader));



        List<String> list = Arrays.asList(permissions);
        if (!list.contains("resource:view-all") || !list.contains("resource:manage")) {
            filter.setStatus(ResourceStatus.PUBLISHED);
            return;
        }

        // Organization override for organization admins to view their own resources with restricted statuses
        String sessionUserOrgId = request.getHeader("X-Organization-Id");

        if (sessionUserOrgId != null && filter.getOrganizationId() != null) {
            filter.setOrganizationId(UUID.fromString(sessionUserOrgId));
        }
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

        Set<String> requiredSpecs = specificationDefinitions.stream()
                .filter(SpecificationDefinition::getRequired)
                .map(SpecificationDefinition::getName)
                .collect(Collectors.toSet());

        requiredSpecs.forEach(spec -> {
            if (!request.getSpecificationValues().containsKey(spec))
                throw new RequiredSpecificationNotAvailableException("Missing required specification: " + spec);
        });

    }

    private void assertRequiredSpecificationsExistence(ResourceDto request, List<SpecificationDefinition> specificationDefinitions) {

        Set<String> requiredSpecs = specificationDefinitions.stream()
                .filter(SpecificationDefinition::getRequired)
                .map(SpecificationDefinition::getName)
                .collect(Collectors.toSet());

        requiredSpecs.forEach(spec -> {
            if (!request.getSpecificationValues().containsKey(spec))
                throw new RequiredSpecificationNotAvailableException("Missing required specification: " + spec);
        });

    }


}
