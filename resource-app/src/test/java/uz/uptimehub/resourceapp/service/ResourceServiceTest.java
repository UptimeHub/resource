package uz.uptimehub.resourceapp.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uptimehub.resource.dto.resource.ResourceDto;
import uz.uptimehub.resource.dto.resource.ResourceFilter;
import uz.uptimehub.resource.dto.resource.ResourceStatus;
import uz.uptimehub.resource.dto.resourcetype.SpecificationDefinition;
import uz.uptimehub.resourceapp.jpa.entity.resource.Resource;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;
import uz.uptimehub.resourceapp.jpa.repository.ResourceRepository;
import uz.uptimehub.resourceapp.jpa.repository.ResourceTypeRepository;
import uz.uptimehub.resourceapp.kafka.producer.ResourceIndexEventProducer;
import uz.uptimehub.resourceapp.mapper.ResourceMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResourceServiceTest {

    private ResourceRepository resourceRepository;
    private ResourceMapper resourceMapper;
    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceRepository = mock(ResourceRepository.class);
        resourceMapper = mock(ResourceMapper.class);
        resourceService = new ResourceService(
                resourceRepository,
                mock(ResourceTypeRepository.class),
                resourceMapper,
                mock(ResourceIndexEventProducer.class),
                mock(ResourceSearchService.class)
        );
        resourceService.permissionsHeader = "X-Auth-Permissions";
    }

    @Test
    void updateUsesExistingSpecificationValuesWhenPatchOmitsThem() {
        UUID resourceId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();

        ResourceType resourceType = new ResourceType();
        resourceType.setSpecificationDefinitions(List.of(
                new SpecificationDefinition(
                        "capacity",
                        "Capacity",
                        true,
                        false,
                        false,
                        SpecificationDefinition.DataType.NUMBER
                )
        ));

        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setOrganizationId(organizationId);
        resource.setResourceType(resourceType);
        resource.setSpecificationValues(Map.of("capacity", 20));

        ResourceDto patch = new ResourceDto();
        patch.setId(resourceId);
        patch.setOrganizationId(organizationId);
        patch.setStatus(ResourceStatus.PUBLISHED);

        when(resourceRepository.findByIdAndOrganizationId(resourceId, organizationId)).thenReturn(Optional.of(resource));
        when(resourceMapper.updateFromDto(patch, resource)).thenReturn(resource);

        assertDoesNotThrow(() -> resourceService.update(patch));

        verify(resourceRepository).save(resource);
    }

    @Test
    void statusOverrideConstrainsResourceManagersToTheirOrganizationWithoutForcingPublishedStatus() {
        UUID organizationId = UUID.randomUUID();
        ResourceFilter filter = new ResourceFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getHeader("X-Auth-Permissions")).thenReturn("resource:manage");
        when(request.getHeader("X-Organization-Id")).thenReturn(organizationId.toString());

        resourceService.statusOverride(request, filter);

        assertEquals(organizationId, filter.getOrganizationId());
        assertNull(filter.getStatus());
    }

    @Test
    void statusOverrideConstrainsPublicCallersToPublishedStatus() {
        ResourceFilter filter = new ResourceFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);

        resourceService.statusOverride(request, filter);

        assertEquals(ResourceStatus.PUBLISHED, filter.getStatus());
    }
}
