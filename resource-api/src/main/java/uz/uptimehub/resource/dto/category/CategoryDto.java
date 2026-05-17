package uz.uptimehub.resource.dto.category;

import jakarta.validation.constraints.NotNull;
import uz.uptimehub.resource.dto.Status;

public record CategoryDto (
        @NotNull(message = "Id is required")
        Long id,
        String name,
        String description,
        Status status
){
}
