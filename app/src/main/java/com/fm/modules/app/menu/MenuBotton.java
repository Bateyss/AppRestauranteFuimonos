package com.fm.modules.app.menu;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.app.carrito.CarritoActivity;
import com.fm.modules.app.carrito.GlobalCarrito;
import com.fm.modules.app.carrito.GlobalRestaurantes;
import com.fm.modules.app.carrito.PagarTargetaNuevaFragment;
import com.fm.modules.app.carrito.SeleccionarComplementos;
import com.fm.modules.app.login.Logued;
import com.fm.modules.service.Constantes;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MenuBotton extends FragmentActivity {

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_botton);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.menuHome, R.id.menuShoppingCart)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        showFragment(new OrdenesActuales());

        boolean openShopingCart = GlobalCarrito.toShopinCart;
        if (openShopingCart) {
            showFragment(new CarritoActivity());
            GlobalCarrito.toShopinCart = false;
        }
        if (GlobalCarrito.toComplementos) {
            showFragment(new SeleccionarComplementos());
            GlobalCarrito.toComplementos = false;
        }

        if (GlobalCarrito.toPagoTarjeta) {
            showFragment(new PagarTargetaNuevaFragment());
            GlobalCarrito.toPagoTarjeta = false;
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.mmenuInicio) {
                    showFragment(new OrdenesActuales());
                }
                if (item.getItemId() == R.id.mmenuCarrito) {
                    showFragment(new CarritoActivity());
                    System.out.println("Al Carrito");
                }
                return false;
            }
        });

        queue = Volley.newRequestQueue(MenuBotton.this);
        notifications();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment, "ACTUAL")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void notifications() {
        if (Logued.restauranteLogued != null) {
            CountDownTimer mCountDownTimer = new CountDownTimer(6000, 1000) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    volleyMethod(Logued.restauranteLogued.getRestauranteId());
                }
            }.start();
        }
    }

    public void makeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification";
            NotificationChannel channel = new NotificationChannel("NOTIFICACION", name, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MenuBotton.this, "NOTIFICACION");
        builder.setSmallIcon(R.drawable.ic_app_logo);
        builder.setContentTitle("Fuimonos Restaurante");
        builder.setContentText("tienes un pedido pendiente");
        builder.setColor(Color.BLUE);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setLights(Color.MAGENTA, 1000, 1000);
        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MenuBotton.this);
        notificationManagerCompat.notify(0, builder.build());
        try {
            showFragment(new OrdenesActuales());
        } catch (Exception ignore) {
        }
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
        notifications();
    }

    private void getAvailableMemory() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            if (memoryInfo.lowMemory) {
                Toast.makeText(MenuBotton.this, "Memoria Baja", Toast.LENGTH_SHORT).show();
                Toast.makeText(MenuBotton.this, "limpiando..", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MenuBotton.this, MenuBotton.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception ignore) {
        }
    }

}