package com.projecttracker.controller;

import com.projecttracker.entity.User;
import com.projecttracker.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        
        try {
            User createdUser = userService.createUser(user);
            response.put("message", "User registered successfully");
            response.put("userId", createdUser.getId().toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "API is working");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        System.out.println("Login attempt received: " + loginData.get("username"));
        Map<String, Object> response = new HashMap<>();
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        try {
            if (userService.validateUser(username, password)) {
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    session.setAttribute("user", user);
                    
                    // Set up Spring Security context
                    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole().toString())
                        .build();
                    
                    Authentication authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                        .authenticated(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    response.put("message", "Login successful");
                    response.put("userId", user.getId());
                    response.put("username", user.getUsername());
                    response.put("role", user.getRole().toString());
                    String redirectUrl = getRedirectUrl(user.getRole());
                    response.put("redirectUrl", redirectUrl);
                    
                    System.out.println("Login successful for user: " + username);
                    System.out.println("Redirect URL: " + redirectUrl);
                    System.out.println("Full response: " + response);
                    return ResponseEntity.ok(response);
                }
            }
            
            System.out.println("Login failed for user: " + username);
            response.put("error", "Invalid username or password");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
    
    private String getRedirectUrl(User.Role role) {
        switch (role) {
            case CLIENT:
                return "/client/dashboard";
            case MANAGER:
                return "/manager/dashboard";
            case ADMIN:
                return "/admin/dashboard";
            default:
                return "/";
        }
    }
}
