package club.modelo.instalaciones;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.enums.EstadoInstalacion;
import club.modelo.enums.ModalidadJuego;

public class Instalacion {

    private String id;
    private String nombre;
    private int capacidad;
    private EstadoInstalacion estado;
    private List<ReservaCancha> reservas;
    private Deporte deporte;
    private boolean techada;
    private List<Entrenamiento> entrenamientos;

    public Instalacion(String id, String nombre, int capacidad, Deporte deporte, boolean techada) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la instalacion no puede estar vacio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la instalacion no puede estar vacio");
        }
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que 0");
        }
        if (deporte == null) {
            throw new IllegalArgumentException("El deporte no puede ser nulo");
        }
        this.id = id;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.deporte = deporte;
        this.techada = techada;
        this.estado = EstadoInstalacion.DISPONIBLE;
        this.reservas = new ArrayList<ReservaCancha>();
        this.entrenamientos = new ArrayList<Entrenamiento>();
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public EstadoInstalacion getEstado() {
        return estado;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public boolean esTechada() {
        return techada;
    }

    public List<ReservaCancha> getReservas() {
        return new ArrayList<ReservaCancha>(reservas);
    }

    public List<Entrenamiento> getEntrenamientos() {
        return new ArrayList<Entrenamiento>(entrenamientos);
    }

    // Esta disponible si no esta fuera de servicio y no hay reservas ni entrenamientos que se crucen
    public boolean estaDisponible(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("La fecha y las horas no pueden ser nulas");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser despues de la hora de inicio");
        }
        if (estado != EstadoInstalacion.DISPONIBLE) {
            return false;
        }
        for (ReservaCancha r : reservas) {
            if (r.estaActiva() && r.seSolapaCon(fecha, horaInicio, horaFin)) {
                return false;
            }
        }
        for (Entrenamiento e : entrenamientos) {
            if (e.seSolapaCon(fecha, horaInicio, horaFin)) {
                return false;
            }
        }
        return true;
    }

    // SENCILLOS = 2 jugadores, DOBLES = 4 jugadores. Tambien tiene que caber en la instalacion.
    public boolean admiteJugadores(int cantidad, ModalidadJuego modalidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de jugadores debe ser mayor que 0");
        }
        if (modalidad == null) {
            throw new IllegalArgumentException("La modalidad no puede ser nula");
        }
        int necesarios;
        if (modalidad == ModalidadJuego.SENCILLOS) {
            necesarios = 2;
        } else {
            necesarios = 4;
        }
        return cantidad == necesarios && cantidad <= capacidad;
    }

    public void fueraDeServ() {
        if (estado == EstadoInstalacion.FUERADESERVICIO) {
            throw new IllegalStateException("La instalacion ya esta fuera de servicio");
        }
        estado = EstadoInstalacion.FUERADESERVICIO;
    }

    public void enServ() {
        if (estado == EstadoInstalacion.DISPONIBLE) {
            throw new IllegalStateException("La instalacion ya esta en servicio");
        }
        estado = EstadoInstalacion.DISPONIBLE;
    }

    public void agregarReserva(ReservaCancha reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if (reserva.getInstalacion() != this) {
            throw new IllegalArgumentException("La reserva es de otra instalacion");
        }
        if (!estaDisponible(reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin())) {
            throw new IllegalStateException("La instalacion no esta disponible en ese horario");
        }
        reservas.add(reserva);
    }

    public void agregarEntrenamiento(Entrenamiento entrenamiento) {
        if (entrenamiento == null) {
            throw new IllegalArgumentException("El entrenamiento no puede ser nulo");
        }
        if (entrenamiento.getInstalacion() != this) {
            throw new IllegalArgumentException("El entrenamiento es de otra instalacion");
        }
        if (!estaDisponible(entrenamiento.getFecha(), entrenamiento.getHora(), entrenamiento.getHoraFin())) {
            throw new IllegalStateException("La instalacion no esta disponible en ese horario");
        }
        entrenamientos.add(entrenamiento);
    }

    @Override
    public String toString() {
        return nombre + " (" + id + ")";
    }
}
