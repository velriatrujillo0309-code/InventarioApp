package com.example.inventarioapp.models;

public class Usuario {

    private String uid;
    private String nombre;
    private String correo;
    private String rol;

    public Usuario() {
        // Constructor vacío requerido por Firestore
    }

    public Usuario(String uid, String nombre, String correo, String rol) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}