package Model.Game.Enemies;

import Model.Game.Entity;
import Model.Game.Knight.Knight;
import Model.Game.Line;
import Model.Game.Room;
import Model.Game.Units;
import Model.Game.Vector2D;
import View.Animations;

import java.awt.*;

public class Crystallized extends Entity implements GettingHit{
	private static Animations animations = new Animations();

	static {
		String[] animNames = {
				"Shoot", "Run", "Evade", "Idle", "Turn", "Death Air", "Death Land"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/Enemies/CrystallizedAnimations/");
		}

		
		String[] soundNames = {"Hit", "Death", "Laser"};
		for (String name : soundNames) {
			animations.addSound(name, "src/Model/Game/Enemies/CrystallizedAnimations/Sounds/");
		}
	}
	private String stateAfterTurn = "Idle";
	private int tickCounter = 0;
	private int runTimer = 0;
	private int evadeFramesCounter = 0;
	private boolean hasShotLaser = false; 

	
	private Vector2D lockedTargetPos = new Vector2D(0, 0);

	public Crystallized(double x, double y, Room room) {
		super(x, y, room);
		width = 120;
		height = 120;
		state = "Idle";
		room.getEntities().add(this);
		hp = 30;
		hold = 8;
	}

	@Override
	public void paint(Graphics g) {
		if (isFlipped) {
			animations.paintFlipped(g, (int) position.x, (int) position.y, (int) width, (int) height, state, frame);
		} else {
			animations.paint(g, (int) position.x, (int) position.y, (int) width, (int) height, state, frame);
		}
	}

	@Override
	public void movements() {
		
		int currentHold = hold;

		
		if (state.equals("Shoot") && (frame == 2 || frame == 3)) {
			currentHold = 30;
		}

		tickCounter++;
		if (tickCounter >= currentHold) {
			frame++;
			tickCounter = 0;

			if (state.equals("Evade")) {
				evadeFramesCounter++;
			}
		}

		
		if (hp <= 0 && !state.startsWith("Death")) {
			die();
		}

		
		if (!isOnGround() && !state.equals("Death Land")) {
			speed.y += 3.5 * Units.LENGTH.number * Units.TICK.number;
		}

		Vector2D knightCenter = getKnightCenter();
		Vector2D myCenter = getCenter();
		double distToKnight = getDistance(myCenter, knightCenter);
		
		if (runTimer > 0 && (state.equals("Run") || (state.equals("Turn") && stateAfterTurn.equals("Run")))) {
			runTimer--;
		}
		
		switch (state) {
			case "Turn":
				speed.x = 0;

				if (frame >= getAnimSize("Turn")) {
					isFlipped = !isFlipped;

					if (stateAfterTurn.equals("Run") && runTimer <= 0) {
						state = "Idle";
					} else {
						state = stateAfterTurn;
					}

					frame = 0;
					tickCounter = 0;
				}
				break;
			case "Idle":
				speed.x = 0;
				if (frame >= getAnimSize("Idle")) frame = 0;

				
				if (distToKnight < 2.0 * Units.LENGTH.number) {
					double dx = knightCenter.x - myCenter.x;
					double dy = knightCenter.y - myCenter.y;

					double facingDir = isFlipped ? 1.0 : -1.0;
					double angleToKnight = Math.toDegrees(Math.atan2(dy, dx * facingDir));

					
					if (angleToKnight >= -60 && angleToKnight <= 20 && checkLineOfSight(knightCenter)) {
						lockedTargetPos.x = knightCenter.x;
						lockedTargetPos.y = knightCenter.y;

						boolean shouldFaceRight = knightCenter.x > myCenter.x;

						if (shouldFaceRight != isFlipped) {
							evadeFramesCounter = 0;
							beginTurn("Evade");
						} else {
							state = "Evade";
							frame = 0;
							tickCounter = 0;
							evadeFramesCounter = 0;
						}
					}
				}
				break;

			case "Evade":
				double evadeDir = isFlipped ? -1.0 : 1.0;
				speed.x = evadeDir * 1.0 * Units.LENGTH.number*(1-frame/7.0);

				if (evadeFramesCounter <= 4) {
					lockedTargetPos.x = knightCenter.x;
					lockedTargetPos.y = knightCenter.y;
				}

				if (evadeFramesCounter >= 7) {
					state = "Shoot";
					frame = 0;
					tickCounter = 0;
					speed.x = 0;
					hasShotLaser = false; 
				}
				break;

			case "Shoot":
				speed.x = 0;

				
				if (frame == 2 && !hasShotLaser) {
					shootLaser(lockedTargetPos);
					hasShotLaser = true; 
				}

				if (frame >= getAnimSize("Shoot")) {
					state = "Run";
					frame = 0;
					tickCounter = 0;
					runTimer = (int) (10.0 / Units.TICK.number);
				}
				break;

			case "Run":
				if (frame >= getAnimSize("Run")) frame = 0;
				runTimer--;
				if (runTimer <= 0) {
					state = "Idle";
					frame = 0;
					tickCounter = 0;
					speed.x = 0;
					break;
				}
				double moveDir = knightCenter.x > myCenter.x ? 1.0 : -1.0;

				if (Math.abs(knightCenter.x - myCenter.x) < 4) {
					speed.x = 0;
				} else {

					boolean shouldFaceRight = moveDir > 0;

					if (shouldFaceRight != isFlipped) {
						beginTurn("Run");
						break;
					}

					speed.x = moveDir * 0.5 * Units.LENGTH.number;
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

	

	private void shootLaser(Vector2D targetPos) {
		animations.playSound("Laser"); 
		double spawnX = isFlipped ? position.x + width -width/3 : position.x+width/3;
		double spawnY = position.y + 9*height/20;

		Laser laser = new Laser(spawnX, spawnY, room, targetPos);
		if (room.getProjectiles() != null) {
			room.getProjectiles().add(new LaserSpawn(spawnX,spawnY,room));
			room.getProjectiles().add(laser);
		}
	}

	public void die() {
		animations.playSound("Death"); 
		state = "Death Air";
		frame = 0;
		room.save.killedLaser=true;
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

	private void beginTurn(String nextState) {
		stateAfterTurn = nextState;
		state = "Turn";
		frame = 0;
		tickCounter = 0;
		speed.x = 0;
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