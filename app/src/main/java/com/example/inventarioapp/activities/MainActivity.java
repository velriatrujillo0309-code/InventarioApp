package com.example.inventarioapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventarioapp.R;
import com.example.inventarioapp.firebase.ArticuloRepository;
import com.example.inventarioapp.firebase.AuthManager;
import com.example.inventarioapp.models.Articulo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    // TextInputLayout
    private TextInputLayout tilCodigo;
    private TextInputLayout tilDescripcion;
    private TextInputLayout tilPrecio;

    // EditText
    private TextInputEditText etCodigo;
    private TextInputEditText etDescripcion;
    private TextInputEditText etPrecio;

    // TextView
    private TextView tvBienvenida;
    private TextView tvRol;

    // Switch
    private SwitchMaterial swOferta;

    // ProgressBar
    private ProgressBar pbCarga;

    // Botones
    private MaterialButton btnBuscar;
    private MaterialButton btnRegistrar;
    private MaterialButton btnVerInventario;
    private MaterialButton btnUsuarios;
    private MaterialButton btnCerrarSesion;

    // Firebase
    private AuthManager authManager;
    private ArticuloRepository articuloRepository;

    // Preferencias
    private SharedPreferences preferencias;

    // Estado
    private long fechaCreacionOriginal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager();
        articuloRepository = new ArticuloRepository();

        preferencias = getSharedPreferences("SesionUsuario", MODE_PRIVATE);

        inicializarVistas();
        cargarDatosUsuario();
        configurarEventos();
    }

    private void inicializarVistas() {
        tilCodigo = findViewById(R.id.tilCodigo);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        tilPrecio = findViewById(R.id.tilPrecio);

        etCodigo = findViewById(R.id.etCodigo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);

        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvRol = findViewById(R.id.tvRol);

        swOferta = findViewById(R.id.swOferta);
        pbCarga = findViewById(R.id.pbCarga);

        btnBuscar = findViewById(R.id.btnBuscar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnVerInventario = findViewById(R.id.btnVerInventario);
        btnUsuarios = findViewById(R.id.btnUsuarios);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
    }

    private void cargarDatosUsuario() {
        String nombre = preferencias.getString("nombre", "Usuario");
        String rol = preferencias.getString("rol", "Empleado");

        tvBienvenida.setText("Bienvenido, " + nombre);
        tvRol.setText("Rol: " + rol);

        configurarRoles(rol);
    }

    private void configurarRoles(String rol) {
        if (rol.equalsIgnoreCase("Administrador")) {
            btnUsuarios.setVisibility(View.VISIBLE);
        } else {
            btnUsuarios.setVisibility(View.GONE);
        }
    }

    private void configurarEventos() {
        btnRegistrar.setOnClickListener(v -> guardarArticulo());
        btnVerInventario.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InventarioActivity.class);
            startActivity(intent);
        });
        btnBuscar.setOnClickListener(v -> buscarArticulo());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        btnUsuarios.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UsuariosActivity.class);
            startActivity(intent);
        });
    }

    private void limpiarErrores() {
        tilCodigo.setError(null);
        tilDescripcion.setError(null);
        tilPrecio.setError(null);
    }

    private boolean validarFormulario() {
        limpiarErrores();
        boolean valido = true;

        String codigoStr = etCodigo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (codigoStr.isEmpty()) {
            tilCodigo.setError("Ingrese el código");
            valido = false;
        }

        if (descripcion.isEmpty()) {
            tilDescripcion.setError("Ingrese la descripción");
            valido = false;
        }

        if (precioStr.isEmpty()) {
            tilPrecio.setError("Ingrese el precio");
            valido = false;
        }

        if (!valido) return false;

        try {
            int codigo = Integer.parseInt(codigoStr);
            double precio = Double.parseDouble(precioStr);

            if (codigo <= 0) {
                tilCodigo.setError("El código debe ser mayor que cero");
                valido = false;
            }

            if (precio <= 0) {
                tilPrecio.setError("El precio debe ser mayor que cero");
                valido = false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de código o precio inválido", Toast.LENGTH_SHORT).show();
            valido = false;
        }

        return valido;
    }

    private void setLoadingState(boolean isLoading) {
        pbCarga.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!isLoading);
        btnBuscar.setEnabled(!isLoading);
    }

    private void cerrarSesion() {
        authManager.cerrarSesion();
        preferencias.edit().clear().apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void guardarArticulo() {
        if (!validarFormulario()) return;

        String uid = preferencias.getString("uid", null);
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show();
            cerrarSesion();
            return;
        }

        setLoadingState(true);

        int codigo = Integer.parseInt(etCodigo.getText().toString().trim());
        String descripcion = etDescripcion.getText().toString().trim();
        double precio = Double.parseDouble(etPrecio.getText().toString().trim());
        boolean oferta = swOferta.isChecked();

        Articulo articulo = new Articulo(
                codigo,
                descripcion,
                precio,
                oferta,
                uid,
                System.currentTimeMillis()
        );

        articuloRepository.guardarArticulo(articulo)
                .addOnSuccessListener(unused -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Artículo guardado correctamente.", Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "No fue posible guardar el artículo.", Toast.LENGTH_SHORT).show();
                });
    }

    private void limpiarFormulario() {
        etCodigo.setText("");
        etDescripcion.setText("");
        etPrecio.setText("");
        swOferta.setChecked(false);
        fechaCreacionOriginal = 0;
        limpiarErrores();
    }

    private void buscarArticulo() {
        limpiarErrores();
        String codigoTexto = etCodigo.getText().toString().trim();

        if (codigoTexto.isEmpty()) {
            tilCodigo.setError("Ingrese el código");
            return;
        }

        int codigo;
        try {
            codigo = Integer.parseInt(codigoTexto);
            if (codigo <= 0) {
                tilCodigo.setError("Código inválido");
                return;
            }
        } catch (NumberFormatException e) {
            tilCodigo.setError("Formato numérico inválido");
            return;
        }

        setLoadingState(true);

        articuloRepository.obtenerArticulo(codigo)
                .addOnSuccessListener(document -> {
                    setLoadingState(false);

                    if (!document.exists()) {
                        Toast.makeText(this, "Artículo no encontrado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Articulo articulo = document.toObject(Articulo.class);
                    if (articulo == null) return;

                    etDescripcion.setText(articulo.getDescripcion());
                    etPrecio.setText(String.valueOf(articulo.getPrecio()));
                    swOferta.setChecked(articulo.isOferta());
                    fechaCreacionOriginal = articulo.getFechaCreacion();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Error al buscar el artículo.", Toast.LENGTH_SHORT).show();
                });
    }
}
