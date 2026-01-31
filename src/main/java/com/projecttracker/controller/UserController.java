package com.projecttracker.controller;

import com.projecttracker.entity.User;
import com.projecttracker.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.badRequest().build();
        }
        
        List<User> users = userService.findAll();
        // Remove passwords from response
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.badRequest().build();
        }
        
        return userService.findById(id)
                .map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User userDetails, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            Map<String, Object> response = Map.of("error", "Access denied");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            updatedUser.setPassword(null);
            return ResponseEntity.ok(Map.of(
                "message", "User updated successfully",
                "user", updatedUser
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/managers")
    public ResponseEntity<List<User>> getManagers() {
        List<User> managers = userService.findAllManagers();
        managers.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(managers);
    }
    
    @GetMapping("/clients")
    public ResponseEntity<List<User>> getClients() {
        List<User> clients = userService.findAllClients();
        clients.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(clients);
    }
}
