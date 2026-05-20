package uz.uptimehub.resource.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@Schema(description = "Request object for creating a new resource")
public record ResourceCreateRequest(
        @Schema(description = "Name of the resource")
        @NotBlank(message = "Name is required")
        String name,
        @Schema(description = "Description of the resource")
        String description,
        @NotBlank(message = "Resource type ID is required")
        Long resourceTypeId,
        @Schema(description = "Custom characteristics of the resource")
        Map<String, String> customCharacteristics,
        @Schema(description = "Specification values of the resource. Key defines the name of a specification while value defines the value of that specification. The keys must match the specification definitions of the resource type.")
        Map<String, Object> specificationValues
) {
}
