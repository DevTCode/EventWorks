package com.event.GestionEvents.dto;

import com.event.GestionEvents.Entity.Employe;
import jakarta.validation.constraints.NotEmpty;

public class RegisterDto {

    @NotEmpty
    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
