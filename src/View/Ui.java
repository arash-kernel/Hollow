package View;

import Controller.SaveFile;
import Controller.SystemController;
import Model.Game.Knight.Knight;
import Model.Game.Knight.SoulOrb;
import Model.Game.Projectile;
import Model.Game.Room;
import View.Uis.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

public class Ui {
	private static Room room;
	private static Animations animations = new Animations();

	
	private MainMenuUi mainMenuUi;
	private GuideUi guideUi;
	private static StartGameUi startGameUi;
	private SettingsUi settingsUi; 
	private RemappingUi remappingUi;
	private AchievementsUi achievementsUi;
	private PauseUi pauseUi;
	private InventoryUi inventoryUi;
	private VictoryUi victoryUi;

	
	public Ui(SaveFile saveFile) {
		this.inventoryUi = new InventoryUi(saveFile);
		
	}

	public InventoryUi getInventoryUi() {
		return inventoryUi;
	}
	static {
		String[] animNames = {
				"HealthBar",
				"BreakHealth",
				"EmptyHealth",
				"FilledHealth",
				"FilledHealthShine",
				"HealthRefill",
				"SoulOrbFull",
				"SoulEffect",
				"SoulEye"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/View/UiFrames/");
		}
	}

	public Ui(Room room){
		this.inventoryUi = new InventoryUi(room.save);
		this.room = room;
		this.mainMenuUi = new MainMenuUi(this.animations);
		this.guideUi = new GuideUi(); 
		this.startGameUi = new StartGameUi();
		this.settingsUi=new SettingsUi();
		this.remappingUi=new RemappingUi();
		this.achievementsUi=new AchievementsUi();
		this.pauseUi=new PauseUi(room.getKnight(),room.save);
		this.victoryUi = new VictoryUi(room.save);
	}

