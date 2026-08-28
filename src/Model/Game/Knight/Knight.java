package Model.Game.Knight;

import Controller.GeneralSave;
import Controller.SaveFile;
import Model.Game.*;
import Model.Game.Enemies.GettingHit;
import Model.Game.ZoteBote.ZoteBote;
import View.Animations;
import java.awt.*;
import java.util.ArrayList;

public class Knight extends Entity {

	public boolean movingToTalk = false;
	private Entity targetZote = null;
	
	public boolean quickFocus = false; 
	private enum FocusState { NONE, DELAY, START, LOOP, GET, END }
	private FocusState focusState = FocusState.NONE;

	private int focusTickTimer = 0;
	private boolean wasFocusHeld = false; 
	private static final int FOCUS_TAP_THRESHOLD = 8; 

	public boolean isHazardRespawning = false;
	public int hazardRespawnTimer = 0;
	private static final int HAZARD_RESPAWN_MAX_TIME = 60;

	private static final int FOCUS_TOTAL_TIME = 90;
	private static final int FOCUS_SPELL_COST = 90;

	private static final int FOCUS_END_LOCK_TIME = 20; 
	private static final int FOCUS_RESTART_DELAY = 10;
	private static final int FOCUS_START_TIME = 10;
	private static final int FOCUS_LOOP_TIME = 25;
	private static final int FOCUS_HEAL_TIME = 90; 
	
	private boolean left, right, up, down;
	private boolean jumpHeld, attackHeld, dashHeld, focusHeld, quickCastHeld;
	private boolean jumpJustPressed, attackJustPressed, dashJustPressed, quickCastJustPressed;
	private boolean jumpReleased = false;
	private boolean dialogueLocked = false;
	private int deathTimer = 0;
	
	private boolean hasJump = true, hasDoubleJump = true, hasDash = true;
	private int isTouchingWall = 0; 
	private boolean onGround = false;
	private int coyoteTimer = 0;
	protected int soul = 0;
	protected int maxsoul = 270;
	private static Animations animations = new Animations();

	
	private int wallJumpLockout = 0;
	private int spellLockout = 0;
	private int dashDuration = 0;
	private int dashCooldown = 0;
	private int slashCooldown = 0;
	private int slashAnimTimer = 0;
	private int comboWindowTimer = 0;
	private int landingTimer = 0;
	private int hurtTimer = 0;
	private int hitLockTimer = 0; 
	private int lookTimer = 0;    
	private boolean isDead = false;
	public int frameSinceHealed=100;
	public int frameSinceDamaged=100;
	public int frameSinceSoul=100;
	private int talkStuckTimer = 0;

	private Vector2D lastSafePosition;

	private ArrayList<Entity> dashEnemiesHit = new ArrayList<>();
	static {
		String[] animNames = {
				"AirBorne", "Dash", "Death", "Double Jump", "DownSlash", "Fall",
				"FireBall Cast", "Focus", "Focus End", "Focus Get", "Focus Start","idle hurt",
				"Idle", "Idle Hurt", "Landing", "LookDown", "LookUp", "Run",
				"Scream", "Shadow Dash", "Slash", "SlashAlt", "UpSlash", "WallJump", "Wall Slide"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/Knight/KnightAnimations/");
		}
		
		String[] soundNames = {
				"Dash", "Slash", "Fire Ball", "Shriek", "Shade Dash", "Double Jump", "Jump", "Focus", "Hurt", "Death"
		};
		for (String name : soundNames) {
			animations.addSound(name, "src/Model/Game/Knight/KnightAnimations/Sounds/");
		}
	}
	public Knight(double x, double y) {
		super(x, y);
		this.width = 40;
		this.height = 80;
		this.hp = 5;
		this.curHp = 5;
		this.soul = 270;
	}
	public Knight(double x, double y, Room room) {
		super(x, y);
		this.width = 40;
		this.height = 80;
		this.hp = 5;
		this.curHp = 5;
		this.soul = 270;
		room.setKnight(this);
	}

	int hold=0;

