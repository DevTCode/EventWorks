package com.event.GestionEvents.Repository;

import com.event.GestionEvents.Entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    //countByEtatIgnoreCase(String etat) est une méthode de requête automatique de Spring Data
    // JPA qui compte le nombre d'entités dans une table de base de données où le champ etat
    // correspond à une valeur spécifiée (etat), en ignorant la casse des caractères pour cette
    // valeur.
    //ca marche juste avec etat si l entite contient att etat
    long countByEtatIgnoreCase(String etat);

    //pour récupérer tous les événements dont la date est postérieure à une date donnée jai utilisé
    //findByDateAfter
    List<Event> findByDateAfterOrderByDateDesc(Date date);


    //liste des evenements chez le chef RH

    Page<Event> findAllByOrderByDateDesc(Pageable pageable);
}
