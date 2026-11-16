package club.modelo.deportes;

public class DeporteIndividual extends Deporte {

	private int maxInstalacionesSimultaneas;

	public DeporteIndividual(String nombre, int maxInstalacionesSimultaneas) {
		super(nombre);
		if (maxInstalacionesSimultaneas <= 0) {
			throw new IllegalArgumentException("El máximo de instalaciones simultáneas debe ser mayor que cero.");
		}
		this.maxInstalacionesSimultaneas = maxInstalacionesSimultaneas;
	}

	public int getMaxInstalaciones() {
		return maxInstalacionesSimultaneas;
	}

	public String getDeporteIndividual() {
		return getNombre();
	}
}
