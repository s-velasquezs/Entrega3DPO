package club.logica.tests;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import club.modelo.personal.Empleado;
import club.modelo.personal.Entrenador;
import club.modelo.personal.Fisioterapeuta;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.personal.Turno;
import club.modelo.usuarios.JugadorEquipo;

public class PruebaPersonal {
    public static void main(String[] args) {
        LocalDate dia = LocalDate.of(2026, 10, 20);
        LocalTime ocho = LocalTime.of(8, 0);
        LocalTime doce = LocalTime.of(12, 0);
        LocalTime dos = LocalTime.of(14, 0);
        LocalTime seis = LocalTime.of(18, 0);

        Entrenador e1 = new Entrenador("e1", "1", "Entrenador 1");
        Entrenador e2 = new Entrenador("e2", "2", "Entrenador 2");
        Entrenador e3 = new Entrenador("e3", "3", "Entrenador 3");
        Fisioterapeuta f1 = new Fisioterapeuta("f1", "4", "Fisio 1");
        List<Empleado> personal = Arrays.asList(e1, e2, e3, f1);

        Turno t1 = new Turno(e1, dia, ocho, doce);
        Turno t2 = new Turno(e2, dia, ocho, doce);
        Turno t3 = new Turno(e3, dia, ocho, doce);
        Turno tf = new Turno(f1, dia, ocho, doce);
        e1.agregarTurno(t1);
        e2.agregarTurno(t2);
        e3.agregarTurno(t3);
        f1.agregarTurno(tf);

        System.out.println("Turnos de e1: " + e1.consultarTurnos(dia, dia));
        System.out.println("Sin turnos otro dia: " + e1.consultarTurnos(dia.plusDays(1), dia.plusDays(2)));
        System.out.println("Turno que se cruza: "
                + e1.agregarTurno(new Turno(e1, dia, LocalTime.of(11, 0), LocalTime.of(13, 0))));

        SolicitudCambioTurno s1 = e1.solicitarCambioTurno(t1, new Turno(e1, dia, dos, seis), "Clase", personal);
        System.out.println("Solicitud e1 pendiente: " + s1.getEstado());
        SolicitudCambioTurno s2 = e2.solicitarCambioTurno(t2, new Turno(e2, dia, dos, seis), "Cita", personal);
        System.out.println("Solicitud e2 pendiente: " + s2.getEstado());
        SolicitudCambioTurno s3 = f1.solicitarCambioTurno(tf, new Turno(f1, dia, dos, seis), "Examen", personal);
        System.out.println("Solicitud del unico fisio: " + s3);

        System.out.println("Aprobar solicitud e1: " + s1.aprobar(personal));
        System.out.println("Turno de e1 ahora: " + e1.consultarTurnos(dia, dia));
        System.out.println("Aprobar solicitud e2 (rompe el minimo): " + s2.aprobar(personal));
        System.out.println("Estado e2: " + s2.getEstado());
        s2.rechazar("Falta personal");
        System.out.println("Rechazar e2: ok");
        System.out.println("Estado e2: " + s2.getEstado() + " - " + s2.getRazonRechazo());

        JugadorEquipo ana = new JugadorEquipo("ana", "9", "Ana", 14);
        System.out.println("Lesion: " + f1.registrarLesion(ana, "Esguince"));
        System.out.println("Ana habilitada: " + ana.estaHabilitado());
        System.out.println("Alta: " + f1.darAlta(ana));
        System.out.println("Alta sin lesion: " + f1.darAlta(ana));
        System.out.println("Ana habilitada: " + ana.estaHabilitado());
        System.out.println("Historial: " + f1.consultarHistorial(ana).getObservaciones());
    }
}
