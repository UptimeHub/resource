package uz.uptimehub.resourceapp.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uz.uptimehub.core.exception.InvalidSortRule;
import uz.uptimehub.core.pagination.FilteredSortedPaginatedRequest;
import uz.uptimehub.resource.dto.resourcetype.ResourceTypeCreateRequest;
import uz.uptimehub.resource.dto.resourcetype.ResourceTypeDto;
import uz.uptimehub.resource.dto.resourcetype.ResourceTypeFilter;
import uz.uptimehub.resourceapp.service.ResourceTypeService;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/resource/type")
@RequiredArgsConstructor
public class ResourceTypeController {

    private final ResourceTypeService resourceTypeService;

    @PostMapping
    public ResourceTypeDto create(@Valid ResourceTypeCreateRequest request) {
        return resourceTypeService.create(request);
    }

    @PatchMapping
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void update(@Valid ResourceTypeDto request) {
        resourceTypeService.update(request);
    }

    @GetMapping("/filters")
    public Map<String, Set<String>> getFilters(HttpServletRequest request) {
        return resourceTypeService.getFiltersMap(request);
    }

    @GetMapping
    public Page<ResourceTypeDto> findAll(
            @Parameter(description = "Filter criteria")
            @ParameterObject
            ResourceTypeFilter filter,
            @ParameterObject
            Pageable pageable,
            HttpServletRequest request
    ) {
        resourceTypeService.statusOverride(request, filter);
        return resourceTypeService.findAll(new FilteredSortedPaginatedRequest<>(filter, pageable, InvalidSortRule::new));
    }

}
