package com.event.GestionEvents.Controller;

import com.event.GestionEvents.Entity.AppUser;
import com.event.GestionEvents.Entity.Employe;
import com.event.GestionEvents.Entity.Event;
import com.event.GestionEvents.Entity.Reservation;
import com.event.GestionEvents.Repository.EmployeeRepository;
import com.event.GestionEvents.Repository.EventRepository;
import com.event.GestionEvents.Repository.ReservationRepository;
import com.event.GestionEvents.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Controller
public class ReservationController {
    private List<String> notifications = new ArrayList<>();
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReservationController(ReservationRepository reservationRepository,EmployeeRepository employeeRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.employeeRepository=employeeRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/editProfile/{id}")
    public String editEmployee(@PathVariable(value = "id") int id, Model model){
        AppUser user = userRepository.getById(id);

        model.addAttribute("user", user);

        return "updateProfile";
    }
    @PostMapping("/editProfile")
    public String editEmployee(@ModelAttribute("user") AppUser user,
                               @RequestParam("currentPassword") String currentPassword,
                               @RequestParam("newPassword") String newPassword,
                               RedirectAttributes redirectAttributes) {
        // Récupérer l'utilisateur existant à partir de la base de données
        AppUser existingUser = userRepository.getById(user.getId());
        var bCryptEncoder = new BCryptPasswordEncoder();

        // Vérifier si le mot de passe actuel est correct
        if (bCryptEncoder.matches(currentPassword, existingUser.getPassword())) {
            // Si le mot de passe actuel est correct, mettre à jour avec le nouveau mot de passe
            existingUser.setPassword(bCryptEncoder.encode(newPassword));
            userRepository.save(existingUser);

            // Ajouter le message de confirmation
            redirectAttributes.addFlashAttribute("message", "Le mot de passe a été mis à jour avec succès.");
        } else {
            // Si le mot de passe actuel est incorrect, ajouter un message d'erreur
            redirectAttributes.addFlashAttribute("error", "Le mot de passe actuel est incorrect.");
        }

        // Rediriger vers la méthode GET pour recharger la page avec l'objet utilisateur
        return "redirect:/editProfile/" + user.getId();
    }




    //Dans Spring Security, UserDetails est une interface centrale qui représente
    // l'utilisateur connecté et ses détails lors d'une session. Son rôle principal e
    // st de fournir à Spring Security les informations nécessaires sur l'utilisateur pour
    // l'authentification et l'autorisation.
    //ds securityconfig on specifie pour reserve le role car userdetails nous ramene les informations de
    //connexion de l employe connecte en se basant sur le role entré
    @PostMapping("/reserve")
    @Transactional
    public String reserveEvent(@RequestParam("eventId") int eventId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        AppUser user = userRepository.findByUsername(userDetails.getUsername());
        Employe employe = employeeRepository.findByUtilisateur(user);
        Event event = eventRepository.findById(eventId).orElse(null);

        if (employe != null && event != null) {
            LocalDate eventDate = event.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate currentDate = LocalDate.now();

            // Vérifier si la date actuelle est dans les 5 jours précédant l'événement
            if (currentDate.isAfter(eventDate.minusDays(6))) {
                redirectAttributes.addFlashAttribute("err", "L'événement est inaccessible");
                redirectAttributes.addFlashAttribute("eventId", eventId);
            } else {
                boolean alreadyReserved = reservationRepository.existsByEmployeAndEvenement(employe, event);

                // Si une réservation existe déjà pour cet employé et cet événement
                if (alreadyReserved) {
                    Reservation existingReservation = reservationRepository.findByEmployeAndEvenement(employe, event);
                    if (existingReservation != null) {
                        int existingReservationId = existingReservation.getIdReservation();
                        reservationRepository.deleteById(existingReservationId);

                        // Mettre à jour le nombre de places disponibles et le statut si nécessaire
                        if (!"en attente".equals(existingReservation.getStatus())) {
                            int placesDisponibles = event.getPlacesDisponibles() + 1;
                            event.setPlacesDisponibles(placesDisponibles);
                            eventRepository.save(event);
                        }

                        redirectAttributes.addFlashAttribute("attente", "Votre réservation a été annulée avec succès.");
                        confirmWaitingReservations(event); // Confirmer les réservations en attente
                    } else {
                        redirectAttributes.addFlashAttribute("error", "Erreur lors de l'annulation de la réservation.");
                    }
                } else {
                    // Si aucune réservation existante, procéder à la création de la réservation
                    if (event.getPlacesDisponibles() > 0) {
                        Reservation reservation = new Reservation();
                        reservation.setEmploye(employe);
                        reservation.setEvenement(event);
                        reservation.setDateReservation(LocalDate.now());

                        // Déterminer le statut de la réservation en fonction des places disponibles
                        if (event.getPlacesDisponibles() > 0) {
                            reservation.setStatus("confirmé");
                            int placesDisponibles = event.getPlacesDisponibles();
                            placesDisponibles--; // Décrementer le nombre de places disponibles
                            event.setPlacesDisponibles(placesDisponibles);
                            eventRepository.save(event);
                            redirectAttributes.addFlashAttribute("success", "Votre réservation a été effectuée avec succès.");
                        } else {
                            reservation.setStatus("en attente");
                            redirectAttributes.addFlashAttribute("attente", "Votre réservation est en attente.");
                        }

                        reservationRepository.save(reservation);


                    } else {
                        // Si aucune place disponible, créer une réservation en attente
                        Reservation reservation = new Reservation();
                        reservation.setEmploye(employe);
                        reservation.setEvenement(event);
                        reservation.setDateReservation(LocalDate.now());
                        reservation.setStatus("en attente");
                        reservationRepository.save(reservation);

                        redirectAttributes.addFlashAttribute("attente", "Votre réservation est en attente car il n'y a pas de place disponible.");
                    }
                }
            }
        }

        return "redirect:/showEvents";
    }



    private void confirmWaitingReservations(Event event) {
        List<Reservation> waitingReservations = reservationRepository.findByEvenementAndStatus(event, "en attente");

        if (waitingReservations != null && !waitingReservations.isEmpty()) {
            int placesDisponibles = event.getPlacesDisponibles();

            for (Reservation reservation : waitingReservations) {
                if (placesDisponibles > 0) {
                    reservation.setStatus("confirmé");
                    reservationRepository.save(reservation);
                    placesDisponibles--; // Décrémenter le nombre de places disponibles
                } else {
                    break; // Sortir de la boucle si plus de places disponibles
                }
            }

            // Mettre à jour le nombre de places disponibles pour l'événement
            event.setPlacesDisponibles(placesDisponibles);
            eventRepository.save(event);
        }
    }



    @GetMapping("/showAdminReservations")
    public String reservation(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Reservation> reservationPage = reservationRepository.findAllByOrderByDateReservationDesc(PageRequest.of(page, 3));

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reservationPage.getTotalPages());
        model.addAttribute("totalItems", reservationPage.getTotalElements());
        model.addAttribute("reservations", reservationPage.getContent());
        return "BookingDashboard";
    }


    //add on application.properties spring.mvc.hiddenmethod.filter.enabled=true This filter
    // allows the use of hidden _method parameters to simulate HTTP methods such as PUT and DELETE.




}
