package com.example.inventarioapp.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {

    private final FirebaseAuth auth;

    public AuthManager() {
        auth = FirebaseAuth.getInstance();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseUser getUsuarioActual() {
        return auth.getCurrentUser();
    }

    public boolean haySesionActiva() {
        return auth.getCurrentUser() != null;
    }

    public String getUidUsuarioActual() {

        if (auth.getCurrentUser() == null) {
            return null;
        }

        return auth.getCurrentUser().getUid();
    }

    public void cerrarSesion() {
        auth.signOut();
    }

    public void registrarUsuario(String email, String password, com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public com.google.android.gms.tasks.Task<Void> enviarCorreoRestablecimiento(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    public void eliminarUsuarioActual() {
        if (auth.getCurrentUser() != null) {
            auth.getCurrentUser().delete();
        }
    }

}