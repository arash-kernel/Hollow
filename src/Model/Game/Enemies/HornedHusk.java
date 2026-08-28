package Model.Game.Enemies;

import Model.Game.Entity;
import Model.Game.Knight.Knight;
import Model.Game.Line;
import Model.Game.Room;
import Model.Game.Units;
import Model.Game.Vector2D;
import View.Animations;

import java.awt.*;

public class HornedHusk extends Entity implements GettingHit{
	private static Animations animations = new Animations();
	private int justTurned = 0;

	static {
		String[] animNames = {
				"Walk", "Turn", "Death Air", "Death Land", "Idle", "Attack Anticipate", "Attack Lunge"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/Enemies/HornedHuskAnimations/");
		}

		
		String[] soundNames = {"Hit", "Death", "Charge"};
		for (String name : soundNames) {
			animations.addSound(name, "src/Model/Game/Enemies/HornedHuskAnimations/Sounds/");
		}
	}

	private int tickCounter = 0;
	private int idleTimer = 0;
	private final int IDLE_THRESHOLD = 200; 
	private final int WAKE_THRESHOLD = 100; 

	private final double WALK_SPEED = 0.5 * Units.LENGTH.number;
	private final double LUNGE_SPEED = 2.0 * Units.LENGTH.number;

	public HornedHusk(double x, double y, Room room) {
		super(x, y, room);
		width = 120;  
		height = 120; 
		state = "Walk";
		room.getEntities().add(this);
		hp = 20;     
		hold = 6;    
	}

	@Override
	public void paint(Graphics g) {
		
		String animName = state;
		if (state.equals("Attack Anticipate Reverse")) {
			animName = "Attack Anticipate";
		}

		if (isFlipped) {
			animations.paintFlipped(g, (int) position.x, (int) position.y, (int) width, (int) height, animName, frame);
		} else {
			animations.paint(g, (int) position.x, (int) position.y, (int) width, (int) height, animName, frame);
		}
	}

