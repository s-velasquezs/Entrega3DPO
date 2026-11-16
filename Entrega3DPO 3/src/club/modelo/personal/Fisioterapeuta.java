package club.modelo.personal;

import java.time.LocalDate;

import club.modelo.salud.HistorialMedico;
import club.modelo.usuarios.Jugador;

public class Fisioterapeuta extends Empleado {

	public Fisioterapeuta(String username, String password, String nombre) {
		super(username, password, nombre);
	}

	private void validar(Jugador jugador) {
		if (!estaActivo()) {
			throw new IllegalStateException("El fisioterapeuta está dado de baja.");
		}
		if (jugador == null) {
			throw new IllegalArgumentException("El jugador es obligatorio.");
		}
		if (!jugador.estaActivo()) {
			throw new IllegalStateException("El jugador no está registrado como activo.");
		}
	}

	public boolean registrarLesion(Jugador jugador, String descripcion) {
		validar(jugador);
		return jugador.getHistorialMedico().registrarLesion(descripcion);
	}

	public boolean registrarLesion(Jugador jugador, String descripcion, LocalDate fecha, int diasIncapacidad) {
		validar(jugador);
		return jugador.getHistorialMedico().registrarLesion(descripcion, fecha, diasIncapacidad);
	}

	public boolean darAlta(Jugador jugador) {
		return darAlta(jugador, "Alta médica");
	}

	public boolean darAlta(Jugador jugador, String nota) {
		validar(jugador);
		return jugador.getHistorialMedico().darAlta(nota);
	}

	public HistorialMedico consultarHistorial(Jugador jugador) {
		validar(jugador);
		return jugador.getHistorialMedico();
	}

	public void editarHistorial(Jugador jugador, String cambios) {
		validar(jugador);
		jugador.getHistorialMedico().agregarObservacion(cambios);
	}
}