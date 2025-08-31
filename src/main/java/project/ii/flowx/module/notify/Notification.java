package project.ii.flowx.module.notify;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    UUID id;

    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "UUID")
    UUID userId;

    @Column(name = "title", nullable = false, length = 200)
    String title;

    @Column(name = "content", length = Integer.MAX_VALUE)
    String content;

    @Column(name = "target", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    String target;

    @Column(name = "is_read")
    Boolean isRead;

    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "read_at")
    LocalDateTime readAt;
}