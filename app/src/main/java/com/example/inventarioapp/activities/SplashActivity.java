package com.example.inventarioapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventarioapp.R;
import com.example.inventarioapp.firebase.AuthManager;
import com.example.inventarioapp.firebase.UsuarioRepository;
import com.example.inventarioapp.models.Usuario;

public class SplashActivity extends AppCompatActivity {

    private AuthManager authManager;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authManager = new AuthManager();
        usuarioRepository = new UsuarioRepository();

        // Esperamos 2 segundos para que se vea el logo y luego decidimos a dónde ir
        new Handler().postDelayed(() -> {
            if (authManager.haySesionActiva()) {
                verificarDatosYEntrar();
            } else {
                irALogin();
            }
        }, 2000);
    }

    private void verificarDatosYEntrar() {
        String uid = authManager.getUidUsuarioActual();
        
        usuarioRepository.obtenerUsuario(uid).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                if (usuario != null) {
                    // Guardamos en SharedPreferences por si acaso se perdieron
                    SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
                    prefs.edit()
                            .putString("uid", usuario.getUid())
                            .putString("nombre", usuario.getNombre())
                            .putString("correo", usuario.getCorreo())
                            .putString("rol", usuario.getRol())
                            .apply();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    irALogin();
                }
            } else {
                // Si el usuario está en Auth pero no en Firestore
                authManager.cerrarSesion();
                irALogin();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
            irALogin();
        });
    }

    private void irALogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}