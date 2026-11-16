package club.modelo.personal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoAsistencia;
import club.modelo.instalaciones.Instalacion;
import club.modelo.partidos.Partido;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;

public class Entrenador extends Empleado {

	private List<Entrenamiento> entrenamientos;

	public Entrenador(String username, String password, String nombre) {
		super(username, password, nombre);
		this.entrenamientos = new ArrayList<Entrenamiento>();
	}

	public Entrenamiento planearEntrenamiento(LocalDate fecha, LocalTime hora, int duracionMinutos,
			Instalacion instalacion, List<Jugador> convocados) {
		return planear(fecha, hora, duracionMinutos, instalacion, null, convocados);
	}

	public Entrenamiento planearEntrenamiento(LocalDate fecha, LocalTime hora, int duracionMinutos,
			Instalacion instalacion, Equipo equipo, List<Jugador> convocados) {
		if (equipo == null) {
			throw new IllegalArgumentException("El equipo es obligatorio.");
		}
		return planear(fecha, hora, duracionMinutos, instalacion, equipo, convocados);
	}

	private Entrenamiento planear(LocalDate fecha, LocalTime hora, int duracionMinutos, Instalacion instalacion,
			Equipo equipo, List<Jugador> convocados) {
		if (!estaActivo()) {
			throw new IllegalStateException("El entrenador está dado de baja.");
		}
		if (fecha == null || hora == null || instalacion == null || convocados == null) {
			throw new IllegalArgumentException("Fecha, hora, instalación y convocados son obligatorios.");
		}
		if (convocados.isEmpty()) {
			throw new IllegalArgumentException("La convocatoria no puede estar vacía.");
		}
		if (duracionMinutos <= 0) {
			throw new IllegalArgumentException("La duración debe ser mayor que cero.");
		}
		LocalTime fin = hora.plusMinutes(duracionMinutos);
		if (!fin.isAfter(hora)) {
			throw new IllegalArgumentException("La sesión debe terminar el mismo día en que empieza.");
		}

		Deporte deporte = instalacion.getDeporte();
		Categoria categoria = null;
		if (equipo != null) {
			if (!equipo.estaActivo()) {
				throw new IllegalArgumentException("El equipo está inactivo.");
			}
			if (!equipo.getDeporte().equals(deporte)) {
				throw new IllegalArgumentException("La instalación no es del deporte del equipo.");
			}
			categoria = equipo.getCategoria();
		} else if (!(deporte instanceof DeporteIndividual)) {
			throw new IllegalArgumentException("Las sesiones de socios requieren un deporte individual.");
		}

		Set<Jugador> vistos = new HashSet<Jugador>();
		for (Jugador j : convocados) {
			if (j == null) {
				throw new IllegalArgumentException("La convocatoria contiene un jugador nulo.");
			}
			if (!vistos.add(j)) {
				throw new IllegalArgumentException("La convocatoria tiene jugadores repetidos.");
			}
			if (!j.estaActivo()) {
				throw new IllegalArgumentException("Un convocado no está registrado como activo.");
			}
			if (!j.estaHabilitado()) {
				throw new IllegalArgumentException("Un convocado está suspendido o lesionado.");
			}
			if (equipo != null) {
				if (!(j instanceof JugadorEquipo) || !equipo.tieneJugador((JugadorEquipo) j)) {
					throw new IllegalArgumentException("Un convocado no pertenece al equipo.");
				}
			} else {
				if (!(j instanceof JugadorIndividual)
						|| !((JugadorIndividual) j).estaInscritoEn((DeporteIndividual) deporte)) {
					throw new IllegalArgumentException("Un convocado no está inscrito en el deporte de la sesión.");
				}
				Categoria categoriaJugador = Categoria.paraEdad(j.getEdad());
				if (categoria == null) {
					categoria = categoriaJugador;
				} else if (categoria != categoriaJugador) {
					throw new IllegalArgumentException("Los convocados deben ser de la misma categoría.");
				}
			}
		}

		for (Entrenamiento e : entrenamientos) {
			if (e.getFecha().equals(fecha) && e.getHora().isBefore(fin)
					&& hora.isBefore(e.getHora().plusMinutes(e.getDuracion()))) {
				throw new IllegalStateException("El entrenador ya tiene una sesión en ese horario.");
			}
		}

		if (!instalacion.estaDisponible(fecha, hora, fin)) {
			throw new IllegalStateException("La instalación no está disponible en ese horario.");
		}

		Entrenamiento sesion = new Entrenamiento(fecha, hora, duracionMinutos, deporte, this, instalacion,
				categoria, equipo);
		for (Jugador j : convocados) {
			sesion.convocar(j);
		}
		instalacion.agregarEntrenamiento(sesion);
		entrenamientos.add(sesion);
		for (Jugador j : convocados) {
			j.agregarEntrenamiento(sesion);
		}
		return sesion;
	}

