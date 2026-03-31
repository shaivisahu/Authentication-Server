package com.authforge.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String uuid;
    private String username;
    private String email;
    private String role;
    private String provider;
    private boolean emailVerified;
    private String createdAt;
}
