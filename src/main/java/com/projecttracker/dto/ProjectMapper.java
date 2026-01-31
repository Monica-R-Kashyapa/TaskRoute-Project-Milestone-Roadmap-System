package com.projecttracker.dto;

import com.projecttracker.entity.Project;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMapper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static ProjectDTO toDTO(Project project) {
        if (project == null) {
            return null;
        }
        
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus() != null ? project.getStatus().toString() : null);
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setProgressPercentage((int) Math.round(project.getProgressPercentage()));
        
        if (project.getClient() != null) {
            dto.setClientId(project.getClient().getId());
            dto.setClientName(project.getClient().getUsername());
        }
        
        if (project.getManager() != null) {
            dto.setManagerId(project.getManager().getId());
            dto.setManagerName(project.getManager().getUsername());
        }
        
        if (project.getCreatedAt() != null) {
            dto.setCreatedAt(project.getCreatedAt().format(DATE_FORMATTER));
        }
        
        if (project.getUpdatedAt() != null) {
            dto.setUpdatedAt(project.getUpdatedAt().format(DATE_FORMATTER));
        }
        
        return dto;
    }
    
    public static List<ProjectDTO> toDTOList(List<Project> projects) {
        return projects.stream()
                .map(ProjectMapper::toDTO)
                .collect(Collectors.toList());
    }
}
