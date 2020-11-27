package com.fm.modules.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.models.OpcionesDeSubMenuSeleccionado;

import java.util.List;

public class ListViewAdapterOpcionesSubMenu extends RecyclerView.Adapter<ListViewAdapterOpcionesSubMenu.ViewHolder> {

    private LayoutInflater layoutInflater;
    private Context context;
    private List<OpcionesDeSubMenuSeleccionado> lista;

    public ListViewAdapterOpcionesSubMenu(List<OpcionesDeSubMenuSeleccionado> lista, Context context) {
        this.context = context;
        this.lista = lista;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_item_opciones, parent, false);
        return new ListViewAdapterOpcionesSubMenu.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.asignarDatos(lista.get(position));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvOpcionSubMenuSelec;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOpcionSubMenuSelec = itemView.findViewById(R.id.tvOpcionSubMenuSelec);
        }

        public void asignarDatos(final OpcionesDeSubMenuSeleccionado opc) {
            tvOpcionSubMenuSelec.setText(opc.getNombre());
        }
    }
}
