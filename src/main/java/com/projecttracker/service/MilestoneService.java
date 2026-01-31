package com.projecttracker.service;

import com.projecttracker.entity.Milestone;
import com.projecttracker.entity.Project;
import com.projecttracker.repository.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MilestoneService {
    
    @Autowired
    private MilestoneRepository milestoneRepository;
    
    @Autowired
    private ProjectService projectService;
    
    @Transactional
    public Milestone createMilestone(Milestone milestone) {
        Integer maxSequence = milestoneRepository.findMaxSequenceOrderByProjectId(milestone.getProject().getId());
        if (maxSequence != null) {
            milestone.setSequenceOrder(maxSequence + 1);
        } else {
            milestone.setSequenceOrder(1);
        }
        
        return milestoneRepository.save(milestone);
    }
    
    public Optional<Milestone> findById(Long id) {
        return milestoneRepository.findById(id);
    }
    
    public List<Milestone> findByProject(Project project) {
        return milestoneRepository.findByProjectOrderBySequenceOrder(project);
    }
    
    public List<Milestone> findByProjectId(Long projectId) {
        Optional<Project> projectOpt = projectService.findById(projectId);
        return projectOpt.map(this::findByProject).orElse(List.of());
    }
    
    public Milestone updateMilestone(Long id, Milestone milestoneDetails) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        
        milestone.setTitle(milestoneDetails.getTitle());
        milestone.setDescription(milestoneDetails.getDescription());
        milestone.setStatus(milestoneDetails.getStatus());
        milestone.setDueDate(milestoneDetails.getDueDate());
        
        return milestoneRepository.save(milestone);
    }
    
    public void deleteMilestone(Long id) {
        milestoneRepository.deleteById(id);
    }
    
    @Transactional
    public void updateMilestoneSequence(Long milestoneId, Integer newSequence) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        
        List<Milestone> milestones = findByProject(milestone.getProject());
        
        for (Milestone m : milestones) {
            if (m.getSequenceOrder().equals(newSequence)) {
                m.setSequenceOrder(milestone.getSequenceOrder());
                milestoneRepository.save(m);
                break;
            }
        }
        
        milestone.setSequenceOrder(newSequence);
        milestoneRepository.save(milestone);
    }
    
    public long getMilestoneCountByProjectAndStatus(Long projectId, Milestone.MilestoneStatus status) {
        return milestoneRepository.countByProjectAndStatus(projectId, status);
    }
    
    public double getProjectProgress(Long projectId) {
        List<Milestone> milestones = findByProjectId(projectId);
        if (milestones.isEmpty()) {
            return 0.0;
        }
        
        long completedCount = milestones.stream()
                .filter(m -> m.getStatus() == Milestone.MilestoneStatus.COMPLETED)
                .count();
        
        return (double) completedCount / milestones.size() * 100;
    }
}
