package club.modelo.enums;

public enum Nivel{
	PRINCIPIANTE,
    INTERMEDIO,
    AVANZADO;

	public boolean esCompatibleCon(Nivel otro) {
		return Math.abs(this.ordinal() - otro.ordinal()) <= 1;
	}

}