package uz.uptimehub.resource.dto.resource;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceDto {
    UUID id;
    UUID organizationId;
    String name;
    String description;
    ResourceStatus status;
    Long resourceTypeId;
    Map<String, String> customCharacteristics;
    Map<String, Object> specificationValues;
}
