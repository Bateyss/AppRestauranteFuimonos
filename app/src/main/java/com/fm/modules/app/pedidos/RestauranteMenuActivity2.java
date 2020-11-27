package com.fm.modules.app.pedidos;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.adapters.CustomTabAdapter;
import com.fm.modules.app.carrito.GlobalCarrito;
import com.fm.modules.app.carrito.GlobalRestaurantes;
import com.fm.modules.app.carrito.PlatillosActivity3;
import com.fm.modules.app.commons.utils.Utilities;
import com.fm.modules.app.login.Logued;
import com.fm.modules.app.menu.MenuBotton;
import com.fm.modules.models.Image;
import com.fm.modules.models.Menu;
import com.fm.modules.models.OpcionesDeSubMenu;
import com.fm.modules.models.Platillo;
import com.fm.modules.models.Restaurante;
import com.fm.modules.models.SubMenu;
import com.fm.modules.service.OpcionesDeSubMenuService;
import com.fm.modules.service.PlatilloService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class RestauranteMenuActivity2 extends AppCompatActivity {

    private TabLayout menuTab;
    private ViewPager viewPager;
    private AppCompatImageView imagenLogo;
    private BottomNavigationView bottomNavigationView;
    private AppCompatImageView back;
    private AppCompatImageView fotoPerfil;
    private AppCompatTextView restauranteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frg_restaurant_menu2);
        imagenLogo = findViewById(R.id.ivRestaurantLogoMenu);
        bottomNavigationView = findViewById(R.id.nav_view_for_menus_restaurantes);
        back = findViewById(R.id.ivBack);
        restauranteName = findViewById(R.id.tvRestaurantName);
        viewPager = findViewById(R.id.lvMenus1);
        menuTab = findViewById(R.id.menuTab);
        menuTab.setTabTextColors(R.color.purpleapaquetext, R.color.purple);
        backListener();
        if (isNetActive()) {
            cargarDatos();
        }
        navBottonControl();
    }

    private void navBottonControl() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.mmenuInicio) {
                    Intent intent = new Intent(RestauranteMenuActivity2.this, MenuBotton.class);
                    startActivity(intent);
                }
                if (item.getItemId() == R.id.mmenuCarrito) {
                    GlobalCarrito.toShopinCart = true;
                    Intent intent = new Intent(RestauranteMenuActivity2.this, MenuBotton.class);
                    startActivity(intent);
                }
                return false;
            }
        });
    }

    private void verElementos(List<Menu> menus) {
        try {
            final List<Fragment> tabs = new ArrayList<>();
            final List<String> tabsTitles = new ArrayList<>();
            if (!menus.isEmpty()) {
                for (Menu m : menus) {
                    tabs.add(new PlatillosActivity3(m));
                    tabsTitles.add(m.getNombreMenu());
                }
                if (!tabs.isEmpty()) {
                    CustomTabAdapter customTabAdapter = new CustomTabAdapter(getSupportFragmentManager(), tabs, tabsTitles);
                    viewPager.setAdapter(customTabAdapter);
                    menuTab.setupWithViewPager(viewPager, false);
                    //menuTab.setTabTextColors(R.color.purpleapaquetext, R.color.purple);
                    int tabCount = menuTab.getTabCount();
                    for (int i = 0; i < tabCount; i++) {
                        TabLayout.Tab tab = menuTab.getTabAt(i);
                        View tabView = ((ViewGroup) menuTab.getChildAt(0)).getChildAt(i);
                        tabView.requestLayout();
                        View viewTab = LayoutInflater.from(this).inflate(R.layout.holder_item_text_only, null);
                        try {
                            tab.setCustomView(viewTab);
                        } catch (Exception ignore) {
                        }
                        AppCompatTextView txt = viewTab.findViewById(R.id.tvOptionName);
                        tab.setText(tabsTitles.get(i));
                        txt.setText(tab.getText());
                        if (menuTab.getSelectedTabPosition() == i) {
                            txt.setTextColor(getResources().getColor(R.color.purple));
                        } else {
                            txt.setTextColor(getResources().getColor(R.color.purpleapaquetext));
                        }
                    }
                    menuTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tabx) {
                            try {
                                AppCompatTextView txt = tabx.getCustomView().findViewById(R.id.tvOptionName);
                                txt.setText(tabx.getText());
                                txt.setTextColor(getResources().getColor(R.color.purple));
                            } catch (Exception ignore) {
                            }
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tabx) {
                            try {
                                AppCompatTextView txt = tabx.getCustomView().findViewById(R.id.tvOptionName);
                                txt.setText(tabx.getText());
                                txt.setTextColor(getResources().getColor(R.color.purpleapaquetext));
                            } catch (Exception ignore) {
                            }
                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("error al cargar elementos: " + e);
        }
    }

    private void cargarDatos() {
        Restaurante restaurante = Logued.restauranteLogued;
        if (restaurante != null) {
            restauranteName.setText(restaurante.getNombreRestaurante());
        }
        CategoraisDeRestaurante categoraisDeRestaurante = new CategoraisDeRestaurante();
        categoraisDeRestaurante.execute();
        verLogo();
    }


    private void backListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RestauranteMenuActivity2.this, MenuBotton.class);
                startActivity(intent);
            }
        });
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

    public void verLogo() {
        Image image = null;
        List<Integer> integers = Logued.imagenesIDs;
        Restaurante res = Logued.restauranteLogued;
        if (res != null) {
            if (integers != null && !integers.isEmpty()) {
                for (int i = 0; i < integers.size(); i++) {
                    if (res.getLogoDeRestaurante().intValue() == integers.get(i)) {
                        image = Logued.imagenes.get(i);
                    }
                }
            }
        }
        if (image != null) {
            Utilities.displayAppCompatImageFromBytea(image.getContent(), imagenLogo, RestauranteMenuActivity2.this);
        } else {
            Utilities.displayAppCompatImageFromBytea(null, imagenLogo, RestauranteMenuActivity2.this);
        }
    }

    private class CategoraisDeRestaurante extends AsyncTask<String, String, List<Menu>> {


        @Override
        protected List<Menu> doInBackground(String... strings) {
            List<Menu> subM = new ArrayList<>();
            try {
                OpcionesDeSubMenuService opcionesDeSubMenuService = new OpcionesDeSubMenuService();
                List<OpcionesDeSubMenu> opciones = opcionesDeSubMenuService.obtenerOpcionesDeSubMenu();
                if (!opciones.isEmpty()) {
                    System.out.println("********* opciones cargadas ***************");
                    List<SubMenu> subMenus = new ArrayList<>();
                    List<Integer> ints = new ArrayList<>();
                    for (OpcionesDeSubMenu op : opciones) {
                        try {
                            if (!ints.contains(op.getSubMenu().getSubMenuId().intValue())) {
                                subMenus.add(op.getSubMenu());
                                ints.add(op.getSubMenu().getSubMenuId().intValue());
                            }
                        } catch (Exception ignore) {
                        }
                    }
                    GlobalRestaurantes.opcionesDeSubMenuList = opciones;
                    if (!subMenus.isEmpty()) {
                        GlobalRestaurantes.subMenuList = subMenus;
                    }
                } else {
                    GlobalRestaurantes.opcionesDeSubMenuList = new ArrayList<>();
                    GlobalRestaurantes.subMenuList = new ArrayList<>();
                }
                PlatilloService platilloService = new PlatilloService();
                List<Platillo> platillos = platilloService.obtenerPlatillos();
                if (!platillos.isEmpty()) {
                    GlobalRestaurantes.platilloList = platillos;
                    List<Menu> menus = new ArrayList<>();
                    List<Integer> ints = new ArrayList<>();
                    for (Platillo pa : platillos) {
                        try {
                            if (!ints.contains(pa.getMenu().getMenuId().intValue())) {
                                menus.add(pa.getMenu());
                                ints.add(pa.getMenu().getMenuId().intValue());
                            }
                        } catch (Exception ignore) {
                        }
                    }
                    if (!menus.isEmpty()) {
                        GlobalRestaurantes.menuList = menus;
                        subM = menus;
                    }
                } else {
                    GlobalRestaurantes.platilloList = new ArrayList<>();
                    GlobalRestaurantes.menuList = new ArrayList<>();
                }

            } catch (Exception e) {
                System.out.println("Error en UnderThreash:" + e.getMessage() + " " + e.getClass());
            }
            return subM;
        }

        @Override
        protected void onPostExecute(List<Menu> menus) {
            super.onPostExecute(menus);
            Restaurante restaurante = Logued.restauranteLogued;
            int idRestaurante = 0;
            if (restaurante != null) {
                idRestaurante = restaurante.getRestauranteId().intValue();
            }
            System.out.println("a cargar elementos");
            try {
                if (!menus.isEmpty()) {
                    List<Menu> menuList = new ArrayList<>();
                    for (Menu m : menus) {
                        if (m.getRestaurante().getRestauranteId().intValue() == idRestaurante) {
                            if (!menuList.contains(m)) {
                                menuList.add(m);
                            }
                        }
                    }
                    verElementos(menuList);
                }
            } catch (Exception e) {
                System.out.println("Error CategoraisDeRestaurante: " + e);
            }
        }
    }
}
