package Model.Game.Enemies;

import Model.Game.Entity;
import Model.Game.Line;
import Model.Game.Room;
import Model.Game.Units;
import Model.Game.Vector2D;
import Model.Game.Knight.Knight;
import View.Animations;

import java.awt.*;

public class CrystalCrawler extends Entity implements GettingHit {
	private static Animations animations = new Animations();
	private int justTurned = 0;

	
	private Vector2D activeKnockback = new Vector2D(0, 0);
	private int knockbackFrames = 0;

	static {
		String[] animNames = {
				"Walk", "Turn", "DeathAir", "DeathLand"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/Enemies/CrystalCrawlerAnimations/");
		}

		
		String[] soundNames = {"Hit", "Death"};
		for (String name : soundNames) {
			animations.addSound(name, "src/Model/Game/Enemies/CrystalCrawlerAnimations/Sounds/");
		}
	}

	private int tickCounter = 0;

	public CrystalCrawler(double x, double y, Room room) {
		super(x, y, room);
		width = 40;
		height = 50;
		state = "Walk";
		room.getEntities().add(this);
		hp = 10;
		hold = 6;
	}

	@Override
	public void paint(Graphics g) {
		String animName = state;

		if (isFlipped) {
			animations.paintFlipped(g, (int) position.x, (int) position.y, (int) width, (int) height, animName, frame);
		} else {
			animations.paint(g, (int) position.x, (int) position.y, (int) width, (int) height, animName, frame);
		}
	}

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

		
		switch (state) {
			case "Walk":
				if (frame >= getAnimSize("Walk")) {
					frame = 0;
				}

				
				speed.y += 3.5 * Units.LENGTH.number * Units.TICK.number;

				
				double walkSpeed = 0.5 * Units.LENGTH.number;
				speed.x = isFlipped ? walkSpeed : -walkSpeed;

				
				if (hitTurnAround() && justTurned <= 0) {
					state = "Turn";
					frame = 0;
				}
				justTurned--;
				break;

			case "Turn":
				speed.x = 0;
				speed.y += 3.5 * Units.LENGTH.number * Units.TICK.number;

				if (frame >= getAnimSize("Turn")) {
					isFlipped = !isFlipped;
					state = "Walk";
					frame = 0;
					justTurned = 4;
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
		room.save.killedCrawler=true;
		room.save.totalEnemyKilled++;
	}

	private boolean hitTurnAround() {
		for (WalkerTurnAround wta : room.getTurnArounds()) {
			if (wta.intersects(position, width, height)) {
				return true;
			}
		}
		return false;
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
}