package com.mmi.config;

import com.mmi.entity.*;
import com.mmi.enums.*;
import com.mmi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository    userRepository;
    private final ProjetRepository  projetRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // ── ADMIN ─────────────────────────────────────────────
        User admin = User.builder()
                .nom("Admin").prenom("Super")
                .pseudo("admin").email("admin@mmi.fr")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN).specialisation("Gestion plateforme")
                .build();
        userRepository.save(admin);

        // ── PROF ──────────────────────────────────────────────
        User prof = User.builder()
                .nom("Martin").prenom("Sophie")
                .pseudo("prof_sophie").email("sophie.martin@mmi.fr")
                .password(passwordEncoder.encode("prof123"))
                .role(Role.PROF).matiereEnseignee("Web Design & UX")
                .build();
        userRepository.save(prof);

        // ── USER 1 ────────────────────────────────────────────
        User lucas = User.builder()
                .nom("Dupont").prenom("Lucas")
                .pseudo("lucas_d").email("lucas@etudiant.fr")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER).classePromo("MMI2")
                .build();
        userRepository.save(lucas);

        // ── USER 2 ────────────────────────────────────────────
        User emma = User.builder()
                .nom("Richard").prenom("Emma")
                .pseudo("emma_r").email("emma@etudiant.fr")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER).classePromo("MMI1")
                .telephone("06 12 34 56 78")
                .build();
        userRepository.save(emma);

        // ── USER 3 ────────────────────────────────────────────
        User thomas = User.builder()
                .nom("Vidal").prenom("Thomas")
                .pseudo("thomas_v").email("thomas@etudiant.fr")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER).classePromo("MMI2")
                .build();
        userRepository.save(thomas);

        // ── PROJET 1 : Portfolio Web — validé, compétition, équipe ──
        Projet p1 = Projet.builder()
                .titre("Portfolio Interactif")
                .description(
                        "Portfolio personnel développé en React et TailwindCSS avec animations GSAP. " +
                                "Le projet inclut une page d'accueil animée, une section projets filtrable par catégorie, " +
                                "un système de mode sombre/clair et un formulaire de contact fonctionnel. " +
                                "Réalisé en 3 semaines dans le cadre du cours de développement web avancé."
                )
                .matiere(Matiere.WEB)
                .statut(ProjetStatut.VALIDE)
                .competition(true)
                .likes(7)
                .auteur(lucas)
                .urlMedia("https://lucas-portfolio.fr")
                .build();

        // Équipe : lucas + emma
        p1.getEquipe().add(emma);
        p1.getLiensUrls().add("https://lucas-portfolio.fr");
        p1.getLiensUrls().add("https://github.com/lucas-d/portfolio");
        projetRepository.save(p1);

        // ── PROJET 2 : Maquette UX — en attente, solo ──
        Projet p2 = Projet.builder()
                .titre("Redesign App Fitness")
                .description(
                        "Refonte complète de l'interface d'une application fitness mobile. " +
                                "Le projet couvre la recherche utilisateur (interviews, personas), " +
                                "l'architecture d'information, les wireframes basse fidélité et " +
                                "un prototype haute fidélité sur Figma. Axé sur l'accessibilité et l'ergonomie."
                )
                .matiere(Matiere.UI)
                .statut(ProjetStatut.EN_ATTENTE)
                .competition(false)
                .likes(0)
                .auteur(emma)
                .build();

        p2.getLiensUrls().add("https://figma.com/file/fitness-redesign");
        projetRepository.save(p2);

        // ── PROJET 3 : Projet 3D — validé par prof ──
        Projet p3 = Projet.builder()
                .titre("Environnement 3D Low Poly")
                .description(
                        "Création d'un environnement 3D low poly d'une forêt enchantée sous Blender. " +
                                "Le rendu final est exporté en format GLB pour une visualisation web via Three.js. " +
                                "Le projet explore les techniques de lighting volumétrique et les shaders personnalisés."
                )
                .matiere(Matiere.TROIS_D)
                .statut(ProjetStatut.VALIDE)
                .competition(false)
                .likes(12)
                .auteur(thomas)
                .build();

        projetRepository.save(p3);

        log.info("╔══════════════════════════════════════════╗");
        log.info("║         DONNÉES INITIALISÉES             ║");
        log.info("╠══════════════════════════════════════════╣");
        log.info("║  ADMIN  : admin@mmi.fr       / admin123  ║");
        log.info("║  PROF   : sophie.martin@mmi.fr/ prof123  ║");
        log.info("║  USER 1 : lucas@etudiant.fr  / user123   ║");
        log.info("║  USER 2 : emma@etudiant.fr   / user123   ║");
        log.info("║  USER 3 : thomas@etudiant.fr / user123   ║");
        log.info("╠══════════════════════════════════════════╣");
        log.info("║  3 projets créés (VALIDE / EN_ATTENTE)   ║");
        log.info("╚══════════════════════════════════════════╝");
    }
}