package Model.Game.Knight;

import Model.Game.Entity;
import Model.Game.Room;
import Model.Game.Vector2D;
import Model.Game.Units;
import Model.Game.Enemies.GettingHit;
import View.Animations;

import java.awt.*;
import java.util.ArrayList;

public class Shriek extends Entity {
	private static Animations animations = new Animations();
	static {
		String[] animNames = {
				"SoulScream", "ShadowScream"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/Knight/ShriekAnimations/");
		}
		
		animations.addSound("Hit", "src/Model/Game/Knight/ShriekAnimations/Sounds/");
	}

	private int damage;
	private boolean upgraded = false;
	private int hold = 0;

	private ArrayList<Entity> enemiesHit = new ArrayList<>();

	public Shriek(double x, double y, boolean upgraded, Room room) {
		super(x, y);
		this.room = room;
		this.upgraded = upgraded;
		state = "SoulScream";
		frame = 0;
		damage = 13;
		width = 400;
		height = 400;
		if (upgraded) {
			state = "ShadowScream";
			damage = 20;
		}
	}

	@Override
	public void paint(Graphics g) {
		animations.paint(g, position.getIntX(), position.getIntY(), (int)width, (int)height, state, frame);
	}

	@Override
	public void movements() {
		hold++;
		if (hold == 4) {
			frame++;
			hold = 0;

			
			enemiesHit.clear();
		}
		if (frame == 13) {
			room.getEntities().remove(this);
			return;
		}

		
		Hit(room);
	}

	@Override
	public void move() {
	}

	
	public void Hit(Room room) {
		
		if (frame < 3 || frame > 6) {
			return;
		}

		for (Entity e : room.getEntities()) {
			if (e instanceof GettingHit && !enemiesHit.contains(e)) {

				
				boolean isIntersecting =
						this.position.x < e.getPosition().x + e.getWidth() &&
								this.position.x + this.width > e.getPosition().x &&
								this.position.y < e.getPosition().y + e.getHeight() &&
								this.position.y + this.height > e.getPosition().y;

				if (isIntersecting) {
					
					if (!e.getClass().getSimpleName().equals("Hazard")) {
						animations.playSound("Hit");
					}

					
					double kbForceY = -1.2 * Units.LENGTH.number;
					Vector2D knockback = new Vector2D(0, kbForceY);

					((GettingHit) e).takeDamage(damage, knockback);
					enemiesHit.add(e);
				}
			}
		}
	}
}