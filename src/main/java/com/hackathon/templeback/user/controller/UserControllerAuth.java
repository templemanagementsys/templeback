package com.hackathon.templeback.user.controller;

import com.hackathon.templeback.user.annotations.SecurityAnnotations;
import com.hackathon.templeback.user.dto.AuthDTO;
import com.hackathon.templeback.user.model.User;
import com.hackathon.templeback.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserControllerAuth {

    private final UserService userService;

    // Get user profile
    @GetMapping("/me")
    @SecurityAnnotations.IsUserOrAdmin.ForMethod
    public ResponseEntity<User> getCurrentUser() {

        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }

    // Get user by ID
    @GetMapping("/{id}")
    @SecurityAnnotations.IsAdmin.ForMethod
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }


    @GetMapping
    @SecurityAnnotations.IsAdmin.ForMethod
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Update user (owner or admin)
    @PutMapping("/updateuser/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AuthDTO.UserRequestDTO userRequest) {
        UUID currentUserId = userService.getCurrentUserId();

        return ResponseEntity.ok(userService.updateUser(currentUserId, userRequest));
    }

    @PutMapping("/changepass/{id}")
    public ResponseEntity<String> changepass(
            @PathVariable UUID id,
            @Valid @RequestBody AuthDTO.ChangePasswordRequest userRequest) {

        UUID currentUserId = userService.getCurrentUserId();
        User currentUser = userService.getCurrentUser();
        if (!currentUserId.equals(id) && !currentUser.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userService.changePassword(id, userRequest.getCurrentPassword(),userRequest.getNewPassword())?"SUCCESS":"FAILED");
    }

    // Delete user (admin only)
    @DeleteMapping("deleteuser/{id}")
    @SecurityAnnotations.IsAdmin.ForMethod
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}