package View.Uis;

import Controller.GeneralSave;
import Controller.SaveFile;
import Controller.SystemController;
import Model.Game.Knight.Knight;
import Controller.SaveManager; // <--- ADD THIS IMPORT
import View.Animations;

import java.awt.*;
import java.io.File;

public class PauseUi {
	public Knight knight;
	public SaveFile saveFile; // Added to manipulate equipped charms
	private final Animations animations = new Animations();
	private static Font trajan;

	private int screenWidth = 1620;
	private int screenHeight = 880;

	private int hoveredMainIndex = -1;
	private int hoveredCheatIndex = -1;

	private final String[] mainOptions = {"CONTINUE", "SETTINGS", "EXIT"};
	private final String[] cheatOptions = {
			"BOSS ARENA TELEPORT",
			"NO CLIP: ",
			"EMERGENCY HEAL: ",
			"REFILL SOUL VESSEL",
			"GOD MODE: ",
			"INFINITE CHARMS: ",
			"SHOW HITBOX: "
	};

	static {
		try {
			File fontFile = new File("src/Model/Fonts/TrajanPro-Regular.ttf");
			if (fontFile.exists()) {
				trajan = Font.createFont(Font.TRUETYPE_FONT, fontFile);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(trajan);
			} else {
				trajan = new Font("Arial", Font.PLAIN, 20);
			}
		} catch (Exception e) {
			trajan = new Font("Arial", Font.PLAIN, 20);
		}
	}

	// Updated constructor to require SaveFile
	public PauseUi(Knight knight, SaveFile saveFile) {
		this.knight = knight;
		this.saveFile = saveFile;
		try {
			animations.addAnimation("Idle2", "src/Model/Game/DialogueBoxAnimations/");

			// Load Main Menu Synchronized Sounds
			animations.addSound("Hover", "src/View/MenuSounds/");
			animations.addSound("Click", "src/View/MenuSounds/");
		} catch (Exception e) {
			System.out.println("Failed to load assets in PauseUi: " + e.getMessage());
		}
	}

	public void paint(Graphics g, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 1. Transparent Dark Overlay (Inventory Background Style)
		g2d.setColor(new Color(0, 0, 0, 200));
		g2d.fillRect(0, 0, screenWidth, screenHeight);

		// 2. Header Title
		String title = "PAUSED";
		g2d.setFont(trajan.deriveFont(Font.BOLD, 48f));
		g2d.setColor(new Color(235, 235, 240));
		FontMetrics titleMetrics = g2d.getFontMetrics();
		g2d.drawString(title, (screenWidth - titleMetrics.stringWidth(title)) / 2, 120);

		// 3. Center-Aligned Main Navigation Buttons (Main Menu Style)
		int btnWidth = 320;
		int btnHeight = 55;
		int startY = (screenHeight / 2) - ((mainOptions.length * (btnHeight + 25)) / 2);
		int spacing = 25;

		for (int i = 0; i < mainOptions.length; i++) {
			int btnX = (screenWidth - btnWidth) / 2;
			int btnY = startY + i * (btnHeight + spacing);

			if (i == hoveredMainIndex) {
				animations.paint(g2d, btnX, btnY, btnWidth, btnHeight, "Idle2", 0);
				g2d.setColor(Color.WHITE);
			} else {
				g2d.setColor(new Color(160, 160, 170));
			}

			g2d.setFont(trajan.deriveFont(Font.PLAIN, 22f));
			FontMetrics fm = g2d.getFontMetrics();
			int textX = btnX + (btnWidth - fm.stringWidth(mainOptions[i])) / 2;
			int textY = btnY + ((btnHeight - fm.getHeight()) / 2) + fm.getAscent();
			g2d.drawString(mainOptions[i], textX, textY);
		}

		// 4. Cheat Menu Side Panel
		int panelWidth = 420;
		int panelHeight = screenHeight - 240;

		// Moved the panel further to the left by changing offset from 60 to 150
		int panelX = screenWidth - panelWidth - 150;
		int panelY = 150;

		animations.paint(g2d, panelX, panelY, panelWidth, panelHeight, "Idle2", 0);

		g2d.setFont(trajan.deriveFont(Font.BOLD, 24f));
		g2d.setColor(Color.WHITE);
		g2d.drawString("CHEAT MENU", panelX + 120, panelY + 130);

		int cheatStartY = panelY + 160;
		int cheatBtnHeight = 45;
		int cheatSpacing = 12;
		int cheatBtnWidth = panelWidth - 60;

		for (int i = 0; i < cheatOptions.length; i++) {
			int cBtnX = panelX + 30;
			int cBtnY = cheatStartY + i * (cheatBtnHeight + cheatSpacing);

			if (i == hoveredCheatIndex) {
				animations.paint(g2d, cBtnX, cBtnY, cheatBtnWidth, cheatBtnHeight, "Idle2", 0);
				g2d.setColor(Color.WHITE);
			} else {
				g2d.setColor(new Color(160, 160, 170));
			}

			// Append states for active toggles dynamically
			String optionText = cheatOptions[i];
			switch (i) {
				case 1: optionText += (GeneralSave.noClip ? "ON" : "OFF"); break;
				case 2: optionText += (GeneralSave.emergencyHeal ? "ON" : "OFF"); break;
				case 4: optionText += (GeneralSave.godMode ? "ON" : "OFF"); break;
				case 5: optionText += (GeneralSave.infiniteCharms ? "ON" : "OFF"); break;
				case 6: optionText += (GeneralSave.showHitbox ? "ON" : "OFF"); break;
			}

			g2d.setFont(trajan.deriveFont(Font.PLAIN, 16f));
			FontMetrics cfm = g2d.getFontMetrics();
			int textX = cBtnX + (cheatBtnWidth - cfm.stringWidth(optionText)) / 2;
			int textY = cBtnY + ((cheatBtnHeight - cfm.getHeight()) / 2) + cfm.getAscent();
			g2d.drawString(optionText, textX, textY);
		}
	}

