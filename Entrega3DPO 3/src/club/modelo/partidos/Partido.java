package club.modelo.partidos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import club.modelo.deportes.Deporte;
import club.modelo.deportes.DeporteEquipo;
import club.modelo.deportes.Registro;
import club.modelo.enums.Categoria;
import club.modelo.estadisticas.EstadisticasPartido;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;

public abstract class Partido {
	private LocalDate fecha;
	private Categoria categoria;
	private Deporte deporte;
	private int marcadorA;
	private int marcadorB;
	private boolean jugado;
	private Registro registro;
	private List<EstadisticasPartido> estadisticas;
	private List<Jugador> participantes;

	public Partido(LocalDate fecha, Categoria categoria, Deporte deporte) {
		if (fecha == null || categoria == null || deporte == null) {
			throw new IllegalArgumentException("El partido debe tener fecha, categoría y deporte.");
		}
		this.fecha = fecha;
		this.categoria = categoria;
		this.deporte = deporte;
		this.marcadorA = 0;
		this.marcadorB = 0;
		this.jugado = false;
		this.registro = new Registro();
		this.estadisticas = new ArrayList<EstadisticasPartido>();
		this.participantes = new ArrayList<Jugador>();
	}

	public void validar() {
		if (!registro.estaRegistrado()) {
			throw new IllegalStateException("El partido no está registrado.");
		}
		if (marcadorA < 0 || marcadorB < 0) {
			throw new IllegalStateException("Los marcadores no pueden ser negativos.");
		}
		if (jugado && participantes.isEmpty()) {
			throw new IllegalStateException("Un partido jugado debe tener participantes.");
		}
	}

	public void registrarResultado(int a, int b) {
		if (a < 0 || b < 0) {
			throw new IllegalArgumentException("Los marcadores no pueden ser negativos.");
		}
		if (!registro.estaRegistrado()) {
			throw new IllegalStateException("El partido no está registrado.");
		}
		if (jugado) {
			throw new IllegalStateException("El partido ya tiene resultado.");
		}
		if (a == b && deporte instanceof DeporteEquipo && !((DeporteEquipo) deporte).permiteEmpate()) {
			throw new IllegalArgumentException("Este deporte no permite empates.");
		}
		validar();
		this.marcadorA = a;
		this.marcadorB = b;
		this.jugado = true;
	}

	public void agregarParticipante(Jugador jugador) {
		if (jugador == null) {
			throw new IllegalArgumentException("El jugador no puede ser nulo.");
		}
		if (jugado) {
			throw new IllegalStateException("No se pueden agregar participantes a un partido ya jugado.");
		}
		if (participantes.contains(jugador)) {
			throw new IllegalArgumentException("El jugador ya participa en el partido.");
		}
		if (!jugador.estaActivo()) {
			throw new IllegalArgumentException("El jugador no está registrado como activo.");
		}
		if (!jugador.estaHabilitado()) {
			throw new IllegalArgumentException("El jugador está suspendido o lesionado.");
		}
		if (jugador instanceof JugadorEquipo && ((JugadorEquipo) jugador).getCategoria() != categoria) {
			throw new IllegalArgumentException("La categoría del jugador no coincide con la del partido.");
		}
		this.participantes.add(jugador);
		jugador.agregarPartido(this);
	}

	public void agregarEstadisticas(EstadisticasPartido nuevas) {
		if (nuevas == null) {
			throw new IllegalArgumentException("Las estadísticas no pueden ser nulas.");
		}
		if (nuevas.getPartido() != this) {
			throw new IllegalArgumentException("Las estadísticas son de otro partido.");
		}
		if (!participantes.contains(nuevas.getJugador())) {
			throw new IllegalArgumentException("El jugador de las estadísticas no participó en el partido.");
		}
		for (EstadisticasPartido e : estadisticas) {
			if (e.getJugador() == nuevas.getJugador()) {
				throw new IllegalStateException("El jugador ya tiene estadísticas en este partido.");
			}
		}
		this.estadisticas.add(nuevas);
	}

	public boolean estaActivo() {
		return registro.estaRegistrado();
	}

	public Registro getRegistro() {
		return registro;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public Deporte getDeporte() {
		return deporte;
	}

	public int getMarcadorA() {
		return marcadorA;
	}

	public int getMarcadorB() {
		return marcadorB;
	}

	public boolean estaJugado() {
		return jugado;
	}

	public List<EstadisticasPartido> getEstadisticas() {
		return Collections.unmodifiableList(estadisticas);
	}

	public List<Jugador> getParticipantes() {
		return Collections.unmodifiableList(participantes);
	}
}