package com.mmi.controller;

import com.mmi.entity.User;
import com.mmi.repository.UserRepository;
import com.mmi.repository.CommentaireRepository;
import com.mmi.entity.Commentaire;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final CommentaireRepository commentaireRepository;

    /** GET /api/users/me */
    @GetMapping("/me")
    public ResponseEntity<User> getMe(Principal principal) {
        return ResponseEntity.ok(getUser(principal));
    }

    /** PUT /api/users/me */
    @PutMapping("/me")
    public ResponseEntity<User> updateMe(@RequestBody Map<String, String> updates, Principal principal) {
        User user = getUser(principal);
        if (updates.containsKey("nom"))       user.setNom(updates.get("nom"));
        if (updates.containsKey("prenom"))    user.setPrenom(updates.get("prenom"));
        if (updates.containsKey("pseudo"))    user.setPseudo(updates.get("pseudo"));
        if (updates.containsKey("adresse"))   user.setAdresse(updates.get("adresse"));
        if (updates.containsKey("telephone")) user.setTelephone(updates.get("telephone"));
        if (updates.containsKey("password"))  user.setPassword(passwordEncoder.encode(updates.get("password")));
        return ResponseEntity.ok(userRepository.save(user));
    }

    /**
     * PUT /api/users/me/liens
     * Sauvegarde les liens profil — body: [{titre, url}, ...]
     */
    @PutMapping("/me/liens")
    public ResponseEntity<User> updateLiens(@RequestBody List<Map<String, String>> liens, Principal principal) {
        User user = getUser(principal);
        try {
            // Limiter à 4 liens max, valider que titre et url sont présents
            List<Map<String, String>> valides = liens.stream()
                    .filter(l -> l.containsKey("titre") && l.containsKey("url")
                            && !l.get("titre").isBlank() && !l.get("url").isBlank())
                    .limit(4)
                    .collect(Collectors.toList());

            user.setLiensProfilJson(objectMapper.writeValueAsString(valides));
            return ResponseEntity.ok(userRepository.save(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/users/mes-commentaires
     * Commentaires postés par l'utilisateur connecté (avec le projet associé)
     */
    @GetMapping("/mes-commentaires")
    public ResponseEntity<List<Commentaire>> getMesCommentaires(Principal principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(commentaireRepository.findByAuteurId(user.getId()));
    }

    /**
     * GET /api/users/search?q=lucas
     * Recherche d'étudiants par nom, prénom ou pseudo
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestParam("q") String q, Principal principal) {
        String query = q.toLowerCase().trim();
        if (query.length() < 2) return ResponseEntity.ok(List.of());

        List<Map<String, Object>> results = userRepository.findAll().stream()
                .filter(u -> !u.getEmail().equals(principal.getName()))
                .filter(u -> u.getRole().name().equals("USER"))
                .filter(u ->
                        (u.getNom()    != null && u.getNom().toLowerCase().contains(query)) ||
                                (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(query)) ||
                                (u.getPseudo() != null && u.getPseudo().toLowerCase().contains(query))
                )
                .limit(8)
                .map(u -> Map.of(
                        "id",          (Object) u.getId(),
                        "nom",         u.getNom()        != null ? u.getNom()        : "",
                        "prenom",      u.getPrenom()     != null ? u.getPrenom()     : "",
                        "pseudo",      u.getPseudo()     != null ? u.getPseudo()     : "",
                        "classePromo", u.getClassePromo() != null ? u.getClassePromo() : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/users/profil/{pseudo}
     * Profil public d'un utilisateur par son pseudo — sans auth
     */
    @GetMapping("/profil/{pseudo}")
    public ResponseEntity<User> getProfilPublic(@PathVariable String pseudo) {
        return userRepository.findByPseudo(pseudo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/users/me/avatar
     * Changer son avatar (emoji ou image uploadée)
     * Body: { avatarType: "emoji"|"image"|"", avatarValue: "🐱"|"/uploads/avatars/uuid.jpg" }
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<User> updateAvatar(@RequestBody java.util.Map<String, String> body,
                                             Principal principal) {
        User user = getUser(principal);
        String type  = body.getOrDefault("avatarType",  "").trim();
        String value = body.getOrDefault("avatarValue", "").trim();

        if (type.isBlank() || value.isBlank()) {
            // Remettre les initiales
            user.setAvatarType(null);
            user.setAvatarValue(null);
        } else {
            user.setAvatarType(type);
            user.setAvatarValue(value);
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    private User getUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }
}