package uz.uptimehub.resourceapp.mapper;

import org.mapstruct.*;
import uz.uptimehub.resource.dto.resourcetype.ResourceTypeCreateRequest;
import uz.uptimehub.resource.dto.resourcetype.ResourceTypeDto;
import uz.uptimehub.resourceapp.jpa.entity.category.Category;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;

@Mapper(componentModel = "spring")
public interface ResourceTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(uz.uptimehub.resource.dto.Status.NOT_PUBLISHED)")
    @Mapping(target = "category", expression = "java(category)")
    @Mapping(target = "name", expression = "java(request.name())")
    @Mapping(target = "description", expression = "java(request.description())")
    ResourceType toEntity(ResourceTypeCreateRequest request, Category category);

    @Mapping(target = "categoryId", source = "category.id")
    ResourceTypeDto toDto(ResourceType resourceType);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    void updateFromDto(ResourceTypeDto resourceTypeDto, @MappingTarget ResourceType resourceType);
}
