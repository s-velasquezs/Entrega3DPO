package club.modelo.usuarios;

import java.util.ArrayList;
import java.util.List;

import club.modelo.deportes.DeporteIndividual;
import club.modelo.enums.Nivel;

public class JugadorIndividual extends Jugador {

	private List<InscripcionIndividual> inscripciones;
	private boolean tambienEntrenador;

	public JugadorIndividual(String username, String password, String nombre, int edad,
			boolean tambienEntrenador) {
		super(username, password, nombre, edad);
		this.inscripciones = new ArrayList<InscripcionIndividual>();
		this.tambienEntrenador = tambienEntrenador;
	}

	public boolean esTambienEntrenador() {
		return tambienEntrenador;
	}

	public boolean inscribirseDeporte(DeporteIndividual deporte, Nivel nivel) {
		if (deporte == null || nivel == null) {
			throw new IllegalArgumentException("El deporte y el nivel son obligatorios.");
		}
		if (!estaActivo()) {
			throw new IllegalStateException("El jugador no está registrado como activo.");
		}
		if (getInscripcion(deporte) != null) {
			throw new IllegalStateException("El jugador ya está inscrito en ese deporte.");
		}
		inscripciones.add(new InscripcionIndividual(this, deporte, nivel));
		return true;
	}

	public InscripcionIndividual getInscripcion(DeporteIndividual deporte) {
		for (InscripcionIndividual i : inscripciones) {
			if (i.estaActiva() && i.getDeporte().equals(deporte)) {
				return i;
			}
		}
		return null;
	}

	public Nivel getNivel(DeporteIndividual deporte) {
		InscripcionIndividual i = getInscripcion(deporte);
		return (i == null) ? null : i.getNivel();
	}

	public RankingELO getRanking(DeporteIndividual deporte) {
		InscripcionIndividual i = getInscripcion(deporte);
		return (i == null) ? null : i.getRankingELO();
	}

	public boolean esRivalValido(JugadorIndividual rival, DeporteIndividual deporte) {
		if (rival == null || deporte == null) {
			throw new IllegalArgumentException("El rival y el deporte son obligatorios.");
		}
		if (this == rival) {
			return false;
		}
		Nivel miNivel = getNivel(deporte);
		Nivel suNivel = rival.getNivel(deporte);
		if (miNivel == null || suNivel == null) {
			return false;
		}
		if (tambienEntrenador || rival.esTambienEntrenador()) {
			return true;
		}
		return miNivel.esCompatibleCon(suNivel);
	}

	public List<InscripcionIndividual> getInscripciones() {
		return new ArrayList<InscripcionIndividual>(inscripciones);
	}

	@Override
	public boolean darDeBaja() {
		super.darDeBaja();
		for (InscripcionIndividual i : inscripciones) {
			if (i.estaActiva()) {
				i.darDeBaja();
			}
		}
		return true;
	}

	public boolean estaInscritoEn(DeporteIndividual deporte) {
		return getInscripcion(deporte) != null;
	}
}
