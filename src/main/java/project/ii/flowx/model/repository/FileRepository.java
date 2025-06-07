package project.ii.flowx.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.File;
import project.ii.flowx.shared.enums.FileTargetType;
import project.ii.flowx.shared.enums.FileStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByUploaderId(Long uploaderId);
    List<File> findByTargetId(Long entityId);
    List<File> findByFileTargetType(FileTargetType fileTargetType);
    List<File> findByTargetIdAndFileTargetType(Long entityId, FileTargetType fileTargetType);

    List<File> findByFileStatus(FileStatus status);

    List<File> findByFileStatusAndCreatedAtBefore(FileStatus fileStatus, Instant createdAt);
}
