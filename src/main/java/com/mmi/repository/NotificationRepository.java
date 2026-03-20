package com.mmi.repository;

import com.mmi.entity.Notification;
import com.mmi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ── Par objet User (utilisé dans NotificationController) ──────────────
    List<Notification> findByDestinataireOrderByDateHeureDesc(User destinataire);
    long countByDestinataireAndLuFalse(User destinataire);

    // ── Par userId Long (utilisé dans NotificationService) ────────────────
    List<Notification> findByDestinataireIdOrderByDateHeureDesc(Long userId);
    long countByDestinataireIdAndLuFalse(Long userId);

    // ── Marquer toutes les notifs d'un user comme lues ────────────────────
    @Modifying
    @Query("UPDATE Notification n SET n.lu = true WHERE n.destinataire.id = :userId")
    void marquerToutesLues(@Param("userId") Long userId);
}