package project.ii.flowx.applications.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.service.communicate.UserPresenceService;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserPresenceCleanupJob {
    private final UserPresenceService presenceService;
    
    // Run cleanup every 2 minutes
    @Scheduled(fixedRate = 120000)
    public void cleanupExpiredUsers() {
        try {
            log.debug("Running user presence cleanup job");
            presenceService.cleanupExpiredUsers();
        } catch (Exception e) {
            log.error("Error during user presence cleanup: {}", e.getMessage());
        }
    }
} 