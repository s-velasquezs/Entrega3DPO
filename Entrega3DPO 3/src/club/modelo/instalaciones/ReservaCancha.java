package club.modelo.instalaciones;

import java.time.LocalDate;
import java.time.LocalTime;

import club.modelo.deportes.DeporteIndividual;
import club.modelo.enums.ModalidadJuego;
import club.modelo.usuarios.JugadorIndividual;

public class ReservaCancha {

    private JugadorIndividual jugador;
    private JugadorIndividual rival;
    private Instalacion instalacion;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int numeroJugadores;
    private boolean activa;
    private ModalidadJuego modalidad;

    public ReservaCancha(JugadorIndividual jugador, JugadorIndividual rival, Instalacion instalacion,
                         LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                         int numeroJugadores, ModalidadJuego modalidad) {
        if (jugador == null || rival == null) {
            throw new IllegalArgumentException("Los jugadores no pueden ser nulos");
        }
        if (instalacion == null) {
            throw new IllegalArgumentException("La instalacion no puede ser nula");
        }
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("La fecha y las horas no pueden ser nulas");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser despues de la hora de inicio");
        }
        if (modalidad == null) {
            throw new IllegalArgumentException("La modalidad no puede ser nula");
        }
        if (jugador == rival) {
            throw new IllegalArgumentException("Un jugador no puede jugar contra si mismo");
        }
        if (!(instalacion.getDeporte() instanceof DeporteIndividual)) {
            throw new IllegalArgumentException("La instalacion no es de un deporte individual");
        }
        DeporteIndividual deporte = (DeporteIndividual) instalacion.getDeporte();

        if (!jugador.estaActivo() || !rival.estaActivo()) {
            throw new IllegalStateException("Los jugadores deben estar activos");
        }
        if (!jugador.esRivalValido(rival, deporte)) {
            throw new IllegalArgumentException("Los jugadores no pueden enfrentarse en este deporte");
        }
        if (!instalacion.admiteJugadores(numeroJugadores, modalidad)) {
            throw new IllegalArgumentException("La instalacion no admite ese numero de jugadores");
        }
        if (!instalacion.estaDisponible(fecha, horaInicio, horaFin)) {
            throw new IllegalStateException("La instalacion no esta disponible en ese horario");
        }

        this.jugador = jugador;
        this.rival = rival;
        this.instalacion = instalacion;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.numeroJugadores = numeroJugadores;
        this.modalidad = modalidad;
        this.activa = true;
    }

    public JugadorIndividual getJugador() {
        return jugador;
    }

    public JugadorIndividual getRival() {
        return rival;
    }

    public Instalacion getInstalacion() {
        return instalacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public int getNumeroJugadores() {
        return numeroJugadores;
    }

    public ModalidadJuego getModalidad() {
        return modalidad;
    }

    public boolean estaActiva() {
        return activa;
    }

    public void cancelar() {
        if (!activa) {
            throw new IllegalStateException("La reserva ya esta cancelada");
        }
        activa = false;
    }

    // Se solapan si son el mismo dia y las horas se cruzan
    public boolean seSolapaCon(ReservaCancha otra) {
        if (otra == null) {
            throw new IllegalArgumentException("La otra reserva no puede ser nula");
        }
        return seSolapaCon(otra.fecha, otra.horaInicio, otra.horaFin);
    }

    public boolean seSolapaCon(LocalDate otraFecha, LocalTime otraInicio, LocalTime otraFin) {
        return fecha.equals(otraFecha) && horaInicio.isBefore(otraFin) && otraInicio.isBefore(horaFin);
    }
}
