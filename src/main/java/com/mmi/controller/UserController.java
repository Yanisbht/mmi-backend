package com.mmi.controller;

import com.mmi.entity.User;
import com.mmi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * GET /api/users/me
     * Profil de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<User> getMe(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/users/me
     * Modifier son propre profil
     * Body (partial update): { nom, prenom, pseudo, adresse, telephone, password }
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateMe(@RequestBody Map<String, String> updates, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (updates.containsKey("nom")) user.setNom(updates.get("nom"));
        if (updates.containsKey("prenom")) user.setPrenom(updates.get("prenom"));
        if (updates.containsKey("pseudo")) user.setPseudo(updates.get("pseudo"));
        if (updates.containsKey("adresse")) user.setAdresse(updates.get("adresse"));
        if (updates.containsKey("telephone")) user.setTelephone(updates.get("telephone"));
        if (updates.containsKey("password")) {
            user.setPassword(passwordEncoder.encode(updates.get("password")));
        }

        return ResponseEntity.ok(userRepository.save(user));
    }
}