package com.example.inventarioapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventarioapp.R;
import com.example.inventarioapp.firebase.AuthManager;
import com.example.inventarioapp.firebase.UsuarioRepository;
import com.example.inventarioapp.models.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegistroActivity extends AppCompatActivity {

    private TextInputLayout tilNombre, tilCorreo, tilPassword, tilConfirmarPassword;
    private TextInputEditText etRegistrarNombre, etRegistrarCorreo, etRegistrarPassword, etRegistrarPasswordConf;
    private Button btnRegistrarUsuario;
    private TextView tvVolverLogin;

    private AuthManager authManager;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        authManager = new AuthManager();
        usuarioRepository = new UsuarioRepository();

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilNombre = findViewById(R.id.tilNombre);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmarPassword = findViewById(R.id.tilConfirmarPassword);

        etRegistrarNombre = findViewById(R.id.etRegistrarNombre);
        etRegistrarCorreo = findViewById(R.id.etRegistrarCorreo);
        etRegistrarPassword = findViewById(R.id.etRegistrarPassword);
        etRegistrarPasswordConf = findViewById(R.id.etRegistrarPasswordConf);

        btnRegistrarUsuario = findViewById(R.id.btnRegistrarUsuario);
        tvVolverLogin = findViewById(R.id.tvVolverLogin);
    }

    private void setupListeners() {
        tvVolverLogin.setOnClickListener(v -> finish());
        btnRegistrarUsuario.setOnClickListener(v -> validarFormulario());
    }

    private void validarFormulario() {
        limpiarErrores();

        String nombre = getText(etRegistrarNombre);
        String correo = getText(etRegistrarCorreo);
        String password = getText(etRegistrarPassword);
        String confirmar = getText(etRegistrarPasswordConf);

        boolean esValido = true;

        if (nombre.isEmpty()) {
            tilNombre.setError("Ingrese su nombre");
            esValido = false;
        } else if (nombre.length() < 3) {
            tilNombre.setError("El nombre debe tener al menos 3 caracteres");
            esValido = false;
        }

        if (correo.isEmpty()) {
            tilCorreo.setError("Ingrese un correo electrónico");
            esValido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Correo electrónico no válido");
            esValido = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Ingrese una contraseña");
            esValido = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Debe tener mínimo 6 caracteres");
            esValido = false;
        }

        if (confirmar.isEmpty()) {
            tilConfirmarPassword.setError("Confirme la contraseña");
            esValido = false;
        } else if (!password.equals(confirmar)) {
            tilConfirmarPassword.setError("Las contraseñas no coinciden");
            esValido = false;
        }

        if (esValido) {
            ejecutarRegistro(nombre, correo, password);
        }
    }

    private void ejecutarRegistro(String nombre, String correo, String password) {
        setLoadingState(true);

        authManager.registrarUsuario(correo, password, task -> {
            if (task.isSuccessful() && authManager.getUsuarioActual() != null) {
                String uid = authManager.getUidUsuarioActual();
                Usuario usuario = new Usuario(uid, nombre, correo, "Empleado");

                guardarEnBaseDeDatos(usuario);
            } else {
                setLoadingState(false);
                String msj = task.getException() != null ? task.getException().getMessage() : "Error al crear la cuenta.";
                tilCorreo.setError(msj);
            }
        });
    }

    private void guardarEnBaseDeDatos(Usuario usuario) {
        usuarioRepository.guardarUsuario(usuario)
                .addOnSuccessListener(unused -> {
                    setLoadingState(false);
                    mostrarMensaje("Cuenta creada correctamente");

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    authManager.eliminarUsuarioActual();
                    // Ahora mostramos el error real de Firebase
                    mostrarMensaje("Error en base de datos: " + e.getMessage());
                    android.util.Log.e("REGISTRO_ERROR", "Error al guardar usuario", e);
                });
    }

    private void setLoadingState(boolean isLoading) {
        btnRegistrarUsuario.setEnabled(!isLoading);
        btnRegistrarUsuario.setText(isLoading ? "Creando cuenta..." : "REGISTRARME");
    }

    private void limpiarErrores() {
        tilNombre.setError(null);
        tilCorreo.setError(null);
        tilPassword.setError(null);
        tilConfirmarPassword.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}