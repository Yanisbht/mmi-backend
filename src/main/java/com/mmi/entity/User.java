package com.mmi.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String telephone;

    // Pour les profs : matière enseignée
    private String matiereEnseignee;

    // Pour les admins : spécialisation
    private String specialisation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @JsonManagedReference
    @OneToMany(mappedBy = "auteur", cascade = CascadeType.ALL)
    private List<Projet> projets;

    @OneToMany(mappedBy = "auteur", cascade = CascadeType.ALL)
    private List<Commentaire> commentaires;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ActionLog> actions;
}