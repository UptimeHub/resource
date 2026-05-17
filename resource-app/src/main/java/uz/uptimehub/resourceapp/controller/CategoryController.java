package uz.uptimehub.resourceapp.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uz.uptimehub.core.exception.InvalidSortRule;
import uz.uptimehub.core.pagination.FilteredSortedPaginatedRequest;
import uz.uptimehub.resource.dto.category.CategoryCreateRequest;
import uz.uptimehub.resource.dto.category.CategoryDto;
import uz.uptimehub.resource.dto.category.CategoryFilter;
import uz.uptimehub.resourceapp.service.CategoryService;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/resource/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public CategoryDto createCategory(CategoryCreateRequest request) {
        return categoryService.create(request);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategory(CategoryDto request) {
        categoryService.update(request);
    }

    @GetMapping("/filters")
    public Map<String, Set<String>> getFilters(HttpServletRequest request) {
        return categoryService.getFiltersMap(request);
    }

    @GetMapping
    public Page<CategoryDto> findAll(
        @Parameter(description = "Filter criteria")
        @ParameterObject
        CategoryFilter filter,
        @ParameterObject
        Pageable pageable,
        HttpServletRequest request
    ) {
        categoryService.statusOverride(request, filter);
        return categoryService.findAll(new FilteredSortedPaginatedRequest<>(filter, pageable, InvalidSortRule::new));
    }


}
