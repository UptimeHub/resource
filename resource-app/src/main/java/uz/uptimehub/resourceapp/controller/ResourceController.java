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
import uz.uptimehub.resource.dto.resource.DetailedResourceResponse;
import uz.uptimehub.resource.dto.resource.ResourceCreateRequest;
import uz.uptimehub.resource.dto.resource.ResourceDto;
import uz.uptimehub.resource.dto.resource.ResourceFilter;
import uz.uptimehub.resourceapp.service.ResourceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public DetailedResourceResponse createResource(@Valid @RequestBody ResourceCreateRequest body, HttpServletRequest request) {
        resourceService.organizationIdOverride(body, request);
        return resourceService.create(body);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Valid @RequestBody ResourceDto data, HttpServletRequest request) {
        resourceService.organizationIdOverride(data, request);
        resourceService.update(data);
    }

    @GetMapping
    public Page<ResourceDto> findAllFiltered(
            @Parameter(description = "Filter criteria")
            @ParameterObject
            ResourceFilter filter,
            @ParameterObject
            Pageable pageable,
            HttpServletRequest request
    ) {
        resourceService.statusOverride(request, filter);
        return resourceService.findAll(new FilteredSortedPaginatedRequest<>(filter, pageable, InvalidSortRule::new));
    }
}
