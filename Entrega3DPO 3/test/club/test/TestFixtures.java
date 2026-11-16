package club.test;

import java.util.ArrayList;
import java.util.List;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.usuarios.JugadorEquipo;

public final class TestFixtures {

	private TestFixtures() {
	}

	public static DeporteEquipo futbol() {
		return new DeporteEquipo("Futbol", 7, true, 3, 1, 0);
	}

	public static DeporteEquipo baloncesto() {
		return new DeporteEquipo("Baloncesto", 5, false, 2, 0, 1);
	}

	public static DeporteEquipo voleibol() {
		return new DeporteEquipo("Voleibol", 6, false, 2, 0, 1);
	}

	public static DeporteIndividual tenis() {
		return new DeporteIndividual("Tenis", 4);
	}

	public static DeporteIndividual padel() {
		return new DeporteIndividual("Padel", 3);
	}

	public static int edadPara(Categoria categoria) {
		switch (categoria) {
		case SUB13:
			return 12;
		case SUB15:
			return 15;
		case SUB17:
			return 17;
		default:
			return 20;
		}
	}

	public static Equipo equipo(String nombre, DeporteEquipo deporte, Categoria categoria) {
		return new Equipo(nombre, categoria, deporte, 25);
	}

	public static List<JugadorEquipo> poblar(Equipo equipo, int cantidad, String prefijo) {
		List<JugadorEquipo> lista = new ArrayList<JugadorEquipo>();
		for (int i = 1; i <= cantidad; i++) {
			JugadorEquipo j = new JugadorEquipo(prefijo + i, "pass" + i, "Jugador " + prefijo + i,
					edadPara(equipo.getCategoria()));
			j.inscribirseEquipo(equipo, "Delantero", i);
			lista.add(j);
		}
		return lista;
	}
}
