package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import project.ii.flowx.applications.service.helper.MinioService;
import project.ii.flowx.model.dto.file.FileCreateRequest;
import project.ii.flowx.model.dto.file.FileResponse;
import project.ii.flowx.model.dto.file.FileUpdateRequest;
import project.ii.flowx.model.entity.File;

import java.util.List;

/**
 * Mapper interface for converting between File entity and File DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public abstract class FileMapper {

    @Autowired
    protected MinioService minioService;

    @Mapping(target = "url", source = "objectKey", qualifiedByName = "objectKeyToUrl")
    public abstract FileResponse toFileResponse(File file);

    public abstract File toFile(FileCreateRequest fileCreateRequest);

    public abstract void updateFileFromRequest(@MappingTarget File file, FileUpdateRequest fileUpdateRequest);

    public abstract List<FileResponse> toFileResponseList(List<File> files);

    @Named("objectKeyToUrl")
    protected String objectKeyToUrl(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) return null;

        try {
            String url = minioService.getPresignedDownloadUrlFromObjectKey(objectKey, 3600 * 24); // 24 hours expiry
            System.out.println("Converting objectKey: " + objectKey + " to URL: " + (url != null ? "SUCCESS" : "FAILED"));
            return url;
        } catch (Exception e) {
            // Log error and return null
            System.err.println("Error generating presigned URL for objectKey: " + objectKey + ", error: " + e.getMessage());
            return null;
        }
    }

}