	public void movements() {



		if(room.save.currentCharms.contains(SaveFile.Charm.QUICK_FOCUS))
			quickFocus=true;

		hold++;
		if (hold == Units.HOLD.number) {
			frame++;
			hold = 0;
			frameSinceDamaged++;
			frameSinceHealed++;
			frameSinceSoul++;

			
			if (state.equalsIgnoreCase("Scream") && frame >= 6) {
				frame = 5;
			}

			
			if ((state.equalsIgnoreCase("LookUp") || state.equalsIgnoreCase("LookDown")) && frame > 5) {
				frame = 2;
			}

			
			if (state.equalsIgnoreCase("Run") && frame > 12) {
				frame = 5;
			}

			
			if (state.equalsIgnoreCase("AirBorne")) {
				if (speed.getY() < 0 && frame >= 5) {
					
					frame = 5;
				} else if (frame > 11) {
					
					frame = 8;
				}
			}
			if(state.equalsIgnoreCase("fall") && frame>5){
				frame=3;
			}
		}

		if (isDead) {
			resolveAnimations();
			deathTimer++;

			
			if (deathTimer >= 420) {
				
				room.save.deathCount++;

				
				respawnRoom();
			}
			return;
		}

		if (isHazardRespawning) {
			hazardRespawnTimer--;

			if (hazardRespawnTimer <= 0) {
				
				isHazardRespawning = false;

				if (lastSafePosition != null) {
					position.x = lastSafePosition.x;
					position.y = lastSafePosition.y;
				}

				
				speed.setX(0);
				speed.setY(0);

				
				hasDash = true;
				hasJump = true;
				hasDoubleJump = true;

				
				setState("Idle");
			} else {
				
				stopX();
				gravity();
				resolveAnimations();
				return; 
			}
		}

		if (GeneralSave.noClip) {
			
			if (dashCooldown > 0) dashCooldown--;
			if (slashCooldown > 0) slashCooldown--;
			if (slashAnimTimer > 0) slashAnimTimer--;
			if (spellLockout > 0) spellLockout--;
			if (hurtTimer > 0) hurtTimer--;

			
			hasDash = true;

			if (dashDuration > 0) {
				dashDuration--;
				speed.setY(0); 

				
				if (state.equalsIgnoreCase("Shadow Dash") && room != null) {
					double boxWidth = this.width;
					double boxHeight = 20;
					double boxX = this.position.x;
					double boxY = this.position.y + (this.height / 2.0) - (boxHeight / 2.0);

					for (Entity e : room.getEntities()) {
						if (e instanceof GettingHit && !dashEnemiesHit.contains(e)) {
							boolean isIntersecting =
									boxX < e.getPosition().x + e.getWidth() &&
											boxX + boxWidth > e.getPosition().x &&
											boxY < e.getPosition().y + e.getHeight() &&
											boxY + boxHeight > e.getPosition().y;

							if (isIntersecting) {
								int dashDamage = 5;
								if(room.save.currentCharms.contains(SaveFile.Charm.UNBREAKABLE_STRENGTH))
									dashDamage=10;
								((GettingHit) e).takeDamage(dashDamage, new Vector2D(0, 0));
								dashEnemiesHit.add(e);
							}
						}
					}
				}
				if (isFlipped) speed.setX(-2.4 * Units.LENGTH.number);
				else speed.setX(2.4 * Units.LENGTH.number);
				return; 
			}

			
			boolean isFocusJustPressed = focusHeld && !wasFocusHeld;
			wasFocusHeld = focusHeld;

			
			if (!focusHeld) {
				if (focusState == FocusState.DELAY) {
					focusState = FocusState.NONE;
					castSpell();
					return;
				} else if (focusState == FocusState.START || focusState == FocusState.LOOP) {
					focusState = FocusState.END;
					setState("Focus End");
					setFrame(0);
				} else if (focusState == FocusState.END) {
					focusState = FocusState.NONE;
				}
			}

			
			if (isFocusJustPressed && focusState == FocusState.NONE && hitLockTimer <= 0 && spellLockout <= 0 && dashDuration <= 0) {
				if (soul > 0) {
					focusState = FocusState.DELAY;
					focusTickTimer = 0;
				}
			}

			
			if (focusState == FocusState.DELAY) {
				stopX();
				speed.setY(0);
				focusTickTimer++;
				if (focusTickTimer >= FOCUS_TAP_THRESHOLD) {
					focusState = FocusState.START;
					setState("Focus Start");
					setFrame(0);
				}
				resolveAnimations();
				return;
			}

			if (focusState == FocusState.START) {
				stopX();
				speed.setY(0);
				if (frame >= 3) {
					focusState = FocusState.LOOP;
					setState("Focus");
					setFrame(0);
					focusTickTimer = 0;
				}
				resolveAnimations();
				return;
			}

			if (focusState == FocusState.LOOP) {
				stopX();
				speed.setY(0);
				focusTickTimer++;

				int drain = quickFocus ? 2 : 1;
				int maxTicks = quickFocus ? 45 : 90;

				soul -= drain;
				if (soul < 0) soul = 0;

				if (focusTickTimer >= maxTicks) {
					if (curHp < hp) {
						curHp++;
						frameSinceHealed = 0;
					}
					animations.playSound("Focus"); 
					focusState = FocusState.GET;
					setState("Focus Get");
					setFrame(0);
				} else if (soul <= 0) {
					focusState = FocusState.END;
					setState("Focus End");
					setFrame(0);
				}
				resolveAnimations();
				return;
			}

			if (focusState == FocusState.GET) {
				stopX();
				speed.setY(0);
				if (frame >= 6) {
					if (focusHeld && soul > 0) {
						focusState = FocusState.LOOP;
						setState("Focus");
						setFrame(0);
						focusTickTimer = 0;
					} else {
						focusState = FocusState.NONE;
					}
				}
				resolveAnimations();
				return;
			}

			if (focusState == FocusState.END) {
				speed.setY(0);
				if (left || right || up || down || jumpJustPressed || dashJustPressed) {
					focusState = FocusState.NONE;
				} else {
					stopX();
					if (frame >= 3) {
						focusState = FocusState.NONE;
					}
					resolveAnimations();
					return;
				}
			}

			
			if (focusState == FocusState.START || focusState == FocusState.LOOP || focusState == FocusState.END) {
				stopX();
				speed.setY(0);
				resolveAnimations();
				return;
			}

			
			double noClipSpeed = 1.0 * Units.LENGTH.number;

			if (up) speed.setY(-noClipSpeed);
			else if (down) speed.setY(noClipSpeed);
			else speed.setY(0);

			if (left) {
				speed.setX(-noClipSpeed);
				isFlipped = true;
			} else if (right) {
				speed.setX(noClipSpeed);
				isFlipped = false;
			} else {
				speed.setX(0);
			}

			
			if (dashJustPressed) {
				if (dashCooldown <= 0) executeDash();
				dashJustPressed = false;
			}
			if (attackJustPressed) {
				if (slashCooldown <= 0) attack();
				attackJustPressed = false;
			}
			if (quickCastJustPressed) {
				castSpell();
				quickCastJustPressed = false;
			}

			if(dashDuration<0 && state.equalsIgnoreCase("Dash") || dashDuration<0 && state.equalsIgnoreCase("Shadow Dash")){
				state="Idle";
			}
			resolveAnimations();
			return;
		}
		boolean isFocusJustPressed = focusHeld && !wasFocusHeld;
		wasFocusHeld = focusHeld;
		
		if (onGround && state.equalsIgnoreCase("Idle") && up && !movingToTalk) { 
			Entity zote = findZoteBote(); 
			if (zote != null) { 
				if(zote.getClass()==ZoteBote.class){ 
					if(zote.getState().equalsIgnoreCase("talk")){ 
						movingToTalk = true; 
						targetZote = zote; 
						dialogueLocked = true; 
						talkStuckTimer = 0;    
					}
				}
			}
		}
		if (movingToTalk) { 
			double targetX = targetZote.getPosition().x - 50; 
			talkStuckTimer++; 

			
			boolean isStuck = (talkStuckTimer >= 120);

			
			if (Math.abs(this.position.x - targetX) > 2 && !isStuck) { 
				if (this.position.x < targetX) rightKey(); 
				else leftKey(); 
				setState("Run"); 
			} else {
				
				stopX(); 

				
				isFlipped = (this.position.x > targetZote.getPosition().x); 
				setState("Idle"); 

				
				if (targetZote instanceof ZoteBote) { 
					((ZoteBote) targetZote).triggerTalk(); 
				}

				
				movingToTalk = false; 
				targetZote = null; 
				talkStuckTimer = 0;
			}
			resolveAnimations(); 
			return; 
		}
		
		if (dashCooldown > 0) dashCooldown--;
		if (slashCooldown > 0) slashCooldown--;
		if (slashAnimTimer > 0) slashAnimTimer--;
		if (comboWindowTimer > 0) comboWindowTimer--;
		if (landingTimer > 0) landingTimer--;
		if (hurtTimer > 0) hurtTimer--;       
		if (hitLockTimer > 0) hitLockTimer--; 

		

		
		if (hitLockTimer > 0) {
			double influence = 0.15 * Units.LENGTH.number; 

			if (left) speed.setX(speed.getX() - influence);
			if (right) speed.setX(speed.getX() + influence);

			
			double maxKnockbackSpeed = 1.8 * Units.LENGTH.number;
			if (speed.getX() > maxKnockbackSpeed) speed.setX(maxKnockbackSpeed);
			if (speed.getX() < -maxKnockbackSpeed) speed.setX(-maxKnockbackSpeed);

			gravity();
			resolveAnimations();
			return; 
		}

		if (spellLockout > 0) {
			spellLockout--;

			
			speed.setY(0);
			if (isFlipped) speed.setX((0.8*(1-frame/8.0)) * Units.LENGTH.number);
			else speed.setX(-(0.8*(1-frame/8.0)) * Units.LENGTH.number);
			if (state.equalsIgnoreCase("Scream")) {
				stopX(); 
			}

			return; 
		}

		if (dashDuration > 0) {
			dashDuration--;
			speed.setY(0);

			
			if (state.equalsIgnoreCase("Shadow Dash") && room != null) {
				
				double boxWidth = this.width;
				double boxHeight = 20;
				double boxX = this.position.x;
				double boxY = this.position.y + (this.height / 2.0) - (boxHeight / 2.0);

				for (Entity e : room.getEntities()) {
					if (e instanceof GettingHit && !dashEnemiesHit.contains(e)) {

						
						boolean isIntersecting =
								boxX < e.getPosition().x + e.getWidth() &&
										boxX + boxWidth > e.getPosition().x &&
										boxY < e.getPosition().y + e.getHeight() &&
										boxY + boxHeight > e.getPosition().y;

						if (isIntersecting) {
							int dashDamage = 5; 
							if(room.save.currentCharms.contains(SaveFile.Charm.UNBREAKABLE_STRENGTH))
								dashDamage=10;
							
							((GettingHit) e).takeDamage(dashDamage, new Vector2D(0, 0));
							dashEnemiesHit.add(e);
						}
					}
				}
			}

			if (isTouchingWall != 0) dashDuration = 0;
			return;
		}

		if (isTouchingWall != 0) wallJumpLockout = 0;
		if (wallJumpLockout > 0) {
			wallJumpLockout--;
			return;
		}

		if (dialogueLocked) {
			stopX();       
			gravity();     
			resolveAnimations();

			
			dashJustPressed = false;
			attackJustPressed = false;
			jumpJustPressed = false;
			quickCastJustPressed = false;

			return; 
		}

		
		
		

		
		if (!focusHeld) {
			if (focusState == FocusState.DELAY) {
				
				focusState = FocusState.NONE;
				castSpell();
				return;
			} else if (focusState == FocusState.START || focusState == FocusState.LOOP) {
				
				focusState = FocusState.END;
				setState("Focus End");
				setFrame(0);
			} else if (focusState == FocusState.END) {
				
				focusState = FocusState.NONE;
			}
		}

		
		if (isFocusJustPressed && focusState == FocusState.NONE && hitLockTimer <= 0 && spellLockout <= 0 && dashDuration <= 0) {
			if (!onGround) {
				
				if (soul >= 90) castSpell();
				return;
			} else if (soul > 0) { 
				
				focusState = FocusState.DELAY;
				focusTickTimer = 0;
			}
		}

		
		if (focusState == FocusState.DELAY) {
			stopX();
			gravity();
			focusTickTimer++;
			if (focusTickTimer >= FOCUS_TAP_THRESHOLD) {
				
				focusState = FocusState.START;
				setState("Focus Start");
				setFrame(0);
			}
			resolveAnimations();
			return; 
		}

		if (focusState == FocusState.START) {
			stopX();
			gravity();
			
			if (frame >= 3) {
				focusState = FocusState.LOOP;
				setState("Focus");
				setFrame(0);
				focusTickTimer = 0;
			}
			resolveAnimations();
			return;
		}

		if (focusState == FocusState.LOOP) {
			stopX();
			gravity();
			focusTickTimer++;

			
			int drain = quickFocus ? 2 : 1;
			int maxTicks = quickFocus ? 45 : 90;

			soul -= drain;
			if (soul < 0) soul = 0;

			if (focusTickTimer >= maxTicks) {
				
				if (curHp < hp) {
					curHp++;
					frameSinceHealed = 0;
				}
				animations.playSound("Focus"); 
				focusState = FocusState.GET;
				setState("Focus Get");
				setFrame(0);
			} else if (soul <= 0) {
				
				focusState = FocusState.END;
				setState("Focus End");
				setFrame(0);
			}
			resolveAnimations();
			return;
		}

		if (focusState == FocusState.GET) {
			stopX();
			gravity();
			
			if (frame >= 6) {
				
				
				
				if (focusHeld && soul > 0 ) { 
					focusState = FocusState.LOOP; 
					setState("Focus");
					setFrame(0);
					focusTickTimer = 0;
				} else {
					focusState = FocusState.NONE;
				}
			}
			resolveAnimations();
			return;
		}

		if (focusState == FocusState.END) {
			gravity();
			
			if (left || right || jumpJustPressed || dashJustPressed) {
				focusState = FocusState.NONE;
				
				
			} else {
				stopX();
				
				if (frame >= 3) {
					focusState = FocusState.NONE;
				}
				resolveAnimations();
				return;
			}
		}

		
		
		
		if (focusState == FocusState.START || focusState == FocusState.LOOP || focusState == FocusState.END) {

			stopX();
			gravity();
			resolveAnimations();
			return;
		}

		
		if (quickCastJustPressed) {
			castSpell();
			quickCastJustPressed = false;
		}
		if (dashJustPressed) {
			if (hasDash && dashCooldown <= 0) {
				executeDash();
			}
			dashJustPressed = false;
		}

		if (attackJustPressed) {
			if (slashCooldown <= 0) {
				attack();
			}
			attackJustPressed = false;
		}

		if (jumpReleased) {
			if (hasDoubleJump) { 
				releaseJump();
			}
			jumpReleased = false; 
		}

		
		if (dashDuration <= 0) {
			if (right && left) stopX();
			else if (right) rightKey();
			else if (left) leftKey();
			else stopX();

			boolean isPushingIntoWall = (right && isTouchingWall == 1) || (left && isTouchingWall == -1);
			if (isTouchingWall != 0 && isPushingIntoWall && !onGround) {
				if (speed.getY() < 0) speed.setY(0);
				slide();
			} else {
				gravity();
			}

			
			if (!onGround && room != null && speed.getY() >= 0) {
				double checkX = this.position.x;
				double checkY = this.position.y + this.height - 1;
				double checkWidth = this.width;
				double checkHeight = 4;

				Vector2D checkPos = new Vector2D(checkX, checkY);
				for (Line line : room.getBoundaries()) {
					if (line.isFloor() && line.intersect(checkPos, checkWidth, checkHeight)) {
						setOnGround(true);
						break;
					}
				}
			}

			
			if (jumpJustPressed) {
				if (isTouchingWall != 0) {
					wallJumpLockout = 12;
					speed.setY(-1.2 * Units.LENGTH.number);
					if (isTouchingWall == 1) {
						speed.setX(-1.4 * Units.LENGTH.number);
						isFlipped = true;
					} else if (isTouchingWall == -1) {
						speed.setX(1.4 * Units.LENGTH.number);
						isFlipped = false;
					}
					isTouchingWall = 0;
					jumpReleased = false;
					animations.playSound("Jump"); 
					setState("WallJump");
					setFrame(0);
				}
				else if (onGround && hasJump && !state.equalsIgnoreCase("Wall Slide")) {
					jump();
					hasJump = false;
					jumpReleased = false;
				}
				else if (!onGround && hasDoubleJump) {
					doubleJump();
					hasDoubleJump = false;
				}
				jumpJustPressed = false;
			}
		}

		if (speed.x < 5 && speed.x > -5 && onGround) {
			if (lastSafePosition == null) {
				lastSafePosition = new Vector2D(0, 0);
			}
			
			
			lastSafePosition.x = position.x;
			lastSafePosition.y = position.y;
		}
		
		resolveAnimations();
	}

