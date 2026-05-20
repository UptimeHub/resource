package uz.uptimehub.resource.dto.resourcetype;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import uz.uptimehub.core.pagination.Filter;
import uz.uptimehub.core.pagination.IdPropertyOverride;
import uz.uptimehub.core.pagination.SortPropertyOverride;
import uz.uptimehub.resource.dto.Status;

@Getter
@Setter
@Schema(description = "Resource type filter")
@IdPropertyOverride("rt.id")
public class ResourceTypeFilter extends Filter {
    @Schema(description = "Resource type id")
    private Long id;
    @Schema(description = "Resource type name")
    @SortPropertyOverride("rt.name")
    private String name;
    @Schema(description = "Resource type status")
    @SortPropertyOverride("rt.status")
    private Status status;
    @Schema(description = "Resource type category id")
    @SortPropertyOverride("c.id")
    private Long categoryId;
}
