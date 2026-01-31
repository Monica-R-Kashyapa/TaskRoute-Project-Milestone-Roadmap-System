package com.projecttracker.controller;

import com.projecttracker.entity.User;
import com.projecttracker.service.report.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/api/reports")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @GetMapping("/project/{projectId}")
    public void generateProjectReport(@PathVariable Long projectId, 
                                    HttpServletResponse response, 
                                    HttpSession session) throws IOException {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        try {
            reportService.generateProjectReport(projectId, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    @GetMapping("/client/{clientId}")
    public void generateClientReport(@PathVariable Long clientId, 
                                   HttpServletResponse response, 
                                   HttpSession session) throws IOException {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Check if user has access to this report
        if (currentUser.getRole() != User.Role.ADMIN && 
            !currentUser.getId().equals(clientId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            reportService.generateClientReport(clientId, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    @GetMapping("/manager/{managerId}")
    public void generateManagerReport(@PathVariable Long managerId, 
                                    HttpServletResponse response, 
                                    HttpSession session) throws IOException {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Check if user has access to this report
        if (currentUser.getRole() != User.Role.ADMIN && 
            !currentUser.getId().equals(managerId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            reportService.generateManagerReport(managerId, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    @GetMapping("/admin/all")
    public void generateAdminReport(HttpServletResponse response, 
                                  HttpSession session) throws IOException {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        try {
            reportService.generateAdminReport(response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    @GetMapping("/csv/project/{projectId}")
    public ResponseEntity<String> generateProjectCSV(@PathVariable Long projectId, 
                                                    HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String csv = reportService.generateProjectCSV(projectId);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=project_" + projectId + "_report.csv")
                    .header("Content-Type", "text/csv")
                    .body(csv);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
