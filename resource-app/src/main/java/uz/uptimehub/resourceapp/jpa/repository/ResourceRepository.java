package uz.uptimehub.resourceapp.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.uptimehub.resource.dto.Status;
import uz.uptimehub.resource.dto.resource.ResourceStatus;
import uz.uptimehub.resourceapp.jpa.entity.resource.Resource;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    @Query("""
                select r from Resource r
                left join r.resourceType t
                where (:id is null or r.id = :id)
                and (:organizationId is null or r.organizationId = :organizationId)
                and (:name is null or :name = '' or lower(r.name) like lower(concat('%', :name, '%')))
                and (:resourceTypeId is null or t.id = :resourceTypeId)
                and (:status is null or r.status = :status)
            """)
    Page<Resource> findAllFiltered(
            UUID id,
            UUID organizationId,
            String name,
            Long resourceTypeId,
            ResourceStatus status,
            Pageable pageable
    );


        Optional<Resource> findByIdAndOrganizationId(UUID id, UUID organizationId);
}