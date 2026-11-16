package club.test.deportes;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;

public class DeporteTest {

	private Deporte crear(String nombre) {
		return new Deporte(nombre) {
		};
	}

	@Test
	@DisplayName("Un deporte guarda su nombre")
	void guardaNombre() {
		assertEquals("Fútbol", crear("Fútbol").getNombre());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Un nombre inválido lanza excepción")
	void nombreInvalido(String nombre) {
		assertThrows(IllegalArgumentException.class, () -> crear(nombre));
	}

	@Test
	@DisplayName("toString devuelve el nombre")
	void toStringNombre() {
		assertEquals("Tenis", crear("Tenis").toString());
	}

	@Test
	@DisplayName("Dos deportes del mismo tipo y nombre son iguales y tienen el mismo hash")
	void igualdadMismoNombre() {
		Deporte a = crear("Fútbol");
		Deporte b = crear("Fútbol");
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	@DisplayName("Dos deportes del mismo tipo con nombre distinto no son iguales")
	void igualdadNombreDistinto() {
		assertNotEquals(crear("Fútbol"), crear("Baloncesto"));
	}

	@Test
	@DisplayName("La igualdad distingue mayúsculas")
	void igualdadMayusculas() {
		assertNotEquals(crear("Fútbol"), crear("fútbol"));
	}

	@Test
	@DisplayName("Un deporte es igual a sí mismo y distinto de null o de otro tipo de objeto")
	void igualdadCasosBorde() {
		Deporte d = crear("Fútbol");
		assertEquals(d, d);
		assertNotEquals(d, null);
		assertNotEquals(d, "Fútbol");
	}

	@Test
	@DisplayName("Un deporte de equipo y uno individual con el mismo nombre no son iguales")
	void igualdadTiposDistintos() {
		Deporte equipo = new DeporteEquipo("Mixto", 5, true, 3, 1, 0);
		Deporte individual = new DeporteIndividual("Mixto", 2);
		assertNotEquals(equipo, individual);
		assertNotEquals(individual, equipo);
	}

	@Test
	@DisplayName("Dos deportes de equipo con el mismo nombre son iguales aunque cambien sus reglas")
	void igualdadPorNombreEnSubclase() {
		Deporte a = new DeporteEquipo("Fútbol", 7, true, 3, 1, 0);
		Deporte b = new DeporteEquipo("Fútbol", 9, false, 2, 0, 1);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	@DisplayName("En un conjunto no se duplican deportes con el mismo nombre")
	void usoEnConjuntos() {
		Set<Deporte> conjunto = new HashSet<Deporte>();
		conjunto.add(new DeporteIndividual("Tenis", 4));
		conjunto.add(new DeporteIndividual("Tenis", 2));
		conjunto.add(new DeporteIndividual("Pádel", 3));
		assertEquals(2, conjunto.size());
		assertTrue(conjunto.contains(new DeporteIndividual("Pádel", 1)));
	}
}