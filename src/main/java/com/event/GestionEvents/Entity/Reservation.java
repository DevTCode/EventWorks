package com.event.GestionEvents.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "reservation")
public class Reservation {
    //reservation enrelation avec employe et employe en relaation onetoone avec utilisateur
    //car un employe est un utilisateur dont le role est employe
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idReservation;

    @ManyToOne
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event evenement;

    private LocalDate dateReservation;
    private String status;

    // Getters et Setters

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public Event getEvenement() {
        return evenement;
    }

    public void setEvenement(Event evenement) {
        this.evenement = evenement;
    }

    public LocalDate getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDate dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
