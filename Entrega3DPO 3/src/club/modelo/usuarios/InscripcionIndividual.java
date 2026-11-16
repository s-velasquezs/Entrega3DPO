package club.modelo.usuarios;

import java.time.LocalDate;

import club.modelo.deportes.DeporteIndividual;
import club.modelo.enums.Nivel;

public class InscripcionIndividual {

	private JugadorIndividual jugador;
	private DeporteIndividual deporte;
	private Nivel nivel;
	private LocalDate fechaInscripcion;
	private RankingELO rankingELO;
	private boolean activa;

	public InscripcionIndividual(JugadorIndividual jugador, DeporteIndividual deporte, Nivel nivel) {
		this(jugador, deporte, nivel, LocalDate.now());
	}

	public InscripcionIndividual(JugadorIndividual jugador, DeporteIndividual deporte, Nivel nivel,
			LocalDate fechaInscripcion) {
		if (jugador == null || deporte == null || nivel == null) {
			throw new IllegalArgumentException("La inscripción necesita jugador, deporte y nivel.");
		}
		if (fechaInscripcion == null) {
			throw new IllegalArgumentException("La fecha de inscripción es obligatoria.");
		}
		this.jugador = jugador;
		this.deporte = deporte;
		this.nivel = nivel;
		this.fechaInscripcion = fechaInscripcion;
		this.activa = true;
		this.rankingELO = new RankingELO();
	}

	public void cambiarNivel(Nivel nuevoNivel) {
		if (nuevoNivel == null) {
			throw new IllegalArgumentException("El nivel es obligatorio.");
		}
		if (!activa) {
			throw new IllegalStateException("La inscripción no está activa.");
		}
		this.nivel = nuevoNivel;
	}

	public void darDeBaja() {
		this.activa = false;
	}

	public boolean estaActiva() {
		return activa;
	}

	public JugadorIndividual getJugador() {
		return jugador;
	}

	public DeporteIndividual getDeporte() {
		return deporte;
	}

	public Nivel getNivel() {
		return nivel;
	}

	public LocalDate getFechaInscripcion() {
		return fechaInscripcion;
	}

	public RankingELO getRankingELO() {
		return rankingELO;
	}
}