package club.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.entrenamientos.RegistroEntrenamiento;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoAsistencia;
import club.modelo.estadisticas.Estadisticas;
import club.modelo.estadisticas.EstadisticasEntrenamiento;
import club.modelo.estadisticas.EstadisticasEquipo;
import club.modelo.estadisticas.EstadisticasJugador;
import club.modelo.estadisticas.EstadisticasPartido;
import club.modelo.instalaciones.Instalacion;
import club.modelo.partidos.Partido;
import club.modelo.personal.Entrenador;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;

// Pruebas del paquete estadisticas
public class TestEstadisticas {

    // Estadisticas es abstracta, entonces usamos esta clase simple para probarla
    private static class EstadisticasSimple extends Estadisticas {
    }

    private DeporteEquipo futbol;
    private Equipo leones;
    private JugadorEquipo ana;
    private JugadorEquipo luis;
    private Entrenamiento entrenamiento;
    private Partido partido;

    @BeforeEach
    public void preparar() {
        futbol = new DeporteEquipo("Futbol", 2);
        leones = new Equipo("Leones", Categoria.SUB15, futbol, 10);
        ana = new JugadorEquipo("ana", "1", "Ana", 14);
        luis = new JugadorEquipo("luis", "2", "Luis", 15);
        ana.inscribirseEquipo(leones, "Delantera", 9);
        luis.inscribirseEquipo(leones, "Defensa", 4);

        Instalacion cancha = new Instalacion("F1", "Cancha futbol", 22, futbol, false);
        Entrenador coach = new Entrenador("coach", "1", "Carlos");
        entrenamiento = new Entrenamiento(LocalDate.of(2026, 12, 1), LocalTime.of(16, 0), 90, futbol, coach,
                cancha, Categoria.SUB15, leones);
        entrenamiento.convocar(ana);
        entrenamiento.convocar(luis);

        partido = new Partido(LocalDate.of(2026, 12, 1), Categoria.SUB15, futbol) {
        };
    }

    // ---------- Estadisticas (clase base) ----------

    @Test
    public void registrarYConsultarValores() {
        Estadisticas e = new EstadisticasSimple();
        e.registrar("goles", 3);
        assertEquals(3, e.get("goles"));
        assertEquals(3, e.getRazon("goles"));
    }

    @Test
    public void razonQueNoExisteVale0() {
        Estadisticas e = new EstadisticasSimple();
        assertEquals(0, e.getRazon("aces"));
    }

    @Test
    public void registrarConRazonInvalidaLanzaExcepcion() {
        Estadisticas e = new EstadisticasSimple();
        assertThrows(IllegalArgumentException.class, () -> e.registrar("", 1));
        assertThrows(IllegalArgumentException.class, () -> e.registrar(null, 1));
    }

    @Test
    public void registrarValorNegativoLanzaExcepcion() {
        Estadisticas e = new EstadisticasSimple();
        assertThrows(IllegalArgumentException.class, () -> e.registrar("goles", -1));
    }

    @Test
    public void getRazonNulaLanzaExcepcion() {
        Estadisticas e = new EstadisticasSimple();
        assertThrows(IllegalArgumentException.class, () -> e.getRazon(null));
    }

    @Test
    public void getValoresDevuelveUnaCopia() {
        Estadisticas e = new EstadisticasSimple();
        e.registrar("goles", 3);
        Map<String, Integer> copia = e.getValores();
        copia.put("goles", 100);
        assertEquals(3, e.get("goles"));
    }

    @Test
    public void sumarEstadisticas() {
        Estadisticas a = new EstadisticasSimple();
        Estadisticas b = new EstadisticasSimple();
        a.registrar("goles", 2);
        b.registrar("goles", 3);
        b.registrar("asistencias", 1);

        a.sumar(b);

        assertEquals(5, a.get("goles"));
        assertEquals(1, a.get("asistencias"));
        assertEquals(3, b.get("goles")); // b no cambia
    }

    @Test
    public void sumarEstadisticaNulaLanzaExcepcion() {
        Estadisticas a = new EstadisticasSimple();
        assertThrows(IllegalArgumentException.class, () -> a.sumar(null));
    }

    // ---------- EstadisticasJugador ----------

    @Test
    public void unaAmarillaNoSuspende() {
        EstadisticasJugador ej = new EstadisticasJugador();
        Estadisticas p = new EstadisticasSimple();
        p.registrar("amarillas", 1);
        ej.acumular(p);
        assertFalse(ej.tieneSancionActiva());
    }

