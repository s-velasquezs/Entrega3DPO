package club.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.entrenamientos.RegistroEntrenamiento;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoAsistencia;
import club.modelo.instalaciones.Instalacion;
import club.modelo.personal.Entrenador;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;

// Pruebas del paquete entrenamientos: Entrenamiento y RegistroEntrenamiento
public class TestEntrenamientos {

    private DeporteEquipo futbol;
    private Instalacion cancha;
    private Entrenador coach;
    private Equipo leones;
    private JugadorEquipo ana;
    private JugadorEquipo luis;
    private LocalDate dia;
    private Entrenamiento entrenamiento; // 16:00 a 17:30

    @BeforeEach
    public void preparar() {
        futbol = new DeporteEquipo("Futbol", 2);
        cancha = new Instalacion("F1", "Cancha futbol", 22, futbol, false);
        coach = new Entrenador("coach", "1", "Carlos");
        leones = new Equipo("Leones", Categoria.SUB15, futbol, 10);
        ana = new JugadorEquipo("ana", "1", "Ana", 14);
        luis = new JugadorEquipo("luis", "2", "Luis", 15);
        ana.inscribirseEquipo(leones, "Delantera", 9);
        luis.inscribirseEquipo(leones, "Defensa", 4);
        dia = LocalDate.of(2026, 12, 1);
        entrenamiento = new Entrenamiento(dia, LocalTime.of(16, 0), 90, futbol, coach, cancha,
                Categoria.SUB15, leones);
    }

    // ---------- Crear entrenamiento ----------

    @Test
    public void entrenamientoValido() {
        assertEquals(dia, entrenamiento.getFecha());
        assertEquals(LocalTime.of(16, 0), entrenamiento.getHora());
        assertEquals(90, entrenamiento.getDuracion());
        assertEquals(LocalTime.of(17, 30), entrenamiento.getHoraFin());
        assertSame(futbol, entrenamiento.getDeporte());
        assertSame(coach, entrenamiento.getResponsable());
        assertSame(cancha, entrenamiento.getInstalacion());
        assertSame(leones, entrenamiento.getEquipo());
        assertEquals(Categoria.SUB15, entrenamiento.getCategoria());
        assertTrue(entrenamiento.getRegistro().estaRegistrado());
        assertTrue(entrenamiento.getConvocados().isEmpty());
    }

    @Test
    public void entrenamientoSinEquipoEsValido() {
        Entrenamiento libre = new Entrenamiento(dia, LocalTime.of(9, 0), 60, futbol, coach, cancha,
                Categoria.MAYORES, null);
        assertNull(libre.getEquipo());
    }

