package com.mmi.controller;

import com.mmi.entity.Notification;
import com.mmi.entity.User;
import com.mmi.repository.CommentaireRepository;
import com.mmi.repository.NotificationRepository;
import com.mmi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CommentaireRepository commentaireRepository;

    /** GET /api/notifications */
    @GetMapping
    public ResponseEntity<List<Notification>> getMesNotifs(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return ResponseEntity.ok(notificationRepository.findByDestinataireOrderByDateHeureDesc(user));
    }

    /** GET /api/notifications/count */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCount(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        long count = notificationRepository.countByDestinataireAndLuFalse(user);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /** PATCH /api/notifications/lire-tout */
    @PatchMapping("/lire-tout")
    public ResponseEntity<Void> lireTout(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        List<Notification> notifs = notificationRepository.findByDestinataireOrderByDateHeureDesc(user);
        notifs.forEach(n -> n.setLu(true));
        notificationRepository.saveAll(notifs);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/notifications/{id}/lire */
    @PatchMapping("/{id}/lire")
    public ResponseEntity<Void> lire(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setLu(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/notifications/signalement
     * Signalement d'un commentaire → notif envoyée à tous les admins
     */
    @PostMapping("/signalement")
    public ResponseEntity<Void> signaler(@RequestBody Map<String, String> body, Principal principal) {
        String signaledBy  = principal.getName();
        String projetId    = body.getOrDefault("projetId", "0");
        String auteurCom   = body.getOrDefault("auteurCommentaire", "?");
        String message     = body.getOrDefault("message", "");

        long projetIdLong = 0L;
        try { projetIdLong = Long.parseLong(projetId); } catch (NumberFormatException ignored) {}

        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole().name().equals("ADMIN"))
                .toList();

        for (User admin : admins) {
            Notification notif = Notification.builder()
                    .type("SIGNALEMENT")
                    .titre("⚑ Commentaire signalé")
                    .message("Le commentaire de @" + auteurCom + " sur le projet #" + projetId
                            + " a été signalé par " + signaledBy + ".\n\nRaison : " + message)
                    .lu(false)
                    .dateHeure(LocalDateTime.now())
                    .projetId(projetIdLong)
                    .destinataire(admin)
                    .build();
            notificationRepository.save(notif);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/notifications/admin-message
     * Admin supprime un commentaire avec un message → notif envoyée à l'auteur du commentaire
     */
    @Transactional
    @PostMapping("/admin-message")
    public ResponseEntity<Void> adminMessage(@RequestBody Map<String, String> body, Principal principal) {
        String message        = body.getOrDefault("message", "").trim();
        String commentaireIdS = body.getOrDefault("commentaireId", "");
        if (message.isBlank()) return ResponseEntity.noContent().build();

        try {
            Long commentaireId = Long.parseLong(commentaireIdS);

            // Récupérer l'auteur du commentaire
            commentaireRepository.findById(commentaireId).ifPresent(commentaire -> {
                User auteur = commentaire.getAuteur();
                if (auteur == null) return;

                Notification notif = Notification.builder()
                        .type("SUPPRESSION_COMMENTAIRE")
                        .titre("Votre commentaire a été supprimé")
                        .message("Un administrateur a supprimé votre commentaire"
                                + (commentaire.getProjet() != null
                                ? " sur le projet \"" + commentaire.getProjet().getTitre() + "\""
                                : "")
                                + ".\n\nMessage de l'administrateur :\n" + message)
                        .lu(false)
                        .dateHeure(LocalDateTime.now())
                        .projetId(commentaire.getProjet() != null ? commentaire.getProjet().getId() : null)
                        .destinataire(auteur)
                        .build();

                notificationRepository.save(notif);
            });

        } catch (NumberFormatException ignored) {}

        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/notifications/equipe
     * Notifie un membre ajouté à l'équipe d'un projet
     */
    @Transactional
    @PostMapping("/equipe")
    public ResponseEntity<Void> notifEquipe(@RequestBody Map<String, Object> body,
                                            Principal principal) {
        Long membreId  = Long.parseLong(body.get("membreId").toString());
        Long projetId  = Long.parseLong(body.get("projetId").toString());
        String titre   = body.getOrDefault("projetTitre", "un projet").toString();

        String auteurPseudo = userRepository.findByEmail(principal.getName())
                .map(u -> "@" + u.getPseudo()).orElse("un étudiant");

        userRepository.findById(membreId).ifPresent(membre -> {
            Notification notif = Notification.builder()
                    .type("EQUIPE")
                    .titre("Vous avez été ajouté à un projet")
                    .message(auteurPseudo + " vous a ajouté à son projet \"" + titre + "\".")
                    .lu(false)
                    .dateHeure(java.time.LocalDateTime.now())
                    .projetId(projetId)
                    .destinataire(membre)
                    .build();
            notificationRepository.save(notif);
        });

        return ResponseEntity.noContent().build();
    }
}