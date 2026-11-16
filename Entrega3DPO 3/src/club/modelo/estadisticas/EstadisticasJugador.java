package club.modelo.estadisticas;

import java.util.ArrayList;
import java.util.List;

import club.modelo.entrenamientos.RegistroEntrenamiento;
import club.modelo.enums.EstadoAsistencia;

public class EstadisticasJugador extends Estadisticas {

    private List<String> sanciones = new ArrayList<String>();
    private int partidosSuspension = 0;
    private int amarillasSancionadas = 0;

    public List<String> getSanciones() {
        return new ArrayList<String>(sanciones);
    }

    // Suma las estadisticas de un partido y aplica sanciones si corresponde:
    // cada 2 amarillas acumuladas = 1 partido, cada roja = 1 partido
    public void acumular(Estadisticas estadisticas) {
        if (estadisticas == null) {
            throw new IllegalArgumentException("Las estadisticas no pueden ser nulas");
        }
        sumar(estadisticas);

        int ciclos = get("amarillas") / 2;
        while (amarillasSancionadas < ciclos) {
            aplicarSancion("Acumulacion de amarillas", 1);
            amarillasSancionadas++;
        }

        for (int i = 0; i < estadisticas.get("rojas"); i++) {
            aplicarSancion("Roja directa", 1);
        }
    }

    // Suma lo que paso en un entrenamiento (asistencia y nota)
    public void acumularReg(RegistroEntrenamiento registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
        EstadoAsistencia estado = registro.getAsistencia();
        if (estado == null) {
            throw new IllegalStateException("El registro todavia no tiene asistencia");
        }

        if (estado == EstadoAsistencia.ASISTIO) {
            registrar("asistencias", get("asistencias") + 1);
        } else if (estado == EstadoAsistencia.TARDE) {
            registrar("tardanzas", get("tardanzas") + 1);
        } else {
            registrar("faltas", get("faltas") + 1);
        }

        if (registro.fueEvaluado()) {
            registrar("entrenamientos_evaluados", get("entrenamientos_evaluados") + 1);
            registrar("nota_total", get("nota_total") + registro.getNota());
        }
    }

    public int getPartidosSuspension() {
        return partidosSuspension;
    }

    public void aplicarSancion(String motivo, int partidos) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la sancion no puede estar vacio");
        }
        if (partidos <= 0) {
            throw new IllegalArgumentException("Los partidos de sancion deben ser mayores que 0");
        }
        sanciones.add(motivo);
        partidosSuspension += partidos;
    }

    public void consumirSuspension() {
        if (partidosSuspension <= 0) {
            throw new IllegalStateException("El jugador no tiene suspension pendiente");
        }
        partidosSuspension--;
    }

    public boolean tieneSancionActiva() {
        return partidosSuspension > 0;
    }
}
