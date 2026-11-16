package club.consola;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.enums.Nivel;
import club.modelo.instalaciones.ReservaCancha;
import club.modelo.partidos.PartidoPractica;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.InscripcionIndividual;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorIndividual;
import club.modelo.usuarios.RankingELO;

public class MenuJugadorIndividual extends MenuJugadorBase {

	private final JugadorIndividual yo;

	public MenuJugadorIndividual(Entrada in, Administrador admin, List<Deporte> deportes,
			JugadorIndividual jugador) {
		super(in, admin, deportes, jugador);
		this.yo = jugador;
	}

	@Override
	public void iniciar() {
		while (true) {
			switch (menu("Jugador de academia: " + yo.getNombre() + (yo.esTambienEntrenador() ? " (también entrenador)" : ""),
					"Inscribirme a un deporte y nivel", "Mis inscripciones y ranking ELO", "Mis reservas",
					"Programar partido de práctica", "Sanciones y estado", "Rendimiento y estadísticas",
					"Próximos entrenamientos", "Próximos partidos", "Historial de partidos",
					"Horario de entrenamientos (por fechas)", "Tienda y cafetería")) {
			case 1:
				inscribirse();
				break;
			case 2:
				verInscripciones();
				break;
			case 3:
				verReservas();
				break;
			case 4:
				programarPractica();
				break;
			case 5:
				verEstado();
				break;
			case 6:
				verRendimiento();
				break;
			case 7:
				verProximosEntrenamientos();
				break;
			case 8:
				verProximosPartidos();
				break;
			case 9:
				verHistorialPartidos();
				break;
			case 10:
				verHorario();
				break;
			case 11:
				menuTienda();
				break;
			default:
				return;
			}
		}
	}

	private void inscribirse() {
		intentar(() -> {
			List<String> nombres = new ArrayList<String>();
			for (DeporteIndividual d : deportesIndividuales()) {
				nombres.add(d.getNombre());
			}
			String texto = in.textoObligatorio("Deporte (" + String.join(", ", nombres) + ")");
			DeporteIndividual elegido = null;
			for (DeporteIndividual d : deportesIndividuales()) {
				if (d.getNombre().equalsIgnoreCase(texto)) {
					elegido = d;
				}
			}
			if (elegido == null) {
				throw new IllegalArgumentException("La academia no ofrece el deporte '" + texto + "'.");
			}
			Nivel nivel = in.elegirEnum("Nivel", Nivel.values());
			if (nivel == null) {
				return;
			}
			yo.inscribirseDeporte(elegido, nivel);
			System.out.println("[OK] Quedaste inscrito en " + elegido.getNombre() + " como " + nivel);
		});
	}

	private void verInscripciones() {
		System.out.println("\n--- Mis inscripciones y ranking ELO ---");
		boolean alguna = false;
		for (InscripcionIndividual i : yo.getInscripciones()) {
			if (!i.estaActiva()) {
				continue;
			}
			alguna = true;
			RankingELO r = i.getRankingELO();
			String ranking = r.tieneRanking() ? r.getPuntaje() + " puntos en " + r.getPartidosJugados() + " partido(s)"
					: "sin ranking (aún no ha jugado)";
			System.out.println(" - " + i.getDeporte().getNombre() + " | nivel " + i.getNivel() + " | " + ranking);
		}
		if (!alguna) {
			System.out.println("No estás inscrito en ningún deporte.");
		}
	}

	private List<ReservaCancha> misReservasActivas() {
		List<ReservaCancha> lista = new ArrayList<ReservaCancha>();
		for (ReservaCancha r : admin.getReservas()) {
			if (r.getJugador() == yo && r.estaActiva()) {
				lista.add(r);
			}
		}
		return lista;
	}

	private void verReservas() {
		System.out.println("\n--- Mis reservas activas ---");
		List<ReservaCancha> lista = misReservasActivas();
		if (lista.isEmpty()) {
			System.out.println("No tienes reservas activas.");
		}
		for (ReservaCancha r : lista) {
			System.out.println(" - " + Vista.reserva(r));
		}
	}

	private void programarPractica() {
		intentar(() -> {
			ReservaCancha reserva = in.elegir("Elige la reserva donde se jugará", misReservasActivas(),
					Vista::reserva);
			if (reserva == null) {
				System.out.println("Primero necesitas una reserva activa.");
				return;
			}
			if (!(reserva.getInstalacion().getDeporte() instanceof DeporteIndividual)) {
				throw new IllegalArgumentException("La instalación reservada no es de un deporte individual.");
			}
			DeporteIndividual deporte = (DeporteIndividual) reserva.getInstalacion().getDeporte();
			List<JugadorIndividual> rivales = new ArrayList<JugadorIndividual>();
			for (Jugador j : admin.getJugadores()) {
				if (j instanceof JugadorIndividual && j != yo && j.estaActivo()) {
					rivales.add((JugadorIndividual) j);
				}
			}
			JugadorIndividual rival = in.elegir("Elige a tu rival", rivales, Jugador::toString);
			if (rival == null) {
				return;
			}
			
			if (!yo.esRivalValido(rival, deporte)) {
				throw new IllegalArgumentException(
						"Los jugadores no son de niveles iguales o adyacentes (y ninguno es también entrenador).");
			}
			PartidoPractica partido = new PartidoPractica((LocalDate) reserva.getFecha(), deporte, yo, rival,
					reserva.getModalidad(), reserva);
			admin.registrarPartido(partido);
			System.out.println("[OK] Partido de práctica agendado contra " + rival.getNombre());
		});
	}
}