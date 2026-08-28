package Model.Game.Enemies;

import Model.Game.Projectile;
import Model.Game.Room;
import View.Animations;

import java.awt.*;

public class LaserSpawn extends Projectile {

	private static Animations animations = new Animations();

	static {
		animations.addAnimation("LaserCircle", "src/Model/Game/Enemies/LaserSpawnAnimations/");
	}
	private int lifeTime=60;
	private int ticker=0;
	public LaserSpawn(double x, double y, Room room) {
		super(x, y, room);
		width=50;
		height=50;
		hold=6;
		state="LaserCircle";
	}

	@Override
	public void paint(Graphics g) {
		animations.paintFlipped(g, (int) position.x-(int)width/2, (int) position.y-(int)height/2, (int) width, (int) height, state, frame);
	}

	@Override
	public void movements() {
		lifeTime--;
		ticker++;
		if(ticker==hold){
			ticker=0;
			frame++;
		}
		if(lifeTime==0){
			room.getProjectiles().remove(this);
			return;
		}

	}
}
