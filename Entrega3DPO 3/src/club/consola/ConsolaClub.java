package club.consola;

import java.util.List;

import club.modelo.comercio.Cafeteria;
import club.modelo.comercio.Tienda;
import club.modelo.deportes.Deporte;
import club.modelo.personal.Empleado;
import club.modelo.personal.Entrenador;
import club.modelo.personal.Fisioterapeuta;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;
import club.modelo.usuarios.Usuario;


public class ConsolaClub {

	private static final int MAX_INTENTOS = 3;

	private final Entrada in = new Entrada();
	private final Administrador admin;
	private final List<Deporte> deportes;

	private final ModuloComercio comercio;

	public ConsolaClub(Administrador admin, List<Deporte> deportes, Tienda tienda, Cafeteria cafeteria) {
		this.admin = admin;
		this.deportes = deportes;
		this.comercio = new ModuloComercio(in, admin, tienda, cafeteria);
	}

	public void iniciar() {
		System.out.println("==== Club Deportivo Séneca ====");
		while (true) {
			int op = menuPrincipal();
			if (op == 0) {
				System.out.println("Hasta pronto.");
				return;
			}
			if (op == 1) {
				ingresar();
			} else {
				registrarSocio();
			}
		}
	}

	private int menuPrincipal() {
		System.out.println("\n1. Ingresar\n2. Registrarme como jugador\n0. Salir");
		while (true) {
			int n = in.entero("Opción");
			if (n >= 0 && n <= 2) {
				return n;
			}
			System.out.println("  Opción inválida.");
		}
	}

	private void ingresar() {
		for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
			String user = in.textoObligatorio("Usuario");
			String pass = in.textoObligatorio("Contraseña");
			Usuario u = autenticar(user, pass);
			if (u == null) {
				System.out.println("[X] Credenciales incorrectas (" + intento + "/" + MAX_INTENTOS + ").");
				continue;
			}
			if (!estaActivo(u)) {
				System.out.println("[X] Este usuario fue dado de baja.");
				return;
			}
			abrirMenu(u);
			return;
		}
		System.out.println("Demasiados intentos fallidos.");
	}

	private Usuario autenticar(String user, String pass) {
		if (admin.autenticar(user, pass)) {
			return admin;
		}
		for (Jugador j : admin.getJugadores()) {
			if (j.autenticar(user, pass)) {
				return j;
			}
		}
		for (Empleado e : admin.getPersonal()) {
			if (e.autenticar(user, pass)) {
				return e;
			}
		}
		return null;
	}

	private boolean estaActivo(Usuario u) {
		if (u instanceof Jugador) {
			return ((Jugador) u).estaActivo();
		}
		if (u instanceof Empleado) {
			return ((Empleado) u).estaActivo();
		}
		return true;
	}

	private void abrirMenu(Usuario u) {
		if (u instanceof Administrador) {
			new MenuAdministrador(in, admin, deportes).conComercio(comercio).iniciar();
		} else if (u instanceof JugadorEquipo) {
			new MenuJugadorEquipo(in, admin, deportes, (JugadorEquipo) u).conComercio(comercio).iniciar();
		} else if (u instanceof JugadorIndividual) {
			new MenuJugadorIndividual(in, admin, deportes, (JugadorIndividual) u).conComercio(comercio).iniciar();
		} else if (u instanceof Entrenador) {
			new MenuEntrenador(in, admin, deportes, (Entrenador) u).conComercio(comercio).iniciar();
		} else if (u instanceof Fisioterapeuta) {
			new MenuFisioterapeuta(in, admin, deportes, (Fisioterapeuta) u).conComercio(comercio).iniciar();
		} else if (u instanceof Empleado) {
			new MenuEmpleado(in, admin, deportes, (Empleado) u).conComercio(comercio).iniciar();
		}
	}

	private void registrarSocio() {
		try {
			System.out.println("\n--- Registro de jugador ---");
			String user = in.textoObligatorio("Username");
			String pass = in.textoObligatorio("Password");
			String nombre = in.textoObligatorio("Nombre");
			int edad = in.entero("Edad");
			System.out.println("Tipo de jugador:\n  1. De equipo\n  2. De academia individual");
			int tipo = in.entero("Opción");
			if (tipo == 1) {
				admin.crearJugadorEquipo(user, pass, nombre, edad);
			} else if (tipo == 2) {
				admin.crearJugadorIndividual(user, pass, nombre, edad, in.confirmar("¿Eres también entrenador?"));
			} else {
				System.out.println("Opción inválida.");
				return;
			}
			System.out.println("[OK] Registro exitoso. Ya puedes ingresar.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("[X] Rechazado: " + e.getMessage());
		}
	}
}
