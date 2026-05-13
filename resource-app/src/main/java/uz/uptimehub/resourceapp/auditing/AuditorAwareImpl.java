package uz.uptimehub.resourceapp.auditing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<UUID> {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    @NonNull
    public Optional<UUID> getCurrentAuditor() {

        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return Optional.empty();
        }

        HttpServletRequest request = attributes.getRequest();

        String userId = request.getHeader(USER_ID_HEADER);

        if (!StringUtils.hasText(userId)) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(userId));
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", userId);
            log.error("Exception: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
