package project.ii.flowx.module.manage.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import project.ii.flowx.applications.enums.Visibility;
import project.ii.flowx.applications.enums.PriorityLevel;
import project.ii.flowx.applications.enums.TaskStatus;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    UUID id;

    @Column(name = "title", nullable = false, length = 200)
    String title;

    @Column(name = "description", length = Integer.MAX_VALUE)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    Visibility targetType;

    @Column(name = "target_id", nullable = false)
    UUID targetId;

    @Column(name = "assigner_id", nullable = false, updatable = false, columnDefinition = "UUID")
    UUID assignerId;

    @Column(name = "assignee_id", nullable = false, updatable = false, columnDefinition = "UUID")
    UUID assigneeId;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "due_date")
    LocalDate dueDate;

    @Column(name = "completed_date")
    LocalDate completedDate;

    @ColumnDefault("false")
    @Column(name = "has_files")
    Boolean hasFiles;

    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ColumnDefault("'to_do'")
    @Column(name = "status", columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    TaskStatus status;

    @ColumnDefault("'medium'")
    @Column(name = "priority", columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    PriorityLevel priority;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Task task = (Task) o;
        return getId() != null && Objects.equals(getId(), task.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}