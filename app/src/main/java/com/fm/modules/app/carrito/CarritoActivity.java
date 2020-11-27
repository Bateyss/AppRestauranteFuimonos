package com.fm.modules.app.carrito;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.adapters.RecyclerPlatillosSeleccionadosAdapter;
import com.fm.modules.app.login.Logued;
import com.fm.modules.app.menu.MenuBotton;
import com.fm.modules.app.pedidos.RestauranteMenuActivity2;
import com.fm.modules.models.PlatilloSeleccionado;
import com.fm.modules.models.Promociones;
import com.fm.modules.models.Restaurante;
import com.fm.modules.service.Constantes;
import com.fm.modules.service.PromocionesService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

//import com.fm.modules.app.restaurantes.RestaurantePorCategoria;

public class CarritoActivity extends Fragment {

    private RecyclerView carritoRecicler;
    private TextView totalOrdenTxtVw;
    private TextView impuestoTxtVw;
    private TextView cargoFuimonosTxtVw;
    private TextView promocionTxtVw;
    private TextView totalaCancelarTxtVw;
    private Button btnCodigo;
    private Button btnMas;
    private Button btnTerminate;
    private View viewGlobal;
    private AppCompatImageView back;
    private EditText txtCodigo;
    private String codigoAProbar;
    RequestQueue queue;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frg_carrito_actual, container, false);
        viewGlobal = view;
        queue = Volley.newRequestQueue(viewGlobal.getContext());
        carritoRecicler = (RecyclerView) view.findViewById(R.id.rvcarrito_compras);
        totalOrdenTxtVw = (TextView) view.findViewById(R.id.carritoTotal1);
        impuestoTxtVw = (TextView) view.findViewById(R.id.carritoTotal2);
        cargoFuimonosTxtVw = (TextView) view.findViewById(R.id.carritoTotal3);
        promocionTxtVw = (TextView) view.findViewById(R.id.carritoDescuento);
        totalaCancelarTxtVw = (TextView) view.findViewById(R.id.carritoTotal4);
        btnCodigo = (Button) view.findViewById(R.id.carritoBtnCodigo);
        btnMas = (Button) view.findViewById(R.id.carritoBtnMas);
        btnTerminate = (Button) view.findViewById(R.id.carritoBtnTerminar);
        back = (AppCompatImageView) view.findViewById(R.id.ivBack);
        txtCodigo = (EditText) view.findViewById(R.id.carritoTxtCodigo);
        btnTerminate.setEnabled(false);
        mostrarCarrito();
        btnListeners();
        backListener();
        onBack();
        btnCodigoListener();
        notifications();
        return view;
    }

    private void btnCodigoListener() {
        btnCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtCodigo.getText().toString() != null && !"".equals(txtCodigo.getText().toString())) {
                    if (GlobalUsuario.codigoUsado == null) {
                        GlobalUsuario.codigoUsado = new ArrayList<>();
                    }
                    if (!GlobalUsuario.codigoUsado.contains(txtCodigo.getText().toString())) {
                        GlobalUsuario.codigoUsado.add(txtCodigo.getText().toString());
                        btnCodigo.setEnabled(false);
                        codigoAProbar = txtCodigo.getText().toString();
                        CargarPromos cargarPromos = new CargarPromos();
                        cargarPromos.execute();
                    } else {
                        Toast.makeText(viewGlobal.getContext(), "Ya haz usado este codigo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(viewGlobal.getContext(), "Ingresa Un Codigo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void backListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showFragment(new MenuDeRestauranteFragment());
                Intent intent = new Intent(viewGlobal.getContext(), RestauranteMenuActivity2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    public void onBack() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                //showFragment(new MenuDeRestauranteFragment());
                Intent intent = new Intent(viewGlobal.getContext(), RestauranteMenuActivity2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
        getActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
    }

    private void mostrarCarrito() {
        try {
            List<PlatilloSeleccionado> lista = Logued.platillosSeleccionadosActuales;
            double total1 = 0.00;
            double total2 = 0;
            double total3 = 0;
            double descuento = 0;
            double total4 = 0;
            if (lista == null || lista.isEmpty()) {
                showFragment(new CarritoEmptyActivity());
            }
            if (lista != null) {
                if (!lista.isEmpty()) {
                    for (PlatilloSeleccionado pl : lista) {
                        total1 = total1 + pl.getPrecio();
                    }
                    RecyclerPlatillosSeleccionadosAdapter adapter = new RecyclerPlatillosSeleccionadosAdapter(lista, viewGlobal.getContext(), getActivity());
                    carritoRecicler.setLayoutManager(new LinearLayoutManager(viewGlobal.getContext(), LinearLayoutManager.VERTICAL, false));
                    carritoRecicler.setAdapter(adapter);
                    btnTerminate.setEnabled(true);
                } else {
                    showFragment(new CarritoEmptyActivity());
                }
            } else {
                showFragment(new CarritoEmptyActivity());
            }
            // total2 = total1 * 0.13;
            // total3 = total1 * 0.05;
            // descuento = descuento * total1;
            if (GlobalUsuario.descuento != null) {
                descuento = GlobalUsuario.descuento;
            }
            total4 = total1 + total2 + total3 - descuento;
            DecimalFormat decimalFormat = new DecimalFormat("$ #,##0.00");
            totalOrdenTxtVw.setText(String.valueOf(decimalFormat.format(total1)));
            impuestoTxtVw.setText(String.valueOf(decimalFormat.format(total2)));
            cargoFuimonosTxtVw.setText(String.valueOf(decimalFormat.format(total3)));
            promocionTxtVw.setText(String.valueOf(decimalFormat.format(descuento)));
            totalaCancelarTxtVw.setText(String.valueOf(decimalFormat.format(total4)));
        } catch (Exception e) {
            System.out.println("error carrito: " + e);
        }
    }

    public void btnListeners() {
        btnCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent i = new Intent(viewGlobal.getContext(), RestauranteMenuActivity.class);
                //startActivity(i);
                //showFragment(new MenuDeRestauranteFragment());
                Intent intent = new Intent(viewGlobal.getContext(), RestauranteMenuActivity2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        btnTerminate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(viewGlobal.getContext(), ProcesarCarritoActivity.class);
                startActivity(i);
            }
        });
    }

    private void showFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private class CargarPromos extends AsyncTask<Long, String, List<Promociones>> {

        @Override
        protected List<Promociones> doInBackground(Long... longs) {
            List<Promociones> promociones = new ArrayList<>();
            try {
                PromocionesService promocionesService = new PromocionesService();
                promociones = promocionesService.obtenerPromocionesDeHoyNonDelay();
            } catch (Exception e) {
                System.out.println("*** error asynk imagePerfil: " + e);
            }
            return promociones;
        }

        @Override
        protected void onPostExecute(List<Promociones> promociones) {
            super.onPostExecute(promociones);
            if (!promociones.isEmpty()) {
                boolean encontrado = false;
                for (Promociones promocion : promociones) {
                    if (promocion.getCodigo() != null && codigoAProbar.equals(promocion.getCodigo())) {
                        encontrado = true;
                        if (promocion.getPorcentajeDescuento() != null) {
                            try {

                                List<PlatilloSeleccionado> lista = Logued.platillosSeleccionadosActuales;
                                DecimalFormat decimalFormat = new DecimalFormat("- $ #,##0.00");
                                DecimalFormat decimalFormat1 = new DecimalFormat("$ #,##0.00");
                                double total1 = 0;
                                if (lista != null) {
                                    if (!lista.isEmpty()) {
                                        for (PlatilloSeleccionado pl : lista) {
                                            total1 = total1 + pl.getPrecio();
                                        }
                                    }
                                }
                                double d = total1 * promocion.getPorcentajeDescuento().doubleValue();
                                promocionTxtVw.setText(decimalFormat.format(d));
                                double total = total1 - d;
                                totalaCancelarTxtVw.setText(decimalFormat1.format(total));
                                GlobalUsuario.descuento = d;
                            } catch (Exception e) {
                                System.out.println("error codigo promo: " + e);
                            }
                        }
                    }
                }
                if (!encontrado) {
                    Toast.makeText(viewGlobal.getContext(), "Codigo no Valido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(viewGlobal.getContext(), "No hay Promos", Toast.LENGTH_SHORT).show();
            }
            btnCodigo.setEnabled(true);
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
            NotificationManager notificationManager = (NotificationManager) viewGlobal.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(viewGlobal.getContext(), "NOTIFICACION");
        builder.setSmallIcon(R.drawable.ic_app_logo);
        builder.setContentTitle("Fuimonos Restaurante");
        builder.setContentText("tienes un pedido pendiente");
        builder.setColor(Color.BLUE);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setLights(Color.MAGENTA, 1000, 1000);
        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(viewGlobal.getContext());
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
            ActivityManager activityManager = (ActivityManager) viewGlobal.getContext().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            if (memoryInfo.lowMemory) {
                Toast.makeText(viewGlobal.getContext(), "Memoria Baja", Toast.LENGTH_SHORT).show();
                Toast.makeText(viewGlobal.getContext(), "limpiando..", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(viewGlobal.getContext(), MenuBotton.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception ignore) {
        }
    }
}