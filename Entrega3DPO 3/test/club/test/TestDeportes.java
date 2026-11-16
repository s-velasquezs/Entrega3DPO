package club.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.deportes.Registro;
import club.modelo.enums.Categoria;
import club.modelo.usuarios.Inscripcion;
import club.modelo.usuarios.JugadorEquipo;

// Pruebas del paquete deportes: Registro, Deporte y Equipo
public class TestDeportes {

    private DeporteEquipo futbol;
    private Equipo leones;
    private JugadorEquipo ana;
    private JugadorEquipo sofia;

    @BeforeEach
    public void preparar() {
        futbol = new DeporteEquipo("Futbol", 2);
        leones = new Equipo("Leones", Categoria.SUB15, futbol, 2);
        ana = new JugadorEquipo("ana", "1", "Ana", 14);
        sofia = new JugadorEquipo("sofia", "2", "Sofia", 14);
    }

    // ---------- Registro ----------

    @Test
    public void registroNuevoEstaActivo() {
        Registro r = new Registro();
        assertTrue(r.estaRegistrado());
        assertEquals(LocalDate.now(), r.getFechaRegistro());
    }

    @Test
    public void registroConFechaPasada() {
        LocalDate fecha = LocalDate.of(2020, 5, 10);
        Registro r = new Registro(fecha);
        assertEquals(fecha, r.getFechaRegistro());
    }

