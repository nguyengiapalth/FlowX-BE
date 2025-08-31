package project.ii.flowx.module.notify;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.dto.Response;
import project.ii.flowx.dto.PageResponse;
import project.ii.flowx.module.notify.dto.NotificationResponse;

import java.util.UUID;

@RestController
@RequestMapping("api/notification")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Notification", description = "Notification API")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    NotificationService notificationService;

    @Operation(
            summary = "Get my notifications",
            description = "Retrieves a list of all notifications for the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of notifications retrieved successfully"
                    )
            }
    )
    @GetMapping("/my-notifications")
    public Response<PageResponse<NotificationResponse>> getMyNotifications(@RequestParam int page) {
        log.info("Retrieving notifications for the current user");
        return Response.<PageResponse<NotificationResponse>>builder()
                .data(notificationService.getMyNotifications(page))
                .message("Notifications retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Marks a specific notification as read.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Notification marked as read successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Notification not found"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied"
                    )
            }
    )
    @PutMapping("/{id}/mark-read")
    public Response<Void> markAsRead(@PathVariable UUID id) {
        log.info("Marking notification as read - ID: {}", id);
        notificationService.markAsRead(id);
        return Response.<Void>builder()
                .message("Notification marked as read successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Mark notification as unread",
            description = "Marks a specific notification as unread.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Notification marked as unread successfully"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Notification not found"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Access denied"
                    )
            }
    )
    @PutMapping("/{id}/mark-unread")
    public Response<Void> markAsUnread(@PathVariable UUID id) {
        log.info("Marking notification as unread - ID: {}", id);
        notificationService.markAsUnread(id);
        return Response.<Void>builder()
                .message("Notification marked as unread successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all notifications for the current user as read.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "All notifications marked as read successfully"
                    )
            }
    )
    @PutMapping("/mark-all-read")
    public Response<Void> markAllAsRead() {
        log.info("Marking all notifications as read for current user");
        notificationService.markAllAsRead();
        return Response.<Void>builder()
                .message("All notifications marked as read successfully")
                .code(200)
                .build();
    }
}
