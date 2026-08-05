package com.example.inventarioapp.models;

import java.io.Serializable;

public class Articulo implements Serializable {

    private int codigo;
    private String descripcion;
    private double precio;
    private boolean oferta;

    // Opcionales, pero muy útiles para el proyecto
    private String creadoPor;
    private long fechaCreacion;

    public Articulo() {
        // Constructor vacío requerido por Firestore
    }

    public Articulo(int codigo,
                    String descripcion,
                    double precio,
                    boolean oferta,
                    String creadoPor,
                    long fechaCreacion) {

        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.oferta = oferta;
        this.creadoPor = creadoPor;
        this.fechaCreacion = fechaCreacion;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isOferta() {
        return oferta;
    }

    public void setOferta(boolean oferta) {
        this.oferta = oferta;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}