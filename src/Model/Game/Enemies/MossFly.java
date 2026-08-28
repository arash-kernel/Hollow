package Model.Game.Enemies;

import Model.Game.Entity;
import Model.Game.Knight.Knight;
import Model.Game.Line;
import Model.Game.Room;
import Model.Game.Units;
import Model.Game.Vector2D;
import View.Animations;

import java.awt.*;

public class MossFly extends Entity implements GettingHit{
	private static Animations animations = new Animations();

	static {
		String[] animNames = {
				"Appear", "DeathAir", "DeathLand", "Fly", "TurnToFly"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/Enemies/MossFlyAnimations/");
		}

		
		String[] soundNames = {"Hit", "Death", "Appear"};
		for (String name : soundNames) {
			animations.addSound(name, "src/Model/Game/Enemies/MossFlyAnimations/Sounds/");
		}
	}

	private int tickCounter = 0;
	private Vector2D originPos;
	private Vector2D hoverTarget;
	private int hoverTimer = 0;

	public MossFly(double x, double y, Room room) {
		super(x, y, room);
		width = 50;
		height = 20;
		state = "Hide";
		room.getEntities().add(this);
		originPos = new Vector2D(x, y);
		hp = 10; 
		hold=6;
	}

	@Override
	public void paint(Graphics g) {
		String animName = state;

		
		if (state.equals("Hide")) {
			animName = "Appear";
		} else if (state.equals("Fly")) {
			animName = "Fly"; 
		}

		if (isFlipped) {
			animations.paintFlipped(g, (int) position.x, (int) position.y, (int) width, (int) height, animName, frame);
		} else {
			animations.paint(g, (int) position.x, (int) position.y, (int) width, (int) height, animName, frame);
		}
	}

	@Override
	public void movements() {
		
		tickCounter++;
		if (tickCounter >= hold) {
			frame++;
			tickCounter = 0;
		}

		
		if (hp <= 0 && !state.startsWith("Death")) {
			die();
		}

		Vector2D knightCenter = getKnightCenter();
		Vector2D myCenter = getCenter();
		double distToKnight = getDistance(myCenter, knightCenter);

		
		switch (state) {
			case "Hide":
				
				if (frame > 1) {
					frame = 0;
				}
				
				if (distToKnight < 0.75 * Units.LENGTH.number) {
					state = "Appear";
					frame = 0;
					animations.playSound("Appear"); 
				}
				break;

			case "Appear":
				
				if (frame == 3 && height != 50) {
					height = 50;
					width = 50;
					position.y -= 30; 
				}

				if (frame >= getAnimSize("Appear")) {
					state = "Fly";
					frame = 0;
				}
				break;

			case "Fly":
				
				if (frame >= getAnimSize("Fly")) frame = 0;

				boolean hasLineOfSight = checkLineOfSight(knightCenter);

				if (distToKnight < 1.5 * Units.LENGTH.number && hasLineOfSight) {
					
					Vector2D dir = new Vector2D(knightCenter.x - myCenter.x, knightCenter.y - myCenter.y);
					dir.normal();
					
					dir.multiply(0.25 * Units.LENGTH.number);
					speed.x = dir.x;
					speed.y = dir.y;
				} else {
					
					if (hoverTarget == null || getDistance(myCenter, hoverTarget) < 10 || hoverTimer <= 0) {
						hoverTarget = new Vector2D(originPos.x + (Math.random() - 0.5) * 200,
								originPos.y + (Math.random() - 0.5) * 200);
						hoverTimer = 120; 
					}
					hoverTimer--;
					Vector2D dir = new Vector2D(hoverTarget.x - myCenter.x, hoverTarget.y - myCenter.y);
					dir.normal();
					
					dir.multiply(0.1 * Units.LENGTH.number);
					speed.x = dir.x;
					speed.y = dir.y;
				}

				
				
				if (speed.x > 0 && !isFlipped) {
					
					state = "TurnToFly";
					frame = 0;
				} else if (speed.x < 0 && isFlipped) {
					
					state = "TurnToFly";
					frame = 0;
				}
				break;

			case "TurnToFly":
				speed.x = 0;
				speed.y = 0; 
				if (frame >= getAnimSize("TurnToFly")) {
					isFlipped = !isFlipped;
					state = "Fly";
					frame = 0;
				}
				break;

			case "DeathAir":
				
				speed.y += 3.5 * Units.LENGTH.number * Units.TICK.number;

				if (frame >= getAnimSize("DeathAir")) {
					frame = getAnimSize("DeathAir") - 1; 
				}

				
				if (isOnGround() && speed.y >= 0) {
					state = "DeathLand";
					frame = 0;
					speed.x = 0;
					speed.y = 0;
				}
				break;

			case "DeathLand":
				speed.x = 0;
				speed.y = 0;
				if (frame >= getAnimSize("DeathLand")) {
					frame = getAnimSize("DeathLand") - 1; 
				}
				break;
		}

		
		if (knockbackFrames > 0) {
			speed.x += activeKnockback.x;
			speed.y += activeKnockback.y;
			knockbackFrames--;
		}
		doDamage();

	}

	

