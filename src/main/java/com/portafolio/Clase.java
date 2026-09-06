package com.portafolio;

import jakarta.persistence.*;

@Entity
@Table(name = "clases")
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "semana_id")
    private Integer semanaId;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_registro")
    private String fechaRegistro;

    public Clase() {}

    public Clase(Integer semanaId, String titulo, String descripcion, String fechaRegistro) {
        this.semanaId = semanaId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSemanaId() { return semanaId; }
    public void setSemanaId(Integer semanaId) { this.semanaId = semanaId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}