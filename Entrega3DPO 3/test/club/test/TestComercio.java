package club.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.modelo.comercio.Cafeteria;
import club.modelo.comercio.EmpleadoNegocio;
import club.modelo.comercio.Inventario;
import club.modelo.comercio.Producto;
import club.modelo.comercio.ProgramaFidelidad;
import club.modelo.comercio.Tienda;
import club.modelo.comercio.VentaProducto;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.JugadorEquipo;

// Pruebas del paquete comercio
public class TestComercio {

    private Producto agua;
    private Producto camiseta;
    private Inventario inventario;
    private Tienda tienda;
    private Cafeteria cafeteria;
    private JugadorEquipo comprador;

    @BeforeEach
    public void preparar() {
        agua = new Producto("Agua", "Bebidas", 2000);
        camiseta = new Producto("Camiseta", "Ropa", 50000);
        inventario = new Inventario();
        tienda = new Tienda(inventario);
        cafeteria = new Cafeteria(inventario);
        comprador = new JugadorEquipo("ana", "1", "Ana", 14);
    }

    private Map<Producto, Integer> items(Producto p, int cantidad) {
        Map<Producto, Integer> mapa = new HashMap<Producto, Integer>();
        mapa.put(p, cantidad);
        return mapa;
    }

    // ---------- Producto ----------

    @Test
    public void productoValido() {
        assertEquals("Agua", agua.getNombre());
        assertEquals("Bebidas", agua.getCategoria());
        assertEquals(2000, agua.getPrecio(), 0.001);
    }

