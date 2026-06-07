package uz.uptimehub.resourceapp.controller.internal;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uptimehub.resourceapp.service.ResourceIndexService;

import java.util.Map;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/resource")
public class InternalResourceController {

    private final ResourceIndexService resourceIndexService;

    @PostMapping("/reindex")
    public Map<String, Long> reindex() {
        return Map.of("indexedCount", resourceIndexService.reindexAll());
    }
}
