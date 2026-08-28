package Model.Game.Enemies;

import Model.Game.*;
import Model.Game.Knight.Knight;
import View.Animations;

import java.awt.*;

public class CrystalDart extends Projectile implements GettingHit {
	private static Animations animations = new Animations();

	static {
		
		animations.addAnimation("Dart", "src/Model/Game/Enemies/CrystalDartAnimations/");
	}

	private int tickCounter = 0;
	private double angle = 0;
	private int lifeTime = 300; 
	private double initialSpeedLength;

	public CrystalDart(double x, double y, Room room, Vector2D targetPos) {
		super(x, y, room);
		width = 20;
		height = 10;
		state = "Dart";
		hold = 4; 

		
		Vector2D dir = new Vector2D(targetPos.x - x, targetPos.y - y);
		dir.normal();

		
		dir.multiply(0.9 * Units.LENGTH.number);
		speed.x = dir.x;
		speed.y = dir.y;

		initialSpeedLength = speed.getLength();

		
		angle = Math.atan2(speed.y, speed.x);
	}

	@Override
	public void paint(Graphics g) {
		
		Graphics2D g2d = (Graphics2D) g.create();

		int cx = (int) (position.x + width / 2);
		int cy = (int) (position.y + height / 2);

		
		g2d.rotate(angle, cx, cy);

		
		animations.paint(g2d, (int) position.x, (int) position.y, (int) width*2, (int) height*2, state, frame);

		
		g2d.dispose();
	}

	@Override
	public void movements() {
		tickCounter++;
		if (tickCounter >= hold) {
			frame++;
			tickCounter = 0;
		}

		if (frame >= getAnimSize("Dart")) {
			frame = 0;
		}

		
		lifeTime--;
		if (lifeTime <= 0) {
			destroy();
		}

		
		if (speed.getLength() < initialSpeedLength * 0.98) {
			destroy();
		}

		
		doDamage();
	}

	@Override
	public void doDamage() {
		Knight knight = room.getKnight();

		if (knight == null) {
			return;
		}

		
		double dX = position.x;
		double dY = position.y;
		double dW = width;
		double dH = height;

		
		double kX = knight.getPosition().x + 5;
		double kY = knight.getPosition().y + 10;
		double kW = knight.getWidth() - 10;
		double kH = knight.getHeight() - 10;

		
		if (dX < kX + kW && dX + dW > kX && dY < kY + kH && dY + dH > kY) {
			knight.takeDamage(1);
			destroy(); 
		}
	}

	@Override
	public void takeDamage(int damage, Vector2D knockback) {
		
		destroy();
	}

	private void destroy() {
		if (room.getProjectiles() != null) {
			room.getProjectiles().remove(this);
		}
	}

	private int getAnimSize(String name) {
		if (animations.animations.containsKey(name)) {
			return animations.animations.get(name).size();
		}
		return 1;
	}

	@Override
	public void move() {
		
		onSteepSlope = false;
		Vector2D step = new Vector2D(speed.x, speed.y);
		step.multiply(Units.TICK.number);
		position.add(step);

		for (Line line : room.getBoundaries()) {
			if (line.intersect(position, width, height)) {
				Vector2D normal = line.normalRight();
				double penetration = getPenetrationDepth(line);

				
				if (penetration > 0 && Math.abs(normal.y) < 0.5) {
					double minY = Math.min(line.y1, line.y2);
					double maxY = Math.max(line.y1, line.y2);
					double entityTop = position.y;
					double entityBottom = position.y + height;

					double overlapY = Math.min(entityBottom - minY, maxY - entityTop);

					if (overlapY > 0 && overlapY < 4.0 && overlapY < penetration) {
						continue;
					}
				}
				

				if (penetration > 0) {
					Vector2D pushVector = new Vector2D(normal.x, normal.y);
					if(normal.y<=-0.9848)
						pushVector=new Vector2D(0,-1);
					pushVector.multiply(penetration);
					position.add(pushVector); 

					
					if (normal.y <= -0.9848) {

					}
					else if (normal.y > -0.9848 && normal.y < -0.05) {
						
						onSteepSlope = true;
						lastSlopeNormal = normal;

						Vector2D tangent = new Vector2D(-normal.y, +normal.x);
						if (tangent.y < 0) {
							tangent.x = -tangent.x;
							tangent.y = -tangent.y;
						}

						double slideVelocity = 40*Units.LENGTH.number;
						speed.x = tangent.x * slideVelocity;
						speed.y = tangent.y * slideVelocity;
					}
				}

				
				double dotSpeed = speed.dot(normal);
				if(normal.y<=-0.9848)
					dotSpeed=speed.dot(new Vector2D(0,-1));
				if (dotSpeed < 0) {
					Vector2D projection = new Vector2D(normal.x, normal.y);
					projection.multiply(dotSpeed);
					speed.x -= projection.x;
					speed.y -= projection.y;
				}
			}
		}
	}
}