    @Test
    public void productoConDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Producto("", "Bebidas", 100));
        assertThrows(IllegalArgumentException.class, () -> new Producto(null, "Bebidas", 100));
        assertThrows(IllegalArgumentException.class, () -> new Producto("Agua", " ", 100));
        assertThrows(IllegalArgumentException.class, () -> new Producto("Agua", null, 100));
        assertThrows(IllegalArgumentException.class, () -> new Producto("Agua", "Bebidas", -1));
    }

    @Test
    public void productoSettersInvalidosLanzanExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> agua.setNombre(""));
        assertThrows(IllegalArgumentException.class, () -> agua.setCategoria(null));
        assertThrows(IllegalArgumentException.class, () -> agua.setPrecio(-5));
    }

    @Test
    public void productoSettersValidos() {
        agua.setPrecio(2500);
        assertEquals(2500, agua.getPrecio(), 0.001);
    }

    @Test
    public void productosIgualesSinImportarMayusculas() {
        Producto otra = new Producto("AGUA", "bebidas", 1);
        assertEquals(agua, otra);
        assertEquals(agua.hashCode(), otra.hashCode());
    }

    // ---------- Inventario ----------

    @Test
    public void agregarYConsultarCantidad() {
        inventario.agregar(agua, "TIENDA", 10);
        inventario.agregar(agua, "TIENDA", 5);
        assertEquals(15, inventario.getCantidad(agua, "TIENDA"));
        assertEquals(0, inventario.getCantidad(agua, "CAFETERIA"));
        assertEquals(0, inventario.getCantidad(camiseta, "TIENDA"));
    }

    @Test
    public void agregarConDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> inventario.agregar(agua, "TIENDA", 0));
        assertThrows(IllegalArgumentException.class, () -> inventario.agregar(agua, "TIENDA", -4));
        assertThrows(IllegalArgumentException.class, () -> inventario.agregar(null, "TIENDA", 4));
        assertThrows(IllegalArgumentException.class, () -> inventario.agregar(agua, "", 4));
        assertThrows(IllegalArgumentException.class, () -> inventario.agregar(agua, null, 4));
    }

    @Test
    public void retirarProductos() {
        inventario.agregar(agua, "TIENDA", 10);
        inventario.retirar(agua, "TIENDA", 4);
        assertEquals(6, inventario.getCantidad(agua, "TIENDA"));
    }

    @Test
    public void retirarMasDeLoQueHayLanzaExcepcion() {
        inventario.agregar(agua, "TIENDA", 3);
        assertThrows(IllegalStateException.class, () -> inventario.retirar(agua, "TIENDA", 4));
        assertEquals(3, inventario.getCantidad(agua, "TIENDA"));
    }

    @Test
    public void retirarCantidadInvalidaLanzaExcepcion() {
        inventario.agregar(agua, "TIENDA", 3);
        assertThrows(IllegalArgumentException.class, () -> inventario.retirar(agua, "TIENDA", 0));
        assertThrows(IllegalArgumentException.class, () -> inventario.retirar(null, "TIENDA", 1));
    }

    @Test
    public void moverEntreUbicaciones() {
        inventario.agregar(agua, "TIENDA", 10);
        inventario.mover(agua, "TIENDA", "CAFETERIA", 4);
        assertEquals(6, inventario.getCantidad(agua, "TIENDA"));
        assertEquals(4, inventario.getCantidad(agua, "CAFETERIA"));
    }

    @Test
    public void moverMasDeLoQueHayLanzaExcepcionYNoCambiaNada() {
        inventario.agregar(agua, "TIENDA", 2);
        assertThrows(IllegalStateException.class, () -> inventario.mover(agua, "TIENDA", "CAFETERIA", 5));
        assertEquals(2, inventario.getCantidad(agua, "TIENDA"));
        assertEquals(0, inventario.getCantidad(agua, "CAFETERIA"));
    }

    @Test
    public void moverAlMismoLugarLanzaExcepcion() {
        inventario.agregar(agua, "TIENDA", 2);
        assertThrows(IllegalArgumentException.class, () -> inventario.mover(agua, "TIENDA", "TIENDA", 1));
    }

    @Test
    public void listarExistenciasSumaLasUbicaciones() {
        inventario.agregar(agua, "TIENDA", 3);
        inventario.agregar(agua, "CAFETERIA", 7);
        inventario.agregar(camiseta, "TIENDA", 2);

        Map<Producto, Integer> totales = inventario.listarExistencias();
        int totalAgua = totales.get(agua);
        int totalCamisetas = totales.get(camiseta);
        assertEquals(10, totalAgua);
        assertEquals(2, totalCamisetas);
    }

    // ---------- ProgramaFidelidad ----------

    @Test
    public void programaFidelidadAcumulaYRedime() {
        ProgramaFidelidad programa = new ProgramaFidelidad(comprador);
        programa.acumular(250);
        assertEquals(2, programa.getSaldo());

        double valor = programa.redimir(1);
        assertEquals(10.0, valor, 0.001);
        assertEquals(1, programa.getSaldo());
    }

    @Test
    public void programaFidelidadConPropietarioNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new ProgramaFidelidad(null));
    }

    @Test
    public void acumularValorNegativoLanzaExcepcion() {
        ProgramaFidelidad programa = new ProgramaFidelidad(comprador);
        assertThrows(IllegalArgumentException.class, () -> programa.acumular(-1));
    }

    @Test
    public void redimirPuntosInvalidosLanzaExcepcion() {
        ProgramaFidelidad programa = new ProgramaFidelidad(comprador);
        programa.acumular(300);
        assertThrows(IllegalArgumentException.class, () -> programa.redimir(0));
        assertThrows(IllegalStateException.class, () -> programa.redimir(4));
        assertEquals(3, programa.getSaldo());
    }

    // ---------- VentaProducto ----------

    @Test
    public void ventaCalculaSubtotalDescuentoImpuestoYTotal() {
        VentaProducto venta = new VentaProducto(LocalDate.of(2026, 12, 1), comprador, items(agua, 5), "TIENDA");
        assertEquals(10000, venta.getSubtotal(), 0.01);

        venta.aplicarDescuento(10);
        assertEquals(1000, venta.getDescuento(), 0.01);
        assertEquals(1710, venta.calcularImpuesto(), 0.01);
        assertEquals(10710, venta.calcularTotal(), 0.01);
        assertEquals(107, venta.calcularPuntos());
    }

    @Test
    public void ventaConPropinaSumaAlTotal() {
        VentaProducto venta = new VentaProducto(LocalDate.of(2026, 12, 1), comprador, items(agua, 5), "CAFETERIA");
        venta.setPropina(500);
        assertEquals(10000 + 1900 + 500, venta.getTotal(), 0.01);
    }

    @Test
    public void ventaConDatosInvalidosLanzaExcepcion() {
        LocalDate hoy = LocalDate.of(2026, 12, 1);
        Map<Producto, Integer> vacio = new HashMap<Producto, Integer>();
        assertThrows(IllegalArgumentException.class, () -> new VentaProducto(null, comprador, items(agua, 1), "TIENDA"));
        assertThrows(IllegalArgumentException.class, () -> new VentaProducto(hoy, null, items(agua, 1), "TIENDA"));
        assertThrows(IllegalArgumentException.class, () -> new VentaProducto(hoy, comprador, null, "TIENDA"));
        assertThrows(IllegalArgumentException.class, () -> new VentaProducto(hoy, comprador, vacio, "TIENDA"));
        assertThrows(IllegalArgumentException.class, () -> new VentaProducto(hoy, comprador, items(agua, 1), ""));
        assertThrows(IllegalArgumentException.class, () -> new VentaProducto(hoy, comprador, items(agua, 0), "TIENDA"));
    }

    @Test
    public void descuentoFueraDeRangoLanzaExcepcion() {
        VentaProducto venta = new VentaProducto(LocalDate.of(2026, 12, 1), comprador, items(agua, 1), "TIENDA");
        assertThrows(IllegalArgumentException.class, () -> venta.aplicarDescuento(-1));
        assertThrows(IllegalArgumentException.class, () -> venta.aplicarDescuento(101));
    }

    @Test
    public void propinaNegativaLanzaExcepcion() {
        VentaProducto venta = new VentaProducto(LocalDate.of(2026, 12, 1), comprador, items(agua, 1), "CAFETERIA");
        assertThrows(IllegalArgumentException.class, () -> venta.setPropina(-10));
    }

    // ---------- Tienda ----------

    @Test
    public void tiendaConInventarioNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Tienda(null));
    }

    @Test
    public void tiendaVendeYDescuentaDelInventario() {
        tienda.reabastecer(camiseta, 5);
        VentaProducto venta = tienda.vender(comprador, items(camiseta, 2), null);

        assertEquals(3, inventario.getCantidad(camiseta, "TIENDA"));
        assertEquals(100000, venta.getSubtotal(), 0.01);
        assertEquals(119000, venta.getTotal(), 0.01);
        assertEquals("TIENDA", venta.getRubro());
        assertEquals(1, tienda.getVentas().size());
        assertEquals(1190, tienda.getPrograma(comprador).getSaldo());
    }

    @Test
    public void tiendaAplicaCodigoDeDescuento() {
        tienda.reabastecer(camiseta, 5);
        VentaProducto venta = tienda.vender(comprador, items(camiseta, 2), "club10");
        assertEquals(10000, venta.getDescuento(), 0.01);
        assertEquals(107100, venta.getTotal(), 0.01);
    }

    @Test
    public void tiendaConCodigoDesconocidoNoAplicaDescuento() {
        tienda.reabastecer(camiseta, 5);
        VentaProducto venta = tienda.vender(comprador, items(camiseta, 1), "XYZ");
        assertEquals(0, venta.getDescuento(), 0.01);
    }

    @Test
    public void tiendaVenderSinExistenciasLanzaExcepcion() {
        tienda.reabastecer(camiseta, 1);
        assertThrows(IllegalStateException.class, () -> tienda.vender(comprador, items(camiseta, 2), null));
        assertEquals(1, inventario.getCantidad(camiseta, "TIENDA"));
        assertTrue(tienda.getVentas().isEmpty());
    }

    @Test
    public void tiendaVenderConDatosInvalidosLanzaExcepcion() {
        tienda.reabastecer(camiseta, 5);
        Map<Producto, Integer> vacio = new HashMap<Producto, Integer>();
        assertThrows(IllegalArgumentException.class, () -> tienda.vender(null, items(camiseta, 1), null));
        assertThrows(IllegalArgumentException.class, () -> tienda.vender(comprador, null, null));
        assertThrows(IllegalArgumentException.class, () -> tienda.vender(comprador, vacio, null));
        assertThrows(IllegalArgumentException.class, () -> tienda.vender(comprador, items(camiseta, 0), null));
        assertThrows(IllegalArgumentException.class, () -> tienda.vender(comprador, items(camiseta, -1), null));
    }

    @Test
    public void tiendaReabastecerConDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> tienda.reabastecer(camiseta, 0));
        assertThrows(IllegalArgumentException.class, () -> tienda.reabastecer(null, 3));
    }

    @Test
    public void tiendaBuscarProducto() {
        tienda.reabastecer(camiseta, 5);
        assertSame(camiseta, tienda.buscarProducto("camiseta"));
    }

    @Test
    public void tiendaBuscarProductoQueNoExisteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> tienda.buscarProducto("Balon"));
        assertThrows(IllegalArgumentException.class, () -> tienda.buscarProducto(""));
        assertThrows(IllegalArgumentException.class, () -> tienda.buscarProducto(null));
    }

    @Test
    public void tiendaAgregarEmpleado() {
        EmpleadoNegocio pepe = new EmpleadoNegocio("pepe", "1", "Pepe");
        tienda.agregarEmpleado(pepe);
        assertEquals(1, tienda.getEmpleados().size());
        assertThrows(IllegalStateException.class, () -> tienda.agregarEmpleado(pepe));
        assertThrows(IllegalArgumentException.class, () -> tienda.agregarEmpleado(null));
    }

    // ---------- Cafeteria ----------

    @Test
    public void cafeteriaConInventarioNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Cafeteria(null));
    }

    @Test
    public void cafeteriaVendeConPropina() {
        inventario.agregar(agua, "CAFETERIA", 10);
        VentaProducto venta = cafeteria.vender(comprador, items(agua, 2), null);

        assertEquals(8, inventario.getCantidad(agua, "CAFETERIA"));
        assertEquals(4000, venta.getSubtotal(), 0.01);
        assertEquals(400, venta.getPropina(), 0.01);
        assertEquals(5160, venta.getTotal(), 0.01);
        assertEquals("CAFETERIA", venta.getRubro());
        assertEquals(1, cafeteria.getVentas().size());
    }

    @Test
    public void cafeteriaNoUsaLasExistenciasDeLaTienda() {
        tienda.reabastecer(agua, 10); // solo hay agua en la tienda
        assertThrows(IllegalStateException.class, () -> cafeteria.vender(comprador, items(agua, 1), null));
    }

    @Test
    public void cafeteriaVenderConDatosInvalidosLanzaExcepcion() {
        inventario.agregar(agua, "CAFETERIA", 10);
        assertThrows(IllegalArgumentException.class, () -> cafeteria.vender(null, items(agua, 1), null));
        assertThrows(IllegalArgumentException.class, () -> cafeteria.vender(comprador, null, null));
        assertThrows(IllegalArgumentException.class, () -> cafeteria.vender(comprador, items(agua, 0), null));
    }

    @Test
    public void propinaSugerida() {
        assertEquals(100, cafeteria.propinaSugerida(1000), 0.01);
        assertThrows(IllegalArgumentException.class, () -> cafeteria.propinaSugerida(-1));
    }

    @Test
    public void cafeteriaAgregarEmpleado() {
        EmpleadoNegocio ana = new EmpleadoNegocio("mesero", "1", "Mesero");
        cafeteria.agregarEmpleado(ana);
        assertEquals(1, cafeteria.getEmpleados().size());
        assertThrows(IllegalStateException.class, () -> cafeteria.agregarEmpleado(ana));
        assertThrows(IllegalArgumentException.class, () -> cafeteria.agregarEmpleado(null));
    }

    // ---------- Puntos de fidelidad y descuento de empleado ----------

    @Test
    public void laVentaSumaPuntosAlProgramaDelJugador() {
        tienda.reabastecer(camiseta, 5);
        tienda.vender(comprador, items(camiseta, 2), null);
        // 119000 / 100 = 1190 puntos, y quedan en el programa del propio jugador
        assertEquals(1190, comprador.getProgramaFidelidad().getSaldo());
        assertSame(comprador.getProgramaFidelidad(), tienda.getPrograma(comprador));
    }

    @Test
    public void tiendaYCafeteriaSumanPuntosAlMismoPrograma() {
        tienda.reabastecer(camiseta, 5);
        inventario.agregar(agua, "CAFETERIA", 5);
        tienda.vender(comprador, items(camiseta, 2), null);
        cafeteria.vender(comprador, items(agua, 1), null);
        // agua: 2000 + 380 de IVA + 200 de propina = 2580 -> 25 puntos
        assertEquals(1190 + 25, comprador.getProgramaFidelidad().getSaldo());
    }

    @Test
    public void empleadoPuedeUsarSuPropioCodigoDeDescuento() {
        EmpleadoNegocio emp = new EmpleadoNegocio("pepe", "1", "Pepe");
        tienda.reabastecer(camiseta, 5);
        VentaProducto venta = tienda.vender(emp, items(camiseta, 1), emp.getCodigoDescuento());
        assertEquals(10000, venta.getDescuento(), 0.01);
        assertEquals(47600, venta.getTotal(), 0.01);
        assertEquals(476, emp.getProgramaFidelidad().getSaldo());
    }

    @Test
    public void codigoDeDescuentoDeOtroEmpleadoNoSirve() {
        EmpleadoNegocio emp = new EmpleadoNegocio("pepe", "1", "Pepe");
        tienda.reabastecer(camiseta, 5);
        // "comprador" es un jugador: no puede usar el codigo de un empleado
        VentaProducto venta = tienda.vender(comprador, items(camiseta, 1), emp.getCodigoDescuento());
        assertEquals(0, venta.getDescuento(), 0.01);
    }

    @Test
    public void compradorSinProgramaDeFidelidadPuedeComprar() {
        Administrador admin = new Administrador("admin", "1", "Admin");
        tienda.reabastecer(camiseta, 5);
        VentaProducto venta = tienda.vender(admin, items(camiseta, 1), null);
        assertEquals(59500, venta.getTotal(), 0.01);
        assertNull(tienda.getPrograma(admin));
    }

    @Test
    public void getVentasYEmpleadosDevuelvenCopias() {
        tienda.reabastecer(camiseta, 5);
        tienda.vender(comprador, items(camiseta, 1), null);
        tienda.getVentas().clear();
        assertEquals(1, tienda.getVentas().size());
        VentaProducto venta = tienda.getVentas().get(0);
        venta.getItems().clear();
        assertEquals(1, venta.getItems().size());
    }
}
