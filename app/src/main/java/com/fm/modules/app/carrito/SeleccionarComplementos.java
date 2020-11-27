package com.fm.modules.app.carrito;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fm.apprestaurantefuimonos.R;
import com.fm.modules.adapters.RecyclerSubMenuAdapter2;
import com.fm.modules.app.commons.utils.Utilities;
import com.fm.modules.app.login.Logued;
import com.fm.modules.app.pedidos.RestauranteMenuActivity2;
import com.fm.modules.models.Driver;
import com.fm.modules.models.Image;
import com.fm.modules.models.OpcionesDeSubMenu;
import com.fm.modules.models.OpcionesDeSubMenuSeleccionado;
import com.fm.modules.models.Pedido;
import com.fm.modules.models.Platillo;
import com.fm.modules.models.PlatilloSeleccionado;
import com.fm.modules.models.SubMenu;
import com.fm.modules.models.Usuario;
import com.fm.modules.service.ImageService;
import com.google.android.material.button.MaterialButton;
import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.Interface.ValueChangedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SeleccionarComplementos extends Fragment {

    private RecyclerView rvComplementsArea;
    private NumberPicker numberPicker;
    private AppCompatTextView tvFoodName, tvFoodPrice;
    private AppCompatImageView appCompatImageView, ivBack;
    private MaterialButton btnAddCarrito;
    private Platillo platilloActual;
    private CargarImagen cargarImagen = new CargarImagen();
    private View viewGlobal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frg_food_complements, container, false);
        viewGlobal = view;
        rvComplementsArea = (RecyclerView) view.findViewById(R.id.rvComplementsArea);
        numberPicker = (NumberPicker) view.findViewById(R.id.npFoodQuantity);
        tvFoodName = (AppCompatTextView) view.findViewById(R.id.tvFoodName);
        tvFoodPrice = (AppCompatTextView) view.findViewById(R.id.tvFoodPrice);
        appCompatImageView = (AppCompatImageView) view.findViewById(R.id.ivFoodImageAdicionales);
        btnAddCarrito = (MaterialButton) view.findViewById(R.id.btnAddToShoppingCart);
        ivBack = view.findViewById(R.id.ivBack);
        GlobalRestaurantes.opcionesDeSubMenusSeleccionados = new ArrayList<>();
        GlobalCarrito.habilitarAdd = new ArrayList<>();
        mostrarPlatillo();
        Platillo p = GlobalRestaurantes.platilloSeleccionado;
        int idPlatillo = 0;
        if (p != null) {
            idPlatillo = p.getPlatilloId().intValue();
            mostrarComplementos(idPlatillo);
        }
        agregarAlCarritoListener();
        cargarImg();
        listeners();
        onBack();
        return view;
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

    @Override
    public void onResume() {
        super.onResume();
        cargarImg();
    }

    private void cargarImg() {
        Platillo platillo = GlobalRestaurantes.platilloSeleccionado;
        cargarImagen.cancel(true);
        cargarImagen = new CargarImagen();
        if (platillo != null) {
            cargarImagen.execute(platillo.getImagen());
        }
    }

    private void agregarAlCarritoListener() {
        btnAddCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                PlatilloSeleccionado plas = null;
                try {
                    plas = registrarPlatillo();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (plas != null) {
                    showFragment(new CarritoActivity());
                } else {
                    v.setEnabled(true);
                }
            }
        });
    }

    public PlatilloSeleccionado registrarPlatillo() throws ParseException {
        final int cantidad = numberPicker.getValue();
        Pedido pedido = registrarPedido();
        PlatilloSeleccionado platilloSeleccionado = new PlatilloSeleccionado();
        platilloSeleccionado.setPlatillo(platilloActual);
        platilloSeleccionado.setPedido(pedido);
        platilloSeleccionado.setNombre(platilloActual.getNombre());
        platilloSeleccionado.setCantidad(cantidad);
        platilloSeleccionado.setPrecio(platilloActual.getPrecioBase() * cantidad);
        platilloSeleccionado.setPlatilloSeleccionadoId((long) ++GlobalCarrito.NumeroPlatilloSeleccionadp);
        List<PlatilloSeleccionado> list = Logued.platillosSeleccionadosActuales;
        if (list == null) {
            list = new ArrayList<>();
        }
        List<OpcionesDeSubMenu> opcionesSeleccionadas = GlobalRestaurantes.opcionesDeSubMenusSeleccionados;
        if (opcionesSeleccionadas == null) {
            opcionesSeleccionadas = new ArrayList<>();
        }
        if (!opcionesSeleccionadas.isEmpty()) {
            platilloSeleccionado = registrarOpcionesSeleccionadas(platilloSeleccionado);
        }
        list.add(platilloSeleccionado);
        Logued.platillosSeleccionadosActuales = list;
        return platilloSeleccionado;
    }

    public PlatilloSeleccionado registrarOpcionesSeleccionadas(PlatilloSeleccionado platilloSeleccionado) {
        final List<OpcionesDeSubMenu> listOpcionesExtra = GlobalRestaurantes.opcionesDeSubMenusSeleccionados;
        List<OpcionesDeSubMenuSeleccionado> opcionesSeleccionadas = Logued.opcionesDeSubMenusEnPlatillosSeleccionados;
        if (opcionesSeleccionadas == null) {
            opcionesSeleccionadas = new ArrayList<>();
        }
        final int cantidad = numberPicker.getValue();
        double adicional = 0;
        for (OpcionesDeSubMenu opcione : listOpcionesExtra) {
            OpcionesDeSubMenuSeleccionado op = new OpcionesDeSubMenuSeleccionado();
            op.setOpcionesDeSubMenu(opcione);
            op.setPlatilloSeleccionado(platilloSeleccionado);
            op.setNombre(opcione.getNombre());
            op.setOpcionesDeSubMenuSeleccionadoId((long) ++GlobalCarrito.NumeroComplementoSeleccionado);
            opcionesSeleccionadas.add(op);
            System.out.println(" * **** ***** ***** *");
            System.out.println(" * cobrado en add platillo: * " + opcione.cobrado + " opcion: " + opcione.getNombre());
            if (opcione.cobrado) {
                adicional = adicional + (opcione.getPrecio() * cantidad);
            }
        }
        Logued.opcionesDeSubMenusEnPlatillosSeleccionados = opcionesSeleccionadas;
        double precioActual = platilloSeleccionado.getPrecio();

        platilloSeleccionado.setPrecio(precioActual + adicional);
        return platilloSeleccionado;
    }

    public Pedido registrarPedido() throws ParseException {
        Pedido pedido = Logued.pedidoActual;
        Usuario usuario = new Usuario();
        usuario.setUsuarioId(1L);
        SimpleDateFormat simpleHourFormat = new SimpleDateFormat("HH:mm:ss");
        if (pedido == null) {
            pedido = new Pedido();
            pedido.setPedidoId(platilloActual.getMenu().getRestaurante().getRestauranteId());
            pedido.setRestaurante(platilloActual.getMenu().getRestaurante());
            pedido.setUsuario(usuario);
            Driver driver = new Driver();
            driver.setDriverId(0L);
            pedido.setDrivers(driver);
            pedido.setStatus(1);
            pedido.setFormaDePago("Efectivo");
            pedido.setTotalDePedido(0);
            pedido.setTotalEnRestautante(0);
            pedido.setTotalDeCargosExtra(0);
            pedido.setTotalEnRestautanteSinComision(0);
            pedido.setPedidoPagado(false);
            pedido.setFechaOrdenado((new Date()).toString());
            pedido.setTiempoPromedioEntrega(simpleHourFormat.parse(platilloActual.getMenu().getRestaurante().getTiempoEstimadoDeEntrega()));
            pedido.setPedidoEntregado(false);
            pedido.setNotas("no confirmado");
            pedido.setTiempoAdicional(simpleHourFormat.parse("00:00:00"));
            pedido.setDireccion("no direction");
            Logued.pedidoActual = pedido;
        }
        return pedido;
    }

    public Date getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR);
        calendar.set(year, month, day, hour, 0, 0);
        return calendar.getTime();
    }

    private void mostrarPlatillo() {
        final DecimalFormat decimalFormat = new DecimalFormat("$ #,##0.00");
        Platillo platillo = GlobalRestaurantes.platilloSeleccionado;
        if (platillo != null) {
            platilloActual = platillo;
            final Platillo finalPlatillo = platillo;
            tvFoodName.setText(finalPlatillo.getNombre());
            tvFoodPrice.setText(decimalFormat.format(finalPlatillo.getPrecioBase()));
            numberPicker.setValueChangedListener(new ValueChangedListener() {
                @Override
                public void valueChanged(int value, ActionEnum action) {
                    double total;
                    total = Double.parseDouble(String.valueOf(finalPlatillo.getPrecioBase() * value));
                    tvFoodPrice.setText(decimalFormat.format(total));
                }
            });
        }
    }


    private void mostrarComplementos(int idPlatillo) {
        if (!GlobalRestaurantes.opcionesDeSubMenuList.isEmpty()) {
            List<OpcionesDeSubMenu> opcionesDeSubMenus = new ArrayList<>();
            List<SubMenu> subMenus = new ArrayList<>();
            for (OpcionesDeSubMenu op : GlobalRestaurantes.opcionesDeSubMenuList) {
                if (op.getSubMenu().getPlatillo().getPlatilloId().intValue() == idPlatillo) {
                    opcionesDeSubMenus.add(op);
                }
            }
            List<Integer> integers = new ArrayList<>();
            for (OpcionesDeSubMenu op : opcionesDeSubMenus) {
                if (!integers.contains(op.getSubMenu().getSubMenuId().intValue())) {
                    subMenus.add(op.getSubMenu());
                    integers.add(op.getSubMenu().getSubMenuId().intValue());
                }
            }
            if (!opcionesDeSubMenus.isEmpty()) {
                GlobalRestaurantes.opcionesDeSubMenusSeleccionados = new ArrayList<>();
            }
            RecyclerSubMenuAdapter2 recyclerSubMenuAdapter = new RecyclerSubMenuAdapter2(subMenus, opcionesDeSubMenus, viewGlobal.getContext(), btnAddCarrito);
            rvComplementsArea.setLayoutManager(new LinearLayoutManager(viewGlobal.getContext(), LinearLayoutManager.VERTICAL, false));
            rvComplementsArea.setAdapter(recyclerSubMenuAdapter);
        }
    }

    private class CargarImagen extends AsyncTask<Long, String, Image> {

        @Override
        protected Image doInBackground(Long... longs) {
            Image image = null;
            try {
                if (Logued.imagenesIDs == null) {
                    Logued.imagenes = new ArrayList<>();
                    Logued.imagenesIDs = new ArrayList<>();
                }
                List<Integer> integers = Logued.imagenesIDs;
                if (!integers.contains(longs[0].intValue())) {
                    ImageService imageService = new ImageService();
                    image = imageService.obtenerImagenPorId(longs[0]);
                    if (image != null) {
                        Logued.imagenesIDs.add(image.getId().intValue());
                        Logued.imagenes.add(image);
                    }
                } else {
                    for (int i = 0; i < integers.size(); i++) {
                        if (integers.get(i) == longs[0].intValue()) {
                            image = Logued.imagenes.get(i);
                        }
                    }
                }
            } catch (
                    Exception e) {
                System.out.println("error asynk image: " + e);
            }
            return image;
        }

        @Override
        protected void onPostExecute(Image image) {
            super.onPostExecute(image);
            if (image != null) {
                Utilities.displayAppCompatImageFoodFromBytea(image.getContent(), appCompatImageView, viewGlobal.getContext());
                System.out.println("asynk display image ! !!!!!!!!!!!!!!!!");
            }
        }
    }

    private void showFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private void listeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showFragment(new MenuDeRestauranteFragment());
                Intent intent = new Intent(viewGlobal.getContext(), RestauranteMenuActivity2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}