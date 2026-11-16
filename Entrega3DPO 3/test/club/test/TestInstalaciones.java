package club.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoInstalacion;
import club.modelo.enums.ModalidadJuego;
import club.modelo.enums.Nivel;
import club.modelo.instalaciones.Instalacion;
import club.modelo.instalaciones.ReservaCancha;
import club.modelo.personal.Entrenador;
import club.modelo.usuarios.JugadorIndividual;

// Pruebas del paquete instalaciones: Instalacion y ReservaCancha
public class TestInstalaciones {

    private DeporteIndividual tenis;
    private Instalacion cancha1;
    private Instalacion cancha2;
    private JugadorIndividual marta;
    private JugadorIndividual juan;
    private JugadorIndividual leo;
    private LocalDate dia;

    @BeforeEach
    public void preparar() {
        tenis = new DeporteIndividual("Tenis", 3);
        cancha1 = new Instalacion("C1", "Cancha 1", 4, tenis, false);
        cancha2 = new Instalacion("C2", "Cancha 2", 4, tenis, true);
        marta = new JugadorIndividual("marta", "1", "Marta", 20, false);
        juan = new JugadorIndividual("juan", "2", "Juan", 22, false);
        leo = new JugadorIndividual("leo", "3", "Leo", 25, false);
        marta.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
        juan.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
        leo.inscribirseDeporte(tenis, Nivel.AVANZADO);
        dia = LocalDate.of(2026, 12, 1);
    }

    private ReservaCancha reservaDe(Instalacion cancha, int horaIni, int horaFin) {
        return new ReservaCancha(marta, juan, cancha, dia, LocalTime.of(horaIni, 0), LocalTime.of(horaFin, 0),
                2, ModalidadJuego.SENCILLOS);
    }

    // ---------- Instalacion ----------

    @Test
    public void instalacionValida() {
        assertEquals("C1", cancha1.getId());
        assertEquals("Cancha 1", cancha1.getNombre());
        assertEquals(4, cancha1.getCapacidad());
        assertEquals(EstadoInstalacion.DISPONIBLE, cancha1.getEstado());
        assertFalse(cancha1.esTechada());
        assertTrue(cancha2.esTechada());
        assertTrue(cancha1.getReservas().isEmpty());
    }

