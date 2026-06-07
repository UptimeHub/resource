package uz.uptimehub.resourceapp.mapper;

import org.mapstruct.*;
import uz.uptimehub.resource.dto.resource.DetailedResourceResponse;
import uz.uptimehub.resource.dto.resource.ResourceCreateRequest;
import uz.uptimehub.resource.dto.resource.ResourceDto;
import uz.uptimehub.resource.dto.resourcetype.SpecificationDefinition;
import uz.uptimehub.resourceapp.jpa.document.ResourceDocument;
import uz.uptimehub.resourceapp.jpa.entity.resource.Resource;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ResourceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(uz.uptimehub.resource.dto.resource.ResourceStatus.NOT_PUBLISHED)")
    @Mapping(target = "name", expression = "java(request.getName())")
    @Mapping(target = "description", expression = "java(request.getDescription())")
    @Mapping(target = "resourceType", expression = "java(resourceType)")
    Resource toEntity(ResourceCreateRequest request, ResourceType resourceType);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "resourceType", ignore = true)
    Resource updateFromDto(ResourceDto resourceDto, @MappingTarget Resource resource);

    @Mapping(target = "resourceTypeId", source = "resourceType.id")
    ResourceDto toDto(Resource resource);

    @Mapping(target = "resourceTypeId", source = "resourceType.id")
    DetailedResourceResponse toDetailedDto(Resource resource);

    default ResourceDocument toDocument(Resource resource) {
        if (resource == null) {
            return null;
        }

        ResourceType resourceType = resource.getResourceType();
        Long resourceTypeId = resourceType == null ? null : resourceType.getId();

        return ResourceDocument.builder()
                .id(resource.getId().toString())
                .resourceId(resource.getId())
                .organizationId(resource.getOrganizationId())
                .name(resource.getName())
                .description(resource.getDescription())
                .resourceTypeId(resourceTypeId)
                .status(resource.getStatus() == null ? null : resource.getStatus().name())
                .customCharacteristics(resource.getCustomCharacteristics())
                .specificationValues(resource.getSpecificationValues())
                .searchableText(buildSearchableText(resource))
                .indexedAt(Instant.now())
                .build();
    }

    default ResourceDto toDto(ResourceDocument document) {
        if (document == null) {
            return null;
        }

        return new ResourceDto(
                document.getResourceId(),
                document.getOrganizationId(),
                document.getName(),
                document.getDescription(),
                document.getStatus() == null ? null : uz.uptimehub.resource.dto.resource.ResourceStatus.valueOf(document.getStatus()),
                document.getResourceTypeId(),
                document.getCustomCharacteristics(),
                document.getSpecificationValues()
        );
    }

    private String buildSearchableText(Resource resource) {
        StringBuilder searchableText = new StringBuilder();
        append(searchableText, resource.getName());
        append(searchableText, resource.getDescription());

        if (resource.getCustomCharacteristics() != null) {
            resource.getCustomCharacteristics().forEach((key, value) -> {
                append(searchableText, key);
                append(searchableText, value);
            });
        }

        Map<String, Object> specificationValues = resource.getSpecificationValues();
        List<SpecificationDefinition> definitions = resource.getResourceType() == null
                ? List.of()
                : resource.getResourceType().getSpecificationDefinitions();

        if (specificationValues != null && definitions != null) {
            definitions.stream()
                    .filter(definition -> Boolean.TRUE.equals(definition.getSearchable()))
                    .map(SpecificationDefinition::getName)
                    .filter(specificationValues::containsKey)
                    .map(specificationValues::get)
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .forEach(value -> append(searchableText, value));
        }

        return searchableText.toString().trim();
    }

    private void append(StringBuilder builder, String value) {
        if (value != null && !value.isBlank()) {
            builder.append(value).append(' ');
        }
    }
}
