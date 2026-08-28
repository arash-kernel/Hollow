package Model.Game;

import Controller.SaveFile;
import Controller.GeneralSave;
import Model.Game.Enemies.WalkerTurnAround;
import Model.Game.FalseKnight.FalseKnight;
import Model.Game.Knight.Knight;
import View.Animations;
import View.MyPanel;

import java.awt.*;
import java.util.ArrayList;

public class Room {
	private static Animations animations=new Animations();
	private ArrayList<Line> boundaries = new ArrayList<>();
	private Knight knight;
	private ArrayList<Entity> entities=new ArrayList<>();
	private ArrayList<Projectile> projectiles=new ArrayList<>();
	private ArrayList<Particle> particles=new ArrayList<>();
	private boolean dialogue =false;
	private DialogueBox dialogueBox=null;
	public MyPanel panel;
	public SaveFile save;
	public String nameOfLevel="Green Path";
	public String song="Green Path.wav";

	static {
		animations.addAnimation("Green Path Map","src/Model/Maps/");
		animations.addAnimation("Green Path Fore Ground","src/Model/Maps/");
		animations.addAnimation("Crystal Peak Map","src/Model/Maps/");
		animations.addAnimation("Crystal Peak Fore Ground","src/Model/Maps/");
	}

	public Room(Knight knight, ArrayList<Line> boundaries,SaveFile save) {
		this.knight = knight;
		knight.room = this;
		this.boundaries = boundaries;
		this.save=save;
	}

	public ArrayList<WalkerTurnAround> turnArounds = new ArrayList<>();
	public ArrayList<Line> getBoundaries() { return boundaries; }
	public void setBoundaries(ArrayList<Line> boundaries) { this.boundaries = boundaries; }

	public void paint(Graphics g){

		if (animations != null) {
			animations.paint(g,0,0,4000,880,nameOfLevel + " Map",0);
		}

		if (GeneralSave.showHitbox) {
			g.setColor(Color.cyan);
			for (Line l : boundaries) {
				g.drawLine(l.x1.intValue(), l.y1.intValue(), l.x2.intValue(), l.y2.intValue());
			}
			g.setColor(Color.YELLOW);
			for (WalkerTurnAround wta : turnArounds) {
				g.setColor(Color.YELLOW);
				if(wta.right){
					g.setColor(Color.RED);
				}
				g.drawRect(
						(int) wta.position.x,
						(int) wta.position.y,
						(int) wta.width,
						(int) wta.height
				);
			}
		}

		for(Entity e: entities){
			e.paint(g);
			if(e.getClass()== FalseKnight.class){
				g.setColor(Color.CYAN);
				g.drawString("state: " + e.state, 800, 100);
				g.drawString("hp: " + e.hp, 900, 100);
				g.drawString("stunhits: " + ((FalseKnight) e).hitsTakenInStun, 1000, 100);
			}
		}
		for(Projectile e: projectiles){
			e.paint(g);
		}
		for(Particle e: particles){
			e.paint(g);
		}
		knight.paint(g);

        /*
        	if (animations != null) {
        	animations.paint(g,0,0,8000,1800,nameOfLevel + " Fore Ground",0);
        	}*/

		if (dialogueBox != null) {
			dialogueBox.movements();
			dialogueBox.paint(g);
		}
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public Knight getKnight() {
		return knight;
	}

	public void setKnight(Knight knight) {
		this.knight = knight;
	}

	public ArrayList<WalkerTurnAround> getTurnArounds() {
		return turnArounds;
	}
	public ArrayList<Projectile> getProjectiles() {
		return projectiles;
	}

	public void startDialogue(ArrayList<Message> messages) {
		this.dialogueBox = new DialogueBox(messages, this, knight);
		this.dialogue = true;
	}

	public void endDialogue() {
		this.dialogueBox = null;
		this.dialogue = false;
	}

	public DialogueBox getDialogueBox() {
		return dialogueBox;
	}

	public void movement() {
		save.time++;
		knight.movements();
		knight.move();
		for(int i= entities.size()-1;i>=0;i--){
			Entity e=entities.get(i);
			e.movements();
			e.move();
		}
		if(!projectiles.isEmpty())
			for(int i= projectiles.size()-1;i>=0;i--){
				Projectile e=projectiles.get(i);
				e.movements();
				e.move();
			}
		if(!particles.isEmpty())
			for(int i= particles.size()-1;i>=0;i--){
				Particle e=particles.get(i);
				e.movements();
				e.move();
				if(e.isDead()){
					particles.remove(this);
				}
			}
		if(nameOfLevel.equalsIgnoreCase("Green path")){
			summonParticle(1,panel.camera.getOffsetX(),panel.camera.getOffsetY(),(int)panel.camera.width,(int)panel.camera.height,1,1);
		}
		if(nameOfLevel.equalsIgnoreCase("Crystal peak")){
			summonParticle(2,panel.camera.getOffsetX(),panel.camera.getOffsetY(),(int)panel.camera.width,(int)panel.camera.height,1,1);
		}
	}

	public void summonParticle(int type, int x, int y, int width, int height, int count, int time) {
		java.util.Random random = new java.util.Random();


		int lifeTimeTicks = time * 60;

		for (int i = 0; i < count; i++) {

			double spawnX = x + (random.nextDouble() * width);
			double spawnY = y + (random.nextDouble() * height);

			particles.add(new Particle(spawnX, spawnY, this, type, lifeTimeTicks));
		}
	}
}