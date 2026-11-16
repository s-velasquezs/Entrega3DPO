package club.modelo.usuarios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import club.modelo.comercio.ProgramaFidelidad;
import club.modelo.deportes.Registro;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.estadisticas.EstadisticasJugador;
import club.modelo.partidos.Partido;
import club.modelo.salud.HistorialMedico;

public abstract class Jugador extends Usuario {

	private String nombre;
	private int edad;
	private Registro registro;
	private HistorialMedico historialMedico;
	private EstadisticasJugador estadisticas;
	private ProgramaFidelidad programaFidelidad;
	private List<Partido> partidos;
	private List<Entrenamiento> entrenamientos;

	public Jugador(String username, String password, String nombre, int edad) {
		super(username, password);
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del jugador es obligatorio.");
		}
		if (edad <= 0) {
			throw new IllegalArgumentException("La edad debe ser positiva.");
		}
		this.nombre = nombre;
		this.edad = edad;
		this.registro = new Registro();
		this.historialMedico = new HistorialMedico();
		this.estadisticas = new EstadisticasJugador();
		this.programaFidelidad = new ProgramaFidelidad(this);
		this.partidos = new ArrayList<Partido>();
		this.entrenamientos = new ArrayList<Entrenamiento>();
	}

	public String getNombre() {
		return nombre;
	}

	public int getEdad() {
		return edad;
	}

	public boolean estaActivo() {
		return registro.estaRegistrado();
	}

	public Registro getRegistro() {
		return registro;
	}

	public HistorialMedico getHistorialMedico() {
		return historialMedico;
	}

	public EstadisticasJugador consultarEstadisticas() {
		return estadisticas;
	}

	public ProgramaFidelidad getProgramaFidelidad() {
		return programaFidelidad;
	}

	public boolean estaSuspendido() {
		return estadisticas.tieneSancionActiva();
	}

	public boolean estaHabilitado() {
		return estaActivo() && !estaSuspendido() && !historialMedico.tieneLesionActiva();
	}

	public List<String> consultarSanciones() {
		return estadisticas.getSanciones();
	}

	public void agregarPartido(Partido partido) {
		if (partido == null) {
			throw new IllegalArgumentException("El partido es obligatorio.");
		}
		if (!partidos.contains(partido)) {
			partidos.add(partido);
		}
	}

	public void agregarEntrenamiento(Entrenamiento entrenamiento) {
		if (entrenamiento == null) {
			throw new IllegalArgumentException("El entrenamiento es obligatorio.");
		}
		if (!entrenamientos.contains(entrenamiento)) {
			entrenamientos.add(entrenamiento);
		}
	}

	public List<Partido> consultarHistorialPartidos() {
		List<Partido> lista = new ArrayList<Partido>();
		for (Partido p : partidos) {
			if (p.estaJugado()) {
				lista.add(p);
			}
		}
		lista.sort(Comparator.comparing(Partido::getFecha));
		return lista;
	}

	public List<Partido> consultarProximosPartidos() {
		List<Partido> lista = new ArrayList<Partido>();
		LocalDate hoy = LocalDate.now();
		for (Partido p : partidos) {
			if (!p.estaJugado() && !p.getFecha().isBefore(hoy)) {
				lista.add(p);
			}
		}
		lista.sort(Comparator.comparing(Partido::getFecha));
		return lista;
	}

	public List<Entrenamiento> consultarHorario(LocalDate desde, LocalDate hasta) {
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

	public List<Entrenamiento> consultarProximosEntrenamientos() {
		List<Entrenamiento> lista = new ArrayList<Entrenamiento>();
		LocalDate hoy = LocalDate.now();
		for (Entrenamiento e : entrenamientos) {
			if (!e.getFecha().isBefore(hoy)) {
				lista.add(e);
			}
		}
		lista.sort(Comparator.comparing(Entrenamiento::getFecha).thenComparing(Entrenamiento::getHora));
		return lista;
	}

	public List<Partido> getPartidos() {
		return new ArrayList<Partido>(partidos);
	}

	public List<Entrenamiento> getEntrenamientos() {
		return new ArrayList<Entrenamiento>(entrenamientos);
	}

	public boolean darDeBaja() {
		if (!estaActivo()) {
			throw new IllegalStateException("El jugador ya está dado de baja.");
		}
		if (estaSuspendido()) {
			throw new IllegalStateException("El jugador tiene sanciones activas y no puede darse de baja.");
		}
		registro.darDeBaja();
		return true;
	}

	@Override
	public String toString() {
		return nombre + " (" + getUsername() + ")";
	}
}