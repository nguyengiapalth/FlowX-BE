package project.ii.flowx.module.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import project.ii.flowx.module.auth.entity.Role;
import project.ii.flowx.module.auth.dto.role.RoleRequest;
import project.ii.flowx.module.auth.dto.role.RoleResponse;

import java.util.List;

/**
 * Mapper interface for converting between Role entity and Role DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toRoleResponse(Role role);

    Role toRole(RoleRequest roleRequest);

    void updateRoleFromRequest(@MappingTarget Role role, RoleRequest roleRequest);

    List<RoleResponse> toRoleResponseList(List<Role> roles);
}