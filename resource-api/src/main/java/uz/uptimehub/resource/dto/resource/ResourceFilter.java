package uz.uptimehub.resource.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uptimehub.core.pagination.Filter;
import uz.uptimehub.core.pagination.IdPropertyOverride;
import uz.uptimehub.core.pagination.SortPropertyOverride;

import java.util.UUID;

@Schema(description = "Filter criteria for querying resources")
@IdPropertyOverride("r.id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceFilter extends Filter {
    @Schema(description = "Resource ID to filter by a specific resource")
    private UUID id;
    @Schema(description = "Organization ID to filter resources by their organization")
    @SortPropertyOverride("r.organizationId")
    private UUID organizationId;
    @Schema(description = "Resource name to filter by a specific resource name")
    @SortPropertyOverride("r.name")
    private String name;
    @Schema(description = "Full-text search term for resource name, description, and searchable specifications")
    private String search;
    @Schema(description = "Resource type ID to filter resources by their type")
    @SortPropertyOverride("r.typeId")
    private Long resourceTypeId;
    @Schema(description = "Resource status to filter resources by their status")
    @SortPropertyOverride("r.status")
    private ResourceStatus status;
}