	public void handleMouseMove(int mouseX, int mouseY) {
		int previousMainIndex = hoveredMainIndex;
		int previousCheatIndex = hoveredCheatIndex;

		// Track Main Buttons
		int btnWidth = 320;
		int btnHeight = 55;
		int startY = (screenHeight / 2) - ((mainOptions.length * (btnHeight + 25)) / 2);
		int spacing = 25;
		hoveredMainIndex = -1;

		for (int i = 0; i < mainOptions.length; i++) {
			int btnX = (screenWidth - btnWidth) / 2;
			int btnY = startY + i * (btnHeight + spacing);
			if (mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
				hoveredMainIndex = i;
				break;
			}
		}

		// Track Cheat Buttons
		int panelWidth = 420;
		int panelX = screenWidth - panelWidth - 150;
		int panelY = 150;
		int cheatStartY = panelY + 160;
		int cheatBtnHeight = 45;
		int cheatSpacing = 12;
		int cheatBtnWidth = panelWidth - 60;
		hoveredCheatIndex = -1;

		for (int i = 0; i < cheatOptions.length; i++) {
			int cBtnX = panelX + 30;
			int cBtnY = cheatStartY + i * (cheatBtnHeight + cheatSpacing);
			if (mouseX >= cBtnX && mouseX <= cBtnX + cheatBtnWidth && mouseY >= cBtnY && mouseY <= cBtnY + cheatBtnHeight) {
				hoveredCheatIndex = i;
				break;
			}
		}

		// Sound verification exactly matching MainMenuUi layout tracking rules
		boolean mainChanged = (hoveredMainIndex != previousMainIndex && hoveredMainIndex != -1);
		boolean cheatChanged = (hoveredCheatIndex != previousCheatIndex && hoveredCheatIndex != -1);

		if (mainChanged || cheatChanged) {
			animations.playSound("Hover");
		}
	}

	public void handleMouseClick(int mouseX, int mouseY) {
		handleMouseMove(mouseX, mouseY);

		if (hoveredMainIndex != -1) {
			animations.playSound("Click");
			switch (hoveredMainIndex) {
				case 0: // CONTINUE
					SystemController.setCurrentState(SystemController.GameState.PLAYING);
					break;
				case 1: // SETTINGS
					SettingsUi.previousState = SystemController.GameState.PAUSED;
					SystemController.setCurrentState(SystemController.GameState.SETTINGS);
					break;
				case 2: // EXIT
					if (saveFile != null) {
						SaveManager.saveToFile(saveFile);
						System.out.println("Saved game before exiting to Main Menu.");
					}
					SystemController.setCurrentState(SystemController.GameState.MAIN_MENU);
					break;
			}
		}

		if (hoveredCheatIndex != -1) {
			animations.playSound("Click");
			switch (hoveredCheatIndex) {
				case 0: // BOSS ARENA TELEPORT
					if (knight != null && knight.getRoom().nameOfLevel.equalsIgnoreCase("Crystal Peak")) {

						knight.getPosition().x = 2475;
						knight.getPosition().y = 500;



						System.out.println("Teleported to Crystal Peak Boss Arena.");
					}
					break;
				case 1: // NO CLIP
					GeneralSave.noClip = !GeneralSave.noClip;
					break;
				case 2: // EMERGENCY HEAL
					GeneralSave.emergencyHeal = !GeneralSave.emergencyHeal;
					break;
				case 3: // REFILL SOUL VESSEL
					if (knight != null) {
						try {
							knight.setSoul();
						} catch (Exception e) {
							System.out.println("Map soul refill to your specific Knight layout.");
						}
					}
					break;
				case 4: // GOD MODE
					GeneralSave.godMode = !GeneralSave.godMode;
					break;
				case 5: // INFINITE CHARMS
					GeneralSave.infiniteCharms = !GeneralSave.infiniteCharms;
					if (saveFile != null && saveFile.currentCharms != null) {
						saveFile.currentCharms.clear();
					}
					break;
				case 6: // SHOW HITBOX
					GeneralSave.showHitbox = !GeneralSave.showHitbox;
					break;
			}
		}
	}
}