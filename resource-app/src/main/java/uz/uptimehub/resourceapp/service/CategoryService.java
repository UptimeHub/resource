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
import uz.uptimehub.resource.dto.category.CategoryCreateRequest;
import uz.uptimehub.resource.dto.category.CategoryDto;
import uz.uptimehub.resource.dto.category.CategoryFilter;
import uz.uptimehub.resourceapp.jpa.entity.category.Category;
import uz.uptimehub.resourceapp.jpa.repository.CategoryRepository;
import uz.uptimehub.resourceapp.mapper.CategoryMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryService extends CommonService<CategoryCreateRequest, CategoryDto, CategoryFilter>{



    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public CategoryDto create(CategoryCreateRequest categoryCreateRequest) {
        return categoryMapper.toDto(categoryRepository.save(categoryMapper.fromRequest(categoryCreateRequest)));
    }

    @Override
    public void update(CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryDto.id())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryDto.id()));

        categoryMapper.updateFromDto(categoryDto, category);

        categoryRepository.save(category);
    }

    @Override
    public Page<CategoryDto> findAll(FilteredSortedPaginatedRequest<CategoryFilter, InvalidSortRule> request) {
        return categoryRepository.findAllFiltered(
                request.getFilter().getId(),
                request.getFilter().getName(),
                request.getFilter().getStatus(),
                request.getPageable()
        ).map(categoryMapper::toDto);

    }

    public Map<String, Set<String>> getFiltersMap(HttpServletRequest request) {
        String[] permissions = AuthHeaderUtils.extractRolesOrPermissions(request.getHeader(permissionsHeader));
        Status status = null;

        Set<String> statuses = new HashSet<>();

        if (Arrays.asList(permissions).contains("resource-category:manage")) {
            statuses.addAll(Arrays.stream(Status.values()).map(Enum::name).toList());
        } else {
            statuses.add(Status.PUBLISHED.name());
            status = Status.PUBLISHED;
        }

        return Map.of(
                "name", categoryRepository.findAllNames(status),
                "status", statuses
        );
    }

    public void statusOverride(HttpServletRequest request, CategoryFilter filter) {
        String[] permissions = AuthHeaderUtils.extractRolesOrPermissions(request.getHeader(permissionsHeader));

        if (!Arrays.asList(permissions).contains("resource-category:manage")) {
            filter.setStatus(Status.PUBLISHED);
        }
    }

}