	@Override
	public void move(){
		if(GeneralSave.noClip) {
			speed.multiply(Units.TICK.number);
			position.add(speed);
			return;
		}
		super.move();
	}

	private void resolveAnimations() {
		if (isDead) {
			if (!state.equalsIgnoreCase("Death")) {
				setState("Death");
				setFrame(0);
			}
			if (frame >= animations.animations.get("Death").size() - 1) {
				frame = animations.animations.get("Death").size() - 1;
			}
			return;
		}

		
		if (hitLockTimer > 0 || spellLockout > 0 || dashDuration > 0 || slashAnimTimer > 0) return;

		
		if (focusState == FocusState.START || focusState == FocusState.LOOP
				|| focusState == FocusState.GET || focusState == FocusState.END) {
			return;
		}


		if (GeneralSave.noClip) {
			if (speed.getY() < -0.5) {
				if (!state.equalsIgnoreCase("AirBorne")) {
					setState("AirBorne");
					setFrame(0);
				}
			} else if (speed.getY() > 0.5) {
				if (!state.equalsIgnoreCase("Fall")) {
					setState("Fall");
					setFrame(0);
				}
			} else if (speed.getX() != 0) {
				if (!state.equalsIgnoreCase("Run")) {
					setState("Run");
					setFrame(0); 
				}
			} else {
				lookTimer = 0;
				setState("Idle");
			}
			return; 
		}

		if (onGround && (state.equalsIgnoreCase("Fall") || state.equalsIgnoreCase("AirBorne"))) {
			setState("Landing");
			setFrame(0);
			landingTimer = 12;
			return;
		}
		if (landingTimer > 0) return;

		if (!onGround) {
			boolean isPushingIntoWall = (right && isTouchingWall == 1) || (left && isTouchingWall == -1);

			if (isTouchingWall != 0  && isPushingIntoWall) {
				if (!state.equalsIgnoreCase("Wall Slide")) {
					setState("Wall Slide");
					setFrame(0);
				}
			} else if (speed.getY() < -0.5) {
				if (!state.equalsIgnoreCase("Double Jump") && !state.equalsIgnoreCase("WallJump")) {
					if (!state.equalsIgnoreCase("AirBorne")) {
						setState("AirBorne");
						setFrame(0);
					}
				}
			} else if (speed.getY() > 0.5) {
				
				
				if (!state.equalsIgnoreCase("AirBorne")) {
					setState("Fall");
				}
			}
		} else {
			if (speed.getX() != 0) {
				if (!state.equalsIgnoreCase("Run")) {
					setState("Run");
					setFrame(0); 
				}
				lookTimer = 0;
			} else {
				if (up) {
					lookTimer++;
					if (lookTimer >= 60) { 
						if (!state.equalsIgnoreCase("LookUp")) {
							setState("LookUp");
							setFrame(0);
						}
					} else {
						setState("Idle");
					}
				} else if (down) {
					lookTimer++;
					if (lookTimer >= 60) {
						if (!state.equalsIgnoreCase("LookDown")) {
							setState("LookDown");
							setFrame(0);
						}
					} else {
						setState("Idle");
					}
				} else {
					lookTimer = 0; 
					setState("Idle");
				}
			}
		}
	}