    @Test
    public void entrenamientoConValoresNulosLanzaExcepcion() {
        LocalTime hora = LocalTime.of(16, 0);
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(null, hora, 90, futbol, coach, cancha, Categoria.SUB15, leones));
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, null, 90, futbol, coach, cancha, Categoria.SUB15, leones));
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, hora, 90, null, coach, cancha, Categoria.SUB15, leones));
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, hora, 90, futbol, null, cancha, Categoria.SUB15, leones));
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, hora, 90, futbol, coach, null, Categoria.SUB15, leones));
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, hora, 90, futbol, coach, cancha, null, leones));
    }

    @Test
    public void entrenamientoConDuracionInvalidaLanzaExcepcion() {
        LocalTime hora = LocalTime.of(16, 0);
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, hora, 0, futbol, coach, cancha, Categoria.SUB15, leones));
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, hora, -30, futbol, coach, cancha, Categoria.SUB15, leones));
    }

    @Test
    public void entrenamientoQuePasaDeMedianocheLanzaExcepcion() {
        LocalTime tarde = LocalTime.of(23, 0);
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, tarde, 120, futbol, coach, cancha, Categoria.SUB15, leones));
    }

    @Test
    public void entrenamientoEnInstalacionFueraDeServicioLanzaExcepcion() {
        cancha.fueraDeServ();
        assertThrows(IllegalStateException.class,
                () -> new Entrenamiento(dia, LocalTime.of(9, 0), 60, futbol, coach, cancha, Categoria.SUB15, leones));
    }

    @Test
    public void entrenamientoEnInstalacionDeOtroDeporteLanzaExcepcion() {
        DeporteEquipo baloncesto = new DeporteEquipo("Baloncesto", 5);
        Instalacion coliseo = new Instalacion("B1", "Coliseo", 20, baloncesto, true);
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, LocalTime.of(9, 0), 60, futbol, coach, coliseo, Categoria.SUB15, leones));
    }

    @Test
    public void entrenamientoConEquipoDeOtroDeporteLanzaExcepcion() {
        DeporteEquipo baloncesto = new DeporteEquipo("Baloncesto", 5);
        Equipo celtics = new Equipo("Celtics", Categoria.SUB15, baloncesto, 10);
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, LocalTime.of(9, 0), 60, futbol, coach, cancha, Categoria.SUB15, celtics));
    }

    @Test
    public void entrenamientoConCategoriaDistintaALaDelEquipoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new Entrenamiento(dia, LocalTime.of(9, 0), 60, futbol, coach, cancha, Categoria.SUB17, leones));
    }

    @Test
    public void entrenamientoConEquipoDeBajaLanzaExcepcion() {
        leones.darDeBaja();
        assertThrows(IllegalStateException.class,
                () -> new Entrenamiento(dia, LocalTime.of(9, 0), 60, futbol, coach, cancha, Categoria.SUB15, leones));
    }

    // ---------- yaFinalizo ----------

    @Test
    public void yaFinalizo() {
        assertFalse(entrenamiento.yaFinalizo(LocalDateTime.of(2026, 12, 1, 16, 30)));
        assertFalse(entrenamiento.yaFinalizo(LocalDateTime.of(2026, 12, 1, 17, 29)));
        assertTrue(entrenamiento.yaFinalizo(LocalDateTime.of(2026, 12, 1, 17, 30)));
        assertTrue(entrenamiento.yaFinalizo(LocalDateTime.of(2026, 12, 2, 8, 0)));
    }

    @Test
    public void yaFinalizoConMomentoNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> entrenamiento.yaFinalizo(null));
    }

    // ---------- convocar ----------

    @Test
    public void convocarJugadoresDelEquipo() {
        entrenamiento.convocar(ana);
        entrenamiento.convocar(luis);
        assertEquals(2, entrenamiento.getConvocados().size());
    }

    @Test
    public void convocarJugadorNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> entrenamiento.convocar(null));
    }

    @Test
    public void convocarDosVecesAlMismoJugadorLanzaExcepcion() {
        entrenamiento.convocar(ana);
        assertThrows(IllegalStateException.class, () -> entrenamiento.convocar(ana));
    }

    @Test
    public void convocarJugadorLesionadoLanzaExcepcion() {
        ana.getHistorialMedico().registrarLesion("Esguince");
        assertThrows(IllegalStateException.class, () -> entrenamiento.convocar(ana));
    }

    @Test
    public void convocarJugadorSuspendidoLanzaExcepcion() {
        luis.consultarEstadisticas().aplicarSancion("Roja", 1);
        assertThrows(IllegalStateException.class, () -> entrenamiento.convocar(luis));
    }

    @Test
    public void convocarJugadorDeOtroEquipoLanzaExcepcion() {
        JugadorEquipo sofia = new JugadorEquipo("sofia", "3", "Sofia", 14); // sin equipo
        assertThrows(IllegalArgumentException.class, () -> entrenamiento.convocar(sofia));
    }

    @Test
    public void convocarJugadorIndividualEnEntrenamientoDeEquipoLanzaExcepcion() {
        JugadorIndividual marta = new JugadorIndividual("marta", "4", "Marta", 20, false);
        assertThrows(IllegalArgumentException.class, () -> entrenamiento.convocar(marta));
    }

    @Test
    public void convocarMasJugadoresQueLaCapacidadLanzaExcepcion() {
        Instalacion pequena = new Instalacion("F2", "Cancha pequena", 1, futbol, false);
        Entrenamiento chico = new Entrenamiento(dia, LocalTime.of(9, 0), 60, futbol, coach, pequena,
                Categoria.SUB15, leones);
        chico.convocar(ana);
        assertThrows(IllegalStateException.class, () -> chico.convocar(luis));
    }

    @Test
    public void getConvocadosDevuelveUnaCopia() {
        entrenamiento.convocar(ana);
        entrenamiento.getConvocados().clear();
        assertEquals(1, entrenamiento.getConvocados().size());
    }

    // ---------- asistencia y evaluacion ----------

    @Test
    public void registrarAsistenciaDeUnConvocado() {
        entrenamiento.convocar(ana);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);

        RegistroEntrenamiento r = entrenamiento.getRegistroDe(ana);
        assertEquals(EstadoAsistencia.ASISTIO, r.getAsistencia());
        assertFalse(r.fueEvaluado());
        assertEquals(1, entrenamiento.getRegistros().size());
    }

    @Test
    public void cambiarLaAsistenciaAntesDeEvaluar() {
        entrenamiento.convocar(ana);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.TARDE);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);

        assertEquals(EstadoAsistencia.ASISTIO, entrenamiento.getRegistroDe(ana).getAsistencia());
        assertEquals(1, entrenamiento.getRegistros().size()); // sigue siendo un solo registro
    }

    @Test
    public void registrarAsistenciaDeAlguienNoConvocadoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO));
    }

    @Test
    public void registrarAsistenciaConDatosNulosLanzaExcepcion() {
        entrenamiento.convocar(ana);
        assertThrows(IllegalArgumentException.class, () -> entrenamiento.registrarAsistencia(null, EstadoAsistencia.ASISTIO));
        assertThrows(IllegalArgumentException.class, () -> entrenamiento.registrarAsistencia(ana, null));
    }

    @Test
    public void evaluarJugador() {
        entrenamiento.convocar(ana);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.evaluar(ana, 5, "Excelente");

        RegistroEntrenamiento r = entrenamiento.getRegistroDe(ana);
        assertTrue(r.fueEvaluado());
        assertEquals(5, r.getNota());
        assertEquals("Excelente", r.getComentario());
    }

    @Test
    public void evaluarSinAsistenciaLanzaExcepcion() {
        entrenamiento.convocar(ana);
        assertThrows(IllegalStateException.class, () -> entrenamiento.evaluar(ana, 4, "Bien"));
    }

    @Test
    public void evaluarJugadorQueFaltoLanzaExcepcion() {
        entrenamiento.convocar(ana);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.FALTO);
        assertThrows(IllegalStateException.class, () -> entrenamiento.evaluar(ana, 4, "Bien"));
    }

    @Test
    public void evaluarConNotaFueraDeRangoLanzaExcepcion() {
        entrenamiento.convocar(ana);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        assertThrows(IllegalArgumentException.class, () -> entrenamiento.evaluar(ana, 0, "Mal"));
        assertThrows(IllegalArgumentException.class, () -> entrenamiento.evaluar(ana, 6, "Genial"));
    }

    @Test
    public void evaluarDosVecesLanzaExcepcion() {
        entrenamiento.convocar(ana);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.evaluar(ana, 4, "Bien");
        assertThrows(IllegalStateException.class, () -> entrenamiento.evaluar(ana, 5, "Mejor"));
    }

    @Test
    public void cambiarAsistenciaDespuesDeEvaluarLanzaExcepcion() {
        entrenamiento.convocar(ana);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.evaluar(ana, 4, "Bien");
        assertThrows(IllegalStateException.class,
                () -> entrenamiento.registrarAsistencia(ana, EstadoAsistencia.FALTO));
    }

    @Test
    public void evaluarConComentarioNuloGuardaComentarioVacio() {
        entrenamiento.convocar(ana);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.evaluar(ana, 3, null);
        assertEquals("", entrenamiento.getRegistroDe(ana).getComentario());
    }

    @Test
    public void registroEntrenamientoConDatosInvalidosLanzaExcepcion() {
        entrenamiento.convocar(ana);
        assertThrows(IllegalArgumentException.class, () -> new RegistroEntrenamiento(null, ana));
        assertThrows(IllegalArgumentException.class, () -> new RegistroEntrenamiento(entrenamiento, null));
        // luis no fue convocado
        assertThrows(IllegalArgumentException.class, () -> new RegistroEntrenamiento(entrenamiento, luis));
    }

    @Test
    public void registroEntrenamientoRegistrarAsistenciaNulaLanzaExcepcion() {
        entrenamiento.convocar(ana);
        RegistroEntrenamiento r = new RegistroEntrenamiento(entrenamiento, ana);
        assertThrows(IllegalArgumentException.class, () -> r.registrarAsistencia(null));
    }

    // ---------- acumular estadisticas ----------

    @Test
    public void acumularEstadisticasPasaLosRegistrosALosJugadores() {
        entrenamiento.convocar(ana);
        entrenamiento.convocar(luis);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.registrarAsistencia(luis, EstadoAsistencia.FALTO);
        entrenamiento.evaluar(ana, 4, "Bien");

        entrenamiento.acumularEstadisticas();

        assertEquals(1, ana.consultarEstadisticas().get("asistencias"));
        assertEquals(4, ana.consultarEstadisticas().get("nota_total"));
        assertEquals(1, luis.consultarEstadisticas().get("faltas"));
    }

    @Test
    public void acumularEstadisticasDosVecesLanzaExcepcion() {
        entrenamiento.convocar(ana);
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.acumularEstadisticas();
        assertThrows(IllegalStateException.class, () -> entrenamiento.acumularEstadisticas());
        assertEquals(1, ana.consultarEstadisticas().get("asistencias")); // no se sumo dos veces
    }

    // ---------- conflictos ----------

    private Entrenamiento otro(Instalacion instalacion, Entrenador responsable, Equipo equipo, int hora, int minutos) {
        return new Entrenamiento(dia, LocalTime.of(hora, minutos), 60, futbol, responsable, instalacion,
                Categoria.SUB15, equipo);
    }

    @Test
    public void conflictoPorMismaInstalacion() {
        Entrenador otroCoach = new Entrenador("coach2", "2", "Pedro");
        Equipo pumas = new Equipo("Pumas", Categoria.SUB15, futbol, 10);
        Entrenamiento e2 = otro(cancha, otroCoach, pumas, 17, 0);
        assertTrue(entrenamiento.estaEnConflicto(e2));
        assertTrue(e2.estaEnConflicto(entrenamiento));
    }

    @Test
    public void conflictoPorMismoEntrenador() {
        Instalacion cancha2 = new Instalacion("F2", "Cancha 2", 22, futbol, false);
        Equipo pumas = new Equipo("Pumas", Categoria.SUB15, futbol, 10);
        Entrenamiento e2 = otro(cancha2, coach, pumas, 17, 0);
        assertTrue(entrenamiento.estaEnConflicto(e2));
    }

    @Test
    public void conflictoPorMismoEquipo() {
        Instalacion cancha2 = new Instalacion("F2", "Cancha 2", 22, futbol, false);
        Entrenador otroCoach = new Entrenador("coach2", "2", "Pedro");
        Entrenamiento e2 = otro(cancha2, otroCoach, leones, 17, 0);
        assertTrue(entrenamiento.estaEnConflicto(e2));
    }

    @Test
    public void conflictoPorJugadorConvocadoEnAmbos() {
        Instalacion cancha2 = new Instalacion("F2", "Cancha 2", 22, futbol, false);
        Entrenador otroCoach = new Entrenador("coach2", "2", "Pedro");
        Entrenamiento libre = otro(cancha2, otroCoach, null, 17, 0);
        entrenamiento.convocar(ana);
        libre.convocar(ana);
        assertTrue(entrenamiento.estaEnConflicto(libre));
    }

    @Test
    public void sinConflictoSiNoComparteNada() {
        Instalacion cancha2 = new Instalacion("F2", "Cancha 2", 22, futbol, false);
        Entrenador otroCoach = new Entrenador("coach2", "2", "Pedro");
        Equipo pumas = new Equipo("Pumas", Categoria.SUB15, futbol, 10);
        Entrenamiento e2 = otro(cancha2, otroCoach, pumas, 17, 0);
        assertFalse(entrenamiento.estaEnConflicto(e2));
    }

    @Test
    public void sinConflictoSiUnoEmpiezaCuandoTerminaElOtro() {
        Entrenamiento seguido = otro(cancha, coach, leones, 17, 30);
        assertFalse(entrenamiento.estaEnConflicto(seguido));
    }

    @Test
    public void sinConflictoSiEsOtroDia() {
        Entrenamiento otroDia = new Entrenamiento(dia.plusDays(1), LocalTime.of(16, 0), 90, futbol, coach, cancha,
                Categoria.SUB15, leones);
        assertFalse(entrenamiento.estaEnConflicto(otroDia));
    }

    @Test
    public void conflictoConUnoMismoOConNuloNoSePermite() {
        assertFalse(entrenamiento.estaEnConflicto(entrenamiento));
        assertThrows(IllegalArgumentException.class, () -> entrenamiento.estaEnConflicto(null));
    }

    @Test
    public void seSolapaConHorario() {
        assertTrue(entrenamiento.seSolapaCon(dia, LocalTime.of(17, 0), LocalTime.of(18, 0)));
        assertFalse(entrenamiento.seSolapaCon(dia, LocalTime.of(17, 30), LocalTime.of(18, 0)));
        assertFalse(entrenamiento.seSolapaCon(dia.plusDays(1), LocalTime.of(16, 0), LocalTime.of(17, 0)));
    }

    // ---------- Instalacion + entrenamiento ----------

    @Test
    public void instalacionOcupadaPorEntrenamiento() {
        cancha.agregarEntrenamiento(entrenamiento);
        assertFalse(cancha.estaDisponible(dia, LocalTime.of(17, 0), LocalTime.of(18, 0)));
        assertTrue(cancha.estaDisponible(dia, LocalTime.of(17, 30), LocalTime.of(18, 30)));

        Entrenador otroCoach = new Entrenador("coach2", "2", "Pedro");
        Entrenamiento cruzado = otro(cancha, otroCoach, null, 17, 0);
        assertThrows(IllegalStateException.class, () -> cancha.agregarEntrenamiento(cruzado));
    }
}
