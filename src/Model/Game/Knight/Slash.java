package Model.Game.Knight;

import Controller.SaveFile;
import Model.Game.*;
import Model.Game.FalseKnight.FalseKnight;
import Model.Game.Enemies.GettingHit;
import View.Animations;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Slash extends Entity {
	private static Animations animations = new Animations();
	private int damage;
	private boolean isFinished = false;
	public Boolean hasHit = false;
	public Boolean jumped = false;
	
	private ArrayList<Entity> enemiesHit = new ArrayList<>();

	static {
		animations.addAnimation("Right", "src/Model/Game/Knight/SlashAnimations/");
		animations.addAnimation("Up", "src/Model/Game/Knight/SlashAnimations/");
		animations.addAnimation("Down", "src/Model/Game/Knight/SlashAnimations/");

		
		animations.addSound("Hit", "src/Model/Game/Knight/SlashAnimations/Sounds/");
		animations.addSound("Hit Steel", "src/Model/Game/Knight/SlashAnimations/Sounds/");
	}

	public Slash(double x, double y) {
		super(x, y);
		width = 120;
		height = 70;
		hold = 2;
	}

	@Override
	public void paint(Graphics g) {
		if (state.equalsIgnoreCase("Right") && isFlipped) {
			animations.paint(g, position.getIntX(), position.getIntY(), (int) width, (int) height, state, frame);
		}
		if (state.equalsIgnoreCase("Right") && !isFlipped) {
			animations.paintFlipped(g, position.getIntX(), position.getIntY(), (int) width, (int) height, state, frame);
		}
		if (isFlipped && !state.equalsIgnoreCase("Right")) {
			animations.paint(g, position.getIntX(), position.getIntY(), (int) width, (int) height, state, frame);
		}
		if (!isFlipped && !state.equalsIgnoreCase("Right")) {
			animations.paintFlipped(g, position.getIntX(), position.getIntY(), (int) width, (int) height, state, frame);
		}
	}

	@Override
	public void movements() {
		
		Knight knight = room.getKnight();
		double knightCenterX = knight.getPosition().getX() + (knight.getWidth() / 2.0);
		double knightCenterY = knight.getPosition().getY() + (knight.getHeight() / 2.0);

		
		if (state.equalsIgnoreCase("Right")) {
			if (isFlipped) {
				position.setX(knightCenterX - this.width);
				position.setY(knightCenterY - (this.height / 2.0));
			} else {
				position.setX(knightCenterX);
				position.setY(knightCenterY - (this.height / 2.0));
			}
		} else if (state.equalsIgnoreCase("Down")) {
			position.setX(knightCenterX - (this.width / 2.0));
			position.setY(knightCenterY);
		} else if (state.equalsIgnoreCase("Up")) {
			position.setX(knightCenterX - (this.width / 2.0));
			position.setY(knightCenterY - this.height);
		}

		
		Hit(room);

		
		hold++;
		if (hold >= Units.HOLD.number) {
			frame++;
			hold = 0;
		}
		if (frame == 6) {
			room.getEntities().remove(this);
			isFinished = true;
		}
	}

	@Override
	public void move() {
		return;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setDamage(int i) {
		damage = i;
	}

	
	public void Hit(Room room) {
		if (frame <= 2 || frame > 5)
			return;
		for (Entity e : room.getEntities()) {
			
			if (e instanceof GettingHit && !enemiesHit.contains(e)) {

				boolean isIntersecting = false;
				String enemyType = e.getClass().getSimpleName();

				
				if (enemyType.equals("Crystallized") || enemyType.equals("HornedHusk")) {

					
					double midLeft = e.getPosition().x + (e.getWidth() / 3.0);
					double midRight = e.getPosition().x + (e.getWidth() * 2.0 / 3.0);

					isIntersecting =
							this.position.x < midRight &&
									this.position.x + this.width > midLeft &&
									this.position.y < e.getPosition().y + e.getHeight() &&
									this.position.y + this.height > e.getPosition().y;

				}
				
				else if (enemyType.equals("FalseKnight") && e.getState().equalsIgnoreCase("stunned")) {

					
					double headW = e.getWidth() * 0.25;
					double headH = e.getHeight() * 0.25;

					
					double headY = e.getPosition().y + (e.getHeight() * (1 - 0.25));

					double headX;

					
					if (e.isFlipped()) {
						
						headX = e.getPosition().x + e.getWidth();
					} else {
						
						headX = e.getPosition().x - headW;
					}

					
					isIntersecting =
							this.position.x < headX + headW &&
									this.position.x + this.width > headX &&
									this.position.y < headY + headH &&
									this.position.y + this.height > headY;

				} else {
					
					isIntersecting =
							this.position.x < e.getPosition().x + e.getWidth() &&
									this.position.x + this.width > e.getPosition().x &&
									this.position.y < e.getPosition().y + e.getHeight() &&
									this.position.y + this.height > e.getPosition().y;
				}

				if (isIntersecting) {

					
					animations.playSound("Hit");

					Vector2D knockback = new Vector2D(0, 0);
					double kbForceX = 1 * Units.LENGTH.number;
					double kbForceY = -1 * Units.LENGTH.number; 

					
					if (state.equalsIgnoreCase("Right")) {
						knockback.x = isFlipped ? -kbForceX : kbForceX;
					} else if (state.equalsIgnoreCase("Up")) {
						knockback.y = kbForceY * 1.5; 
					} else if (state.equalsIgnoreCase("Down")) {
						knockback.y = Math.abs(kbForceY) * 1; 
					}

					
					if (room.save.currentCharms.contains(SaveFile.Charm.HEAVY_BLOW))
						knockback.multiply(2);
					((GettingHit) e).takeDamage(damage, knockback);
					hasHit = true;
					if (enemyType.equals("FalseKnight") && !e.getState().equalsIgnoreCase("stunned")) {
						
					} else {
						
					}
					
					enemiesHit.add(e);
				}
			}
		}

		if (hasHit && !jumped) {
			jumped = true;

			
			double tipX = position.x;
			double tipY = position.y;

			if (state.equalsIgnoreCase("Right")) {
				if (isFlipped) { 
					tipX = position.x;
					tipY = position.y + (height / 2.0);
				} else { 
					tipX = position.x + width;
					tipY = position.y + (height / 2.0);
				}
			} else if (state.equalsIgnoreCase("Up")) {
				tipX = position.x + (width / 2.0);
				tipY = position.y;
			} else if (state.equalsIgnoreCase("Down")) {
				tipX = position.x + (width / 2.0);
				tipY = position.y + height;
			}

			boolean flag = false;
			boolean flag2 = false;

			for (Entity e : enemiesHit) {
				String enemyType = e.getClass().getSimpleName();

				
				if (e.getHp() > -damage) {
					flag2 = true;
				}

				
				boolean canGiveSoul = true;
				if (enemyType.equals("SecretDoor") || enemyType.equals("Hazard")) {
					canGiveSoul = false;
				} else if (enemyType.equals("FalseKnight") && !e.getState().equalsIgnoreCase("stunned")) {
					canGiveSoul = false;
				}

				if (canGiveSoul && (e.getState().equalsIgnoreCase("stunned") || e.getHp() > -damage)) {
					flag = true;
				}
			}

			if (flag) {
				for (int i = 0; i < 3 + (room.save.currentCharms.contains(SaveFile.Charm.SOUL_CATCHER) ? 1 : 0); i++) {
					
					SoulOrb A = new SoulOrb(tipX, tipY, room);

					Random rand = new Random();
					A.getPosition().add(new Vector2D(-20 + rand.nextInt(41), -20 + rand.nextInt(41)));
					A.getSpeed().x = 0;
					A.getSpeed().y = 0;
					A.getSpeed().add(new Vector2D(-20 + rand.nextInt(41), -20 + rand.nextInt(41)));
					A.getSpeed().multiply(1 / A.getSpeed().getLength());
				}
			}
			if (state.equalsIgnoreCase("Down") && flag2) {
				room.getKnight().pogo();
			}
		}
	}
}