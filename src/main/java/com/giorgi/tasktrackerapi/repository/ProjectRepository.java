package com.giorgi.tasktrackerapi.repository;

import com.giorgi.tasktrackerapi.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);
    Boolean existsByIdAndOwnerEmail(Long id, String ownerEmail);
}
