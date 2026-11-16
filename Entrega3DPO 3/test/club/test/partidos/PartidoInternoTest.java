package club.test.partidos;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.test.TestFixtures;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.enums.Nivel;
import club.modelo.partidos.PartidoInterno;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;

public class PartidoInternoTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private DeporteEquipo futbol;
	private Equipo equipoA;
	private Equipo equipoB;
	private List<JugadorEquipo> jugadoresA;
	private List<JugadorEquipo> jugadoresB;
	private PartidoInterno partido;

	@BeforeEach
	void setUp() {
		futbol = TestFixtures.futbol();
		equipoA = TestFixtures.equipo("A", futbol, Categoria.MAYORES);
		equipoB = TestFixtures.equipo("B", futbol, Categoria.MAYORES);
		jugadoresA = TestFixtures.poblar(equipoA, 7, "a");
		jugadoresB = TestFixtures.poblar(equipoB, 7, "b");
		partido = new PartidoInterno(FECHA, Categoria.MAYORES, futbol, equipoA, equipoB);
	}

	@Test
	@DisplayName("Un partido interno válido guarda sus datos y empieza sin jugar")
	void partidoNuevo() {
		assertEquals(FECHA, partido.getFecha());
		assertEquals(Categoria.MAYORES, partido.getCategoria());
		assertEquals(futbol, partido.getDeporte());
		assertSame(equipoA, partido.getEquipoA());
		assertSame(equipoB, partido.getEquipoB());
		assertEquals(0, partido.getMarcadorA());
		assertEquals(0, partido.getMarcadorB());
		assertFalse(partido.estaJugado());
		assertTrue(partido.estaActivo());
		assertNotNull(partido.getRegistro());
		assertTrue(partido.getParticipantes().isEmpty());
		assertTrue(partido.getEstadisticas().isEmpty());
	}

	@Test
	@DisplayName("Dos equipos nulos o un equipo nulo lanzan excepción")
	void equiposNulos() {
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(FECHA, Categoria.MAYORES, futbol, null, equipoB));
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(FECHA, Categoria.MAYORES, futbol, equipoA, null));
	}

	@Test
	@DisplayName("Fecha, categoría o deporte nulos lanzan excepción")
	void datosNulos() {
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(null, Categoria.MAYORES, futbol, equipoA, equipoB));
		assertThrows(IllegalArgumentException.class, () -> new PartidoInterno(FECHA, null, futbol, equipoA, equipoB));
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(FECHA, Categoria.MAYORES, null, equipoA, equipoB));
	}

	@Test
	@DisplayName("Un equipo no puede jugar contra sí mismo")
	void mismoEquipo() {
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(FECHA, Categoria.MAYORES, futbol, equipoA, equipoA));
	}

	@Test
	@DisplayName("Equipos de distinta categoría se rechazan")
	void distintaCategoria() {
		Equipo sub13 = TestFixtures.equipo("C", futbol, Categoria.SUB13);
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(FECHA, Categoria.MAYORES, futbol, equipoA, sub13));
	}

	@Test
	@DisplayName("Equipos de distinto deporte se rechazan")
	void distintoDeporte() {
		Equipo basket = TestFixtures.equipo("D", TestFixtures.baloncesto(), Categoria.MAYORES);
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(FECHA, Categoria.MAYORES, futbol, equipoA, basket));
	}

	@Test
	@DisplayName("La categoría o el deporte del partido deben coincidir con los de los equipos")
	void partidoNoCoincide() {
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(FECHA, Categoria.SUB13, futbol, equipoA, equipoB));
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(FECHA, Categoria.MAYORES, TestFixtures.baloncesto(), equipoA, equipoB));
	}

	@Test
	@DisplayName("Un equipo inactivo se rechaza")
	void equipoInactivo() {
		equipoB.darDeBaja();
		assertThrows(IllegalArgumentException.class,
				() -> new PartidoInterno(FECHA, Categoria.MAYORES, futbol, equipoA, equipoB));
	}

	@Test
	@DisplayName("Validar con el mínimo de jugadores habilitados no lanza excepción")
	void validarConMinimo() {
		assertDoesNotThrow(() -> partido.validar());
	}

	@Test
	@DisplayName("Un equipo sin el mínimo de jugadores no puede jugar")
	void validarSinMinimo() {
		Equipo corto = TestFixtures.equipo("Corto", futbol, Categoria.MAYORES);
		TestFixtures.poblar(corto, 6, "c");
		PartidoInterno p = new PartidoInterno(FECHA, Categoria.MAYORES, futbol, equipoA, corto);
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> p.validar());
		assertTrue(e.getMessage().contains("Corto"));
	}

	@Test
	@DisplayName("Un jugador lesionado deja al equipo por debajo del mínimo")
	void lesionBajaDelMinimo() {
		jugadoresA.get(0).getHistorialMedico().registrarLesion("Esguince");
		assertThrows(IllegalStateException.class, () -> partido.validar());
	}

	@Test
	@DisplayName("Agregar jugadores de ambos equipos")
	void agregarParticipantes() {
		partido.agregarParticipante(jugadoresA.get(0));
		partido.agregarParticipante(jugadoresB.get(0));
		assertEquals(2, partido.getParticipantes().size());
	}

	@Test
	@DisplayName("Un jugador nulo lanza excepción")
	void participanteNulo() {
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(null));
	}

	@Test
	@DisplayName("Un jugador individual no participa en partidos internos")
	void participanteIndividual() {
		JugadorIndividual ind = new JugadorIndividual("i", "pw", "I", 20, false);
		ind.inscribirseDeporte(TestFixtures.tenis(), Nivel.PRINCIPIANTE);
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(ind));
	}

	@Test
	@DisplayName("Un jugador de otro equipo se rechaza")
	void participanteAjeno() {
		Equipo otro = TestFixtures.equipo("Otro", futbol, Categoria.MAYORES);
		JugadorEquipo ajeno = TestFixtures.poblar(otro, 1, "z").get(0);
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(ajeno));
	}

	@Test
	@DisplayName("Un participante repetido se rechaza")
	void participanteRepetido() {
		partido.agregarParticipante(jugadoresA.get(0));
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(jugadoresA.get(0)));
		assertEquals(1, partido.getParticipantes().size());
	}

	@Test
	@DisplayName("Un jugador lesionado no puede participar")
	void participanteLesionado() {
		JugadorEquipo j = jugadoresA.get(0);
		j.getHistorialMedico().registrarLesion("Rodilla");
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(j));
	}

	@Test
	@DisplayName("Un jugador dado de baja no puede participar")
	void participanteInactivo() {
		JugadorEquipo j = jugadoresA.get(0);
		j.darDeBaja();
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(j));
	}

	@Test
	@DisplayName("Registrar un resultado marca el partido como jugado")
	void registrarResultado() {
		partido.registrarResultado(3, 1);
		assertTrue(partido.estaJugado());
		assertEquals(3, partido.getMarcadorA());
		assertEquals(1, partido.getMarcadorB());
	}

	@Test
	@DisplayName("En fútbol el empate es válido")
	void empateFutbol() {
		partido.registrarResultado(2, 2);
		assertTrue(partido.estaJugado());
	}

	@Test
	@DisplayName("Marcadores negativos lanzan excepción y no marcan el partido")
	void marcadorNegativo() {
		assertThrows(IllegalArgumentException.class, () -> partido.registrarResultado(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> partido.registrarResultado(0, -1));
		assertFalse(partido.estaJugado());
	}

	@Test
	@DisplayName("No se puede registrar el resultado dos veces")
	void resultadoDoble() {
		partido.registrarResultado(1, 0);
		assertThrows(IllegalStateException.class, () -> partido.registrarResultado(2, 0));
		assertEquals(1, partido.getMarcadorA());
	}

	@Test
	@DisplayName("No se agregan participantes a un partido ya jugado")
	void participanteTrasResultado() {
		partido.registrarResultado(1, 0);
		assertThrows(IllegalStateException.class, () -> partido.agregarParticipante(jugadoresA.get(0)));
	}

	@Test
	@DisplayName("Un partido jugado sin participantes no valida")
	void jugadoSinParticipantes() {
		partido.registrarResultado(1, 0);
		assertThrows(IllegalStateException.class, () -> partido.validar());
	}

	@Test
	@DisplayName("Un partido jugado con participantes valida")
	void jugadoConParticipantes() {
		partido.agregarParticipante(jugadoresA.get(0));
		partido.registrarResultado(1, 0);
		assertDoesNotThrow(() -> partido.validar());
	}

	@Test
	@DisplayName("En baloncesto el empate no se permite")
	void empateBaloncesto() {
		DeporteEquipo basket = TestFixtures.baloncesto();
		Equipo x = TestFixtures.equipo("X", basket, Categoria.MAYORES);
		Equipo y = TestFixtures.equipo("Y", basket, Categoria.MAYORES);
		TestFixtures.poblar(x, 5, "x");
		TestFixtures.poblar(y, 5, "y");
		PartidoInterno p = new PartidoInterno(FECHA, Categoria.MAYORES, basket, x, y);
		assertThrows(IllegalArgumentException.class, () -> p.registrarResultado(80, 80));
		assertFalse(p.estaJugado());
		p.registrarResultado(80, 78);
		assertTrue(p.estaJugado());
	}

	@Test
	@DisplayName("Las listas de participantes y estadísticas no se pueden modificar desde afuera")
	void listasInmodificables() {
		assertThrows(UnsupportedOperationException.class, () -> partido.getParticipantes().add(null));
		assertThrows(UnsupportedOperationException.class, () -> partido.getEstadisticas().add(null));
	}
}
