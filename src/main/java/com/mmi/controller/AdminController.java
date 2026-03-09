package com.mmi.controller;

import com.mmi.dto.RegisterRequest;
import com.mmi.entity.*;
import com.mmi.enums.ProjetStatut;
import com.mmi.repository.*;
import com.mmi.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final ProjetService projetService;
    private final ActionLogRepository actionLogRepository;

    /**
     * GET /api/admin/users
     * Lister tous les utilisateurs
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable")));
    }

    /**
     * POST /api/admin/users
     * Créer un utilisateur ou prof (avec rôle forcé)
     */
    @PostMapping("/users")
    public ResponseEntity<?> creerUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> supprimerUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/admin/projets
     * Voir TOUS les projets (y compris en attente)
     */
    @GetMapping("/projets")
    public ResponseEntity<List<Projet>> getAllProjets() {
        return ResponseEntity.ok(projetService.getAll());
    }

    /**
     * PATCH /api/admin/projets/{id}/statut?statut=VALIDE
     * Valider ou rejeter un projet
     */
    @PatchMapping("/projets/{id}/statut")
    public ResponseEntity<Projet> changerStatut(@PathVariable Long id,
                                                @RequestParam ProjetStatut statut) {
        return ResponseEntity.ok(projetService.changerStatut(id, statut));
    }

    /**
     * GET /api/admin/users/{id}/historique
     * Historique des actions d'un étudiant
     */
    @GetMapping("/users/{id}/historique")
    public ResponseEntity<List<ActionLog>> getHistorique(@PathVariable Long id) {
        return ResponseEntity.ok(actionLogRepository.findByUserIdOrderByDateHeureDesc(id));
    }
}