package com.mmi.config;

import com.mmi.entity.*;
import com.mmi.enums.*;
import com.mmi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjetRepository projetRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // Admin
        User admin = User.builder()
                .nom("Admin").prenom("Super")
                .pseudo("admin").email("admin@mmi.fr")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN).specialisation("Gestion")
                .build();
        userRepository.save(admin);

        // Prof
        User prof = User.builder()
                .nom("Martin").prenom("Sophie")
                .pseudo("prof_sophie").email("sophie.martin@mmi.fr")
                .password(passwordEncoder.encode("prof123"))
                .role(Role.PROF).matiereEnseignee("Web Design")
                .build();
        userRepository.save(prof);

        // User étudiant
        User user = User.builder()
                .nom("Dupont").prenom("Lucas")
                .pseudo("lucas_d").email("lucas@etudiant.fr")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER).classePromo("MMI2")
                .build();
        userRepository.save(user);

        // Projets exemples
        Projet p1 = Projet.builder()
                .titre("Portfolio Web").description("Mon portfolio en React et TailwindCSS")
                .matiere(Matiere.WEB).statut(ProjetStatut.VALIDE)
                .likes(5).auteur(user).build();
        projetRepository.save(p1);

        Projet p2 = Projet.builder()
                .titre("Redesign App Mobile").description("Maquette UI/UX d'une app fitness")
                .matiere(Matiere.UI).statut(ProjetStatut.EN_ATTENTE)
                .likes(0).auteur(user).build();
        projetRepository.save(p2);

        log.info("=== DATA INITIALISÉE ===");
        log.info("Admin    : admin@mmi.fr / admin123");
        log.info("Prof     : sophie.martin@mmi.fr / prof123");
        log.info("Étudiant : lucas@etudiant.fr / user123");
        log.info("=======================");
    }
}