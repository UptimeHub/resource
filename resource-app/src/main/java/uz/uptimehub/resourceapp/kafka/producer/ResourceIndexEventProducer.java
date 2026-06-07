package uz.uptimehub.resourceapp.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uz.uptimehub.resource.dto.kafka.dto.ResourceIndexEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceIndexEventProducer {

    private final KafkaTemplate<String, ResourceIndexEvent> kafkaTemplate;

    @Value("${app.kafka.topics.resource-index}")
    private String topic;

    public void publish(ResourceIndexEvent event) {
        log.info("Publishing ResourceIndexEvent: {}", event);
        kafkaTemplate.send(topic, event.resourceId().toString(), event);
    }

}
