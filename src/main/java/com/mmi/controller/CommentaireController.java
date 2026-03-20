package com.mmi.controller;

import com.mmi.dto.CommentaireRequest;
import com.mmi.entity.Commentaire;
import com.mmi.service.CommentaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projets/{projetId}/commentaires")
@RequiredArgsConstructor
public class CommentaireController {

    private final CommentaireService commentaireService;

    /**
     * GET /api/projets/{projetId}/commentaires
     */
    @GetMapping
    public ResponseEntity<List<Commentaire>> getByProjet(@PathVariable Long projetId) {
        return ResponseEntity.ok(commentaireService.getByProjet(projetId));
    }

    /**
     * POST /api/projets/{projetId}/commentaires
     */
    @PostMapping
    public ResponseEntity<Commentaire> ajouter(@PathVariable Long projetId,
                                               @Valid @RequestBody CommentaireRequest request,
                                               Principal principal) {
        return ResponseEntity.ok(commentaireService.ajouter(projetId, request, principal.getName()));
    }

    /**
     * DELETE /api/projets/{projetId}/commentaires/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long projetId,
                                          @PathVariable Long id,
                                          Principal principal) {
        commentaireService.supprimer(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}