package com.hackathon.templeback.user.dto;

import com.hackathon.templeback.user.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class AuthDTO {

    @Data
    public static class LoginRequest {
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class PhoneRequest {
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid phone number format")
        private String phoneNumber;
    }

    @Data
    public static class FirebaseTokenRequest {
        @NotBlank(message = "Firebase ID token is required")
        private String firebaseIdToken;
    }

    @Data
    public static class CompleteRegistrationRequest {
        @NotBlank(message = "Firebase ID token is required")
        private String firebaseIdToken;

        @NotBlank(message = "Phone number is required")
        private String phoneNumber;

        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "password is required")
        private String password;

        private String email;

        private String avatarUrl;
    }

    @Data
    public static class AuthResponse {
        private String message;
        private String token;
        private String status;
        private UserResponse user;


        public AuthResponse(String message, String token,
                            String status,UserResponse user) {
            this.message = message;
            this.token = token;
            this.status = status;
            this.user=user;
        }
    }


    @Data
    public static class UserResponse {
        private String id;
        private String email;
        private String fullName;
        private String phoneNumber;
        private List<String> roles;
        private Boolean isEmailVerified;
        private LocalDateTime createdAt;

        public UserResponse(User user) {
            this.id = user.getId().toString();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.phoneNumber = user.getPhoneNumber();
            this.roles = user.getRoles();
            this.isEmailVerified = user.getIsEmailVerified();
            this.createdAt = user.getCreatedAt();
        }

    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;

        @NotBlank
        private String newPassword;
    }

    @Data
    public static class UserRequestDTO {

        @Email(message = "Invalid email format")
        private String email;

        private String phoneNumber;

        private String fullName;

        private String avatarUrl;
    }

}

