package uz.uptimehub.resourceapp.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uz.uptimehub.resource.dto.kafka.dto.ResourceIndexEvent;
import uz.uptimehub.resourceapp.service.ResourceIndexService;


@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceIndexEventListener {

    private final ResourceIndexService resourceIndexService;

    @KafkaListener(
            topics = "${app.kafka.topics.resource-index}",
            concurrency = "${app.kafka.concurrency}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ResourceIndexEvent event) {
        log.info("Received ResourceIndexEvent: {}", event);
        switch (event.indexType()) {
            case CREATED, UPDATED -> resourceIndexService.indexById(event.resourceId());
            case DELETED -> resourceIndexService.deleteById(event.resourceId());
        }
    }
}
