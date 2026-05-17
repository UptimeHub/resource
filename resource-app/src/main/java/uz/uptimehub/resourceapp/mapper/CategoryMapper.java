package uz.uptimehub.resourceapp.mapper;

import org.mapstruct.*;
import uz.uptimehub.resource.dto.category.CategoryCreateRequest;
import uz.uptimehub.resource.dto.category.CategoryDto;
import uz.uptimehub.resourceapp.jpa.entity.category.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", defaultExpression = "java(uz.uptimehub.resource.dto.Status.NOT_PUBLISHED)")
    Category fromRequest(CategoryCreateRequest categoryCreateRequest);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CategoryDto categoryDto, @MappingTarget Category category);

    CategoryDto toDto(Category category);
}
