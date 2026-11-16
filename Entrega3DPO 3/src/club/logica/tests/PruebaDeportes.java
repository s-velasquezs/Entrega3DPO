package club.logica.tests;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;

public class PruebaDeportes {
    public static void main(String[] args) {
        DeporteEquipo futbol = new DeporteEquipo("Futbol", 7);
        DeporteEquipo baloncesto = new DeporteEquipo("Baloncesto", 5);
        DeporteEquipo voleibol = new DeporteEquipo("Voleibol", 6);
        DeporteIndividual tenis = new DeporteIndividual("Tenis", 3);
        DeporteIndividual padel = new DeporteIndividual("Padel", 3);
        DeporteIndividual tenisMesa = new DeporteIndividual("Tenis de mesa", 3);

        Deporte[] todos = { futbol, baloncesto, voleibol, tenis, padel, tenisMesa };

        for (Deporte d : todos) {
            if (d instanceof DeporteEquipo) {
                DeporteEquipo de = (DeporteEquipo) d;
                System.out.println(d + " (equipo) minimo: " + de.getMinimoJugadoresHabilitados());
            } else if (d instanceof DeporteIndividual) {
                DeporteIndividual di = (DeporteIndividual) d;
                System.out.println(d + " (individual) max instalaciones: " + di.getMaxInstalaciones());
            }
        }

        System.out.println("Mismo deporte (==): " + (futbol == futbol));
        System.out.println("Futbol vs baloncesto (==): " + (futbol == baloncesto));
    }
}