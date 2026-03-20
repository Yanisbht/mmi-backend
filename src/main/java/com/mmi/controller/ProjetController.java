package com.mmi.controller;

import com.mmi.dto.ProjetRequest;
import com.mmi.entity.Projet;
import com.mmi.enums.ProjetStatut;
import com.mmi.service.ProjetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;

    /** GET /api/projets — projets validés publics */
    @GetMapping
    public ResponseEntity<List<Projet>> getPublics() {
        return ResponseEntity.ok(projetService.getAllPublics());
    }

    /** GET /api/projets/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Projet> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projetService.getById(id));
    }

    /** GET /api/projets/mes-projets */
    @GetMapping("/mes-projets")
    public ResponseEntity<List<Projet>> getMesProjets(Principal principal) {
        return ResponseEntity.ok(projetService.getMesProjets(principal.getName()));
    }

    /** GET /api/projets/{id}/liked — l'utilisateur connecté a-t-il liké ? */
    @GetMapping("/{id}/liked")
    public ResponseEntity<Map<String, Boolean>> hasLiked(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.ok(Map.of("liked", false));
        boolean liked = projetService.hasLiked(id, principal.getName());
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    /** POST /api/projets */
    @PostMapping
    public ResponseEntity<Projet> creer(@Valid @RequestBody ProjetRequest request, Principal principal) {
        return ResponseEntity.ok(projetService.creer(request, principal.getName()));
    }

    /** PUT /api/projets/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Projet> modifier(@PathVariable Long id,
                                           @Valid @RequestBody ProjetRequest request,
                                           Principal principal) {
        return ResponseEntity.ok(projetService.modifier(id, request, principal.getName()));
    }

    /** DELETE /api/projets/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, Principal principal) {
        projetService.supprimer(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    /** GET /api/projets/mes-likes — projets likés par l'utilisateur connecté */
    @GetMapping("/mes-likes")
    public ResponseEntity<List<Projet>> getMesLikes(Principal principal) {
        String email = principal.getName();
        List<Projet> likes = projetService.getAll().stream()
                .filter(p -> p.getLikers().stream()
                        .anyMatch(u -> u.getEmail().equals(email)))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(likes);
    }

    /** GET /api/projets/auteur/{userId} — projets validés d'un auteur (public) */
    @GetMapping("/auteur/{userId}")
    public ResponseEntity<List<Projet>> getByAuteur(@PathVariable Long userId) {
        return ResponseEntity.ok(
                projetService.getAll().stream()
                        .filter(p -> p.getStatut().name().equals("VALIDE")
                                && p.getAuteur() != null
                                && p.getAuteur().getId().equals(userId))
                        .collect(java.util.stream.Collectors.toList())
        );
    }

    /** PATCH /api/projets/{id}/like — like ou délike */
    @PatchMapping("/{id}/like")
    public ResponseEntity<Projet> liker(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(projetService.liker(id, principal.getName()));
    }
}