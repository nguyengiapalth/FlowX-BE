package project.ii.flowx.module.file.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;
import project.ii.flowx.applications.enums.FileTargetType;
import project.ii.flowx.applications.enums.FileStatus;
import project.ii.flowx.applications.enums.FileVisibility;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    UUID id;

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
    UUID targetId;

    @Column(name = "description", length = Integer.MAX_VALUE)
    String description;

    @Column(name = "uploader_id", nullable = false, updatable = false, columnDefinition = "UUID")
    UUID uploaderId;

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