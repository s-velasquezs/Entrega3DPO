package club.modelo.comercio;

public class Producto {

    private String nombre;
    private String categoria;
    private double precio;

    public Producto(String nombre, String categoria, double precio) {
        validarNombre(nombre);
        validarCategoria(categoria);
        validarPrecio(precio);
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacio");
        }
    }

    private void validarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("La categoria del producto no puede estar vacia");
        }
    }

    private void validarPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        validarCategoria(categoria);
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        validarPrecio(precio);
        this.precio = precio;
    }

    // Dos productos son iguales si tienen el mismo nombre y categoria (sin importar mayusculas)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Producto)) {
            return false;
        }
        Producto p = (Producto) o;
        return nombre.equalsIgnoreCase(p.nombre) && categoria.equalsIgnoreCase(p.categoria);
    }

    @Override
    public int hashCode() {
        return nombre.toLowerCase().hashCode() + 31 * categoria.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + categoria + ")";
    }
}
