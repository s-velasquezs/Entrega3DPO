package club.modelo.estadisticas;

import club.modelo.deportes.Equipo;
import club.modelo.usuarios.JugadorEquipo;

public class EstadisticasEquipo extends Estadisticas {

    private Equipo equipo;

    public EstadisticasEquipo(Equipo equipo) {
        if (equipo == null) {
            throw new IllegalArgumentException("El equipo no puede ser nulo");
        }
        this.equipo = equipo;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    // Suma las estadisticas de un jugador del equipo en un partido
    public void acumular(EstadisticasPartido estadisticas) {
        if (estadisticas == null) {
            throw new IllegalArgumentException("Las estadisticas no pueden ser nulas");
        }
        boolean esDelEquipo = false;
        if (estadisticas.getJugador() instanceof JugadorEquipo) {
            JugadorEquipo j = (JugadorEquipo) estadisticas.getJugador();
            esDelEquipo = equipo.tieneJugador(j);
        }
        if (!esDelEquipo) {
            throw new IllegalArgumentException("El jugador no pertenece al equipo");
        }
        sumar(estadisticas);
    }
}