    @Test
    public void dosAmarillasSuspendenUnPartido() {
        EstadisticasJugador ej = new EstadisticasJugador();
        Estadisticas p1 = new EstadisticasSimple();
        Estadisticas p2 = new EstadisticasSimple();
        p1.registrar("amarillas", 1);
        p2.registrar("amarillas", 1);
        ej.acumular(p1);
        ej.acumular(p2);

        assertTrue(ej.tieneSancionActiva());
        assertEquals(1, ej.getPartidosSuspension());
        assertEquals(2, ej.get("amarillas"));
    }

    @Test
    public void rojaDirectaSuspende() {
        EstadisticasJugador ej = new EstadisticasJugador();
        Estadisticas p = new EstadisticasSimple();
        p.registrar("rojas", 1);
        ej.acumular(p);

        assertTrue(ej.tieneSancionActiva());
        assertEquals(1, ej.getSanciones().size());
    }

    @Test
    public void consumirSuspension() {
        EstadisticasJugador ej = new EstadisticasJugador();
        ej.aplicarSancion("Pelea", 2);
        ej.consumirSuspension();
        assertTrue(ej.tieneSancionActiva());
        ej.consumirSuspension();
        assertFalse(ej.tieneSancionActiva());
    }

    @Test
    public void consumirSuspensionSinSuspensionLanzaExcepcion() {
        EstadisticasJugador ej = new EstadisticasJugador();
        assertThrows(IllegalStateException.class, () -> ej.consumirSuspension());
    }

    @Test
    public void aplicarSancionConDatosInvalidosLanzaExcepcion() {
        EstadisticasJugador ej = new EstadisticasJugador();
        assertThrows(IllegalArgumentException.class, () -> ej.aplicarSancion("", 1));
        assertThrows(IllegalArgumentException.class, () -> ej.aplicarSancion(null, 1));
        assertThrows(IllegalArgumentException.class, () -> ej.aplicarSancion("Pelea", 0));
        assertThrows(IllegalArgumentException.class, () -> ej.aplicarSancion("Pelea", -2));
    }

    @Test
    public void acumularNuloLanzaExcepcion() {
        EstadisticasJugador ej = new EstadisticasJugador();
        assertThrows(IllegalArgumentException.class, () -> ej.acumular(null));
    }

    @Test
    public void acumularEstadisticasDeUnPartido() {
        EstadisticasPartido ep = new EstadisticasPartido(partido, ana, 90);
        ep.registrar("goles", 2);
        ana.consultarEstadisticas().acumular(ep);
        assertEquals(2, ana.consultarEstadisticas().get("goles"));
    }

    @Test
    public void getSancionesDevuelveUnaCopia() {
        EstadisticasJugador ej = new EstadisticasJugador();
        ej.aplicarSancion("Pelea", 1);
        ej.getSanciones().clear();
        assertEquals(1, ej.getSanciones().size());
    }

    @Test
    public void acumularRegistroDeEntrenamientoEvaluado() {
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.evaluar(ana, 4, "Buen trabajo");

        EstadisticasJugador ej = new EstadisticasJugador();
        ej.acumularReg(entrenamiento.getRegistroDe(ana));

        assertEquals(1, ej.get("asistencias"));
        assertEquals(1, ej.get("entrenamientos_evaluados"));
        assertEquals(4, ej.get("nota_total"));
    }

    @Test
    public void acumularRegistroDeJugadorQueFalto() {
        entrenamiento.registrarAsistencia(luis, EstadoAsistencia.FALTO);

        EstadisticasJugador ej = new EstadisticasJugador();
        ej.acumularReg(entrenamiento.getRegistroDe(luis));

        assertEquals(1, ej.get("faltas"));
        assertEquals(0, ej.get("entrenamientos_evaluados"));
    }

    @Test
    public void acumularRegistroDeJugadorQueLlegoTarde() {
        entrenamiento.registrarAsistencia(luis, EstadoAsistencia.TARDE);

        EstadisticasJugador ej = new EstadisticasJugador();
        ej.acumularReg(entrenamiento.getRegistroDe(luis));

        assertEquals(1, ej.get("tardanzas"));
    }

    @Test
    public void acumularRegistroSinAsistenciaLanzaExcepcion() {
        RegistroEntrenamiento sinAsistencia = new RegistroEntrenamiento(entrenamiento, ana);
        EstadisticasJugador ej = new EstadisticasJugador();
        assertThrows(IllegalStateException.class, () -> ej.acumularReg(sinAsistencia));
    }

    @Test
    public void acumularRegistroNuloLanzaExcepcion() {
        EstadisticasJugador ej = new EstadisticasJugador();
        assertThrows(IllegalArgumentException.class, () -> ej.acumularReg(null));
    }

    // ---------- EstadisticasPartido ----------

