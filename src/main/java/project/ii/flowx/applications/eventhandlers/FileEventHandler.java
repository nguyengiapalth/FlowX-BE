package project.ii.flowx.applications.eventhandlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.FileEvent;
import project.ii.flowx.module.manage.service.TaskService;
import project.ii.flowx.applications.enums.FileTargetType;

import java.util.UUID;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileEventHandler {
    
    TaskService taskService;
    
    @EventListener
    public void handleFileUploadedEvent(FileEvent.FileUploadedEvent event) {
        log.info("File uploaded event received: {}", event);
        
        try {
            syncHasFileFlag(event.targetId(), event.targetType(), event.fileId());
            log.info("HasFile flag synchronized synchronously after file upload for entity {} (type: {})", 
                    event.targetId(), event.targetType());
        } catch (Exception e) {
            log.error("Error synchronizing hasFile flag after file upload: {}", e.getMessage(), e);
        }
    }
    
    @EventListener
    public void handleFileDeletedEvent(FileEvent.FileDeletedEvent event) {
        log.info("File deleted event received: {}", event);
        
        try {
            syncHasFileFlag(event.targetId(), event.targetType(), null); // No fileId needed for deletion
            log.info("HasFile flag synchronized synchronously after file deletion for entity {} (type: {})", 
                    event.targetId(), event.targetType());
        } catch (Exception e) {
            log.error("Error synchronizing hasFile flag after file deletion: {}", e.getMessage(), e);
        }
    }
    
    @EventListener
    @Async
    public void handleFileAccessedEvent(FileEvent.FileAccessedEvent event) {
        log.debug("File accessed event received: {}", event);
        // Could be used for analytics or audit logging
    }

    
    /**
     * Synchronizes the hasFile flag for the specified entity
     *
     * @param targetId   The ID of the entity
     * @param targetType The type of the entity (CONTENT, TASK, etc.)
     */
    private void syncHasFileFlag(UUID targetId, FileTargetType targetType, UUID fileId) {
        if (targetId == null || targetType == null) {
            log.warn("Cannot sync hasFile flag: entityId or entityType is null");
            return;
        }
        
        try {

            switch (targetType) {
                case POST -> {
                    log.debug("Updated hasFile flag for content {}", targetId);
                }
                case COMMENT -> {
                    log.debug("Updated hasFile flag for comment {}", targetId);
                }
                case MESSAGE -> {
                    log.debug("Updated hasFile flag for message {}", targetId);
                }
                case TASK -> {
                    taskService.updateHasFileFlag(targetId);
                    log.debug("Updated hasFile flag for task {}", targetId);
                }
                default -> {
                    log.warn("Unknown entity type for hasFile flag sync: {}", targetType);
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid entity type: {}", targetType, e);
        } catch (Exception e) {
            log.error("Error updating hasFile flag for entity {} (type: {}): {}",
                    targetId, targetType, e.getMessage(), e);
        }
    }
}