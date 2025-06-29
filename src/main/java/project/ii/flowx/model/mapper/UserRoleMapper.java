package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.ii.flowx.model.entity.UserRole;
import project.ii.flowx.model.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.model.dto.userrole.UserRoleResponse;

import java.util.List;

/**
 * Mapper interface for converting between UserRole entity and UserRole DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    UserRoleResponse toUserRoleResponse(UserRole userRole);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "role.id", source = "roleId")
    UserRole toUserRole(UserRoleCreateRequest userRoleCreateRequest);

    List<UserRoleResponse> toUserRoleResponseList(List<UserRole> userRoles);
}