	public List<Jugador> consultarConvocables(Equipo equipo, List<? extends Jugador> candidatos) {
		if (equipo == null || candidatos == null) {
			throw new IllegalArgumentException("Equipo y candidatos son obligatorios.");
		}
		List<Jugador> lista = new ArrayList<Jugador>();
		for (Jugador j : candidatos) {
			if (j instanceof JugadorEquipo && j.estaHabilitado() && equipo.tieneJugador((JugadorEquipo) j)) {
				lista.add(j);
			}
		}
		return lista;
	}

	public void alinearJugadores(Partido partido, List<? extends Jugador> jugadores) {
		if (!estaActivo()) {
			throw new IllegalStateException("El entrenador está dado de baja.");
		}
		if (partido == null || jugadores == null) {
			throw new IllegalArgumentException("Partido y jugadores son obligatorios.");
		}
		if (partido.estaJugado()) {
			throw new IllegalStateException("El partido ya fue jugado.");
		}
		Set<Jugador> vistos = new HashSet<Jugador>();
		for (Jugador j : jugadores) {
			if (j == null) {
				throw new IllegalArgumentException("La alineación contiene un jugador nulo.");
			}
			if (!vistos.add(j) || partido.getParticipantes().contains(j)) {
				throw new IllegalArgumentException("La alineación tiene jugadores repetidos.");
			}
		}
		for (Jugador j : jugadores) {
			partido.agregarParticipante(j);
		}
	}

	@SuppressWarnings("unlikely-arg-type")
	private void validarSesionPropia(Entrenamiento entrenamiento, Jugador jugador) {
		if (entrenamiento == null || jugador == null) {
			throw new IllegalArgumentException("Entrenamiento y jugador son obligatorios.");
		}
		if (!entrenamientos.contains(entrenamiento)) {
			throw new IllegalArgumentException("La sesión no está a cargo de este entrenador.");
		}
		if (!entrenamiento.getConvocados().contains(jugador)) {
			throw new IllegalArgumentException("El jugador no fue convocado a la sesión.");
		}
	}

	public void registrarAsistencia(Entrenamiento entrenamiento, Jugador jugador, EstadoAsistencia estado) {
		validarSesionPropia(entrenamiento, jugador);
		if (estado == null) {
			throw new IllegalArgumentException("El estado de asistencia es obligatorio.");
		}
		LocalDateTime fin = LocalDateTime.of(entrenamiento.getFecha(), entrenamiento.getHora())
				.plusMinutes(entrenamiento.getDuracion());
		if (LocalDateTime.now().isBefore(fin)) {
			throw new IllegalStateException("La sesión aún no ha finalizado.");
		}
		entrenamiento.registrarAsistencia(jugador, estado);
	}

	public void evaluarJugador(Entrenamiento entrenamiento, Jugador jugador, int nota, String comentario) {
		validarSesionPropia(entrenamiento, jugador);
		if (nota < 1 || nota > 5) {
			throw new IllegalArgumentException("La nota debe estar entre 1 y 5.");
		}
		EstadoAsistencia asistencia = entrenamiento.getAsistencia(jugador);
		if (asistencia == null) {
			throw new IllegalStateException("Primero debe registrarse la asistencia del jugador.");
		}
		if (asistencia == EstadoAsistencia.FALTO) {
			throw new IllegalStateException("No se puede evaluar a un jugador que no asistió.");
		}
		entrenamiento.evaluar(jugador, nota, comentario);
	}

	public List<Entrenamiento> consultarEntrenamientos(LocalDate desde, LocalDate hasta) {
		if (desde == null || hasta == null) {
			throw new IllegalArgumentException("Las fechas del periodo son obligatorias.");
		}
		if (desde.isAfter(hasta)) {
			throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
		}
		List<Entrenamiento> lista = new ArrayList<Entrenamiento>();
		for (Entrenamiento e : entrenamientos) {
			if (!e.getFecha().isBefore(desde) && !e.getFecha().isAfter(hasta)) {
				lista.add(e);
			}
		}
		lista.sort(Comparator.comparing(Entrenamiento::getFecha).thenComparing(Entrenamiento::getHora));
		return lista;
	}

	public List<Entrenamiento> getEntrenamientos() {
		return new ArrayList<Entrenamiento>(entrenamientos);
	}
}