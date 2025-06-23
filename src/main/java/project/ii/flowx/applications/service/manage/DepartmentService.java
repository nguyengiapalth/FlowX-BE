package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentService {
    DepartmentRepository departmentRepository;
    DepartmentMapper departmentMapper;
    EntityLookupService entityLookupService;
    ApplicationEventPublisher applicationEventPublisher;
    CacheManager cacheManager;

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    public DepartmentResponse createDepartment(DepartmentCreateRequest departmentCreateRequest) {
        Department department = departmentMapper.toDepartment(departmentCreateRequest);
//        Objects.requireNonNull(cacheManager.getCache("departments")).evict("all");

        return departmentMapper.toDepartmentResponse(departmentRepository.save(department));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MANAGER', #id)")
    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest departmentUpdateRequest) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));

        departmentMapper.updateDepartmentFromRequest(department, departmentUpdateRequest);
        departmentRepository.save(department);

        // Evict cache entries for all departments and the specific one
//        evictDepartmentsCache(department.getId());

        return departmentMapper.toDepartmentResponse(departmentRepository.save(department));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
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

        // Evict cache entries for all departments and the specific one
//        evictDepartmentsCache(id);

        return response;
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
        departmentRepository.delete(department);

        // Evict cache entries for all departments and the specific one
//        evictDepartmentsCache(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
//    @Cacheable(value = "departments", key = "'all'")
    public List<DepartmentResponse> getAllDepartments() {
        return departmentMapper.toDepartmentResponseList(departmentRepository.findAll());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
//    @Cacheable(value = "departments", key = "#id")
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
        return departmentMapper.toDepartmentResponse(department);
    }

    /**
     * Internal method to update department background object key without security checks
     * Used by event handlers
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MANAGER', #id)")
    public void updateDepartmentBackgroundObjectKey(Long id, String objectKey) {
        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
            
            String oldBackground = department.getBackground();
            department.setBackground(objectKey);
            departmentRepository.save(department);

            // Cache eviction key all and specific department
//            evictDepartmentsCache(id);

            log.info("Successfully updated background object key for department {}: {} -> {}", id, oldBackground, objectKey);
        } catch (Exception e) {
            log.error("Error updating background object key for department {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public void evictDepartmentsCache(Long id) {
        Objects.requireNonNull(cacheManager.getCache("departments")).evict("all");
        Objects.requireNonNull(cacheManager.getCache("departments")).evict(id);
    }
}