package club.consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import club.modelo.competencias.Competencia;
import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoAsistencia;
import club.modelo.instalaciones.ReservaCancha;
import club.modelo.partidos.Partido;
import club.modelo.partidos.PartidoExterno;
import club.modelo.partidos.PartidoInterno;
import club.modelo.personal.Empleado;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.personal.Turno;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;


public class MenuAdministrador extends MenuBase {

	public MenuAdministrador(Entrada in, Administrador admin, List<Deporte> deportes) {
		super(in, admin, deportes);
	}

	@Override
	public void iniciar() {
		while (true) {
			switch (menu("Administrador: " + admin.getNombre(), "Equipos", "Jugadores", "Personal y turnos",
					"Solicitudes de cambio de turno", "Competencias", "Partidos", "Historiales y reportes",
					"Tienda, cafetería e inventario")) {
			case 1:
				menuEquipos();
				break;
			case 2:
				menuJugadores();
				break;
			case 3:
				menuPersonal();
				break;
			case 4:
				resolverSolicitudes();
				break;
			case 5:
				menuCompetencias();
				break;
			case 6:
				menuPartidos();
				break;
			case 7:
				menuReportes();
				break;
			case 8:
				comercio.menuAdministrador(this);
				break;
			default:
				return;
			}
		}
	}


	private void menuEquipos() {
		while (true) {
			switch (menu("Equipos", "Crear equipo", "Listar equipos", "Dar de baja un equipo")) {
			case 1:
				intentar(() -> {
					String nombre = in.textoObligatorio("Nombre del equipo");
					DeporteEquipo deporte = in.elegir("Deporte", deportesEquipo());
					if (deporte == null) {
						return;
					}
					Categoria categoria = in.elegirEnum("Categoría", Categoria.values());
					if (categoria == null) {
						return;
					}
					int cupo = in.entero("Cupo máximo de jugadores");
					Equipo e = admin.crearEquipo(nombre, categoria, deporte, cupo);
					System.out.println("[OK] Equipo creado y disponible para asignar jugadores: " + Vista.equipo(e));
				});
				break;
			case 2:
				listar(admin.getEquipos(), "No hay equipos creados.");
				break;
			case 3:
				intentar(() -> {
					Equipo e = in.elegir("Equipo a dar de baja", equiposActivos(), Vista::equipo);
					if (e != null) {
						admin.darDeBajaEquipo(e);
						System.out.println("[OK] Equipo dado de baja (se conserva el historial).");
					}
				});
				break;
			default:
				return;
			}
		}
	}

	private <T> void listar(List<T> lista, String vacio) {
		if (lista.isEmpty()) {
			System.out.println(vacio);
		}
		for (T t : lista) {
			if (t instanceof Equipo) {
				System.out.println(" - " + Vista.equipo((Equipo) t));
			} else if (t instanceof Jugador) {
				System.out.println(" - " + Vista.jugador((Jugador) t));
			} else if (t instanceof Empleado) {
				System.out.println(" - " + Vista.empleado((Empleado) t));
			} else if (t instanceof Partido) {
				System.out.println(" - " + Vista.partido((Partido) t));
			} else {
				System.out.println(" - " + t);
			}
		}
	}


	private void menuJugadores() {
		while (true) {
			switch (menu("Jugadores", "Crear jugador de equipo", "Crear jugador de academia individual",
					"Listar jugadores", "Dar de baja un jugador")) {
			case 1:
				intentar(() -> {
					String user = in.textoObligatorio("Username");
					String pass = in.textoObligatorio("Password");
					String nombre = in.textoObligatorio("Nombre");
					int edad = in.entero("Edad");
					admin.crearJugadorEquipo(user, pass, nombre, edad);
					System.out.println("[OK] Jugador de equipo creado.");
				});
				break;
			case 2:
				intentar(() -> {
					String user = in.textoObligatorio("Username");
					String pass = in.textoObligatorio("Password");
					String nombre = in.textoObligatorio("Nombre");
					int edad = in.entero("Edad");
					boolean entrenador = in.confirmar("¿Es también entrenador?");
					admin.crearJugadorIndividual(user, pass, nombre, edad, entrenador);
					System.out.println("[OK] Jugador de academia creado.");
				});
				break;
			case 3:
				listar(admin.getJugadores(), "No hay jugadores registrados.");
				break;
			case 4:
				intentar(() -> {
					List<Jugador> activos = new ArrayList<Jugador>();
					for (Jugador j : admin.getJugadores()) {
						if (j.estaActivo()) {
							activos.add(j);
						}
					}
					Jugador j = in.elegir("Jugador a dar de baja", activos, Vista::jugador);
					if (j != null) {
						admin.darDeBajaJugador(j);
						System.out.println("[OK] Jugador dado de baja (se conserva el historial).");
					}
				});
				break;
			default:
				return;
			}
		}
	}


