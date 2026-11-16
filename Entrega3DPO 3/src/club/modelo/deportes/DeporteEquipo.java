package club.modelo.deportes;

public class DeporteEquipo extends Deporte {
	private int minimoJugadoresHabilitados;
	private boolean permiteEmpate;
	private int puntosVictoria;
	private int puntosEmpate;
	private int puntosDerrota;

	// Reglas por defecto (estilo futbol): permite empate, victoria 3, empate 1, derrota 0
	public DeporteEquipo(String nombre, int minimoJugadoresHabilitados) {
		this(nombre, minimoJugadoresHabilitados, true, 3, 1, 0);
	}

	public DeporteEquipo(String nombre, int minimoJugadoresHabilitados, boolean permiteEmpate,
			int puntosVictoria, int puntosEmpate, int puntosDerrota) {
		super(nombre);
		if (minimoJugadoresHabilitados <= 0) {
			throw new IllegalArgumentException("El mínimo de jugadores habilitados debe ser mayor que cero.");
		}
		if (puntosVictoria < 0 || puntosEmpate < 0 || puntosDerrota < 0) {
			throw new IllegalArgumentException("Los puntos no pueden ser negativos.");
		}
		this.minimoJugadoresHabilitados = minimoJugadoresHabilitados;
		this.permiteEmpate = permiteEmpate;
		this.puntosVictoria = puntosVictoria;
		this.puntosEmpate = puntosEmpate;
		this.puntosDerrota = puntosDerrota;
	}

	public int getMinimoJugadoresHabilitados() {
		return minimoJugadoresHabilitados;
	}

	public boolean permiteEmpate() {
		return permiteEmpate;
	}

	public int puntosPorVictoria() {
		return puntosVictoria;
	}

	public int puntosPorEmpate() {
		return puntosEmpate;
	}

	public int puntosPorDerrota() {
		return puntosDerrota;
	}

	public int getPuntosVictoria() {
		return puntosVictoria;
	}

	public int getPuntosEmpate() {
		return puntosEmpate;
	}

	public int getPuntosDerrota() {
		return puntosDerrota;
	}
	
	public int puntosPara(int tantosPropios, int tantosRival) {
		if (tantosPropios < 0 || tantosRival < 0) {
			throw new IllegalArgumentException("Los tantos no pueden ser negativos.");
		}
		if (tantosPropios > tantosRival) {
			return puntosVictoria;
		}
		if (tantosPropios == tantosRival) {
			if (!permiteEmpate) {
				throw new IllegalArgumentException("Este deporte no permite empates.");
			}
			return puntosEmpate;
		}
		return puntosDerrota;
	}
	
}