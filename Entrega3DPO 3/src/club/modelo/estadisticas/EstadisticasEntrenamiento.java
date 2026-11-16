package club.modelo.estadisticas;

import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.entrenamientos.RegistroEntrenamiento;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;

// Estadisticas de un entrenamiento, de un jugador o de un equipo.
// Si se crea con jugador se calcula con ese jugador; si se crea con equipo, con los jugadores del equipo.
public class EstadisticasEntrenamiento extends Estadisticas {

    private Entrenamiento entrenamiento;
    private Jugador jugador;
    private Equipo equipo;

    public EstadisticasEntrenamiento(Entrenamiento entrenamiento, Jugador jugador) {
        if (entrenamiento == null) {
            throw new IllegalArgumentException("El entrenamiento no puede ser nulo");
        }
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }
        this.entrenamiento = entrenamiento;
        this.jugador = jugador;
        this.equipo = null;
    }

    public EstadisticasEntrenamiento(Entrenamiento entrenamiento, Equipo equipo) {
        if (entrenamiento == null) {
            throw new IllegalArgumentException("El entrenamiento no puede ser nulo");
        }
        if (equipo == null) {
            throw new IllegalArgumentException("El equipo no puede ser nulo");
        }
        this.entrenamiento = entrenamiento;
        this.jugador = null;
        this.equipo = equipo;
    }

    public Entrenamiento getEntrenamiento() {
        return entrenamiento;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    // Promedio de las notas de los registros evaluados que le corresponden
    public double promedioNota() {
        int suma = 0;
        int cantidad = 0;
        for (RegistroEntrenamiento r : entrenamiento.getRegistros()) {
            if (r.fueEvaluado() && esDeEstaEstadistica(r)) {
                suma = suma + r.getNota();
                cantidad++;
            }
        }
        if (cantidad == 0) {
            throw new IllegalStateException("No hay evaluaciones para calcular el promedio");
        }
        return (double) suma / cantidad;
    }

    private boolean esDeEstaEstadistica(RegistroEntrenamiento r) {
        if (jugador != null) {
            return r.getJugador() == jugador;
        }
        if (r.getJugador() instanceof JugadorEquipo) {
            return equipo.tieneJugador((JugadorEquipo) r.getJugador());
        }
        return false;
    }
}
