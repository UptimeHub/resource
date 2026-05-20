package uz.uptimehub.resourceapp.jpa.entity.resource;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.uptimehub.resource.dto.resource.ResourceStatus;
import uz.uptimehub.resourceapp.jpa.entity.UserAuditableEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "resource")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Resource extends UserAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    private ResourceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private ResourceType resourceType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_characteristics", columnDefinition = "jsonb")
    private Map<String, String> customCharacteristics = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specification_values", columnDefinition = "jsonb")
    private Map<String, Object> specificationValues = new HashMap<>();

}