	public void takeDamageHazard(int amount) {
		if (isDead || GeneralSave.godMode || isHazardRespawning) return;

		if (dialogueLocked) {
			dialogueLocked = false;
			if (room != null && room.getDialogueBox() != null) {
				room.endDialogue();
			}
		}

		
		focusState = FocusState.NONE;
		focusTickTimer = 0;

		curHp -= amount;
		frameSinceDamaged = 0;
		setFrame(0);
		room.panel.camera.triggerShake(20,10);

		if (curHp <= 0) {
			if(GeneralSave.emergencyHeal){
				GeneralSave.emergencyHeal=false;
				curHp = 1;
				hurtTimer = 70;
				hitLockTimer = 20;

				hasDash = true;
				hasDoubleJump = true;
				hasJump = true;

				speed.setY(-1.2 * Units.LENGTH.number);
				if (isFlipped) speed.setX(1.1 * Units.LENGTH.number);
				else speed.setX(-1.1 * Units.LENGTH.number);
				animations.playSound("Hurt"); 
				return;
			}
			curHp = 0;
			isDead = true;
			speed.setX(0);
			speed.setY(0);
			animations.playSound("Death"); 
		} else {
			animations.playSound("Hurt"); 
			
			isHazardRespawning = true;
			hazardRespawnTimer = HAZARD_RESPAWN_MAX_TIME;

			
			hurtTimer = HAZARD_RESPAWN_MAX_TIME + 60;

			
			dashDuration = 0;
			spellLockout = 0;

			
			speed.setY(-1.5 * Units.LENGTH.number);
			speed.setX(0);
		}
	}
	public void takeDamage(int amount) {
		
		boolean isShadeDashing = (dashDuration > 0 && state.equalsIgnoreCase("Shadow Dash"));
		if (isDead || hurtTimer > 0 || GeneralSave.godMode || isShadeDashing) return;

		room.panel.camera.triggerShake(20,10);
		if (dialogueLocked) {
			dialogueLocked = false;
			if (room != null && room.getDialogueBox() != null) {
				room.endDialogue();
			}
		}

		
		
		focusState = FocusState.NONE;
		focusTickTimer = 0;

		curHp -= amount;
		frameSinceDamaged=0;
		setFrame(0);

		if (curHp <= 0) {
			if(GeneralSave.emergencyHeal){
				GeneralSave.emergencyHeal=false;
				curHp=1;
				hurtTimer = 70;       
				hitLockTimer = 20;    

				
				hasDash = true;
				hasDoubleJump = true;
				hasJump = true;

				
				speed.setY(-1.2 * Units.LENGTH.number);

				
				if (isFlipped) speed.setX(1.1 * Units.LENGTH.number);
				else speed.setX(-1.1 * Units.LENGTH.number);
				animations.playSound("Hurt"); 
				return;
			}
			curHp = 0;
			isDead = true;
			speed.setX(0);
			speed.setY(0);
			animations.playSound("Death"); 
		} else {
			hurtTimer = 70;       
			hitLockTimer = 20;    

			
			hasDash = true;
			hasDoubleJump = true;
			hasJump = true;
			animations.playSound("Hurt"); 

			
			speed.setY(-1.2 * Units.LENGTH.number);

			
			if (isFlipped) speed.setX(1.1 * Units.LENGTH.number);
			else speed.setX(-1.1 * Units.LENGTH.number);
		}
	}
	public void attack() {
		animations.playSound("Slash"); 
		slashCooldown = 40;
		if(room.save.currentCharms.contains(SaveFile.Charm.QUICK_SLASH))
			slashCooldown=30;
		slashAnimTimer = 15;
		double centerX = this.position.x + (this.width / 2.0);
		double centerY = this.position.y + (this.height / 2.0);
		Slash slash = new Slash(centerX,centerY);
		slash.setRoom(room);

		boolean useAlt = false;
		if (comboWindowTimer > 0) {
			useAlt = true;
			comboWindowTimer = 0;
		} else {
			comboWindowTimer = 60;
		}

		if (isTouchingWall != 0 && !onGround && speed.getY() > 0) {
			if (isTouchingWall == 1) {
				slash.setState("Right");
				slash.setFlipped(true);
			} else if (isTouchingWall == -1) {
				slash.setState("Right");
				slash.setFlipped(false);
			} else {
				slash.setFlipped(isFlipped);
			}
		} else {
			if (down && !onGround) {
				setState("DownSlash");
				slash.setState("Down");
				slash.setWidth(70);
				slash.setHeight(120);
				if(isFlipped) slash.setFlipped(true);
			} else if (up) {
				setState("UpSlash");
				slash.setState("Up");
				slash.setWidth(70);
				slash.setHeight(120);
				if(isFlipped) slash.setFlipped(true);
			} else {
				if (useAlt) setState("SlashAlt");
				else setState("Slash");

				slash.setState("Right");
				slash.setFlipped(isFlipped);
			}
		}
		setFrame(0);
		slash.setDamage(5);
		if(room.save.currentCharms.contains(SaveFile.Charm.UNBREAKABLE_STRENGTH))
			slash.setDamage(10);
		room.getEntities().add(slash);
	}

