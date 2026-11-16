package club.modelo.estadisticas;

import club.modelo.partidos.Partido;
import club.modelo.usuarios.Jugador;

public class EstadisticasPartido extends Estadisticas {

    private Partido partido;
    private Jugador jugador;
    private int minutos;

    public EstadisticasPartido(Partido partido, Jugador jugador, int minutos) {
        if (partido == null) {
            throw new IllegalArgumentException("El partido no puede ser nulo");
        }
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }
        if (minutos < 0) {
            throw new IllegalArgumentException("Los minutos no pueden ser negativos");
        }
        this.partido = partido;
        this.jugador = jugador;
        this.minutos = minutos;
    }

    public Partido getPartido() {
        return partido;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public int getMinutos() {
        return minutos;
    }
}
