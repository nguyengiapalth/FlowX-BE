package project.ii.flowx.model.entity;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.proxy.HibernateProxy;
import project.ii.flowx.shared.enums.PriorityLevel;
import project.ii.flowx.shared.enums.ProjectStatus;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projects", indexes = {
    @jakarta.persistence.Index(name = "idx_project_department_id", columnList = "department_id"),
    @jakarta.persistence.Index(name = "idx_project_status", columnList = "status"),
    @jakarta.persistence.Index(name = "idx_project_dates", columnList = "start_date, end_date")
})
@ToString
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "name", nullable = false, length = 100)
    String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    String description;

    @Column(name = "background")
    String background;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ToString.Exclude
    Department department;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, 
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, 
               orphanRemoval = true)
    @ToString.Exclude
    List<ProjectMember> members;

    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ColumnDefault("'planning'")
    @Column(name = "status", columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    ProjectStatus status;

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
        Project project = (Project) o;
        return getId() != null && Objects.equals(getId(), project.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}