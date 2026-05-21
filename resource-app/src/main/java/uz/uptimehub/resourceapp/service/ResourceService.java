package uz.uptimehub.resourceapp.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uz.uptimehub.core.exception.EntityNotFoundException;
import uz.uptimehub.core.exception.InvalidSortRule;
import uz.uptimehub.core.pagination.FilteredSortedPaginatedRequest;
import uz.uptimehub.core.utils.AuthHeaderUtils;
import uz.uptimehub.resource.dto.Status;
import uz.uptimehub.resource.dto.resource.*;
import uz.uptimehub.resource.dto.resourcetype.ResourceTypeFilter;
import uz.uptimehub.resourceapp.jpa.entity.resource.Resource;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;
import uz.uptimehub.resourceapp.jpa.repository.ResourceRepository;
import uz.uptimehub.resourceapp.jpa.repository.ResourceTypeRepository;
import uz.uptimehub.resourceapp.mapper.ResourceMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ResourceService extends CommonService<ResourceCreateRequest, ResourceDto, ResourceFilter>{

    private final ResourceRepository resourceRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ResourceMapper resourceMapper;

    @Override
    public DetailedResourceResponse create(ResourceCreateRequest request) {
        ResourceType resourceType = resourceTypeRepository.findById(request.getResourceTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Resource type not found with id: " + request.getResourceTypeId()));

         return resourceMapper.toDetailedDto(resourceRepository.save(resourceMapper.toEntity(request, resourceType)));
    }

    @Override
    public void update(ResourceDto resourceData) {
        Resource resource = resourceRepository.findByIdAndOrganizationId(resourceData.getId(), resourceData.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Resource not found with id: " + resourceData.getId()));

        resourceRepository.save(resourceMapper.updateFromDto(resourceData, resource));
    }

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


    public void organizationIdOverride(ResourceCreateRequest body, HttpServletRequest request) {
        String header = request.getHeader("X-Organization-Id");

        body.setOrganizationId(UUID.fromString(header));
    }

    public void organizationIdOverride(ResourceDto body, HttpServletRequest request) {
        String header = request.getHeader("X-Organization-Id");

        body.setOrganizationId(UUID.fromString(header));
    }
    public void statusOverride(HttpServletRequest request, ResourceFilter filter) {
        ResourceStatus status = filter.getStatus();

        if (status == ResourceStatus.PUBLISHED || status == ResourceStatus.MAINTENANCE || status == ResourceStatus.ARCHIVED) {
            return;
        }

        String[] permissions = AuthHeaderUtils.extractRolesOrPermissions(request.getHeader(permissionsHeader));

        List<String> list = Arrays.asList(permissions);
        if (!list.contains("resource:view-all") || !list.contains("resource:manage")) {
            filter.setStatus(ResourceStatus.PUBLISHED);
        }
    }


}
