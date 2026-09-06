package com.portafolio;

import jakarta.persistence.*;

@Entity
@Table(name = "archivos")
public class Archivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clase_id")
    private Integer claseId;

    @Column(name = "nombre_original")
    private String nombreOriginal;

    @Column(name = "nombre_servidor")
    private String nombreServidor;

    private String tipo;

    @Column(name = "fecha_subida")
    private String fechaSubida;

    public Archivo() {}

    public Archivo(Integer claseId, String nombreOriginal, String nombreServidor, String tipo, String fechaSubida) {
        this.claseId = claseId;
        this.nombreOriginal = nombreOriginal;
        this.nombreServidor = nombreServidor;
        this.tipo = tipo;
        this.fechaSubida = fechaSubida;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getClaseId() { return claseId; }
    public void setClaseId(Integer claseId) { this.claseId = claseId; }

    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }

    public String getNombreServidor() { return nombreServidor; }
    public void setNombreServidor(String nombreServidor) { this.nombreServidor = nombreServidor; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(String fechaSubida) { this.fechaSubida = fechaSubida; }
}
