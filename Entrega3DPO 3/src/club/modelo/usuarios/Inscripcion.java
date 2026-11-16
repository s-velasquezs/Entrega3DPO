package club.modelo.usuarios;

import java.time.LocalDate;

import club.modelo.deportes.Equipo;

public class Inscripcion {

	private JugadorEquipo jugador;
	private Equipo equipo;
	private String posicion;
	private int numeroCamiseta;
	private LocalDate fechaInscripcion;
	private boolean activa;

	public Inscripcion(JugadorEquipo jugador, Equipo equipo, String posicion, int numeroCamiseta) {
		this(jugador, equipo, posicion, numeroCamiseta, LocalDate.now());
	}

	public Inscripcion(JugadorEquipo jugador, Equipo equipo, String posicion, int numeroCamiseta,
			LocalDate fechaInscripcion) {
		if (jugador == null || equipo == null) {
			throw new IllegalArgumentException("La inscripción necesita jugador y equipo.");
		}
		if (numeroCamiseta < 0) {
			throw new IllegalArgumentException("El número de camiseta no puede ser negativo.");
		}
		if (fechaInscripcion == null) {
			throw new IllegalArgumentException("La fecha de inscripción es obligatoria.");
		}
		this.jugador = jugador;
		this.equipo = equipo;
		this.posicion = (posicion == null || posicion.isBlank()) ? null : posicion;
		this.numeroCamiseta = numeroCamiseta;
		this.fechaInscripcion = fechaInscripcion;
		this.activa = true;
	}

	public JugadorEquipo getJugador() {
		return jugador;
	}

	public Equipo getEquipo() {
		return equipo;
	}

	public String getPosicion() {
		return posicion;
	}

	public int getNumeroCamiseta() {
		return numeroCamiseta;
	}

	public boolean tienePosicion() {
		return posicion != null;
	}

	public boolean tieneNumero() {
		return numeroCamiseta > 0;
	}

	public LocalDate getFechaInscripcion() {
		return fechaInscripcion;
	}

	public boolean estaActiva() {
		return activa;
	}

	public void asignarPosicion(String posicion) {
		if (posicion == null || posicion.isBlank()) {
			throw new IllegalArgumentException("La posición es obligatoria.");
		}
		if (!activa) {
			throw new IllegalStateException("La inscripción no está activa.");
		}
		this.posicion = posicion;
	}

	public boolean asignarNumero(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("El número de camiseta debe ser positivo.");
		}
		if (!activa) {
			throw new IllegalStateException("La inscripción no está activa.");
		}
		if (n != numeroCamiseta && !equipo.numeroDisponible(n)) {
			throw new IllegalStateException("El número de camiseta ya está tomado en el equipo.");
		}
		this.numeroCamiseta = n;
		return true;
	}

	public void darDeBaja() {
		this.activa = false;
	}
}