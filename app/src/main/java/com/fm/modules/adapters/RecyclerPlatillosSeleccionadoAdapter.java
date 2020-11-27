package com.fm.modules.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.app.carrito.GlobalRestaurantes;
import com.fm.modules.app.commons.utils.Utilities;
import com.fm.modules.app.login.Logued;
import com.fm.modules.app.pedidos.OpcPlatilloSeleccionado;
import com.fm.modules.entities.RespuestaPedidosDriver;
import com.fm.modules.models.Image;
import com.fm.modules.models.OpcionesDeSubMenuSeleccionado;
import com.fm.modules.models.Pedido;
import com.fm.modules.models.PlatilloSeleccionado;
import com.fm.modules.models.Restaurante;
import com.fm.modules.service.ImageService;
import com.fm.modules.service.OpcionesDeSubMenuSeleccionadoService;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RecyclerPlatillosSeleccionadoAdapter extends RecyclerView.Adapter<RecyclerPlatillosSeleccionadoAdapter.ViewHolder> {

    List<PlatilloSeleccionado> pedidosList;
    Context context;

    //SimpleDateFormat ffecha = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat fhora = new SimpleDateFormat("HH:mm:ss");
    DecimalFormat decimalFormat = new DecimalFormat("0.00");



    public RecyclerPlatillosSeleccionadoAdapter(Context context, List<PlatilloSeleccionado> pedidosList){
        this.context = context;
        this.pedidosList = pedidosList;
    }

    @NonNull
    @Override
    public RecyclerPlatillosSeleccionadoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_detalle_pedido, parent, false);
        return new RecyclerPlatillosSeleccionadoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerPlatillosSeleccionadoAdapter.ViewHolder holder, int position) {
        holder.asignarDatos(pedidosList.get(position));
    }

    @Override
    public int getItemCount() {
        return pedidosList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPlatillo, tvPrecioPlatillo;
        RecyclerView listViewOpciones;
        Long idPlatillo = 0L;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlatillo = itemView.findViewById(R.id.tvPlatillo);
            tvPrecioPlatillo = itemView.findViewById(R.id.tvPrecioPlatillo);
            listViewOpciones = itemView.findViewById(R.id.listViewOpciones);
        }

        public void asignarDatos(final PlatilloSeleccionado platilloSeleccionado) {
            idPlatillo = platilloSeleccionado.getPlatilloSeleccionadoId();
            tvPlatillo.setText(platilloSeleccionado.getPlatillo().getNombre());
            tvPrecioPlatillo.setText("$" +decimalFormat.format(platilloSeleccionado.getPrecio()));
            cargarOpciones();
        }

        private void cargarOpciones() {
            OpcionesPlatillo opcionesPlatillo = new OpcionesPlatillo();
            if(idPlatillo != null && idPlatillo.intValue() != 0){
                opcionesPlatillo.execute();
            }
            System.out.println("Aqui en cargar opciones");
        }

        public class OpcionesPlatillo extends AsyncTask<String, String, List<OpcionesDeSubMenuSeleccionado>> {

            @Override
            protected List<OpcionesDeSubMenuSeleccionado> doInBackground(String... strings) {
                List<OpcionesDeSubMenuSeleccionado> opcionesDeSubMenuSelec = new ArrayList<>();
                Restaurante restaurante = Logued.restauranteLogued;
                try {
                    OpcionesDeSubMenuSeleccionadoService opcionesDeSubMenuService = new OpcionesDeSubMenuSeleccionadoService();
                    opcionesDeSubMenuSelec = opcionesDeSubMenuService.obtenerOpcionesPorPlatillo(idPlatillo.toString());

                }catch (Exception e){
                    System.out.println("Error en UnderThreash:" +e.getMessage() +" " +e.getClass());
                }
                return opcionesDeSubMenuSelec;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(List<OpcionesDeSubMenuSeleccionado> opcionesDeSubMenuSelec) {
                super.onPostExecute(opcionesDeSubMenuSelec);
                try {
                    System.out.println("VALOR DE LISTA: " +opcionesDeSubMenuSelec.size());
                    if (!opcionesDeSubMenuSelec.isEmpty()){
                        ListViewAdapterOpcionesSubMenu adapter = new ListViewAdapterOpcionesSubMenu(opcionesDeSubMenuSelec, context);
                        listViewOpciones.setLayoutManager(new LinearLayoutManager(context.getApplicationContext()));
                        listViewOpciones.setAdapter(adapter);
                    }else{

                    }
                }catch (Throwable throwable){
                    System.out.println("Error Activity: " +throwable.getMessage());
                    throwable.printStackTrace();
                }
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
            }
        }
    }



}
