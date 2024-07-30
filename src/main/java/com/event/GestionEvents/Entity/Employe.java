package com.event.GestionEvents.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "employe")
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int employe_id;

    private String nom;
    private String prenom;
    private String ville;
    private String tel;


    @OneToOne
    @MapsId
    @JoinColumn(name = "employe_id")
    private AppUser utilisateur;

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    // Getters et Setters

    public int getEmploye_id() {
        return employe_id;
    }

    public void setEmploye_id(int employe_id) {
        this.employe_id = employe_id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public AppUser getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(AppUser utilisateur) {
        this.utilisateur = utilisateur;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
