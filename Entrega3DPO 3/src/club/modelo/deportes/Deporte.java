package club.modelo.deportes;
public abstract class Deporte {

	private String nombre;

	public Deporte(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del deporte es obligatorio.");
		}
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		return nombre.equals(((Deporte) o).nombre);
	}

	@Override
	public int hashCode() {
		return nombre.hashCode();
	}

	@Override
	public String toString() {
		return nombre;
	}
}