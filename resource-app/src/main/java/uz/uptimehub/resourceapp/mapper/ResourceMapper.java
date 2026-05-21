package uz.uptimehub.resourceapp.mapper;

import org.mapstruct.*;
import uz.uptimehub.resource.dto.resource.DetailedResourceResponse;
import uz.uptimehub.resource.dto.resource.ResourceCreateRequest;
import uz.uptimehub.resource.dto.resource.ResourceDto;
import uz.uptimehub.resourceapp.jpa.entity.resource.Resource;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ResourceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(uz.uptimehub.resource.dto.resource.ResourceStatus.NOT_PUBLISHED)")
    @Mapping(target = "name", expression = "java(request.name())")
    @Mapping(target = "description", expression = "java(request.description())")
    @Mapping(target = "resourceType", expression = "java(resourceType)")
    Resource toEntity(ResourceCreateRequest request, ResourceType resourceType);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "resourceType", ignore = true)
    Resource updateFromDto(ResourceDto resourceDto, @MappingTarget Resource resource);

    @Mapping(target = "resourceTypeId", source = "resourceType.id")
    ResourceDto toDto(Resource resource);

    @Mapping(target = "resourceTypeId", source = "resourceType.id")
    DetailedResourceResponse toDetailedDto(Resource resource);
}
