package com.fm.modules.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.app.login.Logued;
import com.fm.modules.app.pedidos.Principal;
import com.fm.modules.entities.RespuestaPedidosDriver;
import com.fm.modules.models.Driver;
import com.fm.modules.models.Pedido;
import com.fm.modules.models.Restaurante;
import com.fm.modules.models.Usuario;
import com.fm.modules.service.DriverService;
import com.fm.modules.service.PedidoService;
import com.fm.modules.service.UsuarioService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecyclerPedidosActualesAdapter extends RecyclerView.Adapter<RecyclerPedidosActualesAdapter.ViewHolder> {

    List<RespuestaPedidosDriver> pedidosList;
    Context context;

    //SimpleDateFormat ffecha = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat fhora = new SimpleDateFormat("HH:mm:ss");

    RespuestaPedidosDriver resObt;
    Restaurante buscarRestaurante = new Restaurante();
    Driver buscarDriver = new Driver();
    Usuario buscarUsuario = new Usuario();
    RespuestaPedidosDriver upRespuestaPedidoDriver = new RespuestaPedidosDriver();

    public RecyclerPedidosActualesAdapter(Context context, List<RespuestaPedidosDriver> pedidosList) {
        this.context = context;
        this.pedidosList = pedidosList;
    }

    @NonNull
    @Override
    public RecyclerPedidosActualesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_pedidos2, parent, false);
        return new RecyclerPedidosActualesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerPedidosActualesAdapter.ViewHolder holder, int position) {
        if (this.pedidosList.isEmpty()) {
            Toast.makeText(this.context, "No tienes pedidos a entregar", Toast.LENGTH_SHORT).show();
        } else {
            holder.asignarDatos(pedidosList.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return pedidosList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView tvNombreRestaurante;
        AppCompatTextView tvDireccionCliente;
        AppCompatTextView tvPrecioPedido;
        AppCompatTextView tvFormadePago;
        AppCompatTextView btnTomarPedido;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNombreRestaurante = itemView.findViewById(R.id.tvNombreRestaurante);
            tvDireccionCliente = itemView.findViewById(R.id.tvDirecionCliente);
            tvPrecioPedido = itemView.findViewById(R.id.tvPrecioPedido);
            tvFormadePago = itemView.findViewById(R.id.tvFormadePago);
            btnTomarPedido = itemView.findViewById(R.id.btnTomarPedido);
        }

        public void asignarDatos(final RespuestaPedidosDriver res) {
            tvNombreRestaurante.setText(res.getUsuario());
            tvDireccionCliente.setText(res.getDireccion());
            tvPrecioPedido.setText("$" + String.valueOf(res.getTotalDePedido()));
            tvFormadePago.setText("Forma de pago: " + res.getFormaDePago());
            btnTomarPedido.setText("Entregar a cliente");
            btnTomarPedido.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog(res);
                }
            });
        }

        public void dialog(final RespuestaPedidosDriver res) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Pedido");
            builder.setMessage("¿Pedido Entregado?").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    buscarRestaurante.setRestauranteId(res.getRestauranteId());
                    buscarDriver.setDriverId(res.getDriverId());
                    buscarUsuario.setUsuarioId(res.getUsuarioId());
                    resObt = res;
                    buscarRestaurante = Logued.restauranteLogued;
                    ObtenerDriver2 obtenerDriver = new ObtenerDriver2();
                    obtenerDriver.execute();
                    ObtenerUsuario2 obtenerUsuario = new ObtenerUsuario2();
                    obtenerUsuario.execute();
                    ActualizarPedido2 actualizarPedido = new ActualizarPedido2();
                    actualizarPedido.execute();

                    Intent intent = new Intent(context, Principal.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    //reiniciarAsynkProcess();
                    Toast.makeText(context, "Ha decidido tomar este pedido..", Toast.LENGTH_SHORT).show();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(context, "No tomocesa este pedido..", Toast.LENGTH_SHORT).show();
                }
            }).show();
        }
    }


    public class ObtenerDriver2 extends AsyncTask<String, String, List<Driver>> {

        @Override
        protected List<Driver> doInBackground(String... strings) {
            List<Driver> drivers = new ArrayList<>();
            try {
                DriverService driverService = new DriverService();
                Driver driver = driverService.obtenerDriverPorId(buscarDriver.getDriverId());
                buscarDriver = driver;
            } catch (Exception e) {
                System.out.println("Error en UnderThreash ObtenerDriver:" + e.getMessage() + " " + e.getClass());
            }
            return drivers;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Driver> drivers) {
            super.onPostExecute(drivers);
            try {
                if (!drivers.isEmpty()) {

                    Toast.makeText(context, "Pedidso Cargados" + drivers.size(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Pedidos No Cargados" + drivers.size(), Toast.LENGTH_SHORT).show();
                }
                //reiniciarAsynkProcess();
            } catch (Throwable throwable) {
                System.out.println("Error Activity: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    public class ObtenerUsuario2 extends AsyncTask<String, String, List<Usuario>> {

        @Override
        protected List<Usuario> doInBackground(String... strings) {
            List<Usuario> usuarios = new ArrayList<>();
            try {
                UsuarioService usuarioService = new UsuarioService();
                Usuario usuario = usuarioService.obtenerUsuarioPorId(buscarUsuario.getUsuarioId());
                buscarUsuario = usuario;

            } catch (Exception e) {
                System.out.println("Error en UnderThreash ObtenerUsuario:" + e.getMessage() + " " + e.getClass());
            }
            return usuarios;
        }
    }

    public class ActualizarPedido2 extends AsyncTask<String, String, List<Pedido>> {

        @Override
        protected List<Pedido> doInBackground(String... strings) {
            List<Pedido> usuarios = new ArrayList<>();
            try {
                PedidoService pedidoService2 = new PedidoService();
                Pedido upPedido2 = new Pedido();
                upPedido2.setPedidoId(resObt.getPedidoId());
                upPedido2.setRestaurante(buscarRestaurante);
                upPedido2.setDrivers(buscarDriver);
                upPedido2.setUsuario(buscarUsuario);
                upPedido2.setStatus(4);
                upPedido2.setFormaDePago(resObt.getFormaDePago());
                upPedido2.setTotalDePedido(resObt.getTotalDePedido());
                upPedido2.setTotalEnRestautante(resObt.getTotalEnRestautante());
                upPedido2.setTotalDeCargosExtra(resObt.getTotalDeCargosExtra());
                upPedido2.setTotalEnRestautanteSinComision(resObt.getTotalEnRestautanteSinComision());
                upPedido2.setPedidoPagado(resObt.isPedidoPagado());
                upPedido2.setFechaOrdenado((new Date()).toString());
                upPedido2.setTiempoPromedioEntrega(resObt.getTiempoPromedioEntrega());
                upPedido2.setPedidoEntregado(true);
                upPedido2.setNotas(resObt.getNotas());
                upPedido2.setTiempoAdicional(resObt.getTiempoAdicional());
                upPedido2.setDireccion(resObt.getDireccion());
                System.out.println(upPedido2.toString());
                System.out.println(upPedido2.getStatus());
                pedidoService2.actualizarPedidoPorId(upPedido2);
            } catch (Exception e) {
                System.out.println("Error en ActualizarPedido2 asynck :" + e.getMessage() + " " + e.getClass());
            }
            return usuarios;
        }
    }
}