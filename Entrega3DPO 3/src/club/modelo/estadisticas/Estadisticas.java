package club.modelo.estadisticas;

import java.util.HashMap;
import java.util.Map;

public abstract class Estadisticas {

    // razon (por ejemplo "goles") -> valor acumulado
    private Map<String, Integer> valores = new HashMap<String, Integer>();

    public void registrar(String razon, int valor) {
        if (razon == null || razon.isBlank()) {
            throw new IllegalArgumentException("La razon no puede estar vacia");
        }
        if (valor < 0) {
            throw new IllegalArgumentException("El valor no puede ser negativo");
        }
        this.valores.put(razon, valor);
    }

    // Devuelve una copia para que nadie cambie el mapa sin pasar por registrar()
    public Map<String, Integer> getValores() {
        return new HashMap<String, Integer>(valores);
    }

    // Si la razon no existe devuelve 0
    public int getRazon(String razon) {
        if (razon == null) {
            throw new IllegalArgumentException("La razon no puede ser nula");
        }
        if (valores.containsKey(razon)) {
            return valores.get(razon);
        }
        return 0;
    }

    public int getValor(String razon) {
        return getRazon(razon);
    }

    public int get(String razon) {
        return getRazon(razon);
    }

    // Suma todos los valores de otra estadistica a esta
    public void sumar(Estadisticas otra) {
        if (otra == null) {
            throw new IllegalArgumentException("Las estadisticas a sumar no pueden ser nulas");
        }
        Map<String, Integer> copia = otra.getValores();
        for (Map.Entry<String, Integer> e : copia.entrySet()) {
            registrar(e.getKey(), getRazon(e.getKey()) + e.getValue());
        }
    }
}
