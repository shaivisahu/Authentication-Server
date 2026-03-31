package com.authforge.service;

import com.authforge.dto.response.UserResponse;
import com.authforge.entity.User;
import com.authforge.exception.ResourceNotFoundException;
import com.authforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void updateRole(String uuid, User.Role newRole) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + uuid));
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + uuid));
        userRepository.delete(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .uuid(user.getUuid())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .provider(user.getProvider().name())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}
