package Model.Game.Knight;

import Model.Game.Projectile;
import Model.Game.Room;
import Model.Game.Vector2D;
import View.Animations;

import java.awt.*;

public class SoulOrb extends Projectile {

	public static Animations animations = new Animations();
	static {
		String[] animNames = {
				"SoulBall", "ShadowBall"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/Knight/FireBallAnimations/");
		}
	}

	public SoulOrb(double x, double y, Room room) {
		super(x, y, room);
		room.getProjectiles().add(this);
	}

	@Override
	public void paint(Graphics g) {
		
	}

	@Override
	public void movements() {
		
		
		double targetWorldX = 109 + room.panel.camera.getOffsetX();
		double targetWorldY = 119 + room.panel.camera.getOffsetY();

		
		Vector2D targetHeading = new Vector2D(targetWorldX - position.x, targetWorldY - position.y);

		speed = Vector2D.rotateTowards(speed, targetHeading, 3);
		speed.multiply(1.03); 

		
		if(position.x < targetWorldX + 20 && position.x > targetWorldX - 20 &&
				position.y < targetWorldY + 20 && position.y > targetWorldY - 20) {
			room.getProjectiles().remove(this);
			room.getKnight().gainSoul(10);
		}
	}
}