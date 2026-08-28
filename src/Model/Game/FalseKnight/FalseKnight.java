package Model.Game.FalseKnight;

import Controller.SystemController;
import Model.Game.*;
import Model.Game.Enemies.GettingHit;
import View.Animations;
import Model.Game.Knight.Knight;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class FalseKnight extends Entity implements GettingHit {
	private LinkedList<String> moveHistory = new LinkedList<>();
	private Random random = new Random();
	private int idleTimer = 0;
	private boolean isEnteringState = false;
	private static Animations animations = new Animations();
	private String animationState = "Idle";
	private boolean enraged = false;
	private Vector2D target;
	private int frameOfState = 0;
	private int ticker = 0;
	private int airSlamPhase = 0;
	private boolean invincible = false;
	private boolean hasGoneToVictory = false;

	
	private boolean hasBeenStunned = false;
	public int hitsTakenInStun = 0;

	
	private boolean isDeadCountingDown = false;
	private int deathCountdownTimer = 0;
	private static final int DEATH_WAIT_TIME = 600; 

	static {
		String[] animNames = {
				"Attack Antic",
				"Attack",
				"Attack Recover",
				"Body",
				"DeathHit",
				"DeathLand",
				"Idle",
				"Jump Attack",
				"Jump",
				"Land",
				"Stun Recover",
				"Turn",
				"Run",
				"Run Antic"
		};

		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/FalseKnight/FalseKnightAnimations/");
		}

		
		String[] soundNames = {"hurt", "death", "slam", "jump", "land"};
		for (String name : soundNames) {
			animations.addSound(name, "src/Model/Game/FalseKnight/FalseKnightAnimations/Sounds/");
		}
	}

	public FalseKnight(double x, double y, Room room) {
		super(x, y, room);
		room.getEntities().add(this);
		hold = 8;
		width = 170;
		height = 170;
		state = "idle";
		this.hp = 600;
		state="Dormant";
	}

	@Override
	public void paint(Graphics g) {
		double scale = this.width / 230.0;

		int paintW = (int) (1095 * scale);
		int paintH = (int) (636 * scale);

		int paintX = (int) (position.x - (430 * scale));
		int paintY = (int) (position.y - (350 * scale));

		if (isFlipped) {
			animations.paintFlipped(g, paintX, paintY, paintW, paintH, animationState, frame);
		} else {
			animations.paint(g, paintX, paintY, paintW, paintH, animationState, frame);
		}
	}

	@Override
	public void movements() {
		ticker++;
		if (ticker >= hold) {
			ticker = 0;
			frame++;
		}
		doDamage();
		if(((room.getKnight().getPosition().x-position.x)*(room.getKnight().getPosition().x-position.x)+(room.getKnight().getPosition().y-position.y)*(room.getKnight().getPosition().y-position.y))<(1000)*(1000)&&state.equalsIgnoreCase("dormant")){
			state="idle";
			room.song = "False Knight.wav";
		}
		if (isDeadCountingDown && !hasGoneToVictory) {
			deathCountdownTimer++;
			if (deathCountdownTimer >= DEATH_WAIT_TIME) {
				hasGoneToVictory = true;
				room.song = "Crystal Peak.wav";
				SystemController.setCurrentState(SystemController.GameState.VICTORY);
			}
		}


		if (state.equalsIgnoreCase("dormant")){
			speed.add(new Vector2D(0, 3.5 * Units.LENGTH.number * Units.TICK.number));
			return;
		}
		if (state.equalsIgnoreCase("charge")) charge();
		if (state.equalsIgnoreCase("groundSlam")) groundSlam();
		if (state.equalsIgnoreCase("Jump")) jump();
		if (state.equalsIgnoreCase("hop")) hop();
		if (state.equalsIgnoreCase("airSlam")) airSlam();
		if (state.equalsIgnoreCase("stunned")) stunned();
		if (state.equalsIgnoreCase("idle")) idle();
		if (state.equalsIgnoreCase("turn")) turn();

		speed.add(new Vector2D(0, 3.5 * Units.LENGTH.number * Units.TICK.number));
	}

	private void charge() {
		frameOfState++;
		if (isEnteringState) {
			isEnteringState = false;
			state = "charge";
			frameOfState = 0;
			target = new Vector2D(room.getKnight().getPosition().x, room.getKnight().getPosition().y);
			if (target.x > position.x + width / 2) {
				isFlipped = true;
			}
			animationState = "Run Antic";
		}
		if (animationState.equalsIgnoreCase("Run Antic") && frame < 2) return;
		if (animationState.equalsIgnoreCase("Run Antic")) frame = 0;
		if (isFlipped ^ target.x > position.x + width / 2) {
			state = "idle";
			animationState = "Idle";
			return;
		}

		if (wallAhead()) {
			speed.x = 0;
			state = "idle";
			animationState = "Idle";
			frame = 0;
			return;
		}

		speed.x = 2 * Units.LENGTH.number * (isFlipped ? 1 : -1) * 8 / hold;
		animationState = "Run";
	}

	private void groundSlam() {
		if (isEnteringState) {
			isEnteringState = false;
			state = "groundSlam";
			frame = 0;
			animationState = "Attack Antic";
			isFlipped = room.getKnight().getPosition().x > position.x + width / 2;
			speed.x = 0;
		}

		if (animationState.equalsIgnoreCase("Attack Antic")) {
			if (frame >= animations.getFrameCount("Attack Antic") - 1) {
				frame = 0;
				animationState = "Attack";
			}
			return;
		}

		if (animationState.equalsIgnoreCase("Attack")) {
			if (frame >= animations.getFrameCount("Attack")) {
				frame = 0;
				animationState = "Attack Recover";

				animations.playSound("slam"); 

				
				if (room.panel != null && room.panel.camera != null) {
					room.panel.camera.triggerShake(18, 12);
				}

				
				if (enraged) {
					ShockWave boi = new ShockWave(position.x + (isFlipped ? 1.5 * width : -0.5 * width), position.y + 170, room);
					boi.setFlipped(isFlipped);
				}
			}
			return;
		}

		if (animationState.equalsIgnoreCase("Attack Recover")) {
			if (frame >= animations.getFrameCount("Attack Recover") - 1) {
				frame = 0;
				animationState = "Idle";
				state = "idle";
			}
		}
	}

	private void jump() {
		if (isEnteringState) {
			isEnteringState = false;
			state = "jump";
			frame = 0;
			animationState = "Jump";

			animations.playSound("jump"); 

			double center = position.x + width / 2;
			double target = room.getKnight().getPosition().x;
			double dx = target - center;

			if (dx > 700) dx = 700;
			if (dx < -700) dx = -700;

			isFlipped = dx > 0;
			speed.y = -4 * Units.LENGTH.number * 8 / hold;

			double airTimeSeconds = Math.abs((-2 * speed.y) / (3.5 * Units.LENGTH.number));
			speed.x = dx / airTimeSeconds;
		}

		if (animationState.equals("Jump")) {
			if (Math.abs(speed.y) < 0.1 && isGrounded()) {
				frame = 0;
				animationState = "Land";
				speed.x = 0;

				animations.playSound("land"); 

				
				if (room.panel != null && room.panel.camera != null) {
					room.panel.camera.triggerShake(10, 5);
				}
			}
		} else if (animationState.equals("Land")) {
			if (frame >= animations.getFrameCount("Land") - 1) {
				frame = 0;
				animationState = "Idle";
				state = "idle";
			}
		}
	}

	private void hop() {
		if (isEnteringState) {
			isEnteringState = false;
			state = "hop";
			frame = 0;
			animationState = "Jump";

			animations.playSound("jump"); 

			speed.x = (isFlipped ? -1 : 1) * Units.LENGTH.number * 8 / hold;
			speed.y = -2.8 * Units.LENGTH.number * 8 / hold;
		}

		if (animationState.equals("Jump")) {
			if (Math.abs(speed.y) < 0.1 && isGrounded()) {
				frame = 0;
				animationState = "Land";
				speed.x = 0;
				
			}
		} else if (animationState.equals("Land")) {
			if (frame >= animations.animations.get("Land").size() - 1) {
				frame = 0;
				animationState = "Idle";
				state = "idle";
			}
		}
	}

	private void airSlam() {
		if (isEnteringState) {
			isEnteringState = false;
			state = "airSlam";
			airSlamPhase = 0;
			frame = 0;
			animationState = "Jump Attack";

			animations.playSound("jump"); 

			double center = position.x + width / 2;
			double target = room.getKnight().getPosition().x;
			double dx = target - center;

			if (dx > 700) dx = 700;
			if (dx < -700) dx = -700;

			isFlipped = dx > 0;
			speed.y = -4 * Units.LENGTH.number * 8 / hold;

			double airTimeSeconds = Math.abs((-2 * speed.y) / (3.5 * Units.LENGTH.number));
			speed.x = dx / airTimeSeconds;
		}

		switch (airSlamPhase) {
			case 0:
				if (frame >= 3) frame = 3;
				if (Math.abs(speed.y) < 0.1 && isGrounded()) {
					speed.x = 0;
					frame = 4;
					airSlamPhase = 1;

					animations.playSound("land"); 

					
					if (room.panel != null && room.panel.camera != null) {
						room.panel.camera.triggerShake(10, 5);
					}
				}
				break;
			case 1:
				if (frame > 4) {
					frame = 5;
					airSlamPhase = 2;
				}
				break;
			case 2:
				if (frame > 5) {
					frame = 6;
					airSlamPhase = 3;
				}
				break;
			case 3:
				if (frame > 6) {
					frame = 7;
					airSlamPhase = 4;
					ShockWave boi = new ShockWave(position.x + (isFlipped ? 1.5 * width : -0.5 * width), position.y + 170, room);
					boi.setFlipped(isFlipped);

					
					if (room.panel != null && room.panel.camera != null) {
						room.panel.camera.triggerShake(20, 15);
					}
				}
				break;
			case 4:
				if (frame > 7) {
					state = "hop";
					isEnteringState = true;
					frame = 0;
					animationState = "Jump";
					airSlamPhase = 5;
				}
				break;
			case 5:
				if (state.equalsIgnoreCase("idle")) {
					airSlamPhase = 0;
				}
				break;
		}
	}

	private void stunned() {
		if (isEnteringState) {
			isEnteringState = false;
			animationState = "DeathLand";
			frame = 0;

			double center = position.x + width / 2;
			double targetX = room.getKnight().getPosition().x;
			isFlipped = targetX > center;

			speed.y = -1 * Units.LENGTH.number * 8 / hold;
			speed.x = (isFlipped ? -1 : 1) * 4 * Units.LENGTH.number * 8 / hold;

			this.invincible = true;
		}

		if (animationState.equalsIgnoreCase("DeathLand")) {
			if (!isGrounded()) {
				if (frame > 2) {
					frame = 1;
				}
			} else {
				speed.x = 0;
				if (frame >= animations.getFrameCount("DeathLand") - 1) {
					frame = 0;
					animationState = "Body";
					this.invincible = false;
				}
			}
		}
		else if (animationState.equalsIgnoreCase("Body")) {
			speed.x = 0;
		}
		else if (animationState.equalsIgnoreCase("Stun Recover")) {
			if (frame >= 4) {
				enraged = true;
				hold = 6;
				state = "idle";
				animationState = "Idle";
				frame = 0;
				invincible = false;
				idleTimer = generateIdleTime();
			}
		}
	}

	private void idle() {
		if (idleTimer <= 0) {
			idleTimer = generateIdleTime();
			animationState = "Idle";
			speed.x = 0;
			return;
		}

		double center = position.x + width / 2;
		double targetX = room.getKnight().getPosition().x;
		double distance = Math.abs(targetX - center);

		if (distance > 10) {
			boolean playerOnRight = targetX > center;

			if ((playerOnRight && !isFlipped) || (!playerOnRight && isFlipped)) {
				state = "turn";
				frame = 0;
				isEnteringState = true;
				return;
			}
		}

		idleTimer--;

		if (idleTimer <= 0) {
			chooseNextAction();
		}
	}

	private void turn() {
		if (isEnteringState) {
			isEnteringState = false;
			animationState = "Turn";
			frame = 0;
			speed.x = 0;
		}

		if (frame >= animations.getFrameCount("Turn") - 1) {
			isFlipped = !isFlipped;
			frame = 0;
			animationState = "Idle";
			state = "idle";
		}

		idleTimer--;
		if (idleTimer <= 0)
			idleTimer = 1;
	}

	private boolean wallAhead() {
		double check = 50;
		double x = isFlipped ? position.x + width + check : position.x - check;

		for (Line line : room.getBoundaries()) {
			Vector2D normal = line.normalRight();

			if (Math.abs(normal.x) < 0.9) continue;
			if (isFlipped && normal.x >= 0) continue;
			if (!isFlipped && normal.x <= 0) continue;

			if (line.intersect(new Vector2D(x, position.y + 2), check, height - 4)) {
				return true;
			}
		}
		return false;
	}

	private boolean isGrounded() {
		double checkDepth = 3.0;
		Vector2D checkPosition = new Vector2D(position.x, position.y + height);

		for (Line line : room.getBoundaries()) {
			if (!line.isFloor()) continue;
			if (line.intersect(checkPosition, width, checkDepth)) {
				return true;
			}
		}
		return false;
	}

	private void chooseNextAction() {
		double center = position.x + width / 2;
		double targetX = room.getKnight().getPosition().x;
		double distance = Math.abs(targetX - center);

		HashMap<String, Double> weights = new HashMap<>();

		
		if (distance < 250) {
			weights.put("groundSlam", 45.0);
			weights.put("hop", 35.0);
			weights.put("airSlam", enraged ? 10.0 : 0.0);
			weights.put("jump", 10.0);
			weights.put("charge", 0.0);
		} else if (distance < 550) {
			weights.put("groundSlam", 10.0);
			weights.put("hop", 15.0);
			weights.put("airSlam", enraged ? 35.0 : 0.0);
			weights.put("jump", 30.0);
			weights.put("charge", 10.0);
		} else {
			weights.put("groundSlam", 0.0);
			weights.put("hop", 0.0);
			weights.put("airSlam", enraged ? 20.0 : 0.0);
			weights.put("jump", 35.0);
			weights.put("charge", 45.0);
		}

		int i = 0;
		for (String pastMove : moveHistory) {
			if (weights.containsKey(pastMove)) {
				double multiplier;
				if (i == 2) multiplier = 0.05;
				else if (i == 1) multiplier = 0.25;
				else multiplier = 0.50;

				weights.put(pastMove, weights.get(pastMove) * multiplier);
			}
			i++;
		}

		double totalWeight = 0.0;
		for (double w : weights.values()) {
			totalWeight += w;
		}

		double randomVal = random.nextDouble() * totalWeight;
		double currentWeight = 0.0;
		String chosenMove = "groundSlam";

		for (Map.Entry<String, Double> entry : weights.entrySet()) {
			currentWeight += entry.getValue();
			if (randomVal <= currentWeight) {
				chosenMove = entry.getKey();
				break;
			}
		}

		moveHistory.add(chosenMove);
		if (moveHistory.size() > 3) {
			moveHistory.removeFirst();
		}

		this.state = chosenMove;
		this.isEnteringState = true;
	}

	private int generateIdleTime() {
		int minTicks = 90;
		int maxTicks = 120;
		if (enraged) {
			minTicks = 60;
			maxTicks = 90;
		}
		return random.nextInt((maxTicks - minTicks) + 1) + minTicks;
	}

	@Override
	public void takeDamage(int damage, Vector2D knockback) {
		if (hp <= 0 || invincible) {
			return;
		}

		room.song = "False Knight.wav";
		if (state.equalsIgnoreCase("stunned") && animationState.equalsIgnoreCase("Body")) {
			animations.playSound("hurt"); 

			hitsTakenInStun++;
			if (hitsTakenInStun >= 6 && hp > 0) {
				animationState = "Stun Recover";
				frame = 0;
				invincible = true;

				speed.y = -1 * Units.LENGTH.number * 8 / hold;
			}
			return;
		}

		this.hp -= damage;

		if (this.hp <= 300 && !hasBeenStunned && !state.equalsIgnoreCase("stunned")) {
			hasBeenStunned = true;
			state = "stunned";
			isEnteringState = true;
			hitsTakenInStun = 0;
		}

		if (this.hp <= 0 && !state.equalsIgnoreCase("stunned")) {
			room.song = "None"; 
			animations.playSound("death"); 

			hasBeenStunned = true;
			state = "stunned";
			isEnteringState = true;
			hitsTakenInStun = 0;
			room.save.totalEnemyKilled++;
			room.save.killedBoss = true;

			
			isDeadCountingDown = true;
		}
	}

	public void doDamage() {
		Knight knight = room.getKnight();

		if (knight == null || hp <= 0 || state.equalsIgnoreCase("stunned") || state.equalsIgnoreCase("dormant")) {
			return;
		}

		double kX = knight.getPosition().x + 5;
		double kY = knight.getPosition().y + 10;
		double kW = knight.getWidth() - 10;
		double kH = knight.getHeight() - 10;

		boolean playerHit = false;

		double bodyX = position.x;
		double bodyY = position.y;
		double bodyW = width;
		double bodyH = height;

		if (checkAABB(bodyX, bodyY, bodyW, bodyH, kX, kY, kW, kH)) {
			playerHit = true;
		}

		boolean isAttackActive = (animationState.equalsIgnoreCase("Attack") && frame >= 2) ||
				(animationState.equalsIgnoreCase("Jump Attack") && frame >= 5);

		if (!playerHit && isAttackActive) {
			double weaponX;
			double weaponY = position.y + (0.5 * height);
			double weaponW = 2 * width;
			double weaponH = 1 * height;

			if (isFlipped) {
				weaponX = position.x;
			} else {
				weaponX = position.x - (1 * width);
			}

			if (checkAABB(weaponX, weaponY, weaponW, weaponH, kX, kY, kW, kH)) {
				playerHit = true;
			}
		}

		if (playerHit) {
			knight.takeDamage(1);
		}
	}

	private boolean checkAABB(double r1x, double r1y, double r1w, double r1h,
							  double r2x, double r2y, double r2w, double r2h) {
		return r1x < r2x + r2w &&
				r1x + r1w > r2x &&
				r1y < r2y + r2h &&
				r1y + r1h > r2y;
	}
}