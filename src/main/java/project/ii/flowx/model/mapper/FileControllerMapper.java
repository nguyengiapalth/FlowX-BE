package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.ii.flowx.model.dto.file.FileCreateRequest;
import project.ii.flowx.model.dto.file.FileUploadRequest;

@Mapper(componentModel = "spring")
public interface FileControllerMapper {

    @Mapping(target = "name", source = "file.originalFilename")
    @Mapping(target = "type", source = "file.contentType")
    @Mapping(target = "size", source = "file.size")
    FileCreateRequest toFileCreateRequest(FileUploadRequest uploadRequest);
} 