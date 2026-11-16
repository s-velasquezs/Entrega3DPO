package club.consola;

import java.util.ArrayList;
import java.util.List;

import club.modelo.comercio.Cafeteria;
import club.modelo.comercio.Inventario;
import club.modelo.comercio.Producto;
import club.modelo.comercio.Tienda;
import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.usuarios.Administrador;


public class AppConsola {

	public static void main(String[] args) {
		List<Deporte> deportes = new ArrayList<Deporte>();
		deportes.add(new DeporteEquipo("Fútbol", 7, true, 3, 1, 0));
		deportes.add(new DeporteEquipo("Baloncesto", 5, false, 2, 0, 0));
		deportes.add(new DeporteEquipo("Voleibol", 6, false, 2, 0, 0));
		deportes.add(new DeporteIndividual("Tenis", 4));
		deportes.add(new DeporteIndividual("Pádel", 3));
		deportes.add(new DeporteIndividual("Tenis de mesa", 3));

		// La tienda, la cafetería y el administrador comparten el mismo inventario
		Inventario inventario = new Inventario();
		cargarProductosIniciales(inventario);
		Tienda tienda = new Tienda(inventario);
		Cafeteria cafeteria = new Cafeteria(inventario);

		Administrador admin = new Administrador("admin", "admin123", inventario);
		new ConsolaClub(admin, deportes, tienda, cafeteria).iniciar();
	}

	private static void cargarProductosIniciales(Inventario inv) {
		inv.agregar(new Producto("Camiseta del club", "Ropa", 60000), "TIENDA", 20);
		inv.agregar(new Producto("Balón de fútbol", "Implementos", 85000), "TIENDA", 10);
		inv.agregar(new Producto("Raqueta de tenis", "Implementos", 240000), "TIENDA", 5);
		inv.agregar(new Producto("Gorra", "Ropa", 35000), "TIENDA", 15);
		inv.agregar(new Producto("Agua", "Bebidas", 3000), "CAFETERIA", 50);
		inv.agregar(new Producto("Jugo natural", "Bebidas", 7000), "CAFETERIA", 30);
		inv.agregar(new Producto("Sándwich", "Comida", 12000), "CAFETERIA", 25);
		inv.agregar(new Producto("Café", "Bebidas", 4500), "CAFETERIA", 40);
		inv.agregar(new Producto("Barra energética", "Comida", 5000), "BODEGA", 60);
	}
}
