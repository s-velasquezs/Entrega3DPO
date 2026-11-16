package club.modelo.usuarios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import club.modelo.comercio.Inventario;
import club.modelo.comercio.Producto;
import club.modelo.comercio.SugerenciaProducto;
import club.modelo.comercio.VentaProducto;
import club.modelo.competencias.Competencia;
import club.modelo.competencias.CompetenciaFases;
import club.modelo.competencias.CompetenciaLiga;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.DeporteIndividual;
import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.enums.Categoria;
import club.modelo.enums.EstadoAsistencia;
import club.modelo.enums.EstadoSolicitud;
import club.modelo.enums.Rubro;
import club.modelo.instalaciones.Instalacion;
import club.modelo.instalaciones.ReservaCancha;
import club.modelo.partidos.Partido;
import club.modelo.partidos.PartidoExterno;
import club.modelo.partidos.PartidoInterno;
import club.modelo.partidos.PartidoPractica;
import club.modelo.personal.Empleado;
import club.modelo.personal.Entrenador;
import club.modelo.personal.Fisioterapeuta;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.personal.Turno;

public class Administrador extends Usuario {

	private String nombre;
	private Inventario inventario;
	private List<Equipo> equipos;
	private List<Jugador> jugadores;
	private List<Empleado> personal;
	private List<SugerenciaProducto> sugerencias;
	private List<ReservaCancha> reservas;
	private List<Partido> partidos;
	private List<VentaProducto> ventas;
	private List<Competencia> competencias;
	private List<Instalacion> instalaciones;

	public Administrador(String username, String password, Inventario inventario) {
		this(username, password, "Administrador", inventario);
	}

	public Administrador(String username, String password, String nombre) {
		this(username, password, nombre, new Inventario());
	}

