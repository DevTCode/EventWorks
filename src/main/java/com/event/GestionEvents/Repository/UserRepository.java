package com.event.GestionEvents.Repository;

import com.event.GestionEvents.Entity.AppUser;
import com.event.GestionEvents.Entity.AppUser;
import com.event.GestionEvents.Entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
//we define AppUser and long to refer to the model and the key we write integer or long
//depending on the type of id on the model
public interface UserRepository extends JpaRepository<AppUser, Integer> {
    public AppUser findByUsername(String username);


}
