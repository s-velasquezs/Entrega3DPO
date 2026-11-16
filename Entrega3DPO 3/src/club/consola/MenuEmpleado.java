package club.consola;

import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.personal.Empleado;
import club.modelo.usuarios.Administrador;

public class MenuEmpleado extends MenuEmpleadoBase {

	public MenuEmpleado(Entrada in, Administrador admin, List<Deporte> deportes, Empleado empleado) {
		super(in, admin, deportes, empleado);
	}

	@Override
	public void iniciar() {
		while (true) {
			switch (menu("Empleado de tienda y cafetería: " + empleado.getNombre(), "Mis turnos",
					"Solicitar cambio de turno", "Mis solicitudes", "Ver mi código de descuento",
					"Vender en la tienda", "Vender en la cafetería", "Ver productos de la tienda",
					"Ver productos de la cafetería", "Sugerir un producto nuevo", "Mis puntos de fidelidad",
					"Mis compras")) {
			case 1:
				verTurnos();
				break;
			case 2:
				solicitarCambio();
				break;
			case 3:
				verSolicitudes();
				break;
			case 4:
				System.out.println("Tu código de descuento: " + empleado.getCodigoDescuento()
						+ " (20% de descuento en tus compras; también valen CLUB10 y CLUB20).");
				break;
			case 5:
				comercio.venderComoEmpleado(empleado, false);
				break;
			case 6:
				comercio.venderComoEmpleado(empleado, true);
				break;
			case 7:
				comercio.verCatalogo(ModuloComercio.TIENDA);
				break;
			case 8:
				comercio.verCatalogo(ModuloComercio.CAFETERIA);
				break;
			case 9:
				comercio.sugerirProducto(empleado);
				break;
			case 10:
				comercio.verPuntos(empleado);
				break;
			case 11:
				comercio.verMisCompras(empleado);
				break;
			default:
				return;
			}
		}
	}
}