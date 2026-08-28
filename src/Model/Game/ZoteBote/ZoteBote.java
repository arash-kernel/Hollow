package Model.Game.ZoteBote;

import Controller.GeneralSave;
import Controller.SaveManager;
import Model.Game.*;
import Model.Game.Enemies.GettingHit;
import Model.Game.Knight.Knight;
import View.Animations;
import View.MyFrame;

import java.awt.Graphics;
import java.util.ArrayList;

/**
 * ZoteBote boss:
 * - TALK mode: cycles dialogue sets (player-triggered externally)
 * - ATTACK mode: chases player and bounces back on hit (Colosseum of Fools style)
 */
public class ZoteBote extends Entity implements GettingHit {

	private static Animations animations = new Animations();
	private boolean voidHeart=false;
	private boolean transfer=false;

	static {
		String[] animNames = {
				"Idle", "Attack"
		};
		for (String name : animNames) {
			
			animations.addAnimation(name, "src/Model/Game/ZoteBote/ZoteBoteAnimations/");
		}

		
		animations.addSound("hurt", "src/Model/Game/ZoteBote/ZoteBoteAnimations/Sounds/");
		animations.addSound("Tonk", "src/Model/Game/ZoteBote/ZoteBoteAnimations/Sounds/");

	}

	public enum State {
		TALK,
		ATTACK
	}

	public State state = State.TALK;

	
	
	public ArrayList<ArrayList<Message>> dialogueSets;
	public int dialogueIndex = -1;

	private DialogueBox activeDialogue = null;

	
	private double attackSpeed = 0.8 * Units.LENGTH.number;

	private int hitCooldown = 0;
	private int angryFrames = 0; 

	
	private int tickCounter = 0;

	public ZoteBote(double x, double y, Room room, ArrayList<ArrayList<Message>> dialogueSets,boolean voidHeart,boolean transfer) {
		super(x, y, room);

		this.dialogueSets = dialogueSets;

		this.width = 210*0.4;
		this.height = 100;

		this.hp = 50;
		this.curHp = 50;

		this.state = State.TALK;
		this.hold = 6; 

		this.voidHeart=voidHeart;
		this.transfer = transfer; 
		
		room.getEntities().add(this);
	}

	
	private int getFps() {
		return (int)(1.0 / Units.TICK.number);
	}

	private int getAnimSize(String name) {
		if (animations.animations.containsKey(name)) {
			return animations.animations.get(name).size();
		}
		return 1;
	}

	/**
	 * Called externally by player interaction system (YOU hook this in)
	 */
	public boolean triggerTalk() {
		if (state != State.TALK) return false;

		dialogueIndex++;
		if (dialogueIndex >= dialogueSets.size() && transfer){
			if (room.save != null) {
				
				boolean isCurrentlyCrystal = room.save.nameOfLevel.equalsIgnoreCase("Crystal Peak");
				String nextLevel = isCurrentlyCrystal ? "Green Path" : "Crystal Peak";
				String mapFile = "src/Model/Maps/" + nextLevel + ".txt";

				
				room.save.nameOfLevel = nextLevel;

				
				SaveManager.saveToFile(room.save);

				
				if (room.panel != null) {
					Knight knight = room.getKnight();

					
					knight.getPosition().setX(500);
					knight.getPosition().setY(300);
					knight.movingToTalk = false;
					knight.setDialogueLocked(false);

					Room newRoom = new Room(knight, new ArrayList<>(), room.save);
					knight.setRoom(newRoom);
					newRoom.panel = room.panel;
					if(nextLevel.equalsIgnoreCase("Crystal Peak")){
						knight.getPosition().x=150;
						knight.getPosition().y=150;
					}
					Camera camera = new Camera(0, 0, 1520, 880);
					camera.setTarget(knight);
					CameraBoundingBox roomBounds = new CameraBoundingBox(0, 0, 4000, 880);
					camera.addBound(roomBounds);

					newRoom.nameOfLevel = nextLevel;
					newRoom.song = nextLevel + ".wav";
					MyFrame.onlyPanel.camera = camera;

					if (MyFrame.onlyPanel != null && MyFrame.onlyPanel.camera != null) {
						MyFrame.onlyPanel.camera.setTarget(knight);
					}

					
					Model.Game.ObjectSpawner.spawnFromFile(mapFile, newRoom);
					room.panel.changeRoom(newRoom);
					return true;
				}
			}
		}

		if (dialogueIndex >= dialogueSets.size()) dialogueIndex--;

		ArrayList<Message> messages = dialogueSets.get(dialogueIndex);

		activeDialogue = new DialogueBox(messages, room, room.getKnight());
		room.startDialogue(messages);
		GeneralSave.talkToZote=true;
		if(voidHeart){
			room.save.voidHeart=true;
		}
		return true;
	}