	public void executeDash() {
		if(!onGround)
			hasDash = false;
		dashCooldown = 38;
		if(room.save.currentCharms.contains(SaveFile.Charm.DASH_MASTER))
			dashCooldown=24;

		
		if(room.save.currentCharms.contains(SaveFile.Charm.SHARP_SHADOW)) {
			dashDuration = (int)Units.DASH.number + (6 * (int)Units.HOLD.number);
			animations.playSound("Shade Dash"); 
			setState("Shadow Dash");
			dashEnemiesHit.clear(); 
		} else {
			dashDuration = (int)Units.DASH.number;
			animations.playSound("Dash");
			setState("Dash");
		}

		setFrame(0);
		speed.setY(0);

		if (isTouchingWall != 0) {
			hasDash=true;
			if (isTouchingWall == 1) {
				speed.setX(-2.4 * Units.LENGTH.number);
				isFlipped = true;
			} else if (isTouchingWall == -1) {
				speed.setX(2.4 * Units.LENGTH.number);
				isFlipped = false;
			}
		} else {
			if (isFlipped) speed.setX(-2.4 * Units.LENGTH.number);
			else speed.setX(2.4 * Units.LENGTH.number);
		}
		isTouchingWall = 0;
	}

	public void castSpell() {
		if (soul >= 90 && spellLockout <= 0) {
			soul -= 90;
			frameSinceSoul=0;
			speed.setY(0);
			stopX();

			
			double centerX = this.position.x + (this.width / 2.0);
			double centerY = this.position.y + (this.height / 2.0);
			boolean isUpgraded = false; 
			if(room.save.currentCharms.contains(SaveFile.Charm.VOID_HEART))
				isUpgraded=true;
			if (up) {
				
				animations.playSound("Shriek"); 
				setState("Scream");
				setFrame(0);
				spellLockout = 10 * (int)Units.HOLD.number; 

				
				Shriek shriek = new Shriek(0, 0, isUpgraded, room);

				
				shriek.getPosition().x = centerX - (shriek.getWidth() / 2.0);
				shriek.getPosition().y = centerY - shriek.getHeight()+width/2;

				room.getEntities().add(shriek);

			} else {
				
				animations.playSound("Fire Ball"); 
				setState("FireBall Cast");
				setFrame(0);
				spellLockout = 8 * (int)Units.HOLD.number; 

				
				if (isFlipped) speed.setX(0.8 * Units.LENGTH.number);
				else speed.setX(-0.8 * Units.LENGTH.number);

				Blast blast = new Blast(0, 0,room);
				blast.setFlipped(isFlipped);

				blast.getPosition().x = centerX - (blast.getWidth() / 2.0);
				blast.getPosition().y = centerY - (blast.getHeight() / 2.0);

				room.getEntities().add(blast);

				
				FireBall fireball = new FireBall(0, 0, isUpgraded, room);
				fireball.setFlipped(isFlipped);

				
				fireball.getPosition().x = centerX - (fireball.getWidth() / 2.0);
				fireball.getPosition().y = centerY - (fireball.getHeight() / 2.0);

				room.getEntities().add(fireball);
			}
		}
	}

