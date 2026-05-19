package uz.uptimehub.resourceapp.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.uptimehub.resourceapp.jpa.entity.category.Category;
import uz.uptimehub.resource.dto.Status;

import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
                select c from Category c
                where (:id is null or c.id = :id)
                and (:name is null or :name = '' or lower(c.name) like lower(concat('%', :name, '%')))
                and (:status is null or c.status = :status)
             """
    )
    Page<Category> findAllFiltered(
            Long id,
            String name,
            Status status,
            Pageable pageable
    );

    @Query("select c.name from Category c where (:status is null or c.status = :status)")
    Set<String> findAllNames(Status status);
}