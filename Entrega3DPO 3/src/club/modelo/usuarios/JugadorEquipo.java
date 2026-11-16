package club.modelo.usuarios;

import java.util.ArrayList;
import java.util.List;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;

public class JugadorEquipo extends Jugador {

	private Categoria categoria;
	private List<Inscripcion> inscripciones;

	public JugadorEquipo(String username, String password, String nombre, int edad) {
		super(username, password, nombre, edad);
		this.categoria = Categoria.paraEdad(edad);
		this.inscripciones = new ArrayList<Inscripcion>();
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public boolean inscribirseEquipo(Equipo equipo, String posicion, int numero) {
		if (equipo == null) {
			throw new IllegalArgumentException("El equipo no puede ser nulo.");
		}
		if (numero < 0) {
			throw new IllegalArgumentException("El número de camiseta no puede ser negativo.");
		}
		if (!estaActivo()) {
			throw new IllegalStateException("El jugador no está registrado como activo.");
		}
		if (!equipo.estaActivo()) {
			throw new IllegalStateException("El equipo está inactivo.");
		}
		if (categoria == null || categoria != equipo.getCategoria()) {
			throw new IllegalStateException("La categoría del jugador no coincide con la del equipo.");
		}
		if (tieneEquipoEn(equipo.getDeporte())) {
			throw new IllegalStateException("El jugador ya tiene un equipo en ese deporte.");
		}
		if (!equipo.hayCupo()) {
			throw new IllegalStateException("El equipo no tiene cupo disponible.");
		}
		if (numero > 0 && !equipo.numeroDisponible(numero)) {
			throw new IllegalStateException("El número de camiseta ya está tomado en el equipo.");
		}
		Inscripcion i = new Inscripcion(this, equipo, posicion, numero);
		equipo.agregarInscripcion(i);
		inscripciones.add(i);
		return true;
	}

	public Inscripcion getInscripcion(DeporteEquipo deporte) {
		for (Inscripcion i : inscripciones) {
			if (i.estaActiva() && i.getEquipo().getDeporte().equals(deporte)) {
				return i;
			}
		}
		return null;
	}

	public boolean tieneEquipoEn(DeporteEquipo deporte) {
		return getInscripcion(deporte) != null;
	}

	public Equipo getEquipo(DeporteEquipo deporte) {
		Inscripcion i = getInscripcion(deporte);
		return (i == null) ? null : i.getEquipo();
	}

	public String getPosicion(Equipo equipo) {
		for (Inscripcion i : inscripciones) {
			if (i.estaActiva() && i.getEquipo() == equipo) {
				return i.getPosicion();
			}
		}
		return null;
	}

	public int getNumeroCamiseta(Equipo equipo) {
		for (Inscripcion i : inscripciones) {
			if (i.estaActiva() && i.getEquipo() == equipo) {
				return i.getNumeroCamiseta();
			}
		}
		return 0;
	}

	public List<Inscripcion> getInscripciones() {
		return new ArrayList<Inscripcion>(inscripciones);
	}

	@Override
	public boolean darDeBaja() {
		super.darDeBaja();
		for (Inscripcion i : inscripciones) {
			if (i.estaActiva()) {
				i.darDeBaja();
			}
		}
		return true;
	}
}
