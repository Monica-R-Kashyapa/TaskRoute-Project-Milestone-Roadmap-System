package com.projecttracker.repository;

import com.projecttracker.entity.Milestone;
import com.projecttracker.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    
    List<Milestone> findByProject(Project project);
    
    List<Milestone> findByProjectOrderBySequenceOrder(Project project);
    
    List<Milestone> findByStatus(Milestone.MilestoneStatus status);
    
    @Query("SELECT m FROM Milestone m WHERE m.project.id = :projectId AND m.sequenceOrder = :sequenceOrder")
    Optional<Milestone> findByProjectAndSequenceOrder(@Param("projectId") Long projectId, @Param("sequenceOrder") Integer sequenceOrder);
    
    @Query("SELECT COUNT(m) FROM Milestone m WHERE m.project.id = :projectId AND m.status = :status")
    long countByProjectAndStatus(@Param("projectId") Long projectId, @Param("status") Milestone.MilestoneStatus status);
    
    @Query("SELECT MAX(m.sequenceOrder) FROM Milestone m WHERE m.project.id = :projectId")
    Integer findMaxSequenceOrderByProjectId(@Param("projectId") Long projectId);
}
