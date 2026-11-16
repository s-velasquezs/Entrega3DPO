package club.test.usuarios;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.test.TestFixtures;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.enums.Nivel;
import club.modelo.usuarios.InscripcionIndividual;
import club.modelo.usuarios.JugadorIndividual;

public class InscripcionIndividualTest {

	private DeporteIndividual tenis;
	private JugadorIndividual jugador;
	private InscripcionIndividual inscripcion;

	@BeforeEach
	void setUp() {
		tenis = TestFixtures.tenis();
		jugador = new JugadorIndividual("ana", "pw", "Ana", 20, false);
		inscripcion = new InscripcionIndividual(jugador, tenis, Nivel.PRINCIPIANTE, LocalDate.of(2026, 2, 1));
	}

	@Test
	@DisplayName("Una inscripción nueva está activa con ranking inicial")
	void inscripcionNueva() {
		assertSame(jugador, inscripcion.getJugador());
		assertSame(tenis, inscripcion.getDeporte());
		assertEquals(Nivel.PRINCIPIANTE, inscripcion.getNivel());
		assertEquals(LocalDate.of(2026, 2, 1), inscripcion.getFechaInscripcion());
		assertTrue(inscripcion.estaActiva());
		assertEquals(1000, inscripcion.getRankingELO().getPuntaje());
	}

	@Test
	@DisplayName("El constructor sin fecha usa la fecha actual")
	void fechaPorDefecto() {
		assertEquals(LocalDate.now(),
				new InscripcionIndividual(jugador, tenis, Nivel.AVANZADO).getFechaInscripcion());
	}

	@Test
	@DisplayName("Datos nulos lanzan excepción")
	void nulos() {
		assertThrows(IllegalArgumentException.class, () -> new InscripcionIndividual(null, tenis, Nivel.AVANZADO));
		assertThrows(IllegalArgumentException.class, () -> new InscripcionIndividual(jugador, null, Nivel.AVANZADO));
		assertThrows(IllegalArgumentException.class, () -> new InscripcionIndividual(jugador, tenis, null));
		assertThrows(IllegalArgumentException.class,
				() -> new InscripcionIndividual(jugador, tenis, Nivel.AVANZADO, null));
	}

	@Test
	@DisplayName("Cambiar el nivel")
	void cambiarNivel() {
		inscripcion.cambiarNivel(Nivel.AVANZADO);
		assertEquals(Nivel.AVANZADO, inscripcion.getNivel());
	}

	@Test
	@DisplayName("Cambiar a un nivel nulo lanza excepción")
	void cambiarNivelNulo() {
		assertThrows(IllegalArgumentException.class, () -> inscripcion.cambiarNivel(null));
		assertEquals(Nivel.PRINCIPIANTE, inscripcion.getNivel());
	}

	@Test
	@DisplayName("Dar de baja desactiva la inscripción")
	void darDeBaja() {
		inscripcion.darDeBaja();
		assertFalse(inscripcion.estaActiva());
	}

	@Test
	@DisplayName("Cambiar el nivel de una inscripción inactiva lanza excepción")
	void cambiarNivelInactiva() {
		inscripcion.darDeBaja();
		assertThrows(IllegalStateException.class, () -> inscripcion.cambiarNivel(Nivel.AVANZADO));
	}
}