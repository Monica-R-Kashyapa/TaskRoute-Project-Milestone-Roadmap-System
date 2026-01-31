package com.projecttracker.controller;

import com.projecttracker.entity.Milestone;
import com.projecttracker.entity.Project;
import com.projecttracker.entity.User;
import com.projecttracker.service.MilestoneService;
import com.projecttracker.service.ProjectService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/milestones")
public class MilestoneController {
    
    @Autowired
    private MilestoneService milestoneService;
    
    @Autowired
    private ProjectService projectService;
    
    @GetMapping("/project/{projectId}")
    @ResponseBody
    public ResponseEntity<List<Milestone>> getMilestonesByProject(@PathVariable Long projectId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Project> projectOpt = projectService.findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if (hasAccessToProject(currentUser, project)) {
                List<Milestone> milestones = milestoneService.findByProject(project);
                return ResponseEntity.ok(milestones);
            }
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Milestone> getMilestoneById(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Milestone> milestoneOpt = milestoneService.findById(id);
        if (milestoneOpt.isPresent()) {
            Milestone milestone = milestoneOpt.get();
            if (hasAccessToProject(currentUser, milestone.getProject())) {
                return ResponseEntity.ok(milestone);
            }
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createMilestone(@RequestBody Map<String, Object> milestoneData, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        Long projectId = null;
        if (milestoneData.get("project") instanceof Map<?, ?> projectObj) {
            Object idObj = ((Map<?, ?>) projectObj).get("id");
            if (idObj != null && !idObj.toString().isBlank()) {
                projectId = Long.valueOf(idObj.toString());
            }
        } else if (milestoneData.get("projectId") != null && !milestoneData.get("projectId").toString().isBlank()) {
            projectId = Long.valueOf(milestoneData.get("projectId").toString());
        }

        if (projectId == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Project ID is required");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Project> projectOpt = projectService.findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if (canManageMilestones(currentUser, project)) {
                Milestone milestone = new Milestone();
                milestone.setProject(project);
                milestone.setTitle((String) milestoneData.get("title"));
                milestone.setDescription((String) milestoneData.get("description"));

                Object statusObj = milestoneData.get("status");
                if (statusObj != null && !statusObj.toString().isBlank()) {
                    milestone.setStatus(Milestone.MilestoneStatus.valueOf(statusObj.toString()));
                }

                Object dueDateObj = milestoneData.get("dueDate");
                if (dueDateObj != null && !dueDateObj.toString().isBlank()) {
                    milestone.setDueDate(LocalDate.parse(dueDateObj.toString()));
                }

                Milestone createdMilestone = milestoneService.createMilestone(milestone);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Milestone created successfully");
                response.put("milestone", createdMilestone);
                return ResponseEntity.ok(response);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Project not found or access denied");
        return ResponseEntity.badRequest().body(response);
    }
    
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMilestone(@PathVariable Long id, @RequestBody Milestone milestoneDetails, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Milestone> milestoneOpt = milestoneService.findById(id);
        if (milestoneOpt.isPresent()) {
            Milestone milestone = milestoneOpt.get();
            if (canManageMilestones(currentUser, milestone.getProject())) {
                Milestone updatedMilestone = milestoneService.updateMilestone(id, milestoneDetails);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Milestone updated successfully");
                response.put("milestone", updatedMilestone);
                return ResponseEntity.ok(response);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Milestone not found or access denied");
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteMilestone(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Milestone> milestoneOpt = milestoneService.findById(id);
        if (milestoneOpt.isPresent()) {
            Milestone milestone = milestoneOpt.get();
            if (canManageMilestones(currentUser, milestone.getProject())) {
                milestoneService.deleteMilestone(id);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Milestone deleted successfully");
                return ResponseEntity.ok(response);
            }
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "Milestone not found or access denied");
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/progress/{projectId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProjectProgress(@PathVariable Long projectId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Project> projectOpt = projectService.findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if (hasAccessToProject(currentUser, project)) {
                double progress = milestoneService.getProjectProgress(projectId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("projectId", projectId);
                response.put("progress", progress);
                return ResponseEntity.ok(response);
            }
        }
        
        return ResponseEntity.notFound().build();
    }
    
    private boolean hasAccessToProject(User user, Project project) {
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (user.getRole() == User.Role.CLIENT
                && project.getClient() != null
                && project.getClient().getId().equals(user.getId())) {
            return true;
        }
        if (user.getRole() == User.Role.MANAGER
                && project.getManager() != null
                && project.getManager().getId().equals(user.getId())) {
            return true;
        }
        return false;
    }
    
    private boolean canManageMilestones(User user, Project project) {
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (user.getRole() == User.Role.MANAGER
                && project.getManager() != null
                && project.getManager().getId().equals(user.getId())) {
            return true;
        }
        if (user.getRole() == User.Role.CLIENT
                && project.getClient() != null
                && project.getClient().getId().equals(user.getId())) {
            return true;
        }
        return false;
    }
}
