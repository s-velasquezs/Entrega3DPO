package club.test.personal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.modelo.enums.EstadoSolicitud;
import club.modelo.personal.Empleado;
import club.modelo.personal.Entrenador;
import club.modelo.personal.Fisioterapeuta;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.personal.Turno;

public class EmpleadoTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private Empleado empleado;

	private Turno turno(Empleado e, LocalDate fecha, int ini, int fin) {
		return new Turno(e, fecha, LocalTime.of(ini, 0), LocalTime.of(fin, 0));
	}

	@BeforeEach
	void setUp() {
		empleado = new Empleado("val", "pw", "Valentina");
	}

	@Test
	@DisplayName("Un empleado nuevo está activo y tiene código de descuento")
	void empleadoNuevo() {
		assertEquals("Valentina", empleado.getNombre());
		assertTrue(empleado.estaActivo());
		assertEquals("DESC-VAL", empleado.getCodigoDescuento());
		assertTrue(empleado.puedeComprarConDescuento());
		assertNotNull(empleado.getProgramaFidelidad());
		assertTrue(empleado.getTurnos().isEmpty());
		assertTrue(empleado.getSolicitudes().isEmpty());
		assertEquals("Valentina (val)", empleado.toString());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Un nombre inválido lanza excepción")
	void nombreInvalido(String nombre) {
		assertThrows(IllegalArgumentException.class, () -> new Empleado("val", "pw", nombre));
	}

	@Test
	@DisplayName("Un empleado dado de baja queda inactivo y sin descuento")
	void darDeBaja() {
		empleado.darDeBaja();
		assertFalse(empleado.estaActivo());
		assertFalse(empleado.puedeComprarConDescuento());
	}

	@Test
	@DisplayName("Dar de baja dos veces lanza excepción")
	void bajaDoble() {
		empleado.darDeBaja();
		assertThrows(IllegalStateException.class, () -> empleado.darDeBaja());
	}

	@Test
	@DisplayName("Agregar un turno válido")
	void agregarTurno() {
		Turno t = turno(empleado, FECHA, 8, 12);
		assertTrue(empleado.agregarTurno(t));
		assertEquals(1, empleado.getTurnos().size());
	}

	@Test
	@DisplayName("Un turno nulo lanza excepción")
	void turnoNulo() {
		assertThrows(IllegalArgumentException.class, () -> empleado.agregarTurno(null));
	}

	@Test
	@DisplayName("Un turno de otro empleado lanza excepción")
	void turnoDeOtro() {
		Empleado otro = new Empleado("luis", "pw", "Luis");
		assertThrows(IllegalArgumentException.class, () -> empleado.agregarTurno(turno(otro, FECHA, 8, 12)));
	}

	@Test
	@DisplayName("Un turno que se cruza se rechaza")
	void turnoCruzado() {
		empleado.agregarTurno(turno(empleado, FECHA, 8, 12));
		assertThrows(IllegalStateException.class, () -> empleado.agregarTurno(turno(empleado, FECHA, 10, 14)));
		assertEquals(1, empleado.getTurnos().size());
	}

	@Test
	@DisplayName("Turnos contiguos o de otro día se aceptan")
	void turnosCompatibles() {
		assertTrue(empleado.agregarTurno(turno(empleado, FECHA, 8, 12)));
		assertTrue(empleado.agregarTurno(turno(empleado, FECHA, 12, 16)));
		assertTrue(empleado.agregarTurno(turno(empleado, FECHA.plusDays(1), 8, 12)));
		assertEquals(3, empleado.getTurnos().size());
	}

	@Test
	@DisplayName("Un empleado dado de baja no puede recibir turnos")
	void turnoEmpleadoInactivo() {
		empleado.darDeBaja();
		assertThrows(IllegalStateException.class, () -> empleado.agregarTurno(turno(empleado, FECHA, 8, 12)));
	}

	@Test
	@DisplayName("Consultar turnos los devuelve ordenados por fecha y hora")
	void consultarOrdenados() {
		Turno t3 = turno(empleado, FECHA.plusDays(1), 8, 10);
		Turno t2 = turno(empleado, FECHA, 14, 16);
		Turno t1 = turno(empleado, FECHA, 8, 10);
		empleado.agregarTurno(t3);
		empleado.agregarTurno(t2);
		empleado.agregarTurno(t1);
		List<Turno> lista = empleado.consultarTurnos(FECHA, FECHA.plusDays(1));
		assertEquals(List.of(t1, t2, t3), lista);
	}

	@Test
	@DisplayName("Consultar turnos fuera del periodo devuelve vacío")
	void consultarVacio() {
		empleado.agregarTurno(turno(empleado, FECHA, 8, 10));
		assertTrue(empleado.consultarTurnos(FECHA.plusDays(5), FECHA.plusDays(9)).isEmpty());
	}

	@Test
	@DisplayName("Consultar turnos incluye los extremos del periodo")
	void consultarExtremos() {
		empleado.agregarTurno(turno(empleado, FECHA, 8, 10));
		assertEquals(1, empleado.consultarTurnos(FECHA, FECHA).size());
	}

	@Test
	@DisplayName("Consultar turnos con fechas inválidas lanza excepción")
	void consultarInvalido() {
		assertThrows(IllegalArgumentException.class, () -> empleado.consultarTurnos(null, FECHA));
		assertThrows(IllegalArgumentException.class, () -> empleado.consultarTurnos(FECHA, null));
		assertThrows(IllegalArgumentException.class, () -> empleado.consultarTurnos(FECHA.plusDays(1), FECHA));
	}

	@Test
	@DisplayName("tieneTurnoEn detecta cruces y descarta fechas distintas")
	void tieneTurnoEn() {
		empleado.agregarTurno(turno(empleado, FECHA, 8, 12));
		assertTrue(empleado.tieneTurnoEn(FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0)));
		assertFalse(empleado.tieneTurnoEn(FECHA, LocalTime.of(12, 0), LocalTime.of(14, 0)));
		assertFalse(empleado.tieneTurnoEn(FECHA.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0)));
	}

	@Test
	@DisplayName("tieneTurnoEn con nulos lanza excepción")
	void tieneTurnoEnNulos() {
		assertThrows(IllegalArgumentException.class,
				() -> empleado.tieneTurnoEn(null, LocalTime.of(9, 0), LocalTime.of(10, 0)));
	}

	@Test
	@DisplayName("Reemplazar un turno por otro libre")
	void reemplazarTurno() {
		Turno actual = turno(empleado, FECHA, 8, 12);
		Turno nuevo = turno(empleado, FECHA, 14, 18);
		empleado.agregarTurno(actual);
		assertTrue(empleado.reemplazarTurno(actual, nuevo));
		assertTrue(empleado.getTurnos().contains(nuevo));
		assertFalse(empleado.getTurnos().contains(actual));
	}

	@Test
	@DisplayName("Reemplazar por un turno que se cruza con otro lanza excepción")
	void reemplazarCruzado() {
		Turno actual = turno(empleado, FECHA, 8, 12);
		Turno otro = turno(empleado, FECHA, 14, 18);
		empleado.agregarTurno(actual);
		empleado.agregarTurno(otro);
		assertThrows(IllegalStateException.class,
				() -> empleado.reemplazarTurno(actual, turno(empleado, FECHA, 13, 15)));
		assertTrue(empleado.getTurnos().contains(actual));
	}

	@Test
	@DisplayName("Reemplazar un turno solapado consigo mismo es válido")
	void reemplazarSolapadoConsigoMismo() {
		Turno actual = turno(empleado, FECHA, 8, 12);
		empleado.agregarTurno(actual);
		assertTrue(empleado.reemplazarTurno(actual, turno(empleado, FECHA, 9, 13)));
	}

	@Test
	@DisplayName("Reemplazar con argumentos inválidos lanza excepción")
	void reemplazarInvalido() {
		Turno actual = turno(empleado, FECHA, 8, 12);
		empleado.agregarTurno(actual);
		Empleado otro = new Empleado("luis", "pw", "Luis");
		assertThrows(IllegalArgumentException.class, () -> empleado.reemplazarTurno(null, actual));
		assertThrows(IllegalArgumentException.class, () -> empleado.reemplazarTurno(actual, null));
		assertThrows(IllegalArgumentException.class,
				() -> empleado.reemplazarTurno(actual, turno(otro, FECHA, 14, 16)));
		assertThrows(IllegalArgumentException.class, () -> empleado.reemplazarTurno(turno(empleado, FECHA, 1, 2),
				turno(empleado, FECHA, 14, 16)));
	}

	@Test
	@DisplayName("La lista de turnos devuelta es una copia")
	void turnosCopia() {
		empleado.agregarTurno(turno(empleado, FECHA, 8, 12));
		empleado.getTurnos().clear();
		assertEquals(1, empleado.getTurnos().size());
	}

	private List<Empleado> personalCompleto(Entrenador e1) {
		Entrenador e2 = new Entrenador("e2", "pw", "E2");
		Entrenador e3 = new Entrenador("e3", "pw", "E3");
		Fisioterapeuta f = new Fisioterapeuta("f1", "pw", "F1");
		List<Empleado> personal = new ArrayList<Empleado>();
		personal.add(e1);
		personal.add(e2);
		personal.add(e3);
		personal.add(f);
		for (Empleado e : personal) {
			e.agregarTurno(turno(e, FECHA, 8, 12));
		}
		return personal;
	}

	@Test
	@DisplayName("Una solicitud que conserva el mínimo queda pendiente")
	void solicitudValida() {
		Entrenador e1 = new Entrenador("e1", "pw", "E1");
		List<Empleado> personal = personalCompleto(e1);
		Turno actual = e1.getTurnos().get(0);
		Turno nuevo = turno(e1, FECHA, 14, 18);
		SolicitudCambioTurno s = e1.solicitarCambioTurno(actual, nuevo, "Cita médica", personal);
		assertEquals(EstadoSolicitud.PENDIENTE, s.getEstado());
		assertEquals(1, e1.getSolicitudes().size());
		assertTrue(e1.getTurnos().contains(actual));
	}

	@Test
	@DisplayName("Una solicitud que rompe el mínimo se rechaza y no se registra")
	void solicitudRompeMinimo() {
		Entrenador e1 = new Entrenador("e1", "pw", "E1");
		Entrenador e2 = new Entrenador("e2", "pw", "E2");
		Fisioterapeuta f = new Fisioterapeuta("f1", "pw", "F1");
		List<Empleado> personal = List.of(e1, e2, f);
		for (Empleado e : personal) {
			e.agregarTurno(turno(e, FECHA, 8, 12));
		}
		Turno actual = e1.getTurnos().get(0);
		Turno nuevo = turno(e1, FECHA, 14, 18);
		assertThrows(IllegalStateException.class, () -> e1.solicitarCambioTurno(actual, nuevo, "Motivo", personal));
		assertTrue(e1.getSolicitudes().isEmpty());
	}

	@Test
	@DisplayName("El fisioterapeuta único no puede pedir un cambio que deja la franja sin fisioterapeuta")
	void solicitudFisioUnico() {
		Entrenador e1 = new Entrenador("e1", "pw", "E1");
		Entrenador e2 = new Entrenador("e2", "pw", "E2");
		Fisioterapeuta f = new Fisioterapeuta("f1", "pw", "F1");
		List<Empleado> personal = List.of(e1, e2, f);
		for (Empleado e : personal) {
			e.agregarTurno(turno(e, FECHA, 8, 12));
		}
		Turno actual = f.getTurnos().get(0);
		Turno nuevo = turno(f, FECHA, 14, 18);
		assertThrows(IllegalStateException.class, () -> f.solicitarCambioTurno(actual, nuevo, "Motivo", personal));
	}

	@Test
	@DisplayName("Solicitud con un empleado de baja lanza excepción")
	void solicitudEmpleadoInactivo() {
		Entrenador e1 = new Entrenador("e1", "pw", "E1");
		List<Empleado> personal = personalCompleto(e1);
		Turno actual = e1.getTurnos().get(0);
		Turno nuevo = turno(e1, FECHA, 14, 18);
		e1.darDeBaja();
		assertThrows(IllegalStateException.class, () -> e1.solicitarCambioTurno(actual, nuevo, "Motivo", personal));
	}

	@Test
	@DisplayName("Solicitud con argumentos nulos o motivo inválido lanza excepción")
	void solicitudArgumentosInvalidos() {
		Entrenador e1 = new Entrenador("e1", "pw", "E1");
		List<Empleado> personal = personalCompleto(e1);
		Turno actual = e1.getTurnos().get(0);
		Turno nuevo = turno(e1, FECHA, 14, 18);
		assertThrows(IllegalArgumentException.class, () -> e1.solicitarCambioTurno(null, nuevo, "m", personal));
		assertThrows(IllegalArgumentException.class, () -> e1.solicitarCambioTurno(actual, null, "m", personal));
		assertThrows(IllegalArgumentException.class, () -> e1.solicitarCambioTurno(actual, nuevo, "m", null));
		assertThrows(IllegalArgumentException.class, () -> e1.solicitarCambioTurno(actual, nuevo, null, personal));
		assertThrows(IllegalArgumentException.class, () -> e1.solicitarCambioTurno(actual, nuevo, "  ", personal));
	}

	@Test
	@DisplayName("El turno actual debe ser del empleado y el nuevo también")
	void solicitudTurnosAjenos() {
		Entrenador e1 = new Entrenador("e1", "pw", "E1");
		List<Empleado> personal = personalCompleto(e1);
		Turno ajeno = personal.get(1).getTurnos().get(0);
		Turno nuevo = turno(e1, FECHA, 14, 18);
		Turno nuevoAjeno = turno(personal.get(1), FECHA, 14, 18);
		Turno actual = e1.getTurnos().get(0);
		assertThrows(IllegalArgumentException.class, () -> e1.solicitarCambioTurno(ajeno, nuevo, "m", personal));
		assertThrows(IllegalArgumentException.class, () -> e1.solicitarCambioTurno(actual, nuevoAjeno, "m", personal));
	}

	@Test
	@DisplayName("No se permiten dos solicitudes pendientes para el mismo turno")
	void solicitudDuplicada() {
		Entrenador e1 = new Entrenador("e1", "pw", "E1");
		List<Empleado> personal = personalCompleto(e1);
		Turno actual = e1.getTurnos().get(0);
		e1.solicitarCambioTurno(actual, turno(e1, FECHA, 14, 18), "m", personal);
		assertThrows(IllegalStateException.class,
				() -> e1.solicitarCambioTurno(actual, turno(e1, FECHA, 15, 19), "m", personal));
		assertEquals(1, e1.getSolicitudes().size());
	}

	@Test
	@DisplayName("El turno nuevo no puede cruzarse con otro turno del empleado")
	void solicitudNuevoCruzado() {
		Entrenador e1 = new Entrenador("e1", "pw", "E1");
		List<Empleado> personal = personalCompleto(e1);
		e1.agregarTurno(turno(e1, FECHA, 14, 18));
		Turno actual = e1.getTurnos().get(0);
		assertThrows(IllegalStateException.class,
				() -> e1.solicitarCambioTurno(actual, turno(e1, FECHA, 16, 20), "m", personal));
	}

	@Test
	@DisplayName("La lista de solicitudes devuelta es una copia")
	void solicitudesCopia() {
		Entrenador e1 = new Entrenador("e1", "pw", "E1");
		List<Empleado> personal = personalCompleto(e1);
		e1.solicitarCambioTurno(e1.getTurnos().get(0), turno(e1, FECHA, 14, 18), "m", personal);
		e1.getSolicitudes().clear();
		assertEquals(1, e1.getSolicitudes().size());
	}
}