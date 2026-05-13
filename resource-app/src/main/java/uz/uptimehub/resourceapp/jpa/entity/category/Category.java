package uz.uptimehub.resourceapp.jpa.entity.category;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uptimehub.resourceapp.jpa.entity.Status;
import uz.uptimehub.resourceapp.jpa.entity.UserAuditableEntity;


@Entity
@Table(name = "category", indexes = {
        @Index(name = "idx_category_name", columnList = "name", unique = true),
        @Index(name = "idx_category_status", columnList = "status")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Category extends UserAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

}
