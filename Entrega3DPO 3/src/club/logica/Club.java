package club.logica;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import club.modelo.comercio.Cafeteria;
import club.modelo.comercio.Inventario;
import club.modelo.comercio.Tienda;
import club.modelo.competencias.Competencia;
import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.enums.Categoria;
import club.modelo.enums.ModalidadJuego;
import club.modelo.instalaciones.Instalacion;
import club.modelo.instalaciones.ReservaCancha;
import club.modelo.partidos.Partido;
import club.modelo.personal.Empleado;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.personal.Turno;
import club.modelo.usuarios.Administrador;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;
import club.modelo.usuarios.JugadorIndividual;
import club.modelo.usuarios.Usuario;

// Clase principal: guarda todo lo del club y revisa las reglas que involucran varias clases
public class Club {

    private List<Jugador> jugadores;
    private List<Empleado> empleados;
    private Administrador administrador;
    private List<Deporte> deportes;
    private List<Equipo> equipos;
    private List<Partido> partidos;
    private List<Competencia> competencias;
    private List<Entrenamiento> entrenamientos;
    private List<Instalacion> instalaciones;
    private Tienda tienda;
    private Cafeteria cafeteria;

    public Club(Administrador administrador) {
        if (administrador == null) {
            throw new IllegalArgumentException("El club necesita un administrador");
        }
        this.administrador = administrador;
        this.jugadores = new ArrayList<Jugador>();
        this.empleados = new ArrayList<Empleado>();
        this.deportes = new ArrayList<Deporte>();
        this.equipos = new ArrayList<Equipo>();
        this.partidos = new ArrayList<Partido>();
        this.competencias = new ArrayList<Competencia>();
        this.entrenamientos = new ArrayList<Entrenamiento>();
        this.instalaciones = new ArrayList<Instalacion>();

        // La tienda y la cafeteria comparten el inventario (cada una usa su ubicacion)
        Inventario inventario = new Inventario();
        this.tienda = new Tienda(inventario);
        this.cafeteria = new Cafeteria(inventario);
    }

    // ---------------- Getters ----------------

    public Administrador getAdministrador() {
        return administrador;
    }

    public Tienda getTienda() {
        return tienda;
    }

    public Cafeteria getCafeteria() {
        return cafeteria;
    }

    public List<Jugador> getJugadores() {
        return new ArrayList<Jugador>(jugadores);
    }

    public List<Empleado> getEmpleados() {
        return new ArrayList<Empleado>(empleados);
    }

    public List<Deporte> getDeportes() {
        return new ArrayList<Deporte>(deportes);
    }

    public List<Equipo> getEquipos() {
        return new ArrayList<Equipo>(equipos);
    }

    public List<Partido> getPartidos() {
        return new ArrayList<Partido>(partidos);
    }

    public List<Competencia> getCompetencias() {
        return new ArrayList<Competencia>(competencias);
    }

    public List<Entrenamiento> getEntrenamientos() {
        return new ArrayList<Entrenamiento>(entrenamientos);
    }

    public List<Instalacion> getInstalaciones() {
        return new ArrayList<Instalacion>(instalaciones);
    }

    // ---------------- Usuarios ----------------

