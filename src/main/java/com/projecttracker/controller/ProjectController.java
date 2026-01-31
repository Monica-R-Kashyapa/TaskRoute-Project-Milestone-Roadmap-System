package com.projecttracker.controller;

import com.projecttracker.dto.ProjectDTO;
import com.projecttracker.dto.ProjectMapper;
import com.projecttracker.entity.Project;
import com.projecttracker.entity.User;
import com.projecttracker.service.ProjectService;
import com.projecttracker.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/projects")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ProjectDTO>> getAllProjects(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Project> projects = projectService.getProjectsForUser(currentUser);
        List<ProjectDTO> projectDTOs = ProjectMapper.toDTOList(projects);
        return ResponseEntity.ok(projectDTOs);
    }
    
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Project> projectOpt = projectService.findById(id);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if (hasAccessToProject(currentUser, project)) {
                return ResponseEntity.ok(ProjectMapper.toDTO(project));
            }
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody Map<String, Object> projectData, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != User.Role.CLIENT) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Only clients can create projects");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Project project = new Project();
            project.setName((String) projectData.get("name"));
            project.setDescription((String) projectData.get("description"));
            project.setClient(currentUser);
            
            // Handle manager assignment
            if (projectData.containsKey("manager") && projectData.get("manager") instanceof Map) {
                Map<String, Object> managerData = (Map<String, Object>) projectData.get("manager");
                if (managerData.containsKey("id")) {
                    Long managerId = Long.valueOf(managerData.get("id").toString());
                    Optional<User> managerOpt = userService.findById(managerId);
                    if (managerOpt.isPresent() && managerOpt.get().getRole() == User.Role.MANAGER) {
                        project.setManager(managerOpt.get());
                    } else {
                        Map<String, Object> response = new HashMap<>();
                        response.put("error", "Invalid manager selected");
                        return ResponseEntity.badRequest().body(response);
                    }
                }
            }
            
            // Handle dates
            if (projectData.containsKey("startDate") && projectData.get("startDate") != null) {
                project.setStartDate(java.time.LocalDate.parse(projectData.get("startDate").toString()));
            }
            if (projectData.containsKey("endDate") && projectData.get("endDate") != null) {
                project.setEndDate(java.time.LocalDate.parse(projectData.get("endDate").toString()));
            }
            
            project.setStatus(Project.ProjectStatus.PLANNING);
            Project createdProject = projectService.createProject(project);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Project created successfully");
            response.put("project", ProjectMapper.toDTO(createdProject));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to create project: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProject(@PathVariable Long id, @RequestBody Project projectDetails, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Project> projectOpt = projectService.findById(id);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if (canUpdateProject(currentUser, project)) {
                Project updatedProject = projectService.updateProject(id, projectDetails);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Project updated successfully");
                response.put("project", updatedProject);
                return ResponseEntity.ok(response);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Project not found or access denied");
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Only admins can delete projects");
            return ResponseEntity.badRequest().body(response);
        }
        
        projectService.deleteProject(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Project deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/managers")
    @ResponseBody
    public ResponseEntity<List<User>> getManagers() {
        List<User> managers = userService.findAllManagers();
        return ResponseEntity.ok(managers);
    }
    
    private boolean hasAccessToProject(User user, Project project) {
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (user.getRole() == User.Role.CLIENT && project.getClient().getId().equals(user.getId())) {
            return true;
        }
        if (user.getRole() == User.Role.MANAGER
                && project.getManager() != null
                && project.getManager().getId().equals(user.getId())) {
            return true;
        }
        return false;
    }
    
    private boolean canUpdateProject(User user, Project project) {
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (user.getRole() == User.Role.CLIENT && project.getClient().getId().equals(user.getId())) {
            return true;
        }
        if (user.getRole() == User.Role.MANAGER
                && project.getManager() != null
                && project.getManager().getId().equals(user.getId())) {
            return true;
        }
        return false;
    }
}
