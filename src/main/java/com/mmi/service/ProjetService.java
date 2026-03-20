package com.mmi.service;

import com.mmi.dto.ProjetRequest;
import com.mmi.entity.*;
import com.mmi.enums.ProjetStatut;
import com.mmi.enums.Role;
import com.mmi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
                .thumbnailUrl(request.getThumbnailUrl())
                .fichierUrl(request.getFichierUrl())
                .competition(request.getCompetition())
                .auteur(auteur)
                .statut(ProjetStatut.EN_ATTENTE)
                .likes(0)
                .build();

        // Ajouter fichiers et liens dans les collections gérées par Hibernate
        if (request.getFichiersUrls() != null) projet.getFichiersUrls().addAll(request.getFichiersUrls());
        if (request.getLiensUrls()    != null) projet.getLiensUrls().addAll(request.getLiensUrls());

        // Résoudre les membres de l'équipe
        if (request.getEquipeIds() != null) {
            for (Long uid : request.getEquipeIds()) {
                userRepository.findById(uid).ifPresent(m -> projet.getEquipe().add(m));
            }
        }

        Projet saved = projetRepository.save(projet);
        logAction("AJOUT", "Ajout du projet \"" + projet.getTitre() + "\"", auteur, saved);

        if (auteur.getRole() == Role.USER) {
            emailService.notifierAjoutProjet(auteur.getPseudo(), projet.getTitre());
        } else {
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

        if (request.getUrlMedia()     != null) projet.setUrlMedia(request.getUrlMedia());
        if (request.getThumbnailUrl() != null) projet.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getFichierUrl()   != null) projet.setFichierUrl(request.getFichierUrl());
        if (request.getCompetition()  != null) projet.setCompetition(request.getCompetition());
        if (request.getFichiersUrls() != null) {
            projet.getFichiersUrls().clear();
            projet.getFichiersUrls().addAll(request.getFichiersUrls());
        }
        if (request.getLiensUrls() != null) {
            projet.getLiensUrls().clear();
            projet.getLiensUrls().addAll(request.getLiensUrls());
        }

        // Mise à jour équipe
        if (request.getEquipeIds() != null) {
            List<User> equipe = new ArrayList<>();
            for (Long uid : request.getEquipeIds()) {
                userRepository.findById(uid).ifPresent(equipe::add);
            }
            projet.getEquipe().clear();
            projet.getEquipe().addAll(equipe);
        }

        if (user.getRole() == Role.USER) {
            projet.setStatut(ProjetStatut.EN_ATTENTE);
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
        User user   = getUserByEmail(email);
        Projet projet = getById(id);

        boolean dejaLike = projet.getLikers().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));

        if (dejaLike) {
            // Déliker
            projet.getLikers().removeIf(u -> u.getId().equals(user.getId()));
            projet.setLikes(Math.max(0, projet.getLikes() - 1));
        } else {
            // Liker
            projet.getLikers().add(user);
            projet.setLikes(projet.getLikes() + 1);
        }

        return projetRepository.save(projet);
    }

    public boolean hasLiked(Long projetId, String email) {
        User user = getUserByEmail(email);
        Projet projet = getById(projetId);
        return projet.getLikers().stream().anyMatch(u -> u.getId().equals(user.getId()));
    }

    // ── Helpers ───────────────────────────────────────────

    private void verifierDroit(Projet projet, User user) {
        boolean estAuteur      = projet.getAuteur().getId().equals(user.getId());
        boolean estAdminOuProf = user.getRole() == Role.ADMIN || user.getRole() == Role.PROF;
        if (!estAuteur && !estAdminOuProf)
            throw new RuntimeException("Accès refusé : vous n'êtes pas l'auteur de ce projet");
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private void logAction(String type, String description, User user, Projet projet) {
        ActionLog log = ActionLog.builder()
                .typeAction(type).description(description)
                .user(user).projet(projet).build();
        actionLogRepository.save(log);
    }

    private <T> List<T> nvl(List<T> list) {
        return list != null ? list : new ArrayList<>();
    }
}