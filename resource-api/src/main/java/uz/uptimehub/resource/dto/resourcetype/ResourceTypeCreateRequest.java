package uz.uptimehub.resource.dto.resourcetype;

import java.util.List;

public record ResourceTypeCreateRequest(
        String name,
        String description,
        Long categoryId,
        List<SpecificationDefinition> specificationDefinitions
) {
}
