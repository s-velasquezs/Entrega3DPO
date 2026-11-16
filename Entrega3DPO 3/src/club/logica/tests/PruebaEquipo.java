package club.logica.tests;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;

public class PruebaEquipo {
    public static void main(String[] args) {
        DeporteEquipo futbol = new DeporteEquipo("Futbol", 7);
        Equipo leones = new Equipo("Leones", Categoria.SUB15, futbol, 18);

        System.out.println(leones);
        System.out.println("Activo: " + leones.estaActivo());
        System.out.println("Registrado: " + leones.getRegistro().estaRegistrado());
        System.out.println("Cupo maximo: " + leones.getCupoMaximo());

        leones.darDeBaja();
        System.out.println("Despues de la baja, activo: " + leones.estaActivo());
        System.out.println("Despues de la baja, registrado: " + leones.getRegistro().estaRegistrado());
    }
}