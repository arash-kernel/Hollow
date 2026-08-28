package Model.Game;

import Model.Game.Enemies.GettingHit;
import View.Animations;

import java.awt.*;

public class SecretDoor extends Entity implements GettingHit {
	private static Animations animations=new Animations();
	static {
		String[] animNames = {
				"Idle"
		};
		for (String name : animNames) {
			
			animations.addAnimation(name, "src/Model/Game/SecretDoorAnimations/");
		}
		animations.addSound("found","src/Model/Game/SecretDoorAnimations/");
	}

	public SecretDoor(double x, double y, Room room) {
		super(x, y, room);
		width=453*1.5;
		height=175*1.7;
		room.getEntities().add(this);
		hp=3;
	}

	@Override
	public void paint(Graphics g) {
		String animName = "Idle";

		if (isFlipped) {
			animations.paintFlipped(g, (int) position.x, (int) position.y, (int) width, (int) height, animName, frame);
		} else {
			animations.paint(g, (int) position.x, (int) position.y, (int) width, (int) height, animName, frame);
		}
	}


	@Override
	public void movements() {

	}
	@Override
	public void move(){
		return;
	}

	@Override
	public void takeDamage(int damage, Vector2D knockback) {
		room.summonParticle(3, position.getIntX(), position.getIntY(),(int) width,(int)height,100,3);
		hp--;
		if(hp>0)
			return;
		room.getEntities().remove(this);
		animations.playSound("found");
	}

	@Override
	public void doDamage() {

	}
}
