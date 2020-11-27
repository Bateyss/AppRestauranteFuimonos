package com.fm.modules.app.pedidos;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.adapters.RecyclerPlatillosSeleccionadoAdapter;
import com.fm.modules.app.carrito.GlobalRestaurantes;
import com.fm.modules.app.login.Logued;
import com.fm.modules.app.menu.MenuBotton;
import com.fm.modules.app.ui.PrincipalRestaurante;
import com.fm.modules.models.Pedido;
import com.fm.modules.models.PlatilloSeleccionado;
import com.fm.modules.models.Restaurante;
import com.fm.modules.service.PedidoService;
import com.fm.modules.service.PlatilloSeleccionadoService;

import java.util.ArrayList;
import java.util.List;

public class PlatillosSeleccionadoPorPedido extends AppCompatActivity {

    Long idPedido;
    String totalPedido;
    int status;
    RecyclerView rvPlatillosSeleccionados;
    Button btnTomarPedido, btnAsignarDriver, btnRechazar;
    TextView tvTotal, tvStatus, tvOrden, tvStatusOrden;
    AppCompatImageView ivBack;
    public static List<PlatilloSeleccionado> platilloSeleccionados;
    View viewGlobal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frg_platillos_selecccionado_pedido);

        idPedido = getIntent().getLongExtra("idPedido", 1L);
        totalPedido = getIntent().getStringExtra("totalPedido");
        status = getIntent().getIntExtra("status", 0);

        btnRechazar = findViewById(R.id.btnRechazar);
        rvPlatillosSeleccionados = findViewById(R.id.rvPlatillosSeleccionados);
        btnTomarPedido = findViewById(R.id.btnTomarPedido);
        btnAsignarDriver = findViewById(R.id.btnAsignarDriver);
        tvTotal = findViewById(R.id.tvTotal);
        tvStatus = findViewById(R.id.tvStatus);
        tvOrden = findViewById(R.id.tvOrden);
        tvStatusOrden = findViewById(R.id.tvStatusOrden);
        ivBack = findViewById(R.id.ivBack);
        tvTotal.setText("Total: " + totalPedido);
        tvOrden.setText("Orden #" + idPedido);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MenuBotton.class);
                startActivity(i);
            }
        });
        BuscarPedido buscarPedido = new BuscarPedido();
        buscarPedido.execute();
        rechazarListener();
    }

    public class BuscarPedido extends AsyncTask<String, String, Pedido> {

        @Override
        protected Pedido doInBackground(String... strings) {
            Pedido pedidoBuscar = null;
            try {
                if (GlobalRestaurantes.pedidoTomadoId != null) {
                    PedidoService pedidoService = new PedidoService();
                    pedidoBuscar = pedidoService.obtenerPedidoPorId(GlobalRestaurantes.pedidoTomadoId.longValue());
                }
            } catch (Exception e) {
                System.out.println("Error en UnderThreash:" + e.getMessage() + " " + e.getClass());
            }
            return pedidoBuscar;
        }

        @Override
        protected void onPostExecute(Pedido pedido) {
            super.onPostExecute(pedido);
            try {
                if (pedido != null) {
                    Logued.pedidoAsiganar = pedido;
                    if (pedido != null) {
                        if (pedido.getStatus() == 1) {
                            btnAsignarDriver.setVisibility(View.VISIBLE);
                            btnTomarPedido.setVisibility(View.INVISIBLE);
                            btnRechazar.setVisibility(View.INVISIBLE);
                            tvStatus.setText("Pedido Listo");
                            tvStatus.setTextColor(getResources().getColor(R.color.enable));
                            tvStatusOrden.setText("Orden para entregar");
                        } else {
                            btnAsignarDriver.setVisibility(View.INVISIBLE);
                            btnTomarPedido.setVisibility(View.VISIBLE);
                            btnRechazar.setVisibility(View.VISIBLE);
                        }
                    } else {
                        btnAsignarDriver.setVisibility(View.INVISIBLE);
                        btnTomarPedido.setVisibility(View.INVISIBLE);
                        btnRechazar.setVisibility(View.INVISIBLE);
                    }


                    btnTomarPedido.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            btnTomarPedido.setEnabled(false);
                            btnRechazar.setEnabled(false);
                            ActualizarPedido actualizarPedido = new ActualizarPedido();
                            actualizarPedido.execute();
                        }
                    });

                    btnAsignarDriver.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActualizarPedidoDriver actualizarPedidoDriver = new ActualizarPedidoDriver();
                            actualizarPedidoDriver.execute();
                        }
                    });
                    PlatilloSelecPedido platilloSelecPedido = new PlatilloSelecPedido();
                    platilloSelecPedido.execute();
                }
            } catch (Throwable throwable) {
                System.out.println("Error Activity: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }
    }

    private void rechazarListener() {
        btnRechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTomarPedido.setEnabled(false);
                btnRechazar.setEnabled(false);
                RechazarPedido rechazarPedido = new RechazarPedido();
                rechazarPedido.execute();
            }
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private class PlatilloSelecPedido extends AsyncTask<String, String, List<PlatilloSeleccionado>> {

        @Override
        protected List<PlatilloSeleccionado> doInBackground(String... strings) {
            platilloSeleccionados = new ArrayList<>();
            try {
                if (Logued.pedidoAsiganar != null) {
                    PlatilloSeleccionadoService platilloSeleccionadoService = new PlatilloSeleccionadoService();
                    platilloSeleccionados = platilloSeleccionadoService.platilloSeleccionadoPorPedido(String.valueOf(Logued.pedidoAsiganar.getPedidoId()));
                }
            } catch (Exception e) {
                System.out.println("Error en UnderThreash:" + e.getMessage() + " " + e.getClass());
            }
            return platilloSeleccionados;
        }

        @Override
        protected void onPostExecute(List<PlatilloSeleccionado> platilloSeleccionados) {
            super.onPostExecute(platilloSeleccionados);
            try {
                if (!platilloSeleccionados.isEmpty()) {
                    RecyclerPlatillosSeleccionadoAdapter adapter = new RecyclerPlatillosSeleccionadoAdapter(getApplicationContext(), platilloSeleccionados);
                    rvPlatillosSeleccionados.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
                    rvPlatillosSeleccionados.setAdapter(adapter);
                }
            } catch (Throwable throwable) {
                System.out.println("Error Activity: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }
    }


    private class ActualizarPedido extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                PedidoService pedidoService = new PedidoService();
                Logued.pedidoAsiganar.setStatus(1);
                pedidoService.actualizarPedidoPorId(Logued.pedidoAsiganar);
                Logued.pedidoAsiganar = null;
            } catch (Exception e) {
                System.out.println("Error en UnderThreash:" + e.getMessage() + " " + e.getClass());
            }
            return "null";
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            try {
                Intent i = new Intent(PlatillosSeleccionadoPorPedido.this, MenuBotton.class);
                startActivity(i);
            } catch (Throwable throwable) {
                System.out.println("Error Activity: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }
    }

    private class RechazarPedido extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            try {
                PedidoService pedidoService = new PedidoService();
                Logued.pedidoAsiganar.setStatus(5);
                pedidoService.actualizarPedidoPorId(Logued.pedidoAsiganar);
                Logued.pedidoAsiganar = null;
            } catch (Exception e) {
                System.out.println("Error en UnderThreash:" + e.getMessage() + " " + e.getClass());
            }
            return "opcionesDeSubMenuSelec";
        }

        @Override
        protected void onPostExecute(String opcionesDeSubMenuSelec) {
            super.onPostExecute(opcionesDeSubMenuSelec);
            try {
                Intent i = new Intent(PlatillosSeleccionadoPorPedido.this, MenuBotton.class);
                startActivity(i);
            } catch (Throwable throwable) {
                System.out.println("Error Activity: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }
    }


    private class ActualizarPedidoDriver extends AsyncTask<String, String, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            Restaurante restaurante = Logued.restauranteLogued;
            int res = 0;
            try {
                PedidoService pedidoService = new PedidoService();
                Logued.pedidoAsiganar.setStatus(2);
                res = pedidoService.actualizarPedidoDriver(Logued.pedidoAsiganar);
                Logued.pedidoAsiganar = null;
            } catch (Exception e) {
                System.out.println("Error en UnderThreash actualizar:" + e.getMessage() + " " + e.getClass());
            }
            return res;
        }

        @Override
        protected void onPostExecute(Integer res) {
            super.onPostExecute(res);
            try {
                if (res == 555) {
                    Toast.makeText(PlatillosSeleccionadoPorPedido.this, "No se ha encontrado driver disponibles", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), PrincipalRestaurante.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    Toast.makeText(PlatillosSeleccionadoPorPedido.this, "Pedido enviado...", Toast.LENGTH_LONG).show();
                    Logued.pedidoLogued = new Pedido();
                    Intent i = new Intent(PlatillosSeleccionadoPorPedido.this, MenuBotton.class);
                    startActivity(i);
                }

            } catch (Throwable throwable) {
                System.out.println("Error Activity: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }
    }

}
