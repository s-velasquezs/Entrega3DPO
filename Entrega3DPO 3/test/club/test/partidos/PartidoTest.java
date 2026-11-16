package club.test.partidos;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.partidos.Partido;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;
import club.test.TestFixtures;

public class PartidoTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private DeporteEquipo futbol;
	private DeporteEquipo baloncesto;
	private Equipo equipo;
	private List<JugadorEquipo> jugadores;
	private Partido partido;

	@BeforeEach
	void setUp() {
		futbol = TestFixtures.futbol();
		baloncesto = TestFixtures.baloncesto();
		equipo = TestFixtures.equipo("Halcones", futbol, Categoria.MAYORES);
		jugadores = TestFixtures.poblar(equipo, 7, "h");
		partido = new Partido(FECHA, Categoria.MAYORES, futbol) {
		};
	}

	@Test
	@DisplayName("Un partido nuevo guarda sus datos, está activo y sin jugar")
	void partidoNuevo() {
		assertEquals(FECHA, partido.getFecha());
		assertEquals(Categoria.MAYORES, partido.getCategoria());
		assertSame(futbol, partido.getDeporte());
		assertEquals(0, partido.getMarcadorA());
		assertEquals(0, partido.getMarcadorB());
		assertFalse(partido.estaJugado());
		assertTrue(partido.estaActivo());
		assertNotNull(partido.getRegistro());
		assertTrue(partido.getParticipantes().isEmpty());
		assertTrue(partido.getEstadisticas().isEmpty());
	}

	@Test
	@DisplayName("Fecha, categoría o deporte nulos lanzan excepción")
	void constructorNulos() {
		assertThrows(IllegalArgumentException.class, () -> new Partido(null, Categoria.MAYORES, futbol) {
		});
		assertThrows(IllegalArgumentException.class, () -> new Partido(FECHA, null, futbol) {
		});
		assertThrows(IllegalArgumentException.class, () -> new Partido(FECHA, Categoria.MAYORES, null) {
		});
	}

	@Test
	@DisplayName("Un partido nuevo pasa la validación")
	void validarNuevo() {
		assertDoesNotThrow(() -> partido.validar());
	}

	@Test
	@DisplayName("Un partido dado de baja no valida")
	void validarNoRegistrado() {
		partido.getRegistro().darDeBaja();
		assertFalse(partido.estaActivo());
		assertThrows(IllegalStateException.class, () -> partido.validar());
	}

	@Test
	@DisplayName("Registrar un resultado lo marca como jugado y guarda el marcador")
	void registrarResultado() {
		partido.registrarResultado(3, 1);
		assertTrue(partido.estaJugado());
		assertEquals(3, partido.getMarcadorA());
		assertEquals(1, partido.getMarcadorB());
	}

	@Test
	@DisplayName("Un marcador en cero es válido")
	void marcadorCero() {
		partido.registrarResultado(0, 0);
		assertTrue(partido.estaJugado());
	}

	@ParameterizedTest
	@CsvSource({ "-1,0", "0,-1", "-3,-3" })
	@DisplayName("Marcadores negativos lanzan excepción y no marcan jugado")
	void marcadorNegativo(int a, int b) {
		assertThrows(IllegalArgumentException.class, () -> partido.registrarResultado(a, b));
		assertFalse(partido.estaJugado());
		assertEquals(0, partido.getMarcadorA());
	}

	@Test
	@DisplayName("No se puede registrar el resultado dos veces")
	void resultadoDoble() {
		partido.registrarResultado(2, 0);
		assertThrows(IllegalStateException.class, () -> partido.registrarResultado(3, 3));
		assertEquals(2, partido.getMarcadorA());
		assertEquals(0, partido.getMarcadorB());
	}

	@Test
	@DisplayName("No se registra resultado en un partido dado de baja")
	void resultadoSinRegistro() {
		partido.getRegistro().darDeBaja();
		assertThrows(IllegalStateException.class, () -> partido.registrarResultado(1, 0));
		assertFalse(partido.estaJugado());
	}

	@Test
	@DisplayName("Un deporte con empate permite marcadores iguales")
	void empatePermitido() {
		assertDoesNotThrow(() -> partido.registrarResultado(2, 2));
		assertTrue(partido.estaJugado());
	}

	@Test
	@DisplayName("Un deporte sin empate rechaza marcadores iguales")
	void empateNoPermitido() {
		Partido p = new Partido(FECHA, Categoria.MAYORES, baloncesto) {
		};
		assertThrows(IllegalArgumentException.class, () -> p.registrarResultado(80, 80));
		assertFalse(p.estaJugado());
		assertDoesNotThrow(() -> p.registrarResultado(81, 80));
	}

	@Test
	@DisplayName("Un deporte individual no restringe el empate")
	void deporteIndividual() {
		DeporteIndividual tenis = TestFixtures.tenis();
		Partido p = new Partido(FECHA, Categoria.MAYORES, tenis) {
		};
		assertDoesNotThrow(() -> p.registrarResultado(1, 1));
	}

	@Test
	@DisplayName("Agregar participantes habilitados de la categoría correcta")
	void agregarParticipante() {
		partido.agregarParticipante(jugadores.get(0));
		partido.agregarParticipante(jugadores.get(1));
		assertEquals(2, partido.getParticipantes().size());
		assertTrue(partido.getParticipantes().contains(jugadores.get(0)));
	}

	@Test
	@DisplayName("Un participante nulo lanza excepción")
	void participanteNulo() {
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(null));
	}

	@Test
	@DisplayName("No se puede agregar dos veces al mismo jugador")
	void participanteDuplicado() {
		partido.agregarParticipante(jugadores.get(0));
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(jugadores.get(0)));
		assertEquals(1, partido.getParticipantes().size());
	}

	@Test
	@DisplayName("Un jugador dado de baja no puede participar")
	void participanteInactivo() {
		JugadorEquipo j = jugadores.get(0);
		j.getRegistro().darDeBaja();
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(j));
		assertTrue(partido.getParticipantes().isEmpty());
	}

	@Test
	@DisplayName("Un jugador lesionado no puede participar y vuelve a poder tras el alta")
	void participanteLesionado() {
		JugadorEquipo j = jugadores.get(0);
		j.getHistorialMedico().registrarLesion("Esguince");
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(j));
		j.getHistorialMedico().darAlta("Recuperado");
		assertDoesNotThrow(() -> partido.agregarParticipante(j));
	}

	@Test
	@DisplayName("Un jugador de equipo de otra categoría no puede participar")
	void participanteOtraCategoria() {
		Equipo sub15 = TestFixtures.equipo("Juveniles", futbol, Categoria.SUB15);
		JugadorEquipo joven = TestFixtures.poblar(sub15, 1, "j").get(0);
		assertThrows(IllegalArgumentException.class, () -> partido.agregarParticipante(joven));
		assertTrue(partido.getParticipantes().isEmpty());
	}

	@Test
	@DisplayName("Un jugador individual puede participar sin restricción de categoría")
	void participanteIndividual() {
		JugadorIndividual ana = new JugadorIndividual("ana", "pw", "Ana", 20, false);
		assertDoesNotThrow(() -> partido.agregarParticipante(ana));
	}

	@Test
	@DisplayName("No se agregan participantes a un partido ya jugado")
	void participanteTrasJugar() {
		partido.registrarResultado(1, 0);
		assertThrows(IllegalStateException.class, () -> partido.agregarParticipante(jugadores.get(0)));
	}

	@Test
	@DisplayName("Un partido jugado sin participantes no valida")
	void validarJugadoSinParticipantes() {
		partido.registrarResultado(1, 0);
		assertThrows(IllegalStateException.class, () -> partido.validar());
	}

	@Test
	@DisplayName("Un partido jugado con participantes valida")
	void validarJugadoConParticipantes() {
		partido.agregarParticipante(jugadores.get(0));
		partido.registrarResultado(1, 0);
		assertDoesNotThrow(() -> partido.validar());
	}

	@Test
	@DisplayName("Estadísticas nulas lanzan excepción")
	void estadisticasNulas() {
		assertThrows(IllegalArgumentException.class, () -> partido.agregarEstadisticas(null));
		assertTrue(partido.getEstadisticas().isEmpty());
	}

	@Test
	@DisplayName("Las listas de participantes y estadísticas no se pueden modificar desde afuera")
	void listasNoModificables() {
		partido.agregarParticipante(jugadores.get(0));
		assertThrows(UnsupportedOperationException.class, () -> partido.getParticipantes().clear());
		assertThrows(UnsupportedOperationException.class, () -> partido.getEstadisticas().clear());
		assertEquals(1, partido.getParticipantes().size());
	}
}