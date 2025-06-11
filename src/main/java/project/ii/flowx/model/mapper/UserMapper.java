package project.ii.flowx.model.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import project.ii.flowx.applications.service.helper.MinioService;
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
public abstract class UserMapper {

    @Autowired
    protected MinioService minioService;

    @Value("${minio.bucket-name}")
    protected String bucketName;

        /**
         * Converts a User entity to a UserResponse DTO.
         * convert department to departmentId
         * @param user the User entity to convert
         * @return the converted UserResponse DTO
         */
        @Mapping(target = "department", source = "department")
        @Mapping(target = "avatar", source = "avatar", qualifiedByName = "objectKeyToUrl")
        @Mapping(target = "background", source = "background", qualifiedByName = "objectKeyToUrl")
        public abstract UserResponse toUserResponse(User user);

        public abstract User toUser(UserCreateRequest userCreateRequest);

        public abstract void updateUserFromRequest(@MappingTarget User user, UserUpdateRequest userUpdateRequest);

        public abstract List<UserResponse> toUserResponseList(List<User> users);

        @Named("objectKeyToUrl")
        protected String objectKeyToUrl(String objectKey) {
            if (objectKey == null || objectKey.trim().isEmpty()) {
                return null;
            }
            
            try {
                // 24 hours expiry
                return minioService.getPresignedDownloadUrlFromObjectKey(objectKey, 3600 * 24);
            } catch (Exception e) {
                return null;
            }
        }
}
