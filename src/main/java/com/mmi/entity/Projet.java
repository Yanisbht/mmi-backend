package com.mmi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mmi.enums.Matiere;
import com.mmi.enums.ProjetStatut;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
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

    private String urlMedia; // lien vers le rendu (image, vidéo, etc.)

    // ---------------------------------------------------------------
    // Thumbnail — image de couverture affichée sur les cards
    // Formats acceptés : jpg, jpeg, png — max 5MB
    // ---------------------------------------------------------------
    private String thumbnailUrl;

    // ---------------------------------------------------------------
    // Fichier principal du projet
    // Formats acceptés : pdf, mp4, glb, gltf, png, jpg, jpeg
    // ---------------------------------------------------------------
    private String fichierUrl;      // URL Cloudinary du fichier
    private String fichierNom;      // Nom original du fichier (ex: "rendu_final.pdf")
    private String fichierType;     // Extension : "pdf", "mp4", "glb", "gltf", "png", "jpg", "jpeg"

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "auteur_id")
    private User auteur;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL)
    private List<Commentaire> commentaires;

    @PrePersist
    public void prePersist() {
        this.datePublication = LocalDateTime.now();
    }
}
