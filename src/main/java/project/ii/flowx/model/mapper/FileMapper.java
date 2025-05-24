package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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
public interface FileMapper {

    FileResponse toFileResponse(File file);

    @Mapping(target = "uploader.id", source = "uploaderId")
    File toFile(FileCreateRequest fileCreateRequest);

    void updateFileFromRequest(@MappingTarget File file, FileUpdateRequest fileUpdateRequest);

    List<FileResponse> toFileResponseList(List<File> files);
}