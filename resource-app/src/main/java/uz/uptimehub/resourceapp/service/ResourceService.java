package uz.uptimehub.resourceapp.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uz.uptimehub.core.exception.EntityNotFoundException;
import uz.uptimehub.core.exception.InvalidSortRule;
import uz.uptimehub.core.pagination.FilteredSortedPaginatedRequest;
import uz.uptimehub.resource.dto.resource.DetailedResourceResponse;
import uz.uptimehub.resource.dto.resource.ResourceCreateRequest;
import uz.uptimehub.resource.dto.resource.ResourceDto;
import uz.uptimehub.resource.dto.resource.ResourceFilter;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;
import uz.uptimehub.resourceapp.jpa.repository.ResourceRepository;
import uz.uptimehub.resourceapp.jpa.repository.ResourceTypeRepository;
import uz.uptimehub.resourceapp.mapper.ResourceMapper;

import java.util.UUID;

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
    public void update(ResourceDto dto) {

    }

    @Override
    public Page<ResourceDto> findAll(FilteredSortedPaginatedRequest<ResourceFilter, InvalidSortRule> filter) {
        return null;
    }

    public void organizationIdOverride(ResourceCreateRequest body, HttpServletRequest request) {
        String header = request.getHeader("X-Organization-Id");

        body.setOrganizationId(UUID.fromString(header));
    }

}
