package club.test.partidos;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.test.TestFixtures;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.enums.ModalidadJuego;
import club.modelo.enums.Nivel;
import club.modelo.partidos.PartidoPractica;
import club.modelo.usuarios.JugadorIndividual;

public class PartidoPracticaTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private DeporteIndividual tenis;
	private JugadorIndividual a;
	private JugadorIndividual b;

	@BeforeEach
	void setUp() {
		tenis = TestFixtures.tenis();
		a = new JugadorIndividual("a", "pw", "A", 20, false);
		b = new JugadorIndividual("b", "pw", "B", 20, false);
		a.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
		b.inscribirseDeporte(tenis, Nivel.AVANZADO);
	}

	@Test
	@DisplayName("Un jugador nulo lanza excepción")
	void jugadoresNulos() {
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoPractica(FECHA, tenis, null, b, ModalidadJuego.SENCILLOS, null));
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoPractica(FECHA, tenis, a, null, ModalidadJuego.SENCILLOS, null));
	}

	@Test
	@DisplayName("Sin modalidad o sin reserva lanza excepción")
	void modalidadOReservaNulas() {
		assertThrows(IllegalArgumentException.class, () -> new PartidoPractica(FECHA, tenis, a, b, null, null));
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoPractica(FECHA, tenis, a, b, ModalidadJuego.SENCILLOS, null));
	}

	@Test
	@DisplayName("Un jugador no puede jugar contra sí mismo")
	void contraSiMismo() {
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoPractica(FECHA, tenis, a, a, ModalidadJuego.SENCILLOS, null));
	}
}