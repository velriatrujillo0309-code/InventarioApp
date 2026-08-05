package com.example.inventarioapp.firebase;

import com.example.inventarioapp.models.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class UsuarioRepository {

    private static final String COLECCION_USUARIOS = "usuarios";

    private final FirebaseFirestore db;

    public UsuarioRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // ==========================
    // Crear o actualizar usuario
    // ==========================
    public Task<Void> guardarUsuario(Usuario usuario) {

        return db.collection(COLECCION_USUARIOS)
                .document(usuario.getUid())
                .set(usuario);

    }

    // ==========================
    // Obtener un usuario
    // ==========================
    public Task<DocumentSnapshot> obtenerUsuario(String uid) {

        return db.collection(COLECCION_USUARIOS)
                .document(uid)
                .get();

    }

    // ==========================
    // Obtener todos los usuarios
    // ==========================
    public Task<QuerySnapshot> obtenerTodosLosUsuarios() {

        return db.collection(COLECCION_USUARIOS)
                .get();

    }

    // ==========================
    // Escuchar cambios en tiempo real
    // ==========================
    public ListenerRegistration escucharUsuarios(
            EventListener<QuerySnapshot> listener) {

        return db.collection(COLECCION_USUARIOS)
                .addSnapshotListener(listener);

    }

    // ==========================
    // Cambiar rol
    // ==========================
    public Task<Void> actualizarRol(String uid, String nuevoRol) {

        return db.collection(COLECCION_USUARIOS)
                .document(uid)
                .update("rol", nuevoRol);

    }

    // ==========================
    // Actualizar nombre
    // ==========================
    public Task<Void> actualizarNombre(String uid, String nombre) {

        return db.collection(COLECCION_USUARIOS)
                .document(uid)
                .update("nombre", nombre);

    }

    // ==========================
    // Eliminar usuario
    // ==========================
    public Task<Void> eliminarUsuario(String uid) {

        return db.collection(COLECCION_USUARIOS)
                .document(uid)
                .delete();

    }

    // ==========================
    // Verificar si existe
    // ==========================
    public Task<DocumentSnapshot> existeUsuario(String uid) {

        return db.collection(COLECCION_USUARIOS)
                .document(uid)
                .get();

    }

}