package project.ii.flowx.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.File;
import project.ii.flowx.shared.enums.EntityType;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByUploaderId(Long uploaderId);
    List<File> findByEntityId(Long entityId);
    List<File> findByEntityType(EntityType entityType);
    List<File> findByEntityIdAndEntityType(Long entityId, EntityType entityType);
}
