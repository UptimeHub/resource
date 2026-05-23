package uz.uptimehub.resource.dto.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uz.uptimehub.resource.dto.resource.ResourceDto;

import java.util.UUID;

@FeignClient(name = "resource-service", path = "/api/resource")
public interface ResourceClient {

    @GetMapping("/{resourceId}")
    ResourceDto getById(@PathVariable UUID resourceId);

}
