package club.modelo.partidos;

import java.time.LocalDate;

import club.modelo.competencias.Competencia;
import club.modelo.deportes.Equipo;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;

public class PartidoExterno extends Partido {

	private Equipo equipoClub;
	private String institucionRival;
	private String escenario;
	private Competencia competencia;

	public PartidoExterno(LocalDate fecha, Equipo equipoClub, String institucionRival, String escenario) {
		this(fecha, equipoClub, institucionRival, escenario, null);
	}

	public PartidoExterno(LocalDate fecha, Equipo equipoClub, String institucionRival, String escenario,
			Competencia competencia) {
		super(fecha, exigirEquipo(equipoClub).getCategoria(), exigirEquipo(equipoClub).getDeporte());
		if (!equipoClub.estaActivo()) {
			throw new IllegalArgumentException("El equipo del club está inactivo.");
		}
		if (institucionRival == null || institucionRival.isBlank()) {
			throw new IllegalArgumentException("La institución rival es obligatoria.");
		}
		if (escenario == null || escenario.isBlank()) {
			throw new IllegalArgumentException("El escenario es obligatorio.");
		}
		if (competencia != null) {
			if (!competencia.getDeporte().equals(equipoClub.getDeporte())) {
				throw new IllegalArgumentException("La competencia es de otro deporte.");
			}
			if (competencia.getCategoria() != equipoClub.getCategoria()) {
				throw new IllegalArgumentException("La competencia es de otra categoría.");
			}
			if (!competencia.estaInscrito(equipoClub)) {
				throw new IllegalArgumentException("El equipo no está inscrito en la competencia.");
			}
		}
		this.equipoClub = equipoClub;
		this.institucionRival = institucionRival;
		this.escenario = escenario;
		this.competencia = competencia;
	}

	private static Equipo exigirEquipo(Equipo equipo) {
		if (equipo == null) {
			throw new IllegalArgumentException("El partido externo necesita un equipo del club.");
		}
		return equipo;
	}

	@Override
	public void validar() {
		super.validar();
		if (!equipoClub.estaActivo()) {
			throw new IllegalStateException("El equipo del club está inactivo.");
		}
		if (!estaJugado() && !equipoClub.cumpleMinimoParaJugar()) {
			throw new IllegalStateException(
					"El equipo " + equipoClub.getNombre() + " no tiene el mínimo de jugadores habilitados.");
		}
	}

	@Override
	public void agregarParticipante(Jugador jugador) {
		if (jugador == null) {
			throw new IllegalArgumentException("El jugador no puede ser nulo.");
		}
		if (!(jugador instanceof JugadorEquipo) || !equipoClub.tieneJugador((JugadorEquipo) jugador)) {
			throw new IllegalArgumentException("El jugador no pertenece al equipo del club.");
		}
		if (competencia != null && !competencia.estaEnNomina(equipoClub, (JugadorEquipo) jugador)) {
			throw new IllegalArgumentException("El jugador no está en la nómina de la competencia.");
		}
		super.agregarParticipante(jugador);
	}

	public boolean esAmistoso() {
		return competencia == null;
	}

	public Equipo getEquipoClub() {
		return equipoClub;
	}

	public String getInstitucionRival() {
		return institucionRival;
	}

	public String getEscenario() {
		return escenario;
	}

	public Competencia getCompetencia() {
		return competencia;
	}
}