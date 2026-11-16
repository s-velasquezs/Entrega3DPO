package club.test.usuarios;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.modelo.usuarios.Jugador;

public class JugadorTest {

	private Jugador jugador;

	@BeforeEach
	void setUp() {
		jugador = new Jugador("pedro", "clave123", "Pedro Pérez", 22) {
		};
	}

	@Test
	@DisplayName("Un jugador nuevo guarda sus datos y queda activo y habilitado")
	void jugadorNuevo() {
		assertEquals("pedro", jugador.getUsername());
		assertEquals("Pedro Pérez", jugador.getNombre());
		assertEquals(22, jugador.getEdad());
		assertTrue(jugador.estaActivo());
		assertTrue(jugador.estaHabilitado());
		assertFalse(jugador.estaSuspendido());
		assertTrue(jugador.consultarSanciones().isEmpty());
	}

	@Test
	@DisplayName("Un jugador nuevo tiene registro, historial, estadísticas y programa de fidelidad")
	void componentesInicializados() {
		assertNotNull(jugador.getRegistro());
		assertNotNull(jugador.getHistorialMedico());
		assertNotNull(jugador.consultarEstadisticas());
		assertNotNull(jugador.getProgramaFidelidad());
		assertFalse(jugador.getHistorialMedico().tieneLesionActiva());
	}

	@Test
	@DisplayName("Autentica con las credenciales heredadas de Usuario")
	void autenticacion() {
		assertTrue(jugador.autenticar("pedro", "clave123"));
		assertFalse(jugador.autenticar("pedro", "otra"));
		assertFalse(jugador.autenticar("otro", "clave123"));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Un nombre inválido lanza excepción")
	void nombreInvalido(String nombre) {
		assertThrows(IllegalArgumentException.class, () -> new Jugador("pedro", "clave123", nombre, 22) {
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -1, -50 })
	@DisplayName("Una edad no positiva lanza excepción")
	void edadInvalida(int edad) {
		assertThrows(IllegalArgumentException.class, () -> new Jugador("pedro", "clave123", "Pedro", edad) {
		});
	}

	@Test
	@DisplayName("La edad mínima válida es 1")
	void edadMinima() {
		assertEquals(1, new Jugador("bebe", "clave123", "Bebé", 1) {
		}.getEdad());
	}

	@Test
	@DisplayName("Un username o password inválido lo rechaza la clase Usuario")
	void credencialesInvalidas() {
		assertThrows(IllegalArgumentException.class, () -> new Jugador(null, "clave123", "Pedro", 22) {
		});
		assertThrows(IllegalArgumentException.class, () -> new Jugador("pedro", null, "Pedro", 22) {
		});
	}

	@Test
	@DisplayName("Dar de baja deja al jugador inactivo y no habilitado")
	void darDeBaja() {
		assertTrue(jugador.darDeBaja());
		assertFalse(jugador.estaActivo());
		assertFalse(jugador.estaHabilitado());
	}

	@Test
	@DisplayName("Dar de baja dos veces lanza excepción con el motivo")
	void darDeBajaDoble() {
		jugador.darDeBaja();
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> jugador.darDeBaja());
		assertTrue(e.getMessage().contains("baja"));
		assertFalse(jugador.estaActivo());
	}

	@Test
	@DisplayName("Una lesión activa deja al jugador activo pero no habilitado")
	void lesionadoNoHabilitado() {
		jugador.getHistorialMedico().registrarLesion("Esguince");
		assertTrue(jugador.estaActivo());
		assertFalse(jugador.estaHabilitado());
		assertFalse(jugador.estaSuspendido());
	}

	@Test
	@DisplayName("El alta médica vuelve a habilitar al jugador")
	void altaHabilita() {
		jugador.getHistorialMedico().registrarLesion("Esguince");
		jugador.getHistorialMedico().darAlta("Recuperado");
		assertTrue(jugador.estaHabilitado());
	}

	@Test
	@DisplayName("Un jugador lesionado sí puede darse de baja")
	void bajaConLesion() {
		jugador.getHistorialMedico().registrarLesion("Esguince");
		assertTrue(jugador.darDeBaja());
		assertFalse(jugador.estaActivo());
	}

	@Test
	@DisplayName("Los componentes devueltos son siempre los mismos objetos")
	void mismosComponentes() {
		assertSame(jugador.getRegistro(), jugador.getRegistro());
		assertSame(jugador.getHistorialMedico(), jugador.getHistorialMedico());
		assertSame(jugador.consultarEstadisticas(), jugador.consultarEstadisticas());
		assertSame(jugador.getProgramaFidelidad(), jugador.getProgramaFidelidad());
	}

	@Test
	@DisplayName("Las sanciones devueltas están vacías y se pueden consultar varias veces")
	void sancionesConsulta() {
		assertNotNull(jugador.consultarSanciones());
		assertEquals(0, jugador.consultarSanciones().size());
		assertEquals(0, jugador.consultarSanciones().size());
	}

	@Test
	@DisplayName("toString muestra nombre y username")
	void toStringFormato() {
		assertEquals("Pedro Pérez (pedro)", jugador.toString());
	}
}
