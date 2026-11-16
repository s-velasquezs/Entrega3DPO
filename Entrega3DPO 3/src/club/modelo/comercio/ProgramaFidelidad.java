package club.modelo.comercio;

import club.modelo.personal.Empleado;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.Usuario;

public class ProgramaFidelidad {

    public static final double VALOR_POR_PUNTO = 100;  // cada 100 pesos gastados = 1 punto
    public static final double VALOR_REDENCION = 10;   // cada punto redimido vale 10 pesos

    private Usuario propietario;
    private int saldoPuntos;

    public ProgramaFidelidad(Usuario propietario) {
        if (propietario == null) {
            throw new IllegalArgumentException("El propietario no puede ser nulo");
        }
        this.propietario = propietario;
        this.saldoPuntos = 0;
    }

    // Devuelve el programa del usuario (jugador o empleado). Si no tiene (ej: el administrador) devuelve null.
    public static ProgramaFidelidad deUsuario(Usuario u) {
        if (u instanceof Jugador) {
            return ((Jugador) u).getProgramaFidelidad();
        }
        if (u instanceof Empleado) {
            return ((Empleado) u).getProgramaFidelidad();
        }
        return null;
    }

    public void acumular(double valorVenta) {
        if (valorVenta < 0) {
            throw new IllegalArgumentException("El valor de la venta no puede ser negativo");
        }
        saldoPuntos = saldoPuntos + (int) (valorVenta / VALOR_POR_PUNTO);
    }

    public double redimir(int puntos) {
        if (puntos <= 0) {
            throw new IllegalArgumentException("Los puntos deben ser positivos");
        }
        if (puntos > saldoPuntos) {
            throw new IllegalStateException("Saldo de puntos insuficiente");
        }
        saldoPuntos = saldoPuntos - puntos;
        return puntos * VALOR_REDENCION;
    }

    public int getSaldo() {
        return saldoPuntos;
    }

    public Usuario getPropietario() {
        return propietario;
    }
}