	private List<Empleado> empleadosActivos() {
		List<Empleado> lista = new ArrayList<Empleado>();
		for (Empleado e : admin.getPersonal()) {
			if (e.estaActivo()) {
				lista.add(e);
			}
		}
		return lista;
	}

	private void menuPersonal() {
		while (true) {
			switch (menu("Personal y turnos", "Crear entrenador", "Crear fisioterapeuta",
					"Crear empleado de tienda y cafetería", "Listar personal", "Dar de baja un empleado",
					"Asignar turno", "Editar turno", "Ver turnos de un empleado",
					"Verificar mínimo de personal en una franja")) {
			case 1:
				intentar(() -> {
					admin.crearEntrenador(in.textoObligatorio("Username"), in.textoObligatorio("Password"),
							in.textoObligatorio("Nombre"));
					System.out.println("[OK] Entrenador creado.");
				});
				break;
			case 2:
				intentar(() -> {
					admin.crearFisioterapeuta(in.textoObligatorio("Username"), in.textoObligatorio("Password"),
							in.textoObligatorio("Nombre"));
					System.out.println("[OK] Fisioterapeuta creado.");
				});
				break;
			case 3:
				intentar(() -> {
					admin.crearEmpleado(in.textoObligatorio("Username"), in.textoObligatorio("Password"),
							in.textoObligatorio("Nombre"));
					System.out.println("[OK] Empleado creado.");
				});
				break;
			case 4:
				listar(admin.getPersonal(), "No hay personal registrado.");
				break;
			case 5:
				intentar(() -> {
					Empleado e = in.elegir("Empleado a dar de baja", empleadosActivos(), Vista::empleado);
					if (e != null) {
						admin.darDeBajaEmpleado(e);
						System.out.println("[OK] Empleado dado de baja.");
					}
				});
				break;
			case 6:
				intentar(() -> {
					Empleado e = in.elegir("Empleado", empleadosActivos(), Vista::empleado);
					if (e == null) {
						return;
					}
					Turno t = new Turno(e, in.fecha("Fecha"), in.hora("Hora de inicio"), in.hora("Hora de fin"));
					admin.asignarTurno(e, t);
					System.out.println("[OK] Turno asignado: " + t);
				});
				break;
			case 7:
				intentar(() -> {
					Empleado e = in.elegir("Empleado", empleadosActivos(), Vista::empleado);
					if (e == null) {
						return;
					}
					Turno actual = in.elegir("Turno a editar", e.getTurnos());
					if (actual == null) {
						return;
					}
					Turno nuevo = new Turno(e, in.fecha("Nueva fecha"), in.hora("Nueva hora de inicio"),
							in.hora("Nueva hora de fin"));
					admin.editarTurno(e, actual, nuevo);
					System.out.println("[OK] Turno actualizado: " + nuevo);
				});
				break;
			case 8:
				intentar(() -> {
					Empleado e = in.elegir("Empleado", admin.getPersonal(), Vista::empleado);
					if (e == null) {
						return;
					}
					List<Turno> turnos = e.consultarTurnos(in.fecha("Desde"), in.fecha("Hasta"));
					if (turnos.isEmpty()) {
						System.out.println("No hay turnos en el periodo.");
					}
					for (Turno t : turnos) {
						System.out.println(" - " + t);
					}
				});
				break;
			case 9:
				intentar(() -> {
					boolean ok = admin.cumpleMinimoPersonal(in.fecha("Fecha"), in.hora("Inicio de la franja"),
							in.hora("Fin de la franja"));
					System.out.println(ok ? "Cumple el mínimo (2 entrenadores y 1 fisioterapeuta)."
							: "NO cumple el mínimo de 2 entrenadores y 1 fisioterapeuta.");
				});
				break;
			default:
				return;
			}
		}
	}


	private void resolverSolicitudes() {
		intentar(() -> {
			List<SolicitudCambioTurno> pendientes = admin.consultarSolicitudesPendientes();
			SolicitudCambioTurno s = in.elegir("Solicitudes pendientes", pendientes, Vista::solicitud);
			if (s == null) {
				return;
			}
			boolean cumple = s.cumpleMinimoPersonal(admin.getPersonal());
			if (!cumple) {
				System.out.println("Esta solicitud deja la franja sin el mínimo de personal: no se puede aprobar.");
				if (in.confirmar("¿Rechazarla?")) {
					admin.rechazarSolicitud(s, in.textoObligatorio("Razón del rechazo"));
					System.out.println("[OK] Solicitud rechazada.");
				}
				return;
			}
			int op = menu("Solicitud de " + s.getEmpleado().getNombre(), "Aprobar", "Rechazar");
			if (op == 1) {
				admin.aprobarSolicitud(s);
				System.out.println("[OK] Solicitud aprobada: el turno fue actualizado.");
			} else if (op == 2) {
				admin.rechazarSolicitud(s, in.textoObligatorio("Razón del rechazo"));
				System.out.println("[OK] Solicitud rechazada.");
			}
		});
	}


