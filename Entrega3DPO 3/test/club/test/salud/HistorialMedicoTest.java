package club.test.salud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.modelo.salud.HistorialMedico;

public class HistorialMedicoTest {

	private HistorialMedico historial;

	@BeforeEach
	void setUp() {
		historial = new HistorialMedico();
	}

	@Test
	@DisplayName("Un historial nuevo está vacío y sin lesión activa")
	void historialVacio() {
		assertTrue(historial.getLesiones().isEmpty());
		assertTrue(historial.getObservaciones().isEmpty());
		assertFalse(historial.tieneLesionActiva());
		assertFalse(historial.estaLesionado());
		assertNull(historial.getLesionActiva());
	}

	@Test
	@DisplayName("Registrar una lesión la deja activa y la anota")
	void registrarLesion() {
		assertTrue(historial.registrarLesion("Rotura de ligamento"));
		assertTrue(historial.tieneLesionActiva());
		assertTrue(historial.estaLesionado());
		assertEquals("Rotura de ligamento", historial.getLesionActiva().getDescripcion());
		assertEquals(1, historial.getLesiones().size());
		assertEquals(1, historial.getObservaciones().size());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Una descripción inválida se rechaza y no cambia el estado")
	void descripcionInvalida(String descripcion) {
		assertThrows(IllegalArgumentException.class, () -> historial.registrarLesion(descripcion));
		assertFalse(historial.tieneLesionActiva());
		assertTrue(historial.getLesiones().isEmpty());
	}

	@Test
	@DisplayName("No se puede registrar una segunda lesión mientras haya una activa")
	void segundaLesionActiva() {
		historial.registrarLesion("Primera");
		assertThrows(IllegalStateException.class, () -> historial.registrarLesion("Segunda"));
		assertEquals(1, historial.getLesiones().size());
	}

	@Test
	@DisplayName("Dar alta habilita de nuevo y deja la nota")
	void darAlta() {
		historial.registrarLesion("Esguince");
		assertTrue(historial.darAlta("Alta médica"));
		assertFalse(historial.tieneLesionActiva());
		assertNull(historial.getLesionActiva());
		assertEquals("Alta médica", historial.getLesiones().get(0).getNotaAlta());
		assertEquals(2, historial.getObservaciones().size());
	}

	@Test
	@DisplayName("Dar alta sin lesión activa informa que no hay lesión para cerrar")
	void altaSinLesion() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> historial.darAlta("ok"));
		assertTrue(e.getMessage().toLowerCase().contains("lesión"));
	}

	@Test
	@DisplayName("Dar alta dos veces seguidas falla la segunda")
	void altaDoble() {
		historial.registrarLesion("Esguince");
		historial.darAlta("ok");
		assertThrows(IllegalStateException.class, () -> historial.darAlta("ok"));
	}

	@Test
	@DisplayName("Una nota de alta inválida mantiene la lesión activa")
	void notaAltaInvalida() {
		historial.registrarLesion("Esguince");
		assertThrows(IllegalArgumentException.class, () -> historial.darAlta(" "));
		assertTrue(historial.tieneLesionActiva());
	}

	@Test
	@DisplayName("Después del alta se puede registrar una nueva lesión")
	void nuevaLesionTrasAlta() {
		historial.registrarLesion("Primera");
		historial.darAlta("ok");
		assertTrue(historial.registrarLesion("Segunda"));
		assertEquals(2, historial.getLesiones().size());
		assertEquals("Segunda", historial.getLesionActiva().getDescripcion());
	}

	@Test
	@DisplayName("Agregar una observación válida")
	void agregarObservacion() {
		historial.agregarObservacion("Reposo 3 días");
		assertEquals(1, historial.getObservaciones().size());
		assertEquals("Reposo 3 días", historial.getObservaciones().get(0));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Una observación inválida lanza excepción")
	void observacionInvalida(String obs) {
		assertThrows(IllegalArgumentException.class, () -> historial.agregarObservacion(obs));
		assertTrue(historial.getObservaciones().isEmpty());
	}

	@Test
	@DisplayName("Las listas devueltas son copias")
	void listasSonCopias() {
		historial.registrarLesion("Esguince");
		historial.getLesiones().clear();
		historial.getObservaciones().clear();
		assertEquals(1, historial.getLesiones().size());
		assertFalse(historial.getObservaciones().isEmpty());
	}
}