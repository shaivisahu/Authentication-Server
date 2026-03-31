package com.authforge.controller;

import com.authforge.dto.response.ApiResponse;
import com.authforge.dto.response.UserResponse;
import com.authforge.entity.User;
import com.authforge.security.oauth2.UserPrincipal;
import com.authforge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/profile")
    public ResponseEntity<ApiResponse<UserResponse>> profile(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse user = userService.getCurrentUser(principal.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @PatchMapping("/admin/users/{uuid}/role")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @PathVariable String uuid,
            @RequestParam String role) {
        userService.updateRole(uuid, User.Role.valueOf(role));
        return ResponseEntity.ok(ApiResponse.ok("Role updated", null));
    }

    @DeleteMapping("/admin/users/{uuid}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String uuid) {
        userService.deleteUser(uuid);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }
}
