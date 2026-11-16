package club.test.competencias;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.test.TestFixtures;
import club.modelo.competencias.CompetenciaFases;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.enums.Fase;
import club.modelo.partidos.PartidoExterno;
import club.modelo.usuarios.JugadorEquipo;

public class CompetenciaFasesTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private DeporteEquipo futbol;
	private CompetenciaFases ascun;
	private Equipo equipo;
	private List<JugadorEquipo> jugadores;

	@BeforeEach
	void setUp() {
		futbol = TestFixtures.futbol();
		ascun = new CompetenciaFases("ASCUN", "ASCUN", 2026, futbol, Categoria.MAYORES);
		equipo = TestFixtures.equipo("Halcones", futbol, Categoria.MAYORES);
		jugadores = TestFixtures.poblar(equipo, 7, "h");
		ascun.inscribirEquipo(equipo, jugadores);
	}

	private void jugar(String rival, int propios, int ajenos, int dia) {
		PartidoExterno p = new PartidoExterno(FECHA.plusDays(dia), equipo, rival, "Estadio", ascun);
		p.agregarParticipante(jugadores.get(0));
		p.registrarResultado(propios, ajenos);
		ascun.registrarPartido(p);
	}

	
	@Test
	@DisplayName("La tabla vacía no tiene equipos")
	void tablaVacia() {
		assertTrue(ascun.calcularTabla().isEmpty());
	}

	@Test
	@DisplayName("La tabla ordena por puntos")
	void tablaPorPuntos() {
		jugar("R1", 3, 0, 0);
		jugar("R2", 2, 2, 1);
		Map<String, Integer> tabla = ascun.calcularTabla();
		assertEquals(List.of("Halcones", "R2", "R1"), new ArrayList<String>(tabla.keySet()));
		assertEquals(4, tabla.get("Halcones"));
	}

	@Test
	@DisplayName("El empate en puntos se desempata por el enfrentamiento directo")
	void desempateDirecto() {
		jugar("R1", 3, 0, 0);
		jugar("R2", 0, 1, 1);
		Map<String, Integer> tabla = ascun.calcularTabla();
		assertEquals(3, tabla.get("Halcones"));
		assertEquals(3, tabla.get("R2"));
		assertEquals(List.of("R2", "Halcones", "R1"), new ArrayList<String>(tabla.keySet()));
	}
}