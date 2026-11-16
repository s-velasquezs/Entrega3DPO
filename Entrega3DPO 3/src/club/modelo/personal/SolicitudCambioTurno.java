package club.modelo.personal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import club.modelo.enums.EstadoSolicitud;

public class SolicitudCambioTurno {

	public static final int MIN_ENTRENADORES = 2;
	public static final int MIN_FISIOTERAPEUTAS = 1;

	private Empleado empleado;
	private Turno turnoActual;
	private Turno turnoNuevo;
	private String motivo;
	private EstadoSolicitud estado;
	private String razonRechazo;

	public SolicitudCambioTurno(Empleado empleado, Turno turnoActual, Turno turnoNuevo, String motivo) {
		if (empleado == null || turnoActual == null || turnoNuevo == null) {
			throw new IllegalArgumentException("La solicitud necesita empleado, turno actual y turno nuevo.");
		}
		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo es obligatorio.");
		}
		if (turnoActual.getEmpleado() != empleado || turnoNuevo.getEmpleado() != empleado) {
			throw new IllegalArgumentException("Los dos turnos deben ser del empleado que solicita.");
		}
		this.empleado = empleado;
		this.turnoActual = turnoActual;
		this.turnoNuevo = turnoNuevo;
		this.motivo = motivo;
		this.estado = EstadoSolicitud.PENDIENTE;
		this.razonRechazo = null;
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public Turno getTurnoActual() {
		return turnoActual;
	}

	public Turno getTurnoNuevo() {
		return turnoNuevo;
	}

	public String getMotivo() {
		return motivo;
	}

	public EstadoSolicitud getEstado() {
		return estado;
	}

	public String getRazonRechazo() {
		return razonRechazo;
	}

	public boolean cumpleMinimoPersonal(List<Empleado> personal) {
		if (personal == null) {
			throw new IllegalArgumentException("La lista de personal es obligatoria.");
		}
		LocalDate fecha = turnoActual.getFecha();
		LocalTime ini = turnoActual.getHoraInicio();
		LocalTime fin = turnoActual.getHoraFin();
		int entrenadores = 0;
		int fisios = 0;

		for (Empleado e : personal) {
			if (!e.estaActivo()) {
				continue;
			}
			boolean cubre;
			if (e == empleado) {
				cubre = turnoNuevo.seSobrepone(fecha, ini, fin);
			} else {
				cubre = e.tieneTurnoEn(fecha, ini, fin);
			}
			if (cubre) {
				if (e instanceof Entrenador) {
					entrenadores++;
				} else if (e instanceof Fisioterapeuta) {
					fisios++;
				}
			}
		}
		return entrenadores >= MIN_ENTRENADORES && fisios >= MIN_FISIOTERAPEUTAS;
	}

	public boolean aprobar(List<Empleado> personal) {
		if (estado != EstadoSolicitud.PENDIENTE) {
			throw new IllegalStateException("La solicitud ya fue resuelta.");
		}
		if (!empleado.estaActivo()) {
			throw new IllegalStateException("El empleado está dado de baja.");
		}
		if (!cumpleMinimoPersonal(personal)) {
			throw new IllegalStateException("No se puede aprobar: la franja quedaría sin el mínimo de "
					+ MIN_ENTRENADORES + " entrenadores y " + MIN_FISIOTERAPEUTAS + " fisioterapeuta.");
		}
		empleado.reemplazarTurno(turnoActual, turnoNuevo);
		estado = EstadoSolicitud.APROBADA;
		return true;
	}

	public void rechazar(String razon) {
		if (estado != EstadoSolicitud.PENDIENTE) {
			throw new IllegalStateException("La solicitud ya fue resuelta.");
		}
		if (razon == null || razon.isBlank()) {
			throw new IllegalArgumentException("La razón del rechazo es obligatoria.");
		}
		estado = EstadoSolicitud.RECHAZADA;
		razonRechazo = razon;
	}
}
