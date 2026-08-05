package com.example.inventarioapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventarioapp.R;
import com.example.inventarioapp.firebase.ArticuloRepository;
import com.example.inventarioapp.models.Articulo;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class DetalleArticuloActivity extends AppCompatActivity {

    private TextInputEditText etCodigo, etDescripcion, etPrecio;
    private SwitchMaterial swOferta;
    private ProgressBar pbDetalle;
    private MaterialButton btnActualizar, btnEliminar;
    private ArticuloRepository articuloRepository;
    private Articulo articuloActual;
    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_articulo);

        articuloRepository = new ArticuloRepository();
        articuloActual = (Articulo) getIntent().getSerializableExtra("articulo_obj");
        preferencias = getSharedPreferences("SesionUsuario", MODE_PRIVATE);

        inicializarVistas();
        cargarDatos();
        configurarSegunRol();
    }

    private void configurarSegunRol() {
        String rol = preferencias.getString("rol", "Empleado");
        String currentUid = preferencias.getString("uid", "");

        boolean esAdmin = rol.equalsIgnoreCase("Administrador");
        boolean esDuenio = articuloActual != null && currentUid.equals(articuloActual.getCreadoPor());

        if (btnEliminar != null) {
            if (esAdmin || esDuenio) {
                btnEliminar.setVisibility(View.VISIBLE);
            } else {
                btnEliminar.setVisibility(View.GONE);
            }
        }
    }

    private void inicializarVistas() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarDetalle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etCodigo = findViewById(R.id.etDetalleCodigo);
        etDescripcion = findViewById(R.id.etDetalleDescripcion);
        etPrecio = findViewById(R.id.etDetallePrecio);
        swOferta = findViewById(R.id.swDetalleOferta);
        pbDetalle = findViewById(R.id.pbDetalle);

        btnActualizar = findViewById(R.id.btnActualizarDetalle);
        btnEliminar = findViewById(R.id.btnEliminarDetalle);

        btnActualizar.setOnClickListener(v -> actualizar());
        btnEliminar.setOnClickListener(v -> eliminar());
    }

    private void cargarDatos() {
        if (articuloActual != null) {
            etCodigo.setText(String.valueOf(articuloActual.getCodigo()));
            etDescripcion.setText(articuloActual.getDescripcion());
            etPrecio.setText(String.valueOf(articuloActual.getPrecio()));
            swOferta.setChecked(articuloActual.isOferta());
        }
    }

    private void actualizar() {
        if (articuloActual == null) return;
        
        String desc = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (desc.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        pbDetalle.setVisibility(View.VISIBLE);
        articuloActual.setDescripcion(desc);
        articuloActual.setPrecio(Double.parseDouble(precioStr));
        articuloActual.setOferta(swOferta.isChecked());

        articuloRepository.actualizarArticulo(articuloActual)
                .addOnSuccessListener(unused -> {
                    pbDetalle.setVisibility(View.GONE);
                    Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pbDetalle.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                });
    }

    private void eliminar() {
        if (articuloActual == null) return;

        // Verificar permisos: Admin o Dueño del artículo
        String rol = preferencias.getString("rol", "Empleado");
        String currentUid = preferencias.getString("uid", "");

        boolean esAdmin = rol.equalsIgnoreCase("Administrador");
        boolean esDuenio = currentUid.equals(articuloActual.getCreadoPor());

        if (!esAdmin && !esDuenio) {
            Toast.makeText(this, "No tiene permisos para eliminar este artículo", Toast.LENGTH_SHORT).show();
            return;
        }

        pbDetalle.setVisibility(View.VISIBLE);
        articuloRepository.eliminarArticulo(articuloActual.getCodigo())
                .addOnSuccessListener(unused -> {
                    pbDetalle.setVisibility(View.GONE);
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pbDetalle.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                });
    }
}