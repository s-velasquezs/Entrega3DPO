package club.consola;

import java.time.LocalDate;
import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.personal.Fisioterapeuta;
import club.modelo.salud.HistorialMedico;
import club.modelo.salud.Lesion;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Jugador;

public class MenuFisioterapeuta extends MenuEmpleadoBase {

	private final Fisioterapeuta fisio;

	public MenuFisioterapeuta(Entrada in, Administrador admin, List<Deporte> deportes, Fisioterapeuta fisio) {
		super(in, admin, deportes, fisio);
		this.fisio = fisio;
	}

	@Override
	public void iniciar() {
		while (true) {
			switch (menu("Fisioterapeuta: " + fisio.getNombre(), "Registrar lesión", "Dar alta médica",
					"Ver historial médico de un jugador", "Agregar observación al historial", "Mis turnos",
					"Solicitar cambio de turno", "Mis solicitudes")) {
			case 1:
				registrarLesion();
				break;
			case 2:
				darAlta();
				break;
			case 3:
				verHistorial();
				break;
			case 4:
				agregarObservacion();
				break;
			case 5:
				verTurnos();
				break;
			case 6:
				solicitarCambio();
				break;
			case 7:
				verSolicitudes();
				break;
			default:
				return;
			}
		}
	}

	private Jugador elegirJugador() {
		return in.elegir("Jugador", admin.getJugadores(), Vista::jugador);
	}

	private void registrarLesion() {
		intentar(() -> {
			Jugador j = elegirJugador();
			if (j == null) {
				return;
			}
			String descripcion = in.texto("Tipo/descripción de la lesión");
			LocalDate fecha = in.fechaOHoy("Fecha de la lesión");
			int dias = in.entero("Días de incapacidad");
			fisio.registrarLesion(j, descripcion, fecha, dias);
			System.out.println("[OK] Lesión registrada. " + j.getNombre() + " queda inhabilitado para partidos.");
		});
	}

	private void darAlta() {
		intentar(() -> {
			Jugador j = elegirJugador();
			if (j == null) {
				return;
			}
			String nota = in.texto("Nota del alta (enter = 'Alta médica')");
			if (nota.isEmpty()) {
				fisio.darAlta(j);
			} else {
				fisio.darAlta(j, nota);
			}
			System.out.println("[OK] " + j.getNombre() + " quedó habilitado nuevamente.");
		});
	}

	private void verHistorial() {
		intentar(() -> {
			Jugador j = elegirJugador();
			if (j == null) {
				return;
			}
			HistorialMedico h = fisio.consultarHistorial(j);
			System.out.println("\n--- Historial médico de " + j.getNombre() + " ---");
			System.out.println("Estado: " + (h.tieneLesionActiva() ? "LESIONADO" : "sin lesión activa"));
			if (h.getLesiones().isEmpty() && h.getObservaciones().isEmpty()) {
				System.out.println("El historial está vacío.");
			}
			for (Lesion l : h.getLesiones()) {
				System.out.println(" - " + l.getFechaRegistro() + " | " + l.getDescripcion() + " | incapacidad: "
						+ l.getDiasIncapacidad() + " días | "
						+ (l.estaActiva() ? "ACTIVA" : "alta el " + l.getFechaAlta() + " (" + l.getNotaAlta() + ")"));
			}
			for (String o : h.getObservaciones()) {
				System.out.println("   nota: " + o);
			}
		});
	}

	private void agregarObservacion() {
		intentar(() -> {
			Jugador j = elegirJugador();
			if (j == null) {
				return;
			}
			fisio.editarHistorial(j, in.texto("Observación"));
			System.out.println("[OK] Observación agregada.");
		});
	}
}