	public void onDialogueFinished() {
		dialogueIndex++;

		if (dialogueIndex >= dialogueSets.size()) {
			dialogueIndex= dialogueSets.size()-1;
		}
	}

	@Override
	public void movements() {
		Knight k = room.getKnight();

		tickCounter++;
		if (tickCounter >= hold) {
			frame++;
			tickCounter = 0;
		}

		String animState = (state == State.TALK) ? "Idle" : "Attack";
		if (frame >= getAnimSize(animState)) {
			frame = 0;
		}

		if (hitCooldown > 0) hitCooldown--;

		speed.y += 3.5 * Units.LENGTH.number * Units.TICK.number;

		switch (state) {
			case TALK:
				speed.x = 0;
				
				if (k.getPosition().x < position.x) {
					isFlipped = true;
				} else if (k.getPosition().x > position.x) {
					isFlipped = false;
				}
				break;

			case ATTACK:
				angryFrames--;

				
				if (angryFrames <= 0) {
					if (state != State.TALK) {
						state = State.TALK;
						frame = 0; 
					}
					speed.x = 0;
				}
				
				else if (hitCooldown == 0) {
					double dir = Math.signum(
							(k.getPosition().x + k.getWidth() / 2) -
									(position.x + width / 2)
					);
					speed.x = dir * attackSpeed;

					
					if (speed.x < 0) {
						isFlipped = true;
					} else if (speed.x > 0) {
						isFlipped = false;
					}
				}
				break;
		}

		if (knockbackFrames > 0) {
			speed.x += activeKnockback.x;
			speed.y += activeKnockback.y;
			knockbackFrames--;
		}
	}

	@Override
	public void move() {
		super.move();

		
		if (state == State.ATTACK && hitCooldown == 0) {
			Knight k = room.getKnight();

			if (intersectsKnight(k)) {
				bounceZote(k);
				hitCooldown = 30; 
				angryFrames -= 5*getFps(); 
			}
		}
	}

	private boolean intersectsKnight(Knight k) {
		
		

		
		return position.x < k.getPosition().x + k.getWidth() &&
				position.x + width > k.getPosition().x &&
				position.y < k.getPosition().y + k.getHeight() &&
				position.y + height > k.getPosition().y;
	}

	private void bounceZote(Knight k) {
		
		animations.playSound("Tonk");

		
		double dir = Math.signum(position.x - k.getPosition().x);
		if (dir == 0) dir = 1; 

		
		this.speed.x = dir * 2 * Units.LENGTH.number;
		this.speed.y = -0.8 * Units.LENGTH.number;
	}

	@Override
	public void paint(Graphics g) {
		String animState = (state == State.TALK) ? "Idle" : "Attack";

		if (!isFlipped) {
			animations.paintFlipped(g, (int) (position.x-140*0.3), (int) position.y, (int) (width+140*0.7), (int) height, animState, frame);
		} else {
			animations.paint(g, (int) (position.x-140*0.3), (int) position.y, (int) (width+140*0.7), (int) height, animState, frame);
		}
	}

	private Vector2D activeKnockback = new Vector2D(0, 0);
	private int knockbackFrames = 0;

	@Override
	public void takeDamage(int damage, Vector2D knockback) {
		animations.playSound("hurt"); 

		
		if (this.state != State.ATTACK) {
			this.state = State.ATTACK;
			this.frame = 0; 
			this.tickCounter = 0;
		}
		this.angryFrames = 25 * getFps();

		
		this.activeKnockback = new Vector2D(knockback.x , knockback.y );
		this.knockbackFrames = 6;
	}

	public String getState(){
		return state.toString();
	}
	@Override
	public void doDamage(){

	}
}