	public void rightKey() { speed.setX(Units.LENGTH.number); isFlipped = false; }
	public void leftKey() { speed.setX(-Units.LENGTH.number); isFlipped = true; }
	public void stopX() { speed.setX(0); }

	public void releaseJump() {
		
		if (state.equalsIgnoreCase("WallJump") || state.equalsIgnoreCase("Double Jump")) return;

		
		if (speed.getY() < 0) {
			speed.setY(speed.getY() * 0.5);
		}
	}

	public void gravity() {
		speed.setY(speed.getY() + 3.5 * Units.LENGTH.number * Units.TICK.number);
		if (speed.getY() > 2.4 * Units.LENGTH.number) speed.setY(2.4 * Units.LENGTH.number);
	}

	public void slide() {
		speed.setY(speed.getY() + 3.5 * Units.LENGTH.number * Units.TICK.number);
		if (speed.getY() > 1.2 * Units.LENGTH.number) speed.setY(1.2 * Units.LENGTH.number);
	}

	public void gainSoul(int soul){
		this.soul+=soul;
		if(this.soul>=270){
			this.soul=270;
			return;
		}
		frameSinceSoul=0;
	}

	public void jump() {
		animations.playSound("Jump"); 
		speed.setY(-2.2 * Units.LENGTH.number);
		onGround = false;
	}
	public void doubleJump() {
		animations.playSound("Double Jump"); 
		speed.setY(-2.0 * Units.LENGTH.number);
		setState("Double Jump");
		setFrame(0);
		onGround = false;
	}
	public void pogo(){
		speed.setY(-2 * Units.LENGTH.number);
		onGround = false;
		resetDash();
		resetDoubleJump();

	}
	@Override
	public void paint(Graphics g) {

		if(GeneralSave.showHitbox) {
			g.setColor(Color.cyan);
			double kX = this.getPosition().x + 5;
			double kY = this.getPosition().y + 10;
			double kW = this.getWidth() - 10;
			double kH = this.getHeight() - 10;
			g.drawRect((int) kX, (int) kY, (int) kW, (int) kH);
		}

		
		if (hurtTimer > 0 && hurtTimer % 4 < 2) return;

		
		
		double scale = this.height / 127.0;

		
		int paintW = (int) (349 * scale);
		int paintH = (int) (186 * scale);

		
		int paintX = (int) (position.x - (148 * scale))+6;
		int paintY = (int) (position.y - (54 * scale));
		String name=this.state;
		if(name.equalsIgnoreCase("idle")&&curHp==1)
			name="idle hurt";
		try {
			
			if (isFlipped) {
				
				animations.paint(g, paintX, paintY, paintW, paintH, name, frame);
			} else {
				
				animations.paintFlipped(g, paintX, paintY, paintW, paintH, name, frame);
			}
		} catch (Exception e) {
			
			g.setColor(Color.RED);
			g.fillRect(position.getIntX(), position.getIntY(), (int)width, (int)height);
		}
	}
	public void resetDash() { hasDash = true; }
	public void resetJump() { hasJump = true; }
	public void resetDoubleJump() { hasDoubleJump = true; }

	
	public boolean isOnGround() { return onGround; }
	public void setOnGround(boolean onGround) { this.onGround = onGround; if(onGround) this.coyoteTimer=6; }

