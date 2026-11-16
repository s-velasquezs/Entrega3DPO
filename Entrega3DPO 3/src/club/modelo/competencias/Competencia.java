package club.modelo.competencias;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Equipo;
import club.modelo.enums.Categoria;
import club.modelo.estadisticas.EstadisticasPartido;
import club.modelo.partidos.PartidoExterno;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;

public abstract class Competencia {

	private String nombre;
	private String entidadOrganizadora;
	private int temporada;
	private DeporteEquipo deporte;
	private Categoria categoria;
	private Map<Equipo, List<JugadorEquipo>> nominas;
	private List<PartidoExterno> partidos;

	public Competencia(String nombre, String entidadOrganizadora, int temporada, DeporteEquipo deporte,
			Categoria categoria) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre de la competencia es obligatorio.");
		}
		if (entidadOrganizadora == null || entidadOrganizadora.isBlank()) {
			throw new IllegalArgumentException("La entidad organizadora es obligatoria.");
		}
		if (temporada <= 0) {
			throw new IllegalArgumentException("La temporada debe ser un año válido.");
		}
		if (deporte == null || categoria == null) {
			throw new IllegalArgumentException("La competencia necesita deporte y categoría.");
		}
		this.nombre = nombre;
		this.entidadOrganizadora = entidadOrganizadora;
		this.temporada = temporada;
		this.deporte = deporte;
		this.categoria = categoria;
		this.nominas = new LinkedHashMap<Equipo, List<JugadorEquipo>>();
		this.partidos = new ArrayList<PartidoExterno>();
	}

	public boolean estaInscrito(Equipo equipo) {
		return nominas.containsKey(equipo);
	}

	public boolean estaEnNomina(Equipo equipo, JugadorEquipo jugador) {
		List<JugadorEquipo> nomina = nominas.get(equipo);
		return nomina != null && nomina.contains(jugador);
	}

	public void inscribirEquipo(Equipo equipo, List<JugadorEquipo> nomina) {
		if (equipo == null || nomina == null) {
			throw new IllegalArgumentException("Equipo y nómina son obligatorios.");
		}
		if (estaInscrito(equipo)) {
			throw new IllegalArgumentException("El equipo ya está inscrito en la competencia.");
		}
		if (!equipo.estaActivo()) {
			throw new IllegalArgumentException("El equipo está inactivo.");
		}
		if (!equipo.getDeporte().equals(deporte)) {
			throw new IllegalArgumentException("El equipo es de otro deporte.");
		}
		if (equipo.getCategoria() != categoria) {
			throw new IllegalArgumentException("El equipo es de otra categoría.");
		}
		if (!equipo.cumpleMinimoParaJugar()) {
			throw new IllegalArgumentException("El equipo no cumple el mínimo de jugadores para jugar.");
		}
		if (new HashSet<JugadorEquipo>(nomina).size() != nomina.size()) {
			throw new IllegalArgumentException("La nómina tiene jugadores repetidos.");
		}
		for (JugadorEquipo jugador : nomina) {
			if (jugador == null) {
				throw new IllegalArgumentException("La nómina contiene un jugador nulo.");
			}
			if (!equipo.tieneJugador(jugador)) {
				throw new IllegalArgumentException("Un jugador de la nómina no pertenece al equipo.");
			}
			if (!jugador.estaHabilitado()) {
				throw new IllegalArgumentException("Un jugador de la nómina no está habilitado.");
			}
		}
		if (nomina.size() < deporte.getMinimoJugadoresHabilitados()) {
			throw new IllegalArgumentException("La nómina no alcanza el mínimo de jugadores habilitados.");
		}
		nominas.put(equipo, new ArrayList<JugadorEquipo>(nomina));
	}

	public void registrarPartido(PartidoExterno partido) {
		if (partido == null) {
			throw new IllegalArgumentException("El partido es obligatorio.");
		}
		if (partido.getCompetencia() != this) {
			throw new IllegalArgumentException("El partido no pertenece a esta competencia.");
		}
		if (partidos.contains(partido)) {
			throw new IllegalStateException("El partido ya está registrado en la competencia.");
		}
		Equipo equipo = partido.getEquipoClub();
		if (!estaInscrito(equipo)) {
			throw new IllegalArgumentException("El equipo del partido no está inscrito.");
		}
		for (Jugador jugador : partido.getParticipantes()) {
			if (!(jugador instanceof JugadorEquipo) || !estaEnNomina(equipo, (JugadorEquipo) jugador)) {
				throw new IllegalArgumentException("Un jugador del partido no está en la nómina.");
			}
		}
		partido.validar();
		partidos.add(partido);
	}

	protected Map<String, Integer> acumularPuntos() {
		Map<String, Integer> puntos = new LinkedHashMap<String, Integer>();
		for (Equipo e : nominas.keySet()) {
			puntos.put(e.getNombre(), 0);
		}
		for (PartidoExterno p : partidos) {
			if (!p.estaJugado()) {
				continue;
			}
			int propios = p.getMarcadorA();
			int rival = p.getMarcadorB();
			puntos.merge(p.getEquipoClub().getNombre(), deporte.puntosPara(propios, rival), Integer::sum);
			puntos.merge(p.getInstitucionRival(), deporte.puntosPara(rival, propios), Integer::sum);
		}
		return puntos;
	}

	protected Map<String, Integer> ordenarPorPuntos(Map<String, Integer> puntos) {
		Map<String, Integer> ordenado = new LinkedHashMap<String, Integer>();
		puntos.entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
				.forEach(e -> ordenado.put(e.getKey(), e.getValue()));
		return ordenado;
	}

	public int acumuladoDe(JugadorEquipo jugador, String estadistica) {
		if (jugador == null) {
			throw new IllegalArgumentException("El jugador es obligatorio.");
		}
		if (estadistica == null || estadistica.isBlank()) {
			throw new IllegalArgumentException("La estadística es obligatoria.");
		}
		int total = 0;
		for (PartidoExterno p : partidos) {
			if (!p.estaJugado()) {
				continue;
			}
			for (EstadisticasPartido e : p.getEstadisticas()) {
				if (e.getJugador() == jugador) {
					total += e.getValor(estadistica);
				}
			}
		}
		return total;
	}

	public Map<JugadorEquipo, Integer> clasificacionPor(String estadistica) {
		if (estadistica == null || estadistica.isBlank()) {
			throw new IllegalArgumentException("La estadística es obligatoria.");
		}
		Map<JugadorEquipo, Integer> totales = new LinkedHashMap<JugadorEquipo, Integer>();
		for (List<JugadorEquipo> nomina : nominas.values()) {
			for (JugadorEquipo j : nomina) {
				totales.put(j, 0);
			}
		}
		for (PartidoExterno p : partidos) {
			if (!p.estaJugado()) {
				continue;
			}
			for (EstadisticasPartido e : p.getEstadisticas()) {
				if (e.getJugador() instanceof JugadorEquipo) {
					totales.merge((JugadorEquipo) e.getJugador(), e.getValor(estadistica), Integer::sum);
				}
			}
		}
		Map<JugadorEquipo, Integer> ordenado = new LinkedHashMap<JugadorEquipo, Integer>();
		totales.entrySet().stream()
				.sorted(Map.Entry.<JugadorEquipo, Integer>comparingByValue(Comparator.reverseOrder()))
				.forEach(e -> ordenado.put(e.getKey(), e.getValue()));
		return ordenado;
	}

	public abstract Map<String, Integer> calcularTabla();

	public String getNombre() {
		return nombre;
	}

	public String getEntidadOrganizadora() {
		return entidadOrganizadora;
	}

	public int getTemporada() {
		return temporada;
	}

	public DeporteEquipo getDeporte() {
		return deporte;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public List<Equipo> getEquiposInscritos() {
		return new ArrayList<Equipo>(nominas.keySet());
	}

	public List<JugadorEquipo> getNomina(Equipo equipo) {
		List<JugadorEquipo> nomina = nominas.get(equipo);
		if (nomina == null) {
			throw new IllegalArgumentException("El equipo no está inscrito en la competencia.");
		}
		return new ArrayList<JugadorEquipo>(nomina);
	}

	public List<PartidoExterno> getPartidos() {
		return new ArrayList<PartidoExterno>(partidos);
	}
}