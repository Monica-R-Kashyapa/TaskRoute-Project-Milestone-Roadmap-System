package com.projecttracker.repository;

import com.projecttracker.entity.Project;
import com.projecttracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    List<Project> findByClient(User client);
    
    List<Project> findByManager(User manager);
    
    List<Project> findByStatus(Project.ProjectStatus status);
    
    @Query("SELECT p FROM Project p WHERE p.client.id = :clientId OR p.manager.id = :managerId")
    List<Project> findByClientOrManager(@Param("clientId") Long clientId, @Param("managerId") Long managerId);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") Project.ProjectStatus status);
}