	public void setRight(boolean right) { this.right = right; }
	public void setLeft(boolean left) { this.left = left; }
	public void setUp(boolean up) { this.up = up; }
	public void setDown(boolean down) { this.down = down; }

	public boolean isJumpHeld() { return jumpHeld; }
	public void setJumpHeld(boolean jumpHeld) { this.jumpHeld = jumpHeld; }
	public void setJumpJustPressed(boolean jumpJustPressed) { this.jumpJustPressed = jumpJustPressed; }
	public void setJumpReleased(boolean jumpReleased) { this.jumpReleased = jumpReleased; }

	public boolean isDashHeld() { return dashHeld; }
	public void setDashHeld(boolean dashHeld) { this.dashHeld = dashHeld; }
	public void setDashJustPressed(boolean dashJustPressed) { this.dashJustPressed = dashJustPressed; }

	public boolean isAttackHeld() { return attackHeld; }
	public void setAttackHeld(boolean attackHeld) { this.attackHeld = attackHeld; }
	public void setAttackJustPressed(boolean attackJustPressed) { this.attackJustPressed = attackJustPressed; }

	public void setFocusHeld(boolean focusHeld) { this.focusHeld = focusHeld; }

	public boolean isQuickCastHeld() { return quickCastHeld; }
	public void setQuickCastHeld(boolean quickCastHeld) { this.quickCastHeld = quickCastHeld; }
	public void setQuickCastJustPressed(boolean quickCastJustPressed) { this.quickCastJustPressed = quickCastJustPressed; }

