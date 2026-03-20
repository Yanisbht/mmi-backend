package com.mmi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String identifiant; // email ou pseudo
    @NotBlank
    private String password;
}