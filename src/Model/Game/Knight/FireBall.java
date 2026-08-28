package Model.Game.Knight;

import Model.Game.*;
import Model.Game.Enemies.GettingHit;
import View.Animations;

import java.awt.*;
import java.util.ArrayList;

public class FireBall extends Entity {
	private static Animations animations = new Animations();
	static {
		String[] animNames = {
				"SoulBall", "ShadowBall"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/Knight/FireBallAnimations/");
		}
		
		animations.addSound("Hit", "src/Model/Game/Knight/FireBallAnimations/Sounds/");
	}

	private int damage;
	private boolean upgraded = false;
	private int hold = 0;

	
	private int animationFramesPassed = 0;

	
	private ArrayList<Entity> enemiesHit = new ArrayList<>();

	public FireBall(double x, double y, boolean upgraded, Room room) {
		super(x, y, room);
		this.room = room;
		this.upgraded = upgraded;
		state = "SoulBall";
		frame = 0;
		damage = 13;
		height = 70;
		width = 140;
		if (upgraded) {
			state = "ShadowBall";
			damage = 20;
		}
	}

	@Override
	public void paint(Graphics g) {
		if (!isFlipped)
			animations.paint(g, position.getIntX()-30, position.getIntY()-30, 240,120, state, frame);
		else
			animations.paintFlipped(g, position.getIntX()-70, position.getIntY()-30, 240,120, state, frame);
	}

	@Override
	public void movements() {
		
		if (isFlipped) {
			for (Line l : room.getBoundaries()) {
				Vector2D p = new Vector2D(position.x, position.y);
				if (l.intersect(p, width / 2, height)) {
					room.getEntities().remove(this);
					return;
				}
			}
		} else {
			for (Line l : room.getBoundaries()) {
				Vector2D p = new Vector2D(position.x + width / 2, position.y);
				if (l.intersect(p, width / 2, height)) {
					room.getEntities().remove(this);
					return;
				}
			}
		}

		
		hold++;
		if (hold == 4) {
			frame++;
			hold = 0;
			animationFramesPassed++;

			
			if (animationFramesPassed % 5 == 0) {
				enemiesHit.clear();
			}
		}

		
		if (frame == 4 && !upgraded) {
			frame = 2;
		}
		if (frame == 6 && upgraded) {
			frame = 3;
		}

		
		if (!isFlipped)
			speed.setX(3 * Units.LENGTH.number);
		else
			speed.setX(-3 * Units.LENGTH.number);

		
		Hit(room);
	}

	@Override
	public void move() {
		speed.multiply(Units.TICK.number);
		position.add(speed);
	}

	
	public void Hit(Room room) {
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

					
					double kbForceX = 0.8 * Units.LENGTH.number;
					Vector2D knockback = new Vector2D(isFlipped ? -kbForceX : kbForceX, 0);

					((GettingHit) e).takeDamage(damage, knockback);
					enemiesHit.add(e);
				}
			}
		}
	}
}