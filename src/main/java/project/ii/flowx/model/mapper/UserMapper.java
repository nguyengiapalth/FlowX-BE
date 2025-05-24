package project.ii.flowx.model.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import project.ii.flowx.model.dto.user.UserCreateRequest;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.model.dto.user.UserUpdateRequest;
import project.ii.flowx.model.entity.User;

import java.util.List;

/**
 * Mapper interface for converting between User entity and User DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

        /**
         * Converts a User entity to a UserResponse DTO.
         * convert department to departmentId
         * @param user the User entity to convert
         * @return the converted UserResponse DTO
         */
        @Mapping(target = "departmentId", source = "department.id")
        UserResponse toUserResponse(User user);

        User toUser(UserCreateRequest userCreateRequest);

        void updateUserFromRequest(@MappingTarget User user, UserUpdateRequest userUpdateRequest);

        List<UserResponse> toUserResponseList(List<User> users);
}
