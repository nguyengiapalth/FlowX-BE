package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.entity.UserActivityLog;
import project.ii.flowx.model.repository.UserActivityLogRepository;
import project.ii.flowx.model.dto.useractivitylog.UserActivityLogCreateRequest;
import project.ii.flowx.model.dto.useractivitylog.UserActivityLogResponse;
import project.ii.flowx.model.mapper.UserActivityLogMapper;
import project.ii.flowx.security.UserPrincipal;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActivityLogService {
    UserActivityLogRepository userActivityLogRepository;
    UserActivityLogMapper userActivityLogMapper;

    @Transactional
    public UserActivityLogResponse logActivity(UserActivityLogCreateRequest logCreateRequest) {
        UserActivityLog activityLog = userActivityLogMapper.toUserActivityLog(logCreateRequest);
        activityLog = userActivityLogRepository.save(activityLog);
        return userActivityLogMapper.toUserActivityLogResponse(activityLog);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public Map<User, List<UserActivityLog>> getAllActivityLogs() {
        List<UserActivityLog> activityLogs = userActivityLogRepository.findAll();
        // sort by timestamp
        activityLogs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        List<UserActivityLogResponse> activityLogResponses = userActivityLogMapper.toUserActivityLogResponseList(activityLogs);

        return activityLogs.stream()
                .collect(Collectors.groupingBy(UserActivityLog::getUser));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') " +
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


        List<UserActivityLog> activityLogs = userActivityLogRepository.findByUserId(userId);
        return userActivityLogMapper.toUserActivityLogResponseList(activityLogs);
    }
}
