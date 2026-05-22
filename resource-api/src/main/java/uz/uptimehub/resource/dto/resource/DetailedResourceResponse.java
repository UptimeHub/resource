package uz.uptimehub.resource.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Detailed response object for a resource, including audit information")
public class DetailedResourceResponse extends ResourceDto {
    @Schema(description = "Timestamp when the resource was created", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;
    @Schema(description = "Timestamp when the resource was last updated", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;
    @Schema(description = "ID of the user who created the resource")
    private UUID createdBy;
    @Schema(description = "ID of the user who last updated the resource")
    private UUID updatedBy;
}
