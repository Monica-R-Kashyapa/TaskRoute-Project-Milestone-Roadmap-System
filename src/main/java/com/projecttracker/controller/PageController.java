package com.projecttracker.controller;

import com.projecttracker.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {
    
    @GetMapping("/")
    public String home(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        switch (user.getRole()) {
            case CLIENT:
                return "redirect:/client/dashboard";
            case MANAGER:
                return "redirect:/manager/dashboard";
            case ADMIN:
                return "redirect:/admin/dashboard";
            default:
                return "redirect:/auth/login";
        }
    }
    
    @GetMapping("/client/dashboard")
    public String clientDashboard(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.CLIENT) {
            return "redirect:/auth/login";
        }
        return "client/dashboard";
    }

    @GetMapping("/client/projects/{projectId}")
    public String clientProjectDetails(@PathVariable Long projectId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.CLIENT) {
            return "redirect:/auth/login";
        }
        model.addAttribute("projectId", projectId);
        return "client/milestones";
    }

    @GetMapping("/client/projects/{projectId}/milestones")
    public String clientProjectMilestones(@PathVariable Long projectId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.CLIENT) {
            return "redirect:/auth/login";
        }
        model.addAttribute("projectId", projectId);
        return "client/milestones";
    }
    
    @GetMapping("/manager/dashboard")
    public String managerDashboard(HttpSession session) {
        User user = (User) session.getAttribute("user");
        System.out.println("Manager dashboard accessed - User: " + (user != null ? user.getUsername() : "null") + ", Role: " + (user != null ? user.getRole() : "null"));
        
        if (user == null || user.getRole() != User.Role.MANAGER) {
            System.out.println("Access denied - redirecting to login");
            return "redirect:/auth/login";
        }
        return "manager/dashboard";
    }

    @GetMapping("/manager/projects/{projectId}/milestones")
    public String managerProjectMilestones(@PathVariable Long projectId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.MANAGER) {
            return "redirect:/auth/login";
        }
        model.addAttribute("projectId", projectId);
        return "client/milestones";
    }
    
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return "redirect:/auth/login";
        }
        return "admin/dashboard";
    }
}
