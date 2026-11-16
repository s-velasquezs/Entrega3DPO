package club.test.usuarios;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import club.test.TestFixtures;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.usuarios.Inscripcion;
import club.modelo.usuarios.JugadorEquipo;

public class InscripcionTest {

	private Equipo equipo;
	private JugadorEquipo jugador;
	private Inscripcion inscripcion;

	@BeforeEach
	void setUp() {
		equipo = TestFixtures.equipo("Halcones", TestFixtures.futbol(), Categoria.MAYORES);
		jugador = new JugadorEquipo("juan", "pw", "Juan", 20);
		inscripcion = new Inscripcion(jugador, equipo, "Portero", 1, LocalDate.of(2026, 1, 1));
	}

	@Test
	@DisplayName("Una inscripción nueva está activa y guarda sus datos")
	void inscripcionNueva() {
		assertSame(jugador, inscripcion.getJugador());
		assertSame(equipo, inscripcion.getEquipo());
		assertEquals("Portero", inscripcion.getPosicion());
		assertEquals(1, inscripcion.getNumeroCamiseta());
		assertEquals(LocalDate.of(2026, 1, 1), inscripcion.getFechaInscripcion());
		assertTrue(inscripcion.estaActiva());
		assertTrue(inscripcion.tienePosicion());
		assertTrue(inscripcion.tieneNumero());
	}

	@Test
	@DisplayName("El constructor sin fecha usa la fecha actual")
	void fechaPorDefecto() {
		assertEquals(LocalDate.now(), new Inscripcion(jugador, equipo, "Portero", 1).getFechaInscripcion());
	}

	@Test
	@DisplayName("Jugador, equipo o fecha nulos lanzan excepción")
	void nulos() {
		assertThrows(IllegalArgumentException.class, () -> new Inscripcion(null, equipo, "P", 1));
		assertThrows(IllegalArgumentException.class, () -> new Inscripcion(jugador, null, "P", 1));
		assertThrows(IllegalArgumentException.class, () -> new Inscripcion(jugador, equipo, "P", 1, null));
	}

	@Test
	@DisplayName("Un número negativo lanza excepción")
	void numeroNegativo() {
		assertThrows(IllegalArgumentException.class, () -> new Inscripcion(jugador, equipo, "P", -1));
	}

	@Test
	@DisplayName("Posición nula o en blanco y número cero quedan pendientes")
	void pendientes() {
		Inscripcion nula = new Inscripcion(jugador, equipo, null, 0);
		Inscripcion blanca = new Inscripcion(jugador, equipo, "   ", 0);
		assertNull(nula.getPosicion());
		assertNull(blanca.getPosicion());
		assertFalse(nula.tienePosicion());
		assertFalse(nula.tieneNumero());
	}

	@Test
	@DisplayName("Asignar posición válida")
	void asignarPosicion() {
		Inscripcion pendiente = new Inscripcion(jugador, equipo, null, 0);
		pendiente.asignarPosicion("Defensa");
		assertEquals("Defensa", pendiente.getPosicion());
		assertTrue(pendiente.tienePosicion());
	}

	@Test
	@DisplayName("Asignar posición nula o en blanco lanza excepción")
	void asignarPosicionInvalida() {
		assertThrows(IllegalArgumentException.class, () -> inscripcion.asignarPosicion(null));
		assertThrows(IllegalArgumentException.class, () -> inscripcion.asignarPosicion("  "));
		assertEquals("Portero", inscripcion.getPosicion());
	}

	@Test
	@DisplayName("Asignar un número libre")
	void asignarNumeroLibre() {
		assertTrue(inscripcion.asignarNumero(9));
		assertEquals(9, inscripcion.getNumeroCamiseta());
	}

	@Test
	@DisplayName("Asignar el mismo número que ya se tiene es válido")
	void asignarMismoNumero() {
		assertTrue(inscripcion.asignarNumero(1));
	}

	@Test
	@DisplayName("Asignar un número tomado por otro jugador se rechaza")
	void asignarNumeroTomado() {
		TestFixtures.poblar(equipo, 1, "otro");
		Inscripcion mia = new Inscripcion(jugador, equipo, "Portero", 0);
		assertThrows(IllegalStateException.class, () -> mia.asignarNumero(1));
		assertEquals(0, mia.getNumeroCamiseta());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -1, -9 })
	@DisplayName("Asignar un número no positivo lanza excepción")
	void asignarNumeroInvalido(int n) {
		assertThrows(IllegalArgumentException.class, () -> inscripcion.asignarNumero(n));
	}

	@Test
	@DisplayName("Dar de baja desactiva la inscripción")
	void darDeBaja() {
		inscripcion.darDeBaja();
		assertFalse(inscripcion.estaActiva());
	}

	@Test
	@DisplayName("Una inscripción inactiva no admite cambios")
	void inactivaNoAdmiteCambios() {
		inscripcion.darDeBaja();
		assertThrows(IllegalStateException.class, () -> inscripcion.asignarNumero(5));
		assertThrows(IllegalStateException.class, () -> inscripcion.asignarPosicion("Defensa"));
	}
}
