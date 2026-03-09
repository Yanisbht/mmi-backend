package com.mmi.repository;

import com.mmi.entity.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    List<Commentaire> findByProjetId(Long projetId);
    List<Commentaire> findByAuteurId(Long auteurId);
}