	public void paint(Graphics g){
		Knight knight = room.getKnight();

		
		animations.paint(g, 50, 50, 200, 120, "HealthBar", 5);

		if(knight.getSoul() < 270 && knight.getSoul()>0) { 
			int soulX = 67;
			int soulY = 77;
			int soulSize = 84;
			int maxSoul = 270;

			
			double soulRatio = Math.min(1.0, Math.max(0.0, (double) knight.getSoul() / maxSoul));
			int liquidHeight = (int) (soulSize * soulRatio);
			int baseY = soulY + soulSize - liquidHeight; 

			
			double frequency = 0.06;


			double phase = (System.currentTimeMillis() / 200.0);

			double milkyPhase = phase + 1.2;

			double amplitude = 2.0;
			int waveThreshold = 5;

			if (knight.frameSinceSoul < waveThreshold) {
				double damping = 1.0 - ((double) knight.frameSinceSoul / waveThreshold);
				amplitude += 8.0 * damping;
				phase += knight.frameSinceSoul * 0.4;
				milkyPhase += knight.frameSinceSoul * 0.3; 
			}


			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


			g2d.setClip(new Ellipse2D.Double(soulX, soulY, soulSize, soulSize));


			GeneralPath liquidPath = new GeneralPath();
			GeneralPath milkyPath = new GeneralPath();


			liquidPath.moveTo(soulX, soulY + soulSize);
			milkyPath.moveTo(soulX, soulY + soulSize);


			for (int x = soulX; x <= soulX + soulSize; x++) {
				double waveOffset = 0;
				double milkyWaveOffset = 0;

				if (amplitude > 0) {
					waveOffset = Math.sin((x - soulX) * frequency + phase) * amplitude;
					milkyWaveOffset = Math.sin((x - soulX) * frequency + milkyPhase) * amplitude;
				}

				liquidPath.lineTo(x, baseY + waveOffset);
				milkyPath.lineTo(x, baseY + 5 + milkyWaveOffset); 
			}

			
			liquidPath.lineTo(soulX + soulSize, soulY + soulSize);
			liquidPath.closePath();

			milkyPath.lineTo(soulX + soulSize, soulY + soulSize);
			milkyPath.closePath();

			
			g2d.setColor(new Color(255, 255, 255, 255));
			if(knight.getSoul()<90)
				g2d.setColor(new Color(140, 140, 140, 255));
			g2d.fill(liquidPath);

			
			g2d.setColor(new Color(255, 248, 225, 200));
			if(knight.getSoul()<90)
				g2d.setColor(new Color(115, 114, 97, 255));
			g2d.fill(milkyPath);

			g2d.dispose();
			
			if(knight.getSoul() > 130)
				animations.paint(g, 77, 125, 64, 26, "SoulEye", 0);
		}
		else if(knight.getSoul()!=0)
			animations.paint(g, 51, 47, 190, 124, "SoulOrbFull", 0);

		if(knight.getSoul() >= 270 && knight.frameSinceSoul < 5){
			animations.paint(g, 10, 30, 200, 200, "SoulEffect", knight.frameSinceSoul);
		}

		
		animations.paint(g, 170, 50, 80, 100, "EmptyHealth", 0);
		animations.paint(g, 240, 50, 80, 100, "EmptyHealth", 0);
		animations.paint(g, 310, 50, 80, 100, "EmptyHealth", 0);
		animations.paint(g, 380, 50, 80, 100, "EmptyHealth", 0);
		animations.paint(g, 450, 50, 80, 100, "EmptyHealth", 0);

		for(int i = 1; i <= 5; i++){
			if(i <= knight.getCurHp()){
				animations.paint(g, 100 + i * 70, 50, 80, 100, "FilledHealth", 0);
			}
			if(knight.frameSinceHealed < 5){
				animations.paint(g, 100 + knight.getCurHp() * 70, 50, 80, 100, "EmptyHealth", 0);
				animations.paint(g, 100 + knight.getCurHp() * 70, 50, 80, 100, "HealthRefill", knight.frameSinceHealed);
			}
			if(knight.frameSinceDamaged < 6){
				animations.paint(g, 170 + knight.getCurHp() * 70, 50, 80, 100, "EmptyHealth", 0);
				animations.paint(g, 170 + knight.getCurHp() * 70, 50, 80, 100, "BreakHealth", knight.frameSinceHealed);
			}
			if(knight.getCurHp() == 5 && knight.frameSinceHealed > 50){
				animations.paint(g, 100 + i * 70, 50, 80, 100, "FilledHealthShine", (knight.frameSinceHealed / 4) % 5);
			}
		}

		
		for(Projectile p : room.getProjectiles()){
			if(p.getClass() == SoulOrb.class){
				
				int screenX = p.getPosition().getIntX() - room.panel.camera.getOffsetX();
				int screenY = p.getPosition().getIntY() - room.panel.camera.getOffsetY();

				SoulOrb.animations.paint(g, screenX - 5, screenY - 5, 10, 10, "SoulBall", 0);
			}
		}

		if (SystemController.getCurrentState() == SystemController.GameState.INVENTORY) {
			inventoryUi.paint(g);
		}
		if (SystemController.getCurrentState() == SystemController.GameState.SETTINGS &&
				SettingsUi.previousState != SystemController.GameState.MAIN_MENU) {
			settingsUi.paint(g, 1540, 880);
		}
		if (SystemController.getCurrentState() == SystemController.GameState.REMAPPING &&
				SettingsUi.previousState != SystemController.GameState.MAIN_MENU) {
			remappingUi.paint(g, 1540, 880);
		}
		if (SystemController.getCurrentState() == SystemController.GameState.PAUSED) {
			if (pauseUi != null) {
				pauseUi.paint(g, 1620, 880);
			}
		}
		if (SystemController.getCurrentState() == SystemController.GameState.VICTORY) {
			if (victoryUi != null) {
				
				victoryUi.paint(g, 1620, 880);
			}
		}
		if (achievementsUi != null) {
			achievementsUi.updateAndDrawPopups((Graphics2D) g, 1540, 880);
		}
	}

	public static void setRoom(Room room) {
		Ui.room = room;
	}
	public GuideUi getGuideUi() {return guideUi;}
	public static StartGameUi getStartGameUi(){return  startGameUi;}
	public SettingsUi getSettingsUi() {return settingsUi;}
	public RemappingUi getRemappingUi() {return remappingUi;}
	public MainMenuUi getMainMenuUi() {return mainMenuUi;}
	public AchievementsUi getAchievementsUi() {return achievementsUi;}
	public PauseUi getPauseUi() { return this.pauseUi; }
	public VictoryUi getVictoryUi() {
		return victoryUi;
	}
	public void setPanel(MyPanel panel) {
		if (this.startGameUi != null) {
			this.startGameUi.setPanel(panel);
		}
	}
}
