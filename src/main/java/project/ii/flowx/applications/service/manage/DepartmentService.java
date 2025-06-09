package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import project.ii.flowx.model.entity.Department;
import project.ii.flowx.model.repository.DepartmentRepository;
import project.ii.flowx.model.dto.department.DepartmentCreateRequest;
import project.ii.flowx.model.dto.department.DepartmentResponse;
import project.ii.flowx.model.dto.department.DepartmentUpdateRequest;
import project.ii.flowx.model.dto.department.DepartmentBackgroundUpdateRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.mapper.DepartmentMapper;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentService {
    DepartmentRepository departmentRepository;
    DepartmentMapper departmentMapper;

    @PreAuthorize("isAuthenticated()")
    public List<DepartmentResponse> getAllDepartments() {
        log.debug("Fetching all departments from database");
        return departmentMapper.toDepartmentResponseList(departmentRepository.findAll());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MEMBER', #id)")
    public DepartmentResponse getDepartmentById(Long id) {
        log.debug("Fetching department with id: {} from database", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
        return departmentMapper.toDepartmentResponse(department);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    public DepartmentResponse createDepartment(DepartmentCreateRequest departmentCreateRequest) {
        log.debug("Creating new department: {}", departmentCreateRequest);
        Department department = departmentMapper.toDepartment(departmentCreateRequest);
        DepartmentResponse response = departmentMapper.toDepartmentResponse(departmentRepository.save(department));
        log.debug("Created department with id: {}", response.getId());
        return response;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MANAGER', #id)")
    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest departmentUpdateRequest) {
        // validate new department name if provided
        if (departmentUpdateRequest.getName() != null && departmentRepository.existsByName(departmentUpdateRequest.getName())) {
            throw new FlowXException(FlowXError.ALREADY_EXISTS, "Department with name " + departmentUpdateRequest.getName() + " already exists");
        }
        log.debug("Updating department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));

        departmentMapper.updateDepartmentFromRequest(department, departmentUpdateRequest);
        DepartmentResponse response = departmentMapper.toDepartmentResponse(departmentRepository.save(department));
        log.debug("Updated department with id: {}", id);
        return response;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    public void deleteDepartment(Long id) {
        log.debug("Deleting department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
        departmentRepository.delete(department);
        log.debug("Deleted department with id: {}", id);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MANAGER', #id)")
    public DepartmentResponse updateDepartmentBackground(Long id, DepartmentBackgroundUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
        
        if (request.getBackground() != null) {
            department.setBackground(request.getBackground());
            log.info("Updated background for department {}", id);
        }
        
        department = departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(department);
    }
}