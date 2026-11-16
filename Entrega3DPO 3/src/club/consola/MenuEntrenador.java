package club.consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.enums.EstadoAsistencia;
import club.modelo.instalaciones.Instalacion;
import club.modelo.partidos.Partido;
import club.modelo.personal.Entrenador;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorIndividual;

public class MenuEntrenador extends MenuEmpleadoBase {

	private final Entrenador entrenador;

	public MenuEntrenador(Entrada in, Administrador admin, List<Deporte> deportes, Entrenador entrenador) {
		super(in, admin, deportes, entrenador);
		this.entrenador = entrenador;
	}

	@Override
	public void iniciar() {
		while (true) {
			switch (menu("Entrenador: " + entrenador.getNombre(), "Agendar entrenamiento de un equipo",
					"Agendar entrenamiento de socios (deporte individual)", "Ver mis entrenamientos (por fechas)",
					"Ver jugadores convocables de un equipo", "Registrar asistencia", "Evaluar jugador",
					"Alinear jugadores en un partido", "Mis turnos", "Solicitar cambio de turno",
					"Mis solicitudes")) {
			case 1:
				agendarEquipo();
				break;
			case 2:
				agendarSocios();
				break;
			case 3:
				verEntrenamientos();
				break;
			case 4:
				verConvocables();
				break;
			case 5:
				registrarAsistencia();
				break;
			case 6:
				evaluar();
				break;
			case 7:
				alinear();
				break;
			case 8:
				verTurnos();
				break;
			case 9:
				solicitarCambio();
				break;
			case 10:
				verSolicitudes();
				break;
			default:
				return;
			}
		}
	}

	private Jugador buscarJugador(String username) {
		for (Jugador j : admin.getJugadores()) {
			if (j.getUsername().equals(username)) {
				return j;
			}
		}
		throw new IllegalArgumentException("No existe un jugador con el username '" + username + "'.");
	}

	private void agendarEquipo() {
		intentar(() -> {
			Equipo equipo = in.elegir("Equipo", equiposActivos(), Vista::equipo);
			if (equipo == null) {
				return;
			}
			List<Instalacion> opciones = new ArrayList<Instalacion>();
			for (Instalacion i : admin.getInstalaciones()) {
				if (i.getDeporte().equals(equipo.getDeporte())) {
					opciones.add(i);
				}
			}
			Instalacion instalacion = in.elegir("Instalación", opciones, Instalacion::getNombre);
			if (instalacion == null) {
				return;
			}
			LocalDate fecha = in.fecha("Fecha");
			LocalTime hora = in.hora("Hora de inicio");
			int duracion = in.entero("Duración en minutos");
			List<Jugador> candidatos = entrenador.consultarConvocables(equipo, admin.getJugadores());
			List<Jugador> convocados = in.elegirVarios("Convocables (solo habilitados del equipo)", candidatos);
			String extra = in.texto("Username de otro jugador a convocar (enter para omitir)");
			if (!extra.isEmpty()) {
				Jugador j = buscarJugador(extra);
				if (!convocados.contains(j)) {
					convocados.add(j);
				}
			}
			entrenador.planearEntrenamiento(fecha, hora, duracion, instalacion, equipo, convocados);
			System.out.println("[OK] Sesión agendada y instalación bloqueada.");
		});
	}

