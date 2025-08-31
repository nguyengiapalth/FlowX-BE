package project.ii.flowx.module.user.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import project.ii.flowx.applications.helper.MinioService;
import project.ii.flowx.module.user.dto.UserCreateRequest;
import project.ii.flowx.module.user.dto.UserResponse;
import project.ii.flowx.module.user.dto.UserUpdateRequest;
import project.ii.flowx.module.user.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * Mapper interface for converting between User entity and User DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring", imports = UUID.class)
public abstract class UserMapper {

    @Autowired
    protected MinioService minioService;

    @Value("${minio.bucket-name}")
    protected String bucketName;

        /**
         * Converts a User entity to a UserResponse DTO.
         * @param user the User entity to convert
         * @return the converted UserResponse DTO
         */
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
