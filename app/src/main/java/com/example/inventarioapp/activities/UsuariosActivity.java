package com.example.inventarioapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventarioapp.R;
import com.example.inventarioapp.adapters.UsuarioAdapter;
import com.example.inventarioapp.firebase.AuthManager;
import com.example.inventarioapp.firebase.UsuarioRepository;
import com.example.inventarioapp.models.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class UsuariosActivity extends AppCompatActivity
        implements UsuarioAdapter.OnUsuarioListener {

    private Toolbar toolbarUsuarios;
    private RecyclerView rvUsuarios;
    private ProgressBar pbUsuarios;
    private TextView tvCantidadUsuarios;

    private UsuarioAdapter adapter;
    private ArrayList<Usuario> listaUsuarios;

    private UsuarioRepository usuarioRepository;
    private AuthManager authManager;

    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        inicializarVistas();

        usuarioRepository = new UsuarioRepository();
        authManager = new AuthManager();

        configurarToolbar();

        configurarRecyclerView();

        escucharUsuarios();
    }

    private void inicializarVistas() {

        toolbarUsuarios = findViewById(R.id.toolbarUsuarios);

        rvUsuarios = findViewById(R.id.rvUsuarios);

        pbUsuarios = findViewById(R.id.pbUsuarios);

        tvCantidadUsuarios = findViewById(R.id.tvCantidadUsuarios);

    }

    private void configurarToolbar() {

        setSupportActionBar(toolbarUsuarios);

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        toolbarUsuarios.setNavigationOnClickListener(v -> finish());

    }

    private void configurarRecyclerView() {

        listaUsuarios = new ArrayList<>();

        adapter = new UsuarioAdapter(listaUsuarios, this);

        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));

        rvUsuarios.setAdapter(adapter);

    }

    private void escucharUsuarios() {

        pbUsuarios.setVisibility(View.VISIBLE);

        listenerRegistration = usuarioRepository.escucharUsuarios((value, error) -> {

            pbUsuarios.setVisibility(View.GONE);

            if (error != null) {

                Toast.makeText(
                        this,
                        "Error al cargar usuarios",
                        Toast.LENGTH_SHORT
                ).show();

                return;

            }

            if (value == null) {

                return;

            }

            ArrayList<Usuario> nuevaLista = new ArrayList<>();

            for (DocumentSnapshot document : value.getDocuments()) {

                Usuario usuario = document.toObject(Usuario.class);

                if (usuario != null) {

                    nuevaLista.add(usuario);

                }

            }

            adapter.actualizarLista(nuevaLista);

            tvCantidadUsuarios.setText(
                    "Usuarios registrados: " + nuevaLista.size()
            );

        });

    }

    @Override
    public void editarUsuario(@NonNull Usuario usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Usuario");

        View view = getLayoutInflater().inflate(R.layout.dialog_editar_usuario, null);
        TextInputEditText etNombre = view.findViewById(R.id.etEditarNombre);
        com.google.android.material.button.MaterialButton btnReset = view.findViewById(R.id.btnResetPassword);

        etNombre.setText(usuario.getNombre());

        btnReset.setOnClickListener(v -> {
            authManager.enviarCorreoRestablecimiento(usuario.getCorreo())
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Correo de restablecimiento enviado a " + usuario.getCorreo(), Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setView(view);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            usuarioRepository.actualizarNombre(usuario.getUid(), nuevoNombre)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    public void cambiarRol(@NonNull Usuario usuario) {

        String nuevoRol;

        if (usuario.getRol().equalsIgnoreCase("Administrador")) {

            nuevoRol = "Empleado";

        } else {

            nuevoRol = "Administrador";

        }

        usuarioRepository.actualizarRol(
                usuario.getUid(),
                nuevoRol
        ).addOnSuccessListener(unused ->

                Toast.makeText(
                        this,
                        "Rol actualizado",
                        Toast.LENGTH_SHORT
                ).show()

        ).addOnFailureListener(e ->

                Toast.makeText(
                        this,
                        "No fue posible actualizar el rol",
                        Toast.LENGTH_SHORT
                ).show()

        );

    }

    @Override
    public void eliminarUsuario(@NonNull Usuario usuario) {

        new AlertDialog.Builder(this)

                .setTitle("Eliminar usuario")

                .setMessage(
                        "¿Desea eliminar a " +
                                usuario.getNombre() +
                                "?"
                )

                .setPositiveButton("Eliminar", (dialog, which) -> {

                    usuarioRepository.eliminarUsuario(usuario.getUid())

                            .addOnSuccessListener(unused ->

                                    Toast.makeText(
                                            this,
                                            "Usuario eliminado",
                                            Toast.LENGTH_SHORT
                                    ).show()

                            )

                            .addOnFailureListener(e ->

                                    Toast.makeText(
                                            this,
                                            "No fue posible eliminar el usuario",
                                            Toast.LENGTH_SHORT
                                    ).show()

                            );

                })

                .setNegativeButton("Cancelar", null)

                .show();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (listenerRegistration != null) {

            listenerRegistration.remove();

        }

    }

}