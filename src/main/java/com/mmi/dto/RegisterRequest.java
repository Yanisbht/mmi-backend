package com.mmi.dto;

import com.mmi.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String nom;
    @NotBlank
    private String prenom;
    @NotBlank
    private String pseudo;
    private String classePromo;
    private String adresse;
    @Email @NotBlank
    private String email;
    @NotBlank
    private String password;
    private String telephone;
    private String matiereEnseignee;   // pour les profs
    private String specialisation;     // pour les admins
    private Role role = Role.USER;     // défaut USER, admin peut forcer PROF/ADMIN
}