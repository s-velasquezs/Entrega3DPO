package club.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.logica.Club;
import club.modelo.competencias.Competencia;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoSolicitud;
import club.modelo.enums.ModalidadJuego;
import club.modelo.enums.Nivel;
import club.modelo.instalaciones.Instalacion;
import club.modelo.instalaciones.ReservaCancha;
import club.modelo.partidos.Partido;
import club.modelo.personal.Entrenador;
import club.modelo.personal.Fisioterapeuta;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.personal.Turno;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;

// Pruebas de la clase Club (paquete logica)
public class TestLogica {

    private Administrador admin;
    private Club club;
    private LocalDate dia;

    @BeforeEach
    public void preparar() {
        admin = new Administrador("admin", "admin123", "Admin");
        club = new Club(admin);
        dia = LocalDate.of(2026, 12, 1);
    }

    // ---------- Crear el club ----------

    @Test
    public void clubNuevoEstaVacio() {
        assertSame(admin, club.getAdministrador());
        assertTrue(club.getJugadores().isEmpty());
        assertTrue(club.getEmpleados().isEmpty());
        assertTrue(club.getEquipos().isEmpty());
        assertTrue(club.getInstalaciones().isEmpty());
        assertTrue(club.getEntrenamientos().isEmpty());
    }

    @Test
    public void clubSinAdministradorLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Club(null));
    }

    @Test
    public void tiendaYCafeteriaComparteInventario() {
        assertSame(club.getTienda().getInventario(), club.getCafeteria().getInventario());
    }

    // ---------- Jugadores ----------

    @Test
    public void crearJugadorIndividualYDeEquipo() {
        Jugador individual = club.crearJugador("marta", "1", "Marta", 20, true);
        Jugador deEquipo = club.crearJugador("ana", "2", "Ana", 14, false);

        assertTrue(individual instanceof JugadorIndividual);
        assertTrue(deEquipo instanceof JugadorEquipo);
        assertEquals(2, club.getJugadores().size());
    }

    @Test
    public void crearJugadorConUsernameRepetidoLanzaExcepcion() {
        club.crearJugador("ana", "1", "Ana", 14, false);
        assertThrows(IllegalArgumentException.class, () -> club.crearJugador("ana", "2", "Otra Ana", 15, false));
    }

    @Test
    public void crearJugadorConUsernameDelAdministradorLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> club.crearJugador("admin", "1", "Falso", 30, true));
    }

    @Test
    public void crearJugadorConDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> club.crearJugador("", "1", "Ana", 14, false));
        assertThrows(IllegalArgumentException.class, () -> club.crearJugador(null, "1", "Ana", 14, false));
        assertThrows(IllegalArgumentException.class, () -> club.crearJugador("ana", "", "Ana", 14, false));
        assertThrows(IllegalArgumentException.class, () -> club.crearJugador("ana", "1", " ", 14, false));
        assertThrows(IllegalArgumentException.class, () -> club.crearJugador("ana", "1", "Ana", -1, false));
        assertThrows(IllegalArgumentException.class, () -> club.crearJugador("ana", "1", "Ana", 121, false));
    }

    @Test
    public void buscarJugador() {
        Jugador ana = club.crearJugador("ana", "1", "Ana", 14, false);
        assertSame(ana, club.buscarJugador("ana"));
    }

    @Test
    public void buscarJugadorQueNoExisteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> club.buscarJugador("nadie"));
    }

    @Test
    public void darDeBajaJugador() {
        Jugador ana = club.crearJugador("ana", "1", "Ana", 14, false);
        club.darDeBajaJugador(ana);
        assertTrue(!ana.estaActivo());
    }

    @Test
    public void darDeBajaJugadorDosVecesLanzaExcepcion() {
        Jugador ana = club.crearJugador("ana", "1", "Ana", 14, false);
        club.darDeBajaJugador(ana);
        assertThrows(IllegalStateException.class, () -> club.darDeBajaJugador(ana));
    }

    @Test
    public void darDeBajaJugadorSancionadoLanzaExcepcion() {
        Jugador ana = club.crearJugador("ana", "1", "Ana", 14, false);
        ana.consultarEstadisticas().aplicarSancion("Roja", 2);
        assertThrows(IllegalStateException.class, () -> club.darDeBajaJugador(ana));
        assertTrue(ana.estaActivo());
    }

    @Test
    public void darDeBajaJugadorQueNoEsDelClubLanzaExcepcion() {
        JugadorEquipo ajeno = new JugadorEquipo("ajeno", "1", "Ajeno", 14);
        assertThrows(IllegalArgumentException.class, () -> club.darDeBajaJugador(ajeno));
        assertThrows(IllegalArgumentException.class, () -> club.darDeBajaJugador(null));
    }

    // ---------- Empleados ----------

    @Test
    public void registrarEmpleado() {
        Entrenador e1 = new Entrenador("e1", "1", "Entrenador 1");
        club.registrarEmpleado(e1);
        assertEquals(1, club.getEmpleados().size());
    }

    @Test
    public void registrarEmpleadoInvalidoLanzaExcepcion() {
        Entrenador e1 = new Entrenador("e1", "1", "Entrenador 1");
        club.registrarEmpleado(e1);
        Entrenador repetido = new Entrenador("e1", "2", "Otro");
        assertThrows(IllegalArgumentException.class, () -> club.registrarEmpleado(repetido));
        assertThrows(IllegalArgumentException.class, () -> club.registrarEmpleado(null));
    }

    // ---------- Autenticar ----------

    @Test
    public void autenticarAdministradorJugadorYEmpleado() {
        Jugador ana = club.crearJugador("ana", "1234", "Ana", 14, false);
        Entrenador e1 = new Entrenador("e1", "abcd", "Entrenador 1");
        club.registrarEmpleado(e1);

        assertSame(admin, club.autenticar("admin", "admin123"));
        assertSame(ana, club.autenticar("ana", "1234"));
        assertSame(e1, club.autenticar("e1", "abcd"));
    }

    @Test
    public void autenticarComparaElTextoYNoLaReferencia() {
        Jugador ana = club.crearJugador("ana", "1234", "Ana", 14, false);
        // new String crea textos nuevos, distintos objetos pero con el mismo contenido
        assertSame(ana, club.autenticar(new String("ana"), new String("1234")));
    }

    @Test
    public void autenticarConDatosIncorrectosLanzaExcepcion() {
        club.crearJugador("ana", "1234", "Ana", 14, false);
        assertThrows(IllegalArgumentException.class, () -> club.autenticar("ana", "mala"));
        assertThrows(IllegalArgumentException.class, () -> club.autenticar("nadie", "1234"));
        assertThrows(IllegalArgumentException.class, () -> club.autenticar(null, "1234"));
        assertThrows(IllegalArgumentException.class, () -> club.autenticar("ana", null));
    }

    // ---------- Deportes y equipos ----------

    @Test
    public void registrarDeporte() {
        club.registrarDeporte(new DeporteEquipo("Futbol", 7));
        assertEquals(1, club.getDeportes().size());
    }

    @Test
    public void registrarDeporteRepetidoOnuloLanzaExcepcion() {
        club.registrarDeporte(new DeporteEquipo("Futbol", 7));
        assertThrows(IllegalArgumentException.class, () -> club.registrarDeporte(new DeporteEquipo("FUTBOL", 5)));
        assertThrows(IllegalArgumentException.class, () -> club.registrarDeporte(null));
    }

    @Test
    public void crearEquipo() {
        DeporteEquipo futbol = new DeporteEquipo("Futbol", 7);
        club.registrarDeporte(futbol);

        Equipo leones = club.crearEquipo("Leones", futbol, Categoria.SUB15, 18);
        assertSame(leones, club.buscarEquipo("leones"));
        assertEquals(1, club.getEquipos().size());
    }

    @Test
    public void crearEquipoConDeporteNoRegistradoLanzaExcepcion() {
        DeporteEquipo futbol = new DeporteEquipo("Futbol", 7);
        assertThrows(IllegalArgumentException.class, () -> club.crearEquipo("Leones", futbol, Categoria.SUB15, 18));
        assertThrows(IllegalArgumentException.class, () -> club.crearEquipo("Leones", null, Categoria.SUB15, 18));
    }

    @Test
    public void crearEquipoConNombreRepetidoLanzaExcepcion() {
        DeporteEquipo futbol = new DeporteEquipo("Futbol", 7);
        club.registrarDeporte(futbol);
        club.crearEquipo("Leones", futbol, Categoria.SUB15, 18);
        assertThrows(IllegalArgumentException.class, () -> club.crearEquipo("leones", futbol, Categoria.SUB17, 18));
    }

    @Test
    public void crearEquipoConCupoInvalidoLanzaExcepcion() {
        DeporteEquipo futbol = new DeporteEquipo("Futbol", 7);
        club.registrarDeporte(futbol);
        assertThrows(IllegalArgumentException.class, () -> club.crearEquipo("Leones", futbol, Categoria.SUB15, 0));
    }

    @Test
    public void buscarEquipoQueNoExisteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> club.buscarEquipo("Fantasmas"));
    }

    @Test
    public void darDeBajaEquipo() {
        DeporteEquipo futbol = new DeporteEquipo("Futbol", 7);
        club.registrarDeporte(futbol);
        Equipo leones = club.crearEquipo("Leones", futbol, Categoria.SUB15, 18);

        club.darDeBajaEquipo(leones);
        assertTrue(!leones.estaActivo());
        assertThrows(IllegalStateException.class, () -> club.darDeBajaEquipo(leones));
    }

    @Test
    public void darDeBajaEquipoAjenoLanzaExcepcion() {
        Equipo ajeno = new Equipo("Ajeno", Categoria.SUB15, new DeporteEquipo("Futbol", 7), 10);
        assertThrows(IllegalArgumentException.class, () -> club.darDeBajaEquipo(ajeno));
        assertThrows(IllegalArgumentException.class, () -> club.darDeBajaEquipo(null));
    }

    // ---------- Instalaciones ----------

    @Test
    public void agregarYBuscarInstalacion() {
        Instalacion c1 = new Instalacion("C1", "Cancha 1", 4, new DeporteIndividual("Tenis", 3), false);
        club.agregarInstalacion(c1);
        assertSame(c1, club.buscarInstalacion("C1"));
        assertEquals(1, club.consultarEstadoInstalaciones().size());
    }

    @Test
    public void agregarInstalacionRepetidaOnulaLanzaExcepcion() {
        DeporteIndividual tenis = new DeporteIndividual("Tenis", 3);
        club.agregarInstalacion(new Instalacion("C1", "Cancha 1", 4, tenis, false));
        assertThrows(IllegalArgumentException.class,
                () -> club.agregarInstalacion(new Instalacion("C1", "Otra", 4, tenis, false)));
        assertThrows(IllegalArgumentException.class, () -> club.agregarInstalacion(null));
    }

    @Test
    public void buscarInstalacionQueNoExisteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> club.buscarInstalacion("X9"));
    }

    // ---------- Reservas de cancha ----------

    // Crea 4 jugadores de tenis principiantes y 2 canchas
    private JugadorIndividual jugadorTenis(String username, DeporteIndividual tenis) {
        JugadorIndividual j = (JugadorIndividual) club.crearJugador(username, "1", username, 25, true);
        j.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);
        return j;
    }

    @Test
    public void reservarCancha() {
        DeporteIndividual tenis = new DeporteIndividual("Tenis", 3);
        Instalacion c1 = new Instalacion("C1", "Cancha 1", 4, tenis, false);
        club.agregarInstalacion(c1);
        JugadorIndividual marta = jugadorTenis("marta", tenis);
        JugadorIndividual juan = jugadorTenis("juan", tenis);

        ReservaCancha r = club.reservarCancha(marta, juan, c1, dia, LocalTime.of(10, 0), LocalTime.of(11, 0),
                2, ModalidadJuego.SENCILLOS);

        assertTrue(r.estaActiva());
        assertEquals(1, c1.getReservas().size());
    }

    @Test
    public void reservarCanchaConJugadorOInstalacionQueNoSonDelClubLanzaExcepcion() {
        DeporteIndividual tenis = new DeporteIndividual("Tenis", 3);
        Instalacion c1 = new Instalacion("C1", "Cancha 1", 4, tenis, false);
        Instalacion ajena = new Instalacion("C9", "Ajena", 4, tenis, false);
        club.agregarInstalacion(c1);
        JugadorIndividual marta = jugadorTenis("marta", tenis);
        JugadorIndividual juan = jugadorTenis("juan", tenis);
        JugadorIndividual ajeno = new JugadorIndividual("ajeno", "1", "Ajeno", 30, false);
        ajeno.inscribirseDeporte(tenis, Nivel.PRINCIPIANTE);

        assertThrows(IllegalArgumentException.class, () -> club.reservarCancha(marta, ajeno, c1, dia,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 2, ModalidadJuego.SENCILLOS));
        assertThrows(IllegalArgumentException.class, () -> club.reservarCancha(marta, juan, ajena, dia,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 2, ModalidadJuego.SENCILLOS));
    }

    @Test
    public void reservarCanchaOcupadaLanzaExcepcion() {
        DeporteIndividual tenis = new DeporteIndividual("Tenis", 3);
        Instalacion c1 = new Instalacion("C1", "Cancha 1", 4, tenis, false);
        club.agregarInstalacion(c1);
        JugadorIndividual marta = jugadorTenis("marta", tenis);
        JugadorIndividual juan = jugadorTenis("juan", tenis);
        JugadorIndividual pedro = jugadorTenis("pedro", tenis);
        JugadorIndividual luis = jugadorTenis("luis", tenis);

        club.reservarCancha(marta, juan, c1, dia, LocalTime.of(10, 0), LocalTime.of(11, 0),
                2, ModalidadJuego.SENCILLOS);
        assertThrows(IllegalStateException.class, () -> club.reservarCancha(pedro, luis, c1, dia,
                LocalTime.of(10, 30), LocalTime.of(11, 30), 2, ModalidadJuego.SENCILLOS));
    }

    @Test
    public void jugadorNoPuedeTenerDosReservasALaVez() {
        DeporteIndividual tenis = new DeporteIndividual("Tenis", 3);
        Instalacion c1 = new Instalacion("C1", "Cancha 1", 4, tenis, false);
        Instalacion c2 = new Instalacion("C2", "Cancha 2", 4, tenis, false);
        club.agregarInstalacion(c1);
        club.agregarInstalacion(c2);
        JugadorIndividual marta = jugadorTenis("marta", tenis);
        JugadorIndividual juan = jugadorTenis("juan", tenis);
        JugadorIndividual pedro = jugadorTenis("pedro", tenis);

        club.reservarCancha(marta, juan, c1, dia, LocalTime.of(10, 0), LocalTime.of(11, 0),
                2, ModalidadJuego.SENCILLOS);
        // marta ya juega a esa hora en la cancha 1
        assertThrows(IllegalStateException.class, () -> club.reservarCancha(marta, pedro, c2, dia,
                LocalTime.of(10, 30), LocalTime.of(11, 30), 2, ModalidadJuego.SENCILLOS));
        // el rival tambien cuenta
        assertThrows(IllegalStateException.class, () -> club.reservarCancha(pedro, juan, c2, dia,
                LocalTime.of(10, 30), LocalTime.of(11, 30), 2, ModalidadJuego.SENCILLOS));
    }

    @Test
    public void respetaElMaximoDeInstalacionesSimultaneas() {
        DeporteIndividual tenis = new DeporteIndividual("Tenis", 1); // maximo 1 cancha a la vez
        Instalacion c1 = new Instalacion("C1", "Cancha 1", 4, tenis, false);
        Instalacion c2 = new Instalacion("C2", "Cancha 2", 4, tenis, false);
        club.agregarInstalacion(c1);
        club.agregarInstalacion(c2);
        JugadorIndividual marta = jugadorTenis("marta", tenis);
        JugadorIndividual juan = jugadorTenis("juan", tenis);
        JugadorIndividual pedro = jugadorTenis("pedro", tenis);
        JugadorIndividual luis = jugadorTenis("luis", tenis);

        club.reservarCancha(marta, juan, c1, dia, LocalTime.of(10, 0), LocalTime.of(11, 0),
                2, ModalidadJuego.SENCILLOS);
        assertThrows(IllegalStateException.class, () -> club.reservarCancha(pedro, luis, c2, dia,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 2, ModalidadJuego.SENCILLOS));
        // a otra hora si se puede
        ReservaCancha r = club.reservarCancha(pedro, luis, c2, dia, LocalTime.of(11, 0), LocalTime.of(12, 0),
                2, ModalidadJuego.SENCILLOS);
        assertTrue(r.estaActiva());
    }

    @Test
    public void conMaximoMayorSePuedenReservarVariasCanchas() {
        DeporteIndividual tenis = new DeporteIndividual("Tenis", 2);
        Instalacion c1 = new Instalacion("C1", "Cancha 1", 4, tenis, false);
        Instalacion c2 = new Instalacion("C2", "Cancha 2", 4, tenis, false);
        club.agregarInstalacion(c1);
        club.agregarInstalacion(c2);
        JugadorIndividual marta = jugadorTenis("marta", tenis);
        JugadorIndividual juan = jugadorTenis("juan", tenis);
        JugadorIndividual pedro = jugadorTenis("pedro", tenis);
        JugadorIndividual luis = jugadorTenis("luis", tenis);

        club.reservarCancha(marta, juan, c1, dia, LocalTime.of(10, 0), LocalTime.of(11, 0),
                2, ModalidadJuego.SENCILLOS);
        club.reservarCancha(pedro, luis, c2, dia, LocalTime.of(10, 0), LocalTime.of(11, 0),
                2, ModalidadJuego.SENCILLOS);
        assertEquals(1, c1.getReservas().size());
        assertEquals(1, c2.getReservas().size());
    }

    // ---------- Entrenamientos ----------

    private DeporteEquipo futbol;
    private Instalacion canchaFutbol;
    private Entrenador coach;
    private Equipo leones;

    // Deja listo el club con deporte, equipo, instalacion y entrenador
    private void prepararEntrenamientos() {
        futbol = new DeporteEquipo("Futbol", 2);
        club.registrarDeporte(futbol);
        leones = club.crearEquipo("Leones", futbol, Categoria.SUB15, 10);
        canchaFutbol = new Instalacion("F1", "Cancha futbol", 22, futbol, false);
        club.agregarInstalacion(canchaFutbol);
        coach = new Entrenador("coach", "1", "Carlos");
        club.registrarEmpleado(coach);
    }

    @Test
    public void agendarEntrenamiento() {
        prepararEntrenamientos();
        Entrenamiento e = new Entrenamiento(dia, LocalTime.of(16, 0), 90, futbol, coach, canchaFutbol,
                Categoria.SUB15, leones);
        club.agendarEntrenamiento(e);

        assertEquals(1, club.getEntrenamientos().size());
        assertEquals(1, canchaFutbol.getEntrenamientos().size());
    }

    @Test
    public void agendarEntrenamientoEnConflictoLanzaExcepcion() {
        prepararEntrenamientos();
        club.agendarEntrenamiento(new Entrenamiento(dia, LocalTime.of(16, 0), 90, futbol, coach, canchaFutbol,
                Categoria.SUB15, leones));
        Entrenamiento cruzado = new Entrenamiento(dia, LocalTime.of(17, 0), 60, futbol, coach, canchaFutbol,
                Categoria.SUB15, leones);
        assertThrows(IllegalStateException.class, () -> club.agendarEntrenamiento(cruzado));
        assertEquals(1, club.getEntrenamientos().size());
    }

    @Test
    public void agendarEntrenamientoSeguidoNoEsConflicto() {
        prepararEntrenamientos();
        club.agendarEntrenamiento(new Entrenamiento(dia, LocalTime.of(16, 0), 60, futbol, coach, canchaFutbol,
                Categoria.SUB15, leones));
        club.agendarEntrenamiento(new Entrenamiento(dia, LocalTime.of(17, 0), 60, futbol, coach, canchaFutbol,
                Categoria.SUB15, leones));
        assertEquals(2, club.getEntrenamientos().size());
    }

    @Test
    public void agendarEntrenamientoConEntrenadorOInstalacionOEquipoAjenosLanzaExcepcion() {
        prepararEntrenamientos();
        Entrenador ajeno = new Entrenador("ajeno", "1", "Ajeno");
        Instalacion canchaAjena = new Instalacion("F9", "Ajena", 22, futbol, false);
        Equipo equipoAjeno = new Equipo("Ajeno", Categoria.SUB15, futbol, 10);

        Entrenamiento conEntrenadorAjeno = new Entrenamiento(dia, LocalTime.of(8, 0), 60, futbol, ajeno,
                canchaFutbol, Categoria.SUB15, leones);
        Entrenamiento conInstalacionAjena = new Entrenamiento(dia, LocalTime.of(9, 0), 60, futbol, coach,
                canchaAjena, Categoria.SUB15, leones);
        Entrenamiento conEquipoAjeno = new Entrenamiento(dia, LocalTime.of(10, 0), 60, futbol, coach,
                canchaFutbol, Categoria.SUB15, equipoAjeno);

        assertThrows(IllegalArgumentException.class, () -> club.agendarEntrenamiento(conEntrenadorAjeno));
        assertThrows(IllegalArgumentException.class, () -> club.agendarEntrenamiento(conInstalacionAjena));
        assertThrows(IllegalArgumentException.class, () -> club.agendarEntrenamiento(conEquipoAjeno));
        assertThrows(IllegalArgumentException.class, () -> club.agendarEntrenamiento(null));
    }

    // ---------- Turnos del personal ----------

    private Entrenador e1;
    private Entrenador e2;
    private Entrenador e3;
    private Fisioterapeuta f1;
    private Turno t1;
    private Turno t2;

    // Mismos datos que PruebaPersonal: 3 entrenadores y 1 fisio de 8 a 12
    private void prepararPersonal() {
        e1 = new Entrenador("e1", "1", "Entrenador 1");
        e2 = new Entrenador("e2", "2", "Entrenador 2");
        e3 = new Entrenador("e3", "3", "Entrenador 3");
        f1 = new Fisioterapeuta("f1", "4", "Fisio 1");
        club.registrarEmpleado(e1);
        club.registrarEmpleado(e2);
        club.registrarEmpleado(e3);
        club.registrarEmpleado(f1);

        t1 = new Turno(e1, dia, LocalTime.of(8, 0), LocalTime.of(12, 0));
        t2 = new Turno(e2, dia, LocalTime.of(8, 0), LocalTime.of(12, 0));
        e1.agregarTurno(t1);
        e2.agregarTurno(t2);
        e3.agregarTurno(new Turno(e3, dia, LocalTime.of(8, 0), LocalTime.of(12, 0)));
        f1.agregarTurno(new Turno(f1, dia, LocalTime.of(8, 0), LocalTime.of(12, 0)));
    }

    private SolicitudCambioTurno solicitud(Entrenador e, Turno actual, String motivo) {
        Turno nuevo = new Turno(e, dia, LocalTime.of(14, 0), LocalTime.of(18, 0));
        return e.solicitarCambioTurno(actual, nuevo, motivo, club.getEmpleados());
    }

    @Test
    public void aprobarSolicitudDeTurno() {
        prepararPersonal();
        SolicitudCambioTurno s1 = solicitud(e1, t1, "Clase");
        club.aprobarSolicitudTurno(s1);
        assertEquals(EstadoSolicitud.APROBADA, s1.getEstado());
    }

    @Test
    public void aprobarSolicitudQueDejaSinPersonalLanzaExcepcion() {
        prepararPersonal();
        SolicitudCambioTurno s1 = solicitud(e1, t1, "Clase");
        SolicitudCambioTurno s2 = solicitud(e2, t2, "Cita");
        club.aprobarSolicitudTurno(s1);
        // si tambien se va e2 solo quedaria e3 y se necesitan 2 entrenadores
        assertThrows(IllegalStateException.class, () -> club.aprobarSolicitudTurno(s2));
        assertEquals(EstadoSolicitud.PENDIENTE, s2.getEstado());
    }

    @Test
    public void aprobarSolicitudNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> club.aprobarSolicitudTurno(null));
    }

    @Test
    public void rechazarSolicitudDeTurno() {
        prepararPersonal();
        SolicitudCambioTurno s1 = solicitud(e1, t1, "Clase");
        club.rechazarSolicitudTurno(s1, "Falta personal");
        assertEquals(EstadoSolicitud.RECHAZADA, s1.getEstado());
        assertEquals("Falta personal", s1.getRazonRechazo());
    }

    @Test
    public void rechazarSolicitudConDatosInvalidosLanzaExcepcion() {
        prepararPersonal();
        SolicitudCambioTurno s1 = solicitud(e1, t1, "Clase");
        assertThrows(IllegalArgumentException.class, () -> club.rechazarSolicitudTurno(null, "Motivo"));
        assertThrows(IllegalArgumentException.class, () -> club.rechazarSolicitudTurno(s1, ""));
        assertThrows(IllegalArgumentException.class, () -> club.rechazarSolicitudTurno(s1, null));
    }

    @Test
    public void rechazarSolicitudYaRespondidaLanzaExcepcion() {
        prepararPersonal();
        SolicitudCambioTurno s1 = solicitud(e1, t1, "Clase");
        club.rechazarSolicitudTurno(s1, "Falta personal");
        assertThrows(IllegalStateException.class, () -> club.rechazarSolicitudTurno(s1, "Otra vez"));
    }

    @Test
    public void editarTurno() {
        prepararPersonal();
        Turno nuevo = new Turno(e1, dia, LocalTime.of(9, 0), LocalTime.of(13, 0));
        club.editarTurno(t1, nuevo);
        assertEquals(LocalTime.of(9, 0), e1.consultarTurnos(dia, dia).get(0).getHoraInicio());
    }

    @Test
    public void editarTurnoConDatosInvalidosLanzaExcepcion() {
        prepararPersonal();
        Turno deOtro = new Turno(e2, dia, LocalTime.of(9, 0), LocalTime.of(13, 0));
        Turno sinRegistrar = new Turno(e1, dia, LocalTime.of(15, 0), LocalTime.of(16, 0));
        Turno nuevo = new Turno(e1, dia, LocalTime.of(9, 0), LocalTime.of(13, 0));

        assertThrows(IllegalArgumentException.class, () -> club.editarTurno(null, nuevo));
        assertThrows(IllegalArgumentException.class, () -> club.editarTurno(t1, null));
        assertThrows(IllegalArgumentException.class, () -> club.editarTurno(t1, deOtro));
        // e1 no tiene el turno "sinRegistrar"
        assertThrows(IllegalStateException.class, () -> club.editarTurno(sinRegistrar, nuevo));
    }

    // ---------- Partidos y competencias ----------

    @Test
    public void agregarPartidoYCompetencia() {
        DeporteEquipo dep = new DeporteEquipo("Futbol", 7);
        club.agregarPartido(new Partido(dia, Categoria.SUB15, dep) {
        });
        club.agregarCompetencia(new Competencia("Liga", "Federacion", 2026, dep, Categoria.SUB15) {
            @Override
            public java.util.Map<String, Integer> calcularTabla() {
                return new java.util.HashMap<String, Integer>();
            }
        });
        assertEquals(1, club.getPartidos().size());
        assertEquals(1, club.getCompetencias().size());
    }

    @Test
    public void agregarPartidoOCompetenciaNulosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> club.agregarPartido(null));
        assertThrows(IllegalArgumentException.class, () -> club.agregarCompetencia(null));
    }

    // ---------- Listas ----------

    @Test
    public void lasListasDelClubSonCopias() {
        club.crearJugador("ana", "1", "Ana", 14, false);
        club.getJugadores().clear();
        assertEquals(1, club.getJugadores().size());
    }
}
