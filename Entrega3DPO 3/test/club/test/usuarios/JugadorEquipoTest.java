package club.test.usuarios;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.test.TestFixtures;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.usuarios.JugadorEquipo;

public class JugadorEquipoTest {

	private DeporteEquipo futbol;
	private DeporteEquipo baloncesto;
	private Equipo equipo;
	private JugadorEquipo jugador;

	@BeforeEach
	void setUp() {
		futbol = TestFixtures.futbol();
		baloncesto = TestFixtures.baloncesto();
		equipo = TestFixtures.equipo("Halcones", futbol, Categoria.MAYORES);
		jugador = new JugadorEquipo("juan", "pw", "Juan", 20);
	}

	@Test
	@DisplayName("Un jugador nuevo está activo, habilitado y sin sanciones")
	void jugadorNuevo() {
		assertEquals("Juan", jugador.getNombre());
		assertEquals(20, jugador.getEdad());
		assertTrue(jugador.estaActivo());
		assertTrue(jugador.estaHabilitado());
		assertFalse(jugador.estaSuspendido());
		assertTrue(jugador.consultarSanciones().isEmpty());
		assertNotNull(jugador.consultarEstadisticas());
		assertNotNull(jugador.getProgramaFidelidad());
		assertNotNull(jugador.getRegistro());
		assertNotNull(jugador.getHistorialMedico());
		assertTrue(jugador.getInscripciones().isEmpty());
		assertEquals("Juan (juan)", jugador.toString());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "  " })
	@DisplayName("Un nombre inválido lanza excepción")
	void nombreInvalido(String nombre) {
		assertThrows(IllegalArgumentException.class, () -> new JugadorEquipo("juan", "pw", nombre, 20));
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -1, -20 })
	@DisplayName("Una edad no positiva lanza excepción")
	void edadInvalida(int edad) {
		assertThrows(IllegalArgumentException.class, () -> new JugadorEquipo("juan", "pw", "Juan", edad));
	}

	@Test
	@DisplayName("Username y password son obligatorios")
	void credencialesObligatorias() {
		assertThrows(IllegalArgumentException.class, () -> new JugadorEquipo(null, "pw", "Juan", 20));
		assertThrows(IllegalArgumentException.class, () -> new JugadorEquipo("juan", " ", "Juan", 20));
	}

	@ParameterizedTest
	@CsvSource({ "8,SUB13", "13,SUB13", "14,SUB15", "15,SUB15", "16,SUB17", "17,SUB17", "18,MAYORES", "40,MAYORES" })
	@DisplayName("La categoría se calcula con la edad")
	void categoriaPorEdad(int edad, Categoria esperada) {
		assertEquals(esperada, new JugadorEquipo("x", "pw", "X", edad).getCategoria());
	}

	@Test
	@DisplayName("Inscribirse a un equipo de su categoría con cupo y número libre")
	void inscripcionValida() {
		assertTrue(jugador.inscribirseEquipo(equipo, "Portero", 1));
		assertTrue(jugador.tieneEquipoEn(futbol));
		assertSame(equipo, jugador.getEquipo(futbol));
		assertEquals("Portero", jugador.getPosicion(equipo));
		assertEquals(1, jugador.getNumeroCamiseta(equipo));
		assertTrue(equipo.tieneJugador(jugador));
		assertEquals(1, jugador.getInscripciones().size());
	}

	@Test
	@DisplayName("Sin posición ni número queda pendiente de asignación")
	void inscripcionPendiente() {
		assertTrue(jugador.inscribirseEquipo(equipo, null, 0));
		assertNull(jugador.getPosicion(equipo));
		assertEquals(0, jugador.getNumeroCamiseta(equipo));
		assertFalse(jugador.getInscripcion(futbol).tienePosicion());
		assertFalse(jugador.getInscripcion(futbol).tieneNumero());
	}

	@Test
	@DisplayName("Un equipo nulo lanza excepción")
	void equipoNulo() {
		assertThrows(IllegalArgumentException.class, () -> jugador.inscribirseEquipo(null, "Portero", 1));
	}

	@Test
	@DisplayName("Un número de camiseta negativo lanza excepción")
	void numeroNegativo() {
		assertThrows(IllegalArgumentException.class, () -> jugador.inscribirseEquipo(equipo, "Portero", -3));
	}

	@Test
	@DisplayName("Un equipo de otra categoría se rechaza")
	void categoriaDistinta() {
		Equipo sub13 = TestFixtures.equipo("Pequeños", futbol, Categoria.SUB13);
		assertThrows(IllegalStateException.class, () -> jugador.inscribirseEquipo(sub13, "Portero", 1));
		assertFalse(jugador.tieneEquipoEn(futbol));
	}

	@Test
	@DisplayName("Un equipo sin cupo se rechaza")
	void sinCupo() {
		Equipo chico = new Equipo("Chico", Categoria.MAYORES, futbol, 2);
		TestFixtures.poblar(chico, 2, "x");
		assertThrows(IllegalStateException.class, () -> jugador.inscribirseEquipo(chico, "Portero", 9));
		assertFalse(jugador.tieneEquipoEn(futbol));
	}

	@Test
	@DisplayName("Un número de camiseta repetido se rechaza")
	void numeroRepetido() {
		TestFixtures.poblar(equipo, 1, "x");
		assertThrows(IllegalStateException.class, () -> jugador.inscribirseEquipo(equipo, "Portero", 1));
		assertFalse(jugador.tieneEquipoEn(futbol));
	}

	@Test
	@DisplayName("No puede estar en dos equipos del mismo deporte")
	void dosEquiposMismoDeporte() {
		Equipo otro = TestFixtures.equipo("Águilas", futbol, Categoria.MAYORES);
		jugador.inscribirseEquipo(equipo, "Portero", 1);
		assertThrows(IllegalStateException.class, () -> jugador.inscribirseEquipo(otro, "Defensa", 2));
		assertSame(equipo, jugador.getEquipo(futbol));
	}

	@Test
	@DisplayName("Puede estar en equipos de distintos deportes a la vez")
	void dosDeportes() {
		Equipo basket = TestFixtures.equipo("Torres", baloncesto, Categoria.MAYORES);
		assertTrue(jugador.inscribirseEquipo(equipo, "Portero", 1));
		assertTrue(jugador.inscribirseEquipo(basket, "Base", 7));
		assertSame(equipo, jugador.getEquipo(futbol));
		assertSame(basket, jugador.getEquipo(baloncesto));
		assertEquals(2, jugador.getInscripciones().size());
	}

	@Test
	@DisplayName("Un equipo inactivo se rechaza")
	void equipoInactivo() {
		equipo.darDeBaja();
		assertThrows(IllegalStateException.class, () -> jugador.inscribirseEquipo(equipo, "Portero", 1));
	}

	@Test
	@DisplayName("Un jugador dado de baja no puede inscribirse")
	void jugadorInactivo() {
		jugador.darDeBaja();
		assertThrows(IllegalStateException.class, () -> jugador.inscribirseEquipo(equipo, "Portero", 1));
	}

	@Test
	@DisplayName("Consultar equipo, posición y número sin inscripción")
	void sinInscripcion() {
		assertNull(jugador.getEquipo(futbol));
		assertNull(jugador.getPosicion(equipo));
		assertEquals(0, jugador.getNumeroCamiseta(equipo));
		assertNull(jugador.getInscripcion(futbol));
		assertFalse(jugador.tieneEquipoEn(futbol));
	}

	@Test
	@DisplayName("Dar de baja desactiva las inscripciones y al jugador")
	void darDeBaja() {
		jugador.inscribirseEquipo(equipo, "Portero", 1);
		assertTrue(jugador.darDeBaja());
		assertFalse(jugador.estaActivo());
		assertFalse(jugador.estaHabilitado());
		assertFalse(jugador.tieneEquipoEn(futbol));
		assertFalse(jugador.getInscripciones().get(0).estaActiva());
	}

	@Test
	@DisplayName("Dar de baja dos veces lanza excepción")
	void bajaDoble() {
		jugador.darDeBaja();
		assertThrows(IllegalStateException.class, () -> jugador.darDeBaja());
	}

	@Test
	@DisplayName("Un jugador lesionado no está habilitado hasta el alta")
	void lesionInhabilita() {
		jugador.getHistorialMedico().registrarLesion("Esguince");
		assertFalse(jugador.estaHabilitado());
		assertTrue(jugador.estaActivo());
		jugador.getHistorialMedico().darAlta("ok");
		assertTrue(jugador.estaHabilitado());
	}

	@Test
	@DisplayName("Tras la baja se puede inscribir en otro equipo del mismo deporte solo si está activo")
	void liberarDeporteTrasBaja() {
		jugador.inscribirseEquipo(equipo, "Portero", 1);
		jugador.darDeBaja();
		Equipo otro = TestFixtures.equipo("Águilas", futbol, Categoria.MAYORES);
		assertThrows(IllegalStateException.class, () -> jugador.inscribirseEquipo(otro, "Defensa", 2));
	}

	@Test
	@DisplayName("La lista de inscripciones devuelta es una copia")
	void inscripcionesCopia() {
		jugador.inscribirseEquipo(equipo, "Portero", 1);
		jugador.getInscripciones().clear();
		assertEquals(1, jugador.getInscripciones().size());
	}
}
