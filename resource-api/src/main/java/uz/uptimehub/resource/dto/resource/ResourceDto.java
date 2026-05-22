package uz.uptimehub.resource.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data transfer object representing a resource with its basic information")
public class ResourceDto {
    @Schema(description = "ID of the Resource")
    UUID id;
    @Schema(description = "ID of the organization that owns the resource")
    UUID organizationId;
    @Schema(description = "Name of the resource")
    String name;
    @Schema(description = "Description of the resource")
    String description;
    @Schema(description = "Status of the resource")
    ResourceStatus status;
    @Schema(description = "ID of the type of the resource")
    Long resourceTypeId;
    @Schema(description = "Custom characteristics of the resource. Key defines the name of a characteristic while value defines the value of that characteristic.")
    Map<String, String> customCharacteristics;
    @Schema(description = "Specification values of the resource")
    Map<String, Object> specificationValues;
}