	public void die() {
		animations.playSound("Death"); 
		state = "DeathAir";
		frame = 0;
		width=30;
		height=30;
		room.save.killedFly=true;
		room.save.totalEnemyKilled++;
	}

	private int getAnimSize(String name) {
		if (animations.animations.containsKey(name)) {
			return animations.animations.get(name).size();
		}
		return 1;
	}

	private Vector2D getCenter() {
		return new Vector2D(position.x + width / 2, position.y + height / 2);
	}

	private Vector2D getKnightCenter() {
		if (room.getKnight() == null) return new Vector2D(0, 0);
		return new Vector2D(room.getKnight().getPosition().x + room.getKnight().getWidth() / 2,
				room.getKnight().getPosition().y + room.getKnight().getHeight() / 2);
	}

	private double getDistance(Vector2D v1, Vector2D v2) {
		return Math.sqrt(Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2));
	}

	private boolean isOnGround() {
		
		Vector2D footPos = new Vector2D(position.x, position.y + 1);
		for (Line l : room.getBoundaries()) {
			if (l.isFloor() && l.intersect(footPos, width, height)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkLineOfSight(Vector2D target) {
		Vector2D start = getCenter();
		for (Line l : room.getBoundaries()) {
			
			if (rayIntersectsSegment(start.x, start.y, target.x, target.y, l.x1, l.y1, l.x2, l.y2)) {
				return false; 
			}
		}
		return true;
	}

	private boolean rayIntersectsSegment(double x1, double y1, double x2, double y2,
										 double x3, double y3, double x4, double y4) {
		double den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (den == 0) return false;
		double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
		double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / den;
		return (t >= 0 && t <= 1) && (u >= 0 && u <= 1);
	}
	private Vector2D activeKnockback = new Vector2D(0, 0);
	private int knockbackFrames = 0;
	@Override
	public void takeDamage(int damage, Vector2D knockback) {
		animations.playSound("Hit"); 
		this.hp -= damage;
		if(hp<=0)
			return;
		
		this.activeKnockback = new Vector2D(knockback.x / 6, knockback.y / 6);
		this.knockbackFrames = 6;
	}
	@Override
	public void doDamage() {
		Knight knight = room.getKnight();

		
		if (knight == null || state.startsWith("Death") || hp <= 0) {
			return;
		}

		
		double eX = position.x + 2;
		double eY = position.y + 2;
		double eW = width - 4;
		double eH = height - 4;

		
		double kX = knight.getPosition().x + 5;
		double kY = knight.getPosition().y + 10;
		double kW = knight.getWidth() - 10;
		double kH = knight.getHeight() - 10;

		
		if (eX < kX + kW && eX + eW > kX && eY < kY + kH && eY + eH > kY) {
			knight.takeDamage(1);
		}
	}

}