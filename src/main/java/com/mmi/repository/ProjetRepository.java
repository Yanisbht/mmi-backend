package com.mmi.repository;

import com.mmi.entity.Projet;
import com.mmi.enums.ProjetStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {
    List<Projet> findByStatut(ProjetStatut statut);
    List<Projet> findByAuteurId(Long auteurId);
    List<Projet> findByStatutOrderByDatePublicationDesc(ProjetStatut statut);
}