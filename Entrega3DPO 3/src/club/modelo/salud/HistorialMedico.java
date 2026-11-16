package club.modelo.salud;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistorialMedico {

	private List<Lesion> lesiones;
	private List<String> observaciones;

	public HistorialMedico() {
		this.lesiones = new ArrayList<Lesion>();
		this.observaciones = new ArrayList<String>();
	}

	public boolean tieneLesionActiva() {
		return getLesionActiva() != null;
	}

	public boolean estaLesionado() {
		return tieneLesionActiva();
	}

	public Lesion getLesionActiva() {
		for (Lesion l : lesiones) {
			if (l.estaActiva()) {
				return l;
			}
		}
		return null;
	}

	public boolean registrarLesion(String descripcion) {
		return registrarLesion(descripcion, LocalDate.now(), 0);
	}

	public boolean registrarLesion(String descripcion, LocalDate fecha, int diasIncapacidad) {
		Lesion lesion = new Lesion(descripcion, fecha, diasIncapacidad);
		if (tieneLesionActiva()) {
			throw new IllegalStateException("El jugador ya tiene una lesión activa.");
		}
		lesiones.add(lesion);
		observaciones.add("Lesión: " + descripcion);
		return true;
	}

	public boolean darAlta(String nota) {
		Lesion activa = getLesionActiva();
		if (activa == null) {
			throw new IllegalStateException("No hay lesión activa para cerrar.");
		}
		activa.darAlta(nota, LocalDate.now());
		observaciones.add("Alta: " + nota);
		return true;
	}

	public void agregarObservacion(String observacion) {
		if (observacion == null || observacion.isBlank()) {
			throw new IllegalArgumentException("La observación es obligatoria.");
		}
		observaciones.add(observacion);
	}

	public List<Lesion> getLesiones() {
		return new ArrayList<Lesion>(lesiones);
	}

	public List<String> getObservaciones() {
		return new ArrayList<String>(observaciones);
	}
}