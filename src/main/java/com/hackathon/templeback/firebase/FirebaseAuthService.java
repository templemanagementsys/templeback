package com.hackathon.templeback.firebase;


import com.google.firebase.auth.*;
import com.google.firebase.auth.FirebaseAuth;
import com.hackathon.templeback.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseAuthService {

    private final FirebaseAuth firebaseAuth;
    private final JwtService jwtTokenUtil;


    public Map<String, String> verifyAndExtractPhoneNumber(String idToken) throws FirebaseAuthException {

        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);

        Map<String, Object> claims = decodedToken.getClaims();
        String phoneNumber = (String) claims.get("phone_number");

        Boolean phoneVerified = (Boolean) claims.get("phone_verified");
        if (phoneVerified == null || !phoneVerified) {
            throw new RuntimeException("Phone number not verified in Firebase");
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new RuntimeException("Phone number not found in Firebase token");
        }

        String firebaseUid = decodedToken.getUid();


        return Map.of(
                "phoneNumber", phoneNumber,
                "firebaseUid", firebaseUid
        );
    }
    public String generateTestFirebaseToken(String phoneNumber) {
        try {
            return jwtTokenUtil.generateToken(phoneNumber, 300000L);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Firebase token: " + e.getMessage());
        }
    }

    public Map<String, String> verifyTestToken(String idToken) {
        try {
            String decodedToken =  jwtTokenUtil.extracttoken(idToken);

            return Map.of(
                    "phoneNumber", decodedToken
            );

        } catch (Exception e) {
            throw new RuntimeException("Token verification failed: " + e.getMessage());
        }
    }
    public String extractPhoneNumberFromToken(FirebaseToken decodedToken) {
        Map<String, Object> claims = decodedToken.getClaims();
        String phoneNumber = (String) claims.get("phone_number");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new RuntimeException("Phone number not found in Firebase token");
        }

        log.info("Extracted phone number from token: {}", phoneNumber);
        return phoneNumber;
    }

    public UserRecord getUserByUid(String uid) throws FirebaseAuthException {
        return firebaseAuth.getUser(uid);
    }

    public boolean doesUserExistByPhoneNumber(String phoneNumber) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByPhoneNumber(phoneNumber);
            return userRecord != null;
        } catch (FirebaseAuthException e) {
            if (e.getErrorCode().equals("user-not-found")) {
                return false;
            }
            log.error("Error checking Firebase user existence: {}", e.getMessage());
            throw new RuntimeException("Failed to check user existence: " + e.getMessage(), e);
        }
    }

    public UserRecord getUserByPhoneNumber(String phoneNumber) throws FirebaseAuthException {
        return firebaseAuth.getUserByPhoneNumber(phoneNumber);
    }

    public UserRecord createFirebaseUser(String phoneNumber) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setPhoneNumber(phoneNumber)
                .setDisabled(false);

        return firebaseAuth.createUser(request);
    }

    public boolean verifyPhoneNumberOwnership(FirebaseToken decodedToken, String phoneNumber) {
        String tokenPhoneNumber = (String) decodedToken.getClaims().get("phone_number");
        return phoneNumber.equals(tokenPhoneNumber);
    }

    public boolean isPhoneNumberVerified(FirebaseToken decodedToken) {
        Boolean phoneVerified = (Boolean) decodedToken.getClaims().get("phone_verified");
        return phoneVerified != null && phoneVerified;
    }
}