	public Administrador(String username, String password, String nombre, Inventario inventario) {
		super(username, password);
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del administrador es obligatorio.");
		}
		if (inventario == null) {
			throw new IllegalArgumentException("El inventario es obligatorio.");
		}
		this.nombre = nombre;
		this.inventario = inventario;
		this.equipos = new ArrayList<Equipo>();
		this.jugadores = new ArrayList<Jugador>();
		this.personal = new ArrayList<Empleado>();
		this.sugerencias = new ArrayList<SugerenciaProducto>();
		this.reservas = new ArrayList<ReservaCancha>();
		this.partidos = new ArrayList<Partido>();
		this.ventas = new ArrayList<VentaProducto>();
		this.competencias = new ArrayList<Competencia>();
		this.instalaciones = new ArrayList<Instalacion>();
	}

	public String getNombre() {
		return nombre;
	}

	private void validarUsernameLibre(String username) {
		if (getUsername().equals(username)) {
			throw new IllegalStateException("El username ya está en uso.");
		}
		for (Jugador j : jugadores) {
			if (j.getUsername().equals(username)) {
				throw new IllegalStateException("El username ya está en uso.");
			}
		}
		for (Empleado e : personal) {
			if (e.getUsername().equals(username)) {
				throw new IllegalStateException("El username ya está en uso.");
			}
		}
	}

	public Equipo crearEquipo(String nombre, Categoria categoria, DeporteEquipo deporte, int cupoMaximo) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del equipo es obligatorio.");
		}
		if (categoria == null) {
			throw new IllegalArgumentException("La categoría del equipo es obligatoria.");
		}
		if (deporte == null) {
			throw new IllegalArgumentException("El deporte del equipo es obligatorio.");
		}
		if (cupoMaximo <= 0) {
			throw new IllegalArgumentException("El cupo máximo debe ser mayor que cero.");
		}
		for (Equipo e : equipos) {
			if (e.getNombre().equals(nombre)) {
				throw new IllegalStateException("Ya existe un equipo con ese nombre.");
			}
		}
		Equipo equipo = new Equipo(nombre, categoria, deporte, cupoMaximo);
		equipos.add(equipo);
		return equipo;
	}

	private boolean tienePartidoPendiente(Equipo equipo) {
		for (Partido p : partidos) {
			if (p.estaJugado()) {
				continue;
			}
			if (p instanceof PartidoInterno) {
				PartidoInterno pi = (PartidoInterno) p;
				if (pi.getEquipoA() == equipo || pi.getEquipoB() == equipo) {
					return true;
				}
			} else if (p instanceof PartidoExterno) {
				if (((PartidoExterno) p).getEquipoClub() == equipo) {
					return true;
				}
			}
		}
		for (Competencia c : competencias) {
			for (PartidoExterno pe : c.getPartidos()) {
				if (!pe.estaJugado() && pe.getEquipoClub() == equipo) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean tienePartidoPendiente(Jugador jugador) {
		for (Partido p : partidos) {
			if (!p.estaJugado() && p.getParticipantes().contains(jugador)) {
				return true;
			}
		}
		if (jugador instanceof JugadorEquipo) {
			for (Inscripcion i : ((JugadorEquipo) jugador).getInscripciones()) {
				if (i.estaActiva() && tienePartidoPendiente(i.getEquipo())) {
					return true;
				}
			}
		}
		return false;
	}

	public void darDeBajaEquipo(Equipo equipo) {
		if (equipo == null || !equipos.contains(equipo)) {
			throw new IllegalArgumentException("El equipo no está registrado.");
		}
		if (tienePartidoPendiente(equipo)) {
			throw new IllegalStateException("El equipo tiene partidos programados.");
		}
		equipo.darDeBaja();
	}

	public JugadorEquipo crearJugadorEquipo(String username, String password, String nombre, int edad) {
		validarUsernameLibre(username);
		JugadorEquipo jugador = new JugadorEquipo(username, password, nombre, edad);
		jugadores.add(jugador);
		return jugador;
	}

	public JugadorIndividual crearJugadorIndividual(String username, String password, String nombre, int edad,
			boolean tambienEntrenador) {
		validarUsernameLibre(username);
		JugadorIndividual jugador = new JugadorIndividual(username, password, nombre, edad, tambienEntrenador);
		jugadores.add(jugador);
		return jugador;
	}

	public boolean darDeBajaJugador(Jugador jugador) {
		if (jugador == null || !jugadores.contains(jugador)) {
			throw new IllegalArgumentException("El jugador no está registrado.");
		}
		if (tienePartidoPendiente(jugador)) {
			throw new IllegalStateException("El jugador tiene partidos programados.");
		}
		if (jugador.estaSuspendido()) {
			throw new IllegalStateException("El jugador tiene sanciones activas.");
		}
		return jugador.darDeBaja();
	}

	public Entrenador crearEntrenador(String username, String password, String nombre) {
		validarUsernameLibre(username);
		Entrenador entrenador = new Entrenador(username, password, nombre);
		personal.add(entrenador);
		return entrenador;
	}

	public Fisioterapeuta crearFisioterapeuta(String username, String password, String nombre) {
		validarUsernameLibre(username);
		Fisioterapeuta fisio = new Fisioterapeuta(username, password, nombre);
		personal.add(fisio);
		return fisio;
	}

	public Empleado crearEmpleado(String username, String password, String nombre) {
		validarUsernameLibre(username);
		Empleado empleado = new Empleado(username, password, nombre);
		personal.add(empleado);
		return empleado;
	}

	public void darDeBajaEmpleado(Empleado empleado) {
		if (empleado == null || !personal.contains(empleado)) {
			throw new IllegalArgumentException("El empleado no está registrado.");
		}
		empleado.darDeBaja();
	}

	public boolean asignarTurno(Empleado empleado, Turno turno) {
		if (empleado == null || !personal.contains(empleado)) {
			throw new IllegalArgumentException("El empleado no pertenece al personal.");
		}
		return empleado.agregarTurno(turno);
	}

	public boolean editarTurno(Empleado empleado, Turno actual, Turno nuevo) {
		if (empleado == null || !personal.contains(empleado)) {
			throw new IllegalArgumentException("El empleado no pertenece al personal.");
		}
		return empleado.reemplazarTurno(actual, nuevo);
	}

	public boolean cumpleMinimoPersonal(LocalDate fecha, LocalTime ini, LocalTime fin) {
		if (fecha == null || ini == null || fin == null || !ini.isBefore(fin)) {
			throw new IllegalArgumentException("La franja no es válida.");
		}
		int entrenadores = 0;
		int fisioterapeutas = 0;
		for (Empleado e : personal) {
			if (!e.estaActivo() || !e.tieneTurnoEn(fecha, ini, fin)) {
				continue;
			}
			if (e instanceof Entrenador) {
				entrenadores++;
			} else if (e instanceof Fisioterapeuta) {
				fisioterapeutas++;
			}
		}
		return entrenadores >= SolicitudCambioTurno.MIN_ENTRENADORES
				&& fisioterapeutas >= SolicitudCambioTurno.MIN_FISIOTERAPEUTAS;
	}

	private void validarSolicitud(SolicitudCambioTurno solicitud) {
		if (solicitud == null || !personal.contains(solicitud.getEmpleado())) {
			throw new IllegalArgumentException("El empleado de la solicitud no pertenece al personal.");
		}
	}

	public boolean aprobarSolicitud(SolicitudCambioTurno solicitud) {
		validarSolicitud(solicitud);
		return solicitud.aprobar(getPersonal());
	}

	public void rechazarSolicitud(SolicitudCambioTurno solicitud, String razon) {
		validarSolicitud(solicitud);
		solicitud.rechazar(razon);
	}

	public List<SolicitudCambioTurno> consultarSolicitudesPendientes() {
		List<SolicitudCambioTurno> lista = new ArrayList<SolicitudCambioTurno>();
		for (Empleado e : personal) {
			for (SolicitudCambioTurno s : e.getSolicitudes()) {
				if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
					lista.add(s);
				}
			}
		}
		return lista;
	}

	public Competencia crearCompetenciaLiga(String nombre, String entidad, int temporada, DeporteEquipo deporte,
			Categoria categoria) {
		validarCompetenciaLibre(nombre, temporada);
		Competencia competencia = new CompetenciaLiga(nombre, entidad, temporada, deporte, categoria);
		competencias.add(competencia);
		return competencia;
	}

	public Competencia crearCompetenciaFases(String nombre, String entidad, int temporada, DeporteEquipo deporte,
			Categoria categoria) {
		validarCompetenciaLibre(nombre, temporada);
		CompetenciaFases competencia = new CompetenciaFases(nombre, entidad, temporada, deporte, categoria);
		competencias.add(competencia);
		return (Competencia) competencia;
	}

	private void validarCompetenciaLibre(String nombre, int temporada) {
		for (Competencia c : competencias) {
			if (c.getNombre().equals(nombre) && c.getTemporada() == temporada) {
				throw new IllegalStateException("Ya existe esa competencia en la temporada.");
			}
		}
	}

	public void inscribirEquipoEnCompetencia(Competencia competencia, Equipo equipo, List<JugadorEquipo> nomina) {
		if (competencia == null || !competencias.contains(competencia)) {
			throw new IllegalArgumentException("La competencia no está registrada.");
		}
		if (equipo == null || !equipos.contains(equipo)) {
			throw new IllegalArgumentException("El equipo no está registrado.");
		}
		competencia.inscribirEquipo(equipo, nomina);
	}

	public void registrarInstalacion(Instalacion instalacion) {
		if (instalacion == null) {
			throw new IllegalArgumentException("La instalación no puede ser nula.");
		}
		if (instalaciones.contains(instalacion)) {
			throw new IllegalStateException("La instalación ya está registrada.");
		}
		instalaciones.add(instalacion);
	}

	public List<Instalacion> getInstalaciones() {
		return new ArrayList<Instalacion>(instalaciones);
	}

	public Inventario getInventario() {
		return inventario;
	}

	public void moverArticulo(Producto producto, String origen, String destino, int cantidad) {
		if (producto == null) {
			throw new IllegalArgumentException("El producto es obligatorio.");
		}
		if (origen == null || origen.isBlank() || destino == null || destino.isBlank()) {
			throw new IllegalArgumentException("Origen y destino son obligatorios.");
		}
		if (origen.equals(destino)) {
			throw new IllegalArgumentException("Origen y destino deben ser distintos.");
		}
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
		}
		inventario.mover(producto, origen, destino, cantidad);
	}

	public void reabastecerTienda(Producto producto, int cantidad) {
		if (producto == null) {
			throw new IllegalArgumentException("El producto es obligatorio.");
		}
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
		}
		inventario.agregar(producto, "TIENDA", cantidad);
	}

	public void recibirSugerencia(SugerenciaProducto sugerencia) {
		if (sugerencia == null) {
			throw new IllegalArgumentException("La sugerencia no puede ser nula.");
		}
		if (sugerencias.contains(sugerencia)) {
			throw new IllegalStateException("La sugerencia ya fue recibida.");
		}
		sugerencias.add(sugerencia);
	}

	public void aprobarSugerencia(SugerenciaProducto sugerencia) {
		if (sugerencia == null || !sugerencias.contains(sugerencia)) {
			throw new IllegalArgumentException("La sugerencia no fue recibida.");
		}
		if (sugerencia.getEstado() != EstadoSolicitud.PENDIENTE) {
			throw new IllegalStateException("La sugerencia ya fue resuelta.");
		}
		inventario.registrarProducto(sugerencia.getProducto());
		sugerencia.aprobar();
	}

	public void rechazarSugerencia(SugerenciaProducto sugerencia, String razon) {
		if (sugerencia == null || !sugerencias.contains(sugerencia)) {
			throw new IllegalArgumentException("La sugerencia no fue recibida.");
		}
		if (razon == null || razon.isBlank()) {
			throw new IllegalArgumentException("La razón del rechazo es obligatoria.");
		}
		sugerencia.rechazar(razon);
	}

	public List<SugerenciaProducto> consultarSugerenciasPendientes() {
		List<SugerenciaProducto> lista = new ArrayList<SugerenciaProducto>();
		for (SugerenciaProducto s : sugerencias) {
			if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
				lista.add(s);
			}
		}
		return lista;
	}

	public void registrarReserva(ReservaCancha reserva) {
		if (reserva == null) {
			throw new IllegalArgumentException("La reserva no puede ser nula.");
		}
		if (reservas.contains(reserva)) {
			throw new IllegalStateException("La reserva ya está registrada.");
		}
		reservas.add(reserva);
	}

	public void registrarPartido(Partido partido) {
		if (partido == null) {
			throw new IllegalArgumentException("El partido no puede ser nulo.");
		}
		if (partidos.contains(partido)) {
			throw new IllegalStateException("El partido ya está registrado.");
		}
		if (partido instanceof PartidoInterno) {
			PartidoInterno pi = (PartidoInterno) partido;
			if (!equipos.contains(pi.getEquipoA()) || !equipos.contains(pi.getEquipoB())) {
				throw new IllegalArgumentException("Los equipos del partido no están registrados.");
			}
		} else if (partido instanceof PartidoExterno) {
			if (!equipos.contains(((PartidoExterno) partido).getEquipoClub())) {
				throw new IllegalArgumentException("El equipo del partido no está registrado.");
			}
		} else if (partido instanceof PartidoPractica) {
			PartidoPractica pp = (PartidoPractica) partido;
			if (!jugadores.contains(pp.getJugadorA()) || !jugadores.contains(pp.getJugadorB())) {
				throw new IllegalArgumentException("Los jugadores del partido no están registrados.");
			}
		}
		partido.validar();
		if (partido instanceof PartidoExterno) {
			PartidoExterno pe = (PartidoExterno) partido;
			Competencia competencia = pe.getCompetencia();
			if (competencia != null && !competencia.getPartidos().contains(pe)) {
				competencia.registrarPartido(pe);
			}
		}
		partidos.add(partido);
	}

	public void registrarVenta(VentaProducto venta) {
		if (venta == null) {
			throw new IllegalArgumentException("La venta no puede ser nula.");
		}
		if (ventas.contains(venta)) {
			throw new IllegalStateException("La venta ya está registrada.");
		}
		ventas.add(venta);
	}

	private void validarPeriodo(LocalDate desde, LocalDate hasta) {
		if (desde == null || hasta == null || desde.isAfter(hasta)) {
			throw new IllegalArgumentException("El periodo no es válido.");
		}
	}

	public List<ReservaCancha> consultarHistorialReservas() {
		return new ArrayList<ReservaCancha>(reservas);
	}

	public List<ReservaCancha> consultarHistorialReservas(LocalDate desde, LocalDate hasta) {
		validarPeriodo(desde, hasta);
		List<ReservaCancha> lista = new ArrayList<ReservaCancha>();
		for (ReservaCancha r : reservas) {
			LocalDate fecha = (LocalDate) r.getFecha();
			if (!fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
				lista.add(r);
			}
		}
		lista.sort(Comparator.comparing(ReservaCancha::getFecha));
		return lista;
	}

	public List<Partido> consultarHistorialPartidos() {
		return new ArrayList<Partido>(partidos);
	}

	public List<Partido> consultarHistorialPartidos(LocalDate desde, LocalDate hasta) {
		validarPeriodo(desde, hasta);
		List<Partido> lista = new ArrayList<Partido>();
		for (Partido p : partidos) {
			if (!p.getFecha().isBefore(desde) && !p.getFecha().isAfter(hasta)) {
				lista.add(p);
			}
		}
		lista.sort(Comparator.comparing(Partido::getFecha));
		return lista;
	}

	public List<Entrenamiento> consultarHistorialEntrenamientos() {
		List<Entrenamiento> lista = new ArrayList<Entrenamiento>();
		for (Empleado e : personal) {
			if (e instanceof Entrenador) {
				lista.addAll(((Entrenador) e).getEntrenamientos());
			}
		}
		lista.sort(Comparator.comparing(Entrenamiento::getFecha));
		return lista;
	}

	public List<Entrenamiento> consultarHistorialEntrenamientos(LocalDate desde, LocalDate hasta) {
		validarPeriodo(desde, hasta);
		List<Entrenamiento> lista = new ArrayList<Entrenamiento>();
		for (Entrenamiento e : consultarHistorialEntrenamientos()) {
			if (!e.getFecha().isBefore(desde) && !e.getFecha().isAfter(hasta)) {
				lista.add(e);
			}
		}
		return lista;
	}

	public Map<Rubro, Double> reporteVentasDiario(LocalDate dia) {
		if (dia == null) {
			throw new IllegalArgumentException("El día es obligatorio.");
		}
		return reporteVentas(dia, dia);
	}

	public Map<Rubro, Double> reporteVentasSemanal(LocalDate inicioSemana) {
		if (inicioSemana == null) {
			throw new IllegalArgumentException("El inicio de la semana es obligatorio.");
		}
		return reporteVentas(inicioSemana, inicioSemana.plusDays(6));
	}

	public Map<Rubro, Double> reporteVentasMensual(YearMonth mes) {
		if (mes == null) {
			throw new IllegalArgumentException("El mes es obligatorio.");
		}
		return reporteVentas(mes.atDay(1), mes.atEndOfMonth());
	}

	private Map<Rubro, Double> reporteVentas(LocalDate desde, LocalDate hasta) {
		validarPeriodo(desde, hasta);
		Map<Rubro, Double> reporte = new EnumMap<Rubro, Double>(Rubro.class);
		for (Rubro r : Rubro.values()) {
			reporte.put(r, 0.0);
		}
		for (VentaProducto v : ventas) {
			LocalDate fecha = v.getFecha();
			if (!fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
				reporte.merge(Rubro.valueOf(v.getRubro()), v.getTotal(), Double::sum);
			}
		}
		return reporte;
	}

	public Map<EstadoAsistencia, Integer> reporteAsistencia(Jugador jugador, LocalDate desde, LocalDate hasta) {
		if (jugador == null) {
			throw new IllegalArgumentException("El jugador es obligatorio.");
		}
		validarPeriodo(desde, hasta);
		Map<EstadoAsistencia, Integer> reporte = new EnumMap<EstadoAsistencia, Integer>(EstadoAsistencia.class);
		for (EstadoAsistencia estado : EstadoAsistencia.values()) {
			reporte.put(estado, 0);
		}
		for (Entrenamiento ent : consultarHistorialEntrenamientos()) {
			LocalDate fecha = ent.getFecha();
			if (fecha.isBefore(desde) || fecha.isAfter(hasta)) {
				continue;
			}
			EstadoAsistencia estado = ent.getAsistencia(jugador);
			if (estado != null) {
				reporte.merge(estado, 1, Integer::sum);
			}
		}
		return reporte;
	}

	public List<Partido> reporteResultados(LocalDate desde, LocalDate hasta) {
		validarPeriodo(desde, hasta);
		List<Partido> lista = new ArrayList<Partido>();
		for (Partido p : partidos) {
			LocalDate fecha = p.getFecha();
			if (p.estaJugado() && !fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
				lista.add(p);
			}
		}
		lista.sort(Comparator.comparing(Partido::getFecha));
		return lista;
	}

	public List<Integer> reporteEvolucionRanking(JugadorIndividual jugador, DeporteIndividual deporte) {
		if (jugador == null || deporte == null) {
			throw new IllegalArgumentException("Jugador y deporte son obligatorios.");
		}
		RankingELO ranking = jugador.getRanking(deporte);
		if (ranking == null) {
			throw new IllegalStateException("El jugador no está inscrito en ese deporte.");
		}
		return ranking.getHistorial();
	}

	public List<Equipo> getEquipos() {
		return new ArrayList<Equipo>(equipos);
	}

	public List<Jugador> getJugadores() {
		return new ArrayList<Jugador>(jugadores);
	}

	public List<Empleado> getPersonal() {
		return new ArrayList<Empleado>(personal);
	}

	public List<Competencia> getCompetencias() {
		return new ArrayList<Competencia>(competencias);
	}

	public List<Partido> getPartidos() {
		return new ArrayList<Partido>(partidos);
	}

	public List<ReservaCancha> getReservas() {
		return new ArrayList<ReservaCancha>(reservas);
	}

	public List<VentaProducto> getVentas() {
		return new ArrayList<VentaProducto>(ventas);
	}

	public List<SugerenciaProducto> getSugerencias() {
		return new ArrayList<SugerenciaProducto>(sugerencias);
	}
}