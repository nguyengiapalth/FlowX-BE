package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.model.entity.Project;
import project.ii.flowx.model.entity.ProjectMember;
import project.ii.flowx.model.repository.ProjectMemberRepository;
import project.ii.flowx.model.dto.projectmember.ProjectMemberCreateRequest;
import project.ii.flowx.model.dto.projectmember.ProjectMemberResponse;
import project.ii.flowx.model.dto.projectmember.ProjectMemberUpdateRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.mapper.ProjectMemberMapper;
import project.ii.flowx.shared.enums.MemberStatus;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectMemberService {
    ProjectMemberRepository projectMemberRepository;
    ProjectMemberMapper projectMemberMapper;
    EntityLookupService entityLookupService;

    @Transactional
//    @CacheEvict(value = {"projectMember", "projectMembers", "userProjects"}, allEntries = true)
    public ProjectMemberResponse createProjectMember(ProjectMemberCreateRequest projectMemberCreateRequest) {
        log.info("Tạo project member mới cho project ID: {}", projectMemberCreateRequest.getProjectId());

        // Kiểm tra project có tồn tại không
        Project project = entityLookupService.getProjectById(projectMemberCreateRequest.getProjectId());

        boolean memberExists = projectMemberRepository.existsByProjectIdAndUserId(
            projectMemberCreateRequest.getProjectId(),
            projectMemberCreateRequest.getUserId()
        );

        if (memberExists) throw new FlowXException(FlowXError.ALREADY_EXISTS, "Người dùng đã là thành viên của project này");


        ProjectMember projectMember = projectMemberMapper.toProjectMember(projectMemberCreateRequest);
        if (projectMember.getStatus() == null) projectMember.setStatus(MemberStatus.ACTIVE);
        if (projectMember.getJoinDate() == null) projectMember.setJoinDate(LocalDate.now());

        projectMember = projectMemberRepository.save(projectMember);

        log.info("Đã tạo thành công project member với ID: {}", projectMember.getId());
        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional
//    @CacheEvict(value = {"projectMember", "projectMembers", "userProjects"}, allEntries = true)
    public ProjectMemberResponse updateProjectMember(Long id, ProjectMemberUpdateRequest projectMemberUpdateRequest) {
        log.info("Cập nhật project member với ID: {}", id);

        ProjectMember projectMember = projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Không tìm thấy project member với ID: " + id));
        
        projectMemberMapper.updateProjectMemberFromRequest(projectMember, projectMemberUpdateRequest);
        projectMember = projectMemberRepository.save(projectMember);
        
        log.info("Đã cập nhật thành công project member với ID: {}", id);
        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional
//    @CacheEvict(value = {"projectMember", "projectMembers", "userProjects"}, allEntries = true)
    public void deleteProjectMember(Long id) {
        log.info("Xóa project member với ID: {}", id);

        if (!projectMemberRepository.existsById(id)) throw new FlowXException(FlowXError.NOT_FOUND, "Không tìm thấy project member với ID: " + id);

        projectMemberRepository.deleteById(id);
        log.info("Đã xóa thành công project member với ID: {}", id);
    }

    @Transactional(readOnly = true)
//    @Cacheable(value = "projectMember", key = "#id", unless = "#result == null")
    public ProjectMemberResponse getProjectMemberById(Long id) {
        log.debug("Lấy thông tin project member với ID: {}", id);

        ProjectMember projectMember = projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Không tìm thấy project member với ID: " + id));

        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional(readOnly = true)
//    @Cacheable(value = "projectMembers", key = "#projectId", unless = "#result.isEmpty()")
    public List<ProjectMemberResponse> getByProjectId(long projectId) {
        log.debug("Lấy danh sách members của project ID: {}", projectId);

        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(projectId);
        return projectMemberMapper.toProjectMemberResponseList(projectMembers);
    }

    @Transactional(readOnly = true)
//    @Cacheable(value = "userProjects", key = "#userId", unless = "#result.isEmpty()")
    public List<ProjectMemberResponse> getByUserId(long userId) {
        log.debug("Lấy danh sách projects của user ID: {}", userId);

        List<ProjectMember> projectMembers = projectMemberRepository.findByUserId(userId);
        return projectMemberMapper.toProjectMemberResponseList(projectMembers);
    }

    @Transactional
//    @CacheEvict(value = {"projectMember", "projectMembers", "userProjects"}, allEntries = true)
    public ProjectMemberResponse updateMemberStatus(Long id, MemberStatus status) {
        log.info("Cập nhật trạng thái member ID: {} thành: {}", id, status);

        ProjectMember projectMember = projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Không tìm thấy project member với ID: " + id));
        
        // Validate status transition nếu cần
        validateStatusTransition(projectMember.getStatus(), status);

        projectMember.setStatus(status);
        projectMember = projectMemberRepository.save(projectMember);
        
        log.info("Đã cập nhật trạng thái thành công cho member ID: {}", id);
        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional(readOnly = true)
//    @Cacheable(value = "activeProjectMembers", key = "#projectId")
    public List<ProjectMemberResponse> getActiveMembers(long projectId) {
        log.debug("Lấy danh sách active members của project ID: {}", projectId);

        List<ProjectMember> activeMembers = projectMemberRepository.findByProjectIdAndStatus(
            projectId,
            MemberStatus.ACTIVE
        );
        return projectMemberMapper.toProjectMemberResponseList(activeMembers);
    }

    @Transactional
//    @CacheEvict(value = {"projectMember", "projectMembers", "userProjects", "activeProjectMembers", "memberCount"}, allEntries = true)
    public void bulkUpdateMemberStatus(List<Long> memberIds, MemberStatus status) {
        log.info("Cập nhật trạng thái hàng loạt cho {} members", memberIds.size());

        List<ProjectMember> members = projectMemberRepository.findAllById(memberIds);

        if (members.size() != memberIds.size()) {
            throw new FlowXException(FlowXError.FORBIDDEN, "Không tìm thấy một hoặc nhiều project member với ID trong danh sách");
        }

        members.forEach(member -> {
            validateStatusTransition(member.getStatus(), status);
            member.setStatus(status);
        });

        projectMemberRepository.saveAll(members);
        log.info("Đã cập nhật trạng thái thành công cho {} members", members.size());
    }

    // Cache management methods
//    @CacheEvict(value = {"projectMember", "projectMembers", "userProjects", "activeProjectMembers", "memberCount"}, allEntries = true)
    public void clearAllCache() {
        log.info("Đã xóa toàn bộ cache của ProjectMemberService");
    }

//    @CacheEvict(value = "projectMembers", key = "#projectId")
    public void clearProjectMembersCache(long projectId) {
        log.info("Đã xóa cache members của project ID: {}", projectId);
    }

//    @CacheEvict(value = "userProjects", key = "#userId")
    public void clearUserProjectsCache(long userId) {
        log.info("Đã xóa cache projects của user ID: {}", userId);
    }

    // Helper methods
    private void validateStatusTransition(MemberStatus currentStatus, MemberStatus newStatus) {
        // Implement business logic for valid status transitions
        if (currentStatus == MemberStatus.INACTIVE && newStatus == MemberStatus.ACTIVE) {
            log.debug("Chuyển đổi trạng thái từ INACTIVE sang ACTIVE");
        }
        // Add more validation rules as needed
    }

//    // Scheduled cache refresh (optional)
//    @Scheduled(fixedRate = 3600000) // Refresh every hour
//    @CacheEvict(value = {"projectMembers", "userProjects", "activeProjectMembers", "memberCount"}, allEntries = true)
//    public void refreshCache() {
//        log.info("Làm mới cache định kỳ cho ProjectMemberService");
//    }
}