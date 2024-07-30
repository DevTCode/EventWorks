package com.event.GestionEvents.Repository;

import com.event.GestionEvents.Entity.AppUser;
import com.event.GestionEvents.Entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employe, Integer>  {
    // Trouver l'employé associé à cet utilisateur
    Employe findByUtilisateur(AppUser utilisateur);
//here all the methods are defined (findAll etc ) due to jpa which allow us to interact
    //directly with database
    //sinon on definie les methodes comme j'ai fait dans employeService pour travailler avec
    //et dans controller soit on met employeservice.getAll ou bien employerepository.findAll()
    //qui est fct predefinie pour l affichage
    //Employe pour chercher afficher et pour getbyid pour editer on utilise id d ou integer

}
