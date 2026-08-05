package com.example.inventarioapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventarioapp.R;
import com.example.inventarioapp.firebase.AuthManager;
import com.example.inventarioapp.firebase.UsuarioRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import android.widget.Toast;

import com.example.inventarioapp.models.Usuario;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    // TextInputLayouts
    private TextInputLayout tilCorreo;
    private TextInputLayout tilPassword;

    // EditText
    private TextInputEditText etLoginCorreo;
    private TextInputEditText etLoginPassword;

    // Botones
    private Button btnLogin;
    private Button btnGoogle;
    private TextView tvIrRegistro;
    private TextView tvOlvidePassword;

    // Google Sign In
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    // Firebase
    private AuthManager authManager;
    private UsuarioRepository usuarioRepository;

    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = new AuthManager();
        usuarioRepository = new UsuarioRepository();

        preferencias = getSharedPreferences(
                "SesionUsuario",
                MODE_PRIVATE
        );

        // TextInputLayouts
        tilCorreo = findViewById(R.id.tilCorreo);
        tilPassword = findViewById(R.id.tilPassword);

        // EditText
        etLoginCorreo = findViewById(R.id.etLoginCorreo);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        // Botones
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);
        tvOlvidePassword = findViewById(R.id.tvOlvidePassword);

        // Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Auto Login
        if (authManager.haySesionActiva()) {

            cargarUsuarioActual();

            return;
        }

        btnLogin.setOnClickListener(v -> validarFormulario());

        tvIrRegistro.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegistroActivity.class
            );

            startActivity(intent);

        });

        tvOlvidePassword.setOnClickListener(v -> mostrarDialogoRecuperarPassword());

        btnGoogle.setOnClickListener(v -> iniciarSesionGoogle());

    }

    private void iniciarSesionGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Error de Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        btnGoogle.setEnabled(false);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        authManager.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        verificarUsuarioFirestore();
                    } else {
                        btnGoogle.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Error al autenticar con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verificarUsuarioFirestore() {
        String uid = authManager.getUidUsuarioActual();
        usuarioRepository.obtenerUsuario(uid).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                procesarUsuario(documentSnapshot);
            } else {
                // Crear el usuario en Firestore si no existe (primer inicio con Google)
                com.google.firebase.auth.FirebaseUser user = authManager.getUsuarioActual();
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setUid(uid);
                nuevoUsuario.setNombre(user.getDisplayName() != null ? user.getDisplayName() : "Usuario Google");
                nuevoUsuario.setCorreo(user.getEmail());
                nuevoUsuario.setRol("Empleado"); // Rol por defecto

                usuarioRepository.guardarUsuario(nuevoUsuario)
                        .addOnSuccessListener(aVoid -> cargarUsuarioActual())
                        .addOnFailureListener(e -> {
                            authManager.cerrarSesion();
                            Toast.makeText(this, "Error al crear perfil de usuario", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void mostrarDialogoRecuperarPassword() {

        String correo = etLoginCorreo.getText().toString().trim();

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Recuperar contraseña");
        builder.setMessage("Se enviará un correo electrónico para restablecer tu contraseña.");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Correo electrónico");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setText(correo);
        
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        container.addView(input);
        
        builder.setView(container);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Ingrese un correo", Toast.LENGTH_SHORT).show();
            } else {
                enviarCorreoRecuperacion(email);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();

    }

    private void enviarCorreoRecuperacion(String email) {
        authManager.enviarCorreoRestablecimiento(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoginActivity.this, "Correo de recuperación enviado", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error al enviar el correo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void limpiarErrores() {

        tilCorreo.setError(null);
        tilPassword.setError(null);

    }

    private void validarFormulario() {

        limpiarErrores();

        String correo = etLoginCorreo.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        boolean formularioValido = true;

        // Validar correo
        if (correo.isEmpty()) {

            tilCorreo.setError("Ingrese el correo electrónico");
            formularioValido = false;

        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

            tilCorreo.setError("Correo electrónico no válido");
            formularioValido = false;

        }

        // Validar contraseña
        if (password.isEmpty()) {

            tilPassword.setError("Ingrese la contraseña");
            formularioValido = false;

        } else if (password.length() < 6) {

            tilPassword.setError("La contraseña debe tener mínimo 6 caracteres");
            formularioValido = false;

        }

        if (!formularioValido) {
            return;
        }

        iniciarSesionFirebase(correo, password);

    }

    private void iniciarSesionFirebase(String correo, String password) {

        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");

        authManager.getAuth()
                .signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        cargarUsuarioActual();

                    } else {

                        btnLogin.setEnabled(true);
                        btnLogin.setText("INICIAR SESIÓN");

                        tilCorreo.setError("Correo o contraseña incorrectos");

                    }

                });

    }

    private void cargarUsuarioActual() {

        String uid = authManager.getUidUsuarioActual();

        if (uid == null) {

            authManager.cerrarSesion();

            return;

        }

        usuarioRepository.obtenerUsuario(uid)

                .addOnSuccessListener(this::procesarUsuario)

                .addOnFailureListener(e -> {

                    authManager.cerrarSesion();

                    Toast.makeText(
                            this,
                            "No fue posible obtener la información del usuario.",
                            Toast.LENGTH_SHORT
                    ).show();

                });

    }

    private void procesarUsuario(DocumentSnapshot documentSnapshot) {

        btnLogin.setEnabled(true);
        btnLogin.setText("INICIAR SESIÓN");

        if (!documentSnapshot.exists()) {

            authManager.cerrarSesion();

            Toast.makeText(
                    this,
                    "El usuario no existe en Firestore.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Usuario usuario = documentSnapshot.toObject(Usuario.class);

        if (usuario == null) {

            authManager.cerrarSesion();

            Toast.makeText(
                    this,
                    "No fue posible cargar el usuario.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        SharedPreferences.Editor editor = preferencias.edit();

        editor.putString("uid", usuario.getUid());
        editor.putString("nombre", usuario.getNombre());
        editor.putString("correo", usuario.getCorreo());
        editor.putString("rol", usuario.getRol());

        editor.apply();

        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();

    }
}