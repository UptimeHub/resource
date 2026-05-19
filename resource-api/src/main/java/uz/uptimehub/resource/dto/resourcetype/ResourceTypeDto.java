package uz.uptimehub.resource.dto.resourcetype;

import uz.uptimehub.resource.dto.Status;

import java.util.List;

public record ResourceTypeDto(
        Long id,
        String name,
        String description,
        Status status,
        Long categoryId,
        List<SpecificationDefinition> specificationDefinitions
) {
}
