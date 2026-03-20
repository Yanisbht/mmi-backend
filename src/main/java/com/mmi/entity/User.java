package com.mmi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mmi.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @Column(unique = true)
    @NotBlank
    private String pseudo;

    private String classePromo;
    private String adresse;

    @Column(unique = true)
    @Email @NotBlank
    private String email;

    @JsonIgnore
    @NotBlank
    private String password;

    private String telephone;
    private String matiereEnseignee;
    private String specialisation;

    @Column(columnDefinition = "TEXT")
    private String liensProfilJson;

    private String avatarType;  // "emoji" | "image" | null (initiales)
    private String avatarValue; // emoji char ou URL image // JSON array [{titre, url}, ...] max 4

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @JsonIgnore
    @OneToMany(mappedBy = "auteur", cascade = CascadeType.ALL)
    private List<Projet> projets;

    @JsonIgnore
    @OneToMany(mappedBy = "auteur", cascade = CascadeType.ALL)
    private List<Commentaire> commentaires;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ActionLog> actions;

    // La relation ManyToMany est gérée côté Projet — on ignore ici pour éviter les cycles
    @JsonIgnore
    @ManyToMany(mappedBy = "equipe")
    private List<Projet> projetsEquipe;
}