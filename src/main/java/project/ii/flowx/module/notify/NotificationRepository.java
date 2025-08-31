package project.ii.flowx.module.notify;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID currentUserId, Pageable pageable);

    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    @Transactional
    @Modifying
    void markAsReadById(UUID id);

    @Query("UPDATE Notification n SET n.isRead = false WHERE n.id = :id")
    @Transactional
    @Modifying
    void markAsUnreadById(UUID id);

    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    @Transactional
    @Modifying
    void markAllAsReadByUserId(UUID userId);
}
