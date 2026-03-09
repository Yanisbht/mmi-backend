package com.mmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "commentaires")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String titre;

    @Column(columnDefinition = "TEXT")
    @NotBlank
    private String contenu;

    private LocalDateTime dateHeure;

    @ManyToOne
    @JoinColumn(name = "auteur_id")
    private User auteur;

    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @PrePersist
    public void prePersist() {
        this.dateHeure = LocalDateTime.now();
    }
}