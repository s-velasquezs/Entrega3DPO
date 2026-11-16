package club.consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.personal.Empleado;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.personal.Turno;
import club.modelo.usuarios.Administrador;


public abstract class MenuEmpleadoBase extends MenuBase {

	protected final Empleado empleado;

	protected MenuEmpleadoBase(Entrada in, Administrador admin, List<Deporte> deportes, Empleado empleado) {
		super(in, admin, deportes);
		this.empleado = empleado;
	}

	protected void verTurnos() {
		intentar(() -> {
			LocalDate desde = in.fecha("Desde");
			LocalDate hasta = in.fecha("Hasta");
			List<Turno> turnos = empleado.consultarTurnos(desde, hasta);
			System.out.println("\n--- Mis turnos ---");
			if (turnos.isEmpty()) {
				System.out.println("No hay turnos asignados en el periodo consultado.");
			}
			for (Turno t : turnos) {
				System.out.println(" - " + t);
			}
		});
	}

	protected void solicitarCambio() {
		intentar(() -> {
			Turno actual = in.elegir("Turno que quieres dejar", empleado.getTurnos());
			if (actual == null) {
				return;
			}
			System.out.println("Datos del turno nuevo:");
			LocalDate fecha = in.fecha("Fecha");
			LocalTime ini = in.hora("Hora de inicio");
			LocalTime fin = in.hora("Hora de fin");
			String motivo = in.texto("Motivo del cambio");
			Turno nuevo = new Turno(empleado, fecha, ini, fin);
			SolicitudCambioTurno s = empleado.solicitarCambioTurno(actual, nuevo, motivo, admin.getPersonal());
			System.out.println("[OK] Solicitud registrada como " + s.getEstado() + " de aprobación del administrador.");
		});
	}

	protected void verSolicitudes() {
		System.out.println("\n--- Mis solicitudes de cambio de turno ---");
		List<SolicitudCambioTurno> lista = empleado.getSolicitudes();
		if (lista.isEmpty()) {
			System.out.println("No has hecho solicitudes.");
		}
		for (SolicitudCambioTurno s : lista) {
			System.out.println(" - " + Vista.solicitud(s));
		}
	}
}