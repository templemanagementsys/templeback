package com.hackathon.templeback.user.service;

import com.hackathon.templeback.user.model.User;
import com.hackathon.templeback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(@NonNull String userphoneno) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumber(userphoneno)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userphoneno));
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }

        return user;
    }
}
