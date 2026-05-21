package uz.uptimehub.resource.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for creating a new resource")
public class ResourceCreateRequest {
        @Schema(description = "Name of the resource")
        @NotBlank(message = "Name is required")
        private String name;
        @Schema(description = "Description of the resource")
        private String description;
        @Schema(description = "ID of the resource type")
        @NotBlank(message = "Resource type ID is required")
        private Long resourceTypeId;
        @Schema(description = "ID of the organization to which the resource belongs")
        @NotNull(message = "Organization ID is required")
        private UUID organizationId;
        @Schema(description = "Custom characteristics of the resource")
        private Map<String, String> customCharacteristics;
        @Schema(description = "Specification values of the resource. Key defines the name of a specification while value defines the value of that specification. The keys must match the specification definitions of the resource type.")
        private Map<String, Object> specificationValues;
}
