package Model.Game;

public enum Units {
	LENGTH(200),
	DASH(19),
	HOLD(4),
	TICK(0.024);

	public double number;
	Units (double number){
		this.number=number;
	}
}
