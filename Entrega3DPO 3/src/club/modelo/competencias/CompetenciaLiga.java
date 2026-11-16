package club.modelo.competencias;

import java.util.Map;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.enums.Categoria;

public class CompetenciaLiga extends Competencia {

	public CompetenciaLiga(String nombre, String entidadOrganizadora, int temporada, DeporteEquipo deporte,
			Categoria categoria) {
		super(nombre, entidadOrganizadora, temporada, deporte, categoria);
	}

	@Override
	public Map<String, Integer> calcularTabla() {
		return ordenarPorPuntos(acumularPuntos());
	}
}

