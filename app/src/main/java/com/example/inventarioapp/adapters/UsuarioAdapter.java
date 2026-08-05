package com.example.inventarioapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventarioapp.R;
import com.example.inventarioapp.models.Usuario;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private final List<Usuario> listaUsuarios;
    private final OnUsuarioListener listener;

    public interface OnUsuarioListener {

        void editarUsuario(Usuario usuario);

        void cambiarRol(Usuario usuario);

        void eliminarUsuario(Usuario usuario);

    }

    public UsuarioAdapter(List<Usuario> listaUsuarios,
                          OnUsuarioListener listener) {

        this.listaUsuarios = listaUsuarios;
        this.listener = listener;

    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);

        return new UsuarioViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder,
                                 int position) {

        Usuario usuario = listaUsuarios.get(position);

        holder.tvNombre.setText(usuario.getNombre());
        holder.tvCorreo.setText(usuario.getCorreo());
        holder.tvRol.setText(usuario.getRol());

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.editarUsuario(usuario);
            }
        });

        holder.btnCambiarRol.setOnClickListener(v -> {

            if (listener != null) {

                listener.cambiarRol(usuario);

            }

        });

        holder.btnEliminar.setOnClickListener(v -> {

            if (listener != null) {

                listener.eliminarUsuario(usuario);

            }

        });

    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public void actualizarLista(List<Usuario> nuevaLista) {

        listaUsuarios.clear();

        listaUsuarios.addAll(nuevaLista);

        notifyDataSetChanged();

    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {

        CardView cvUsuario;

        TextView tvNombre;
        TextView tvCorreo;
        TextView tvRol;

        Button btnEditar;
        Button btnCambiarRol;
        Button btnEliminar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);

            cvUsuario = itemView.findViewById(R.id.cvUsuario);

            tvNombre = itemView.findViewById(R.id.tvNombreUsuario);
            tvCorreo = itemView.findViewById(R.id.tvCorreoUsuario);
            tvRol = itemView.findViewById(R.id.tvRolUsuario);

            btnEditar = itemView.findViewById(R.id.btnEditarUsuario);
            btnCambiarRol = itemView.findViewById(R.id.btnCambiarRol);
            btnEliminar = itemView.findViewById(R.id.btnEliminarUsuario);

        }

    }

}