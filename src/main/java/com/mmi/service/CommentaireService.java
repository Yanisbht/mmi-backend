package com.mmi.service;

import com.mmi.dto.CommentaireRequest;
import com.mmi.entity.*;
import com.mmi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final UserRepository userRepository;
    private final ProjetRepository projetRepository;

    public List<Commentaire> getByProjet(Long projetId) {
        return commentaireRepository.findByProjetId(projetId);
    }

    public Commentaire ajouter(Long projetId, CommentaireRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        Commentaire commentaire = Commentaire.builder()
                .titre(request.getTitre())
                .contenu(request.getContenu())
                .auteur(user)
                .projet(projet)
                .build();

        return commentaireRepository.save(commentaire);
    }

    public void supprimer(Long id, String email) {
        Commentaire c = commentaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commentaire introuvable"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        boolean estAuteur = c.getAuteur().getId().equals(user.getId());
        boolean estAdmin = user.getRole().name().equals("ADMIN");

        if (!estAuteur && !estAdmin) {
            throw new RuntimeException("Accès refusé");
        }

        commentaireRepository.delete(c);
    }
}