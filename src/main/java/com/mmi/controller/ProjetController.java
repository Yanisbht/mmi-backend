package com.mmi.controller;

import com.mmi.dto.ProjetRequest;
import com.mmi.entity.Projet;
import com.mmi.enums.ProjetStatut;
import com.mmi.service.ProjetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;

    /**
     * GET /api/projets
     * Public - retourne uniquement les projets VALIDÉS
     */
    @GetMapping
    public ResponseEntity<List<Projet>> getPublics() {
        return ResponseEntity.ok(projetService.getAllPublics());
    }

    /**
     * GET /api/projets/{id}
     * Public
     */
    @GetMapping("/{id}")
    public ResponseEntity<Projet> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projetService.getById(id));
    }

    /**
     * GET /api/projets/mes-projets
     * Retourne les projets de l'utilisateur connecté
     */
    @GetMapping("/mes-projets")
    public ResponseEntity<List<Projet>> getMesProjets(Principal principal) {
        return ResponseEntity.ok(projetService.getMesProjets(principal.getName()));
    }

    /**
     * POST /api/projets
     * Ajouter un projet (USER, PROF, ADMIN)
     */
    @PostMapping
    public ResponseEntity<Projet> creer(@Valid @RequestBody ProjetRequest request, Principal principal) {
        return ResponseEntity.ok(projetService.creer(request, principal.getName()));
    }

    /**
     * PUT /api/projets/{id}
     * Modifier un projet (auteur ou admin/prof)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Projet> modifier(@PathVariable Long id,
                                           @Valid @RequestBody ProjetRequest request,
                                           Principal principal) {
        return ResponseEntity.ok(projetService.modifier(id, request, principal.getName()));
    }

    /**
     * DELETE /api/projets/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, Principal principal) {
        projetService.supprimer(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/projets/{id}/like
     * Liker un projet (auth requis)
     */
    @PatchMapping("/{id}/like")
    public ResponseEntity<Projet> liker(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(projetService.liker(id, principal.getName()));
    }
}