package com.mmi.controller;

import com.mmi.dto.RegisterRequest;
import com.mmi.entity.*;
import com.mmi.enums.ProjetStatut;
import com.mmi.enums.Role;
import com.mmi.repository.*;
import com.mmi.service.*;
import com.mmi.entity.Projet;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final ProjetService projetService;
    private final ActionLogRepository actionLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    /** GET /api/admin/users */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /** GET /api/admin/users/{id} */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable")));
    }

    /** POST /api/admin/users — créer un utilisateur avec rôle forcé */
    @PostMapping("/users")
    public ResponseEntity<?> creerUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /** PUT /api/admin/users/{id} — modifier un utilisateur */
    @PutMapping("/users/{id}")
    public ResponseEntity<User> modifierUser(@PathVariable Long id,
                                             @RequestBody Map<String, String> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (updates.containsKey("nom"))               user.setNom(updates.get("nom"));
        if (updates.containsKey("prenom"))            user.setPrenom(updates.get("prenom"));
        if (updates.containsKey("pseudo"))            user.setPseudo(updates.get("pseudo"));
        if (updates.containsKey("email"))             user.setEmail(updates.get("email"));
        if (updates.containsKey("telephone"))         user.setTelephone(updates.get("telephone"));
        if (updates.containsKey("classePromo"))       user.setClassePromo(updates.get("classePromo"));
        if (updates.containsKey("matiereEnseignee"))  user.setMatiereEnseignee(updates.get("matiereEnseignee"));
        if (updates.containsKey("specialisation"))    user.setSpecialisation(updates.get("specialisation"));
        if (updates.containsKey("role"))              user.setRole(Role.valueOf(updates.get("role")));
        if (updates.containsKey("password") && !updates.get("password").isBlank()) {
            user.setPassword(passwordEncoder.encode(updates.get("password")));
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    /** DELETE /api/admin/users/{id} */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> supprimerUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/admin/projets */
    @GetMapping("/projets")
    public ResponseEntity<List<Projet>> getAllProjets() {
        return ResponseEntity.ok(projetService.getAll());
    }

    /** PATCH /api/admin/projets/{id}/statut?statut=VALIDE */
    @org.springframework.transaction.annotation.Transactional
    @PatchMapping("/projets/{id}/statut")
    public ResponseEntity<Projet> changerStatut(
            @PathVariable Long id,
            @RequestParam ProjetStatut statut,
            @RequestBody(required = false) java.util.Map<String, String> body) {

        Projet projet = projetService.changerStatut(id, statut);

        // Envoyer la notification à l'auteur du projet
        if (projet.getAuteur() != null) {
            String commentaire = body != null ? body.getOrDefault("commentaire", "") : "";
            if (statut == ProjetStatut.VALIDE) {
                notificationService.notifierProjetValide(projet.getAuteur(), projet);
            } else if (statut == ProjetStatut.REJETE) {
                notificationService.notifierProjetRejete(projet.getAuteur(), projet, commentaire);
            }
        }

        return ResponseEntity.ok(projet);
    }

    /** GET /api/admin/users/{id}/historique */
    @GetMapping("/users/{id}/historique")
    public ResponseEntity<List<ActionLog>> getHistorique(@PathVariable Long id) {
        return ResponseEntity.ok(actionLogRepository.findByUserIdOrderByDateHeureDesc(id));
    }
}