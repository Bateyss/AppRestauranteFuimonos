package com.fm.modules.app.menu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.adapters.RecyclerPedidosRestauranteAdapter;
import com.fm.modules.app.login.Logued;
import com.fm.modules.entities.RespuestaPedidosDriver;
import com.fm.modules.models.Restaurante;
import com.fm.modules.service.PedidoService;

import java.util.ArrayList;
import java.util.List;

public class OrdenesAnteriores extends Fragment {


    private RecyclerView rvOrdenesActuales;
    private Button btnOrdenesAnteriores, btnRealizarOrden;

    private PedidosRestaurante pedidosRestaurante = new PedidosRestaurante();

    private View viewGlobal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ordenes_anteriores, container, false);
        viewGlobal = view;

        rvOrdenesActuales = view.findViewById(R.id.rvOrdenesActuales);
        btnOrdenesAnteriores = view.findViewById(R.id.btnOrdenesAnteriores);
        //btnRealizarOrden = view.findViewById(R.id.btnRealizarOrden);
        pedidosRestaurante.execute();
        btnListeners();
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

    private void reiniciarAsynkProcess() {
        pedidosRestaurante.cancel(true);
        pedidosRestaurante = new PedidosRestaurante();
    }

    public class PedidosRestaurante extends AsyncTask<String, String, List<RespuestaPedidosDriver>> {

        @Override
        protected List<RespuestaPedidosDriver> doInBackground(String... strings) {
            List<RespuestaPedidosDriver> pedidos = new ArrayList<>();
            Restaurante restaurante = Logued.restauranteLogued;
            try {
                PedidoService pedidoService = new PedidoService();
                pedidos = pedidoService.historialRestaurante(String.valueOf(restaurante.getRestauranteId()));

            } catch (Exception e) {
                System.out.println("Error en UnderThreash:" + e.getMessage() + " " + e.getClass());
            }
            return pedidos;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<RespuestaPedidosDriver> pedidos) {
            super.onPostExecute(pedidos);
            try {
                if (!pedidos.isEmpty()) {
                    RecyclerPedidosRestauranteAdapter adapter = new RecyclerPedidosRestauranteAdapter(viewGlobal.getContext(), pedidos, getActivity());
                    rvOrdenesActuales.setLayoutManager(new LinearLayoutManager(getContext()));
                    rvOrdenesActuales.setAdapter(adapter);
                    reiniciarAsynkProcess();
                } else {
                    reiniciarAsynkProcess();
                    RecyclerPedidosRestauranteAdapter adapter = new RecyclerPedidosRestauranteAdapter(viewGlobal.getContext(), pedidos, getActivity());
                    rvOrdenesActuales.setLayoutManager(new LinearLayoutManager(getContext()));
                    rvOrdenesActuales.setAdapter(adapter);
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

    public void btnListeners() {
        btnOrdenesAnteriores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment(new OrdenesActuales());
            }
        });

    }


    private void showFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}
