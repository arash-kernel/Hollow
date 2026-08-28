package Model.Game.Enemies;

import Model.Game.Line;
import Model.Game.Projectile;
import Model.Game.Room;
import Model.Game.Vector2D;
import Model.Game.Knight.Knight;
import View.Animations;

import java.awt.*;

public class Laser extends Projectile implements GettingHit {
	private static Animations animations = new Animations();

	static {
		animations.addAnimation("Laser", "src/Model/Game/Enemies/LaserAnimations/");
	}

	private int tickCounter = 0;
	private double angle = 0;
	private int lifeTime = 60;

	public Laser(double spawnX, double spawnY, Room room, Vector2D targetPos) {
		super(spawnX, spawnY, room);

		this.height = 30;
		this.width = 700;  
		this.state = "Laser";
		this.hold = 6;

		
		
		double dy = targetPos.y - spawnY;
		double dx = targetPos.x - spawnX;
		angle = Math.atan2(dy, dx);

		this.speed.x = 0;
		this.speed.y = 0;
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();

		
		g2d.translate(position.x, position.y);

		
		g2d.rotate(angle);

		
		animations.paint(
				g2d,
				0,
				-(int)(height / 2),
				(int) width,
				(int) height,
				state,
				frame
		);

		g2d.dispose();
	}

	@Override
	public void movements() {
		tickCounter++;
		if (tickCounter >= hold) {
			frame++;
			tickCounter = 0;
		}

		if (frame >= getAnimSize("Laser")) {
			frame = 0;
		}

		lifeTime--;
		if (lifeTime <= 0) {
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

		
		double startX = position.x;
		double startY = position.y;
		double endX = position.x + Math.cos(angle) * width;
		double endY = position.y + Math.sin(angle) * width;

		
		Line laserLine = new Line();
		laserLine.x1 = startX;
		laserLine.y1 = startY;
		laserLine.x2 = endX;
		laserLine.y2 = endY;

		
		double kX = knight.getPosition().x + 2;
		double kY = knight.getPosition().y + 4;
		double kW = knight.getWidth() - 4;
		double kH = knight.getHeight() - 4;

		
		Vector2D knightHitboxPos = new Vector2D(kX, kY);

		
		if (laserLine.intersect(knightHitboxPos, kW, kH)) {
			knight.takeDamage(1);
		}
	}

	@Override
	public void takeDamage(int damage, Vector2D knockback) {
		
		
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
	public void move(){
	}
}