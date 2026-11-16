package club.modelo.entrenamientos;

import club.modelo.enums.EstadoAsistencia;
import club.modelo.usuarios.Jugador;

public class RegistroEntrenamiento {

    public static final int NOTA_MINIMA = 1;
    public static final int NOTA_MAXIMA = 5;

    private Entrenamiento entrenamiento;
    private Jugador jugador;
    private EstadoAsistencia asistencia;
    private int nota;
    private String comentario;
    private boolean evaluado;

    public RegistroEntrenamiento(Entrenamiento entrenamiento, Jugador jugador) {
        if (entrenamiento == null) {
            throw new IllegalArgumentException("El entrenamiento no puede ser nulo");
        }
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }
        if (!entrenamiento.getConvocados().contains(jugador)) {
            throw new IllegalArgumentException("El jugador no fue convocado al entrenamiento");
        }
        this.entrenamiento = entrenamiento;
        this.jugador = jugador;
        this.asistencia = null;
        this.nota = 0;
        this.comentario = "";
        this.evaluado = false;
    }

    public Entrenamiento getEntrenamiento() {
        return entrenamiento;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public EstadoAsistencia getAsistencia() {
        return asistencia;
    }

    public int getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }

    // La asistencia se puede cambiar mientras no se haya evaluado
    public void registrarAsistencia(EstadoAsistencia estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado de asistencia no puede ser nulo");
        }
        if (evaluado) {
            throw new IllegalStateException("El jugador ya fue evaluado");
        }
        this.asistencia = estado;
    }

    // Solo se evalua una vez y solo si el jugador fue al entrenamiento
    public void evaluar(int nota, String comentario) {
        if (asistencia == null) {
            throw new IllegalStateException("Primero hay que registrar la asistencia");
        }
        if (asistencia == EstadoAsistencia.FALTO) {
            throw new IllegalStateException("No se puede evaluar a un jugador que falto");
        }
        if (evaluado) {
            throw new IllegalStateException("El jugador ya fue evaluado");
        }
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new IllegalArgumentException("La nota debe estar entre " + NOTA_MINIMA + " y " + NOTA_MAXIMA);
        }
        this.nota = nota;
        if (comentario == null) {
            this.comentario = "";
        } else {
            this.comentario = comentario;
        }
        this.evaluado = true;
    }

    public boolean fueEvaluado() {
        return evaluado;
    }
}
