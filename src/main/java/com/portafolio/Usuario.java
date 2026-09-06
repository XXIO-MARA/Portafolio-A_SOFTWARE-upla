package com.portafolio;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String codigo;

    private String nombre;
    private String correo;

    @Column(name = "rol_id")
    private Integer rolId; // 1 = ADMIN, 2 = ESTUDIANTE

    public Usuario() {}

    public Usuario(String codigo, String nombre, String correo, Integer rolId) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.correo = correo;
        this.rolId = rolId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Integer getRolId() { return rolId; }
    public void setRolId(Integer rolId) { this.rolId = rolId; }

    public boolean esAdmin() {
        return Integer.valueOf(1).equals(this.rolId);
    }
}