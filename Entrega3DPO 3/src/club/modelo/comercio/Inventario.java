package club.modelo.comercio;

import java.util.HashMap;
import java.util.Map;

public class Inventario {

    // producto -> (ubicacion -> cantidad)
    private Map<Producto, Map<String, Integer>> existencias = new HashMap<Producto, Map<String, Integer>>();

    private void validar(Producto prod, String ubi) {
        if (prod == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        if (ubi == null || ubi.isBlank()) {
            throw new IllegalArgumentException("La ubicacion no puede estar vacia");
        }
    }

    // Si el producto no esta en esa ubicacion devuelve 0
    public int getCantidad(Producto prod, String ubi) {
        validar(prod, ubi);
        Map<String, Integer> porUbicacion = existencias.get(prod);
        if (porUbicacion == null) {
            return 0;
        }
        Integer cantidad = porUbicacion.get(ubi);
        if (cantidad == null) {
            return 0;
        }
        return cantidad;
    }

    public void agregar(Producto prod, String ubi, int cantidad) {
        validar(prod, ubi);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        Map<String, Integer> porUbicacion = existencias.get(prod);
        if (porUbicacion == null) {
            porUbicacion = new HashMap<String, Integer>();
            existencias.put(prod, porUbicacion);
        }
        porUbicacion.put(ubi, getCantidad(prod, ubi) + cantidad);
    }

    // Da de alta un producto en el catalogo sin existencias (si ya existe no hace nada)
    public void registrarProducto(Producto prod) {
        if (prod == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        if (!existencias.containsKey(prod)) {
            existencias.put(prod, new HashMap<String, Integer>());
        }
    }

    public void retirar(Producto prod, String ubi, int cantidad) {
        validar(prod, ubi);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        int actual = getCantidad(prod, ubi);
        if (actual < cantidad) {
            throw new IllegalStateException("Existencias insuficientes de " + prod.getNombre()
                    + " en " + ubi + " (hay " + actual + ", se piden " + cantidad + ")");
        }
        existencias.get(prod).put(ubi, actual - cantidad);
    }

    // Primero retira del origen y despues agrega al destino (si el retiro falla no se cambia nada)
    public void mover(Producto prod, String origen, String destino, int cantidad) {
        validar(prod, origen);
        validar(prod, destino);
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("El origen y el destino son iguales");
        }
        retirar(prod, origen, cantidad);
        agregar(prod, destino, cantidad);
    }

    // Total de cada producto sumando todas las ubicaciones
    public Map<Producto, Integer> listarExistencias() {
        Map<Producto, Integer> totales = new HashMap<Producto, Integer>();
        for (Map.Entry<Producto, Map<String, Integer>> e : existencias.entrySet()) {
            int suma = 0;
            for (Integer c : e.getValue().values()) {
                suma = suma + c;
            }
            totales.put(e.getKey(), suma);
        }
        return totales;
    }
}
