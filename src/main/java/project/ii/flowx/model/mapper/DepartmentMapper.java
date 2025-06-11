package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import project.ii.flowx.applications.service.helper.MinioService;
import project.ii.flowx.model.dto.department.DepartmentCreateRequest;
import project.ii.flowx.model.dto.department.DepartmentResponse;
import project.ii.flowx.model.dto.department.DepartmentUpdateRequest;
import project.ii.flowx.model.entity.Department;

import java.util.List;

/**
 * Mapper interface for converting between Department entity and Department DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public abstract class DepartmentMapper {
    @Autowired
    protected MinioService minioService;

    @Mapping(target = "background", source = "background", qualifiedByName = "objectKeyToUrl")
    public abstract DepartmentResponse toDepartmentResponse(Department department);

    public abstract Department toDepartment(DepartmentCreateRequest departmentCreateRequest);

    public abstract void updateDepartmentFromRequest(@MappingTarget Department department, DepartmentUpdateRequest departmentUpdateRequest);

    public abstract List<DepartmentResponse> toDepartmentResponseList(List<Department> departments);

    @Named("objectKeyToUrl")
    protected String objectKeyToUrl(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) return null;

        try {
            // 24 hours expiry
            return minioService.getPresignedDownloadUrlFromObjectKey(objectKey, 3600 * 24);
        } catch (Exception e) {
            return null;
        }
    }
}