	@Override
	public void movements() {
		
		if (justTurned > 0) {
			justTurned--;
		}

		
		tickCounter++;
		if (tickCounter >= hold) {
			if (state.equals("Attack Anticipate Reverse")) {
				frame--; 
			} else {
				frame++; 
			}
			tickCounter = 0;
		}

		
		if (hp <= 0 && !state.startsWith("Death")) {
			die();
		}

		
		speed.y += 3.5 * Units.LENGTH.number * Units.TICK.number;

		
		switch (state) {
			case "Walk":
				if (frame >= getAnimSize("Walk")) {
					frame = 0;
				}

				speed.x = isFlipped ? WALK_SPEED : -WALK_SPEED;

				WalkerTurnAround wta = getTurnAround();

				if (wta != null && justTurned <= 0) {
					
					if (isFlipped != wta.right) {
						state = "Turn";
						frame = 0;
						justTurned = 15;
					}
				} else if (canSeeKnight()) {
					state = "Attack Anticipate";
					frame = 0;
					speed.x = 0;
				} else {
					idleTimer++;
					if (idleTimer >= IDLE_THRESHOLD) {
						state = "Idle";
						frame = 0;
						idleTimer = 0;
						speed.x = 0;
					}
				}
				break;

			case "Idle":
				if (frame >= getAnimSize("Idle")) {
					frame = 0;
				}
				speed.x = 0;
				idleTimer++;

				if (canSeeKnight()) {
					state = "Attack Anticipate";
					frame = 0;
					idleTimer = 0;
				} else if (idleTimer >= WAKE_THRESHOLD) {
					state = "Walk";
					frame = 0;
					idleTimer = 0;
				}
				break;

			case "Turn":
				speed.x = 0;

				if (frame >= getAnimSize("Turn")) {
					isFlipped = !isFlipped;
					state = "Walk";
					frame = 0;
					justTurned = 15; 
				}
				break;

			case "Attack Anticipate":
				speed.x = 0;
				if (frame >= getAnimSize("Attack Anticipate")) {
					state = "Attack Lunge";
					frame = 0;
					animations.playSound("Charge"); 
				}
				break;

			case "Attack Lunge":
				if (frame >= getAnimSize("Attack Lunge")) {
					frame = 0; 
				}
				speed.x = isFlipped ? LUNGE_SPEED : -LUNGE_SPEED;

				wta = getTurnAround();

				if (wta != null && justTurned <= 0) {
					state = "Attack Anticipate Reverse";
					frame = getAnimSize("Attack Anticipate") - 1;
					justTurned = 20;
				}
				break;

			case "Attack Anticipate Reverse":
				int maxFrame = getAnimSize("Attack Anticipate") - 1;
				if (maxFrame <= 0) maxFrame = 1; 

				
				double currentC = isFlipped ? LUNGE_SPEED : -LUNGE_SPEED;
				speed.x = currentC * ((double) frame / maxFrame);

				
				if (frame <= 0) {
					state = "Turn";
					frame = 0;
					speed.x = 0;
					justTurned = 15;
				}
				break;

			case "Death Air":
				if (frame >= getAnimSize("Death Air")) {
					frame = getAnimSize("Death Air") - 1;
				}
				if (isOnGround() && speed.y >= 0) {
					state = "Death Land";
					frame = 0;
					speed.x = 0;
					speed.y = 0;
					position.y+=30;
				}
				break;

			case "Death Land":
				speed.x = 0;
				speed.y = 0;
				if (frame >= getAnimSize("Death Land")) {
					frame = getAnimSize("Death Land") - 1;
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
		state = "Death Air";
		frame = 0;
		room.save.killedHorn=true;
		room.save.totalEnemyKilled++;
	}

	private boolean canSeeKnight() {
		if (room.getKnight() == null) return false;

		Vector2D knightCenter = getKnightCenter();
		Vector2D myCenter = getCenter();

		
		if (!isFlipped && knightCenter.x > myCenter.x) return false;
		if (isFlipped && knightCenter.x < myCenter.x) return false;

		
		if (Math.abs(knightCenter.y - myCenter.y) > height) return false;

		
		return !isLineOfSightBlocked(myCenter, knightCenter);
	}

	private boolean isLineOfSightBlocked(Vector2D start, Vector2D end) {
		
		for (Line l : room.getBoundaries()) {
			if (lineIntersectsLine(start.x, start.y, end.x, end.y, l.x1, l.y1, l.x2, l.y2)) {
				return true;
			}
		}
		
		for (WalkerTurnAround wta : room.getTurnArounds()) {
			if (lineIntersectsRect(start.x, start.y, end.x, end.y, wta.position.x, wta.position.y, wta.width, wta.height)) {
				return true;
			}
		}
		return false;
	}

	
	private boolean lineIntersectsLine(double x1, double y1, double x2, double y2,
									   double x3, double y3, double x4, double y4) {
		double den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (den == 0) return false;
		double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
		double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / den;
		return (t >= 0 && t <= 1) && (u >= 0 && u <= 1);
	}

	
	private boolean lineIntersectsRect(double x1, double y1, double x2, double y2,
									   double rx, double ry, double rw, double rh) {
		if (lineIntersectsLine(x1, y1, x2, y2, rx, ry, rx + rw, ry)) return true;
		if (lineIntersectsLine(x1, y1, x2, y2, rx, ry, rx, ry + rh)) return true;
		if (lineIntersectsLine(x1, y1, x2, y2, rx + rw, ry, rx + rw, ry + rh)) return true;
		if (lineIntersectsLine(x1, y1, x2, y2, rx, ry + rh, rx + rw, ry + rh)) return true;

		
		return (x1 >= rx && x1 <= rx + rw && y1 >= ry && y1 <= ry + rh) ||
				(x2 >= rx && x2 <= rx + rw && y2 >= ry && y2 <= ry + rh);
	}

	private WalkerTurnAround getTurnAround() {
		for (WalkerTurnAround wta : room.getTurnArounds()) {
			if (wta.intersects(position, width, height)) {
				return wta;
			}
		}
		return null;
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

	private boolean isOnGround() {
		Vector2D footPos = new Vector2D(position.x, position.y + 1);
		for (Line l : room.getBoundaries()) {
			if (l.isFloor() && l.intersect(footPos, width, height)) {
				return true;
			}
		}
		return false;
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
		if(hp<=0)
			this.activeKnockback =new Vector2D(knockback.x/2,knockback.y/2);
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