package project.ii.flowx.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;
import project.ii.flowx.shared.enums.FileTargetType;
import project.ii.flowx.shared.enums.FileStatus;
import project.ii.flowx.shared.enums.FileVisibility;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "type", length = 100)
    String type;

    @Column(name = "size")
    Long size;

    @Column(name = "actual_size")
    Long actualSize;

    @Column(name = "bucket", length = 100)
    String bucket;

    @Column(name = "object_path")
    String objectKey;

    @Column(name = "content_hash", length = 64)
    String contentHash;

    @Column(name = "entity_id", nullable = false)
    Long targetId;

    @Column(name = "description", length = Integer.MAX_VALUE)
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    @ToString.Exclude
    User uploader;

    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "entity_type", columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    FileTargetType fileTargetType;

    @ColumnDefault("'private'")
    @Column(name = "visibility", columnDefinition = "text")
    @Enumerated(EnumType.STRING)
    FileVisibility visibility;

    @ColumnDefault("'processing'")
    @Column(name = "file_status")
    @Enumerated(EnumType.STRING)
    FileStatus fileStatus;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        File file = (File) o;
        return getId() != null && Objects.equals(getId(), file.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}