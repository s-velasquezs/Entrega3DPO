package club.modelo.comercio;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import club.modelo.usuarios.Usuario;

public class VentaProducto {

    public static final double TASA_IMPUESTO = 0.19; // IVA

    private LocalDate fecha;
    private Usuario comprador;
    private double subtotal;
    private double descuento;
    private Map<Producto, Integer> items;
    private double impuesto;
    private double propina;
    private double total;
    private int puntosGenerados;
    private String rubro;

    public VentaProducto(LocalDate fecha, Usuario comprador, Map<Producto, Integer> items, String rubro) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        if (comprador == null) {
            throw new IllegalArgumentException("El comprador no puede ser nulo");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La venta no tiene productos");
        }
        if (rubro == null || rubro.isBlank()) {
            throw new IllegalArgumentException("El rubro no puede estar vacio");
        }
        for (Map.Entry<Producto, Integer> e : items.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || e.getValue() <= 0) {
                throw new IllegalArgumentException("Cada producto debe tener una cantidad positiva");
            }
        }
        this.fecha = fecha;
        this.comprador = comprador;
        this.items = new HashMap<Producto, Integer>(items);
        this.rubro = rubro;
        calcularSubtotal();
    }

    public double calcularSubtotal() {
        double suma = 0;
        for (Map.Entry<Producto, Integer> e : items.entrySet()) {
            suma = suma + e.getKey().getPrecio() * e.getValue();
        }
        subtotal = suma;
        return subtotal;
    }

    // El porcentaje va de 0 a 100
    public void aplicarDescuento(double porcentaje) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
        descuento = subtotal * porcentaje / 100;
    }

    public double calcularImpuesto() {
        impuesto = (subtotal - descuento) * TASA_IMPUESTO;
        return impuesto;
    }

    public double calcularTotal() {
        calcularImpuesto();
        total = subtotal - descuento + impuesto + propina;
        return total;
    }

    public int calcularPuntos() {
        calcularTotal();
        puntosGenerados = (int) (total / ProgramaFidelidad.VALOR_POR_PUNTO);
        return puntosGenerados;
    }

    public void setPropina(double propina) {
        if (propina < 0) {
            throw new IllegalArgumentException("La propina no puede ser negativa");
        }
        this.propina = propina;
        calcularTotal();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Usuario getComprador() {
        return comprador;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    // Devuelve una copia para que nadie cambie los items de una venta ya hecha
    public Map<Producto, Integer> getItems() {
        return new HashMap<Producto, Integer>(items);
    }

    public double getImpuesto() {
        return impuesto;
    }

    public double getPropina() {
        return propina;
    }

    public double getTotal() {
        return total;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public String getRubro() {
        return rubro;
    }
}
