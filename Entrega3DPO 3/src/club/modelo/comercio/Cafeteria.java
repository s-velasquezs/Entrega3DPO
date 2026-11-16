package club.modelo.comercio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import club.modelo.personal.Empleado;
import club.modelo.usuarios.Usuario;

public class Cafeteria {

    private static final String UBICACION = "CAFETERIA";
    private static final double PORCENTAJE_PROPINA = 0.10;
    private static final double DESCUENTO_EMPLEADO = 20; // porcentaje

    private Inventario inventario;
    private List<VentaProducto> ventas;
    private List<EmpleadoNegocio> empleados;

    public Cafeteria(Inventario inventario) {
        if (inventario == null) {
            throw new IllegalArgumentException("El inventario no puede ser nulo");
        }
        this.inventario = inventario;
        this.ventas = new ArrayList<VentaProducto>();
        this.empleados = new ArrayList<EmpleadoNegocio>();
    }

    // Igual que en la tienda, pero ademas se le suma la propina sugerida (10% de lo que se paga sin impuesto)
    public VentaProducto vender(Usuario comprador, Map<Producto, Integer> items, String codigo) {
        if (comprador == null) {
            throw new IllegalArgumentException("El comprador no puede ser nulo");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La venta no tiene productos");
        }

        for (Map.Entry<Producto, Integer> e : items.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || e.getValue() <= 0) {
                throw new IllegalArgumentException("Cada producto debe tener una cantidad positiva");
            }
            if (inventario.getCantidad(e.getKey(), UBICACION) < e.getValue()) {
                throw new IllegalStateException("Existencias insuficientes de " + e.getKey().getNombre());
            }
        }

        VentaProducto venta = new VentaProducto(LocalDate.now(), comprador, items, UBICACION);
        venta.aplicarDescuento(porcentajeDescuento(comprador, codigo));
        venta.setPropina(propinaSugerida(venta.getSubtotal() - venta.getDescuento()));
        venta.calcularPuntos();

        for (Map.Entry<Producto, Integer> e : items.entrySet()) {
            inventario.retirar(e.getKey(), UBICACION, e.getValue());
        }

        ProgramaFidelidad programa = ProgramaFidelidad.deUsuario(comprador);
        if (programa != null) {
            programa.acumular(venta.getTotal());
        }
        ventas.add(venta);
        return venta;
    }

    public double propinaSugerida(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("El subtotal no puede ser negativo");
        }
        return subtotal * PORCENTAJE_PROPINA;
    }

    private double porcentajeDescuento(Usuario comprador, String codigo) {
        if (codigo == null) {
            return 0;
        }
        String c = codigo.trim().toUpperCase();
        if (c.equals("CLUB10")) {
            return 10;
        }
        if (c.equals("CLUB20")) {
            return 20;
        }
        if (comprador instanceof Empleado) {
            Empleado emp = (Empleado) comprador;
            if (emp.puedeComprarConDescuento() && c.equals(emp.getCodigoDescuento().toUpperCase())) {
                return DESCUENTO_EMPLEADO;
            }
        }
        return 0;
    }

    public void agregarEmpleado(EmpleadoNegocio e) {
        if (e == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo");
        }
        if (empleados.contains(e)) {
            throw new IllegalStateException("El empleado ya esta registrado");
        }
        empleados.add(e);
    }

    public List<EmpleadoNegocio> getEmpleados() {
        return new ArrayList<EmpleadoNegocio>(empleados);
    }

    public List<VentaProducto> getVentas() {
        return new ArrayList<VentaProducto>(ventas);
    }

    public Inventario getInventario() {
        return inventario;
    }

    public ProgramaFidelidad getPrograma(Usuario u) {
        return ProgramaFidelidad.deUsuario(u);
    }
}