    @Test
    public void registroConFechaNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Registro(null));
    }

    @Test
    public void registroConFechaFuturaLanzaExcepcion() {
        LocalDate manana = LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class, () -> new Registro(manana));
    }

    @Test
    public void darDeBajaRegistro() {
        Registro r = new Registro();
        r.darDeBaja();
        assertFalse(r.estaRegistrado());
    }

    @Test
    public void darDeBajaRegistroDosVecesLanzaExcepcion() {
        Registro r = new Registro();
        r.darDeBaja();
        assertThrows(IllegalStateException.class, () -> r.darDeBaja());
    }

    // ---------- Deportes ----------

    @Test
    public void deporteEquipoValido() {
        assertEquals("Futbol", futbol.getNombre());
        assertEquals(2, futbol.getMinimoJugadoresHabilitados());
        assertEquals(3, futbol.puntosPorVictoria());
        assertEquals(1, futbol.puntosPorEmpate());
    }

    @Test
    public void deporteConNombreVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new DeporteEquipo("", 5));
        assertThrows(IllegalArgumentException.class, () -> new DeporteIndividual("   ", 3));
    }

    @Test
    public void deporteConNombreNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new DeporteEquipo(null, 5));
    }

    @Test
    public void deporteEquipoConMinimoCeroLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new DeporteEquipo("Voleibol", 0));
    }

    @Test
    public void deporteIndividualConMaximoCeroLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new DeporteIndividual("Tenis", 0));
    }

    @Test
    public void deporteIndividualValido() {
        DeporteIndividual tenis = new DeporteIndividual("Tenis", 3);
        assertEquals(3, tenis.getMaxInstalaciones());
        assertEquals("Tenis", tenis.getDeporteIndividual());
    }

    // ---------- Equipo ----------

    @Test
    public void equipoValidoEmpiezaActivoYConCupo() {
        assertEquals("Leones", leones.getNombre());
        assertEquals(Categoria.SUB15, leones.getCategoria());
        assertTrue(leones.estaActivo());
        assertTrue(leones.getRegistro().estaRegistrado());
        assertTrue(leones.hayCupo());
    }

    @Test
    public void equipoConDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Equipo("", Categoria.SUB15, futbol, 10));
        assertThrows(IllegalArgumentException.class, () -> new Equipo(null, Categoria.SUB15, futbol, 10));
        assertThrows(IllegalArgumentException.class, () -> new Equipo("A", null, futbol, 10));
        assertThrows(IllegalArgumentException.class, () -> new Equipo("A", Categoria.SUB15, null, 10));
        assertThrows(IllegalArgumentException.class, () -> new Equipo("A", Categoria.SUB15, futbol, 0));
        assertThrows(IllegalArgumentException.class, () -> new Equipo("A", Categoria.SUB15, futbol, -3));
    }

    @Test
    public void agregarInscripcionValida() {
        Inscripcion i = new Inscripcion(ana, leones, "Delantera", 9);
        leones.agregarInscripcion(i);
        assertEquals(1, leones.getInscripciones().size());
        assertTrue(leones.tieneJugador(ana));
        assertFalse(leones.numeroDisponible(9));
    }

    @Test
    public void agregarInscripcionNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> leones.agregarInscripcion(null));
    }

    @Test
    public void agregarInscripcionDeOtroEquipoLanzaExcepcion() {
        Equipo otro = new Equipo("Tigres", Categoria.SUB15, futbol, 5);
        Inscripcion i = new Inscripcion(ana, otro, "Delantera", 9);
        assertThrows(IllegalArgumentException.class, () -> leones.agregarInscripcion(i));
    }

    @Test
    public void agregarInscripcionConEquipoDeBajaLanzaExcepcion() {
        leones.darDeBaja();
        Inscripcion i = new Inscripcion(ana, leones, "Delantera", 9);
        assertThrows(IllegalStateException.class, () -> leones.agregarInscripcion(i));
    }

    @Test
    public void agregarInscripcionConCupoLlenoLanzaExcepcion() {
        Equipo chico = new Equipo("Chico", Categoria.SUB15, futbol, 1);
        chico.agregarInscripcion(new Inscripcion(ana, chico, "Delantera", 1));
        Inscripcion i = new Inscripcion(sofia, chico, "Portera", 2);
        assertFalse(chico.hayCupo());
        assertThrows(IllegalStateException.class, () -> chico.agregarInscripcion(i));
    }

    @Test
    public void agregarInscripcionConNumeroRepetidoLanzaExcepcion() {
        leones.agregarInscripcion(new Inscripcion(ana, leones, "Delantera", 9));
        Inscripcion i = new Inscripcion(sofia, leones, "Portera", 9);
        assertThrows(IllegalStateException.class, () -> leones.agregarInscripcion(i));
    }

    @Test
    public void agregarMismoJugadorDosVecesLanzaExcepcion() {
        leones.agregarInscripcion(new Inscripcion(ana, leones, "Delantera", 9));
        Inscripcion repetida = new Inscripcion(ana, leones, "Defensa", 4);
        assertThrows(IllegalStateException.class, () -> leones.agregarInscripcion(repetida));
    }

    @Test
    public void inscripcionDesdeElJugadorSigueFuncionando() {
        assertTrue(ana.inscribirseEquipo(leones, "Delantera", 9));
        assertFalse(sofia.inscribirseEquipo(leones, "Portera", 9)); // numero repetido
        assertTrue(sofia.inscribirseEquipo(leones, "Portera", 1));
        assertFalse(leones.hayCupo());
    }

    @Test
    public void cumpleMinimoParaJugar() {
        assertFalse(leones.cumpleMinimoParaJugar());
        ana.inscribirseEquipo(leones, "Delantera", 9);
        sofia.inscribirseEquipo(leones, "Portera", 1);
        assertTrue(leones.cumpleMinimoParaJugar());

        ana.getHistorialMedico().registrarLesion("Esguince");
        assertEquals(1, leones.jugadoresHabilitados().size());
        assertFalse(leones.cumpleMinimoParaJugar());
    }

    @Test
    public void getInscripcionesDevuelveUnaCopia() {
        leones.agregarInscripcion(new Inscripcion(ana, leones, "Delantera", 9));
        leones.getInscripciones().clear();
        assertEquals(1, leones.getInscripciones().size());
    }

    @Test
    public void darDeBajaEquipo() {
        leones.darDeBaja();
        assertFalse(leones.estaActivo());
        assertFalse(leones.getRegistro().estaRegistrado());
    }

    @Test
    public void darDeBajaEquipoDosVecesLanzaExcepcion() {
        leones.darDeBaja();
        assertThrows(IllegalStateException.class, () -> leones.darDeBaja());
    }
}
