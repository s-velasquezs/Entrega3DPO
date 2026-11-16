package club.test.personal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.modelo.personal.Fisioterapeuta;
import club.modelo.salud.HistorialMedico;
import club.modelo.usuarios.JugadorEquipo;

public class FisioterapeutaTest {

	private Fisioterapeuta fisio;
	private JugadorEquipo jugador;

	@BeforeEach
	void setUp() {
		fisio = new Fisioterapeuta("fisio1", "pw", "Fabiana");
		jugador = new JugadorEquipo("juan", "pw", "Juan", 20);
	}

	@Test
	@DisplayName("Registrar una lesión inhabilita al jugador")
	void registrarLesion() {
		assertTrue(jugador.estaHabilitado());
		assertTrue(fisio.registrarLesion(jugador, "Esguince"));
		assertFalse(jugador.estaHabilitado());
		assertTrue(jugador.getHistorialMedico().tieneLesionActiva());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Una descripción vacía se rechaza y el jugador sigue habilitado")
	void descripcionVacia(String descripcion) {
		assertThrows(IllegalArgumentException.class, () -> fisio.registrarLesion(jugador, descripcion));
		assertTrue(jugador.estaHabilitado());
	}

	@Test
	@DisplayName("No se puede registrar una lesión si ya hay una activa")
	void lesionDoble() {
		fisio.registrarLesion(jugador, "Esguince");
		assertThrows(IllegalStateException.class, () -> fisio.registrarLesion(jugador, "Otra"));
	}

	@Test
	@DisplayName("El alta médica habilita de nuevo al jugador")
	void altaMedica() {
		fisio.registrarLesion(jugador, "Esguince");
		assertTrue(fisio.darAlta(jugador));
		assertTrue(jugador.estaHabilitado());
		assertEquals("Alta médica", jugador.getHistorialMedico().getLesiones().get(0).getNotaAlta());
	}

	@Test
	@DisplayName("El alta puede llevar una nota personalizada")
	void altaConNota() {
		fisio.registrarLesion(jugador, "Esguince");
		fisio.darAlta(jugador, "Recuperación completa");
		assertEquals("Recuperación completa", jugador.getHistorialMedico().getLesiones().get(0).getNotaAlta());
	}

	@Test
	@DisplayName("Dar de alta a quien no tiene lesión informa que no hay lesión para cerrar")
	void altaSinLesion() {
		assertThrows(IllegalStateException.class, () -> fisio.darAlta(jugador));
	}

	@Test
	@DisplayName("Consultar el historial devuelve el del jugador")
	void consultarHistorial() {
		HistorialMedico h = fisio.consultarHistorial(jugador);
		assertSame(jugador.getHistorialMedico(), h);
		assertTrue(h.getLesiones().isEmpty());
	}

	@Test
	@DisplayName("Editar el historial agrega una observación")
	void editarHistorial() {
		fisio.editarHistorial(jugador, "Reposo una semana");
		assertTrue(jugador.getHistorialMedico().getObservaciones().contains("Reposo una semana"));
	}

	@Test
	@DisplayName("Editar con una observación vacía lanza excepción")
	void editarVacio() {
		assertThrows(IllegalArgumentException.class, () -> fisio.editarHistorial(jugador, " "));
	}

	@Test
	@DisplayName("Un jugador nulo lanza excepción en todas las operaciones")
	void jugadorNulo() {
		assertThrows(IllegalArgumentException.class, () -> fisio.registrarLesion(null, "x"));
		assertThrows(IllegalArgumentException.class, () -> fisio.darAlta(null));
		assertThrows(IllegalArgumentException.class, () -> fisio.consultarHistorial(null));
		assertThrows(IllegalArgumentException.class, () -> fisio.editarHistorial(null, "x"));
	}

	@Test
	@DisplayName("No se puede operar sobre un jugador dado de baja")
	void jugadorInactivo() {
		jugador.darDeBaja();
		assertThrows(IllegalStateException.class, () -> fisio.registrarLesion(jugador, "x"));
	}

	@Test
	@DisplayName("Un fisioterapeuta dado de baja no puede operar")
	void fisioInactivo() {
		fisio.darDeBaja();
		assertThrows(IllegalStateException.class, () -> fisio.registrarLesion(jugador, "x"));
		assertThrows(IllegalStateException.class, () -> fisio.consultarHistorial(jugador));
	}
}