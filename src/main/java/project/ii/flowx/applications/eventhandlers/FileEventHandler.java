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
import project.ii.flowx.applications.service.communicate.ContentService;
import project.ii.flowx.applications.service.communicate.TaskService;
import project.ii.flowx.shared.enums.FileTargetType;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileEventHandler {
    
    ContentService contentService;
    TaskService taskService;
    
    @EventListener
    public void handleFileUploadedEvent(FileEvent.FileUploadedEvent event) {
        log.info("File uploaded event received: {}", event);
        
        try {
            syncHasFileFlag(event.entityId(), event.entityType(), event.fileId());
            log.info("HasFile flag synchronized synchronously after file upload for entity {} (type: {})", 
                    event.entityId(), event.entityType());
        } catch (Exception e) {
            log.error("Error synchronizing hasFile flag after file upload: {}", e.getMessage(), e);
        }
    }
    
    @EventListener
    public void handleFileDeletedEvent(FileEvent.FileDeletedEvent event) {
        log.info("File deleted event received: {}", event);
        
        try {
            syncHasFileFlag(event.entityId(), event.entityType(), null); // No fileId needed for deletion
            log.info("HasFile flag synchronized synchronously after file deletion for entity {} (type: {})", 
                    event.entityId(), event.entityType());
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
     * @param entityId The ID of the entity
     * @param entityType The type of the entity (CONTENT, TASK, etc.)
     */
    private void syncHasFileFlag(Long entityId, String entityType, Long fileId) {
        if (entityId == null || entityType == null) {
            log.warn("Cannot sync hasFile flag: entityId or entityType is null");
            return;
        }
        
        try {
            FileTargetType targetType = FileTargetType.valueOf(entityType);
            
            switch (targetType) {
                case CONTENT -> {
                    contentService.updateHasFileFlag(entityId, fileId);
                    log.debug("Updated hasFile flag for content {}", entityId);
                }
                case TASK -> {
                    taskService.updateHasFileFlag(entityId);
                    log.debug("Updated hasFile flag for task {}", entityId);
                }
                default -> {
                    log.warn("Unknown entity type for hasFile flag sync: {}", entityType);
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid entity type: {}", entityType, e);
        } catch (Exception e) {
            log.error("Error updating hasFile flag for entity {} (type: {}): {}", 
                    entityId, entityType, e.getMessage(), e);
        }
    }
}