package club.consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

import club.modelo.instalaciones.Instalacion;


public class Entrada {

	private final Scanner sc = new Scanner(System.in);

	public String texto(String mensaje) {
		System.out.print(mensaje + ": ");
		return sc.nextLine().trim();
	}

	public String textoObligatorio(String mensaje) {
		while (true) {
			String t = texto(mensaje);
			if (!t.isEmpty()) {
				return t;
			}
			System.out.println("  Este campo es obligatorio, por favor complétalo.");
		}
	}

	public int entero(String mensaje) {
		while (true) {
			try {
				return Integer.parseInt(texto(mensaje));
			} catch (NumberFormatException e) {
				System.out.println("  Escribe un número entero.");
			}
		}
	}

	public double decimal(String mensaje) {
		while (true) {
			try {
				return Double.parseDouble(texto(mensaje).replace(',', '.'));
			} catch (NumberFormatException e) {
				System.out.println("  Escribe un número (ej: 4500 o 4500.50).");
			}
		}
	}

	public LocalDate fecha(String mensaje) {
		while (true) {
			try {
				return LocalDate.parse(texto(mensaje + " (aaaa-mm-dd)"));
			} catch (DateTimeParseException e) {
				System.out.println("  Fecha inválida. Ejemplo: 2026-11-20");
			}
		}
	}

	public LocalDate fechaOHoy(String mensaje) {
		while (true) {
			String t = texto(mensaje + " (aaaa-mm-dd, enter = hoy)");
			if (t.isEmpty()) {
				return LocalDate.now();
			}
			try {
				return LocalDate.parse(t);
			} catch (DateTimeParseException e) {
				System.out.println("  Fecha inválida. Ejemplo: 2026-11-20");
			}
		}
	}

	public LocalTime hora(String mensaje) {
		while (true) {
			try {
				return LocalTime.parse(texto(mensaje + " (hh:mm)"));
			} catch (DateTimeParseException e) {
				System.out.println("  Hora inválida. Ejemplo: 15:30");
			}
		}
	}

	public boolean confirmar(String mensaje) {
		String t = texto(mensaje + " (s/n)").toLowerCase();
		return t.startsWith("s") || t.startsWith("y");
	}

	/** Devuelve la opción elegida o null si no hay opciones o se cancela. */
	public <T> T elegir(String titulo, List<T> opciones, Function<T, String> etiqueta) {
		if (opciones.isEmpty()) {
			System.out.println("  No hay opciones disponibles para: " + titulo);
			return null;
		}
		System.out.println(titulo);
		for (int i = 0; i < opciones.size(); i++) {
			System.out.println("  " + (i + 1) + ". " + etiqueta.apply(opciones.get(i)));
		}
		System.out.println("  0. Cancelar");
		while (true) {
			int n = entero("Opción");
			if (n == 0) {
				return null;
			}
			if (n >= 1 && n <= opciones.size()) {
				return opciones.get(n - 1);
			}
			System.out.println("  Opción inválida.");
		}
	}

	public <T> T elegir(String titulo, List<T> opciones) {
		return elegir(titulo, opciones, o -> String.valueOf(o));
	}

	/** Permite elegir varios por número separados por coma, "todos" o enter para ninguno. */
	public <T> List<T> elegirVarios(String titulo, List<T> opciones, Function<T, String> etiqueta) {
		List<T> elegidos = new ArrayList<T>();
		if (opciones.isEmpty()) {
			System.out.println("  No hay opciones disponibles para: " + titulo);
			return elegidos;
		}
		System.out.println(titulo);
		for (int i = 0; i < opciones.size(); i++) {
			System.out.println("  " + (i + 1) + ". " + etiqueta.apply(opciones.get(i)));
		}
		String t = texto("Números separados por coma, 'todos', o enter para ninguno");
		if (t.equalsIgnoreCase("todos")) {
			return new ArrayList<T>(opciones);
		}
		if (t.isEmpty()) {
			return elegidos;
		}
		for (String parte : t.split(",")) {
			try {
				int n = Integer.parseInt(parte.trim());
				if (n >= 1 && n <= opciones.size() && !elegidos.contains(opciones.get(n - 1))) {
					elegidos.add(opciones.get(n - 1));
				} else {
					System.out.println("  Se ignoró: " + parte.trim());
				}
			} catch (NumberFormatException e) {
				System.out.println("  Se ignoró: " + parte.trim());
			}
		}
		return elegidos;
	}

	public <T> List<T> elegirVarios(String titulo, List<T> opciones) {
		return elegirVarios(titulo, opciones, o -> String.valueOf(o));
	}

	public <E extends Enum<E>> E elegirEnum(String titulo, E[] valores) {
		List<E> lista = new ArrayList<E>();
		for (E v : valores) {
			lista.add(v);
		}
		return elegir(titulo, lista);
	}

}
