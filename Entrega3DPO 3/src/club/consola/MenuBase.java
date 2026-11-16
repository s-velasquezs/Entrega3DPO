package club.consola;

import java.util.ArrayList;
import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.usuarios.Administrador;

public abstract class MenuBase {

	protected final Entrada in;
	protected final Administrador admin;
	protected final List<Deporte> deportes;
	protected ModuloComercio comercio;

	protected MenuBase(Entrada in, Administrador admin, List<Deporte> deportes) {
		this.in = in;
		this.admin = admin;
		this.deportes = deportes;
	}

	/** Conecta el módulo de tienda y cafetería (lo llama ConsolaClub al abrir el menú). */
	public MenuBase conComercio(ModuloComercio comercio) {
		this.comercio = comercio;
		return this;
	}

	public interface Accion {
		void ejecutar();
	}

	/** Ejecuta una operación y muestra el motivo si el modelo la rechaza (sin cambiar el estado). */
	protected void intentar(Accion accion) {
		try {
			accion.ejecutar();
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("[X] Rechazado: " + e.getMessage());
		}
	}

	protected int menu(String titulo, String... opciones) {
		System.out.println("\n=== " + titulo + " ===");
		for (int i = 0; i < opciones.length; i++) {
			System.out.println((i + 1) + ". " + opciones[i]);
		}
		System.out.println("0. Salir / volver");
		while (true) {
			int n = in.entero("Opción");
			if (n >= 0 && n <= opciones.length) {
				return n;
			}
			System.out.println("  Opción inválida.");
		}
	}

	protected List<DeporteEquipo> deportesEquipo() {
		List<DeporteEquipo> lista = new ArrayList<DeporteEquipo>();
		for (Deporte d : deportes) {
			if (d instanceof DeporteEquipo) {
				lista.add((DeporteEquipo) d);
			}
		}
		return lista;
	}

	protected List<DeporteIndividual> deportesIndividuales() {
		List<DeporteIndividual> lista = new ArrayList<DeporteIndividual>();
		for (Deporte d : deportes) {
			if (d instanceof DeporteIndividual) {
				lista.add((DeporteIndividual) d);
			}
		}
		return lista;
	}

	protected List<Equipo> equiposActivos() {
		List<Equipo> lista = new ArrayList<Equipo>();
		for (Equipo e : admin.getEquipos()) {
			if (e.estaActivo()) {
				lista.add(e);
			}
		}
		return lista;
	}

	public abstract void iniciar();
}