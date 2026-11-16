package club.test.usuarios;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.modelo.usuarios.Usuario;

public class UsuarioTest {

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		usuario = new Usuario("val", "clave123") { };
	}


	@Test
	@DisplayName("Un usuario válido guarda su username")
	void usuarioValidoGuardaElUsername() {
		assertEquals("val", usuario.getUsername());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Un username nulo, vacío o en blanco lanza excepción")
	void usernameInvalidoLanzaExcepcion(String username) {
		assertThrows(IllegalArgumentException.class,
				() -> new Usuario(username, "clave123") { });
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Un password nulo, vacío o en blanco lanza excepción")
	void passwordInvalidoLanzaExcepcion(String password) {
		assertThrows(IllegalArgumentException.class,
				() -> new Usuario("val", password) { });
	}


	@Test
	@DisplayName("Autenticar con credenciales correctas")
	void autenticarCorrecto() {
		assertTrue(usuario.autenticar("val", "clave123"));
	}

	@Test
	@DisplayName("Autenticar compara contenido, no referencias")
	void autenticarComparaContenido() {
		assertTrue(usuario.autenticar(new String("val"), new String("clave123")));
	}

	@Test
	@DisplayName("Autenticar con password incorrecto")
	void autenticarConPasswordIncorrecto() {
		assertFalse(usuario.autenticar("val", "otra"));
	}

	@Test
	@DisplayName("Autenticar con username incorrecto")
	void autenticarConUsernameIncorrecto() {
		assertFalse(usuario.autenticar("pepe", "clave123"));
	}

	@Test
	@DisplayName("Autenticar distingue mayúsculas de minúsculas")
	void autenticarDistingueMayusculas() {
		assertFalse(usuario.autenticar("VAL", "clave123"));
		assertFalse(usuario.autenticar("val", "CLAVE123"));
	}

	@Test
	@DisplayName("Autenticar con valores nulos devuelve falso y no explota")
	void autenticarConNulos() {
		assertFalse(usuario.autenticar(null, null));
	}


	@Test
	@DisplayName("Cambiar password con la clave actual correcta")
	void cambiarPasswordCorrecto() {
		assertTrue(usuario.cambiarPassword("clave123", "nueva456"));
		assertTrue(usuario.autenticar("val", "nueva456"));
		assertFalse(usuario.autenticar("val", "clave123"));
	}

	@Test
	@DisplayName("Con la clave actual incorrecta no se cambia nada")
	void cambiarPasswordConActualIncorrectaNoCambiaNada() {
		assertFalse(usuario.cambiarPassword("incorrecta", "nueva456"));
		assertTrue(usuario.autenticar("val", "clave123"));
	}

	@Test
	@DisplayName("Con la clave actual nula no se cambia nada y no explota")
	void cambiarPasswordConActualNulaNoCambiaNada() {
		assertFalse(usuario.cambiarPassword(null, "nueva456"));
		assertTrue(usuario.autenticar("val", "clave123"));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Una clave nueva nula, vacía o en blanco no se acepta")
	void cambiarPasswordConNuevaInvalidaNoCambiaNada(String nueva) {
		assertFalse(usuario.cambiarPassword("clave123", nueva));
		assertTrue(usuario.autenticar("val", "clave123"));
	}
}