package club.logica.tests;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.enums.Nivel;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;
import club.modelo.usuarios.RankingELO;

public class PruebaUsuarios {
    public static void main(String[] args) {
        DeporteEquipo futbol = new DeporteEquipo("Futbol", 7);
        DeporteEquipo baloncesto = new DeporteEquipo("Baloncesto", 5);
        Equipo leones = new Equipo("Leones", Categoria.SUB15, futbol, 2);
        Equipo leones2 = new Equipo("Leones 2", Categoria.SUB15, futbol, 5);
        Equipo celtics = new Equipo("Celtics", Categoria.SUB15, baloncesto, 5);

        JugadorEquipo ana = new JugadorEquipo("ana", "1", "Ana", 14);
        JugadorEquipo luis = new JugadorEquipo("luis", "2", "Luis", 16);
        JugadorEquipo pedro = new JugadorEquipo("pedro", "3", "Pedro", 15);
        JugadorEquipo sofia = new JugadorEquipo("sofia", "4", "Sofia", 14);

        System.out.println("Ana en Leones: " + ana.inscribirseEquipo(leones, "Delantera", 9));
        System.out.println("Luis (SUB17) en Leones: " + luis.inscribirseEquipo(leones, "Defensa", 4));
        System.out.println("Pedro con camiseta repetida: " + pedro.inscribirseEquipo(leones, "Portero", 9));
        System.out.println("Pedro con camiseta 1: " + pedro.inscribirseEquipo(leones, "Portero", 1));
        System.out.println("Sofia con cupo lleno: " + sofia.inscribirseEquipo(leones, "Medio", 5));
        System.out.println("Ana en segundo equipo de futbol: " + ana.inscribirseEquipo(leones2, "Medio", 8));
        System.out.println("Ana en baloncesto: " + ana.inscribirseEquipo(celtics, "Alero", 7));
        System.out.println("Posicion de Ana en Leones: " + ana.getPosicion(leones));
        System.out.println("Camiseta de Ana en Leones: " + ana.getNumeroCamiseta(leones));
        System.out.println("Leones hay cupo: " + leones.hayCupo());

        System.out.println("Habilitados antes de la lesion: " + leones.jugadoresHabilitados().size());
        ana.getHistorialMedico().registrarLesion("Esguince");
        System.out.println("Ana habilitada tras lesion: " + ana.estaHabilitado());
        System.out.println("Habilitados despues: " + leones.jugadoresHabilitados().size());
        System.out.println("Leones cumple minimo: " + leones.cumpleMinimoParaJugar());

        DeporteIndividual tenis = new DeporteIndividual("Tenis", 3);
        JugadorIndividual marta = new JugadorIndividual("marta", "5", "Marta", 20, false);
        JugadorIndividual juan = new JugadorIndividual("juan", "6", "Juan", 22, false);
        JugadorIndividual coach = new JugadorIndividual("coach", "7", "Coach", 35, true);
        JugadorIndividual leo = new JugadorIndividual("leo", "8", "Leo", 25, false);
        marta.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
        juan.inscribirseDeporte(tenis, Nivel.INTERMEDIO);
        coach.inscribirseDeporte(tenis, Nivel.AVANZADO);
        leo.inscribirseDeporte(tenis, Nivel.AVANZADO);

        System.out.println("Marta vs Juan: " + marta.esRivalValido(juan, tenis));
        System.out.println("Marta vs Coach: " + marta.esRivalValido(coach, tenis));
        System.out.println("Marta vs Leo: " + marta.esRivalValido(leo, tenis));
        System.out.println("Marta inscrita otra vez en tenis: " + marta.inscribirseDeporte(tenis, Nivel.AVANZADO));

        RankingELO rm = marta.getRanking(tenis);
        RankingELO rj = juan.getRanking(tenis);
        System.out.println("Marta tiene ranking: " + rm.tieneRanking());

        rm.actualizar(rm, true);
        rj.actualizar(rj, false);
        System.out.println("Marta: " + rm.getPuntaje());
        System.out.println("Juan: " + rj.getPuntaje());
        System.out.println("Marta tiene ranking: " + rm.tieneRanking());
    }
}