package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.model.entity.UserActivityLog;
import project.ii.flowx.model.repository.UserActivityLogRepository;
import project.ii.flowx.model.dto.useractivitylog.UserActivityLogCreateRequest;
import project.ii.flowx.model.dto.useractivitylog.UserActivityLogResponse;
import project.ii.flowx.model.mapper.UserActivityLogMapper;
import project.ii.flowx.security.UserPrincipal;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActivityLogService {
    UserActivityLogRepository userActivityLogRepository;
    UserActivityLogMapper userActivityLogMapper;

    /*
        * Logs a user activity.
     */
    @Transactional
    public UserActivityLogResponse logActivity(UserActivityLogCreateRequest logCreateRequest) {
        UserActivityLog activityLog = userActivityLogMapper.toUserActivityLog(logCreateRequest);
        activityLog = userActivityLogRepository.save(activityLog);
        return userActivityLogMapper.toUserActivityLogResponse(activityLog);
    }
    
//    @Transactional
//    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
//    public void deleteActivityLog(Long id) {
//        userActivityLogRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Activity log not found"));
//        userActivityLogRepository.deleteById(id);
//    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public List<UserActivityLogResponse> getAllActivityLogs() {
        List<UserActivityLog> activityLogs = userActivityLogRepository.findAll();
        return userActivityLogMapper.toUserActivityLogResponseList(activityLogs);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR') " +
            "or userId == authentication.principal.id")
    public List<UserActivityLogResponse> getActivityLogsByUserId(Long userId) {
        List<UserActivityLog> activityLogs = userActivityLogRepository.findByUserId(userId);
        return userActivityLogMapper.toUserActivityLogResponseList(activityLogs);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<UserActivityLogResponse> getAllActivityLogsForCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("Fetching activity logs for user ID: {}", userId);

        List<UserActivityLog> activityLogs = userActivityLogRepository.findByUserId(userId);
        return userActivityLogMapper.toUserActivityLogResponseList(activityLogs);
    }
}
