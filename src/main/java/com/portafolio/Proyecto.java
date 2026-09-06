package com.portafolio;

import jakarta.persistence.*;

@Entity
@Table(name = "proyectos")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String tecnologias;

    @Column(name = "enlace_github")
    private String enlaceGithub;

    private String estado;

    public Proyecto() {}

    public Proyecto(String titulo, String descripcion, String tecnologias, String enlaceGithub, String estado) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tecnologias = tecnologias;
        this.enlaceGithub = enlaceGithub;
        this.estado = estado;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTecnologias() { return tecnologias; }
    public void setTecnologias(String tecnologias) { this.tecnologias = tecnologias; }

    public String getEnlaceGithub() { return enlaceGithub; }
    public void setEnlaceGithub(String enlaceGithub) { this.enlaceGithub = enlaceGithub; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}