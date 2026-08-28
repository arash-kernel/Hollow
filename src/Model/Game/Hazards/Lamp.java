package Model.Game.Hazards;

import Model.Game.Entity;
import Model.Game.Room;
import View.Animations;

import java.awt.*;

public class Lamp extends Entity {
	private static Animations animations = new Animations();
	private int ticker=0;
	static {
		animations.addAnimation("Health_Plant", "src/Model/Game/Hazards/HazardAnimations/");
	}
	public Lamp(double x, double y, Room room) {
		super(x, y, room);
		room.getEntities().add(this);
		width=200;
		height=290;
	}

	@Override
	public void paint(Graphics g) {
		animations.paint(g, position.getIntX(), position.getIntY(), 200, 300, "Health_Plant", frame);	}

	@Override
	public void movements() {
		ticker++;
		if(ticker>=hold*10){
			ticker=0;
			frame++;
		}
		speed.y=100;
	}
}
