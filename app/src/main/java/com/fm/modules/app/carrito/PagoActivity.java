package com.fm.modules.app.carrito;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.app.login.Logued;
import com.fm.modules.app.menu.MenuBotton;
import com.fm.modules.models.Driver;
import com.fm.modules.models.OpcionesDeSubMenuSeleccionado;
import com.fm.modules.models.Pedido;
import com.fm.modules.models.PlatilloSeleccionado;
import com.fm.modules.models.Restaurante;
import com.fm.modules.models.Usuario;
import com.fm.modules.service.Constantes;
import com.fm.modules.service.DriverService;
import com.fm.modules.service.OpcionSubMenuSeleccionadoService;
import com.fm.modules.service.PedidoService;
import com.fm.modules.service.PlatilloSeleccionadoService;
import com.fm.modules.service.UsuarioService;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PagoActivity extends AppCompatActivity {

    TextView txtTotal;
    TextView txtDireccion1;
    TextView txtDireccion2;
    MaterialCardView btnTarjeta;
    MaterialCardView btnEfectivo;
    Button btnPagar;
    Pedido pedido;
    List<PlatilloSeleccionado> platilloSeleccionados;
    List<OpcionesDeSubMenuSeleccionado> opcionesSeleccionadas;
    ImageView checkTarjeta;
    ImageView checkEfectivo;
    MaterialTextView changueDir;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);
        queue = Volley.newRequestQueue(PagoActivity.this);
        txtTotal = (TextView) findViewById(R.id.pagoTxtTotal);
        txtDireccion1 = (TextView) findViewById(R.id.pagoTxtDireccion);
        txtDireccion2 = (TextView) findViewById(R.id.pagoTxtDireccion2);
        btnTarjeta = (MaterialCardView) findViewById(R.id.pagoBtnTarjeta);
        btnEfectivo = (MaterialCardView) findViewById(R.id.pagoBtnEfectivo);
        checkTarjeta = (ImageView) findViewById(R.id.pagoBtnTarjetaCheck);
        checkEfectivo = (ImageView) findViewById(R.id.pagoBtnEfectivoCheck);
        btnPagar = (Button) findViewById(R.id.pagoBtnPagar);
        changueDir = (MaterialTextView) findViewById(R.id.pagoChangeLocation);
        pedido = Logued.pedidoActual;
        btnPagar.setEnabled(false);
        btnPagar.setBackgroundColor(getResources().getColor(R.color.lightGray));

        platilloSeleccionados = new ArrayList<>();
        opcionesSeleccionadas = new ArrayList<>();
        mostrarDatos();
        listenerBotones();
        onBack();
        notifications();
    }

    public void onBack() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                GlobalCarrito.toShopinCart = true;
                Intent intent = new Intent(PagoActivity.this, MenuBotton.class);
                startActivity(intent);
            }
        };
        getOnBackPressedDispatcher().addCallback(PagoActivity.this, callback);
    }

    public void obtenerPedido() {
        if (pedido != null) {
            platilloSeleccionados = Logued.platillosSeleccionadosActuales;
            opcionesSeleccionadas = Logued.opcionesDeSubMenusEnPlatillosSeleccionados;
        }
    }

    private void listenerBotones() {
        btnTarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pedido != null) {
                    pedido.setFormaDePago("Tarjeta");
                    btnPagar.setEnabled(true);
                    btnPagar.setBackgroundColor(getResources().getColor(R.color.orange));
                }
                Logued.pedidoActual = pedido;
                checkTarjeta.setImageResource(R.drawable.ic_checked_round_lime);
                checkEfectivo.setImageResource(R.drawable.ic_checked_round_white);
                GlobalCarrito.toPagoTarjeta = true;
                Intent intent = new Intent(PagoActivity.this, MenuBotton.class);
                startActivity(intent);
            }
        });
        btnEfectivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pedido != null) {
                    pedido.setFormaDePago("Efectivo");
                    btnPagar.setEnabled(true);
                    btnPagar.setBackgroundColor(getResources().getColor(R.color.orange));
                }
                Logued.pedidoActual = pedido;
                checkTarjeta.setImageResource(R.drawable.ic_checked_round_white);
                checkEfectivo.setImageResource(R.drawable.ic_checked_round_lime);
            }
        });
        btnPagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTarjeta.setEnabled(false);
                btnEfectivo.setEnabled(false);
                obtenerPedido();
                btnPagar.setEnabled(false);
                GuardarPedidoAsync guardarPedidoAsync = new GuardarPedidoAsync();
                guardarPedidoAsync.execute();
            }
        });
        changueDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PagoActivity.this, ProcesarCarritoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void mostrarDatos() {
        try {
            List<PlatilloSeleccionado> lista = Logued.platillosSeleccionadosActuales;
            double total1 = 0.00;
            double total2 = 0;
            double total3 = 0;
            double descuento = 0;
            double total4 = 0;
            String direccion = "";
            if (lista != null) {
                if (!lista.isEmpty()) {
                    for (PlatilloSeleccionado pl : lista) {
                        total1 = total1 + pl.getPrecio();
                    }
                    //descuento = lista.get(0).getPlatillo().getMenu().getRestaurante().getDescuento();
                    direccion = lista.get(0).getPedido().getDireccion();
                }
            }
            String[] strings = direccion.split(" ; ", 7);
            if (strings.length > 3) {
                txtDireccion1.setText(strings[0]);
                String dir2 = strings[1] + " , " + strings[3];
                txtDireccion2.setText(dir2);
            }
            total2 = total1 * 0.13;
            total3 = total1 * 0.05;
            // descuento = descuento * total1;
            total4 = total1 + total2 + total3 - descuento;
            pedido.setTotalDePedido(total1);
            pedido.setTotalEnRestautante(total4);
            pedido.setTotalDeCargosExtra(total3);
            pedido.setTotalEnRestautanteSinComision(total1 + total2 - descuento);
            DecimalFormat decimalFormat = new DecimalFormat("$ #,##0.00");
            txtTotal.setText(String.valueOf(decimalFormat.format(total4)));
        } catch (Exception e) {
            System.out.println("error carrito: " + e);
        }
    }

    public boolean isNetActive() {
        boolean c = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                c = true;
            }
        } catch (Exception e) {
            Log.e("error", "" + "error al comprobar conexion");
            Log.e("error", "" + e);
            c = false;
        }
        return c;
    }

    public class GuardarPedidoAsync extends AsyncTask<String, String, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            int b = 0;
            try {
                if (pedido != null) {
                    if (!platilloSeleccionados.isEmpty()) {
                        PedidoService pedidoService = new PedidoService();
                        pedido.setPedidoId(0L);
                        pedido.setFechaOrdenado((new Date()).toString());
                        DriverService driverService = new DriverService();
                        UsuarioService usuarioService = new UsuarioService();
                        Usuario usuario = usuarioService.obtenerUsuarioPorId(1L);
                        Driver driver = driverService.obtenerDriverPorId(1L);
                        pedido.setDrivers(driver);
                        pedido.setUsuario(usuario);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        pedido.setFechaOrdenado(simpleDateFormat.format(new Date()));
                        pedido.setStatus(0);
                        if (isNetActive() && driver != null && usuario != null) {
                            System.out.println("pedido antes de guardar!!!!!!!!!!!!!!!!!!!!!!!!");
                            ObjectMapper objectMapper = new ObjectMapper();
                            String fd = objectMapper.writeValueAsString(pedido);
                            System.out.println(fd);
                            Pedido per = pedidoService.crearPedido(pedido);
                            if (per != null) {
                                b = 1;
                                GlobalCarrito.pedidoRegistrado = per;
                            } else {
                                b = 2;
                            }
                            if (b == 1) {
                                PlatilloSeleccionadoService platilloSeleccionadoService = new PlatilloSeleccionadoService();
                                for (PlatilloSeleccionado pla : platilloSeleccionados) {
                                    pla.setPedido(per);
                                    PlatilloSeleccionado pls = platilloSeleccionadoService.crearPlatilloSeleccionado(pla);
                                    if (pls != null) {
                                        b = 3;
                                        if (!opcionesSeleccionadas.isEmpty()) {
                                            OpcionSubMenuSeleccionadoService opcionSubMenuSeleccionadoService = new OpcionSubMenuSeleccionadoService();
                                            for (OpcionesDeSubMenuSeleccionado opc : opcionesSeleccionadas) {
                                                if (opc.getPlatilloSeleccionado().getPlatilloSeleccionadoId().intValue() == pla.getPlatilloSeleccionadoId().intValue()) {
                                                    opc.setPlatilloSeleccionado(pls);
                                                    OpcionesDeSubMenuSeleccionado pp = opcionSubMenuSeleccionadoService.crearOpcionSubMenu(opc);
                                                    if (pp != null) {
                                                        b = 3;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        b = 2;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("error asynk guardar pedido: " + e);
            }
            return b;
        }

        @Override
        protected void onPostExecute(Integer b) {
            super.onPostExecute(b);
            int procesed = b;
            btnPagar.setEnabled(true);
            switch (procesed) {
                case 0:
                    AlertDialog dialog = new AlertDialog.Builder(PagoActivity.this)
                            .setView(R.layout.dialog_server_err)
                            .setCancelable(true)
                            .setPositiveButton("Continuar", null)
                            .show();
                    break;
                case 2:
                    Intent intent = new Intent(PagoActivity.this, PedidoNoRegistrado.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                case 3:
                    Intent intent2 = new Intent(PagoActivity.this, PedidoRegistrado.class);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                    break;
            }
        }
    }

    private void notifications() {
        Restaurante restaurante = Logued.restauranteLogued;
        if (restaurante != null) {
            volleyMethod(restaurante.getRestauranteId());
        }
    }

    public void makeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification";
            NotificationChannel channel = new NotificationChannel("NOTIFICACION", name, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(PagoActivity.this, "NOTIFICACION");
        builder.setSmallIcon(R.drawable.ic_app_logo);
        builder.setContentTitle("Fuimonos Restaurante");
        builder.setContentText("tienes un pedido pendiente");
        builder.setColor(Color.BLUE);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setLights(Color.MAGENTA, 1000, 1000);
        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(PagoActivity.this);
        notificationManagerCompat.notify(0, builder.build());
    }

    public void volleyMethod(Long g) {
        String url = Constantes.DOMINIO.concat("/push/getpedidosrcnd/").concat(g.toString());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println("!!!!!!!!! dess response: " + response);
                        if (response != null) {
                            try {
                                if (GlobalRestaurantes.pedidoss == null) {
                                    GlobalRestaurantes.pedidoss = 0;
                                }
                                int respuesta = Integer.parseInt(response);
                                if (GlobalRestaurantes.pedidoss == 0 && respuesta > 0) {
                                    GlobalRestaurantes.pedidoss = respuesta;
                                    makeNotificacion();
                                }
                                if (GlobalRestaurantes.pedidoss > 0 && respuesta > GlobalRestaurantes.pedidoss) {
                                    GlobalRestaurantes.pedidoss = respuesta;
                                    makeNotificacion();
                                }
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //none
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
        getAvailableMemory();
    }

    private void getAvailableMemory() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            if (memoryInfo.lowMemory) {
                Toast.makeText(PagoActivity.this, "Memoria Baja", Toast.LENGTH_SHORT).show();
                Toast.makeText(PagoActivity.this, "limpiando..", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PagoActivity.this, PagoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception ignore) {
        }
    }
}