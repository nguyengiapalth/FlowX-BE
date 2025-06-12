package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public DepartmentResponse createDepartment(DepartmentCreateRequest departmentCreateRequest) {
        Department department = departmentMapper.toDepartment(departmentCreateRequest);
        return departmentMapper.toDepartmentResponse(departmentRepository.save(department));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MANAGER', #id)")
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest departmentUpdateRequest) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));

        departmentMapper.updateDepartmentFromRequest(department, departmentUpdateRequest);

        departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(departmentRepository.save(department));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MANAGER', #id)")
    @Transactional
    public DepartmentResponse updateDepartmentBackground(Long id, String background) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));

        department.setBackground(background);
        DepartmentResponse response = departmentMapper.toDepartmentResponse(departmentRepository.save(department));
        
        return response;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    @Transactional
    public DepartmentResponse updateManager(Long id, Long newManagerId) {
        log.info("updateManager called with departmentId={}, newManagerId={}", id, newManagerId);
        
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));

        Long oldManagerId = department.getManagerId();
        log.info("Current manager for department {}: {}", id, oldManagerId);

        // Only validate user exists if newManagerId is not 0 (0 means remove manager)
        if (newManagerId != 0) {
            log.info("Validating new manager with ID: {}", newManagerId);
            User newManager = entityLookupService.getUserById(newManagerId);
            log.info("New manager found: {}", newManager.getFullName());
        } else {
            log.info("Removing manager (newManagerId = 0)");
        }

        department.setManagerId(newManagerId == 0 ? null : newManagerId);
        DepartmentResponse response = departmentMapper.toDepartmentResponse(departmentRepository.save(department));
        log.info("Department saved with new managerId: {}", department.getManagerId());

        // publish manager change event
        DepartmentEvent.ManagerChangedEvent event = new DepartmentEvent.ManagerChangedEvent(
                department.getId(),
                oldManagerId != null ? oldManagerId : 0,
                newManagerId
        );
        log.info("Publishing manager changed event: {}", event);
        applicationEventPublisher.publishEvent(event);

        return response;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
        departmentRepository.delete(department);
    }


    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentMapper.toDepartmentResponseList(departmentRepository.findAll());
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
        return departmentMapper.toDepartmentResponse(department);
    }
}