	private List<JugadorEquipo> plantilla(Equipo equipo) {
		List<JugadorEquipo> lista = new ArrayList<JugadorEquipo>();
		for (Jugador j : admin.getJugadores()) {
			if (j instanceof JugadorEquipo && equipo.tieneJugador((JugadorEquipo) j)) {
				lista.add((JugadorEquipo) j);
			}
		}
		return lista;
	}

	private void menuCompetencias() {
		while (true) {
			switch (menu("Competencias", "Crear competencia", "Listar competencias",
					"Inscribir equipo con su nómina", "Ver tabla de posiciones",
					"Clasificación por estadística (ej. goleador)")) {
			case 1:
				intentar(() -> {
					int formato = menu("Formato", "Liga (todos contra todos, ej. Torneo de los Cerros)",
							"Por fases (ej. ASCUN)");
					if (formato == 0) {
						return;
					}
					String nombre = in.textoObligatorio("Nombre");
					String entidad = in.textoObligatorio("Entidad organizadora");
					int temporada = in.entero("Temporada (año)");
					DeporteEquipo deporte = in.elegir("Deporte", deportesEquipo());
					if (deporte == null) {
						return;
					}
					Categoria categoria = in.elegirEnum("Categoría habilitada", Categoria.values());
					if (categoria == null) {
						return;
					}
					if (formato == 1) {
						admin.crearCompetenciaLiga(nombre, entidad, temporada, deporte, categoria);
					} else {
						admin.crearCompetenciaFases(nombre, entidad, temporada, deporte, categoria);
					}
					System.out.println("[OK] Competencia creada.");
				});
				break;
			case 2:
				if (admin.getCompetencias().isEmpty()) {
					System.out.println("No hay competencias creadas.");
				}
				for (Competencia c : admin.getCompetencias()) {
					System.out.println(" - " + Vista.competencia(c) + " | equipos: " + c.getEquiposInscritos().size()
							+ " | partidos: " + c.getPartidos().size());
				}
				break;
			case 3:
				intentar(() -> {
					Competencia c = in.elegir("Competencia", admin.getCompetencias(), Vista::competencia);
					if (c == null) {
						return;
					}
					Equipo e = in.elegir("Equipo a inscribir", equiposActivos(), Vista::equipo);
					if (e == null) {
						return;
					}
					List<JugadorEquipo> nomina = in.elegirVarios("Nómina (jugadores del equipo)", plantilla(e));
					admin.inscribirEquipoEnCompetencia(c, e, nomina);
					System.out.println("[OK] Equipo inscrito con " + nomina.size() + " jugador(es) en la nómina.");
				});
				break;
			case 4:
				intentar(() -> {
					Competencia c = in.elegir("Competencia", admin.getCompetencias(), Vista::competencia);
					if (c == null) {
						return;
					}
					Map<String, Integer> tabla = c.calcularTabla();
					if (tabla.isEmpty()) {
						System.out.println("Aún no hay equipos inscritos.");
					}
					int pos = 1;
					for (Map.Entry<String, Integer> fila : tabla.entrySet()) {
						System.out.println(String.format("  %2d. %-30s %d pts", pos++, fila.getKey(), fila.getValue()));
					}
				});
				break;
			case 5:
				intentar(() -> {
					Competencia c = in.elegir("Competencia", admin.getCompetencias(), Vista::competencia);
					if (c == null) {
						return;
					}
					String estadistica = in.textoObligatorio("Estadística (ej. goles, asistencias, puntos)");
					Map<JugadorEquipo, Integer> clasif = c.clasificacionPor(estadistica);
					if (clasif.isEmpty()) {
						System.out.println("No hay jugadores en las nóminas.");
					}
					int pos = 1;
					for (Map.Entry<JugadorEquipo, Integer> fila : clasif.entrySet()) {
						System.out.println(String.format("  %2d. %-30s %d", pos++, fila.getKey(), fila.getValue()));
					}
				});
				break;
			default:
				return;
			}
		}
	}

