package club.consola;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import club.modelo.comercio.Cafeteria;
import club.modelo.comercio.Inventario;
import club.modelo.comercio.Producto;
import club.modelo.comercio.ProgramaFidelidad;
import club.modelo.comercio.SugerenciaProducto;
import club.modelo.comercio.Tienda;
import club.modelo.comercio.VentaProducto;
import club.modelo.enums.Rubro;
import club.modelo.personal.Empleado;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.Usuario;

/**
 * Funciones de consola para la tienda y la cafetería: ventas, puntos de fidelidad,
 * sugerencias de productos, inventario y reportes. Los menús de jugador, empleado y
 * administrador reutilizan estos métodos.
 */
public class ModuloComercio {

	public static final String TIENDA = "TIENDA";
	public static final String CAFETERIA = "CAFETERIA";
	public static final String BODEGA = "BODEGA";

	private final Entrada in;
	private final Administrador admin;
	private final Tienda tienda;
	private final Cafeteria cafeteria;

	public ModuloComercio(Entrada in, Administrador admin, Tienda tienda, Cafeteria cafeteria) {
		this.in = in;
		this.admin = admin;
		this.tienda = tienda;
		this.cafeteria = cafeteria;
	}

	private void intentar(Runnable accion) {
		try {
			accion.run();
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("[X] Rechazado: " + e.getMessage());
		}
	}

	private static String dinero(double valor) {
		return String.format("$%,.0f", valor);
	}

	private Inventario inventario() {
		return admin.getInventario();
	}

	// ------------------------------------------------------------------ catálogo

