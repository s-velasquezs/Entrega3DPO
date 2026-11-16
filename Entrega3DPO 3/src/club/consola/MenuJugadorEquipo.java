package club.consola;

import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.Equipo;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Inscripcion;
import club.modelo.usuarios.JugadorEquipo;

public class MenuJugadorEquipo extends MenuJugadorBase {

	private final JugadorEquipo yo;

	public MenuJugadorEquipo(Entrada in, Administrador admin, List<Deporte> deportes, JugadorEquipo jugador) {
		super(in, admin, deportes, jugador);
		this.yo = jugador;
	}

	@Override
	public void iniciar() {
		while (true) {
			switch (menu("Jugador de equipo: " + yo.getNombre() + " (" + yo.getCategoria() + ")",
					"Inscribirme a un equipo", "Ver mi equipo, camiseta y posición", "Sanciones y estado",
					"Rendimiento y estadísticas", "Próximos entrenamientos", "Próximos partidos",
					"Historial de partidos", "Horario de entrenamientos (por fechas)", "Tienda y cafetería")) {
			case 1:
				inscribirse();
				break;
			case 2:
				verEquipos();
				break;
			case 3:
				verEstado();
				break;
			case 4:
				verRendimiento();
				break;
			case 5:
				verProximosEntrenamientos();
				break;
			case 6:
				verProximosPartidos();
				break;
			case 7:
				verHistorialPartidos();
				break;
			case 8:
				verHorario();
				break;
			case 9:
				menuTienda();
				break;
			default:
				return;
			}
		}
	}

	private void inscribirse() {
		intentar(() -> {
			Equipo equipo = in.elegir("Equipos disponibles", equiposActivos(), Vista::equipo);
			if (equipo == null) {
				return;
			}
			String posicion = in.texto("Posición (enter = pendiente de asignación)");
			int numero = in.entero("Número de camiseta (0 = pendiente de asignación)");
			yo.inscribirseEquipo(equipo, posicion, numero);
			System.out.println("[OK] Quedaste inscrito en " + equipo.getNombre());
		});
	}

	private void verEquipos() {
		System.out.println("\n--- Mis equipos ---");
		boolean alguno = false;
		for (Inscripcion i : yo.getInscripciones()) {
			if (!i.estaActiva()) {
				continue;
			}
			alguno = true;
			System.out.println(" - " + i.getEquipo().getNombre() + " (" + i.getEquipo().getDeporte() + ")");
			System.out.println("     Posición: " + (i.tienePosicion() ? i.getPosicion() : "pendiente de asignación"));
			System.out.println(
					"     Camiseta: " + (i.tieneNumero() ? String.valueOf(i.getNumeroCamiseta()) : "pendiente de asignación"));
		}
		if (!alguno) {
			System.out.println("Aún no perteneces a ningún equipo.");
		}
	}
}