package club.test.usuarios;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.test.TestFixtures;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.enums.Nivel;
import club.modelo.usuarios.JugadorIndividual;
import club.modelo.usuarios.RankingELO;

public class JugadorIndividualTest {

	private DeporteIndividual tenis;
	private DeporteIndividual padel;
	private JugadorIndividual ana;

	private JugadorIndividual jugador(String username, Nivel nivel, boolean entrenador) {
		JugadorIndividual j = new JugadorIndividual(username, "pw", username, 20, entrenador);
		j.inscribirseDeporte(tenis, nivel);
		return j;
	}

	@BeforeEach
	void setUp() {
		tenis = TestFixtures.tenis();
		padel = TestFixtures.padel();
		ana = new JugadorIndividual("ana", "pw", "Ana", 20, false);
	}

	@Test
	@DisplayName("Un jugador individual nuevo no tiene inscripciones")
	void jugadorNuevo() {
		assertFalse(ana.esTambienEntrenador());
		assertTrue(ana.getInscripciones().isEmpty());
		assertTrue(ana.estaActivo());
		assertFalse(ana.estaInscritoEn(tenis));
	}

	@Test
	@DisplayName("La bandera de entrenador se conserva")
	void banderaEntrenador() {
		assertTrue(new JugadorIndividual("e", "pw", "E", 30, true).esTambienEntrenador());
	}

	@Test
	@DisplayName("Inscribirse a un deporte con nivel")
	void inscripcionValida() {
		assertTrue(ana.inscribirseDeporte(tenis, Nivel.INTERMEDIO));
		assertTrue(ana.estaInscritoEn(tenis));
		assertEquals(Nivel.INTERMEDIO, ana.getNivel(tenis));
		assertNotNull(ana.getInscripcion(tenis));
	}

	@Test
	@DisplayName("Inscribirse a dos deportes deja ambas activas por separado")
	void dosDeportes() {
		ana.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
		ana.inscribirseDeporte(padel, Nivel.AVANZADO);
		assertEquals(Nivel.PRINCIPIANTE, ana.getNivel(tenis));
		assertEquals(Nivel.AVANZADO, ana.getNivel(padel));
		assertEquals(2, ana.getInscripciones().size());
	}

	@Test
	@DisplayName("Inscribirse dos veces al mismo deporte se rechaza")
	void inscripcionDoble() {
		ana.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
		assertThrows(IllegalStateException.class, () -> ana.inscribirseDeporte(tenis, Nivel.AVANZADO));
		assertEquals(Nivel.PRINCIPIANTE, ana.getNivel(tenis));
		assertEquals(1, ana.getInscripciones().size());
	}

	@Test
	@DisplayName("Deporte o nivel nulos lanzan excepción")
	void inscripcionNulos() {
		assertThrows(IllegalArgumentException.class, () -> ana.inscribirseDeporte(null, Nivel.PRINCIPIANTE));
		assertThrows(IllegalArgumentException.class, () -> ana.inscribirseDeporte(tenis, null));
	}

