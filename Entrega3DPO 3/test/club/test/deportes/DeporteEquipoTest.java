package club.test.deportes;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;

public class DeporteEquipoTest {

	private DeporteEquipo futbol;
	private DeporteEquipo baloncesto;

	@BeforeEach
	void setUp() {
		futbol = new DeporteEquipo("Futbol", 7, true, 3, 1, 0);
		baloncesto = new DeporteEquipo("Baloncesto", 5, false, 2, 0, 1);
	}

	@Test
	@DisplayName("Un deporte válido guarda sus datos")
	void guardaDatos() {
		assertEquals("Futbol", futbol.getNombre());
		assertEquals(7, futbol.getMinimoJugadoresHabilitados());
		assertTrue(futbol.permiteEmpate());
		assertEquals(3, futbol.getPuntosVictoria());
		assertEquals(1, futbol.getPuntosEmpate());
		assertEquals(0, futbol.getPuntosDerrota());
		assertFalse(baloncesto.permiteEmpate());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Un nombre nulo, vacío o en blanco lanza excepción")
	void nombreInvalido(String nombre) {
		assertThrows(IllegalArgumentException.class, () -> new DeporteEquipo(nombre, 7, true, 3, 1, 0));
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -1, -10 })
	@DisplayName("Un mínimo de jugadores menor o igual a cero lanza excepción")
	void minimoInvalido(int minimo) {
		assertThrows(IllegalArgumentException.class, () -> new DeporteEquipo("X", minimo, true, 3, 1, 0));
	}

	@Test
	@DisplayName("Puntos negativos lanzan excepción")
	void puntosNegativos() {
		assertThrows(IllegalArgumentException.class, () -> new DeporteEquipo("X", 5, true, -1, 1, 0));
		assertThrows(IllegalArgumentException.class, () -> new DeporteEquipo("X", 5, true, 3, -1, 0));
		assertThrows(IllegalArgumentException.class, () -> new DeporteEquipo("X", 5, true, 3, 1, -1));
	}

	@Test
	@DisplayName("En fútbol la victoria da 3, el empate 1 y la derrota 0")
	void puntosFutbol() {
		assertEquals(3, futbol.puntosPara(2, 1));
		assertEquals(1, futbol.puntosPara(1, 1));
		assertEquals(0, futbol.puntosPara(0, 4));
	}

	@Test
	@DisplayName("En baloncesto victoria y derrota puntúan según la configuración")
	void puntosBaloncesto() {
		assertEquals(2, baloncesto.puntosPara(80, 70));
		assertEquals(1, baloncesto.puntosPara(70, 80));
	}

	@Test
	@DisplayName("Un empate en un deporte sin empates lanza excepción")
	void empateNoPermitido() {
		assertThrows(IllegalArgumentException.class, () -> baloncesto.puntosPara(10, 10));
	}

	@Test
	@DisplayName("Tantos negativos lanzan excepción")
	void tantosNegativos() {
		assertThrows(IllegalArgumentException.class, () -> futbol.puntosPara(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> futbol.puntosPara(0, -1));
	}

	@Test
	@DisplayName("Dos deportes con el mismo nombre y clase son iguales")
	void igualdadPorNombre() {
		DeporteEquipo otro = new DeporteEquipo("Futbol", 9, false, 1, 0, 0);
		assertEquals(futbol, otro);
		assertEquals(futbol.hashCode(), otro.hashCode());
	}

	@Test
	@DisplayName("Deportes con distinto nombre no son iguales")
	void desigualdad() {
		assertNotEquals(futbol, baloncesto);
		assertNotEquals(futbol, null);
		assertNotEquals(futbol, "Futbol");
		assertEquals(futbol, futbol);
	}

	@Test
	@DisplayName("Un deporte de equipo y uno individual con el mismo nombre no son iguales")
	void distintaClase() {
		DeporteIndividual individual = new DeporteIndividual("Futbol", 2);
		assertNotEquals(futbol, individual);
	}

	@Test
	@DisplayName("toString devuelve el nombre")
	void toStringNombre() {
		assertEquals("Futbol", futbol.toString());
	}
}