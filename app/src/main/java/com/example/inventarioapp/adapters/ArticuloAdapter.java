package com.example.inventarioapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventarioapp.R;
import com.example.inventarioapp.models.Articulo;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ArticuloAdapter extends RecyclerView.Adapter<ArticuloAdapter.ArticuloViewHolder> {

    private final List<Articulo> listaArticulos;
    private final OnArticuloClickListener listener;

    public interface OnArticuloClickListener{
        void onArticuloClick(Articulo articulo);
    }

    public ArticuloAdapter(List<Articulo> listaArticulos,
                           OnArticuloClickListener listener){

        this.listaArticulos = listaArticulos;
        this.listener = listener;

    }

    @NonNull
    @Override
    public ArticuloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_articulo,parent,false);

        return new ArticuloViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ArticuloViewHolder holder, int position) {

        Articulo articulo = listaArticulos.get(position);

        holder.tvCodigo.setText(String.valueOf(articulo.getCodigo()));

        holder.tvDescripcion.setText(articulo.getDescripcion());

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(new Locale("es","CO"));

        holder.tvPrecio.setText(
                formato.format(articulo.getPrecio())
        );

        holder.tvOferta.setText(
                articulo.isOferta() ? "Sí" : "No"
        );

        if(articulo.getPrecio() >= 100000){

            holder.tvEstado.setText("PREMIUM");

            holder.tvEstado.setBackgroundColor(
                    android.graphics.Color.parseColor("#FFC107")
            );

        }else{

            holder.tvEstado.setText("ESTÁNDAR");

            holder.tvEstado.setBackgroundColor(
                    android.graphics.Color.parseColor("#4CAF50")
            );

        }

        holder.cvContenedor.setOnClickListener(v -> {

            if(listener != null){

                listener.onArticuloClick(articulo);

            }

        });

    }

    @Override
    public int getItemCount() {
        return listaArticulos.size();
    }

    public void actualizarLista(List<Articulo> nuevaLista){

        listaArticulos.clear();

        listaArticulos.addAll(nuevaLista);

        notifyDataSetChanged();

    }

    public static class ArticuloViewHolder extends RecyclerView.ViewHolder{

        TextView tvCodigo;
        TextView tvDescripcion;
        TextView tvPrecio;
        TextView tvEstado;
        TextView tvOferta;

        CardView cvContenedor;

        public ArticuloViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCodigo=itemView.findViewById(R.id.tvItemCodigo);
            tvDescripcion=itemView.findViewById(R.id.tvItemDescripcion);
            tvPrecio=itemView.findViewById(R.id.tvItemPrecio);
            tvEstado=itemView.findViewById(R.id.tvItemEstado);
            tvOferta=itemView.findViewById(R.id.tvOferta);

            cvContenedor=itemView.findViewById(R.id.cvContenedor);

        }

    }

}