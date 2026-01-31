package com.projecttracker.service.report;

import com.itextpdf.html2pdf.HtmlConverter;
import com.opencsv.CSVWriter;
import com.projecttracker.entity.Milestone;
import com.projecttracker.entity.Project;
import com.projecttracker.entity.User;
import com.projecttracker.service.MilestoneService;
import com.projecttracker.service.ProjectService;
import com.projecttracker.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private MilestoneService milestoneService;
    
    @Autowired
    private UserService userService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public void generateProjectReport(Long projectId, HttpServletResponse response) throws IOException {
        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        List<Milestone> milestones = milestoneService.findByProject(project);
        
        String htmlContent = generateProjectReportHtml(project, milestones);
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=project_" + projectId + "_report.pdf");
        
        HtmlConverter.convertToPdf(htmlContent, response.getOutputStream());
    }
    
    public void generateClientReport(Long clientId, HttpServletResponse response) throws IOException {
        User client = userService.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        List<Project> projects = projectService.findByClient(client);
        
        String htmlContent = generateClientReportHtml(client, projects);
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=client_" + clientId + "_report.pdf");
        
        HtmlConverter.convertToPdf(htmlContent, response.getOutputStream());
    }
    
    public void generateManagerReport(Long managerId, HttpServletResponse response) throws IOException {
        User manager = userService.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        
        List<Project> projects = projectService.findByManager(manager);
        
        String htmlContent = generateManagerReportHtml(manager, projects);
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=manager_" + managerId + "_report.pdf");
        
        HtmlConverter.convertToPdf(htmlContent, response.getOutputStream());
    }
    
    public void generateAdminReport(HttpServletResponse response) throws IOException {
        List<Project> allProjects = projectService.findAll();
        List<User> allUsers = userService.findAll();
        
        String htmlContent = generateAdminReportHtml(allProjects, allUsers);
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=admin_summary_report.pdf");
        
        HtmlConverter.convertToPdf(htmlContent, response.getOutputStream());
    }
    
    private String generateProjectReportHtml(Project project, List<Milestone> milestones) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }");
        html.append("h2 { color: #34495e; margin-top: 30px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; font-weight: bold; }");
        html.append(".status-completed { color: #27ae60; font-weight: bold; }");
        html.append(".status-in-progress { color: #f39c12; font-weight: bold; }");
        html.append(".status-not-started { color: #95a5a6; font-weight: bold; }");
        html.append(".progress-bar { width: 100%; height: 20px; background-color: #ecf0f1; border-radius: 10px; }");
        html.append(".progress-fill { height: 100%; background-color: #3498db; border-radius: 10px; }");
        html.append("</style></head><body>");
        
        // Project Header
        html.append("<h1>Project Report: ").append(project.getName()).append("</h1>");
        html.append("<p><strong>Description:</strong> ").append(project.getDescription() != null ? project.getDescription() : "N/A").append("</p>");
        html.append("<p><strong>Client:</strong> ").append(project.getClient().getUsername()).append("</p>");
        html.append("<p><strong>Manager:</strong> ").append(project.getManager().getUsername()).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(project.getStatus()).append("</p>");
        html.append("<p><strong>Start Date:</strong> ").append(project.getStartDate() != null ? project.getStartDate().toString() : "N/A").append("</p>");
        html.append("<p><strong>End Date:</strong> ").append(project.getEndDate() != null ? project.getEndDate().toString() : "N/A").append("</p>");
        
        // Progress Overview
        double progress = project.getProgressPercentage();
        html.append("<h2>Progress Overview</h2>");
        html.append("<div class='progress-bar'>");
        html.append("<div class='progress-fill' style='width: ").append(progress).append("%;'></div>");
        html.append("</div>");
        html.append("<p>Overall Progress: ").append(String.format("%.1f", progress)).append("%</p>");
        
        // Milestones Table
        html.append("<h2>Milestones</h2>");
        html.append("<table>");
        html.append("<tr><th>Sequence</th><th>Title</th><th>Description</th><th>Status</th><th>Due Date</th><th>Completed Date</th></tr>");
        
        for (Milestone milestone : milestones) {
            html.append("<tr>");
            html.append("<td>").append(milestone.getSequenceOrder()).append("</td>");
            html.append("<td>").append(milestone.getTitle()).append("</td>");
            html.append("<td>").append(milestone.getDescription() != null ? milestone.getDescription() : "N/A").append("</td>");
            html.append("<td class='status-").append(milestone.getStatus().name().toLowerCase().replace("_", "-")).append("'>")
               .append(milestone.getStatus().name().replace("_", " ")).append("</td>");
            html.append("<td>").append(milestone.getDueDate() != null ? milestone.getDueDate().toString() : "N/A").append("</td>");
            html.append("<td>").append(milestone.getCompletedAt() != null ? milestone.getCompletedAt().format(DATE_FORMATTER) : "N/A").append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        
        // Summary Statistics
        long completedCount = milestones.stream().filter(m -> m.getStatus() == Milestone.MilestoneStatus.COMPLETED).count();
        long inProgressCount = milestones.stream().filter(m -> m.getStatus() == Milestone.MilestoneStatus.IN_PROGRESS).count();
        long notStartedCount = milestones.stream().filter(m -> m.getStatus() == Milestone.MilestoneStatus.NOT_STARTED).count();
        
        html.append("<h2>Summary Statistics</h2>");
        html.append("<ul>");
        html.append("<li>Total Milestones: ").append(milestones.size()).append("</li>");
        html.append("<li>Completed: ").append(completedCount).append("</li>");
        html.append("<li>In Progress: ").append(inProgressCount).append("</li>");
        html.append("<li>Not Started: ").append(notStartedCount).append("</li>");
        html.append("</ul>");
        
        html.append("<p><em>Report generated on: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("</em></p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String generateClientReportHtml(User client, List<Project> projects) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }");
        html.append("h2 { color: #34495e; margin-top: 30px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; font-weight: bold; }");
        html.append("</style></head><body>");
        
        html.append("<h1>Client Report: ").append(client.getUsername()).append("</h1>");
        html.append("<p><strong>Email:</strong> ").append(client.getEmail()).append("</p>");
        html.append("<p><strong>Total Projects:</strong> ").append(projects.size()).append("</p>");
        
        html.append("<h2>Projects Overview</h2>");
        html.append("<table>");
        html.append("<tr><th>Project Name</th><th>Manager</th><th>Status</th><th>Progress</th><th>Start Date</th><th>End Date</th></tr>");
        
        for (Project project : projects) {
            html.append("<tr>");
            html.append("<td>").append(project.getName()).append("</td>");
            html.append("<td>").append(project.getManager().getUsername()).append("</td>");
            html.append("<td>").append(project.getStatus()).append("</td>");
            html.append("<td>").append(String.format("%.1f", project.getProgressPercentage())).append("%</td>");
            html.append("<td>").append(project.getStartDate() != null ? project.getStartDate().toString() : "N/A").append("</td>");
            html.append("<td>").append(project.getEndDate() != null ? project.getEndDate().toString() : "N/A").append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("<p><em>Report generated on: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("</em></p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String generateManagerReportHtml(User manager, List<Project> projects) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("h1 { color: #2c3e50; border-bottom: 2px solid #27ae60; padding-bottom: 10px; }");
        html.append("h2 { color: #34495e; margin-top: 30px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; font-weight: bold; }");
        html.append("</style></head><body>");
        
        html.append("<h1>Manager Report: ").append(manager.getUsername()).append("</h1>");
        html.append("<p><strong>Email:</strong> ").append(manager.getEmail()).append("</p>");
        html.append("<p><strong>Assigned Projects:</strong> ").append(projects.size()).append("</p>");
        
        html.append("<h2>Projects Overview</h2>");
        html.append("<table>");
        html.append("<tr><th>Project Name</th><th>Client</th><th>Status</th><th>Progress</th><th>Start Date</th><th>End Date</th></tr>");
        
        for (Project project : projects) {
            html.append("<tr>");
            html.append("<td>").append(project.getName()).append("</td>");
            html.append("<td>").append(project.getClient().getUsername()).append("</td>");
            html.append("<td>").append(project.getStatus()).append("</td>");
            html.append("<td>").append(String.format("%.1f", project.getProgressPercentage())).append("%</td>");
            html.append("<td>").append(project.getStartDate() != null ? project.getStartDate().toString() : "N/A").append("</td>");
            html.append("<td>").append(project.getEndDate() != null ? project.getEndDate().toString() : "N/A").append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("<p><em>Report generated on: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("</em></p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String generateAdminReportHtml(List<Project> allProjects, List<User> allUsers) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("h1 { color: #2c3e50; border-bottom: 2px solid #e74c3c; padding-bottom: 10px; }");
        html.append("h2 { color: #34495e; margin-top: 30px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; font-weight: bold; }");
        html.append("</style></head><body>");
        
        html.append("<h1>Admin Summary Report</h1>");
        
        // User Statistics
        long clientCount = allUsers.stream().filter(u -> u.getRole() == User.Role.CLIENT).count();
        long managerCount = allUsers.stream().filter(u -> u.getRole() == User.Role.MANAGER).count();
        long adminCount = allUsers.stream().filter(u -> u.getRole() == User.Role.ADMIN).count();
        
        html.append("<h2>User Statistics</h2>");
        html.append("<ul>");
        html.append("<li>Total Users: ").append(allUsers.size()).append("</li>");
        html.append("<li>Clients: ").append(clientCount).append("</li>");
        html.append("<li>Managers: ").append(managerCount).append("</li>");
        html.append("<li>Admins: ").append(adminCount).append("</li>");
        html.append("</ul>");
        
        // Project Statistics
        long planningCount = allProjects.stream().filter(p -> p.getStatus() == Project.ProjectStatus.PLANNING).count();
        long inProgressCount = allProjects.stream().filter(p -> p.getStatus() == Project.ProjectStatus.IN_PROGRESS).count();
        long completedCount = allProjects.stream().filter(p -> p.getStatus() == Project.ProjectStatus.COMPLETED).count();
        long onHoldCount = allProjects.stream().filter(p -> p.getStatus() == Project.ProjectStatus.ON_HOLD).count();
        
        html.append("<h2>Project Statistics</h2>");
        html.append("<ul>");
        html.append("<li>Total Projects: ").append(allProjects.size()).append("</li>");
        html.append("<li>Planning: ").append(planningCount).append("</li>");
        html.append("<li>In Progress: ").append(inProgressCount).append("</li>");
        html.append("<li>Completed: ").append(completedCount).append("</li>");
        html.append("<li>On Hold: ").append(onHoldCount).append("</li>");
        html.append("</ul>");
        
        // Projects Table
        html.append("<h2>All Projects</h2>");
        html.append("<table>");
        html.append("<tr><th>Project Name</th><th>Client</th><th>Manager</th><th>Status</th><th>Progress</th></tr>");
        
        for (Project project : allProjects) {
            html.append("<tr>");
            html.append("<td>").append(project.getName()).append("</td>");
            html.append("<td>").append(project.getClient().getUsername()).append("</td>");
            html.append("<td>").append(project.getManager().getUsername()).append("</td>");
            html.append("<td>").append(project.getStatus()).append("</td>");
            html.append("<td>").append(String.format("%.1f", project.getProgressPercentage())).append("%</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("<p><em>Report generated on: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("</em></p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    public String generateProjectCSV(Long projectId) {
        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        List<Milestone> milestones = milestoneService.findByProject(project);
        
        try (StringWriter writer = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(writer)) {
            
            // Write header
            String[] header = {"Sequence", "Title", "Description", "Status", "Due Date", "Completed Date"};
            csvWriter.writeNext(header);
            
            // Write milestone data
            for (Milestone milestone : milestones) {
                String[] row = {
                    milestone.getSequenceOrder().toString(),
                    milestone.getTitle(),
                    milestone.getDescription() != null ? milestone.getDescription() : "",
                    milestone.getStatus().name().replace("_", " "),
                    milestone.getDueDate() != null ? milestone.getDueDate().toString() : "",
                    milestone.getCompletedAt() != null ? milestone.getCompletedAt().format(DATE_FORMATTER) : ""
                };
                csvWriter.writeNext(row);
            }
            
            // Add summary row
            String[] summary = {
                "SUMMARY",
                "Total Milestones: " + milestones.size(),
                "Completed: " + milestones.stream().filter(m -> m.getStatus() == Milestone.MilestoneStatus.COMPLETED).count(),
                "In Progress: " + milestones.stream().filter(m -> m.getStatus() == Milestone.MilestoneStatus.IN_PROGRESS).count(),
                "Not Started: " + milestones.stream().filter(m -> m.getStatus() == Milestone.MilestoneStatus.NOT_STARTED).count(),
                "Progress: " + String.format("%.1f", project.getProgressPercentage()) + "%"
            };
            csvWriter.writeNext(summary);
            
            csvWriter.flush();
            return writer.toString();
            
        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV report", e);
        }
    }
}
