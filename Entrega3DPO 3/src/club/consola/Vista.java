package club.consola;

import club.modelo.competencias.Competencia;
import club.modelo.deportes.Equipo;
import club.modelo.entrenamientos.Entrenamiento;
import club.modelo.instalaciones.ReservaCancha;
import club.modelo.partidos.Partido;
import club.modelo.partidos.PartidoExterno;
import club.modelo.partidos.PartidoInterno;
import club.modelo.partidos.PartidoPractica;
import club.modelo.personal.Empleado;
import club.modelo.personal.Entrenador;
import club.modelo.personal.Fisioterapeuta;
import club.modelo.personal.SolicitudCambioTurno;
import club.modelo.usuarios.Jugador;
import club.modelo.usuarios.JugadorEquipo;


final class Vista {

	private Vista() {
	}

	static String partido(Partido p) {
		String detalle = "";
		if (p instanceof PartidoInterno) {
			PartidoInterno pi = (PartidoInterno) p;
			detalle = pi.getEquipoA().getNombre() + " vs " + pi.getEquipoB().getNombre() + " (interno)";
		} else if (p instanceof PartidoExterno) {
			PartidoExterno pe = (PartidoExterno) p;
			detalle = pe.getEquipoClub().getNombre() + " vs " + pe.getInstitucionRival() + " en " + pe.getEscenario()
					+ (pe.esAmistoso() ? " (amistoso)" : " (" + pe.getCompetencia().getNombre() + ")");
		} else if (p instanceof PartidoPractica) {
			PartidoPractica pp = (PartidoPractica) p;
			detalle = pp.getJugadorA().getNombre() + " vs " + pp.getJugadorB().getNombre() + " (práctica, "
					+ pp.getModalidad() + ")";
		}
		String marcador = p.estaJugado() ? "marcador " + p.getMarcadorA() + " - " + p.getMarcadorB() : "por jugar";
		return p.getFecha() + " | " + p.getDeporte() + " | " + p.getCategoria() + " | " + detalle + " | " + marcador;
	}

	static String entrenamiento(Entrenamiento e) {
		return e.getFecha() + " " + e.getHora() + " (" + e.getDuracion() + " min) en "
				+ e.getInstalacion().getNombre();
	}

	static String reserva(ReservaCancha r) {
		return r.getFecha() + " " + r.getHoraInicio() + "-" + r.getHoraFin() + " | " + r.getInstalacion().getNombre()
				+ " | " + r.getJugador().getNombre() + " | " + r.getModalidad()
				+ (r.estaActiva() ? "" : " (cancelada)");
	}

	static String equipo(Equipo e) {
		return e.getNombre() + " [" + e.getDeporte() + ", " + e.getCategoria() + "]"
				+ (e.estaActivo() ? "" : " (inactivo)");
	}

	static String jugador(Jugador j) {
		return j + " - " + (j instanceof JugadorEquipo ? "de equipo" : "individual")
				+ (j.estaActivo() ? "" : " (baja)");
	}

	static String empleado(Empleado e) {
		String tipo = (e instanceof Entrenador) ? "Entrenador"
				: (e instanceof Fisioterapeuta) ? "Fisioterapeuta" : "Empleado tienda/cafetería";
		return tipo + ": " + e + (e.estaActivo() ? "" : " (baja)");
	}

	static String competencia(Competencia c) {
		return c.getNombre() + " " + c.getTemporada() + " | " + c.getEntidadOrganizadora() + " | " + c.getDeporte()
				+ " " + c.getCategoria() + " | " + c.getClass().getSimpleName();
	}

	static String solicitud(SolicitudCambioTurno s) {
		return s.getEmpleado() + ": " + s.getTurnoActual() + " -> " + s.getTurnoNuevo() + " | motivo: "
				+ s.getMotivo() + " | " + s.getEstado()
				+ (s.getRazonRechazo() != null ? " (" + s.getRazonRechazo() + ")" : "");
	}
}