	@Test
	@DisplayName("Un jugador dado de baja no puede inscribirse")
	void inscripcionInactivo() {
		ana.darDeBaja();
		assertThrows(IllegalStateException.class, () -> ana.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE));
	}

	@Test
	@DisplayName("Sin inscripción no hay nivel ni ranking")
	void sinInscripcion() {
		assertNull(ana.getNivel(tenis));
		assertNull(ana.getRanking(tenis));
		assertNull(ana.getInscripcion(tenis));
	}

	@Test
	@DisplayName("El ranking inicial existe pero aún no cuenta como ranking")
	void rankingInicial() {
		ana.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
		RankingELO r = ana.getRanking(tenis);
		assertNotNull(r);
		assertEquals(1000, r.getPuntaje());
		assertFalse(r.tieneRanking());
	}

	@Test
	@DisplayName("El ranking es independiente por deporte")
	void rankingPorDeporte() {
		ana.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
		ana.inscribirseDeporte(padel, Nivel.PRINCIPIANTE);
		assertNotSame(ana.getRanking(tenis), ana.getRanking(padel));
	}

	@Test
	@DisplayName("Niveles iguales son rivales válidos")
	void rivalMismoNivel() {
		JugadorIndividual a = jugador("a", Nivel.INTERMEDIO, false);
		JugadorIndividual b = jugador("b", Nivel.INTERMEDIO, false);
		assertTrue(a.esRivalValido(b, tenis));
	}

	@Test
	@DisplayName("Niveles adyacentes son rivales válidos en ambos sentidos")
	void rivalAdyacente() {
		JugadorIndividual p = jugador("p", Nivel.PRINCIPIANTE, false);
		JugadorIndividual i = jugador("i", Nivel.INTERMEDIO, false);
		JugadorIndividual av = jugador("av", Nivel.AVANZADO, false);
		assertTrue(p.esRivalValido(i, tenis));
		assertTrue(i.esRivalValido(p, tenis));
		assertTrue(i.esRivalValido(av, tenis));
		assertTrue(av.esRivalValido(i, tenis));
	}

	@Test
	@DisplayName("Niveles no adyacentes no son rivales válidos")
	void rivalNoAdyacente() {
		JugadorIndividual p = jugador("p", Nivel.PRINCIPIANTE, false);
		JugadorIndividual av = jugador("av", Nivel.AVANZADO, false);
		assertFalse(p.esRivalValido(av, tenis));
		assertFalse(av.esRivalValido(p, tenis));
	}

	@Test
	@DisplayName("Si uno es también entrenador se permite cualquier nivel")
	void rivalEntrenador() {
		JugadorIndividual p = jugador("p", Nivel.PRINCIPIANTE, false);
		JugadorIndividual av = jugador("av", Nivel.AVANZADO, true);
		assertTrue(p.esRivalValido(av, tenis));
		assertTrue(av.esRivalValido(p, tenis));
	}

	@Test
	@DisplayName("Un jugador no es rival de sí mismo")
	void rivalYoMismo() {
		JugadorIndividual a = jugador("a", Nivel.INTERMEDIO, false);
		assertFalse(a.esRivalValido(a, tenis));
	}

	@Test
	@DisplayName("Un rival no inscrito en el deporte no es válido")
	void rivalNoInscrito() {
		JugadorIndividual a = jugador("a", Nivel.INTERMEDIO, false);
		JugadorIndividual b = new JugadorIndividual("b", "pw", "B", 20, false);
		assertFalse(a.esRivalValido(b, tenis));
		assertFalse(b.esRivalValido(a, tenis));
	}

	@Test
	@DisplayName("Inscritos en deportes distintos no son rivales en un deporte que no comparten")
	void rivalOtroDeporte() {
		JugadorIndividual a = jugador("a", Nivel.INTERMEDIO, false);
		assertFalse(a.esRivalValido(a, padel));
		JugadorIndividual b = new JugadorIndividual("b", "pw", "B", 20, false);
		b.inscribirseDeporte(padel, Nivel.INTERMEDIO);
		assertFalse(a.esRivalValido(b, padel));
	}

	@Test
	@DisplayName("Rival o deporte nulos lanzan excepción")
	void rivalNulos() {
		JugadorIndividual a = jugador("a", Nivel.INTERMEDIO, false);
		assertThrows(IllegalArgumentException.class, () -> a.esRivalValido(null, tenis));
		assertThrows(IllegalArgumentException.class, () -> a.esRivalValido(a, null));
	}

	@Test
	@DisplayName("Dar de baja desactiva las inscripciones")
	void darDeBaja() {
		ana.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
		ana.inscribirseDeporte(padel, Nivel.PRINCIPIANTE);
		assertTrue(ana.darDeBaja());
		assertFalse(ana.estaActivo());
		assertFalse(ana.estaInscritoEn(tenis));
		assertFalse(ana.estaInscritoEn(padel));
	}

	@Test
	@DisplayName("Dar de baja dos veces lanza excepción")
	void bajaDoble() {
		ana.darDeBaja();
		assertThrows(IllegalStateException.class, () -> ana.darDeBaja());
	}

	@Test
	@DisplayName("Un jugador individual lesionado no está habilitado")
	void lesionado() {
		ana.getHistorialMedico().registrarLesion("Codo");
		assertFalse(ana.estaHabilitado());
	}

	@Test
	@DisplayName("La lista de inscripciones devuelta es una copia")
	void inscripcionesCopia() {
		ana.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
		ana.getInscripciones().clear();
		assertEquals(1, ana.getInscripciones().size());
	}
}