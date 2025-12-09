package com.coopcredit.credit.domain.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad de dominio: Usuario
 * Representa un usuario del sistema con autenticación y autorización.
 */
public class Usuario {

    private Long id;
    private String username;
    private String password;
    private String email;
    private Set<Rol> roles = new HashSet<>();
    private Afiliado afiliado; // Puede ser null para analistas y admin

    public Usuario() {
    }

    public Usuario(Long id, String username, String password, String email, Set<Rol> roles, Afiliado afiliado) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles != null ? roles : new HashSet<>();
        this.afiliado = afiliado;
    }

    /**
     * Agrega un rol al usuario.
     */
    public void agregarRol(Rol rol) {
        this.roles.add(rol);
    }

    /**
     * Verifica si el usuario tiene un rol específico.
     */
    public boolean tieneRol(Rol rol) {
        return this.roles.contains(rol);
    }

    /**
     * Verifica si el usuario es afiliado.
     */
    public boolean esAfiliado() {
        return this.roles.contains(Rol.ROLE_AFILIADO);
    }

    /**
     * Verifica si el usuario es analista.
     */
    public boolean esAnalista() {
        return this.roles.contains(Rol.ROLE_ANALISTA);
    }

    /**
     * Verifica si el usuario es administrador.
     */
    public boolean esAdmin() {
        return this.roles.contains(Rol.ROLE_ADMIN);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }

    public Afiliado getAfiliado() {
        return afiliado;
    }

    public void setAfiliado(Afiliado afiliado) {
        this.afiliado = afiliado;
    }
}
