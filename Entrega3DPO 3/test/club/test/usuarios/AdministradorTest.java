package club.test.usuarios;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import club.test.TestFixtures;
import club.modelo.comercio.Inventario;
import club.modelo.competencias.Competencia;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoSolicitud;
import club.modelo.enums.Nivel;
import club.modelo.partidos.Partido;
import club.modelo.partidos.PartidoInterno;
import club.modelo.personal.Empleado;
import club.modelo.personal.Entrenador;
import club.modelo.personal.Fisioterapeuta;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.personal.Turno;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;

public class AdministradorTest {

	private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);
	private Administrador admin;
	private DeporteEquipo futbol;

	@BeforeEach
	void setUp() {
		admin = new Administrador("admin", "adm123", new Inventario());
		futbol = TestFixtures.futbol();
	}

	private Turno turno(Empleado e, LocalDate fecha, int ini, int fin) {
		return new Turno(e, fecha, LocalTime.of(ini, 0), LocalTime.of(fin, 0));
	}

	private List<JugadorEquipo> equipoConJugadores(String nombre, int cantidad, String prefijo) {
		Equipo e = admin.crearEquipo(nombre, Categoria.MAYORES, futbol, 25);
		List<JugadorEquipo> lista = new ArrayList<JugadorEquipo>();
		for (int i = 1; i <= cantidad; i++) {
			JugadorEquipo j = admin.crearJugadorEquipo(prefijo + i, "pw", "J" + prefijo + i, 20);
			j.inscribirseEquipo(e, "Delantero", i);
			lista.add(j);
		}
		return lista;
	}

	private Entrenador[] personalBasico() {
		Entrenador e1 = admin.crearEntrenador("e1", "pw", "E1");
		Entrenador e2 = admin.crearEntrenador("e2", "pw", "E2");
		Entrenador e3 = admin.crearEntrenador("e3", "pw", "E3");
		admin.crearFisioterapeuta("f1", "pw", "F1");
		for (Empleado e : admin.getPersonal()) {
			admin.asignarTurno(e, turno(e, FECHA, 8, 12));
		}
		return new Entrenador[] { e1, e2, e3 };
	}

	@Test
	@DisplayName("El administrador autentica con sus credenciales y guarda su nombre")
	void datosBasicos() {
		assertTrue(admin.autenticar("admin", "adm123"));
		assertEquals("Administrador", admin.getNombre());
		assertEquals("Jefe", new Administrador("a", "p", "Jefe", new Inventario()).getNombre());
	}

	@Test
	@DisplayName("Un inventario nulo o un nombre en blanco lanzan excepción")
	void constructorInvalido() {
		assertThrows(IllegalArgumentException.class, () -> new Administrador("a", "p", (Inventario) null));
		assertThrows(IllegalArgumentException.class, () -> new Administrador("a", "p", " ", new Inventario()));
	}

	@Test
	@DisplayName("Crear un equipo válido lo deja disponible")
	void crearEquipo() {
		Equipo e = admin.crearEquipo("Halcones", Categoria.MAYORES, futbol, 20);
		assertTrue(e.estaActivo());
		assertTrue(admin.getEquipos().contains(e));
	}

	@Test
	@DisplayName("Crear un equipo con datos inválidos lanza excepción")
	void crearEquipoInvalido() {
		assertThrows(IllegalArgumentException.class, () -> admin.crearEquipo(null, Categoria.MAYORES, futbol, 20));
		assertThrows(IllegalArgumentException.class, () -> admin.crearEquipo(" ", Categoria.MAYORES, futbol, 20));
		assertThrows(IllegalArgumentException.class, () -> admin.crearEquipo("X", null, futbol, 20));
		assertThrows(IllegalArgumentException.class, () -> admin.crearEquipo("X", Categoria.MAYORES, null, 20));
		assertTrue(admin.getEquipos().isEmpty());
	}

	@Test
	@DisplayName("No se pueden crear dos equipos con el mismo nombre")
	void equipoDuplicado() {
		admin.crearEquipo("Halcones", Categoria.MAYORES, futbol, 20);
		assertThrows(IllegalStateException.class, () -> admin.crearEquipo("Halcones", Categoria.SUB13, futbol, 20));
		assertEquals(1, admin.getEquipos().size());
	}

	@Test
	@DisplayName("Dar de baja un equipo sin compromisos lo marca inactivo")
	void bajaEquipo() {
		Equipo e = admin.crearEquipo("Halcones", Categoria.MAYORES, futbol, 20);
		admin.darDeBajaEquipo(e);
		assertFalse(e.estaActivo());
		assertTrue(admin.getEquipos().contains(e));
	}

	@Test
	@DisplayName("Dar de baja un equipo no registrado lanza excepción")
	void bajaEquipoNoRegistrado() {
		assertThrows(IllegalArgumentException.class, () -> admin.darDeBajaEquipo(null));
		assertThrows(IllegalArgumentException.class,
				() -> admin.darDeBajaEquipo(TestFixtures.equipo("Ajeno", futbol, Categoria.MAYORES)));
	}

	@Test
	@DisplayName("Dar de baja un equipo con partidos programados se rechaza")
	void bajaEquipoConPartido() {
		equipoConJugadores("A", 7, "a");
		equipoConJugadores("B", 7, "b");
		Equipo a = admin.getEquipos().get(0);
		Equipo b = admin.getEquipos().get(1);
		admin.registrarPartido(new PartidoInterno(FECHA, Categoria.MAYORES, futbol, a, b));
		assertThrows(IllegalStateException.class, () -> admin.darDeBajaEquipo(a));
		assertTrue(a.estaActivo());
	}

	@Test
	@DisplayName("Un equipo con un partido ya jugado sí se puede dar de baja")
	void bajaEquipoConPartidoJugado() {
		List<JugadorEquipo> ja = equipoConJugadores("A", 7, "a");
		equipoConJugadores("B", 7, "b");
		Equipo a = admin.getEquipos().get(0);
		Equipo b = admin.getEquipos().get(1);
		PartidoInterno p = new PartidoInterno(FECHA, Categoria.MAYORES, futbol, a, b);
		p.agregarParticipante(ja.get(0));
		p.registrarResultado(1, 0);
		admin.registrarPartido(p);
		assertDoesNotThrow(() -> admin.darDeBajaEquipo(a));
	}

	@Test
	@DisplayName("Crear jugadores de equipo e individuales")
	void crearJugadores() {
		JugadorEquipo je = admin.crearJugadorEquipo("juan", "pw", "Juan", 20);
		JugadorIndividual ji = admin.crearJugadorIndividual("ana", "pw", "Ana", 20, false);
		assertEquals(2, admin.getJugadores().size());
		assertTrue(admin.getJugadores().contains(je));
		assertTrue(admin.getJugadores().contains(ji));
	}

	@Test
	@DisplayName("El username debe ser único entre jugadores, empleados y el administrador")
	void usernameUnico() {
		admin.crearJugadorEquipo("juan", "pw", "Juan", 20);
		admin.crearEntrenador("coach", "pw", "Carlos");
		assertThrows(IllegalStateException.class, () -> admin.crearJugadorEquipo("juan", "pw", "Otro", 20));
		assertThrows(IllegalStateException.class, () -> admin.crearJugadorIndividual("juan", "pw", "Otro", 20, false));
		assertThrows(IllegalStateException.class, () -> admin.crearEntrenador("juan", "pw", "Otro"));
		assertThrows(IllegalStateException.class, () -> admin.crearFisioterapeuta("coach", "pw", "Otro"));
		assertThrows(IllegalStateException.class, () -> admin.crearEmpleado("admin", "pw", "Otro"));
		assertEquals(1, admin.getJugadores().size());
		assertEquals(1, admin.getPersonal().size());
	}

	@Test
	@DisplayName("Crear un usuario sin username o password lanza excepción")
	void usuarioIncompleto() {
		assertThrows(IllegalArgumentException.class, () -> admin.crearJugadorEquipo(null, "pw", "Juan", 20));
		assertThrows(IllegalArgumentException.class, () -> admin.crearJugadorEquipo("juan", " ", "Juan", 20));
	}

	@Test
	@DisplayName("Dar de baja un jugador sin compromisos conserva el historial")
	void bajaJugador() {
		JugadorEquipo j = admin.crearJugadorEquipo("juan", "pw", "Juan", 20);
		assertTrue(admin.darDeBajaJugador(j));
		assertFalse(j.estaActivo());
		assertTrue(admin.getJugadores().contains(j));
	}

	@Test
	@DisplayName("Dar de baja un jugador no registrado lanza excepción")
	void bajaJugadorNoRegistrado() {
		assertThrows(IllegalArgumentException.class, () -> admin.darDeBajaJugador(null));
		assertThrows(IllegalArgumentException.class,
				() -> admin.darDeBajaJugador(new JugadorEquipo("x", "pw", "X", 20)));
	}

	@Test
	@DisplayName("Dar de baja a un jugador con partido programado se rechaza")
	void bajaJugadorConPartido() {
		List<JugadorEquipo> ja = equipoConJugadores("A", 7, "a");
		equipoConJugadores("B", 7, "b");
		Equipo a = admin.getEquipos().get(0);
		Equipo b = admin.getEquipos().get(1);
		PartidoInterno p = new PartidoInterno(FECHA, Categoria.MAYORES, futbol, a, b);
		p.agregarParticipante(ja.get(0));
		admin.registrarPartido(p);
		assertThrows(IllegalStateException.class, () -> admin.darDeBajaJugador(ja.get(0)));
		assertTrue(ja.get(0).estaActivo());
	}

	@Test
	@DisplayName("Dar de baja dos veces al mismo jugador lanza excepción")
	void bajaJugadorDoble() {
		JugadorEquipo j = admin.crearJugadorEquipo("juan", "pw", "Juan", 20);
		admin.darDeBajaJugador(j);
		assertThrows(IllegalStateException.class, () -> admin.darDeBajaJugador(j));
	}

	@Test
	@DisplayName("Crear personal y darlo de baja")
	void personal() {
		Entrenador e = admin.crearEntrenador("coach", "pw", "Carlos");
		Fisioterapeuta f = admin.crearFisioterapeuta("fisio", "pw", "Fabi");
		Empleado t = admin.crearEmpleado("tienda", "pw", "Tomás");
		assertEquals(3, admin.getPersonal().size());
		admin.darDeBajaEmpleado(e);
		assertFalse(e.estaActivo());
		assertTrue(f.estaActivo());
		assertTrue(t.estaActivo());
		assertThrows(IllegalStateException.class, () -> admin.darDeBajaEmpleado(e));
		assertThrows(IllegalArgumentException.class, () -> admin.darDeBajaEmpleado(null));
		assertThrows(IllegalArgumentException.class,
				() -> admin.darDeBajaEmpleado(new Empleado("x", "pw", "X")));
	}

	@Test
	@DisplayName("Asignar y editar turnos de empleados del personal")
	void asignarYEditarTurnos() {
		Entrenador e = admin.crearEntrenador("coach", "pw", "Carlos");
		Turno t1 = turno(e, FECHA, 8, 12);
		assertTrue(admin.asignarTurno(e, t1));
		assertThrows(IllegalStateException.class, () -> admin.asignarTurno(e, turno(e, FECHA, 10, 14)));
		Turno t2 = turno(e, FECHA, 14, 18);
		assertTrue(admin.editarTurno(e, t1, t2));
		assertTrue(e.getTurnos().contains(t2));
		assertFalse(e.getTurnos().contains(t1));
	}

	@Test
	@DisplayName("Asignar turno a alguien ajeno al personal lanza excepción")
	void asignarTurnoAjeno() {
		Empleado ajeno = new Empleado("x", "pw", "X");
		assertThrows(IllegalArgumentException.class, () -> admin.asignarTurno(ajeno, turno(ajeno, FECHA, 8, 12)));
		assertThrows(IllegalArgumentException.class, () -> admin.asignarTurno(null, null));
		assertThrows(IllegalArgumentException.class,
				() -> admin.editarTurno(ajeno, turno(ajeno, FECHA, 8, 12), turno(ajeno, FECHA, 14, 16)));
	}

	@Test
	@DisplayName("El mínimo de personal se cumple con 2 entrenadores y 1 fisioterapeuta")
	void minimoPersonal() {
		personalBasico();
		assertTrue(admin.cumpleMinimoPersonal(FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0)));
		assertFalse(admin.cumpleMinimoPersonal(FECHA, LocalTime.of(13, 0), LocalTime.of(14, 0)));
		assertFalse(admin.cumpleMinimoPersonal(FECHA.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0)));
	}

	@Test
	@DisplayName("El mínimo de personal no cuenta a quienes están de baja")
	void minimoPersonalSinBajas() {
		Entrenador[] es = personalBasico();
		admin.darDeBajaEmpleado(es[0]);
		assertTrue(admin.cumpleMinimoPersonal(FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0)));
		admin.darDeBajaEmpleado(es[1]);
		assertFalse(admin.cumpleMinimoPersonal(FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0)));
	}

	@Test
	@DisplayName("Una franja inválida lanza excepción")
	void franjaInvalida() {
		assertThrows(IllegalArgumentException.class,
				() -> admin.cumpleMinimoPersonal(null, LocalTime.of(8, 0), LocalTime.of(9, 0)));
		assertThrows(IllegalArgumentException.class,
				() -> admin.cumpleMinimoPersonal(FECHA, LocalTime.of(9, 0), LocalTime.of(8, 0)));
	}

	@Test
	@DisplayName("Aprobar una solicitud que cumple el mínimo actualiza el turno")
	void aprobarSolicitud() {
		Entrenador[] es = personalBasico();
		Turno actual = es[0].getTurnos().get(0);
		Turno nuevo = turno(es[0], FECHA, 14, 18);
		SolicitudCambioTurno s = es[0].solicitarCambioTurno(actual, nuevo, "Cita", admin.getPersonal());
		assertEquals(1, admin.consultarSolicitudesPendientes().size());
		assertTrue(admin.aprobarSolicitud(s));
		assertEquals(EstadoSolicitud.APROBADA, s.getEstado());
		assertTrue(es[0].getTurnos().contains(nuevo));
		assertTrue(admin.consultarSolicitudesPendientes().isEmpty());
	}

	@Test
	@DisplayName("No se puede aprobar una solicitud que ya rompe el mínimo")
	void aprobarRompeMinimo() {
		Entrenador[] es = personalBasico();
		SolicitudCambioTurno s1 = es[0].solicitarCambioTurno(es[0].getTurnos().get(0), turno(es[0], FECHA, 14, 18),
				"m", admin.getPersonal());
		SolicitudCambioTurno s2 = es[1].solicitarCambioTurno(es[1].getTurnos().get(0), turno(es[1], FECHA, 14, 18),
				"m", admin.getPersonal());
		admin.aprobarSolicitud(s1);
		assertThrows(IllegalStateException.class, () -> admin.aprobarSolicitud(s2));
		assertEquals(EstadoSolicitud.PENDIENTE, s2.getEstado());
	}

	@Test
	@DisplayName("Rechazar una solicitud guarda la razón")
	void rechazarSolicitud() {
		Entrenador[] es = personalBasico();
		SolicitudCambioTurno s = es[0].solicitarCambioTurno(es[0].getTurnos().get(0), turno(es[0], FECHA, 14, 18),
				"m", admin.getPersonal());
		admin.rechazarSolicitud(s, "Falta personal");
		assertEquals(EstadoSolicitud.RECHAZADA, s.getEstado());
		assertEquals("Falta personal", s.getRazonRechazo());
		assertTrue(admin.consultarSolicitudesPendientes().isEmpty());
	}

	@Test
	@DisplayName("Rechazar sin razón lanza excepción")
	void rechazarSinRazon() {
		Entrenador[] es = personalBasico();
		SolicitudCambioTurno s = es[0].solicitarCambioTurno(es[0].getTurnos().get(0), turno(es[0], FECHA, 14, 18),
				"m", admin.getPersonal());
		assertThrows(IllegalArgumentException.class, () -> admin.rechazarSolicitud(s, " "));
		assertEquals(EstadoSolicitud.PENDIENTE, s.getEstado());
	}

	@Test
	@DisplayName("Solicitudes nulas o de empleados ajenos lanzan excepción")
	void solicitudInvalida() {
		Empleado ajeno = new Entrenador("x", "pw", "X");
		Turno t1 = turno(ajeno, FECHA, 8, 12);
		Turno t2 = turno(ajeno, FECHA, 14, 16);
		SolicitudCambioTurno s = new SolicitudCambioTurno(ajeno, t1, t2, "m");
		assertThrows(IllegalArgumentException.class, () -> admin.aprobarSolicitud(null));
		assertThrows(IllegalArgumentException.class, () -> admin.aprobarSolicitud(s));
		assertThrows(IllegalArgumentException.class, () -> admin.rechazarSolicitud(s, "razón"));
	}

	@Test
	@DisplayName("Crear competencias de liga y por fases")
	void crearCompetencias() {
		Competencia liga = admin.crearCompetenciaLiga("Cerros", "Cerros", 2026, futbol, Categoria.MAYORES);
		Competencia fases = admin.crearCompetenciaFases("ASCUN", "ASCUN", 2026, futbol, Categoria.MAYORES);
		assertEquals(2, admin.getCompetencias().size());
		assertTrue(admin.getCompetencias().contains(liga));
		assertTrue(admin.getCompetencias().contains(fases));
	}

	@Test
	@DisplayName("No se repite una competencia con el mismo nombre en la misma temporada")
	void competenciaDuplicada() {
		admin.crearCompetenciaLiga("Cerros", "Cerros", 2026, futbol, Categoria.MAYORES);
		assertThrows(IllegalStateException.class,
				() -> admin.crearCompetenciaFases("Cerros", "Otra", 2026, futbol, Categoria.MAYORES));
		assertDoesNotThrow(() -> admin.crearCompetenciaLiga("Cerros", "Cerros", 2027, futbol, Categoria.MAYORES));
	}

	@Test
	@DisplayName("Inscribir un equipo registrado con su nómina en una competencia registrada")
	void inscribirEnCompetencia() {
		List<JugadorEquipo> js = equipoConJugadores("Halcones", 7, "h");
		Equipo e = admin.getEquipos().get(0);
		Competencia c = admin.crearCompetenciaLiga("Cerros", "Cerros", 2026, futbol, Categoria.MAYORES);
		admin.inscribirEquipoEnCompetencia(c, e, js);
		assertTrue(c.estaInscrito(e));
	}

	@Test
	@DisplayName("Inscribir con competencia o equipo no registrados lanza excepción")
	void inscribirNoRegistrado() {
		List<JugadorEquipo> js = equipoConJugadores("Halcones", 7, "h");
		Equipo e = admin.getEquipos().get(0);
		Competencia c = admin.crearCompetenciaLiga("Cerros", "Cerros", 2026, futbol, Categoria.MAYORES);
		Equipo ajeno = TestFixtures.equipo("Ajeno", futbol, Categoria.MAYORES);
		assertThrows(IllegalArgumentException.class, () -> admin.inscribirEquipoEnCompetencia(null, e, js));
		assertThrows(IllegalArgumentException.class, () -> admin.inscribirEquipoEnCompetencia(c, null, js));
		assertThrows(IllegalArgumentException.class, () -> admin.inscribirEquipoEnCompetencia(c, ajeno, js));
	}

	@Test
	@DisplayName("Registrar un partido válido lo agrega al historial")
	void registrarPartido() {
		equipoConJugadores("A", 7, "a");
		equipoConJugadores("B", 7, "b");
		PartidoInterno p = new PartidoInterno(FECHA, Categoria.MAYORES, futbol, admin.getEquipos().get(0),
				admin.getEquipos().get(1));
		admin.registrarPartido(p);
		assertTrue(admin.consultarHistorialPartidos().contains(p));
	}

	@Test
	@DisplayName("Registrar un partido nulo o inválido falla")
	void registrarPartidoInvalido() {
		equipoConJugadores("A", 7, "a");
		equipoConJugadores("B", 6, "b");
		Equipo a = admin.getEquipos().get(0);
		Equipo b = admin.getEquipos().get(1);
		PartidoInterno corto = new PartidoInterno(FECHA, Categoria.MAYORES, futbol, a, b);
		assertThrows(IllegalArgumentException.class, () -> admin.registrarPartido(null));
		assertThrows(IllegalStateException.class, () -> admin.registrarPartido(corto));
		assertTrue(admin.consultarHistorialPartidos().isEmpty());
	}

	@Test
	@DisplayName("Registrar el mismo partido dos veces lanza excepción")
	void registrarPartidoDoble() {
		equipoConJugadores("A", 7, "a");
		equipoConJugadores("B", 7, "b");
		PartidoInterno p = new PartidoInterno(FECHA, Categoria.MAYORES, futbol, admin.getEquipos().get(0),
				admin.getEquipos().get(1));
		admin.registrarPartido(p);
		assertThrows(IllegalStateException.class, () -> admin.registrarPartido(p));
	}

	@Test
	@DisplayName("El historial de partidos se puede filtrar por rango de fechas")
	void historialPorRango() {
		equipoConJugadores("A", 7, "a");
		equipoConJugadores("B", 7, "b");
		Equipo a = admin.getEquipos().get(0);
		Equipo b = admin.getEquipos().get(1);
		PartidoInterno p1 = new PartidoInterno(FECHA, Categoria.MAYORES, futbol, a, b);
		PartidoInterno p2 = new PartidoInterno(FECHA.plusDays(10), Categoria.MAYORES, futbol, a, b);
		admin.registrarPartido(p2);
		admin.registrarPartido(p1);
		List<Partido> rango = admin.consultarHistorialPartidos(FECHA, FECHA.plusDays(5));
		assertEquals(List.of(p1), rango);
		assertEquals(2, admin.consultarHistorialPartidos(FECHA, FECHA.plusDays(10)).size());
		assertTrue(admin.consultarHistorialPartidos(FECHA.plusDays(20), FECHA.plusDays(30)).isEmpty());
	}

	@Test
	@DisplayName("Los historiales vacíos devuelven listas vacías")
	void historialesVacios() {
		assertTrue(admin.consultarHistorialReservas().isEmpty());
		assertTrue(admin.consultarHistorialPartidos().isEmpty());
		assertTrue(admin.consultarHistorialEntrenamientos().isEmpty());
		assertTrue(admin.consultarHistorialReservas(FECHA, FECHA).isEmpty());
		assertTrue(admin.consultarHistorialEntrenamientos(FECHA, FECHA).isEmpty());
	}

	@Test
	@DisplayName("Un periodo inválido lanza excepción en los historiales y reportes")
	void periodoInvalido() {
		assertThrows(IllegalArgumentException.class, () -> admin.consultarHistorialPartidos(FECHA.plusDays(1), FECHA));
		assertThrows(IllegalArgumentException.class, () -> admin.consultarHistorialPartidos(null, FECHA));
		assertThrows(IllegalArgumentException.class, () -> admin.consultarHistorialReservas(FECHA.plusDays(1), FECHA));
		assertThrows(IllegalArgumentException.class,
				() -> admin.consultarHistorialEntrenamientos(FECHA.plusDays(1), FECHA));
		assertThrows(IllegalArgumentException.class, () -> admin.reporteResultados(FECHA.plusDays(1), FECHA));
		assertThrows(IllegalArgumentException.class, () -> admin.reporteResultados(null, null));
	}

	@Test
	@DisplayName("El reporte de resultados solo incluye partidos jugados dentro del periodo")
	void reporteResultados() {
		List<JugadorEquipo> ja = equipoConJugadores("A", 7, "a");
		equipoConJugadores("B", 7, "b");
		Equipo a = admin.getEquipos().get(0);
		Equipo b = admin.getEquipos().get(1);
		PartidoInterno jugado = new PartidoInterno(FECHA, Categoria.MAYORES, futbol, a, b);
		jugado.agregarParticipante(ja.get(0));
		jugado.registrarResultado(2, 1);
		PartidoInterno pendiente = new PartidoInterno(FECHA.plusDays(1), Categoria.MAYORES, futbol, a, b);
		PartidoInterno fuera = new PartidoInterno(FECHA.plusDays(30), Categoria.MAYORES, futbol, a, b);
		fuera.agregarParticipante(ja.get(1));
		fuera.registrarResultado(0, 0);
		admin.registrarPartido(jugado);
		admin.registrarPartido(pendiente);
		admin.registrarPartido(fuera);
		assertEquals(List.of(jugado), admin.reporteResultados(FECHA, FECHA.plusDays(7)));
	}

	@Test
	@DisplayName("La evolución del ranking de un jugador sin partidos está vacía")
	void evolucionRankingVacia() {
		DeporteIndividual tenis = TestFixtures.tenis();
		JugadorIndividual ana = admin.crearJugadorIndividual("ana", "pw", "Ana", 20, false);
		ana.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
		assertTrue(admin.reporteEvolucionRanking(ana, tenis).isEmpty());
	}

	@Test
	@DisplayName("La evolución del ranking refleja los partidos jugados")
	void evolucionRankingConPartidos() {
		DeporteIndividual tenis = TestFixtures.tenis();
		JugadorIndividual ana = admin.crearJugadorIndividual("ana", "pw", "Ana", 20, false);
		JugadorIndividual beto = admin.crearJugadorIndividual("beto", "pw", "Beto", 20, false);
		ana.inscribirseDeporte(tenis, Nivel.INTERMEDIO);
		beto.inscribirseDeporte(tenis, Nivel.INTERMEDIO);
		ana.getRanking(tenis).actualizar(beto.getRanking(tenis), true);
		assertEquals(List.of(1016), admin.reporteEvolucionRanking(ana, tenis));
		assertEquals(List.of(984), admin.reporteEvolucionRanking(beto, tenis));
	}

	@Test
	@DisplayName("La evolución del ranking falla si el jugador no está inscrito o hay nulos")
	void evolucionRankingInvalida() {
		DeporteIndividual tenis = TestFixtures.tenis();
		JugadorIndividual ana = admin.crearJugadorIndividual("ana", "pw", "Ana", 20, false);
		assertThrows(IllegalStateException.class, () -> admin.reporteEvolucionRanking(ana, tenis));
		assertThrows(IllegalArgumentException.class, () -> admin.reporteEvolucionRanking(null, tenis));
		assertThrows(IllegalArgumentException.class, () -> admin.reporteEvolucionRanking(ana, null));
	}

	@Test
	@DisplayName("Los reportes de ventas con argumentos nulos lanzan excepción")
	void reportesVentasNulos() {
		assertThrows(IllegalArgumentException.class, () -> admin.reporteVentasDiario(null));
		assertThrows(IllegalArgumentException.class, () -> admin.reporteVentasSemanal(null));
		assertThrows(IllegalArgumentException.class, () -> admin.reporteVentasMensual(null));
	}

	@Test
	@DisplayName("Los reportes de ventas sin ventas devuelven todos los rubros en cero")
	void reportesVentasVacios() {
		for (double total : admin.reporteVentasDiario(FECHA).values()) {
			assertEquals(0.0, total);
		}
		for (double total : admin.reporteVentasMensual(YearMonth.of(2026, 11)).values()) {
			assertEquals(0.0, total);
		}
		assertFalse(admin.reporteVentasSemanal(FECHA).isEmpty());
	}

	@Test
	@DisplayName("Registrar reservas, ventas e instalaciones nulas lanza excepción")
	void registrosNulos() {
		assertThrows(IllegalArgumentException.class, () -> admin.registrarReserva(null));
		assertThrows(IllegalArgumentException.class, () -> admin.registrarVenta(null));
		assertThrows(IllegalArgumentException.class, () -> admin.registrarInstalacion(null));
	}

	@Test
	@DisplayName("Mover o reabastecer con datos inválidos lanza excepción")
	void inventarioInvalido() {
		assertThrows(IllegalArgumentException.class, () -> admin.moverArticulo(null, "Bodega", "Tienda", 1));
		assertThrows(IllegalArgumentException.class, () -> admin.reabastecerTienda(null, 1));
		assertNotNull(admin.getInventario());
	}

	@Test
	@DisplayName("Las listas del administrador son copias")
	void listasCopia() {
		admin.crearEquipo("A", Categoria.MAYORES, futbol, 20);
		admin.crearJugadorEquipo("j", "pw", "J", 20);
		admin.crearEntrenador("c", "pw", "C");
		admin.crearCompetenciaLiga("L", "O", 2026, futbol, Categoria.MAYORES);
		admin.getEquipos().clear();
		admin.getJugadores().clear();
		admin.getPersonal().clear();
		admin.getCompetencias().clear();
		admin.getInstalaciones().clear();
		assertEquals(1, admin.getEquipos().size());
		assertEquals(1, admin.getJugadores().size());
		assertEquals(1, admin.getPersonal().size());
		assertEquals(1, admin.getCompetencias().size());
	}
}