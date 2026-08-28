package Model.Game;

import View.Animations;
import java.awt.*;
import java.util.Random;

public class Particle extends Entity {
	
	private static Animations particleAnimations = new Animations();

	
	static {
		particleAnimations.addAnimation("Grass", "src/Model/Particles/");
		particleAnimations.addAnimation("Crystal", "src/Model/Particles/");
		particleAnimations.addAnimation("Dust", "src/Model/Particles/");
	}

	private String animationName;
	private int lifeTimeTicks;
	private int currentTicks = 0;
	private boolean isDead = false;
	private Random random = new Random();

	public Particle(double x, double y, Room room, int type, int lifeTimeTicks) {
		super(x, y, room);
		this.width = 10;
		this.height = 10;
		this.lifeTimeTicks = lifeTimeTicks;

		if (type == 1) {
			this.animationName = "Grass";
		} else if (type == 2) {
			this.animationName = "Crystal";
		} else {
			this.animationName = "Dust";
		}

		
		int maxFrames = particleAnimations.getFrameCount(animationName);
		if (maxFrames > 0) {
			this.frame = random.nextInt(maxFrames);
		}

		this.speed = new Vector2D((random.nextDouble() - 0.5), (random.nextDouble() - 0.5));
	}

	public boolean isDead() {
		return isDead;
	}

	@Override
	public void paint(Graphics g) {
		if (!isDead) {
			
			particleAnimations.paint(g, (int)position.x, (int)position.y, (int)width, (int)height, animationName, frame);
		}
	}

	@Override
	public void move() {
		position.add(speed);
	}

	@Override
	public void movements() {
		currentTicks++;

		if (currentTicks % 15 == 0) {
			speed.x += (random.nextDouble() - 0.5) * 0.3;
			speed.y += (random.nextDouble() - 0.5) * 0.3;

			speed.x = Math.max(-1.0, Math.min(1.0, speed.x));
			speed.y = Math.max(-1.0, Math.min(1.0, speed.y));
		}

		if (currentTicks % 6 == 0) {
			frame++;
		}

		int shrinkStartTick = lifeTimeTicks - 60;
		if (currentTicks > shrinkStartTick) {
			double ratio = (double)(lifeTimeTicks - currentTicks) / 60.0;
			if (ratio < 0) ratio = 0;
			this.width = 10.0 * ratio;
			this.height = 10.0 * ratio;
		}

		if (currentTicks >= lifeTimeTicks) {
			isDead = true;
		}
	}
}