	private void menuPartidos() {
		while (true) {
			switch (menu("Partidos", "Crear partido interno", "Crear partido externo (amistoso o de competencia)",
					"Registrar resultado", "Listar todos los partidos")) {
			case 1:
				intentar(() -> {
					Equipo a = in.elegir("Equipo A", equiposActivos(), Vista::equipo);
					if (a == null) {
						return;
					}
					Equipo b = in.elegir("Equipo B", equiposActivos(), Vista::equipo);
					if (b == null) {
						return;
					}
					LocalDate fecha = in.fecha("Fecha del partido");
					PartidoInterno p = new PartidoInterno(fecha, a.getCategoria(), a.getDeporte(), a, b);
					admin.registrarPartido(p);
					System.out.println("[OK] Partido programado.");
				});
				break;
			case 2:
				intentar(() -> {
					Equipo club = in.elegir("Equipo del club", equiposActivos(), Vista::equipo);
					if (club == null) {
						return;
					}
					String rival = in.texto("Institución rival");
					String escenario = in.texto("Escenario");
					LocalDate fecha = in.fecha("Fecha del partido");
					List<Competencia> posibles = new ArrayList<Competencia>();
					for (Competencia c : admin.getCompetencias()) {
						if (c.estaInscrito(club)) {
							posibles.add(c);
						}
					}
					Competencia competencia = null;
					if (!posibles.isEmpty() && in.confirmar("¿Pertenece a una competencia?")) {
						competencia = in.elegir("Competencia", posibles, Vista::competencia);
					}
					PartidoExterno p = new PartidoExterno(fecha, club, rival, escenario, competencia);
					admin.registrarPartido(p);
					System.out.println("[OK] Partido externo programado.");
				});
				break;
			case 3:
				intentar(() -> {
					List<Partido> pendientes = new ArrayList<Partido>();
					for (Partido p : admin.getPartidos()) {
						if (!p.estaJugado()) {
							pendientes.add(p);
						}
					}
					Partido p = in.elegir("Partido por jugar", pendientes, Vista::partido);
					if (p == null) {
						return;
					}
					int a = in.entero("Marcador del primer participante");
					int b = in.entero("Marcador del segundo participante");
					p.registrarResultado(a, b);
					System.out.println("[OK] Resultado registrado.");
				});
				break;
			case 4:
				listar(admin.getPartidos(), "No hay partidos registrados.");
				break;
			default:
				return;
			}
		}
	}


	private void menuReportes() {
		while (true) {
			switch (menu("Historiales y reportes (por rango de fechas)", "Historial de reservas",
					"Historial de partidos", "Historial de sesiones de entrenamiento", "Reporte de asistencia",
					"Reporte de resultados", "Evolución del ranking ELO de un jugador")) {
			case 1:
				intentar(() -> {
					List<ReservaCancha> l = admin.consultarHistorialReservas(in.fecha("Desde"), in.fecha("Hasta"));
					if (l.isEmpty()) {
						System.out.println("No hay reservas en el periodo.");
					}
					for (ReservaCancha r : l) {
						System.out.println(" - " + Vista.reserva(r));
					}
				});
				break;
			case 2:
				intentar(() -> listar(admin.consultarHistorialPartidos(in.fecha("Desde"), in.fecha("Hasta")),
						"No hay partidos en el periodo."));
				break;
			case 3:
				intentar(() -> {
					List<Entrenamiento> l = admin.consultarHistorialEntrenamientos(in.fecha("Desde"),
							in.fecha("Hasta"));
					if (l.isEmpty()) {
						System.out.println("No hay sesiones en el periodo.");
					}
					for (Entrenamiento e : l) {
						System.out.println(" - " + Vista.entrenamiento(e));
					}
				});
				break;
			case 4:
				intentar(() -> {
					Jugador j = in.elegir("Jugador", admin.getJugadores(), Vista::jugador);
					if (j == null) {
						return;
					}
					Map<EstadoAsistencia, Integer> r = admin.reporteAsistencia(j, in.fecha("Desde"),
							in.fecha("Hasta"));
					for (Map.Entry<EstadoAsistencia, Integer> e : r.entrySet()) {
						System.out.println("  " + e.getKey() + ": " + e.getValue());
					}
				});
				break;
			case 5:
				intentar(() -> listar(admin.reporteResultados(in.fecha("Desde"), in.fecha("Hasta")),
						"No hay resultados en el periodo."));
				break;
			case 6:
				intentar(() -> {
					List<JugadorIndividual> lista = new ArrayList<JugadorIndividual>();
					for (Jugador j : admin.getJugadores()) {
						if (j instanceof JugadorIndividual) {
							lista.add((JugadorIndividual) j);
						}
					}
					JugadorIndividual j = in.elegir("Jugador", lista, Jugador::toString);
					if (j == null) {
						return;
					}
					DeporteIndividual d = in.elegir("Deporte", deportesIndividuales());
					if (d == null) {
						return;
					}
					List<Integer> historial = admin.reporteEvolucionRanking(j, d);
					System.out.println(historial.isEmpty() ? "Sin ranking: aún no ha jugado."
							: "Evolución del ELO: " + historial);
				});
				break;
			default:
				return;
			}
		}
	}
}
