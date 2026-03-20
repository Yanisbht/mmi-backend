package com.mmi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public void notifierAdmin(String sujet, String corps) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmail);
            message.setSubject("[MMI] " + sujet);
            message.setText(corps);
            mailSender.send(message);
            log.info("Email envoyé à l'admin : {}", sujet);
        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'email (mail non configuré en dev) : {}", e.getMessage());
        }
    }

    public void notifierAjoutProjet(String pseudoUser, String titreProjet) {
        notifierAdmin(
                "Nouveau projet en attente de validation",
                "Bonjour,\n\n" +
                        "L'étudiant « " + pseudoUser + " » vient de soumettre un nouveau projet :\n" +
                        "  → « " + titreProjet + " »\n\n" +
                        "Ce projet est en attente de votre validation.\n\n" +
                        "Accédez directement à la page de validation :\n" +
                        frontendUrl + "/admin/validation\n\n" +
                        "— Plateforme MMI"
        );
    }

    public void notifierModificationProjet(String pseudoUser, String titreProjet) {
        notifierAdmin(
                "Projet modifié — validation requise",
                "Bonjour,\n\n" +
                        "L'étudiant « " + pseudoUser + " » a modifié le projet :\n" +
                        "  → « " + titreProjet + " »\n\n" +
                        "Le projet est repassé en statut EN_ATTENTE et nécessite une nouvelle validation.\n\n" +
                        "Accédez directement à la page de validation :\n" +
                        frontendUrl + "/admin/validation\n\n" +
                        "— Plateforme MMI"
        );
    }

    public void notifierSuppressionProjet(String pseudoUser, String titreProjet) {
        notifierAdmin(
                "Projet supprimé",
                "Bonjour,\n\n" +
                        "L'étudiant « " + pseudoUser + " » a supprimé le projet :\n" +
                        "  → « " + titreProjet + " »\n\n" +
                        "Aucune action requise de votre part.\n\n" +
                        "Consultez l'état des projets :\n" +
                        frontendUrl + "/admin/validation\n\n" +
                        "— Plateforme MMI"
        );
    }
}