    @Test
    public void instalacionConDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Instalacion("", "Cancha", 4, tenis, false));
        assertThrows(IllegalArgumentException.class, () -> new Instalacion(null, "Cancha", 4, tenis, false));
        assertThrows(IllegalArgumentException.class, () -> new Instalacion("C9", " ", 4, tenis, false));
        assertThrows(IllegalArgumentException.class, () -> new Instalacion("C9", "Cancha", 0, tenis, false));
        assertThrows(IllegalArgumentException.class, () -> new Instalacion("C9", "Cancha", 4, null, false));
    }

    @Test
    public void estaDisponibleCuandoNoHayNada() {
        assertTrue(cancha1.estaDisponible(dia, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    public void estaDisponibleConParametrosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> cancha1.estaDisponible(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> cancha1.estaDisponible(dia, null, LocalTime.of(11, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> cancha1.estaDisponible(dia, LocalTime.of(11, 0), LocalTime.of(10, 0)));
    }

    @Test
    public void fueraDeServicioNoEstaDisponible() {
        cancha1.fueraDeServ();
        assertEquals(EstadoInstalacion.FUERADESERVICIO, cancha1.getEstado());
        assertFalse(cancha1.estaDisponible(dia, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    public void fueraDeServicioDosVecesLanzaExcepcion() {
        cancha1.fueraDeServ();
        assertThrows(IllegalStateException.class, () -> cancha1.fueraDeServ());
    }

    @Test
    public void enServicioDespuesDeEstarFuera() {
        cancha1.fueraDeServ();
        cancha1.enServ();
        assertEquals(EstadoInstalacion.DISPONIBLE, cancha1.getEstado());
        assertTrue(cancha1.estaDisponible(dia, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    public void enServicioSiYaEstabaEnServicioLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () -> cancha1.enServ());
    }

    @Test
    public void admiteJugadoresSegunModalidad() {
        assertTrue(cancha1.admiteJugadores(2, ModalidadJuego.SENCILLOS));
        assertFalse(cancha1.admiteJugadores(4, ModalidadJuego.SENCILLOS));
        assertTrue(cancha1.admiteJugadores(4, ModalidadJuego.DOBLES));
        assertFalse(cancha1.admiteJugadores(2, ModalidadJuego.DOBLES));
    }

    @Test
    public void admiteJugadoresNoCabenEnLaInstalacion() {
        Instalacion pequena = new Instalacion("C3", "Pequena", 2, tenis, false);
        assertFalse(pequena.admiteJugadores(4, ModalidadJuego.DOBLES));
    }

    @Test
    public void admiteJugadoresConDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> cancha1.admiteJugadores(0, ModalidadJuego.SENCILLOS));
        assertThrows(IllegalArgumentException.class, () -> cancha1.admiteJugadores(2, null));
    }

    @Test
    public void agregarReservaHaceQueNoEsteDisponible() {
        ReservaCancha r = reservaDe(cancha1, 10, 11);
        cancha1.agregarReserva(r);

        assertEquals(1, cancha1.getReservas().size());
        assertFalse(cancha1.estaDisponible(dia, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        // una reserva que empieza justo cuando termina la otra no se cruza
        assertTrue(cancha1.estaDisponible(dia, LocalTime.of(11, 0), LocalTime.of(12, 0)));
        // otro dia si esta libre
        assertTrue(cancha1.estaDisponible(dia.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    public void agregarReservaNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> cancha1.agregarReserva(null));
    }

    @Test
    public void agregarReservaDeOtraInstalacionLanzaExcepcion() {
        ReservaCancha r = reservaDe(cancha2, 10, 11);
        assertThrows(IllegalArgumentException.class, () -> cancha1.agregarReserva(r));
    }

    @Test
    public void agregarReservaQueSeCruzaLanzaExcepcion() {
        cancha1.agregarReserva(reservaDe(cancha1, 10, 12));
        // el constructor ya revisa la disponibilidad
        assertThrows(IllegalStateException.class, () -> reservaDe(cancha1, 11, 13));
    }

    @Test
    public void reservaCanceladaLiberaLaInstalacion() {
        ReservaCancha r = reservaDe(cancha1, 10, 11);
        cancha1.agregarReserva(r);
        r.cancelar();
        assertFalse(r.estaActiva());
        assertTrue(cancha1.estaDisponible(dia, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    public void getReservasDevuelveUnaCopia() {
        cancha1.agregarReserva(reservaDe(cancha1, 10, 11));
        cancha1.getReservas().clear();
        assertEquals(1, cancha1.getReservas().size());
    }

    @Test
    public void entrenamientoYReservaNoPuedenCruzarse() {
        Entrenador coach = new Entrenador("coach", "1", "Carlos");
        Entrenamiento entrenamiento = new Entrenamiento(dia, LocalTime.of(10, 0), 60, tenis, coach,
                cancha1, Categoria.MAYORES, null);
        cancha1.agregarEntrenamiento(entrenamiento);

        assertEquals(1, cancha1.getEntrenamientos().size());
        assertThrows(IllegalStateException.class, () -> reservaDe(cancha1, 10, 11));
    }

    @Test
    public void agregarEntrenamientoDondeYaHayReservaLanzaExcepcion() {
        cancha1.agregarReserva(reservaDe(cancha1, 10, 11));
        Entrenador coach = new Entrenador("coach", "1", "Carlos");
        Entrenamiento entrenamiento = new Entrenamiento(dia, LocalTime.of(10, 30), 60, tenis, coach,
                cancha1, Categoria.MAYORES, null);
        assertThrows(IllegalStateException.class, () -> cancha1.agregarEntrenamiento(entrenamiento));
    }

    @Test
    public void agregarEntrenamientoInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> cancha1.agregarEntrenamiento(null));
        Entrenador coach = new Entrenador("coach", "1", "Carlos");
        Entrenamiento deOtraCancha = new Entrenamiento(dia, LocalTime.of(10, 0), 60, tenis, coach,
                cancha2, Categoria.MAYORES, null);
        assertThrows(IllegalArgumentException.class, () -> cancha1.agregarEntrenamiento(deOtraCancha));
    }

    // ---------- ReservaCancha ----------

    @Test
    public void reservaValida() {
        ReservaCancha r = reservaDe(cancha1, 10, 11);
        assertSame(marta, r.getJugador());
        assertSame(juan, r.getRival());
        assertSame(cancha1, r.getInstalacion());
        assertEquals(dia, r.getFecha());
        assertEquals(LocalTime.of(10, 0), r.getHoraInicio());
        assertEquals(LocalTime.of(11, 0), r.getHoraFin());
        assertEquals(2, r.getNumeroJugadores());
        assertEquals(ModalidadJuego.SENCILLOS, r.getModalidad());
        assertTrue(r.estaActiva());
    }

    @Test
    public void reservaConValoresNulosLanzaExcepcion() {
        LocalTime diez = LocalTime.of(10, 0);
        LocalTime once = LocalTime.of(11, 0);
        ModalidadJuego s = ModalidadJuego.SENCILLOS;
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(null, juan, cancha1, dia, diez, once, 2, s));
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, null, cancha1, dia, diez, once, 2, s));
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, juan, null, dia, diez, once, 2, s));
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, juan, cancha1, null, diez, once, 2, s));
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, juan, cancha1, dia, null, once, 2, s));
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, juan, cancha1, dia, diez, null, 2, s));
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, juan, cancha1, dia, diez, once, 2, null));
    }

    @Test
    public void reservaConHoraFinAntesDeInicioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> reservaDe(cancha1, 11, 10));
        assertThrows(IllegalArgumentException.class, () -> reservaDe(cancha1, 10, 10));
    }

    @Test
    public void reservaContraSiMismoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, marta, cancha1, dia,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 2, ModalidadJuego.SENCILLOS));
    }

    @Test
    public void reservaConNumeroDeJugadoresIncorrectoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, juan, cancha1, dia,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 3, ModalidadJuego.SENCILLOS));
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, juan, cancha1, dia,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 2, ModalidadJuego.DOBLES));
    }

    @Test
    public void reservaEnInstalacionDeDeporteDeEquipoLanzaExcepcion() {
        DeporteEquipo futbol = new DeporteEquipo("Futbol", 7);
        Instalacion canchaFutbol = new Instalacion("F1", "Futbol 1", 22, futbol, false);
        assertThrows(IllegalArgumentException.class, () -> reservaDe(canchaFutbol, 10, 11));
    }

    @Test
    public void reservaEntreJugadoresDeDistintoNivelLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, leo, cancha1, dia,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 2, ModalidadJuego.SENCILLOS));
    }

    @Test
    public void reservaConJugadorNoInscritoEnElDeporteLanzaExcepcion() {
        JugadorIndividual pedro = new JugadorIndividual("pedro", "4", "Pedro", 30, false);
        assertThrows(IllegalArgumentException.class, () -> new ReservaCancha(marta, pedro, cancha1, dia,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 2, ModalidadJuego.SENCILLOS));
    }

    @Test
    public void reservaConJugadorInactivoLanzaExcepcion() {
        juan.darDeBaja();
        assertThrows(IllegalStateException.class, () -> reservaDe(cancha1, 10, 11));
    }

    @Test
    public void reservaEnInstalacionFueraDeServicioLanzaExcepcion() {
        cancha1.fueraDeServ();
        assertThrows(IllegalStateException.class, () -> reservaDe(cancha1, 10, 11));
    }

    @Test
    public void cancelarReserva() {
        ReservaCancha r = reservaDe(cancha1, 10, 11);
        r.cancelar();
        assertFalse(r.estaActiva());
    }

    @Test
    public void cancelarReservaDosVecesLanzaExcepcion() {
        ReservaCancha r = reservaDe(cancha1, 10, 11);
        r.cancelar();
        assertThrows(IllegalStateException.class, () -> r.cancelar());
    }

    @Test
    public void seSolapaConOtraReserva() {
        ReservaCancha r1 = reservaDe(cancha1, 10, 12);
        ReservaCancha cruzada = reservaDe(cancha2, 11, 13);
        ReservaCancha seguida = reservaDe(cancha2, 12, 13);

        assertTrue(r1.seSolapaCon(cruzada));
        assertTrue(cruzada.seSolapaCon(r1));
        assertFalse(r1.seSolapaCon(seguida));
    }

    @Test
    public void noSeSolapaSiEsOtroDia() {
        ReservaCancha r1 = reservaDe(cancha1, 10, 12);
        ReservaCancha otroDia = new ReservaCancha(marta, juan, cancha2, dia.plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(12, 0), 2, ModalidadJuego.SENCILLOS);
        assertFalse(r1.seSolapaCon(otroDia));
    }

    @Test
    public void seSolapaConReservaNulaLanzaExcepcion() {
        ReservaCancha r1 = reservaDe(cancha1, 10, 12);
        assertThrows(IllegalArgumentException.class, () -> r1.seSolapaCon(null));
    }
}
