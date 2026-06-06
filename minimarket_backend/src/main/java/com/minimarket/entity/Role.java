package com.minimarket.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private ERole nombre;
    
    // Constructores
    public Role() {}
    
    public Role(ERole nombre) {
        this.nombre = nombre;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ERole getNombre() {
        return nombre;
    }
    
    public void setNombre(ERole nombre) {
        this.nombre = nombre;
    }
}