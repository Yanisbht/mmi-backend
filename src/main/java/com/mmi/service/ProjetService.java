package com.mmi.service;

import com.mmi.dto.ProjetRequest;
import com.mmi.entity.*;
import com.mmi.enums.ProjetStatut;
import com.mmi.enums.Role;
import com.mmi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final UserRepository userRepository;
    private final ActionLogRepository actionLogRepository;
    private final EmailService emailService;

    public List<Projet> getAllPublics() {
        return projetRepository.findByStatutOrderByDatePublicationDesc(ProjetStatut.VALIDE);
    }

    public List<Projet> getAll() {
        return projetRepository.findAll();
    }

    public Projet getById(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet introuvable : " + id));
    }

    public List<Projet> getMesProjets(String email) {
        User user = getUserByEmail(email);
        return projetRepository.findByAuteurId(user.getId());
    }

    @Transactional
    public Projet creer(ProjetRequest request, String email) {
        User auteur = getUserByEmail(email);

        Projet projet = Projet.builder()
                .titre(request.getTitre())
                .description(request.getDescription())
                .matiere(request.getMatiere())
                .urlMedia(request.getUrlMedia())
                .auteur(auteur)
                .statut(ProjetStatut.EN_ATTENTE)
                .likes(0)
                .build();

        Projet saved = projetRepository.save(projet);

        // Log
        logAction("AJOUT", "Ajout du projet \"" + projet.getTitre() + "\"", auteur, saved);

        // Notif mail admin (sauf si admin ou prof, qui peuvent valider eux-mêmes)
        if (auteur.getRole() == Role.USER) {
            emailService.notifierAjoutProjet(auteur.getPseudo(), projet.getTitre());
        } else {
            // Prof et Admin => validation automatique
            saved.setStatut(ProjetStatut.VALIDE);
            projetRepository.save(saved);
        }

        return saved;
    }

    @Transactional
    public Projet modifier(Long id, ProjetRequest request, String email) {
        User user = getUserByEmail(email);
        Projet projet = getById(id);

        verifierDroit(projet, user);

        projet.setTitre(request.getTitre());
        projet.setDescription(request.getDescription());
        projet.setMatiere(request.getMatiere());
        projet.setUrlMedia(request.getUrlMedia());

        if (user.getRole() == Role.USER) {
            projet.setStatut(ProjetStatut.EN_ATTENTE); // repasse en validation
            emailService.notifierModificationProjet(user.getPseudo(), projet.getTitre());
        }

        Projet saved = projetRepository.save(projet);
        logAction("MODIFICATION", "Modification du projet \"" + projet.getTitre() + "\"", user, saved);
        return saved;
    }

    @Transactional
    public void supprimer(Long id, String email) {
        User user = getUserByEmail(email);
        Projet projet = getById(id);

        verifierDroit(projet, user);

        logAction("SUPPRESSION", "Suppression du projet \"" + projet.getTitre() + "\"", user, projet);
        if (user.getRole() == Role.USER) {
            emailService.notifierSuppressionProjet(user.getPseudo(), projet.getTitre());
        }

        projetRepository.delete(projet);
    }

    @Transactional
    public Projet changerStatut(Long id, ProjetStatut statut) {
        Projet projet = getById(id);
        projet.setStatut(statut);
        return projetRepository.save(projet);
    }

    @Transactional
    public Projet liker(Long id, String email) {
        User user = getUserByEmail(email);
        Projet projet = getById(id);
        projet.setLikes(projet.getLikes() + 1);
        return projetRepository.save(projet);
    }

    // ---- Helpers ----

    private void verifierDroit(Projet projet, User user) {
        boolean estAuteur = projet.getAuteur().getId().equals(user.getId());
        boolean estAdminOuProf = user.getRole() == Role.ADMIN || user.getRole() == Role.PROF;
        if (!estAuteur && !estAdminOuProf) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas l'auteur de ce projet");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private void logAction(String type, String description, User user, Projet projet) {
        ActionLog log = ActionLog.builder()
                .typeAction(type)
                .description(description)
                .user(user)
                .projet(projet)
                .build();
        actionLogRepository.save(log);
    }
}