package com.fm.modules.app.pedidos;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.adapters.ViewPagerAdapter;
import com.fm.modules.app.login.Logued;
import com.fm.modules.app.menu.MenuBotton;
import com.fm.modules.app.menu.OrdenesActuales;
import com.fm.modules.app.perfil.PerfilDriver;
import com.fm.modules.models.Restaurante;
import com.fm.modules.service.Constantes;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class Principal extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager vPager;
    TabItem tabPedidos, tabPedidoActual;

    ViewPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_principal);

        tabLayout = findViewById(R.id.tabLayout);
        vPager = findViewById(R.id.vPager);
        tabPedidos = findViewById(R.id.tabPedidos);
        tabPedidoActual = findViewById(R.id.tabPedidoActual);

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabLayout.getTabCount());
        vPager.setAdapter(pagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        vPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_navigator_menu, menu);
        return true;
    }


}
