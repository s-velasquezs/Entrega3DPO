package club.test.usuarios;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.modelo.usuarios.RankingELO;

public class RankingELOTest {

	private RankingELO a;
	private RankingELO b;

	@BeforeEach
	void setUp() {
		a = new RankingELO();
		b = new RankingELO();
	}

	@Test
	@DisplayName("Un ranking nuevo empieza en 1000 y sin partidos")
	void rankingNuevo() {
		assertEquals(1000, a.getPuntaje());
		assertEquals(0, a.getPartidosJugados());
		assertFalse(a.tieneRanking());
		assertTrue(a.getHistorial().isEmpty());
	}

	@Test
	@DisplayName("Constructor con valores personalizados")
	void constructorPersonalizado() {
		RankingELO r = new RankingELO(1200, 5);
		assertEquals(1200, r.getPuntaje());
		assertEquals(5, r.getPartidosJugados());
		assertTrue(r.tieneRanking());
	}

	@Test
	@DisplayName("Puntaje negativo lanza excepción")
	void puntajeNegativo() {
		assertThrows(IllegalArgumentException.class, () -> new RankingELO(-1, 0));
	}

	@Test
	@DisplayName("Partidos negativos lanza excepción")
	void partidosNegativos() {
		assertThrows(IllegalArgumentException.class, () -> new RankingELO(1000, -1));
	}

	@Test
	@DisplayName("Victoria entre iguales suma 16 al ganador y resta 16 al perdedor")
	void victoriaEntreIguales() {
		a.actualizar(b, true);
		assertEquals(1016, a.getPuntaje());
		assertEquals(984, b.getPuntaje());
	}

	@Test
	@DisplayName("Derrota entre iguales resta 16 y suma 16 al rival")
	void derrotaEntreIguales() {
		a.actualizar(b, false);
		assertEquals(984, a.getPuntaje());
		assertEquals(1016, b.getPuntaje());
	}

	@Test
	@DisplayName("La suma total de puntos se conserva")
	void sumaSeConserva() {
		RankingELO fuerte = new RankingELO(1400, 0);
		RankingELO debil = new RankingELO(1000, 0);
		int antes = fuerte.getPuntaje() + debil.getPuntaje();
		fuerte.actualizar(debil, false);
		assertEquals(antes, fuerte.getPuntaje() + debil.getPuntaje());
	}

	@Test
	@DisplayName("Ambos jugadores suman un partido y quedan con ranking")
	void ambosSumanPartido() {
		a.actualizar(b, true);
		assertEquals(1, a.getPartidosJugados());
		assertEquals(1, b.getPartidosJugados());
		assertTrue(a.tieneRanking());
		assertTrue(b.tieneRanking());
	}

	@Test
	@DisplayName("El favorito que gana suma poco")
	void favoritoGanaPoco() {
		RankingELO fuerte = new RankingELO(1400, 0);
		RankingELO debil = new RankingELO(1000, 0);
		fuerte.actualizar(debil, true);
		int ganancia = fuerte.getPuntaje() - 1400;
		assertTrue(ganancia >= 0 && ganancia < 16);
	}

	@Test
	@DisplayName("El débil que gana suma mucho")
	void sorpresaGanaMucho() {
		RankingELO fuerte = new RankingELO(1400, 0);
		RankingELO debil = new RankingELO(1000, 0);
		debil.actualizar(fuerte, true);
		int ganancia = debil.getPuntaje() - 1000;
		assertTrue(ganancia > 16);
	}

	@Test
	@DisplayName("El historial guarda el puntaje después de cada partido")
	void historialRegistraPuntajes() {
		a.actualizar(b, true);
		a.actualizar(b, false);
		List<Integer> h = a.getHistorial();
		assertEquals(2, h.size());
		assertEquals(1016, h.get(0));
		assertEquals(a.getPuntaje(), h.get(1));
		assertEquals(2, b.getHistorial().size());
	}

	@Test
	@DisplayName("El historial devuelto es una copia")
	void historialEsCopia() {
		a.actualizar(b, true);
		a.getHistorial().clear();
		assertEquals(1, a.getHistorial().size());
	}

	@Test
	@DisplayName("Actualizar contra un rival nulo lanza excepción")
	void rivalNulo() {
		assertThrows(IllegalArgumentException.class, () -> a.actualizar(null, true));
	}

	@Test
	@DisplayName("Un ranking no puede jugar contra sí mismo")
	void contraSiMismo() {
		assertThrows(IllegalArgumentException.class, () -> a.actualizar(a, true));
	}

	@Test
	@DisplayName("Un fallo no modifica el estado")
	void falloNoModifica() {
		assertThrows(IllegalArgumentException.class, () -> a.actualizar(null, true));
		assertEquals(1000, a.getPuntaje());
		assertEquals(0, a.getPartidosJugados());
	}

	@Test
	@DisplayName("El puntaje nunca baja de cero")
	void puntajeNoNegativo() {
		RankingELO bajo = new RankingELO(5, 0);
		bajo.actualizar(b, false);
		assertTrue(bajo.getPuntaje() >= 0);
	}
}