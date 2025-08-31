//package project.ii.flowx.model.entity;
//
//
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//
//import java.util.List;
//import java.util.UUID;
//
//@Getter
//@Setter
//@ToString
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//@Entity
//@Table(name = "directory")
//public class Directory {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
//    UUID id;
//
//    private String name;
//
//    @ManyToOne
//    @JoinColumn(name = "parent_id")
//    private Directory parent;
//
//    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
//    @ToString.Exclude
//    private List<Directory> children;
//
//    @OneToMany(mappedBy = "namespace", cascade = CascadeType.ALL)
//    @ToString.Exclude
//    private List<File> files;
//}
