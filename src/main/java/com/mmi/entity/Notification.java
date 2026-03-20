package com.mmi.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Type : PROJET_VALIDE, PROJET_REJETE, NOUVEAU_COMMENTAIRE
    private String type;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    private boolean lu = false;

    private LocalDateTime dateHeure;

    // Lien vers le projet concerné (nullable)
    private Long projetId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id")
    private User destinataire;

    @PrePersist
    public void prePersist() {
        this.dateHeure = LocalDateTime.now();
    }
}