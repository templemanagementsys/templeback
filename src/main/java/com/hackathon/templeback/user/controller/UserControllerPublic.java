package com.hackathon.templeback.user.controller;


import com.google.firebase.auth.FirebaseAuthException;
import com.hackathon.templeback.firebase.FirebaseAuthService;
import com.hackathon.templeback.user.dto.AuthDTO;
import com.hackathon.templeback.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class UserControllerPublic {


    private final UserService userService;
    private final FirebaseAuthService firebaseAuthService;


    @PostMapping("/register")
    public ResponseEntity<AuthDTO.AuthResponse> register(
            @Valid @RequestBody AuthDTO.CompleteRegistrationRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthDTO.LoginRequest request) {

        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/firebase/verify")
    public ResponseEntity<AuthDTO.AuthResponse> verifyFirebaseToken(@Valid @RequestBody AuthDTO.FirebaseTokenRequest request) throws FirebaseAuthException {
        System.out.println(request);
        AuthDTO.AuthResponse response = userService.authenticateWithFirebase(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/firebase/generate-test-token")
    public ResponseEntity<?> generateTestToken(@RequestBody Map<String, String> request) {
        String phone = request.get("phoneNumber");
        String result = firebaseAuthService
                .generateTestFirebaseToken(phone);


        return ResponseEntity.ok(Map.of(
                "message", "Test credentials generated",
                "customToken", result,
                "phoneNumber", phone,
                "note", "Use 'mockIdToken' for testing /api/auth/firebase/verify endpoint"
        ));

    }
}
