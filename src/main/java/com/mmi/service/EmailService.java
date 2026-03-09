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
                "L'utilisateur '" + pseudoUser + "' a soumis le projet : \"" + titreProjet + "\".\n" +
                        "Connectez-vous au backoffice pour valider ou rejeter ce projet."
        );
    }

    public void notifierModificationProjet(String pseudoUser, String titreProjet) {
        notifierAdmin(
                "Projet modifié - action requise",
                "L'utilisateur '" + pseudoUser + "' a modifié le projet : \"" + titreProjet + "\".\n" +
                        "Le projet repasse en statut EN_ATTENTE."
        );
    }

    public void notifierSuppressionProjet(String pseudoUser, String titreProjet) {
        notifierAdmin(
                "Projet supprimé",
                "L'utilisateur '" + pseudoUser + "' a supprimé le projet : \"" + titreProjet + "\"."
        );
    }
}