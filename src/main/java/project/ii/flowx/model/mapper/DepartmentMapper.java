package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
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
public interface DepartmentMapper {

    DepartmentResponse toDepartmentResponse(Department department);

    Department toDepartment(DepartmentCreateRequest departmentCreateRequest);

    void updateDepartmentFromRequest(@MappingTarget Department department, DepartmentUpdateRequest departmentUpdateRequest);

    List<DepartmentResponse> toDepartmentResponseList(List<Department> departments);
}