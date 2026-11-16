package club.modelo.salud;

import java.time.LocalDate;

public class Lesion {

	private String descripcion;
	private LocalDate fechaRegistro;
	private int diasIncapacidad;
	private LocalDate fechaAlta;
	private String notaAlta;

	public Lesion(String descripcion) {
		this(descripcion, LocalDate.now(), 0);
	}

	public Lesion(String descripcion, LocalDate fechaRegistro) {
		this(descripcion, fechaRegistro, 0);
	}

	public Lesion(String descripcion, LocalDate fechaRegistro, int diasIncapacidad) {
		if (descripcion == null || descripcion.isBlank()) {
			throw new IllegalArgumentException("La descripción de la lesión es obligatoria.");
		}
		if (fechaRegistro == null) {
			throw new IllegalArgumentException("La fecha de registro es obligatoria.");
		}
		if (diasIncapacidad < 0) {
			throw new IllegalArgumentException("Los días de incapacidad no pueden ser negativos.");
		}
		this.descripcion = descripcion;
		this.fechaRegistro = fechaRegistro;
		this.diasIncapacidad = diasIncapacidad;
	}

	public boolean estaActiva() {
		return fechaAlta == null;
	}

	public void darAlta(String nota, LocalDate fecha) {
		if (nota == null || nota.isBlank()) {
			throw new IllegalArgumentException("La nota de alta es obligatoria.");
		}
		if (fecha == null) {
			throw new IllegalArgumentException("La fecha de alta es obligatoria.");
		}
		if (!estaActiva()) {
			throw new IllegalStateException("La lesión ya tiene alta.");
		}
		if (fecha.isBefore(fechaRegistro)) {
			throw new IllegalArgumentException("El alta no puede ser anterior al registro de la lesión.");
		}
		this.fechaAlta = fecha;
		this.notaAlta = nota;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public LocalDate getFechaRegistro() {
		return fechaRegistro;
	}

	public int getDiasIncapacidad() {
		return diasIncapacidad;
	}

	public LocalDate getFechaAlta() {
		return fechaAlta;
	}

	public String getNotaAlta() {
		return notaAlta;
	}
}