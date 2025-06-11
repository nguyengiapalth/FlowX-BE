package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import project.ii.flowx.applications.events.DepartmentEvent;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.model.entity.Department;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.repository.DepartmentRepository;
import project.ii.flowx.model.dto.department.DepartmentCreateRequest;
import project.ii.flowx.model.dto.department.DepartmentResponse;
import project.ii.flowx.model.dto.department.DepartmentUpdateRequest;
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
    EntityLookupService entityLookupService;
    ApplicationEventPublisher applicationEventPublisher;

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    public DepartmentResponse createDepartment(DepartmentCreateRequest departmentCreateRequest) {
        Department department = departmentMapper.toDepartment(departmentCreateRequest);
        DepartmentResponse response = departmentMapper.toDepartmentResponse(departmentRepository.save(department));
        return response;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MANAGER', #id)")
    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest departmentUpdateRequest) {
        // validate new department name if provided
        if (departmentUpdateRequest.getName() != null && departmentRepository.existsByName(departmentUpdateRequest.getName())) {
            throw new FlowXException(FlowXError.ALREADY_EXISTS, "Department with name " + departmentUpdateRequest.getName() + " already exists");
        }
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));

        departmentMapper.updateDepartmentFromRequest(department, departmentUpdateRequest);
        DepartmentResponse response = departmentMapper.toDepartmentResponse(departmentRepository.save(department));
        return response;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MANAGER', #id)")
    public DepartmentResponse updateDepartmentBackground(Long id, String background) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));

        department.setBackground(background);

        department = departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(department);
    }

    public DepartmentResponse updateManager(Long id, Long newManagerId) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));



        // validate user id exists, is member of department and is not already a manager
        if(newManagerId != 0){
            User user = entityLookupService.getUserById(newManagerId);
            if(!user.getDepartment().getId().equals(department.getId()))
                throw new FlowXException(FlowXError.FORBIDDEN, "User is not a member of this department");
        }

        // publish event to notify that manager has changed
        applicationEventPublisher.publishEvent(new DepartmentEvent.ManagerChangedEvent(id, department.getManagerId(), newManagerId));

        department.setManagerId(newManagerId);
        department = departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(department);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
        departmentRepository.delete(department);
    }


    @PreAuthorize("isAuthenticated()")
    public List<DepartmentResponse> getAllDepartments() {
        return departmentMapper.toDepartmentResponseList(departmentRepository.findAll());
    }

    @PreAuthorize("isAuthenticated()")
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
        return departmentMapper.toDepartmentResponse(department);
    }

}