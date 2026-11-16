package club.modelo.entrenamientos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.Equipo;
import club.modelo.deportes.Registro;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoAsistencia;
import club.modelo.enums.EstadoInstalacion;
import club.modelo.instalaciones.Instalacion;
import club.modelo.personal.Entrenador;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;

public class Entrenamiento {

    private LocalDate fecha;
    private LocalTime hora;
    private int duracion; // en minutos
    private Deporte deporte;
    private Entrenador responsable;
    private Instalacion instalacion;
    private Registro registro;
    private List<RegistroEntrenamiento> registros;
    private Categoria categoria;
    private Equipo equipo; // puede ser null si no es de un equipo
    private List<Jugador> convocados;
    private boolean estadisticasAcumuladas;

    public Entrenamiento(LocalDate fecha, LocalTime hora, int duracion, Deporte deporte,
                         Entrenador responsable, Instalacion instalacion, Categoria categoria, Equipo equipo) {
        this.fecha = fecha;
        this.hora = hora;
        this.duracion = duracion;
        this.deporte = deporte;
        this.responsable = responsable;
        this.instalacion = instalacion;
        this.categoria = categoria;
        this.equipo = equipo;
        this.registro = new Registro();
        this.registros = new ArrayList<RegistroEntrenamiento>();
        this.convocados = new ArrayList<Jugador>();
        this.estadisticasAcumuladas = false;
        validar();
    }

    // Revisa que los datos del entrenamiento tengan sentido
    public void validar() {
        if (fecha == null || hora == null) {
            throw new IllegalArgumentException("La fecha y la hora no pueden ser nulas");
        }
        if (deporte == null) {
            throw new IllegalArgumentException("El deporte no puede ser nulo");
        }
        if (responsable == null) {
            throw new IllegalArgumentException("El responsable no puede ser nulo");
        }
        if (instalacion == null) {
            throw new IllegalArgumentException("La instalacion no puede ser nula");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("La categoria no puede ser nula");
        }
        if (duracion <= 0) {
            throw new IllegalArgumentException("La duracion debe ser mayor que 0");
        }
        if (!hora.plusMinutes(duracion).isAfter(hora)) {
            throw new IllegalArgumentException("El entrenamiento no puede pasar de la medianoche");
        }
        if (!responsable.estaActivo()) {
            throw new IllegalStateException("El entrenador responsable no esta activo");
        }
        if (instalacion.getEstado() != EstadoInstalacion.DISPONIBLE) {
            throw new IllegalStateException("La instalacion esta fuera de servicio");
        }
        if (instalacion.getDeporte() != deporte) {
            throw new IllegalArgumentException("La instalacion no es de ese deporte");
        }
        if (equipo != null) {
            if (equipo.getDeporte() != deporte) {
                throw new IllegalArgumentException("El equipo no juega ese deporte");
            }
            if (equipo.getCategoria() != categoria) {
                throw new IllegalArgumentException("La categoria no coincide con la del equipo");
            }
            if (!equipo.estaActivo()) {
                throw new IllegalStateException("El equipo esta dado de baja");
            }
        }
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public int getDuracion() {
        return duracion;
    }

    public LocalTime getHoraFin() {
        return hora.plusMinutes(duracion);
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public Entrenador getResponsable() {
        return responsable;
    }

    public Instalacion getInstalacion() {
        return instalacion;
    }

    public Registro getRegistro() {
        return registro;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public List<RegistroEntrenamiento> getRegistros() {
        return new ArrayList<RegistroEntrenamiento>(registros);
    }

    public List<Jugador> getConvocados() {
        return new ArrayList<Jugador>(convocados);
    }

    // true si el entrenamiento ya termino en el momento dado
    public boolean yaFinalizo(LocalDateTime momento) {
        if (momento == null) {
            throw new IllegalArgumentException("El momento no puede ser nulo");
        }
        LocalDateTime fin = LocalDateTime.of(fecha, getHoraFin());
        return !momento.isBefore(fin);
    }

    public void convocar(Jugador j) {
        if (j == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }
        if (convocados.contains(j)) {
            throw new IllegalStateException("El jugador ya fue convocado");
        }
        if (!j.estaHabilitado()) {
            throw new IllegalStateException("El jugador no esta habilitado");
        }
        if (equipo != null) {
            boolean esDelEquipo = false;
            if (j instanceof JugadorEquipo) {
                esDelEquipo = equipo.tieneJugador((JugadorEquipo) j);
            }
            if (!esDelEquipo) {
                throw new IllegalArgumentException("El jugador no pertenece al equipo");
            }
        }
        if (convocados.size() >= instalacion.getCapacidad()) {
            throw new IllegalStateException("La instalacion no tiene capacidad para mas jugadores");
        }
        convocados.add(j);
    }

    // Dos entrenamientos estan en conflicto si se cruzan en el tiempo y comparten
    // instalacion, entrenador, equipo o algun jugador
    public boolean estaEnConflicto(Entrenamiento otra) {
        if (otra == null) {
            throw new IllegalArgumentException("El otro entrenamiento no puede ser nulo");
        }
        if (otra == this) {
            return false;
        }
        if (!otra.seSolapaCon(fecha, hora, getHoraFin())) {
            return false;
        }
        if (instalacion == otra.instalacion) {
            return true;
        }
        if (responsable == otra.responsable) {
            return true;
        }
        if (equipo != null && equipo == otra.equipo) {
            return true;
        }
        for (Jugador j : convocados) {
            if (otra.convocados.contains(j)) {
                return true;
            }
        }
        return false;
    }

    public boolean seSolapaCon(LocalDate otraFecha, LocalTime otraInicio, LocalTime otraFin) {
        return fecha.equals(otraFecha) && hora.isBefore(otraFin) && otraInicio.isBefore(getHoraFin());
    }

    // Asistencia registrada del jugador, o null si todavia no tiene
    public EstadoAsistencia getAsistencia(Jugador j) {
        RegistroEntrenamiento r = getRegistroDe(j);
        if (r == null) {
            return null;
        }
        return r.getAsistencia();
    }

    public RegistroEntrenamiento getRegistroDe(Jugador j) {
        for (RegistroEntrenamiento r : registros) {
            if (r.getJugador() == j) {
                return r;
            }
        }
        return null;
    }

    // Crea el registro del jugador si no existe y le pone la asistencia
    public void registrarAsistencia(Jugador j, EstadoAsistencia estado) {
        if (j == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo");
        }
        if (!convocados.contains(j)) {
            throw new IllegalArgumentException("El jugador no fue convocado al entrenamiento");
        }
        RegistroEntrenamiento r = getRegistroDe(j);
        if (r == null) {
            r = new RegistroEntrenamiento(this, j);
            registros.add(r);
        }
        r.registrarAsistencia(estado);
    }

    public void evaluar(Jugador j, int nota, String comentario) {
        RegistroEntrenamiento r = getRegistroDe(j);
        if (r == null) {
            throw new IllegalStateException("El jugador no tiene asistencia registrada");
        }
        r.evaluar(nota, comentario);
    }

    // Pasa los registros a las estadisticas de cada jugador. Solo se puede hacer una vez.
    public void acumularEstadisticas() {
        if (estadisticasAcumuladas) {
            throw new IllegalStateException("Las estadisticas de este entrenamiento ya se acumularon");
        }
        for (RegistroEntrenamiento r : registros) {
            r.getJugador().consultarEstadisticas().acumularReg(r);
        }
        estadisticasAcumuladas = true;
    }
}
