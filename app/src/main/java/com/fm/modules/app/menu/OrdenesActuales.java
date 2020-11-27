package com.fm.modules.app.menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.fm.modules.app.login.Logon;
import com.fm.modules.app.login.Logued;
import com.fm.modules.app.pedidos.RestauranteMenuActivity2;
import com.fm.modules.entities.RespuestaPedidosDriver;
import com.fm.modules.models.Restaurante;
import com.fm.modules.service.PedidoService;
import com.fm.modules.service.RestauranteService;

import java.util.ArrayList;
import java.util.List;

public class OrdenesActuales extends Fragment {


    private RecyclerView rvOrdenesActuales;
    private Button btnOrdenesAnteriores, btnRealizarOrden, btnEstado, btnSession;
    private View viewGlobal;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ordenes_actuales, container, false);
        viewGlobal = view;
        sharedPreferences = view.getContext().getSharedPreferences("LogonData", Context.MODE_PRIVATE);
        rvOrdenesActuales = view.findViewById(R.id.rvOrdenesActuales);
        btnOrdenesAnteriores = view.findViewById(R.id.btnOrdenesAnteriores);
        btnRealizarOrden = view.findViewById(R.id.btnRealizarOrden);
        btnEstado = view.findViewById(R.id.btnEstado);
        btnSession = view.findViewById(R.id.btnSession);
        PedidosRestaurante pedidosRestaurante = new PedidosRestaurante();
        pedidosRestaurante.execute();
        btnListeners();
        estatusRestaurante();
        signOut();
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

    private void signOut() {
        btnSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SharedPreferences.Editor editor;
                    editor = sharedPreferences.edit();
                    editor.putString("email", "neles");
                    editor.putString("password", "neles");
                    editor.apply();
                } catch (Exception ignore) {
                }

                Logued.restauranteLogued = new Restaurante();
                Logued.restauranteLogued.setRestauranteId(0L);
                Intent intent = new Intent(viewGlobal.getContext(), Logon.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void estatusRestaurante() {
        if (Logued.restauranteLogued != null) {
            String text = "";
            try {
                if (Logued.restauranteLogued.getDisponible()) {
                    btnEstado.setBackgroundColor(getResources().getColor(R.color.disabled));
                    text = "Inhabilitar";
                    btnEstado.setText(text);
                } else {
                    btnEstado.setBackgroundColor(getResources().getColor(R.color.cian));
                    text = "Habilitar";
                    btnEstado.setText(text);
                }
            } catch (Exception ignore) {
                text = "Habilitar";
                btnEstado.setText(text);
            }
            btnEstado.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EstadoRestaurante estadoRestaurante = new EstadoRestaurante();
                    estadoRestaurante.execute();
                }
            });
        } else {
            btnEstado.setEnabled(false);
            btnEstado.setVisibility(View.INVISIBLE);
        }
    }

    public class PedidosRestaurante extends AsyncTask<String, String, List<RespuestaPedidosDriver>> {

        @Override
        protected List<RespuestaPedidosDriver> doInBackground(String... strings) {
            List<RespuestaPedidosDriver> pedidos = new ArrayList<>();
            Restaurante restaurante = Logued.restauranteLogued;
            try {
                PedidoService pedidoService = new PedidoService();
                pedidos = pedidoService.obtenerPedidosRestaurante(String.valueOf(restaurante.getRestauranteId()));
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
                } else {
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
                showFragment(new OrdenesAnteriores());
            }
        });
        btnRealizarOrden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showFragment(new MenuDeRestauranteFragment());
                Intent intent = new Intent(viewGlobal.getContext(), RestauranteMenuActivity2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        if (Logued.restauranteLogued != null) {
            btnEstado.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EstadoRestaurante estadoRestaurante = new EstadoRestaurante();
                    estadoRestaurante.execute();
                }
            });
        } else {
            btnEstado.setEnabled(false);
            btnEstado.setVisibility(View.INVISIBLE);
        }
    }


    private void showFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment, "ACTUAL")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    public class EstadoRestaurante extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean v = false;
            Restaurante restaurante = Logued.restauranteLogued;
            try {
                if (restaurante != null) {
                    try {
                        if (restaurante.getDisponible()) {
                            restaurante.setDisponible(false);
                        } else {
                            restaurante.setDisponible(true);
                        }
                    } catch (Exception ignore) {
                        restaurante.setDisponible(false);
                    }
                    RestauranteService restauranteService = new RestauranteService();
                    Restaurante restauranteActualizado = restauranteService.actualizarRestaurantePorId(restaurante);
                    if (restauranteActualizado != null) {
                        Logued.restauranteLogued = restauranteActualizado;
                        v = true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error en UnderThreash:" + e.getMessage() + " " + e.getClass());
            }
            return v;
        }

        @Override
        protected void onPostExecute(Boolean res) {
            super.onPostExecute(res);
            try {
                if (res) {
                    showFragment(new OrdenesActuales());
                }
            } catch (Throwable throwable) {
                System.out.println("Error Activity: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }
    }

}
