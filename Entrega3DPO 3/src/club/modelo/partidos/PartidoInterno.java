package club.modelo.partidos;

import java.time.LocalDate;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;

public class PartidoInterno extends Partido {

	private Equipo equipoA;
	private Equipo equipoB;

	public PartidoInterno(LocalDate fecha, Categoria categoria, Deporte deporte, Equipo equipoA, Equipo equipoB) {
		super(fecha, categoria, deporte);
		if (equipoA == null || equipoB == null) {
			throw new IllegalArgumentException("El partido interno necesita dos equipos.");
		}
		if (equipoA == equipoB) {
			throw new IllegalArgumentException("Un equipo no puede jugar contra sí mismo.");
		}
		this.equipoA = equipoA;
		this.equipoB = equipoB;
	}

	@Override
	public void validar() {
		super.validar();
		if (equipoA.getCategoria() != equipoB.getCategoria()) {
			throw new IllegalArgumentException("Un partido debe ser jugado entre equipos de la misma categoría");
		}
		if (!equipoA.getDeporte().equals(equipoB.getDeporte())) {
			throw new IllegalArgumentException("Un partido debe ser jugado entre equipos del mismo deporte");
		}
		if (equipoA.getCategoria() != getCategoria()) {
			throw new IllegalArgumentException("La categoría del partido no coincide con la de los equipos");
		}
		if (!equipoA.getDeporte().equals(getDeporte())) {
			throw new IllegalArgumentException("El deporte del partido no coincide con el de los equipos");
		}
		if (!equipoA.estaActivo() || !equipoB.estaActivo()) {
			throw new IllegalStateException("Un equipo del partido está inactivo.");
		}
		if (!estaJugado()) {
			if (!equipoA.cumpleMinimoParaJugar()) {
				throw new IllegalStateException(
						"El equipo " + equipoA.getNombre() + " no tiene el mínimo de jugadores habilitados.");
			}
			if (!equipoB.cumpleMinimoParaJugar()) {
				throw new IllegalStateException(
						"El equipo " + equipoB.getNombre() + " no tiene el mínimo de jugadores habilitados.");
			}
		}
	}

	@Override
	public void agregarParticipante(Jugador jugador) {
		if (!(jugador instanceof JugadorEquipo)) {
			throw new IllegalArgumentException("Solo participan jugadores de equipo.");
		}
		JugadorEquipo jugadorEquipo = (JugadorEquipo) jugador;
		if (!equipoA.tieneJugador(jugadorEquipo) && !equipoB.tieneJugador(jugadorEquipo)) {
			throw new IllegalArgumentException("El jugador no pertenece a ninguno de los equipos del partido.");
		}
		super.agregarParticipante(jugador);
	}

	public Equipo getEquipoA() {
		return equipoA;
	}

	public Equipo getEquipoB() {
		return equipoB;
	}
}