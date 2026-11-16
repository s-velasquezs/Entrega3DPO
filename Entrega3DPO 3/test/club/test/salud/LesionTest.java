package club.test.salud;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.modelo.salud.Lesion;

public class LesionTest {

	private static final LocalDate REGISTRO = LocalDate.of(2026, 10, 1);
	private Lesion lesion;

	@BeforeEach
	void setUp() {
		lesion = new Lesion("Esguince de tobillo", REGISTRO);
	}

	@Test
	@DisplayName("Una lesión nueva está activa y guarda sus datos")
	void lesionNuevaActiva() {
		assertTrue(lesion.estaActiva());
		assertEquals("Esguince de tobillo", lesion.getDescripcion());
		assertEquals(REGISTRO, lesion.getFechaRegistro());
		assertNull(lesion.getFechaAlta());
		assertNull(lesion.getNotaAlta());
	}

	@Test
	@DisplayName("El constructor sin fecha usa la fecha actual")
	void constructorSinFecha() {
		assertEquals(LocalDate.now(), new Lesion("Golpe").getFechaRegistro());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Una descripción inválida lanza excepción")
	void descripcionInvalida(String descripcion) {
		assertThrows(IllegalArgumentException.class, () -> new Lesion(descripcion));
	}

	@Test
	@DisplayName("Una fecha de registro nula lanza excepción")
	void fechaRegistroNula() {
		assertThrows(IllegalArgumentException.class, () -> new Lesion("Golpe", null));
	}

	@Test
	@DisplayName("Dar alta cierra la lesión")
	void darAlta() {
		LocalDate alta = REGISTRO.plusDays(10);
		lesion.darAlta("Recuperado", alta);
		assertFalse(lesion.estaActiva());
		assertEquals(alta, lesion.getFechaAlta());
		assertEquals("Recuperado", lesion.getNotaAlta());
	}

	@Test
	@DisplayName("El alta el mismo día del registro es válida")
	void altaMismoDia() {
		lesion.darAlta("Leve", REGISTRO);
		assertFalse(lesion.estaActiva());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Una nota de alta inválida lanza excepción y la lesión sigue activa")
	void notaInvalida(String nota) {
		assertThrows(IllegalArgumentException.class, () -> lesion.darAlta(nota, REGISTRO));
		assertTrue(lesion.estaActiva());
	}

	@Test
	@DisplayName("Una fecha de alta nula lanza excepción")
	void fechaAltaNula() {
		assertThrows(IllegalArgumentException.class, () -> lesion.darAlta("ok", null));
		assertTrue(lesion.estaActiva());
	}

	@Test
	@DisplayName("Un alta anterior al registro lanza excepción")
	void altaAnterior() {
		assertThrows(IllegalArgumentException.class, () -> lesion.darAlta("ok", REGISTRO.minusDays(1)));
		assertTrue(lesion.estaActiva());
	}

	@Test
	@DisplayName("No se puede dar alta dos veces")
	void altaDoble() {
		lesion.darAlta("ok", REGISTRO.plusDays(1));
		assertThrows(IllegalStateException.class, () -> lesion.darAlta("otra", REGISTRO.plusDays(2)));
		assertEquals("ok", lesion.getNotaAlta());
	}
}