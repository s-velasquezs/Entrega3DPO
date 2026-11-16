package club.test.deportes;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.modelo.deportes.DeporteIndividual;

public class DeporteIndividualTest {

	@Test
	@DisplayName("Un deporte individual válido guarda sus datos")
	void guardaDatos() {
		DeporteIndividual tenis = new DeporteIndividual("Tenis", 4);
		assertEquals("Tenis", tenis.getNombre());
		assertEquals(4, tenis.getMaxInstalaciones());
		assertEquals("Tenis", tenis.getDeporteIndividual());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -1, -5 })
	@DisplayName("Un máximo de instalaciones menor o igual a cero lanza excepción")
	void maximoInvalido(int max) {
		assertThrows(IllegalArgumentException.class, () -> new DeporteIndividual("Tenis", max));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "  " })
	@DisplayName("Un nombre inválido lanza excepción")
	void nombreInvalido(String nombre) {
		assertThrows(IllegalArgumentException.class, () -> new DeporteIndividual(nombre, 2));
	}

	@Test
	@DisplayName("Igualdad por nombre y clase")
	void igualdad() {
		assertEquals(new DeporteIndividual("Padel", 3), new DeporteIndividual("Padel", 5));
		assertEquals(new DeporteIndividual("Padel", 3).hashCode(), new DeporteIndividual("Padel", 5).hashCode());
		assertNotEquals(new DeporteIndividual("Padel", 3), new DeporteIndividual("Tenis", 3));
	}
}