    @Test
    public void estadisticasPartidoValidas() {
        EstadisticasPartido ep = new EstadisticasPartido(partido, ana, 75);
        assertEquals(partido, ep.getPartido());
        assertEquals(ana, ep.getJugador());
        assertEquals(75, ep.getMinutos());
    }

    @Test
    public void estadisticasPartidoConDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new EstadisticasPartido(null, ana, 90));
        assertThrows(IllegalArgumentException.class, () -> new EstadisticasPartido(partido, null, 90));
        assertThrows(IllegalArgumentException.class, () -> new EstadisticasPartido(partido, ana, -1));
    }

    // ---------- EstadisticasEquipo ----------

    @Test
    public void estadisticasEquipoSumaLosJugadoresDelEquipo() {
        EstadisticasEquipo ee = new EstadisticasEquipo(leones);
        EstadisticasPartido deAna = new EstadisticasPartido(partido, ana, 90);
        EstadisticasPartido deLuis = new EstadisticasPartido(partido, luis, 90);
        deAna.registrar("goles", 2);
        deLuis.registrar("goles", 1);

        ee.acumular(deAna);
        ee.acumular(deLuis);

        assertEquals(3, ee.get("goles"));
        assertEquals(leones, ee.getEquipo());
    }

    @Test
    public void estadisticasEquipoConJugadorDeOtroEquipoLanzaExcepcion() {
        EstadisticasEquipo ee = new EstadisticasEquipo(leones);
        JugadorEquipo sofia = new JugadorEquipo("sofia", "3", "Sofia", 14); // no esta en Leones
        EstadisticasPartido deSofia = new EstadisticasPartido(partido, sofia, 90);
        assertThrows(IllegalArgumentException.class, () -> ee.acumular(deSofia));
    }

    @Test
    public void estadisticasEquipoConJugadorIndividualLanzaExcepcion() {
        EstadisticasEquipo ee = new EstadisticasEquipo(leones);
        JugadorIndividual marta = new JugadorIndividual("marta", "4", "Marta", 20, false);
        EstadisticasPartido deMarta = new EstadisticasPartido(partido, marta, 90);
        assertThrows(IllegalArgumentException.class, () -> ee.acumular(deMarta));
    }

    @Test
    public void estadisticasEquipoConValoresNulosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new EstadisticasEquipo(null));
        EstadisticasEquipo ee = new EstadisticasEquipo(leones);
        assertThrows(IllegalArgumentException.class, () -> ee.acumular(null));
    }

    // ---------- EstadisticasEntrenamiento ----------

    @Test
    public void promedioDeNotaDeUnJugador() {
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.evaluar(ana, 4, "Bien");

        EstadisticasEntrenamiento ee = new EstadisticasEntrenamiento(entrenamiento, ana);
        assertEquals(4.0, ee.promedioNota(), 0.001);
    }

    @Test
    public void promedioDeNotaDeUnEquipo() {
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.registrarAsistencia(luis, EstadoAsistencia.TARDE);
        entrenamiento.evaluar(ana, 4, "Bien");
        entrenamiento.evaluar(luis, 2, "Llego tarde");

        EstadisticasEntrenamiento ee = new EstadisticasEntrenamiento(entrenamiento, leones);
        assertEquals(3.0, ee.promedioNota(), 0.001);
    }

    @Test
    public void promedioSinEvaluacionesLanzaExcepcion() {
        EstadisticasEntrenamiento ee = new EstadisticasEntrenamiento(entrenamiento, leones);
        assertThrows(IllegalStateException.class, () -> ee.promedioNota());
    }

    @Test
    public void promedioDeJugadorSinEvaluarLanzaExcepcion() {
        entrenamiento.registrarAsistencia(ana, EstadoAsistencia.ASISTIO);
        entrenamiento.evaluar(ana, 5, "Excelente");
        // luis no fue evaluado
        EstadisticasEntrenamiento ee = new EstadisticasEntrenamiento(entrenamiento, luis);
        assertThrows(IllegalStateException.class, () -> ee.promedioNota());
    }

    @Test
    public void estadisticasEntrenamientoConValoresNulosLanzaExcepcion() {
        Equipo equipoNulo = null;
        JugadorEquipo jugadorNulo = null;
        assertThrows(IllegalArgumentException.class, () -> new EstadisticasEntrenamiento(null, ana));
        assertThrows(IllegalArgumentException.class, () -> new EstadisticasEntrenamiento(null, leones));
        assertThrows(IllegalArgumentException.class, () -> new EstadisticasEntrenamiento(entrenamiento, jugadorNulo));
        assertThrows(IllegalArgumentException.class, () -> new EstadisticasEntrenamiento(entrenamiento, equipoNulo));
    }
}
