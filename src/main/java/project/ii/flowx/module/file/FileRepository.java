package project.ii.flowx.module.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.file.entity.File;
import project.ii.flowx.applications.enums.FileTargetType;
import project.ii.flowx.applications.enums.FileStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {
    List<File> findByTargetIdAndFileTargetType(UUID targetId, FileTargetType fileTargetType);

    List<File> findByFileStatusAndCreatedAtBefore(FileStatus fileStatus, LocalDateTime createdAt);
}
