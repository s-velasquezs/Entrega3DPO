package club.modelo.personal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import club.modelo.comercio.ProgramaFidelidad;
import club.modelo.enums.EstadoSolicitud;
import club.modelo.usuarios.Usuario;

public class Empleado extends Usuario {

	private String nombre;
	private boolean activo;
	private String codigoDescuento;
	private List<Turno> turnos;
	private List<SolicitudCambioTurno> solicitudes;
	private ProgramaFidelidad programaFidelidad;

	public Empleado(String username, String password, String nombre) {
		super(username, password);
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del empleado es obligatorio.");
		}
		this.nombre = nombre;
		this.activo = true;
		this.codigoDescuento = "DESC-" + username.toUpperCase();
		this.turnos = new ArrayList<Turno>();
		this.solicitudes = new ArrayList<SolicitudCambioTurno>();
		this.programaFidelidad = new ProgramaFidelidad(this);
	}

	public String getNombre() {
		return nombre;
	}

	public boolean estaActivo() {
		return activo;
	}

	public void darDeBaja() {
		if (!activo) {
			throw new IllegalStateException("El empleado ya está dado de baja.");
		}
		activo = false;
	}

	public String getCodigoDescuento() {
		return codigoDescuento;
	}

	public ProgramaFidelidad getProgramaFidelidad() {
		return programaFidelidad;
	}

	public boolean puedeComprarConDescuento() {
		return activo && codigoDescuento != null;
	}

	private boolean haySolapamiento(Turno candidato, Turno ignorado) {
		for (Turno t : turnos) {
			if (t != ignorado
					&& t.seSobrepone(candidato.getFecha(), candidato.getHoraInicio(), candidato.getHoraFin())) {
				return true;
			}
		}
		return false;
	}

	public boolean agregarTurno(Turno nuevoTurno) {
		if (nuevoTurno == null) {
			throw new IllegalArgumentException("El turno es obligatorio.");
		}
		if (nuevoTurno.getEmpleado() != this) {
			throw new IllegalArgumentException("El turno pertenece a otro empleado.");
		}
		if (!activo) {
			throw new IllegalStateException("El empleado está dado de baja.");
		}
		if (haySolapamiento(nuevoTurno, null)) {
			throw new IllegalStateException("El turno se cruza con otro turno del empleado.");
		}
		turnos.add(nuevoTurno);
		return true;
	}

	public List<Turno> consultarTurnos(LocalDate desde, LocalDate hasta) {
		if (desde == null || hasta == null) {
			throw new IllegalArgumentException("Las fechas del periodo son obligatorias.");
		}
		if (desde.isAfter(hasta)) {
			throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
		}
		List<Turno> lista = new ArrayList<Turno>();
		for (Turno t : turnos) {
			if (!t.getFecha().isBefore(desde) && !t.getFecha().isAfter(hasta)) {
				lista.add(t);
			}
		}
		lista.sort(Comparator.comparing(Turno::getFecha).thenComparing(Turno::getHoraInicio));
		return lista;
	}

	public boolean tieneTurnoEn(LocalDate fecha, LocalTime ini, LocalTime fin) {
		if (fecha == null || ini == null || fin == null) {
			throw new IllegalArgumentException("Fecha y franja horaria son obligatorias.");
		}
		for (Turno t : turnos) {
			if (t.seSobrepone(fecha, ini, fin)) {
				return true;
			}
		}
		return false;
	}

	public boolean reemplazarTurno(Turno actual, Turno nuevo) {
		if (actual == null || nuevo == null) {
			throw new IllegalArgumentException("Los turnos son obligatorios.");
		}
		if (nuevo.getEmpleado() != this) {
			throw new IllegalArgumentException("El turno nuevo pertenece a otro empleado.");
		}
		int pos = turnos.indexOf(actual);
		if (pos < 0) {
			throw new IllegalArgumentException("El turno actual no pertenece al empleado.");
		}
		if (haySolapamiento(nuevo, actual)) {
			throw new IllegalStateException("El turno nuevo se cruza con otro turno del empleado.");
		}
		turnos.set(pos, nuevo);
		return true;
	}

	public List<Turno> getTurnos() {
		return new ArrayList<Turno>(turnos);
	}

	public SolicitudCambioTurno solicitarCambioTurno(Turno actual, Turno nuevo, String motivo,
			List<Empleado> personal) {
		if (!activo) {
			throw new IllegalStateException("El empleado está dado de baja.");
		}
		if (actual == null || nuevo == null || personal == null) {
			throw new IllegalArgumentException("Turno actual, turno nuevo y personal son obligatorios.");
		}
		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo del cambio es obligatorio.");
		}
		if (!turnos.contains(actual)) {
			throw new IllegalArgumentException("El turno actual no pertenece al empleado.");
		}
		if (nuevo.getEmpleado() != this) {
			throw new IllegalArgumentException("El turno nuevo pertenece a otro empleado.");
		}
		for (SolicitudCambioTurno s : solicitudes) {
			if (s.getEstado() == EstadoSolicitud.PENDIENTE && s.getTurnoActual() == actual) {
				throw new IllegalStateException("Ya hay una solicitud pendiente para ese turno.");
			}
		}
		if (haySolapamiento(nuevo, actual)) {
			throw new IllegalStateException("El turno nuevo se cruza con otro turno del empleado.");
		}
		SolicitudCambioTurno s = new SolicitudCambioTurno(this, actual, nuevo, motivo);
		if (!s.cumpleMinimoPersonal(personal)) {
			throw new IllegalStateException(
					"La solicitud rompe el mínimo de personal exigido (2 entrenadores y 1 fisioterapeuta).");
		}
		solicitudes.add(s);
		return s;
	}

	public List<SolicitudCambioTurno> getSolicitudes() {
		return new ArrayList<SolicitudCambioTurno>(solicitudes);
	}

	@Override
	public String toString() {
		return nombre + " (" + getUsername() + ")";
	}
}