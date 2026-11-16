package club.modelo.deportes;
import java.time.LocalDate;

public class Registro {
	private LocalDate fechaRegistro;
	private boolean activo;

	public Registro() {
		this(LocalDate.now());
	}

	public Registro(LocalDate fechaRegistro) {
		if (fechaRegistro == null) {
			throw new IllegalArgumentException("La fecha de registro es obligatoria.");
		}
		this.fechaRegistro = fechaRegistro;
		this.activo = true;
	}

	public boolean estaRegistrado() {
		return activo;
	}

	public void darDeBaja() {
		this.activo = false;
	}

	public LocalDate getFechaRegistro() {
		return fechaRegistro;
	}
}
