package com.event.GestionEvents.Controller;
import com.event.GestionEvents.Entity.AppUser;
import com.event.GestionEvents.Entity.Employe;
import com.event.GestionEvents.Entity.Event;
import com.event.GestionEvents.Entity.Reservation;
import com.event.GestionEvents.Repository.EmployeeRepository;
import com.event.GestionEvents.Repository.EventRepository;
import com.event.GestionEvents.Repository.ReservationRepository;
import com.event.GestionEvents.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class EventController {
    private List<String> notifications = new ArrayList<>();

    @Autowired

    private UserRepository repo;
    private EventRepository eventRepository;
    private EmployeeRepository employeeRepository;
    private ReservationRepository reservationRepository;
    private UserRepository userRepository;

    @Autowired
    public EventController(EventRepository eventRepository, UserRepository repo,EmployeeRepository employeeRepository,ReservationRepository reservationRepository,UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.repo = repo;
        this.employeeRepository=employeeRepository;
        this.userRepository=userRepository;
        this.reservationRepository=reservationRepository;
    }

    //affichage cote client

    @GetMapping("/showEvents")
    public String showEvents(Model model, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        Date currentDate = new Date();
        List<Event> futureEvents = eventRepository.findByDateAfterOrderByDateDesc(currentDate);

        // Récupérer l'utilisateur et l'employé connecté
        AppUser user = userRepository.findByUsername(userDetails.getUsername());
        Employe employe = employeeRepository.findByUtilisateur(user);

        // Créer une map pour stocker les réservations existantes par événement
        Map<Integer, Boolean> reservationsMap = new HashMap<>();
        for (Event event : futureEvents) {
            boolean alreadyReserved = reservationRepository.existsByEmployeAndEvenement(employe, event);
            reservationsMap.put(event.getEvent_id(), alreadyReserved);

        }
        Map<Integer, String> sixDaysBeforeEvents = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Event event : futureEvents) {
            LocalDate eventDate = event.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate sixDaysBefore = eventDate.minusDays(5);
            sixDaysBeforeEvents.put(event.getEvent_id(), sixDaysBefore.format(formatter));
        }

        model.addAttribute("sixDaysBeforeEvents", sixDaysBeforeEvents);

        model.addAttribute("list", employeeRepository.findAll());
        model.addAttribute("listcnx", userRepository.findByUsername(userDetails.getUsername()));
        model.addAttribute("eventList", futureEvents);
        model.addAttribute("reservationsMap", reservationsMap);
        model.addAttribute("notifications", notifications);

        return "events";
    }

    @GetMapping("/showReservations")
    public String showReservations(Model model, @AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "0") int page) {
        AppUser user = userRepository.findByUsername(userDetails.getUsername());
        Employe employe = employeeRepository.findByUtilisateur(user);

        if (employe != null) {
            Page<Reservation> reservationPage = reservationRepository.findByEmployeOrderByDateReservationDesc(employe, PageRequest.of(page, 3));
            model.addAttribute("reservations", reservationPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", reservationPage.getTotalPages());
            model.addAttribute("totalItems", reservationPage.getTotalElements());
        } else {
            model.addAttribute("reservations", null);
        }
        model.addAttribute("list", employeeRepository.findAll());
        model.addAttribute("listcnx", userRepository.findByUsername(userDetails.getUsername()));
        model.addAttribute("notifications", notifications);
        return "ListReservationsEmploye";
    }


    //affichage cote admin
    @GetMapping("/list")
    public String listEvents(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Event> eventPage = eventRepository.findAllByOrderByDateDesc(PageRequest.of(page, 3));
        model.addAttribute("eventList", eventPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventPage.getTotalPages());
        model.addAttribute("totalItems", eventPage.getTotalElements());
        return "eventsDashboard";
    }


    @GetMapping("/addForm")
    public String addForm(Model model) {
        Event event = new Event();
        model.addAttribute("event", event);
        return "new_event";
    }

    @PostMapping("/addEvent")
    //i have to inistialize on controller to work with
    public String addEmployee(@ModelAttribute("event") Event event) {
        // Sauvegarder l'employé dans la table employee
        eventRepository.save(event);
        return "redirect:/list";
    }

    @GetMapping("/editForm/{id}")
    public String editEvent(@PathVariable(value = "id") int id, Model model) {
        Event event = eventRepository.getById(id);

        model.addAttribute("event", event);

        return "update_event";
    }

    @PostMapping("/editForm")
    public String editEventPost(@ModelAttribute("event") Event event,
                                @RequestParam("image") MultipartFile file,
                                @RequestParam("currentImage") String currentImage,
                                HttpServletRequest req, HttpServletResponse resp) {

        Event existingEvent = eventRepository.getById(event.getEvent_id());
        String imagePath;

        // Vérifier si un nouveau fichier image est téléchargé
        if (!file.isEmpty()) {
            // Si un nouveau fichier est téléchargé, enregistrer le fichier
            String uploadPath = req.getServletContext().getRealPath("") + File.separator + "images/";
            String fileName = file.getOriginalFilename();
            imagePath = fileName;

            try (InputStream inputStream = file.getInputStream();
                 OutputStream outputStream = new FileOutputStream(new File(uploadPath + fileName))) {
                // Copier le fichier vers le répertoire d'upload
                int read;
                final byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Gérer l'erreur de sauvegarde du fichier
            }
        } else {
            // Si aucun nouveau fichier n'est téléchargé, conserver l'image existante
            imagePath = currentImage;
        }

        if (!existingEvent.getEtat().equals(event.getEtat())) {
            String notificationMessage = null;
            if (event.getEtat().equals("annulé")) {
                notificationMessage = "L'événement " + existingEvent.getNom() + " a été annulé.";
            } else if (event.getEtat().equals("reporté")) {
                notificationMessage = "L'événement " + existingEvent.getNom() + " a été reporté.";
            }

            if (notificationMessage != null) {
                notifications.add(notificationMessage);
            }
        }

        // Mettre à jour les champs de l'événement existant
        existingEvent.setNom(event.getNom());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setDate(event.getDate());
        existingEvent.setHeure(event.getHeure());
        existingEvent.setType(event.getType());
        existingEvent.setLieu(event.getLieu());
        existingEvent.setDuree(event.getDuree());
        existingEvent.setPlacesDisponibles(event.getPlacesDisponibles());
        existingEvent.setEtat(event.getEtat());
        existingEvent.setChemin_image(imagePath);

        // Enregistrer l'événement mis à jour dans la base de données
        eventRepository.save(existingEvent);

        // Rediriger vers la page de liste des événements après la mise à jour
        return "redirect:/list";
    }


    @GetMapping("/client")
    public String client(Model model,@AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("list", employeeRepository.findAll());
        model.addAttribute("listcnx", userRepository.findByUsername(userDetails.getUsername()));
        model.addAttribute("eventList", eventRepository.findAll());
        model.addAttribute("notifications", notifications);
        return "index";
    }

    @GetMapping("/dash")
    public String dashboard(Model model) {

        long numberOfEmployees = employeeRepository.count();
        model.addAttribute("numberOfEmployees", numberOfEmployees);

        long numberOfEvents = eventRepository.count();
        model.addAttribute("numberOfEvents", numberOfEvents);


        long numberOfCanceledEvents = eventRepository.countByEtatIgnoreCase("Annulé");
        model.addAttribute("numberOfCanceledEvents", numberOfCanceledEvents);


        long numberOfPostponedEvents = eventRepository.countByEtatIgnoreCase("Reporté");
        model.addAttribute("numberOfPostponedEvents", numberOfPostponedEvents);

        long numberOfReservationsForFutureEvents = reservationRepository.countReservationsForFutureEvents();
        model.addAttribute("numberOfReservationsForFutureEvents", numberOfReservationsForFutureEvents);


        return "dashboard";
    }
    @GetMapping("/notifications")
    public String showNotifications(Model model) {
        model.addAttribute("notifications", notifications);
        return "notifications";
    }


    }
