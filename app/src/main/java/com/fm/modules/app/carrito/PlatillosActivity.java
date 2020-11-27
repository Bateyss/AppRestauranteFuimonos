package com.fm.modules.app.carrito;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.adapters.RecyclerPlatillosAdapter;
import com.fm.modules.adapters.RecyclerPlatillosPorRestaurante;
import com.fm.modules.app.commons.utils.Utilities;
import com.fm.modules.app.login.Logued;
import com.fm.modules.models.Image;
import com.fm.modules.models.Menu;
import com.fm.modules.models.Platillo;
import com.fm.modules.models.Restaurante;

import java.util.ArrayList;
import java.util.List;

public class PlatillosActivity extends Fragment {
    private RecyclerView rvPlatillos;
    private View viewGlobal;
    private Menu menu;

    public PlatillosActivity(Menu menu) {
        this.menu = menu;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frg_platillos, container, false);
        viewGlobal = view;
        rvPlatillos = (RecyclerView) view.findViewById(R.id.rvPlatillos);
        verPlatillos();
        return view;
    }

    public void verPlatillos() {
        Menu m = menu;
        int idMenu = 0;
        if (m != null) {
            idMenu = m.getMenuId().intValue();
        }
        if (idMenu != 0) {
            List<Platillo> platilloList = new ArrayList<>();
            List<Integer> ints = new ArrayList<>();
            for (Platillo p : GlobalRestaurantes.platilloList) {
                if (p.getMenu().getMenuId().intValue() == idMenu) {
                    if (!ints.contains(p.getPlatilloId().intValue())) {
                        platilloList.add(p);
                        ints.add(p.getPlatilloId().intValue());
                    }
                }
            }

            RecyclerPlatillosPorRestaurante recyclerPlatillosAdapter = new RecyclerPlatillosPorRestaurante(viewGlobal.getContext(),platilloList, getActivity());
            rvPlatillos.setLayoutManager(new LinearLayoutManager(viewGlobal.getContext(), LinearLayoutManager.VERTICAL, false));
            rvPlatillos.setAdapter(recyclerPlatillosAdapter);
        }
        if (GlobalRestaurantes.menuTabSelected != null) {
            final Menu mm = GlobalRestaurantes.menuTabSelected;
            GlobalRestaurantes.menuTabSelected = null;
            showFragment2(new PlatillosActivity(mm));
        }
    }

    /*public void listeners(){
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment(new RestauranteMenuActivity());
            }
        });
    }*/

    private void showFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
    private void showFragment2(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.tabCoodFragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

}
