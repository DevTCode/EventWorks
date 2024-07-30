package com.event.GestionEvents.Repository;

import com.event.GestionEvents.Entity.AppUser;
import com.event.GestionEvents.Entity.Employe;
import com.event.GestionEvents.Entity.Event;
import com.event.GestionEvents.Entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    //récupérer toutes les réservations associées à un employé donné avec l ordre decroissant dans listreservationsemploye
    Page<Reservation> findByEmployeOrderByDateReservationDesc(Employe employe,Pageable pageable);

    //verifier si l employe a deja reserver un evenement pour ne pas reserver autre fois

    boolean existsByEmployeAndEvenement(Employe employe, Event evenement);

    // Si une réservation existe déjà pour cet employé et cet événement
    Reservation findByEmployeAndEvenement(Employe employe, Event evenement);

     //afficher les reservations chez chef RH dans eventsdashboard
    Page<Reservation> findAllByOrderByDateReservationDesc(Pageable pageable);

    //accepter les reservations en attente

    List<Reservation> findByEvenementAndStatus(Event event, String enAttente);

    //afficher nbre de reservzations pour chaque evenement dont la date est sup a la date actuelle
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.evenement.date > CURRENT_DATE")
    long countReservationsForFutureEvents();

}
