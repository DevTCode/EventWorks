package com.event.GestionEvents.Controller;

import com.event.GestionEvents.Entity.AppUser;
import com.event.GestionEvents.Entity.Employe;
import com.event.GestionEvents.Repository.EmployeeRepository;
import com.event.GestionEvents.Repository.UserRepository;
import com.event.GestionEvents.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EmployeController {
    @Autowired
    private UserRepository repo;
    private EmployeeRepository employeeRepository;
    private EmployeeService employeeService;
    @Autowired
    public EmployeController(EmployeeRepository employeeRepository, UserRepository repo,EmployeeService employeeService) {
        this.employeeRepository = employeeRepository;
        this.repo = repo;
        this.employeeService = employeeService;

    }
    //affichage
    @GetMapping("/users")
    //Le paramètre `page` est correctement annoté avec `@RequestParam` pour spécifier le numéro de la page à afficher.
    //La valeur par défaut de `0` indique que si aucun paramètre de page n'est spécifié dans l'URL, la première page sera affichée.
    //Page<Employe>` pour stocker les résultats paginés de la requête.
    //PageRequest.of(page, 3)` est utilisé pour créer un objet `PageRequest`, spécifiant que vous souhaitez récupérer `3` éléments par page.
    public String viewHomePage(Model model, @RequestParam(defaultValue = "0") int page){
        Page<Employe> employeePage = employeeRepository.findAll(PageRequest.of(page, 3));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());
        model.addAttribute("totalItems", employeePage.getTotalElements());
        model.addAttribute("listEmployees", employeePage.getContent());
        return "admin";
    }
    //affichage forum d'ajout
    @GetMapping("/show")
    public String add(Model model) {
        Employe employee = new Employe();
        model.addAttribute("employee",employee);
        return "new_employee";

    }
    //affichage forum edit
    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable(value = "id") int id, Model model){
        Employe employee = employeeService.getEmployeeById(id);

        model.addAttribute("employee", employee);

        return "update";
    }
//methode d ajout
    //getmapping like doget nous mene vers chemin specifie
    //post mapping c'est dopost qui efectue l action
@PostMapping("/add")
public String addEmployee(@ModelAttribute("employee") Employe employee, @RequestParam("username") String username,
                          @RequestParam("password") String password) {
    var bCryptEncoder = new BCryptPasswordEncoder();

    // Créer un utilisateur correspondant dans la table utilisateur
    AppUser user = new AppUser();
    user.setUsername(username);
    user.setPassword(bCryptEncoder.encode(password));
    user.setRole("CLIENT");
    repo.save(user);  // Sauvegarder l'utilisateur

    // Associer l'utilisateur à l'employé
    employee.setUtilisateur(user);

    // Sauvegarder l'employé dans la table employe
    employeeRepository.save(employee);

    // Redirection vers une page de confirmation ou autre
    return "redirect:/users";
}

    //methode de modif
    @PostMapping("/edit")
    //i have to inistialize on controller to work with
    public String editEmployee(@ModelAttribute("employee") Employe employee) {
        employeeService.saveEmployee(employee);

        return "redirect:/users";
    }
}
