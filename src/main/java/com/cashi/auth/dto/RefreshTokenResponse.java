package com.cashi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // En segundos
}
