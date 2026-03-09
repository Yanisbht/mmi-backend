package com.mmi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_logs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeAction; // AJOUT, MODIFICATION, SUPPRESSION

    private String description;

    private LocalDateTime dateHeure;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @PrePersist
    public void prePersist() {
        this.dateHeure = LocalDateTime.now();
    }
}