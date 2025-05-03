package com.example.ToDo.repository;

import com.example.ToDo.entities.ToDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ToDoRepository extends JpaRepository<ToDo, Long> {
    @Query("SELECT t FROM ToDo t WHERE " +
            "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR LOWER(t.priority) LIKE LOWER(CONCAT('%', :priority, '%')))")
    Page<ToDo> findByFilters(
            @Param("name") String name,
            @Param("status") Boolean status,
            @Param("priority") String priority,
            Pageable pageable
    );
} 