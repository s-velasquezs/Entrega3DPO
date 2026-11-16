package club.modelo.usuarios;

import java.util.ArrayList;
import java.util.List;

public class RankingELO {

	public static final int PUNTAJE_INICIAL = 1000;
	private static final double ESCALA = 400.0;
	private static final double FACTOR_K = 32.0;

	private int puntaje;
	private int partidosJugados;
	private List<Integer> historial;

	public RankingELO() {
		this(PUNTAJE_INICIAL, 0);
	}

	public RankingELO(int puntaje, int partidosJugados) {
		if (puntaje < 0) {
			throw new IllegalArgumentException("El puntaje no puede ser negativo.");
		}
		if (partidosJugados < 0) {
			throw new IllegalArgumentException("Los partidos jugados no pueden ser negativos.");
		}
		this.puntaje = puntaje;
		this.partidosJugados = partidosJugados;
		this.historial = new ArrayList<Integer>();
	}

	public void actualizar(RankingELO rival, boolean gano) {
		if (rival == null) {
			throw new IllegalArgumentException("El rival es obligatorio.");
		}
		if (rival == this) {
			throw new IllegalArgumentException("Un ranking no puede jugar contra sí mismo.");
		}
		double esperado = 1.0 / (1 + Math.pow(10, (rival.puntaje - puntaje) / ESCALA));
		double resultado = gano ? 1 : 0;
		int cambio = (int) Math.round(FACTOR_K * (resultado - esperado));

		puntaje = Math.max(0, puntaje + cambio);
		rival.puntaje = Math.max(0, rival.puntaje - cambio);
		partidosJugados++;
		rival.partidosJugados++;
		historial.add(puntaje);
		rival.historial.add(rival.puntaje);
	}

	public int getPuntaje() {
		return puntaje;
	}

	public int getPartidosJugados() {
		return partidosJugados;
	}

	public boolean tieneRanking() {
		return partidosJugados > 0;
	}

	public List<Integer> getHistorial() {
		return new ArrayList<Integer>(historial);
	}
}