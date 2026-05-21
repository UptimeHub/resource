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
import uz.uptimehub.resource.dto.resourcetype.ResourceTypeCreateRequest;
import uz.uptimehub.resource.dto.resourcetype.ResourceTypeDto;
import uz.uptimehub.resource.dto.resourcetype.ResourceTypeFilter;
import uz.uptimehub.resourceapp.jpa.entity.category.Category;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;
import uz.uptimehub.resourceapp.jpa.repository.CategoryRepository;
import uz.uptimehub.resourceapp.jpa.repository.ResourceTypeRepository;
import uz.uptimehub.resourceapp.mapper.ResourceTypeMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResourceTypeService extends CommonService<ResourceTypeCreateRequest, ResourceTypeDto, ResourceTypeFilter>{

    private final CategoryRepository categoryRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ResourceTypeMapper resourceTypeMapper;

    @Override
    public ResourceTypeDto create(ResourceTypeCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.categoryId()));

        return resourceTypeMapper.toDto(resourceTypeRepository.save(resourceTypeMapper.toEntity(request, category)));
    }

    @Override
    public void update(ResourceTypeDto resourceTypeDto) {
        ResourceType type = resourceTypeRepository.findById(resourceTypeDto.id())
                .orElseThrow(() -> new EntityNotFoundException("Resource type not found with id: " + resourceTypeDto.id()));

        resourceTypeMapper.updateFromDto(resourceTypeDto, type);

        if (resourceTypeDto.categoryId() != null && !resourceTypeDto.categoryId().equals(type.getCategory().getId())) {
            Category category = categoryRepository.findById(resourceTypeDto.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + resourceTypeDto.categoryId()));
            type.setCategory(category);
        }

        resourceTypeRepository.save(type);
    }

    @Override
    public Page<ResourceTypeDto> findAll(FilteredSortedPaginatedRequest<ResourceTypeFilter, InvalidSortRule> filter) {
        return resourceTypeRepository.findAllFiltered(
                filter.getFilter().getId(),
                filter.getFilter().getName(),
                filter.getFilter().getStatus(),
                filter.getFilter().getCategoryId(),
                filter.getPageable()
        ).map(resourceTypeMapper::toDto);
    }

    public Map<String, Set<String>> getFiltersMap(HttpServletRequest request) {
        String[] permissions = AuthHeaderUtils.extractRolesOrPermissions(request.getHeader(permissionsHeader));
        Status status = null;

        Set<String> statuses = new HashSet<>();

        if (Arrays.asList(permissions).contains("resource-type:manage")) {
            statuses.addAll(Arrays.stream(Status.values()).map(Enum::name).toList());
        } else {
            statuses.add(Status.PUBLISHED.name());
            status = Status.PUBLISHED;
        }

        return Map.of(
                "name", resourceTypeRepository.findAllNames(status),
                "status", statuses
        );
    }

    public void statusOverride(HttpServletRequest request, ResourceTypeFilter filter) {
        String[] permissions = AuthHeaderUtils.extractRolesOrPermissions(request.getHeader(permissionsHeader));

        if (!Arrays.asList(permissions).contains("resource-type:manage")) {
            filter.setStatus(Status.PUBLISHED);
        }
    }
}
