package project.ii.flowx.module.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.ii.flowx.module.auth.entity.UserRole;
import project.ii.flowx.module.auth.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.module.auth.dto.userrole.UserRoleResponse;

import java.util.List;

/**
 * Mapper interface for converting between UserRole entity and UserRole DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    /**
     * Convert UserRole entity to UserRoleResponse DTO
     * Maps all fields directly from entity including userId and role object
     */
    UserRoleResponse toUserRoleResponse(UserRole userRole);

    /**
     * Convert UserRoleCreateRequest to UserRole entity
     * Ignores auto-generated and system-managed fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "role", ignore = true) // Will be set by service
    UserRole toUserRole(UserRoleCreateRequest userRoleCreateRequest);

    /**
     * Convert list of UserRole entities to list of UserRoleResponse DTOs
     */
    List<UserRoleResponse> toUserRoleResponseList(List<UserRole> userRoles);
}