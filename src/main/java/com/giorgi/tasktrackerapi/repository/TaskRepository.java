package com.giorgi.tasktrackerapi.repository;

import com.giorgi.tasktrackerapi.entity.Task;
import com.giorgi.tasktrackerapi.enums.Priority;
import com.giorgi.tasktrackerapi.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
    """)
    Page<Task> findByProjectIdWithFilter(
      @Param("projectId") Long projectId,
      @Param("status") TaskStatus status,
      @Param("priority") Priority priority,
      Pageable pageable
    );

    @Query("""
        SELECT t FROM Task t
        WHERE t.assignedUser.id = :userId
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
    """)
    Page<Task> findByAssignedUserIdWithFilter(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("priority") Priority priority,
            Pageable pageable
    );

    @Query("""
    SELECT (COUNT(t) > 0) FROM Task t
    LEFT JOIN t.assignedUser au
    WHERE t.id = :taskId
    AND (t.project.owner.email = :email OR au.email = :email)
    """)
    boolean isProjectOwnerOrAssignedUser(@Param("taskId") Long taskId, @Param("email") String email);

    boolean existsByIdAndAssignedUserEmail(Long taskId, String email);

    @Query("""
    SELECT (COUNT(t) > 0) FROM Task t
    WHERE t.id = :taskId
    AND t.project.owner.email = :email
    """)
    boolean isTaskProjectOwner(@Param("taskId") Long taskId, @Param("email") String email);

    boolean existsByProjectId(Long projectId);
}
