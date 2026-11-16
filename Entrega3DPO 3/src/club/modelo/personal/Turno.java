package club.modelo.personal;

import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {

	private Empleado empleado;
	private LocalDate fecha;
	private LocalTime horaInicio;
	private LocalTime horaFin;

	public Turno(Empleado empleado, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
		if (empleado == null) {
			throw new IllegalArgumentException("El empleado del turno es obligatorio.");
		}
		if (fecha == null || horaInicio == null || horaFin == null) {
			throw new IllegalArgumentException("La fecha y las horas del turno son obligatorias.");
		}
		if (!horaInicio.isBefore(horaFin)) {
			throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
		}
		this.empleado = empleado;
		this.fecha = fecha;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
	}

	public boolean seSobrepone(LocalTime otraHoraInicio, LocalTime otraHoraFin) {
		if (otraHoraInicio == null || otraHoraFin == null) {
			throw new IllegalArgumentException("Las horas son obligatorias.");
		}
		return horaInicio.isBefore(otraHoraFin) && otraHoraInicio.isBefore(horaFin);
	}

	public boolean seSobrepone(LocalDate fechaTurno, LocalTime otraHoraInicio, LocalTime otraHoraFin) {
		if (fechaTurno == null) {
			throw new IllegalArgumentException("La fecha es obligatoria.");
		}
		return fecha.equals(fechaTurno) && seSobrepone(otraHoraInicio, otraHoraFin);
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(Empleado empleado) {
		if (empleado == null) {
			throw new IllegalArgumentException("El empleado del turno es obligatorio.");
		}
		this.empleado = empleado;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		if (fecha == null) {
			throw new IllegalArgumentException("La fecha del turno es obligatoria.");
		}
		this.fecha = fecha;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(LocalTime horaInicio) {
		if (horaInicio == null || !horaInicio.isBefore(horaFin)) {
			throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
		}
		this.horaInicio = horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}

	public void setHoraFin(LocalTime horaFin) {
		if (horaFin == null || !horaInicio.isBefore(horaFin)) {
			throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
		}
		this.horaFin = horaFin;
	}

	@Override
	public String toString() {
		return fecha + " " + horaInicio + "-" + horaFin;
	}
}