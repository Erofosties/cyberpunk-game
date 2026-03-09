package com.cyberpunk.domain.usuario;

import com.cyberpunk.domain.colonia.Colonia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Colonia colonia;

    // Constructor obligatorio JPA
    public Usuario() {}

    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ================= GETTERS =================

    public Long getId() { return id; }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public Colonia getColonia() { return colonia; }

    // ================= SETTERS =================

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ================= RELACIÓN SEGURA =================

    public void setColonia(Colonia colonia) {

        this.colonia = colonia;

        if (colonia != null && colonia.getUsuario() != this) {
            colonia.setUsuario(this);
        }
    }
}