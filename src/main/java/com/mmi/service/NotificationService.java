package com.mmi.service;

import com.mmi.entity.*;
import com.mmi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ── Récupérer les notifs d'un user ────────────────────
    public List<Notification> getMesNotifs(Long userId) {
        return notificationRepository.findByDestinataireIdOrderByDateHeureDesc(userId);
    }

    public long countNonLues(Long userId) {
        return notificationRepository.countByDestinataireIdAndLuFalse(userId);
    }

    @Transactional
    public void marquerToutesLues(Long userId) {
        notificationRepository.marquerToutesLues(userId);
    }

    @Transactional
    public void marquerLue(Long notifId) {
        notificationRepository.findById(notifId).ifPresent(n -> {
            n.setLu(true);
            notificationRepository.save(n);
        });
    }

    // ── Créer des notifications ───────────────────────────

    @Transactional
    public void notifierProjetValide(User destinataire, Projet projet) {
        creer(
                "PROJET_VALIDE",
                "Projet validé ✓",
                "Votre projet \"" + projet.getTitre() + "\" a été validé et est maintenant public.",
                destinataire,
                projet.getId()
        );
    }

    @Transactional
    public void notifierProjetRejete(User destinataire, Projet projet, String messageAdmin) {
        String msg = "Votre projet \"" + projet.getTitre() + "\" a été refusé.";
        if (messageAdmin != null && !messageAdmin.isBlank()) {
            msg += "\n\nMessage de l'administrateur : " + messageAdmin;
        }
        creer("PROJET_REJETE", "Projet refusé", msg, destinataire, projet.getId());
    }

    @Transactional
    public void notifierNouveauCommentaire(User destinataire, Projet projet, User auteurCommentaire) {
        // Ne pas notifier si c'est l'auteur qui commente son propre projet
        if (destinataire.getId().equals(auteurCommentaire.getId())) return;

        creer(
                "NOUVEAU_COMMENTAIRE",
                "Nouveau commentaire",
                "@" + auteurCommentaire.getPseudo() + " a commenté votre projet \"" + projet.getTitre() + "\".",
                destinataire,
                projet.getId()
        );
    }

    // ── Utilitaire interne ────────────────────────────────
    private void creer(String type, String titre, String message, User destinataire, Long projetId) {
        Notification notif = Notification.builder()
                .type(type)
                .titre(titre)
                .message(message)
                .destinataire(destinataire)
                .projetId(projetId)
                .lu(false)
                .build();
        notificationRepository.save(notif);
    }
}