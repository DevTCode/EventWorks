package com.event.GestionEvents.Service;

import com.event.GestionEvents.Entity.Employe;
import com.event.GestionEvents.Repository.EmployeeRepository;

import java.util.List;

public interface EmployeeService {
    List<Employe> getAll();
    void saveEmployee(Employe employee);
    Employe getEmployeeById(int id);
}
