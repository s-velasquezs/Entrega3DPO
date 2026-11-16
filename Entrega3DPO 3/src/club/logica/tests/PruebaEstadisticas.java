package club.logica.tests;

import club.modelo.estadisticas.Estadisticas;
import club.modelo.estadisticas.EstadisticasJugador;
import club.modelo.salud.HistorialMedico;

public class PruebaEstadisticas {
    public static void main(String[] args) {
    	
        EstadisticasJugador ej = new EstadisticasJugador();

        Estadisticas p1 = new Estadisticas() { };
        
        
        p1.registrar("goles", 2);
        p1.registrar("amarillas", 1);
        ej.acumular(p1);
        System.out.println("Sancion activa tras P1: " + ej.tieneSancionActiva());

        Estadisticas p2 = new Estadisticas() { };
        p2.registrar("amarillas", 1);
        ej.acumular(p2);
        System.out.println("Sancion activa tras P2: " + ej.tieneSancionActiva());
        System.out.println("Partidos de suspension: " + ej.getPartidosSuspension());

        ej.consumirSuspension();
        System.out.println("Sancion activa tras cumplirla: " + ej.tieneSancionActiva());

        Estadisticas p3 = new Estadisticas() { };
        p3.registrar("rojas", 1);
        ej.acumular(p3);
        System.out.println("Sancion activa tras roja: " + ej.tieneSancionActiva());

        System.out.println("Sanciones: " + ej.getSanciones());
        System.out.println("Goles: " + ej.get("goles"));
        System.out.println("Amarillas: " + ej.get("amarillas"));
        System.out.println("Rojas: " + ej.get("rojas"));
        System.out.println("Aces (no aplica): " + ej.get("aces"));

        HistorialMedico h = new HistorialMedico();
        System.out.println("Alta sin lesion: " + h.darAlta("x"));
        h.registrarLesion("Esguince");
        System.out.println("Lesion activa: " + h.tieneLesionActiva());
        h.darAlta("Recuperado");
        System.out.println("Observaciones: " + h.getObservaciones());
    }
}
