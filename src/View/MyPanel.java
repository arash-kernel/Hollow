package View;

import Controller.GeneralSave;
import Controller.SystemController;
import Controller.MusicManager;
import Model.Game.Camera;
import Model.Game.Room;
import View.Uis.SettingsUi;

import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {
	Room room;
	Ui ui;
	public static Camera camera;

	public MyPanel(Room room){
		this.room = room;
		this.setPreferredSize(new Dimension(1620,880));
	}

	public MyPanel(Room room, Ui ui){
		this.room = room;
		this.ui = ui;
		this.setPreferredSize(new Dimension(1620,880));
		setCustomCursor();
	}

	
	public void manageMusic() {
		SystemController.GameState state = SystemController.getCurrentState();

		
		float baseVolume = GeneralSave.isMuted ? 0f : GeneralSave.musicVolume;

		switch (state) {
			case VICTORY:
				MusicManager.playMusic("Victory.wav", baseVolume);
				break;

			case MAIN_MENU:
			case START_GAME:
			case GUIDE:
			case ACHIEVEMENTS:
				MusicManager.playMusic("Main Menu.wav", baseVolume);
				break;

			case PLAYING:
			case INVENTORY:
				
				MusicManager.playMusic(room.song, baseVolume);
				break;

			case PAUSED:
				
				MusicManager.playMusic(room.song, baseVolume * 0.3f);
				break;

			case SETTINGS:
			case REMAPPING:
				
				if (SettingsUi.previousState == SystemController.GameState.MAIN_MENU) {
					MusicManager.playMusic("Main Menu.wav", baseVolume);
				} else {
					
					MusicManager.playMusic(room.song, baseVolume * 0.3f);
				}
				break;
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2D = (Graphics2D) g;

		
		manageMusic();

		if(true) {
			
			if (SystemController.getCurrentState() == SystemController.GameState.MAIN_MENU) {
				if (ui != null && ui.getMainMenuUi() != null) {
					ui.getMainMenuUi().paint(g2D, getWidth(), getHeight());
				}
				return;
			}
			
			if (SystemController.getCurrentState() == SystemController.GameState.SETTINGS) {
				if (SettingsUi.previousState == SystemController.GameState.MAIN_MENU) {
					if (ui != null && ui.getSettingsUi() != null) {
						ui.getSettingsUi().paint(g2D, getWidth(), getHeight());
					}
					return;
				}
			}
			if (SystemController.getCurrentState() == SystemController.GameState.GUIDE) {
				if (ui != null && ui.getGuideUi() != null) {
					ui.getGuideUi().paint(g2D, getWidth(), getHeight());
				}
				return;
			}
			if (SystemController.getCurrentState() == SystemController.GameState.START_GAME) {
				if (ui != null && ui.getStartGameUi() != null) {
					ui.getStartGameUi().paint(g2D, getWidth(), getHeight());
				}
				return;
			}
			if (SystemController.getCurrentState() == SystemController.GameState.REMAPPING) {
				if (SettingsUi.previousState == SystemController.GameState.MAIN_MENU) {
					if (ui != null && ui.getRemappingUi() != null) {
						ui.getRemappingUi().paint(g2D, getWidth(), getHeight());
					}
					return;
				}
			}
			if (SystemController.getCurrentState() == SystemController.GameState.ACHIEVEMENTS) {
				if (ui != null && ui.getAchievementsUi() != null) {
					ui.getAchievementsUi().paint(g2D, getWidth(), getHeight());
				}
				return;
			}

			
			g.setColor(Color.BLACK);
			g2D.fillRect(0, 0, 1520, 1000);

			
			java.awt.geom.AffineTransform originalTransform = g2D.getTransform();

			
			if (camera != null) {
				camera.movements();
				g2D.translate(-camera.getOffsetX(), -camera.getOffsetY());
			}

			if (SystemController.getCurrentState() == SystemController.GameState.PLAYING) {
				room.movement();
			} else if (SystemController.getCurrentState() == SystemController.GameState.INVENTORY) {
				if (ui != null && ui.getInventoryUi() != null) {
					ui.getInventoryUi().movements();
				}
			}

			
			room.paint(g);

			
			if(room.save.killedFly && room.save.killedHunter && room.save.killedCrawler && room.save.killedLaser && room.save.killedHorn && room.save.killedMoss){
				GeneralSave.trueHunter = true;
			}
			if(room.save.killedBoss){
				GeneralSave.defeatFalseKnight = true;
				if(room.save.voidHeart){
					GeneralSave.completion = true;
					if(room.save.time <= 60*60*5)
						GeneralSave.speedrun = true;
				}
			}

			
			g2D.setTransform(originalTransform);

			
			int alpha = (int) ((1.0f - GeneralSave.brightness) * 100);
			alpha = Math.max(0, Math.min(100, alpha));

			if (alpha > 0) {
				g2D.setColor(new Color(30, 30, 30, alpha));
				g2D.fillRect(0, 0, getWidth(), getHeight());
			}

			if (ui != null) {
				ui.paint(g);
			}
		}
	}

	public void changeRoom(Room newRoom){
		room=newRoom;
		Ui.setRoom(newRoom);
		ui.getInventoryUi().saveFile=newRoom.save;
		ui.getPauseUi().saveFile=newRoom.save;
		ui.getVictoryUi().saveFile=newRoom.save;
		ui.getStartGameUi().loadAllSaves();
		ui.getPauseUi().knight=newRoom.getKnight();
	}

	public void setCustomCursor() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		// Load your custom image (16x16 or 32x32 pixels recommended)
		Image image = new ImageIcon("src/View/cursor.png").getImage();

		// Point(0, 0) defines the "hotspot" (the exact pixel that clicks)
		Cursor customCursor = toolkit.createCustomCursor(
				image,
				new Point(0, 0),
				"MyCustomCursor"
		);

		this.setCursor(customCursor);
	}
}