	private void agendarSocios() {
		intentar(() -> {
			List<Instalacion> opciones = new ArrayList<Instalacion>();
			for (Instalacion i : admin.getInstalaciones()) {
				if (i.getDeporte() instanceof DeporteIndividual) {
					opciones.add(i);
				}
			}
			Instalacion instalacion = in.elegir("Instalación", opciones, Instalacion::getNombre);
			if (instalacion == null) {
				return;
			}
			DeporteIndividual deporte = (DeporteIndividual) instalacion.getDeporte();
			LocalDate fecha = in.fecha("Fecha");
			LocalTime hora = in.hora("Hora de inicio");
			int duracion = in.entero("Duración en minutos");
			List<Jugador> candidatos = new ArrayList<Jugador>();
			for (Jugador j : admin.getJugadores()) {
				if (j instanceof JugadorIndividual && j.estaHabilitado()
						&& ((JugadorIndividual) j).estaInscritoEn(deporte)) {
					candidatos.add(j);
				}
			}
			List<Jugador> convocados = in.elegirVarios("Socios inscritos y habilitados en " + deporte, candidatos);
			String extra = in.texto("Username de otro jugador a convocar (enter para omitir)");
			if (!extra.isEmpty()) {
				Jugador j = buscarJugador(extra);
				if (!convocados.contains(j)) {
					convocados.add(j);
				}
			}
			entrenador.planearEntrenamiento(fecha, hora, duracion, instalacion, convocados);
			System.out.println("[OK] Sesión agendada y instalación bloqueada.");
		});
	}

	private void verEntrenamientos() {
		intentar(() -> {
			LocalDate desde = in.fecha("Desde");
			LocalDate hasta = in.fecha("Hasta");
			List<Entrenamiento> lista = entrenador.consultarEntrenamientos(desde, hasta);
			if (lista.isEmpty()) {
				System.out.println("No hay entrenamientos en el periodo.");
			}
			for (Entrenamiento e : lista) {
				System.out.println(" - " + Vista.entrenamiento(e));
				for (Jugador j : new ArrayList<Jugador>(e.getConvocados())) {
					System.out.println("     convocado: " + j.getNombre());
				}
			}
		});
	}

	private void verConvocables() {
		intentar(() -> {
			Equipo equipo = in.elegir("Equipo", equiposActivos(), Vista::equipo);
			if (equipo == null) {
				return;
			}
			List<Jugador> lista = entrenador.consultarConvocables(equipo, admin.getJugadores());
			if (lista.isEmpty()) {
				System.out.println("No hay jugadores habilitados para convocar.");
			}
			for (Jugador j : lista) {
				System.out.println(" - " + j);
			}
		});
	}

	private void registrarAsistencia() {
		intentar(() -> {
			Entrenamiento e = in.elegir("Sesión", entrenador.getEntrenamientos(), Vista::entrenamiento);
			if (e == null) {
				return;
			}
			for (Jugador j : new ArrayList<Jugador>(e.getConvocados())) {
				EstadoAsistencia estado = in.elegirEnum("Asistencia de " + j.getNombre() + " (0 = omitir)",
						EstadoAsistencia.values());
				if (estado != null) {
					entrenador.registrarAsistencia(e, j, estado);
					System.out.println("[OK] " + j.getNombre() + ": " + estado);
				}
			}
		});
	}

	private void evaluar() {
		intentar(() -> {
			Entrenamiento e = in.elegir("Sesión", entrenador.getEntrenamientos(), Vista::entrenamiento);
			if (e == null) {
				return;
			}
			Jugador j = in.elegir("Jugador a evaluar", new ArrayList<Jugador>(e.getConvocados()));
			if (j == null) {
				return;
			}
			int nota = in.entero("Nota (1 a 5)");
			String comentario = in.texto("Comentario (opcional)");
			entrenador.evaluarJugador(e, j, nota, comentario);
			System.out.println("[OK] Evaluación guardada.");
		});
	}

	private void alinear() {
		intentar(() -> {
			List<Partido> pendientes = new ArrayList<Partido>();
			for (Partido p : admin.getPartidos()) {
				if (!p.estaJugado()) {
					pendientes.add(p);
				}
			}
			Partido partido = in.elegir("Partido por jugar", pendientes, Vista::partido);
			if (partido == null) {
				return;
			}
			List<Jugador> candidatos = new ArrayList<Jugador>();
			for (Jugador j : admin.getJugadores()) {
				if (j.estaHabilitado() && !partido.getParticipantes().contains(j)) {
					candidatos.add(j);
				}
			}
			List<Jugador> alineacion = in.elegirVarios("Jugadores habilitados", candidatos);
			entrenador.alinearJugadores(partido, alineacion);
			System.out.println("[OK] Alineación registrada (" + alineacion.size() + " jugador(es)).");
		});
	}
}