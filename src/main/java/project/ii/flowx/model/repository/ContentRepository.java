package project.ii.flowx.model.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.Content;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.shared.enums.ContentTargetType;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByParentId(Long parentId);

    @EntityGraph(attributePaths = {"replies"})
    List<Content> findByContentTargetTypeAndTargetId(ContentTargetType contentTargetType, Long targetId);

    List<Content> findByAuthor(User user);
}
