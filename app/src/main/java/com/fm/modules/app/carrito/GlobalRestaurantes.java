package com.fm.modules.app.carrito;

import com.fm.modules.models.Menu;
import com.fm.modules.models.OpcionesDeSubMenu;
import com.fm.modules.models.OpcionesDeSubMenuSeleccionado;
import com.fm.modules.models.Platillo;
import com.fm.modules.models.Restaurante;
import com.fm.modules.models.SubMenu;

import java.util.Date;
import java.util.List;

public class GlobalRestaurantes {
    public static Menu menuSeleccionado;
    public static List<Menu> menuList;
    public static List<Platillo> platilloList;
    public static List<SubMenu> subMenuList;
    public static List<OpcionesDeSubMenu> opcionesDeSubMenuList;
    public static Platillo platillo;
    public static Platillo platilloSeleccionado;
    public static SubMenu subMenu;
    public static List<OpcionesDeSubMenu> opcionesDeSubMenusSeleccionados;
    public static Date horaActualizado;
    public static List<Restaurante> restaurantes;
    public static Restaurante restauranteSelected;
    public static Integer menuTabPosition;
    public static Menu menuTabSelected;
    public static Integer pedidoss;
    public static Integer pedidoTomadoId;
    public static List<OpcionesDeSubMenuSeleccionado> opcionesDeSubMenuSeleccionadosPlatillo;
}
