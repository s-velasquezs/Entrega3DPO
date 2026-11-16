package club.test.partidos;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.test.TestFixtures;
import club.modelo.competencias.CompetenciaLiga;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.enums.Nivel;
import club.modelo.partidos.PartidoExterno;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;

public class PartidoExternoTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private DeporteEquipo futbol;
	private Equipo equipo;
	private List<JugadorEquipo> jugadores;
	private PartidoExterno amistoso;

	@BeforeEach
	void setUp() {
		futbol = TestFixtures.futbol();
		equipo = TestFixtures.equipo("Halcones", futbol, Categoria.MAYORES);
		jugadores = TestFixtures.poblar(equipo, 8, "h");
		amistoso = new PartidoExterno(FECHA, equipo, "Universidad X", "Estadio Central");
	}

	private CompetenciaLiga ligaConEquipo() {
		CompetenciaLiga liga = new CompetenciaLiga("Torneo de los Cerros", "Cerros", 2026, futbol,
				Categoria.MAYORES);
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		return liga;
	}

	@Test
	@DisplayName("Un partido amistoso guarda sus datos")
	void amistoso() {
		assertSame(equipo, amistoso.getEquipoClub());
		assertEquals("Universidad X", amistoso.getInstitucionRival());
		assertEquals("Estadio Central", amistoso.getEscenario());
		assertNull(amistoso.getCompetencia());
		assertTrue(amistoso.esAmistoso());
		assertEquals(Categoria.MAYORES, amistoso.getCategoria());
		assertEquals(futbol, amistoso.getDeporte());
	}

	@Test
	@DisplayName("Un equipo nulo lanza excepción")
	void equipoNulo() {
		assertThrows(IllegalArgumentException.class, () -> new PartidoExterno(FECHA, null, "U", "E"));
	}

	@Test
	@DisplayName("Rival o escenario inválidos lanzan excepción")
	void rivalEscenarioInvalidos() {
		assertThrows(IllegalArgumentException.class, () -> new PartidoExterno(FECHA, equipo, null, "E"));
		assertThrows(IllegalArgumentException.class, () -> new PartidoExterno(FECHA, equipo, "  ", "E"));
		assertThrows(IllegalArgumentException.class, () -> new PartidoExterno(FECHA, equipo, "U", null));
		assertThrows(IllegalArgumentException.class, () -> new PartidoExterno(FECHA, equipo, "U", " "));
	}

	@Test
	@DisplayName("Un equipo inactivo lanza excepción")
	void equipoInactivo() {
		equipo.darDeBaja();
		assertThrows(IllegalArgumentException.class, () -> new PartidoExterno(FECHA, equipo, "U", "E"));
	}

	@Test
	@DisplayName("Un partido de competencia es válido si el equipo está inscrito")
	void partidoDeCompetencia() {
		CompetenciaLiga liga = ligaConEquipo();
		PartidoExterno p = new PartidoExterno(FECHA, equipo, "Universidad X", "Estadio", liga);
		assertSame(liga, p.getCompetencia());
		assertFalse(p.esAmistoso());
	}

	@Test
	@DisplayName("El equipo debe estar inscrito en la competencia")
	void equipoNoInscrito() {
		CompetenciaLiga liga = new CompetenciaLiga("Cerros", "Cerros", 2026, futbol, Categoria.MAYORES);
		assertThrows(IllegalArgumentException.class, () -> new PartidoExterno(FECHA, equipo, "U", "E", liga));
	}

	@Test
	@DisplayName("La competencia debe ser del mismo deporte")
	void competenciaOtroDeporte() {
		CompetenciaLiga basket = new CompetenciaLiga("Liga", "Org", 2026, TestFixtures.baloncesto(),
				Categoria.MAYORES);
		assertThrows(IllegalArgumentException.class, () -> new PartidoExterno(FECHA, equipo, "U", "E", basket));
	}

	@Test
	@DisplayName("La competencia debe ser de la misma categoría")
	void competenciaOtraCategoria() {
		CompetenciaLiga sub17 = new CompetenciaLiga("Liga", "Org", 2026, futbol, Categoria.SUB17);
		assertThrows(IllegalArgumentException.class, () -> new PartidoExterno(FECHA, equipo, "U", "E", sub17));
	}

	@Test
	@DisplayName("Agregar un jugador del equipo del club a un amistoso")
	void participanteDelEquipo() {
		amistoso.agregarParticipante(jugadores.get(0));
		assertEquals(1, amistoso.getParticipantes().size());
	}

	@Test
	@DisplayName("Un jugador nulo lanza excepción")
	void participanteNulo() {
		assertThrows(IllegalArgumentException.class, () -> amistoso.agregarParticipante(null));
	}

	@Test
	@DisplayName("Un jugador de otro equipo se rechaza")
	void participanteAjeno() {
		Equipo otro = TestFixtures.equipo("Otro", futbol, Categoria.MAYORES);
		JugadorEquipo ajeno = TestFixtures.poblar(otro, 1, "z").get(0);
		assertThrows(IllegalArgumentException.class, () -> amistoso.agregarParticipante(ajeno));
	}

	@Test
	@DisplayName("Un jugador individual se rechaza")
	void participanteIndividual() {
		JugadorIndividual ind = new JugadorIndividual("i", "pw", "I", 20, false);
		ind.inscribirseDeporte(TestFixtures.tenis(), Nivel.AVANZADO);
		assertThrows(IllegalArgumentException.class, () -> amistoso.agregarParticipante(ind));
	}

	@Test
	@DisplayName("En competencia solo participan los jugadores de la nómina")
	void soloNomina() {
		CompetenciaLiga liga = ligaConEquipo();
		PartidoExterno p = new PartidoExterno(FECHA, equipo, "U", "E", liga);
		p.agregarParticipante(jugadores.get(0));
		assertThrows(IllegalArgumentException.class, () -> p.agregarParticipante(jugadores.get(7)));
		assertEquals(1, p.getParticipantes().size());
	}

	@Test
	@DisplayName("Un jugador lesionado no puede participar")
	void participanteLesionado() {
		JugadorEquipo j = jugadores.get(0);
		j.getHistorialMedico().registrarLesion("Tobillo");
		assertThrows(IllegalArgumentException.class, () -> amistoso.agregarParticipante(j));
	}

	@Test
	@DisplayName("estaJugado empieza en falso y pasa a verdadero sin recursión infinita")
	void estaJugado() {
		assertFalse(amistoso.estaJugado());
		amistoso.registrarResultado(2, 1);
		assertTrue(amistoso.estaJugado());
	}

	@Test
	@DisplayName("Validar con el mínimo de jugadores habilitados")
	void validarConMinimo() {
		assertDoesNotThrow(() -> amistoso.validar());
	}

	@Test
	@DisplayName("Validar sin el mínimo de jugadores habilitados lanza excepción")
	void validarSinMinimo() {
		jugadores.get(0).getHistorialMedico().registrarLesion("Rodilla");
		jugadores.get(1).getHistorialMedico().registrarLesion("Codo");
		assertThrows(IllegalStateException.class, () -> amistoso.validar());
	}

	@Test
	@DisplayName("Validar un partido jugado sin participantes lanza excepción")
	void validarJugadoSinParticipantes() {
		amistoso.registrarResultado(1, 0);
		assertThrows(IllegalStateException.class, () -> amistoso.validar());
	}

	@Test
	@DisplayName("Un partido jugado con participantes valida")
	void validarJugadoConParticipantes() {
		amistoso.agregarParticipante(jugadores.get(0));
		amistoso.registrarResultado(1, 0);
		assertDoesNotThrow(() -> amistoso.validar());
	}

	@Test
	@DisplayName("Registrar un marcador negativo o repetir el resultado falla")
	void resultadoInvalido() {
		assertThrows(IllegalArgumentException.class, () -> amistoso.registrarResultado(-1, 0));
		amistoso.registrarResultado(1, 0);
		assertThrows(IllegalStateException.class, () -> amistoso.registrarResultado(2, 0));
	}
}