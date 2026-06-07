package uz.uptimehub.resource.dto.kafka.dto;

import java.util.UUID;

public record ResourceIndexEvent(
        UUID resourceId,
        IndexType indexType
) {
}
