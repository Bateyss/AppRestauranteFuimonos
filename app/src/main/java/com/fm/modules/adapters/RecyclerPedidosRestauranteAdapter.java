package com.fm.modules.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.app.carrito.GlobalRestaurantes;
import com.fm.modules.app.pedidos.PlatillosSeleccionadoPorPedido;
import com.fm.modules.entities.RespuestaPedidosDriver;

import java.text.DecimalFormat;
import java.util.List;

public class RecyclerPedidosRestauranteAdapter extends RecyclerView.Adapter<RecyclerPedidosRestauranteAdapter.ViewHolder> {

    List<RespuestaPedidosDriver> pedidosList;
    Context context;
    //SimpleDateFormat ffecha = new SimpleDateFormat("dd/MMM/yyyy");
    DecimalFormat decimalFormat = new DecimalFormat("$ 0.00");
    FragmentActivity fragmentActivity;

    public RecyclerPedidosRestauranteAdapter(Context context, List<RespuestaPedidosDriver> pedidosList, FragmentActivity fragmentActivity) {
        this.context = context;
        this.pedidosList = pedidosList;
        this.fragmentActivity = fragmentActivity;
    }

    @NonNull
    @Override
    public RecyclerPedidosRestauranteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_item_ordenes_actuales, parent, false);
        return new RecyclerPedidosRestauranteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerPedidosRestauranteAdapter.ViewHolder holder, int position) {
        holder.asignarDatos(pedidosList.get(position));
    }

    @Override
    public int getItemCount() {
        return pedidosList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        TextView tvNumeroOrdenH;
        TextView tvFechaH;
        TextView tvTotalH;
        Button leftArrow;
        ConstraintLayout constrauintOne;
        RespuestaPedidosDriver respuestaPedidosDriver = null;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.checkbox);
            tvNumeroOrdenH = itemView.findViewById(R.id.tvNumeroOrdenH);
            tvFechaH = itemView.findViewById(R.id.tvFechaH);
            tvTotalH = itemView.findViewById(R.id.tvTotalH);
            leftArrow = itemView.findViewById(R.id.icon_leftArrow);
            constrauintOne = itemView.findViewById(R.id.constrauintOne);
        }

        public void asignarDatos(final RespuestaPedidosDriver res) {
            respuestaPedidosDriver = res;
            String precioPedido = decimalFormat.format(res.getTotalEnRestautante());
            if (res.getStatus() == 1) {
                tvNumeroOrdenH.setText("Orden #" + res.getPedidoId() + " - Lista");
                tvFechaH.setText(res.getFechaOrdenado());
                //tvFechaH.setText(ffecha.format(res.getFechaOrdenado()));
                tvTotalH.setText(precioPedido);
            } else {
                tvNumeroOrdenH.setText("Orden #" + res.getPedidoId());
                tvFechaH.setText(res.getFechaOrdenado());
                //tvFechaH.setText(ffecha.format(res.getFechaOrdenado()));
                tvTotalH.setText(precioPedido);
            }
            constrauintOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog(res);
                }
            });
        }

        public void dialog(final RespuestaPedidosDriver res) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Pedido");
            builder.setMessage("¿Desea tomar este pedido?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (respuestaPedidosDriver != null) {
                                GlobalRestaurantes.pedidoTomadoId = (int) res.getPedidoId();
                                Intent intent = new Intent(context, PlatillosSeleccionadoPorPedido.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("idPedido", respuestaPedidosDriver.getPedidoId());
                                intent.putExtra("totalPedido", decimalFormat.format(respuestaPedidosDriver.getTotalDePedido()));
                                intent.putExtra("status", respuestaPedidosDriver.getStatus());
                                context.startActivity(intent);
                            }
                        }
                    })
                    .setNegativeButton("No", null).show();
        }


    }


}
