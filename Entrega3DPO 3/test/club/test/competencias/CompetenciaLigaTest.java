package club.test.competencias;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import club.test.TestFixtures;
import club.modelo.competencias.CompetenciaLiga;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.partidos.PartidoExterno;
import club.modelo.usuarios.JugadorEquipo;

public class CompetenciaLigaTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private DeporteEquipo futbol;
	private CompetenciaLiga liga;
	private Equipo equipo;
	private List<JugadorEquipo> jugadores;

	@BeforeEach
	void setUp() {
		futbol = TestFixtures.futbol();
		liga = new CompetenciaLiga("Torneo de los Cerros", "Cerros", 2026, futbol, Categoria.MAYORES);
		equipo = TestFixtures.equipo("Halcones", futbol, Categoria.MAYORES);
		jugadores = TestFixtures.poblar(equipo, 8, "h");
	}

	private PartidoExterno jugar(String rival, int propios, int ajenos, int dia) {
		PartidoExterno p = new PartidoExterno(FECHA.plusDays(dia), equipo, rival, "Estadio", liga);
		p.agregarParticipante(jugadores.get(0));
		p.registrarResultado(propios, ajenos);
		liga.registrarPartido(p);
		return p;
	}

	@Test
	@DisplayName("Una competencia válida guarda sus datos")
	void competenciaValida() {
		assertEquals("Torneo de los Cerros", liga.getNombre());
		assertEquals("Cerros", liga.getEntidadOrganizadora());
		assertEquals(2026, liga.getTemporada());
		assertEquals(futbol, liga.getDeporte());
		assertEquals(Categoria.MAYORES, liga.getCategoria());
		assertTrue(liga.getEquiposInscritos().isEmpty());
		assertTrue(liga.getPartidos().isEmpty());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "  " })
	@DisplayName("Nombre o entidad inválidos lanzan excepción")
	void textosInvalidos(String texto) {
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaLiga(texto, "Org", 2026, futbol, Categoria.MAYORES));
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaLiga("Liga", texto, 2026, futbol, Categoria.MAYORES));
	}

	@Test
	@DisplayName("Temporada, deporte o categoría inválidos lanzan excepción")
	void otrosInvalidos() {
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaLiga("Liga", "Org", 0, futbol, Categoria.MAYORES));
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaLiga("Liga", "Org", -2026, futbol, Categoria.MAYORES));
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaLiga("Liga", "Org", 2026, null, Categoria.MAYORES));
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaLiga("Liga", "Org", 2026, futbol, null));
	}

	@Test
	@DisplayName("Inscribir un equipo con la categoría correcta y nómina suficiente")
	void inscripcionValida() {
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		assertTrue(liga.estaInscrito(equipo));
		assertEquals(1, liga.getEquiposInscritos().size());
		assertEquals(7, liga.getNomina(equipo).size());
		assertTrue(liga.estaEnNomina(equipo, jugadores.get(0)));
		assertFalse(liga.estaEnNomina(equipo, jugadores.get(7)));
	}

	@Test
	@DisplayName("Equipo o nómina nulos lanzan excepción")
	void inscripcionNulos() {
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(null, jugadores));
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(equipo, null));
	}

	@Test
	@DisplayName("Un equipo no se puede inscribir dos veces")
	void inscripcionDoble() {
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(equipo, jugadores.subList(0, 7)));
	}

	@Test
	@DisplayName("Un equipo inactivo se rechaza")
	void equipoInactivo() {
		equipo.darDeBaja();
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(equipo, jugadores.subList(0, 7)));
	}

	@Test
	@DisplayName("Un equipo de otro deporte se rechaza")
	void otroDeporte() {
		Equipo basket = TestFixtures.equipo("Torres", TestFixtures.baloncesto(), Categoria.MAYORES);
		List<JugadorEquipo> js = TestFixtures.poblar(basket, 8, "t");
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(basket, js));
	}

	@Test
	@DisplayName("Un equipo de otra categoría se rechaza")
	void otraCategoria() {
		Equipo sub17 = TestFixtures.equipo("Menores", futbol, Categoria.SUB17);
		List<JugadorEquipo> js = TestFixtures.poblar(sub17, 8, "m");
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(sub17, js));
	}

	@Test
	@DisplayName("Un equipo sin el mínimo de jugadores se rechaza")
	void equipoSinMinimo() {
		Equipo corto = TestFixtures.equipo("Corto", futbol, Categoria.MAYORES);
		List<JugadorEquipo> js = TestFixtures.poblar(corto, 6, "c");
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(corto, js));
		assertFalse(liga.estaInscrito(corto));
	}

	@Test
	@DisplayName("Una nómina con jugadores repetidos se rechaza")
	void nominaRepetidos() {
		List<JugadorEquipo> nomina = new ArrayList<JugadorEquipo>(jugadores.subList(0, 7));
		nomina.set(6, nomina.get(0));
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(equipo, nomina));
	}

	@Test
	@DisplayName("Una nómina con un jugador nulo se rechaza")
	void nominaNulo() {
		List<JugadorEquipo> nomina = new ArrayList<JugadorEquipo>(jugadores.subList(0, 7));
		nomina.set(6, null);
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(equipo, nomina));
	}

	@Test
	@DisplayName("Una nómina con un jugador que no es del equipo se rechaza")
	void nominaAjeno() {
		Equipo otro = TestFixtures.equipo("Otro", futbol, Categoria.MAYORES);
		JugadorEquipo ajeno = TestFixtures.poblar(otro, 1, "z").get(0);
		List<JugadorEquipo> nomina = new ArrayList<JugadorEquipo>(jugadores.subList(0, 6));
		nomina.add(ajeno);
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(equipo, nomina));
	}

	@Test
	@DisplayName("Una nómina con un jugador lesionado se rechaza")
	void nominaLesionado() {
		jugadores.get(7).getHistorialMedico().registrarLesion("Rodilla");
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(equipo, jugadores));
	}

	@Test
	@DisplayName("Una nómina menor al mínimo se rechaza")
	void nominaCorta() {
		assertThrows(IllegalArgumentException.class, () -> liga.inscribirEquipo(equipo, jugadores.subList(0, 6)));
		assertFalse(liga.estaInscrito(equipo));
	}

	@Test
	@DisplayName("La nómina se guarda como copia")
	void nominaCopia() {
		List<JugadorEquipo> nomina = new ArrayList<JugadorEquipo>(jugadores.subList(0, 7));
		liga.inscribirEquipo(equipo, nomina);
		nomina.clear();
		assertEquals(7, liga.getNomina(equipo).size());
		liga.getNomina(equipo).clear();
		assertEquals(7, liga.getNomina(equipo).size());
	}

	@Test
	@DisplayName("Consultar la nómina de un equipo no inscrito lanza excepción")
	void nominaNoInscrito() {
		assertThrows(IllegalArgumentException.class, () -> liga.getNomina(equipo));
		assertFalse(liga.estaEnNomina(equipo, jugadores.get(0)));
	}

	@Test
	@DisplayName("Registrar un partido nulo lanza excepción")
	void partidoNulo() {
		assertThrows(IllegalArgumentException.class, () -> liga.registrarPartido(null));
	}

	@Test
	@DisplayName("Un partido de otra competencia o amistoso se rechaza")
	void partidoOtraCompetencia() {
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		PartidoExterno amistoso = new PartidoExterno(FECHA, equipo, "U", "E");
		assertThrows(IllegalArgumentException.class, () -> liga.registrarPartido(amistoso));
	}

	@Test
	@DisplayName("Registrar un partido de la competencia")
	void registrarPartido() {
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		PartidoExterno p = jugar("R1", 2, 0, 0);
		assertTrue(liga.getPartidos().contains(p));
	}

	@Test
	@DisplayName("Un partido no se puede registrar dos veces")
	void partidoDoble() {
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		PartidoExterno p = jugar("R1", 2, 0, 0);
		assertThrows(IllegalStateException.class, () -> liga.registrarPartido(p));
	}

	@Test
	@DisplayName("La tabla vacía no tiene equipos")
	void tablaVacia() {
		assertTrue(liga.calcularTabla().isEmpty());
	}

	@Test
	@DisplayName("La tabla suma 3 por victoria, 1 por empate y 0 por derrota, ordenada de mayor a menor")
	void tablaPuntos() {
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		jugar("R1", 3, 1, 0);
		jugar("R2", 1, 1, 1);
		jugar("R3", 0, 2, 2);
		Map<String, Integer> tabla = liga.calcularTabla();
		assertEquals(List.of("Halcones", "R3", "R2", "R1"), new ArrayList<String>(tabla.keySet()));
		assertEquals(4, tabla.get("Halcones"));
		assertEquals(3, tabla.get("R3"));
		assertEquals(1, tabla.get("R2"));
		assertEquals(0, tabla.get("R1"));
	}

	@Test
	@DisplayName("Un partido sin jugar no suma puntos")
	void partidoSinJugar() {
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		PartidoExterno p = new PartidoExterno(FECHA, equipo, "R1", "Estadio", liga);
		liga.registrarPartido(p);
		assertTrue(liga.calcularTabla().isEmpty());
	}

	@Test
	@DisplayName("Los puntos se acumulan por equipo a lo largo de varios partidos")
	void puntosAcumulados() {
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		jugar("R1", 1, 0, 0);
		jugar("R1", 2, 0, 1);
		assertEquals(6, liga.calcularTabla().get("Halcones"));
		assertEquals(0, liga.calcularTabla().get("R1"));
	}

	@Test
	@DisplayName("La lista de equipos inscritos y de partidos son copias")
	void listasCopia() {
		liga.inscribirEquipo(equipo, jugadores.subList(0, 7));
		jugar("R1", 1, 0, 0);
		liga.getEquiposInscritos().clear();
		liga.getPartidos().clear();
		assertEquals(1, liga.getEquiposInscritos().size());
		assertEquals(1, liga.getPartidos().size());
	}
}
