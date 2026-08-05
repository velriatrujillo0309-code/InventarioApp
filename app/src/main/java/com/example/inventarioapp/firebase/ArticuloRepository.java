package com.example.inventarioapp.firebase;

import com.example.inventarioapp.models.Articulo;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class ArticuloRepository {

    private final FirebaseFirestore db;

    public ArticuloRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // Guardar
    public Task<Void> guardarArticulo(Articulo articulo) {

        return db.collection("articulos")
                .document(String.valueOf(articulo.getCodigo()))
                .set(articulo);

    }

    // Buscar
    public Task<DocumentSnapshot> obtenerArticulo(int codigo) {

        return db.collection("articulos")
                .document(String.valueOf(codigo))
                .get();

    }

    // Actualizar
    public Task<Void> actualizarArticulo(Articulo articulo) {

        return db.collection("articulos")
                .document(String.valueOf(articulo.getCodigo()))
                .set(articulo);

    }

    // Eliminar
    public Task<Void> eliminarArticulo(int codigo) {

        return db.collection("articulos")
                .document(String.valueOf(codigo))
                .delete();

    }

    // Obtener todos
    public Task<QuerySnapshot> obtenerTodosLosArticulos() {

        return db.collection("articulos")
                .get();

    }

    // Tiempo real
    public ListenerRegistration escucharArticulos(
            com.google.firebase.firestore.EventListener<QuerySnapshot> listener) {

        return db.collection("articulos")
                .addSnapshotListener(listener);

    }

}