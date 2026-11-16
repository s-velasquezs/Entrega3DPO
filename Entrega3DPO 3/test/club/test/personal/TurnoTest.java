package club.test.personal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.modelo.personal.Empleado;
import club.modelo.personal.Turno;

public class TurnoTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private Empleado empleado;
	private Turno turno;

	@BeforeEach
	void setUp() {
		empleado = new Empleado("ana", "pw", "Ana");
		turno = new Turno(empleado, FECHA, LocalTime.of(8, 0), LocalTime.of(12, 0));
	}

	@Test
	@DisplayName("Un turno válido guarda sus datos")
	void guardaDatos() {
		assertSame(empleado, turno.getEmpleado());
		assertEquals(FECHA, turno.getFecha());
		assertEquals(LocalTime.of(8, 0), turno.getHoraInicio());
		assertEquals(LocalTime.of(12, 0), turno.getHoraFin());
	}

	@Test
	@DisplayName("Datos nulos lanzan excepción")
	void datosNulos() {
		assertThrows(IllegalArgumentException.class,
				() -> new Turno(null, FECHA, LocalTime.of(8, 0), LocalTime.of(12, 0)));
		assertThrows(IllegalArgumentException.class,
				() -> new Turno(empleado, null, LocalTime.of(8, 0), LocalTime.of(12, 0)));
		assertThrows(IllegalArgumentException.class, () -> new Turno(empleado, FECHA, null, LocalTime.of(12, 0)));
		assertThrows(IllegalArgumentException.class, () -> new Turno(empleado, FECHA, LocalTime.of(8, 0), null));
	}

	@Test
	@DisplayName("Inicio igual o posterior al fin lanza excepción")
	void horasInvertidas() {
		assertThrows(IllegalArgumentException.class,
				() -> new Turno(empleado, FECHA, LocalTime.of(12, 0), LocalTime.of(8, 0)));
		assertThrows(IllegalArgumentException.class,
				() -> new Turno(empleado, FECHA, LocalTime.of(8, 0), LocalTime.of(8, 0)));
	}

	@Test
	@DisplayName("Franja que se cruza en la mitad se sobrepone")
	void sobreposicionParcial() {
		assertTrue(turno.seSobrepone(LocalTime.of(10, 0), LocalTime.of(14, 0)));
		assertTrue(turno.seSobrepone(LocalTime.of(6, 0), LocalTime.of(9, 0)));
	}

	@Test
	@DisplayName("Franja contenida o que contiene se sobrepone")
	void sobreposicionContenida() {
		assertTrue(turno.seSobrepone(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		assertTrue(turno.seSobrepone(LocalTime.of(6, 0), LocalTime.of(14, 0)));
		assertTrue(turno.seSobrepone(LocalTime.of(8, 0), LocalTime.of(12, 0)));
	}

	@Test
	@DisplayName("Franjas contiguas no se sobreponen")
	void franjasContiguas() {
		assertFalse(turno.seSobrepone(LocalTime.of(12, 0), LocalTime.of(14, 0)));
		assertFalse(turno.seSobrepone(LocalTime.of(6, 0), LocalTime.of(8, 0)));
	}

	@Test
	@DisplayName("Franjas lejanas no se sobreponen")
	void franjasLejanas() {
		assertFalse(turno.seSobrepone(LocalTime.of(15, 0), LocalTime.of(18, 0)));
	}

	@Test
	@DisplayName("Con fecha: mismo día y franja cruzada se sobrepone")
	void sobreposicionConFecha() {
		assertTrue(turno.seSobrepone(FECHA, LocalTime.of(10, 0), LocalTime.of(14, 0)));
	}

	@Test
	@DisplayName("Con fecha distinta no se sobrepone")
	void fechaDistinta() {
		assertFalse(turno.seSobrepone(FECHA.plusDays(1), LocalTime.of(8, 0), LocalTime.of(12, 0)));
	}

	@Test
	@DisplayName("Valores nulos al consultar sobreposición lanzan excepción")
	void sobreposicionNulos() {
		assertThrows(IllegalArgumentException.class, () -> turno.seSobrepone(null, LocalTime.of(8, 0), LocalTime.of(9, 0)));
		assertThrows(IllegalArgumentException.class, () -> turno.seSobrepone(FECHA, null, LocalTime.of(9, 0)));
		assertThrows(IllegalArgumentException.class, () -> turno.seSobrepone(LocalTime.of(8, 0), null));
	}

	@Test
	@DisplayName("Los setters válidos actualizan el turno")
	void settersValidos() {
		turno.setFecha(FECHA.plusDays(2));
		turno.setHoraFin(LocalTime.of(13, 0));
		turno.setHoraInicio(LocalTime.of(9, 0));
		Empleado otro = new Empleado("luis", "pw", "Luis");
		turno.setEmpleado(otro);
		assertEquals(FECHA.plusDays(2), turno.getFecha());
		assertEquals(LocalTime.of(13, 0), turno.getHoraFin());
		assertEquals(LocalTime.of(9, 0), turno.getHoraInicio());
		assertSame(otro, turno.getEmpleado());
	}

	@Test
	@DisplayName("Los setters inválidos lanzan excepción y no cambian el turno")
	void settersInvalidos() {
		assertThrows(IllegalArgumentException.class, () -> turno.setFecha(null));
		assertThrows(IllegalArgumentException.class, () -> turno.setEmpleado(null));
		assertThrows(IllegalArgumentException.class, () -> turno.setHoraInicio(null));
		assertThrows(IllegalArgumentException.class, () -> turno.setHoraInicio(LocalTime.of(13, 0)));
		assertThrows(IllegalArgumentException.class, () -> turno.setHoraFin(LocalTime.of(7, 0)));
		assertEquals(FECHA, turno.getFecha());
		assertEquals(LocalTime.of(8, 0), turno.getHoraInicio());
		assertEquals(LocalTime.of(12, 0), turno.getHoraFin());
	}

	@Test
	@DisplayName("toString muestra fecha y franja")
	void toStringFormato() {
		assertEquals("2026-11-20 08:00-12:00", turno.toString());
	}
}