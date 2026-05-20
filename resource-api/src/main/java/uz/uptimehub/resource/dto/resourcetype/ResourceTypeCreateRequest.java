package uz.uptimehub.resource.dto.resourcetype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ResourceTypeCreateRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        @NotBlank(message = "Description must not be blank")
        String description,
        @NotNull(message = "Category ID must not be null")
        Long categoryId,
        @NotNull(message = "Specification definitions must not be null")
        List<SpecificationDefinition> specificationDefinitions
) {
}
