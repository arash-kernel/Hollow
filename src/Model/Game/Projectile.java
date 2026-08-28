package Model.Game;

public abstract class Projectile extends Entity{
	public Projectile(double x, double y, Room room) {
		super(x, y, room);
	}
	@Override
	public void move(){
		position.add(speed);
	}
}
