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

import club.modelo.competencias.Competencia;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.partidos.PartidoExterno;
import club.modelo.usuarios.JugadorEquipo;
import club.test.TestFixtures;

public class CompetenciaTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);

	private static class CompetenciaSimple extends Competencia {

		CompetenciaSimple(String nombre, String entidad, int temporada, DeporteEquipo deporte, Categoria categoria) {
			super(nombre, entidad, temporada, deporte, categoria);
		}

		@Override
		public Map<String, Integer> calcularTabla() {
			return ordenarPorPuntos(acumularPuntos());
		}
	}

	private DeporteEquipo futbol;
	private CompetenciaSimple competencia;
	private Equipo equipo;
	private List<JugadorEquipo> jugadores;

	@BeforeEach
	void setUp() {
		futbol = TestFixtures.futbol();
		competencia = new CompetenciaSimple("Copa", "Liga Capital", 2026, futbol, Categoria.MAYORES);
		equipo = TestFixtures.equipo("Halcones", futbol, Categoria.MAYORES);
		jugadores = TestFixtures.poblar(equipo, 8, "h");
	}

	private PartidoExterno jugar(String rival, int propios, int ajenos, int dia) {
		PartidoExterno p = new PartidoExterno(FECHA.plusDays(dia), equipo, rival, "Estadio", competencia);
		p.agregarParticipante(jugadores.get(0));
		p.registrarResultado(propios, ajenos);
		competencia.registrarPartido(p);
		return p;
	}

	@Test
	@DisplayName("Una competencia válida guarda sus datos y empieza vacía")
	void datosIniciales() {
		assertEquals("Copa", competencia.getNombre());
		assertEquals("Liga Capital", competencia.getEntidadOrganizadora());
		assertEquals(2026, competencia.getTemporada());
		assertSame(futbol, competencia.getDeporte());
		assertEquals(Categoria.MAYORES, competencia.getCategoria());
		assertTrue(competencia.getEquiposInscritos().isEmpty());
		assertTrue(competencia.getPartidos().isEmpty());
		assertTrue(competencia.calcularTabla().isEmpty());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   " })
	@DisplayName("Un nombre o entidad inválidos lanzan excepción")
	void textosInvalidos(String texto) {
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaSimple(texto, "Liga", 2026, futbol, Categoria.MAYORES));
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaSimple("Copa", texto, 2026, futbol, Categoria.MAYORES));
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -1, -2026 })
	@DisplayName("Una temporada no positiva lanza excepción")
	void temporadaInvalida(int temporada) {
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaSimple("Copa", "Liga", temporada, futbol, Categoria.MAYORES));
	}

	@Test
	@DisplayName("Deporte o categoría nulos lanzan excepción")
	void deporteCategoriaNulos() {
		assertThrows(IllegalArgumentException.class,
				() -> new CompetenciaSimple("Copa", "Liga", 2026, null, Categoria.MAYORES));
		assertThrows(IllegalArgumentException.class, () -> new CompetenciaSimple("Copa", "Liga", 2026, futbol, null));
	}

	@Test
	@DisplayName("Inscribir un equipo con una nómina válida")
	void inscribirEquipo() {
		competencia.inscribirEquipo(equipo, jugadores.subList(0, 7));
		assertTrue(competencia.estaInscrito(equipo));
		assertEquals(List.of(equipo), competencia.getEquiposInscritos());
		assertEquals(7, competencia.getNomina(equipo).size());
		assertTrue(competencia.estaEnNomina(equipo, jugadores.get(0)));
		assertFalse(competencia.estaEnNomina(equipo, jugadores.get(7)));
	}

	@Test
	@DisplayName("Un equipo no inscrito no está inscrito ni tiene nómina")
	void equipoNoInscrito() {
		assertFalse(competencia.estaInscrito(equipo));
		assertFalse(competencia.estaEnNomina(equipo, jugadores.get(0)));
		assertThrows(IllegalArgumentException.class, () -> competencia.getNomina(equipo));
	}

	@Test
	@DisplayName("Equipo o nómina nulos lanzan excepción")
	void inscribirNulos() {
		assertThrows(IllegalArgumentException.class, () -> competencia.inscribirEquipo(null, jugadores));
		assertThrows(IllegalArgumentException.class, () -> competencia.inscribirEquipo(equipo, null));
	}

	@Test
	@DisplayName("No se puede inscribir dos veces el mismo equipo")
	void inscribirDoble() {
		competencia.inscribirEquipo(equipo, jugadores.subList(0, 7));
		assertThrows(IllegalArgumentException.class,
				() -> competencia.inscribirEquipo(equipo, jugadores.subList(0, 7)));
		assertEquals(1, competencia.getEquiposInscritos().size());
	}

	@Test
	@DisplayName("Un equipo inactivo no se inscribe")
	void equipoInactivo() {
		equipo.darDeBaja();
		assertThrows(IllegalArgumentException.class,
				() -> competencia.inscribirEquipo(equipo, jugadores.subList(0, 7)));
	}

	@Test
	@DisplayName("Un equipo de otro deporte o de otra categoría no se inscribe")
	void otroDeporteOCategoria() {
		DeporteEquipo basket = TestFixtures.baloncesto();
		Equipo equipoBasket = TestFixtures.equipo("Aros", basket, Categoria.MAYORES);
		List<JugadorEquipo> jb = TestFixtures.poblar(equipoBasket, 6, "b");
		Equipo sub17 = TestFixtures.equipo("Juveniles", futbol, Categoria.SUB17);
		List<JugadorEquipo> js = TestFixtures.poblar(sub17, 8, "s");
		assertThrows(IllegalArgumentException.class, () -> competencia.inscribirEquipo(equipoBasket, jb));
		assertThrows(IllegalArgumentException.class, () -> competencia.inscribirEquipo(sub17, js));
	}

	@Test
	@DisplayName("Una nómina menor al mínimo de habilitados se rechaza")
	void nominaCorta() {
		assertThrows(IllegalArgumentException.class,
				() -> competencia.inscribirEquipo(equipo, jugadores.subList(0, 6)));
		assertFalse(competencia.estaInscrito(equipo));
	}

	@Test
	@DisplayName("Una nómina con repetidos, nulos, ajenos o lesionados se rechaza")
	void nominasInvalidas() {
		List<JugadorEquipo> repetidos = new ArrayList<JugadorEquipo>(jugadores.subList(0, 7));
		repetidos.add(jugadores.get(0));
		List<JugadorEquipo> conNulo = new ArrayList<JugadorEquipo>(jugadores.subList(0, 7));
		conNulo.add(null);
		Equipo otro = TestFixtures.equipo("Otro", futbol, Categoria.MAYORES);
		JugadorEquipo ajeno = TestFixtures.poblar(otro, 1, "x").get(0);
		List<JugadorEquipo> conAjeno = new ArrayList<JugadorEquipo>(jugadores.subList(0, 7));
		conAjeno.add(ajeno);
		assertThrows(IllegalArgumentException.class, () -> competencia.inscribirEquipo(equipo, repetidos));
		assertThrows(IllegalArgumentException.class, () -> competencia.inscribirEquipo(equipo, conNulo));
		assertThrows(IllegalArgumentException.class, () -> competencia.inscribirEquipo(equipo, conAjeno));
		jugadores.get(0).getHistorialMedico().registrarLesion("Esguince");
		assertThrows(IllegalArgumentException.class,
				() -> competencia.inscribirEquipo(equipo, jugadores.subList(0, 7)));
		assertFalse(competencia.estaInscrito(equipo));
	}

	@Test
	@DisplayName("La nómina guardada y la devuelta son copias")
	void nominaCopia() {
		List<JugadorEquipo> original = new ArrayList<JugadorEquipo>(jugadores.subList(0, 7));
		competencia.inscribirEquipo(equipo, original);
		original.clear();
		assertEquals(7, competencia.getNomina(equipo).size());
		competencia.getNomina(equipo).clear();
		assertEquals(7, competencia.getNomina(equipo).size());
		competencia.getEquiposInscritos().clear();
		assertEquals(1, competencia.getEquiposInscritos().size());
	}

	@Test
	@DisplayName("Registrar un partido nulo o de otra competencia lanza excepción")
	void registrarPartidoInvalido() {
		competencia.inscribirEquipo(equipo, jugadores.subList(0, 7));
		CompetenciaSimple otra = new CompetenciaSimple("Otra", "Liga", 2026, futbol, Categoria.MAYORES);
		PartidoExterno ajeno = new PartidoExterno(FECHA, equipo, "Rival", "Estadio", otra);
		assertThrows(IllegalArgumentException.class, () -> competencia.registrarPartido(null));
		assertThrows(IllegalArgumentException.class, () -> competencia.registrarPartido(ajeno));
	}

	@Test
	@DisplayName("Registrar un partido de un equipo no inscrito lanza excepción")
	void registrarPartidoSinInscribir() {
		PartidoExterno p = new PartidoExterno(FECHA, equipo, "Rival", "Estadio", competencia);
		p.agregarParticipante(jugadores.get(0));
		assertThrows(IllegalArgumentException.class, () -> competencia.registrarPartido(p));
		assertTrue(competencia.getPartidos().isEmpty());
	}

	@Test
	@DisplayName("Un partido con un jugador fuera de la nómina se rechaza")
	void registrarPartidoFueraDeNomina() {
		competencia.inscribirEquipo(equipo, jugadores.subList(0, 7));
		PartidoExterno p = new PartidoExterno(FECHA, equipo, "Rival", "Estadio", competencia);
		p.agregarParticipante(jugadores.get(7));
		assertThrows(IllegalArgumentException.class, () -> competencia.registrarPartido(p));
	}

	@Test
	@DisplayName("Registrar un partido válido lo agrega y no se puede repetir")
	void registrarPartido() {
		competencia.inscribirEquipo(equipo, jugadores.subList(0, 7));
		PartidoExterno p = jugar("Rival", 1, 0, 0);
		assertEquals(List.of(p), competencia.getPartidos());
		assertThrows(IllegalStateException.class, () -> competencia.registrarPartido(p));
		competencia.getPartidos().clear();
		assertEquals(1, competencia.getPartidos().size());
	}

	@Test
	@DisplayName("La tabla suma 3 por victoria, 1 por empate y 0 por derrota")
	void tablaPuntos() {
		competencia.inscribirEquipo(equipo, jugadores.subList(0, 7));
		jugar("Rival A", 2, 0, 0);
		jugar("Rival B", 1, 1, 1);
		jugar("Rival C", 0, 3, 2);
		Map<String, Integer> tabla = competencia.calcularTabla();
		assertEquals(4, tabla.get("Halcones"));
		assertEquals(0, tabla.get("Rival A"));
		assertEquals(1, tabla.get("Rival B"));
		assertEquals(3, tabla.get("Rival C"));
	}

	@Test
	@DisplayName("La tabla se ordena de mayor a menor puntaje")
	void tablaOrdenada() {
		competencia.inscribirEquipo(equipo, jugadores.subList(0, 7));
		jugar("Rival A", 2, 0, 0);
		jugar("Rival B", 3, 1, 1);
		jugar("Rival C", 0, 1, 2);
		List<Integer> valores = new ArrayList<Integer>(competencia.calcularTabla().values());
		for (int i = 1; i < valores.size(); i++) {
			assertTrue(valores.get(i - 1) >= valores.get(i));
		}
		assertEquals("Halcones", competencia.calcularTabla().keySet().iterator().next());
	}

	@Test
	@DisplayName("Los partidos sin jugar no suman puntos")
	void partidosSinJugar() {
		competencia.inscribirEquipo(equipo, jugadores.subList(0, 7));
		PartidoExterno p = new PartidoExterno(FECHA, equipo, "Rival", "Estadio", competencia);
		p.agregarParticipante(jugadores.get(0));
		competencia.registrarPartido(p);
		assertTrue(competencia.calcularTabla().isEmpty());
	}
}