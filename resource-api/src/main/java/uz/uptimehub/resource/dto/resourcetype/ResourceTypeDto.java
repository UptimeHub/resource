package uz.uptimehub.resource.dto.resourcetype;

import uz.uptimehub.resource.dto.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ResourceTypeDto(
        Long id,
        String name,
        String description,
        Status status,
        Long categoryId,
        List<SpecificationDefinition> specificationDefinitions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID createdBy,
        UUID updatedBy
) {
}
