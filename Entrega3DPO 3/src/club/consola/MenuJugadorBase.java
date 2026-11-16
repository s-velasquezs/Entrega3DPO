package club.consola;

import java.time.LocalDate;
import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.estadisticas.EstadisticasJugador;
import club.modelo.partidos.Partido;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Jugador;

public abstract class MenuJugadorBase extends MenuBase {

	private static final String[] CLAVES = { "goles", "asistencias", "puntos", "rebotes", "aces", "bloqueos",
			"amarillas", "rojas" };

	protected final Jugador jugador;

	protected MenuJugadorBase(Entrada in, Administrador admin, List<Deporte> deportes, Jugador jugador) {
		super(in, admin, deportes);
		this.jugador = jugador;
	}

	/** Compras del jugador en la tienda y la cafetería del club. */
	protected void menuTienda() {
		while (true) {
			switch (menu("Tienda y cafetería", "Comprar en la tienda", "Comprar en la cafetería",
					"Ver productos de la tienda", "Ver productos de la cafetería", "Sugerir un producto nuevo",
					"Mis puntos de fidelidad", "Mis compras")) {
			case 1:
				comercio.comprar(jugador, false);
				break;
			case 2:
				comercio.comprar(jugador, true);
				break;
			case 3:
				comercio.verCatalogo(ModuloComercio.TIENDA);
				break;
			case 4:
				comercio.verCatalogo(ModuloComercio.CAFETERIA);
				break;
			case 5:
				comercio.sugerirProducto(jugador);
				break;
			case 6:
				comercio.verPuntos(jugador);
				break;
			case 7:
				comercio.verMisCompras(jugador);
				break;
			default:
				return;
			}
		}
	}

	protected void verEstado() {
		System.out.println("\n--- Sanciones y estado ---");
		if (jugador.estaSuspendido()) {
			System.out.println("Estado: SUSPENDIDO. Partidos de suspensión pendientes: "
					+ jugador.consultarEstadisticas().getPartidosSuspension());
		} else if (jugador.getHistorialMedico().tieneLesionActiva()) {
			System.out.println("Estado: INHABILITADO por lesión activa.");
		} else {
			System.out.println("Estado: habilitado.");
		}
		List<String> sanciones = jugador.consultarSanciones();
		if (sanciones.isEmpty()) {
			System.out.println("Sin sanciones registradas.");
		} else {
			for (String s : sanciones) {
				System.out.println(" - " + s);
			}
		}
	}

	protected void verRendimiento() {
		System.out.println("\n--- Rendimiento y estadísticas acumuladas ---");
		EstadisticasJugador est = jugador.consultarEstadisticas();
		for (String clave : CLAVES) {
			System.out.println(String.format("  %-12s %d", clave, est.getValor(clave)));
		}
	}

	protected void verProximosEntrenamientos() {
		System.out.println("\n--- Próximos entrenamientos ---");
		List<Entrenamiento> lista = jugador.consultarProximosEntrenamientos();
		if (lista.isEmpty()) {
			System.out.println("No tienes entrenamientos próximos.");
		}
		for (Entrenamiento e : lista) {
			System.out.println(" - " + Vista.entrenamiento(e));
		}
	}

	protected void verProximosPartidos() {
		System.out.println("\n--- Próximos partidos ---");
		List<Partido> lista = jugador.consultarProximosPartidos();
		if (lista.isEmpty()) {
			System.out.println("No tienes partidos próximos.");
		}
		for (Partido p : lista) {
			System.out.println(" - " + Vista.partido(p));
		}
	}

	protected void verHistorialPartidos() {
		System.out.println("\n--- Historial de partidos (orden cronológico) ---");
		List<Partido> lista = jugador.consultarHistorialPartidos();
		if (lista.isEmpty()) {
			System.out.println("Tu historial está vacío.");
		}
		for (Partido p : lista) {
			System.out.println(" - " + Vista.partido(p));
		}
	}

	protected void verHorario() {
		intentar(() -> {
			LocalDate desde = in.fecha("Desde");
			LocalDate hasta = in.fecha("Hasta");
			List<Entrenamiento> lista = jugador.consultarHorario(desde, hasta);
			if (lista.isEmpty()) {
				System.out.println("No hay sesiones en el periodo consultado.");
			}
			for (Entrenamiento e : lista) {
				System.out.println(" - " + Vista.entrenamiento(e));
			}
		});
	}
}