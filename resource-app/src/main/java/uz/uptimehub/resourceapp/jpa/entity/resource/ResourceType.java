package uz.uptimehub.resourceapp.jpa.entity.resource;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.uptimehub.resource.dto.Status;
import uz.uptimehub.resourceapp.jpa.entity.UserAuditableEntity;
import uz.uptimehub.resourceapp.jpa.entity.category.Category;
import uz.uptimehub.resource.dto.resourcetype.SpecificationDefinition;

import java.util.List;

@Entity
@Table(name = "resource_type", indexes = {
        @Index(name = "idx_resource_type_name", columnList = "name", unique = true),
        @Index(name = "idx_resource_type_status", columnList = "status")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceType extends UserAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specification_definitions", columnDefinition = "jsonb")
    private List<SpecificationDefinition> specificationDefinitions;
}
