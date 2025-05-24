package project.ii.flowx.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.applications.service.manage.UserActivityLogService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.useractivitylog.UserActivityLogResponse;

import java.util.List;

@RestController
@RequestMapping("api/activity-log")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "User Activity Log", description = "User activity tracking API")
@SecurityRequirement(name = "bearerAuth")
public class UserActivityLogController {
    UserActivityLogService userActivityLogService;

    @Operation(
            summary = "Get all activity logs",
            description = "Retrieves all user activity logs in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Activity logs retrieved successfully"
                    )
            }
    )
    @GetMapping("/get-all")
    public FlowXResponse<List<UserActivityLogResponse>> getAllActivityLogs() {
        return FlowXResponse.<List<UserActivityLogResponse>>builder()
                .data(userActivityLogService.getAllActivityLogs())
                .message("Activity logs retrieved successfully")
                .code(200)
                .build();
    }


    @GetMapping("/{userId}")
    public FlowXResponse<List<UserActivityLogResponse>> getAllActivityLogsForUserId(@PathVariable Long userId) {
        return FlowXResponse.<List<UserActivityLogResponse>>builder()
                .data(userActivityLogService.getActivityLogsByUserId(userId))
                .message("Activity logs retrieved successfully")
                .code(200)
                .build();
    }

    @GetMapping("/me")
    public FlowXResponse<List<UserActivityLogResponse>> getAllActivityLogsForCurrentUser() {
        return FlowXResponse.<List<UserActivityLogResponse>>builder()
                .data(userActivityLogService.getAllActivityLogsForCurrentUser())
                .message("Activity logs retrieved successfully")
                .code(200)
                .build();
    }
}
