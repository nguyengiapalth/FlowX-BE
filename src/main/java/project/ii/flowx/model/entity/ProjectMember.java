package project.ii.flowx.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import project.ii.flowx.shared.enums.MemberStatus;
import project.ii.flowx.shared.enums.RoleDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "project_members",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_project_member", columnNames = {"project_id", "user_id"})
       },
       indexes = {
           @jakarta.persistence.Index(name = "idx_project_member_project_id", columnList = "project_id"),
           @jakarta.persistence.Index(name = "idx_project_member_user_id", columnList = "user_id"),
           @jakarta.persistence.Index(name = "idx_project_member_status", columnList = "status")
       })
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50)
    RoleDefault role; // member or manager

    @CreationTimestamp
    @Column(name = "join_date")
    LocalDate joinDate;

    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ColumnDefault("'active'")
    @Column(name = "status", columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    MemberStatus status;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ProjectMember that = (ProjectMember) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}