package com.fm.modules.app.menu;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.adapters.RecyclerTabMenuAdapter;
import com.fm.modules.app.carrito.GlobalRestaurantes;
import com.fm.modules.app.commons.utils.Generics;
import com.fm.modules.app.commons.utils.Utilities;
import com.fm.modules.app.login.Logued;
import com.fm.modules.models.Image;
import com.fm.modules.models.Menu;
import com.fm.modules.models.OpcionesDeSubMenu;
import com.fm.modules.models.Platillo;
import com.fm.modules.models.Restaurante;
import com.fm.modules.models.SubMenu;
import com.fm.modules.service.ImageService;
import com.fm.modules.service.OpcionesDeSubMenuService;
import com.fm.modules.service.PlatilloService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MenuDeRestauranteFragment2 extends Fragment {

    private FragmentContainerView framgent;
    private CategoraisDeRestaurante categoraisDeRestaurante = new CategoraisDeRestaurante();
    private RecyclerView tabsRV;
    private View viewGlobal;
    private AppCompatImageView imagenLogo;
    private AppCompatImageView ivBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_coordinator, container, false);
        viewGlobal = view;
        framgent = (FragmentContainerView) view.findViewById(R.id.tabCoodFragment);
        tabsRV = (RecyclerView) view.findViewById(R.id.tabCoodtabsRV);
        imagenLogo = (AppCompatImageView) view.findViewById(R.id.ivRestaurantLogoPlatillo);
        ivBack = (AppCompatImageView) view.findViewById(R.id.ivBack);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment(new OrdenesActuales());
            }
        });

        showFragmentInTabs(new PlaceholderFragment());
        if (isNetActive()) {
            cargarDatos();
        }
        onBack();
        return view;
    }

    public void onBack() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                showFragment(new OrdenesActuales());
            }
        };
        getActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
    }

    public boolean isNetActive() {
        boolean c = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private void cargarDatos() {
        final Date anteriorDate = GlobalRestaurantes.horaActualizado;
        Date actualDate = Generics.getHour(new Date());
        if (anteriorDate == null) {
            categoraisDeRestaurante.execute();
        } else {
            if (anteriorDate.getTime() < actualDate.getTime()) {
                GlobalRestaurantes.horaActualizado = actualDate;
                categoraisDeRestaurante.execute();
            } else {
                List<Menu> menus = GlobalRestaurantes.menuList;
                Restaurante restaurante = Logued.restauranteLogued;
                int idRestaurante = 0;
                if (restaurante != null) {
                    idRestaurante = restaurante.getRestauranteId().intValue();
                }
                if (menus != null && !menus.isEmpty()) {
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
            }
        }
    }

    private void verElementos(List<Menu> menus) {
        System.out.println("********* colocando elementos " + menus.size());
        RecyclerTabMenuAdapter recyclerTabMenuAdapter = new RecyclerTabMenuAdapter(menus, viewGlobal.getContext(), getActivity());
        tabsRV.setLayoutManager(new LinearLayoutManager(viewGlobal.getContext(), LinearLayoutManager.HORIZONTAL, false));
        tabsRV.setAdapter(recyclerTabMenuAdapter);
    }

    private class CategoraisDeRestaurante extends AsyncTask<String, String, List<Menu>> {


        @Override
        protected List<Menu> doInBackground(String... strings) {
            List<Menu> subM = new ArrayList<>();
            try {
                OpcionesDeSubMenuService opcionesDeSubMenuService = new OpcionesDeSubMenuService();
                List<OpcionesDeSubMenu> opciones = opcionesDeSubMenuService.obtenerOpcionesDeSubMenu();
                if (!opciones.isEmpty()) {
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

    public void reiniciarAsync() {
        categoraisDeRestaurante.cancel(true);
        categoraisDeRestaurante = new CategoraisDeRestaurante();
    }

    @Override
    public void onStop() {
        super.onStop();
        reiniciarAsync();
    }

    private void showFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private void showFragmentInTabs(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.tabCoodFragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        verLogo();
    }

    public void verLogo() {
        CargarLogo cargarLogo = new CargarLogo();
        cargarLogo.execute();
    }


    private class CargarLogo extends AsyncTask<Long, String, byte[]> {

        @Override
        protected byte[] doInBackground(Long... longs) {

            byte[] imagen = null;
            try {
                Long id = null;
                if (Logued.restauranteLogued != null && Logued.restauranteLogued.getLogoDeRestaurante() != null) {
                    id = Logued.restauranteLogued.getLogoDeRestaurante();
                }
                if (id != null) {
                    if (Logued.imagenesIDs == null) {
                        Logued.imagenesIDs = new ArrayList<>();
                        Logued.imagenes = new ArrayList<>();
                    }
                    boolean found = false;
                    if (Logued.imagenesIDs.contains(id.intValue())) {
                        for (int i = 0; i < Logued.imagenesIDs.size(); i++) {
                            if (Logued.imagenesIDs.get(i) == id.intValue()) {
                                try {
                                    imagen = Logued.imagenes.get(i).getContent();
                                    found = true;
                                } catch (Exception ignore) {
                                }
                            }
                        }
                    }
                    if (!found) {
                        ImageService imageService = new ImageService();
                        Image image = new Image();
                        image = imageService.obtenerImagenPorId(id);
                        if (image != null) {
                            imagen = image.getContent();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("*** error asynk imagePerfil: " + e);
            }
            return imagen;
        }

        @Override
        protected void onPostExecute(byte[] image) {
            super.onPostExecute(image);
            Utilities.displayAppCompatImageFromBytea(image, imagenLogo, viewGlobal.getContext());
        }
    }
}