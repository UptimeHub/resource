package uz.uptimehub.resourceapp.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.uptimehub.resource.dto.Status;
import uz.uptimehub.resourceapp.jpa.entity.resource.ResourceType;

import java.util.Set;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {

    @Query("""
            select distinct rt.name from ResourceType rt
               where (:status is null or rt.status = :status)
            """)
    Set<String> findAllNames(Status status);

    @Query("""
                select rt from ResourceType rt
                left join rt.category c
                where (:id is null or rt.id = :id)
                and (:name is null or lower(rt.name) like lower(concat('%', :name, '%')))
                and (:status is null or rt.status = :status)
                and (:categoryId is null or c.id = :categoryId)
            """)
    Page<ResourceType> findAllFiltered(
            Long id,
            String name,
            String status,
            Long categoryId,
            Pageable pageable
    );
}