package com.event.GestionEvents.Service;

import com.event.GestionEvents.Entity.Employe;
import com.event.GestionEvents.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeDetailsService implements EmployeeService{
@Autowired
private EmployeeRepository employeRepository;
    @Override
    public List<Employe> getAll() {
        return employeRepository.findAll();
    }

    @Override
    public void saveEmployee(Employe employee) {
        this.employeRepository.save(employee);
    }

    @Override
    public Employe getEmployeeById(int id) {
        Optional<Employe> optional = employeRepository.findById(id);
        Employe employee = null;
        if(optional.isPresent()){
            employee = optional.get();
        }
        else{
            throw new RuntimeException("Employee not found !");
        }
        return employee;

    }
}
