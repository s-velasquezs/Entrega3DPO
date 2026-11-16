package club.modelo.partidos;

import java.time.LocalDate;

import club.modelo.deportes.DeporteIndividual;
import club.modelo.enums.Categoria;
import club.modelo.enums.ModalidadJuego;
import club.modelo.instalaciones.ReservaCancha;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorIndividual;
import club.modelo.usuarios.RankingELO;

public class PartidoPractica extends Partido {

	private DeporteIndividual deporteIndividual;
	private JugadorIndividual jugadorA;
	private JugadorIndividual jugadorB;
	private ModalidadJuego modalidad;
	private ReservaCancha reserva;

	public PartidoPractica(LocalDate fecha, DeporteIndividual deporte, JugadorIndividual jugadorA,
			JugadorIndividual jugadorB, ModalidadJuego modalidad, ReservaCancha reserva) {
		super(fecha, categoriaDe(jugadorA), deporte);
		if (jugadorB == null || modalidad == null || reserva == null) {
			throw new IllegalArgumentException("El partido necesita dos jugadores, modalidad y reserva.");
		}
		if (jugadorA == jugadorB) {
			throw new IllegalArgumentException("Un jugador no puede jugar contra sí mismo.");
		}
		if (categoriaDe(jugadorB) != getCategoria()) {
			throw new IllegalArgumentException("Un partido debe ser jugado entre jugadores de la misma categoría");
		}
		if (!jugadorA.estaActivo() || !jugadorB.estaActivo()) {
			throw new IllegalArgumentException("Los jugadores deben estar registrados y activos.");
		}
		if (!jugadorA.estaInscritoEn(deporte) || !jugadorB.estaInscritoEn(deporte)) {
			throw new IllegalArgumentException("Los jugadores deben estar inscritos en el deporte individual.");
		}
		if (!jugadorA.esRivalValido(jugadorB, deporte)) {
			throw new IllegalArgumentException("Los jugadores no son de niveles iguales o adyacentes.");
		}
		if (!jugadorA.estaHabilitado() || !jugadorB.estaHabilitado()) {
			throw new IllegalArgumentException("Un jugador está suspendido o lesionado.");
		}
		if (!reserva.estaActiva()) {
			throw new IllegalArgumentException("La reserva está cancelada.");
		}
		if (!reserva.getFecha().equals(fecha)) {
			throw new IllegalArgumentException("La reserva es de otra fecha.");
		}
		if (reserva.getJugador() != jugadorA && reserva.getJugador() != jugadorB) {
			throw new IllegalArgumentException("La reserva no es de ninguno de los dos jugadores.");
		}
		if (!reserva.getInstalacion().getDeporte().equals(deporte)) {
			throw new IllegalArgumentException("La instalación reservada no es del deporte del partido.");
		}
		if (reserva.getModalidad() != modalidad) {
			throw new IllegalArgumentException("La modalidad no coincide con la de la reserva.");
		}
		this.deporteIndividual = deporte;
		this.jugadorA = jugadorA;
		this.jugadorB = jugadorB;
		this.modalidad = modalidad;
		this.reserva = reserva;
		super.agregarParticipante(jugadorA);
		super.agregarParticipante(jugadorB);
	}

	private static Categoria categoriaDe(JugadorIndividual jugador) {
		if (jugador == null) {
			throw new IllegalArgumentException("Los dos jugadores son obligatorios.");
		}
		Categoria categoria = Categoria.paraEdad(jugador.getEdad());
		if (categoria == null) {
			throw new IllegalArgumentException("La edad del jugador no corresponde a ninguna categoría.");
		}
		return categoria;
	}

	@Override
	public void validar() {
		super.validar();
		if (!estaJugado() && !reserva.estaActiva()) {
			throw new IllegalStateException("La reserva del partido está cancelada.");
		}
	}

	@Override
	public void agregarParticipante(Jugador jugador) {
		if (jugador != jugadorA && jugador != jugadorB) {
			throw new IllegalArgumentException("Solo participan los dos jugadores del partido.");
		}
		super.agregarParticipante(jugador);
	}

	@Override
	public void registrarResultado(int a, int b) {
		if (a == b) {
			throw new IllegalArgumentException("Un partido de práctica no admite empates.");
		}
		RankingELO rankingA = jugadorA.getRanking(deporteIndividual);
		RankingELO rankingB = jugadorB.getRanking(deporteIndividual);
		if (rankingA == null || rankingB == null) {
			throw new IllegalStateException("Ambos jugadores deben seguir inscritos en el deporte.");
		}
		super.registrarResultado(a, b);
		rankingA.actualizar(rankingB, a > b);
	}

	public JugadorIndividual getJugadorA() {
		return jugadorA;
	}

	public JugadorIndividual getJugadorB() {
		return jugadorB;
	}

	public ModalidadJuego getModalidad() {
		return modalidad;
	}

	public ReservaCancha getReserva() {
		return reserva;
	}
}
