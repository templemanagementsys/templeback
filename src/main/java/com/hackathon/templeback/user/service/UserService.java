package com.hackathon.templeback.user.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.hackathon.templeback.JwtService;
import com.hackathon.templeback.firebase.FirebaseAuthService;
import com.hackathon.templeback.user.dto.AuthDTO;
import com.hackathon.templeback.user.model.User;
import com.hackathon.templeback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtTokenUtil;
    private final FirebaseAuthService firebaseAuthService;

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.CompleteRegistrationRequest request) {

        String phone=jwtTokenUtil.extracttoken(request.getFirebaseIdToken());
        System.out.println(request);
        if(!firebaseAuthService.doesUserExistByPhoneNumber(phone)){
            throw new RuntimeException("Phone No is not verifed");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRoles(List.of("ROLE_USER"));
        user.setIsActive(true);
        user.setIsEmailVerified(false);

        userRepository.save(user);

        String accessToken = jwtTokenUtil.generateToken(phone);

        return new AuthDTO.AuthResponse("SUCCESSFUL",accessToken,"REGISTERED", new AuthDTO.UserResponse(user));
    }

//    @Transactional
//    public AuthDTO.TokenResponse login(AuthDTO.LoginRequest request) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        User user = (User) authentication.getPrincipal();
//
//        user.setLastLoginAt(LocalDateTime.now());
//        userRepository.save(user);
//
//
//        String accessToken = jwtTokenUtil.generateToken(user.getEmail());
//        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());
//
//
//        user.setRefreshToken(refreshToken);
//        userRepository.save(user);
//
//        return new AuthDTO.TokenResponse(
//                accessToken,
//                refreshToken,
//                jwtTokenUtil.getJwtExpirationMs(),
//                new AuthDTO.UserResponse(user)
//        );
//    }

    @Transactional
    public void logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.save(user);
    }

    @Transactional
    public boolean changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public User updateUser(UUID id, AuthDTO.UserRequestDTO userRequest) {
        User existingUser = getUserById(id);

        if (!existingUser.getEmail().equals(userRequest.getEmail())
                && userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already taken");
        }
        if(userRequest.getEmail()!=null){
            existingUser.setEmail(userRequest.getEmail());
        }
        if (userRequest.getFullName()!=null) {
            existingUser.setPhoneNumber(userRequest.getPhoneNumber());
        }
        if (userRequest.getFullName()!=null) {
            existingUser.setFullName(userRequest.getFullName());
        }
        if (userRequest.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(userRequest.getAvatarUrl());
        }

        return userRepository.save(existingUser);
    }

    // Delete User
    @Transactional
    public void deleteUser(UUID id) {
        User user = getUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public String login(AuthDTO.LoginRequest request) {
        System.out.println(request);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);


        return jwtTokenUtil.generateToken(user.getPhoneNumber());
    }


    @Transactional
    public AuthDTO.AuthResponse authenticateWithFirebase(
            AuthDTO.FirebaseTokenRequest request) throws FirebaseAuthException {
        //deployment  Map<String,String> decoded = firebaseAuthService.verifyAndExtractPhoneNumber(request.getFirebaseIdToken());
        Map<String,String> decoded = firebaseAuthService.verifyTestToken(request.getFirebaseIdToken());
        String firebaseUid = decoded.get("firebaseUid");
        String phoneNumber = decoded.get("phoneNumber");
        System.out.println(phoneNumber);

        Optional<User> existingUser = userRepository.findByPhoneNumber(phoneNumber);
        System.out.println(existingUser);
        if (existingUser.isPresent()) {
            User user = existingUser.get();

            user.setLastLoginAt(LocalDateTime.now());

            String accessToken = jwtTokenUtil.generateToken(phoneNumber);

            userRepository.save(user);

            AuthDTO.UserResponse userResponse = new AuthDTO.UserResponse(user);

            return new AuthDTO.AuthResponse(
                    "Login successful",
                    accessToken,
                    "ACTIVE",
                    userResponse
            );

        } else {

            String temporaryToken = jwtTokenUtil.generateToken(phoneNumber, 300000L);
            firebaseAuthService.createFirebaseUser(phoneNumber);

            return new AuthDTO.AuthResponse(
                    "Please complete registration",
                    temporaryToken,
                    "NEW_USER",null);
        }

    }

    public boolean isOwner(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();

        User currentUser = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return currentUser.getId().equals(userId);
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String phone = authentication.getName();

        return userRepository.findByPhoneNumber(phone)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String phone = authentication.getName();
        return userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

