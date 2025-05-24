package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.ii.flowx.model.entity.UserActivityLog;
import project.ii.flowx.model.dto.useractivitylog.UserActivityLogCreateRequest;
import project.ii.flowx.model.dto.useractivitylog.UserActivityLogResponse;

import java.util.List;

/**
 * Mapper interface for converting between UserActivityLog entity and UserActivityLog DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface UserActivityLogMapper {

    UserActivityLogResponse toUserActivityLogResponse(UserActivityLog userActivityLog);

    @Mapping(target = "user.id", source = "userId")
    UserActivityLog toUserActivityLog(UserActivityLogCreateRequest userActivityLogCreateRequest);

    List<UserActivityLogResponse> toUserActivityLogResponseList(List<UserActivityLog> userActivityLogs);
}