	private List<Producto> catalogo() {
		List<Producto> lista = new ArrayList<Producto>(inventario().listarExistencias().keySet());
		lista.sort((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
		return lista;
	}

	private List<Producto> disponibles(String ubicacion) {
		List<Producto> lista = new ArrayList<Producto>();
		for (Producto p : catalogo()) {
			if (inventario().getCantidad(p, ubicacion) > 0) {
				lista.add(p);
			}
		}
		return lista;
	}

	public void verCatalogo(String ubicacion) {
		System.out.println("\n--- Productos disponibles en " + ubicacion.toLowerCase() + " ---");
		List<Producto> lista = disponibles(ubicacion);
		if (lista.isEmpty()) {
			System.out.println("No hay productos con existencias en este momento.");
		}
		for (Producto p : lista) {
			System.out.println(" - " + p + " | " + dinero(p.getPrecio()) + " | quedan "
					+ inventario().getCantidad(p, ubicacion));
		}
	}

	// ------------------------------------------------------------------ ventas

	/** Arma una compra (varios productos) para el comprador indicado en la tienda o la cafetería. */
	public void comprar(Usuario comprador, boolean enCafeteria) {
		intentar(() -> {
			String ubicacion = enCafeteria ? CAFETERIA : TIENDA;
			System.out.println("\n--- " + (enCafeteria ? "Cafetería" : "Tienda") + ": nueva compra de "
					+ comprador.getUsername() + " ---");
			Map<Producto, Integer> items = new LinkedHashMap<Producto, Integer>();
			while (true) {
				List<Producto> opciones = disponibles(ubicacion);
				Producto p = in.elegir("Producto", opciones,
						x -> x + " | " + dinero(x.getPrecio()) + " | quedan "
								+ (inventario().getCantidad(x, ubicacion) - items.getOrDefault(x, 0)));
				if (p == null) {
					break;
				}
				int cantidad = in.entero("Cantidad");
				int yaEnCarrito = items.getOrDefault(p, 0);
				if (cantidad <= 0) {
					System.out.println("  La cantidad debe ser positiva.");
				} else if (yaEnCarrito + cantidad > inventario().getCantidad(p, ubicacion)) {
					System.out.println("  No hay existencias suficientes de " + p.getNombre() + ".");
				} else {
					items.put(p, yaEnCarrito + cantidad);
					System.out.println("  Agregado: " + cantidad + " x " + p.getNombre());
				}
				if (!in.confirmar("¿Agregar otro producto?")) {
					break;
				}
			}
			if (items.isEmpty()) {
				System.out.println("Compra cancelada: no se eligió ningún producto.");
				return;
			}
			System.out.println("Códigos de descuento: CLUB10, CLUB20"
					+ (comprador instanceof Empleado ? " o tu código personal de empleado" : "") + ".");
			String codigo = in.texto("Código de descuento (enter = ninguno)");
			if (codigo.isEmpty()) {
				codigo = null;
			}
			VentaProducto venta = enCafeteria ? cafeteria.vender(comprador, items, codigo)
					: tienda.vender(comprador, items, codigo);
			admin.registrarVenta(venta);
			imprimirRecibo(venta, items);
			ofrecerRedencion(comprador, venta);
		});
	}

	private void imprimirRecibo(VentaProducto v, Map<Producto, Integer> items) {
		System.out.println("\n========== RECIBO ==========");
		for (Map.Entry<Producto, Integer> e : items.entrySet()) {
			System.out.println(String.format("  %-28s x%-3d %12s", e.getKey().getNombre(), e.getValue(),
					dinero(e.getKey().getPrecio() * e.getValue())));
		}
		System.out.println("  Subtotal:  " + dinero(v.getSubtotal()));
		System.out.println("  Descuento: -" + dinero(v.getDescuento()));
		System.out.println("  IVA (19%): " + dinero(v.getImpuesto()));
		if (v.getPropina() > 0) {
			System.out.println("  Propina sugerida (10%): " + dinero(v.getPropina()));
		}
		System.out.println("  TOTAL:     " + dinero(v.getTotal()));
		System.out.println("  Puntos ganados: " + v.getPuntosGenerados());
		System.out.println("============================");
	}

	private void ofrecerRedencion(Usuario comprador, VentaProducto venta) {
		ProgramaFidelidad programa = ProgramaFidelidad.deUsuario(comprador);
		if (programa == null) {
			return;
		}
		System.out.println("Saldo de puntos: " + programa.getSaldo());
		if (programa.getSaldo() <= 0 || !in.confirmar("¿Quieres redimir puntos para pagar esta compra?")) {
			return;
		}
		int maximo = (int) Math.min(programa.getSaldo(), Math.ceil(venta.getTotal() / ProgramaFidelidad.VALOR_REDENCION));
		int puntos = in.entero("Puntos a redimir (máximo " + maximo + ")");
		if (puntos <= 0 || puntos > maximo) {
			System.out.println("  Cantidad de puntos no válida; no se redimió nada.");
			return;
		}
		double credito = Math.min(programa.redimir(puntos), venta.getTotal());
		System.out.println("[OK] Redimiste " + puntos + " puntos = " + dinero(credito) + ". Total a pagar: "
				+ dinero(venta.getTotal() - credito) + ". Puntos restantes: " + programa.getSaldo());
	}

	public void verPuntos(Usuario u) {
		ProgramaFidelidad programa = ProgramaFidelidad.deUsuario(u);
		System.out.println("\n--- Programa de fidelidad ---");
		if (programa == null) {
			System.out.println("Este usuario no participa en el programa de fidelidad.");
			return;
		}
		System.out.println("Saldo: " + programa.getSaldo() + " puntos (valen "
				+ dinero(programa.getSaldo() * ProgramaFidelidad.VALOR_REDENCION) + ").");
		System.out.println("Ganas 1 punto por cada " + dinero(ProgramaFidelidad.VALOR_POR_PUNTO)
				+ " pagados; cada punto redime " + dinero(ProgramaFidelidad.VALOR_REDENCION) + ".");
	}

	public void verMisCompras(Usuario u) {
		System.out.println("\n--- Mis compras ---");
		boolean hay = false;
		for (VentaProducto v : admin.getVentas()) {
			if (v.getComprador() == u) {
				hay = true;
				System.out.println(" - " + v.getFecha() + " | " + v.getRubro() + " | total " + dinero(v.getTotal())
						+ " | puntos " + v.getPuntosGenerados());
			}
		}
		if (!hay) {
			System.out.println("Aún no tienes compras registradas.");
		}
	}

	/** Un empleado atiende a un jugador (o a sí mismo). */
	public void venderComoEmpleado(Empleado empleado, boolean enCafeteria) {
		List<Usuario> clientes = new ArrayList<Usuario>();
		clientes.add(empleado);
		for (Jugador j : admin.getJugadores()) {
			if (j.estaActivo()) {
				clientes.add(j);
			}
		}
		Usuario cliente = in.elegir("¿A quién le vendes?", clientes,
				u -> u == empleado ? "Compra propia (" + empleado.getNombre() + ")"
						: "Jugador: " + u);
		if (cliente != null) {
			comprar(cliente, enCafeteria);
		}
	}

	// ------------------------------------------------------------------ sugerencias

	public void sugerirProducto(Usuario autor) {
		intentar(() -> {
			String nombre = in.textoObligatorio("Nombre del producto");
			String categoria = in.textoObligatorio("Categoría");
			double precio = in.decimal("Precio sugerido");
			SugerenciaProducto s = new SugerenciaProducto(autor, new Producto(nombre, categoria, precio));
			admin.recibirSugerencia(s);
			System.out.println("[OK] Sugerencia enviada al administrador.");
		});
	}

	// ------------------------------------------------------------------ administrador

	public void menuAdministrador(MenuBase menu) {
		while (true) {
			switch (menu.menu("Tienda, cafetería e inventario", "Ver existencias", "Agregar existencias",
					"Mover artículos entre ubicaciones", "Reabastecer tienda", "Sugerencias de productos",
					"Reporte de ventas", "Historial de ventas")) {
			case 1:
				verExistencias();
				break;
			case 2:
				agregarExistencias();
				break;
			case 3:
				moverArticulos();
				break;
			case 4:
				reabastecerTienda();
				break;
			case 5:
				resolverSugerencias();
				break;
			case 6:
				reporteVentas();
				break;
			case 7:
				historialVentas();
				break;
			default:
				return;
			}
		}
	}

	private void verExistencias() {
		System.out.println("\n--- Existencias por ubicación ---");
		List<Producto> lista = catalogo();
		if (lista.isEmpty()) {
			System.out.println("El catálogo está vacío.");
		}
		for (Producto p : lista) {
			System.out.println(String.format(" - %-34s %10s | tienda %d | cafetería %d | bodega %d", p,
					dinero(p.getPrecio()), inventario().getCantidad(p, TIENDA),
					inventario().getCantidad(p, CAFETERIA), inventario().getCantidad(p, BODEGA)));
		}
	}

	private String elegirUbicacion(String titulo) {
		List<String> ubicaciones = new ArrayList<String>();
		ubicaciones.add(TIENDA);
		ubicaciones.add(CAFETERIA);
		ubicaciones.add(BODEGA);
		return in.elegir(titulo, ubicaciones);
	}

	private void agregarExistencias() {
		intentar(() -> {
			Producto p;
			if (!catalogo().isEmpty() && !in.confirmar("¿Es un producto nuevo?")) {
				p = in.elegir("Producto", catalogo(), x -> x + " | " + dinero(x.getPrecio()));
			} else {
				p = new Producto(in.textoObligatorio("Nombre"), in.textoObligatorio("Categoría"),
						in.decimal("Precio"));
			}
			if (p == null) {
				return;
			}
			String ubicacion = elegirUbicacion("Ubicación");
			if (ubicacion == null) {
				return;
			}
			int cantidad = in.entero("Cantidad a agregar");
			inventario().agregar(p, ubicacion, cantidad);
			System.out.println("[OK] " + p + ": ahora hay " + inventario().getCantidad(p, ubicacion) + " en "
					+ ubicacion + ".");
		});
	}

	private void moverArticulos() {
		intentar(() -> {
			Producto p = in.elegir("Producto", catalogo());
			if (p == null) {
				return;
			}
			String origen = elegirUbicacion("Ubicación de origen");
			if (origen == null) {
				return;
			}
			String destino = elegirUbicacion("Ubicación de destino");
			if (destino == null) {
				return;
			}
			admin.moverArticulo(p, origen, destino, in.entero("Cantidad a mover"));
			System.out.println("[OK] Artículos movidos de " + origen + " a " + destino + ".");
		});
	}

	private void reabastecerTienda() {
		intentar(() -> {
			Producto p = in.elegir("Producto", catalogo());
			if (p == null) {
				return;
			}
			admin.reabastecerTienda(p, in.entero("Cantidad a agregar en la tienda"));
			System.out.println("[OK] Tienda reabastecida.");
		});
	}

	private void resolverSugerencias() {
		intentar(() -> {
			List<SugerenciaProducto> pendientes = admin.consultarSugerenciasPendientes();
			SugerenciaProducto s = in.elegir("Sugerencias pendientes", pendientes,
					x -> x + " | precio " + dinero(x.getProducto().getPrecio()));
			if (s == null) {
				return;
			}
			if (in.confirmar("¿Aprobar la sugerencia? (n = rechazar)")) {
				admin.aprobarSugerencia(s);
				System.out.println("[OK] Producto agregado al catálogo (sin existencias: agrégalas en el inventario).");
			} else {
				admin.rechazarSugerencia(s, in.textoObligatorio("Razón del rechazo"));
				System.out.println("[OK] Sugerencia rechazada.");
			}
		});
	}

	private void reporteVentas() {
		intentar(() -> {
			System.out.println("Tipo de reporte:\n  1. Diario\n  2. Semanal\n  3. Mensual");
			int tipo = in.entero("Opción");
			Map<Rubro, Double> reporte;
			if (tipo == 1) {
				reporte = admin.reporteVentasDiario(in.fechaOHoy("Día"));
			} else if (tipo == 2) {
				reporte = admin.reporteVentasSemanal(in.fecha("Primer día de la semana"));
			} else if (tipo == 3) {
				LocalDate f = in.fecha("Cualquier día del mes");
				reporte = admin.reporteVentasMensual(YearMonth.from(f));
			} else {
				System.out.println("Opción inválida.");
				return;
			}
			double total = 0;
			System.out.println("\n--- Ventas por rubro ---");
			for (Map.Entry<Rubro, Double> e : reporte.entrySet()) {
				System.out.println(String.format("  %-14s %12s", e.getKey(), dinero(e.getValue())));
				total += e.getValue();
			}
			System.out.println(String.format("  %-14s %12s", "TOTAL", dinero(total)));
		});
	}

	private void historialVentas() {
		System.out.println("\n--- Ventas realizadas ---");
		List<VentaProducto> ventas = admin.getVentas();
		if (ventas.isEmpty()) {
			System.out.println("Aún no hay ventas.");
		}
		for (VentaProducto v : ventas) {
			System.out.println(" - " + v.getFecha() + " | " + v.getRubro() + " | " + v.getComprador().getUsername()
					+ " | total " + dinero(v.getTotal()));
		}
	}
}