	public int getIsTouchingWall() { return isTouchingWall; }
	public void setIsTouchingWall(int isTouchingWall) { this.isTouchingWall = isTouchingWall; }
	public void setDialogueLocked(boolean locked) {
		this.dialogueLocked = locked;
	}

	public int getSoul() {
		return soul;
	}
	private Entity findZoteBote() {
		if (room == null) return null;
		for (Entity e : room.getEntities()) {
			if (e.getClass().getSimpleName().equals("ZoteBote")) {
				double dist = Math.sqrt(Math.pow(e.getPosition().x - this.position.x, 2) +
						Math.pow(e.getPosition().y - this.position.y, 2));
				if (dist <= 100) return e;
			}
		}
		return null;
	}
	public void setSoul(){
		soul=270;
	}

	private void respawnRoom() {
		
		Knight newKnight = new Knight(500, 300);

		
		Room newRoom = new Room(newKnight, new java.util.ArrayList<>(), room.save);
		newKnight.setRoom(newRoom);
		newRoom.panel = this.room.panel;

		
		if (room.save.nameOfLevel != null && room.save.nameOfLevel.equals("Crystal Peak")) {
			newRoom.nameOfLevel = "Crystal Peak";
			newRoom.song = "Crystal Peak.wav";
			newKnight.position.x=150;
			newKnight.position.y=150;
		}

		
		Model.Game.Camera camera = new Model.Game.Camera(0, 0, 1520, 880);
		camera.setTarget(newKnight);
		Model.Game.CameraBoundingBox roomBounds = new Model.Game.CameraBoundingBox(0, 0, 4000, 880);
		camera.addBound(roomBounds);

		
		View.MyFrame.onlyPanel.camera = camera;
		newRoom.panel.camera = camera; 

		
		String mapFilePath = "src/Model/Maps/" + room.save.nameOfLevel + ".txt";
		Model.Game.ObjectSpawner.spawnFromFile(mapFilePath, newRoom);

		
		if (room.panel != null) {
			room.panel.changeRoom(newRoom);
		}
	}
}