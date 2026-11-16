package club.test.personal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoAsistencia;
import club.modelo.personal.Entrenador;
import club.test.TestFixtures;
import club.modelo.usuarios.Jugador;

public class EntrenadorTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private Entrenador entrenador;

	@BeforeEach
	void setUp() {
		entrenador = new Entrenador("coach", "pw", "Carlos");
	}

	@Test
	@DisplayName("Un entrenador nuevo no tiene entrenamientos")
	void sinEntrenamientos() {
		assertTrue(entrenador.getEntrenamientos().isEmpty());
		assertTrue(entrenador.consultarEntrenamientos(FECHA, FECHA.plusDays(7)).isEmpty());
	}

	@Test
	@DisplayName("Consultar entrenamientos con fechas inválidas lanza excepción")
	void consultarInvalido() {
		assertThrows(IllegalArgumentException.class, () -> entrenador.consultarEntrenamientos(null, FECHA));
		assertThrows(IllegalArgumentException.class, () -> entrenador.consultarEntrenamientos(FECHA, null));
		assertThrows(IllegalArgumentException.class,
				() -> entrenador.consultarEntrenamientos(FECHA.plusDays(1), FECHA));
	}

	@Test
	@DisplayName("Planear con datos nulos lanza excepción")
	void planearNulos() {
		List<Jugador> convocados = new ArrayList<Jugador>();
		assertThrows(IllegalArgumentException.class,
				() -> entrenador.planearEntrenamiento(null, LocalTime.of(8, 0), 60, null, convocados));
		assertThrows(IllegalArgumentException.class,
				() -> entrenador.planearEntrenamiento(FECHA, null, 60, null, convocados));
		assertThrows(IllegalArgumentException.class,
				() -> entrenador.planearEntrenamiento(FECHA, LocalTime.of(8, 0), 60, null, null));
	}

	@Test
	@DisplayName("Planear con la sobrecarga de equipo y equipo nulo lanza excepción")
	void planearEquipoNulo() {
		assertThrows(IllegalArgumentException.class, () -> entrenador.planearEntrenamiento(FECHA,
				LocalTime.of(8, 0), 60, null, (Equipo) null, new ArrayList<Jugador>()));
	}

	@Test
	@DisplayName("Un entrenador dado de baja no puede planear")
	void planearEntrenadorInactivo() {
		entrenador.darDeBaja();
		assertThrows(IllegalStateException.class, () -> entrenador.planearEntrenamiento(FECHA,
				LocalTime.of(8, 0), 60, null, new ArrayList<Jugador>()));
	}

	@Test
	@DisplayName("Registrar asistencia con datos nulos lanza excepción")
	void asistenciaNulos() {
		Jugador j = TestFixtures.poblar(TestFixtures.equipo("Eq", TestFixtures.futbol(), Categoria.MAYORES), 1, "p")
				.get(0);
		assertThrows(IllegalArgumentException.class, () -> entrenador.registrarAsistencia(null, j, EstadoAsistencia.ASISTIO));
		assertThrows(IllegalArgumentException.class,
				() -> entrenador.registrarAsistencia((Entrenamiento) null, null, EstadoAsistencia.ASISTIO));
	}

	@Test
	@DisplayName("Evaluar con una sesión que no es del entrenador o nula lanza excepción")
	void evaluarNulos() {
		Jugador j = TestFixtures.poblar(TestFixtures.equipo("Eq", TestFixtures.futbol(), Categoria.MAYORES), 1, "p")
				.get(0);
		assertThrows(IllegalArgumentException.class, () -> entrenador.evaluarJugador(null, j, 3, "ok"));
	}
}