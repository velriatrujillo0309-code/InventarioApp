package com.example.inventarioapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventarioapp.R;
import com.example.inventarioapp.adapters.ArticuloAdapter;
import com.example.inventarioapp.firebase.ArticuloRepository;
import com.example.inventarioapp.models.Articulo;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class InventarioActivity extends AppCompatActivity {

    private RecyclerView rvInventario;
    private ArticuloAdapter adapter;
    private ArrayList<Articulo> listaArticulos;
    private ArticuloRepository articuloRepository;
    private ListenerRegistration articuloListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        articuloRepository = new ArticuloRepository();
        
        MaterialToolbar toolbar = findViewById(R.id.toolbarInventario);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rvInventario = findViewById(R.id.rvInventario);
        listaArticulos = new ArrayList<>();

        adapter = new ArticuloAdapter(listaArticulos, articulo -> {
            Intent intent = new Intent(InventarioActivity.this, DetalleArticuloActivity.class);
            intent.putExtra("articulo_obj", articulo);
            startActivity(intent);
        });

        rvInventario.setLayoutManager(new LinearLayoutManager(this));
        rvInventario.setAdapter(adapter);

        escucharArticulos();
    }

    private void escucharArticulos() {
        articuloListener = articuloRepository.escucharArticulos((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                ArrayList<Articulo> nuevaLista = new ArrayList<>();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Articulo art = doc.toObject(Articulo.class);
                    if (art != null) nuevaLista.add(art);
                }
                adapter.actualizarLista(nuevaLista);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (articuloListener != null) {
            articuloListener.remove();
        }
    }
}