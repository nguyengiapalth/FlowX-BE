package project.ii.flowx.model.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.*;
import org.hibernate.proxy.HibernateProxy;
import project.ii.flowx.shared.enums.UserStatus;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Objects;

@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "users", indexes = {
    @jakarta.persistence.Index(name = "idx_user_email", columnList = "email"),
    @jakarta.persistence.Index(name = "idx_user_department_id", columnList = "department_id"),
    @jakarta.persistence.Index(name = "idx_user_status", columnList = "status")
}) 
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;
    
    @Column(name = "email", nullable = false, length = 100, unique = true)
    String email;

    @Column(name = "password", nullable = false)
    String password;

    @Column(name = "full_name", nullable = false, length = 100)
    String fullName;

    @Column(name = "avatar")
    String avatar;

    @Column(name = "background")
    String background;

    @Column(name = "phone_number", length = 20)
    String phoneNumber;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    @Column(name = "address", length = Integer.MAX_VALUE)
    String address;

    @Column(name = "position", length = 100)
    String position;

    @Column(name = "bio", length = 1000)
    String bio;

    @Column(name = "facebook", length = 255)
    String facebook;

    @Column(name = "linkedin", length = 255)
    String linkedin;

    @Column(name = "twitter", length = 255)
    String twitter;

    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    String gender;

    @Column(name = "join_date")
    LocalDate joinDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "department_id")
    @ToString.Exclude
    Department department;

    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ColumnDefault("'active'")
    @Column(name = "status", columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    UserStatus status;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}