package project.ii.flowx.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.model.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long currentUserId, Pageable pageable);

    List<Notification> findByUserId(Long userId);

    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    @Transactional
    @Modifying
    void markAsReadById(Long id);

    @Query("UPDATE Notification n SET n.isRead = false WHERE n.id = :id")
    @Transactional
    @Modifying
    void markAsUnreadById(Long id);

    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    @Transactional
    @Modifying
    void markAllAsReadByUserId(Long userId);
}
