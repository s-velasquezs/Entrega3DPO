package club.modelo.comercio;

import club.modelo.enums.EstadoSolicitud;
import club.modelo.usuarios.Usuario;

// Propuesta de un producto nuevo para el catalogo; el administrador la aprueba o la rechaza.
public class SugerenciaProducto {

    private Usuario autor;
    private Producto producto;
    private EstadoSolicitud estado;
    private String razonRechazo;

    public SugerenciaProducto(Usuario autor, Producto producto) {
        if (autor == null) {
            throw new IllegalArgumentException("El autor de la sugerencia no puede ser nulo");
        }
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        this.autor = autor;
        this.producto = producto;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.razonRechazo = null;
    }

    public Usuario getAutor() {
        return autor;
    }

    public Producto getProducto() {
        return producto;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public String getRazonRechazo() {
        return razonRechazo;
    }

    public void aprobar() {
        if (estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("La sugerencia ya fue resuelta");
        }
        estado = EstadoSolicitud.APROBADA;
    }

    public void rechazar(String razon) {
        if (estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("La sugerencia ya fue resuelta");
        }
        if (razon == null || razon.isBlank()) {
            throw new IllegalArgumentException("La razon del rechazo es obligatoria");
        }
        estado = EstadoSolicitud.RECHAZADA;
        razonRechazo = razon;
    }

    @Override
    public String toString() {
        return producto + " sugerido por " + autor.getUsername() + " [" + estado + "]";
    }
}
