package club.test.personal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.modelo.enums.EstadoSolicitud;
import club.modelo.personal.Empleado;
import club.modelo.personal.Entrenador;
import club.modelo.personal.Fisioterapeuta;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.personal.Turno;

public class SolicitudCambioTurnoTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private Entrenador e1;
	private Entrenador e2;
	private Entrenador e3;
	private Fisioterapeuta fisio;
	private List<Empleado> personal;
	private Turno actual;

	private Turno turno(Empleado e, int ini, int fin) {
		return new Turno(e, FECHA, LocalTime.of(ini, 0), LocalTime.of(fin, 0));
	}

	@BeforeEach
	void setUp() {
		e1 = new Entrenador("e1", "pw", "E1");
		e2 = new Entrenador("e2", "pw", "E2");
		e3 = new Entrenador("e3", "pw", "E3");
		fisio = new Fisioterapeuta("f1", "pw", "F1");
		personal = new ArrayList<Empleado>(List.of(e1, e2, e3, fisio));
		for (Empleado e : personal) {
			e.agregarTurno(turno(e, 8, 12));
		}
		actual = e1.getTurnos().get(0);
	}

	@Test
	@DisplayName("Una solicitud nueva queda pendiente y guarda sus datos")
	void solicitudNueva() {
		Turno nuevo = turno(e1, 14, 18);
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, nuevo, "Cita");
		assertSame(e1, s.getEmpleado());
		assertSame(actual, s.getTurnoActual());
		assertSame(nuevo, s.getTurnoNuevo());
		assertEquals("Cita", s.getMotivo());
		assertEquals(EstadoSolicitud.PENDIENTE, s.getEstado());
		assertNull(s.getRazonRechazo());
	}

	@Test
	@DisplayName("Datos nulos o motivo inválido lanzan excepción")
	void constructorInvalido() {
		Turno nuevo = turno(e1, 14, 18);
		assertThrows(IllegalArgumentException.class, () -> new SolicitudCambioTurno(null, actual, nuevo, "m"));
		assertThrows(IllegalArgumentException.class, () -> new SolicitudCambioTurno(e1, null, nuevo, "m"));
		assertThrows(IllegalArgumentException.class, () -> new SolicitudCambioTurno(e1, actual, null, "m"));
		assertThrows(IllegalArgumentException.class, () -> new SolicitudCambioTurno(e1, actual, nuevo, null));
		assertThrows(IllegalArgumentException.class, () -> new SolicitudCambioTurno(e1, actual, nuevo, " "));
	}

	@Test
	@DisplayName("Los turnos deben ser del empleado que solicita")
	void turnosDeOtro() {
		Turno ajeno = turno(e2, 14, 18);
		assertThrows(IllegalArgumentException.class, () -> new SolicitudCambioTurno(e1, actual, ajeno, "m"));
		assertThrows(IllegalArgumentException.class,
				() -> new SolicitudCambioTurno(e1, e2.getTurnos().get(0), turno(e1, 14, 18), "m"));
	}

	@Test
	@DisplayName("Cumple el mínimo si quedan 2 entrenadores y 1 fisioterapeuta")
	void cumpleMinimo() {
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		assertTrue(s.cumpleMinimoPersonal(personal));
	}

	@Test
	@DisplayName("No cumple si con el cambio queda un solo entrenador")
	void noCumpleEntrenadores() {
		personal.remove(e3);
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		assertFalse(s.cumpleMinimoPersonal(personal));
	}

	@Test
	@DisplayName("No cumple si no hay fisioterapeuta en la franja")
	void noCumpleFisio() {
		personal.remove(fisio);
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		assertFalse(s.cumpleMinimoPersonal(personal));
	}

	@Test
	@DisplayName("Un empleado dado de baja no cuenta para el mínimo")
	void bajaNoCuenta() {
		e3.darDeBaja();
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		assertFalse(s.cumpleMinimoPersonal(personal));
	}

	@Test
	@DisplayName("Si el turno nuevo sigue cubriendo la franja, el empleado cuenta")
	void turnoNuevoSigueCubriendo() {
		personal.remove(e3);
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 9, 13), "m");
		assertTrue(s.cumpleMinimoPersonal(personal));
	}

	@Test
	@DisplayName("Una lista de personal nula lanza excepción")
	void personalNulo() {
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		assertThrows(IllegalArgumentException.class, () -> s.cumpleMinimoPersonal(null));
		assertThrows(IllegalArgumentException.class, () -> s.aprobar(null));
	}

	@Test
	@DisplayName("Aprobar actualiza el turno del empleado")
	void aprobar() {
		Turno nuevo = turno(e1, 14, 18);
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, nuevo, "m");
		assertTrue(s.aprobar(personal));
		assertEquals(EstadoSolicitud.APROBADA, s.getEstado());
		assertTrue(e1.getTurnos().contains(nuevo));
		assertFalse(e1.getTurnos().contains(actual));
	}

	@Test
	@DisplayName("Aprobar una solicitud que rompe el mínimo lanza excepción y no cambia nada")
	void aprobarRompeMinimo() {
		personal.remove(e3);
		Turno nuevo = turno(e1, 14, 18);
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, nuevo, "m");
		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> s.aprobar(personal));
		assertFalse(ex.getMessage().isBlank());
		assertEquals(EstadoSolicitud.PENDIENTE, s.getEstado());
		assertTrue(e1.getTurnos().contains(actual));
		assertFalse(e1.getTurnos().contains(nuevo));
	}

	@Test
	@DisplayName("No se puede aprobar dos veces")
	void aprobarDoble() {
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		s.aprobar(personal);
		assertThrows(IllegalStateException.class, () -> s.aprobar(personal));
	}

	@Test
	@DisplayName("No se puede aprobar si el empleado fue dado de baja")
	void aprobarEmpleadoInactivo() {
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		e1.darDeBaja();
		assertThrows(IllegalStateException.class, () -> s.aprobar(personal));
		assertEquals(EstadoSolicitud.PENDIENTE, s.getEstado());
	}

	@Test
	@DisplayName("Rechazar guarda la razón y cambia el estado")
	void rechazar() {
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		s.rechazar("Falta personal");
		assertEquals(EstadoSolicitud.RECHAZADA, s.getEstado());
		assertEquals("Falta personal", s.getRazonRechazo());
		assertTrue(e1.getTurnos().contains(actual));
	}

	@Test
	@DisplayName("Rechazar con razón nula o en blanco lanza excepción")
	void rechazarSinRazon() {
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		assertThrows(IllegalArgumentException.class, () -> s.rechazar(null));
		assertThrows(IllegalArgumentException.class, () -> s.rechazar("  "));
		assertEquals(EstadoSolicitud.PENDIENTE, s.getEstado());
	}

	@Test
	@DisplayName("Una solicitud resuelta no se puede rechazar ni aprobar de nuevo")
	void resolverDosVeces() {
		SolicitudCambioTurno s = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		s.rechazar("No");
		assertThrows(IllegalStateException.class, () -> s.rechazar("Otra"));
		assertThrows(IllegalStateException.class, () -> s.aprobar(personal));
	}

	@Test
	@DisplayName("Aprobar dos solicitudes seguidas revalida el mínimo con el estado actual")
	void revalidaAlAprobar() {
		SolicitudCambioTurno s1 = new SolicitudCambioTurno(e1, actual, turno(e1, 14, 18), "m");
		SolicitudCambioTurno s2 = new SolicitudCambioTurno(e2, e2.getTurnos().get(0), turno(e2, 14, 18), "m");
		assertTrue(s1.aprobar(personal));
		assertThrows(IllegalStateException.class, () -> s2.aprobar(personal));
		assertEquals(EstadoSolicitud.PENDIENTE, s2.getEstado());
	}
}