    private boolean existeUsername(String username) {
        if (administrador.getUsername().equals(username)) {
            return true;
        }
        for (Jugador j : jugadores) {
            if (j.getUsername().equals(username)) {
                return true;
            }
        }
        for (Empleado e : empleados) {
            if (e.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public Jugador crearJugador(String username, String password, String nombre, int edad, boolean esIndividual) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El username no puede estar vacio");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La password no puede estar vacia");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (edad < 0 || edad > 120) {
            throw new IllegalArgumentException("La edad no es valida");
        }
        if (existeUsername(username)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese username");
        }
        Jugador nuevo;
        if (esIndividual) {
            nuevo = new JugadorIndividual(username, password, nombre, edad, false);
        } else {
            nuevo = new JugadorEquipo(username, password, nombre, edad);
        }
        jugadores.add(nuevo);
        return nuevo;
    }

    public void registrarEmpleado(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo");
        }
        if (existeUsername(empleado.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese username");
        }
        empleados.add(empleado);
    }

    public void darDeBajaJugador(Jugador jugador) {
        if (jugador == null || !jugadores.contains(jugador)) {
            throw new IllegalArgumentException("El jugador no pertenece al club");
        }
        if (!jugador.estaActivo()) {
            throw new IllegalStateException("El jugador ya esta dado de baja");
        }
        if (!jugador.darDeBaja()) {
            throw new IllegalStateException("El jugador tiene una sancion activa y no se puede dar de baja");
        }
    }

    public Jugador buscarJugador(String username) {
        for (Jugador j : jugadores) {
            if (j.getUsername().equals(username)) {
                return j;
            }
        }
        throw new IllegalArgumentException("No existe el jugador: " + username);
    }

    // Devuelve el usuario si el username y la password son correctos
    public Usuario autenticar(String user, String pass) {
        if (user == null || pass == null) {
            throw new IllegalArgumentException("El usuario y la password no pueden ser nulos");
        }
        if (administrador.autenticar(user, pass)) {
            return administrador;
        }
        for (Jugador j : jugadores) {
            if (j.autenticar(user, pass)) {
                return j;
            }
        }
        for (Empleado e : empleados) {
            if (e.autenticar(user, pass)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Usuario o password incorrectos");
    }

    // ---------------- Deportes y equipos ----------------

    public void registrarDeporte(Deporte deporte) {
        if (deporte == null) {
            throw new IllegalArgumentException("El deporte no puede ser nulo");
        }
        for (Deporte d : deportes) {
            if (d.getNombre().equalsIgnoreCase(deporte.getNombre())) {
                throw new IllegalArgumentException("Ya existe ese deporte");
            }
        }
        deportes.add(deporte);
    }

    public Equipo crearEquipo(String nombre, DeporteEquipo deporte, Categoria categoria, int cupo) {
        if (deporte == null || !deportes.contains(deporte)) {
            throw new IllegalArgumentException("El deporte no esta registrado en el club");
        }
        for (Equipo e : equipos) {
            if (e.getNombre().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe un equipo con ese nombre");
            }
        }
        Equipo nuevo = new Equipo(nombre, categoria, deporte, cupo);
        equipos.add(nuevo);
        return nuevo;
    }

    public void darDeBajaEquipo(Equipo equipo) {
        if (equipo == null || !equipos.contains(equipo)) {
            throw new IllegalArgumentException("El equipo no pertenece al club");
        }
        equipo.darDeBaja();
    }

    public Equipo buscarEquipo(String nombre) {
        for (Equipo e : equipos) {
            if (e.getNombre().equalsIgnoreCase(nombre)) {
                return e;
            }
        }
        throw new IllegalArgumentException("No existe el equipo: " + nombre);
    }

    // ---------------- Instalaciones ----------------

    public void agregarInstalacion(Instalacion instalacion) {
        if (instalacion == null) {
            throw new IllegalArgumentException("La instalacion no puede ser nula");
        }
        for (Instalacion i : instalaciones) {
            if (i.getId().equals(instalacion.getId())) {
                throw new IllegalArgumentException("Ya existe una instalacion con ese id");
            }
        }
        instalaciones.add(instalacion);
    }

    public Instalacion buscarInstalacion(String id) {
        for (Instalacion i : instalaciones) {
            if (i.getId().equals(id)) {
                return i;
            }
        }
        throw new IllegalArgumentException("No existe la instalacion: " + id);
    }

    public List<Instalacion> consultarEstadoInstalaciones() {
        return getInstalaciones();
    }

    // ---------------- Reservas de cancha ----------------

    public ReservaCancha reservarCancha(JugadorIndividual jugador, JugadorIndividual rival, Instalacion instalacion,
                                        LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                        int numeroJugadores, ModalidadJuego modalidad) {
        if (!jugadores.contains(jugador) || !jugadores.contains(rival)) {
            throw new IllegalArgumentException("Los jugadores deben pertenecer al club");
        }
        if (!instalaciones.contains(instalacion)) {
            throw new IllegalArgumentException("La instalacion no pertenece al club");
        }

        // Aqui se revisan todas las reglas propias de la reserva
        ReservaCancha reserva = new ReservaCancha(jugador, rival, instalacion, fecha, horaInicio, horaFin,
                numeroJugadores, modalidad);

        if (tieneReservaEnEseHorario(jugador, fecha, horaInicio, horaFin)
                || tieneReservaEnEseHorario(rival, fecha, horaInicio, horaFin)) {
            throw new IllegalStateException("Alguno de los jugadores ya tiene una reserva en ese horario");
        }

        DeporteIndividual deporte = (DeporteIndividual) instalacion.getDeporte();
        int simultaneas = contarReservasSimultaneas(deporte, fecha, horaInicio, horaFin);
        if (simultaneas >= deporte.getMaxInstalaciones()) {
            throw new IllegalStateException("Ya se alcanzo el maximo de instalaciones simultaneas para " + deporte);
        }

        instalacion.agregarReserva(reserva);
        return reserva;
    }

    private boolean tieneReservaEnEseHorario(JugadorIndividual j, LocalDate fecha, LocalTime ini, LocalTime fin) {
        for (Instalacion i : instalaciones) {
            for (ReservaCancha r : i.getReservas()) {
                boolean participa = (r.getJugador() == j || r.getRival() == j);
                if (participa && r.estaActiva() && r.seSolapaCon(fecha, ini, fin)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int contarReservasSimultaneas(Deporte deporte, LocalDate fecha, LocalTime ini, LocalTime fin) {
        int cantidad = 0;
        for (Instalacion i : instalaciones) {
            if (i.getDeporte() == deporte) {
                for (ReservaCancha r : i.getReservas()) {
                    if (r.estaActiva() && r.seSolapaCon(fecha, ini, fin)) {
                        cantidad++;
                    }
                }
            }
        }
        return cantidad;
    }

    // ---------------- Entrenamientos ----------------

    public void agendarEntrenamiento(Entrenamiento entrenamiento) {
        if (entrenamiento == null) {
            throw new IllegalArgumentException("El entrenamiento no puede ser nulo");
        }
        if (!instalaciones.contains(entrenamiento.getInstalacion())) {
            throw new IllegalArgumentException("La instalacion no pertenece al club");
        }
        if (!empleados.contains(entrenamiento.getResponsable())) {
            throw new IllegalArgumentException("El entrenador no trabaja en el club");
        }
        if (entrenamiento.getEquipo() != null && !equipos.contains(entrenamiento.getEquipo())) {
            throw new IllegalArgumentException("El equipo no pertenece al club");
        }
        for (Entrenamiento otro : entrenamientos) {
            if (entrenamiento.estaEnConflicto(otro)) {
                throw new IllegalStateException("El entrenamiento tiene conflicto con otro ya agendado");
            }
        }
        // Esto lanza excepcion si la instalacion esta ocupada
        entrenamiento.getInstalacion().agregarEntrenamiento(entrenamiento);
        entrenamientos.add(entrenamiento);
    }

    // ---------------- Turnos del personal ----------------

    public void aprobarSolicitudTurno(SolicitudCambioTurno solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud no puede ser nula");
        }
        if (!solicitud.aprobar(empleados)) {
            throw new IllegalStateException("No se pudo aprobar la solicitud");
        }
    }

    public void rechazarSolicitudTurno(SolicitudCambioTurno solicitud, String motivo) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud no puede ser nula");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Hay que dar un motivo para rechazar");
        }
        solicitud.rechazar(motivo);
    }

    public void editarTurno(Turno turno, Turno nuevo) {
        if (turno == null || nuevo == null) {
            throw new IllegalArgumentException("Los turnos no pueden ser nulos");
        }
        if (turno.getEmpleado() != nuevo.getEmpleado()) {
            throw new IllegalArgumentException("Los dos turnos deben ser del mismo empleado");
        }
        if (!turno.getEmpleado().reemplazarTurno(turno, nuevo)) {
            throw new IllegalStateException("El empleado no tiene ese turno");
        }
    }

    // ---------------- Partidos y competencias ----------------

    public void agregarPartido(Partido partido) {
        if (partido == null) {
            throw new IllegalArgumentException("El partido no puede ser nulo");
        }
        partidos.add(partido);
    }

    public void agregarCompetencia(Competencia competencia) {
        if (competencia == null) {
            throw new IllegalArgumentException("La competencia no puede ser nula");
        }
        competencias.add(competencia);
    }
}
