package com.projecttracker.service;

import com.projecttracker.entity.Project;
import com.projecttracker.entity.User;
import com.projecttracker.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }
    
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }
    
    public List<Project> findAll() {
        return projectRepository.findAll();
    }
    
    public List<Project> findByClient(User client) {
        return projectRepository.findByClient(client);
    }
    
    public List<Project> findByManager(User manager) {
        return projectRepository.findByManager(manager);
    }
    
    public List<Project> findByStatus(Project.ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }
    
    public Project updateProject(Long id, Project projectDetails) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        project.setClient(projectDetails.getClient());
        project.setManager(projectDetails.getManager());
        project.setStatus(projectDetails.getStatus());
        project.setStartDate(projectDetails.getStartDate());
        project.setEndDate(projectDetails.getEndDate());
        
        return projectRepository.save(project);
    }
    
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
    
    public List<Project> getProjectsForUser(User user) {
        switch (user.getRole()) {
            case CLIENT:
                return findByClient(user);
            case MANAGER:
                return findByManager(user);
            case ADMIN:
                return findAll();
            default:
                return List.of();
        }
    }
    
    public long getProjectCountByStatus(Project.ProjectStatus status) {
        return projectRepository.countByStatus(status);
    }
}
