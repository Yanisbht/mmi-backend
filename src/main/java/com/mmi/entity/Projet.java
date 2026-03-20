package com.mmi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mmi.enums.Matiere;
import com.mmi.enums.ProjetStatut;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projets")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime datePublication;

    @Enumerated(EnumType.STRING)
    private Matiere matiere;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjetStatut statut = ProjetStatut.EN_ATTENTE;

    @Builder.Default
    private Integer likes = 0;

    private String urlMedia;
    private String thumbnailUrl;
    private String fichierUrl;
    private Boolean competition;

    @ElementCollection
    @CollectionTable(name = "projet_fichiers", joinColumns = @JoinColumn(name = "projet_id"))
    @Column(name = "fichier_url")
    @Builder.Default
    private List<String> fichiersUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "projet_liens", joinColumns = @JoinColumn(name = "projet_id"))
    @Column(name = "lien_url")
    @Builder.Default
    private List<String> liensUrls = new ArrayList<>();

    // Utilisateurs ayant liké (pour limiter à 1 like par user)
    @ManyToMany
    @JoinTable(
            name = "projet_likers",
            joinColumns = @JoinColumn(name = "projet_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> likers = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "projet_equipe",
            joinColumns = @JoinColumn(name = "projet_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> equipe = new ArrayList<>();

    // ── auteur : on n'utilise plus @JsonBackReference pour pouvoir le sérialiser
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "auteur_id")
    private User auteur;

    @JsonIgnore
    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL)
    private List<Commentaire> commentaires;

    @JsonIgnore
    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL)
    private List<ActionLog> actions;

    @PrePersist
    public void prePersist() {
        this.datePublication = LocalDateTime.now();
    }
}