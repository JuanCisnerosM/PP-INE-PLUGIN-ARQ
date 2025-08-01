package com.miempresa.pruebasespecificasdecapa.exposition.dto;

public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;

    // Constructor vacío necesario para serialización
    public UsuarioDTO() {}

    public UsuarioDTO(Long id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
