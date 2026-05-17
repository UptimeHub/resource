package uz.uptimehub.resource.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import uz.uptimehub.core.pagination.Filter;
import uz.uptimehub.core.pagination.IdPropertyOverride;
import uz.uptimehub.core.pagination.SortPropertyOverride;
import uz.uptimehub.resource.dto.Status;

@Getter
@Setter
@Schema(description = "Category filter")
@IdPropertyOverride("c.id")
public class CategoryFilter extends Filter {
    @Schema(description = "Category id")
    private Long id;
    @Schema(description = "Category name")
    @SortPropertyOverride("c.name")
    private String name;
    @Schema(description = "Category status")
    @SortPropertyOverride("c.status")
    private Status status;
}
