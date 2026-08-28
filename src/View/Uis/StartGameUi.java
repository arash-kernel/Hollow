package View.Uis;

import Controller.GeneralSave;
import Controller.SaveFile;
import Controller.SaveManager;
import Controller.SystemController;
import Model.Game.Camera;
import Model.Game.CameraBoundingBox;
import Model.Game.Knight.Knight;
import Model.Game.Room;
import Model.Game.ObjectSpawner; // Make sure to import ObjectSpawner
import View.Animations;
import View.MyFrame;
import View.MyPanel;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

public class StartGameUi {
	// 0-3 for Saves, 4-7 for Delete Buttons, 8 for BACK
	private int selectedIndex = 0;
	private SaveFile[] saveFiles = new SaveFile[4]; // Array to hold the 4 save slots

	// Tracks the 180 tick (3-second) confirmation timer for each delete button
	private int[] resetTimers = new int[4];

	private int screenWidth = 1920;
	private int screenHeight = 1080;

	private static final Animations menuAnimations = new Animations();
	private static Font trajan;

	private Room currentRoom;
	private MyPanel panel;

	static {
		// 1. Load the Trajan Font File dynamically & safely
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
			System.out.println("Failed to load Trajan font in StartGameUi: " + e.getMessage());
		}

		// 2. Pre-load assets safely (so a wrong path doesn't crash the whole screen)
		try {
			menuAnimations.addAnimation("Idle2", "src/Model/Game/DialogueBoxAnimations/");
			menuAnimations.addAnimation("EDP", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Crystal Peak", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Green Path", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Is This A Hazbin Hotel", "src/View/BackGrounds/");
			menuAnimations.addAnimation("S!LK S0NG", "src/View/BackGrounds/");
		} catch (Exception e) {
			System.out.println("Failed to load animations in StartGameUi: " + e.getMessage());
		}
	}

	public StartGameUi(Animations animations, MyPanel panel) {
		this.panel = panel;
	}
	public StartGameUi(MyPanel panel) {
		this.panel = panel;
	}
	public StartGameUi() {}
	public void setPanel(MyPanel panel) {
		this.panel = panel;
		loadAllSaves();
	}

	public void paint(Graphics g, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		// Decrement reset timers every tick
		for (int i = 0; i < 4; i++) {
			if (resetTimers[i] > 0) resetTimers[i]--;
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 1. Draw Theme-Based Background
		String bgAnimationName = GeneralSave.currentTheme;
		if (bgAnimationName != null && !bgAnimationName.isEmpty() &&
				menuAnimations.animations != null && menuAnimations.animations.containsKey(bgAnimationName)) {
			menuAnimations.paint(g2d, 0, 0, screenWidth, screenHeight, bgAnimationName, 0);
		} else {
			GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 25), 0, screenHeight, new Color(5, 5, 8));
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, screenWidth, screenHeight);
		}

		// 2. Draw Title
		String title = "SELECT SAVE FILE";
		g2d.setFont(trajan.deriveFont(Font.BOLD, 48f));
		FontMetrics titleMetrics = g2d.getFontMetrics();
		int titleWidth = titleMetrics.stringWidth(title);
		g2d.setColor(new Color(235, 235, 240));
		g2d.drawString(title, (screenWidth - titleWidth) / 2, 150);

		// 3. Draw Save Slots & Delete Buttons
		int slotWidth = 400;
		int slotHeight = 100;
		int startY = 250;
		int spacing = 30;

		for (int i = 0; i < 4; i++) {
			int btnX = (screenWidth - slotWidth) / 2;
			int btnY = startY + i * (slotHeight + spacing);
			boolean isSelected = (i == selectedIndex);

			// Draw the border for save slots
			menuAnimations.paint(g2d, btnX, btnY, slotWidth, slotHeight, "Idle2", 0);

			// Setup Font & Color
			g2d.setFont(trajan.deriveFont(Font.PLAIN, 24f));
			if (isSelected) {
				g2d.setColor(new Color(255, 255, 255));
			} else {
				g2d.setColor(new Color(160, 160, 170));
			}

			// Draw Save Data or "Empty"
			if (saveFiles[i] == null) {
				String text = "Empty Save " + (i + 1);
				int textX = btnX + (slotWidth - g2d.getFontMetrics().stringWidth(text)) / 2;
				int textY = btnY + ((slotHeight - g2d.getFontMetrics().getHeight()) / 2) + g2d.getFontMetrics().getAscent();
				g2d.drawString(text, textX, textY);
			} else {
				// Formatting for filled saves
				String text = "Save " + (i + 1);
				String subText = "Deaths: " + saveFiles[i].deathCount + " | Kills: " + saveFiles[i].totalEnemyKilled;

				int textX = btnX + (slotWidth - g2d.getFontMetrics().stringWidth(text)) / 2;
				g2d.drawString(text, textX, btnY + 45);

				g2d.setFont(trajan.deriveFont(Font.ITALIC, 16f));
				int subTextX = btnX + (slotWidth - g2d.getFontMetrics().stringWidth(subText)) / 2;
				g2d.drawString(subText, subTextX, btnY + 75);

				// --- DRAW DELETE BUTTON TO THE RIGHT ---
				int rmBtnWidth = 140;
				int rmBtnHeight = 55;
				int rmBtnX = btnX + slotWidth + 20;
				int rmBtnY = btnY + (slotHeight - rmBtnHeight) / 2;
				boolean rmSelected = (selectedIndex == (i + 4));

				if (rmSelected) {
					menuAnimations.paint(g2d, rmBtnX, rmBtnY, rmBtnWidth, rmBtnHeight, "Idle2", 0);
				}

				String rmText = (resetTimers[i] > 0) ? "REALLY??" : "DELETE";
				g2d.setFont(trajan.deriveFont(Font.PLAIN, 20f));

				// Apply Color Coding (Red for confirmation)
				if (resetTimers[i] > 0) {
					g2d.setColor(new Color(255, 80, 80));
				} else if (rmSelected) {
					g2d.setColor(new Color(255, 255, 255));
				} else {
					g2d.setColor(new Color(160, 160, 170));
				}

				int rmTextX = rmBtnX + (rmBtnWidth - g2d.getFontMetrics().stringWidth(rmText)) / 2;
				int rmTextY = rmBtnY + ((rmBtnHeight - g2d.getFontMetrics().getHeight()) / 2) + g2d.getFontMetrics().getAscent();
				g2d.drawString(rmText, rmTextX, rmTextY);
			}
		}

		// 4. Draw Back Button
		int backBtnWidth = 320;
		int backBtnHeight = 55;
		int backBtnY = startY + 4 * (slotHeight + spacing);
		int backBtnX = (screenWidth - backBtnWidth) / 2;

		boolean backSelected = (selectedIndex == 8);

		if (backSelected) {
			menuAnimations.paint(g2d, backBtnX, backBtnY, backBtnWidth, backBtnHeight, "Idle2", 0);
			g2d.setColor(new Color(255, 255, 255));
		} else {
			g2d.setColor(new Color(160, 160, 170));
		}

		g2d.setFont(trajan.deriveFont(Font.PLAIN, 22f));
		String backText = "BACK";
		int backTextX = backBtnX + (backBtnWidth - g2d.getFontMetrics().stringWidth(backText)) / 2;
		int backTextY = backBtnY + ((backBtnHeight - g2d.getFontMetrics().getHeight()) / 2) + g2d.getFontMetrics().getAscent();
		g2d.drawString(backText, backTextX, backTextY);
	}

	// --- MOUSE LISTENERS ---
	public void handleMouseMove(int mouseX, int mouseY) {
		int slotWidth = 400;
		int slotHeight = 100;
		int startY = 250;
		int spacing = 30;
		boolean foundHover = false;

		// Check Save Slots (0-3) and Delete Buttons (4-7)
		for (int i = 0; i < 4; i++) {
			int btnX = (this.screenWidth - slotWidth) / 2;
			int btnY = startY + i * (slotHeight + spacing);

			if (mouseX >= btnX && mouseX <= btnX + slotWidth && mouseY >= btnY && mouseY <= btnY + slotHeight) {
				selectedIndex = i;
				foundHover = true;
				break;
			}

			// Delete hitboxes only exist if the slot is populated
			if (saveFiles[i] != null) {
				int rmBtnWidth = 140;
				int rmBtnHeight = 55;
				int rmBtnX = btnX + slotWidth + 20;
				int rmBtnY = btnY + (slotHeight - rmBtnHeight) / 2;

				if (mouseX >= rmBtnX && mouseX <= rmBtnX + rmBtnWidth && mouseY >= rmBtnY && mouseY <= rmBtnY + rmBtnHeight) {
					selectedIndex = i + 4;
					foundHover = true;
					break;
				}
			}
		}

		// Check Back Button (8)
		int backBtnWidth = 320;
		int backBtnHeight = 55;
		int backBtnY = startY + 4 * (slotHeight + spacing);
		int backBtnX = (this.screenWidth - backBtnWidth) / 2;

		if (!foundHover && mouseX >= backBtnX && mouseX <= backBtnX + backBtnWidth && mouseY >= backBtnY && mouseY <= backBtnY + backBtnHeight) {
			selectedIndex = 8;
			foundHover = true;
		}

		if (!foundHover) {
			selectedIndex = -1;
		}
	}

	public void handleMouseClick(int mouseX, int mouseY) {
		int slotWidth = 400;
		int slotHeight = 100;
		int startY = 250;
		int spacing = 30;

		for (int i = 0; i < 4; i++) {
			int btnX = (this.screenWidth - slotWidth) / 2;
			int btnY = startY + i * (slotHeight + spacing);

			// Slots
			if (mouseX >= btnX && mouseX <= btnX + slotWidth && mouseY >= btnY && mouseY <= btnY + slotHeight) {
				selectedIndex = i;
				selectOption();
				return;
			}

			// Deletes
			if (saveFiles[i] != null) {
				int rmBtnWidth = 140;
				int rmBtnHeight = 55;
				int rmBtnX = btnX + slotWidth + 20;
				int rmBtnY = btnY + (slotHeight - rmBtnHeight) / 2;

				if (mouseX >= rmBtnX && mouseX <= rmBtnX + rmBtnWidth && mouseY >= rmBtnY && mouseY <= rmBtnY + rmBtnHeight) {
					selectedIndex = i + 4;
					selectOption();
					return;
				}
			}
		}

		// Back
		int backBtnWidth = 320;
		int backBtnHeight = 55;
		int backBtnY = startY + 4 * (slotHeight + spacing);
		int backBtnX = (this.screenWidth - backBtnWidth) / 2;

		if (mouseX >= backBtnX && mouseX <= backBtnX + backBtnWidth && mouseY >= backBtnY && mouseY <= backBtnY + backBtnHeight) {
			selectedIndex = 8;
			selectOption();
		}
	}

	// --- KEYBOARD LISTENERS ---
	public void handleKeyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
			if (selectedIndex == 8) {
				selectedIndex = 3; // From Back jump to Slot 4
			} else if (selectedIndex >= 0 && selectedIndex <= 3) {
				selectedIndex = (selectedIndex == 0) ? 8 : selectedIndex - 1; // Move up slots
			} else if (selectedIndex >= 4 && selectedIndex <= 7) {
				// Move up delete buttons
				int next = selectedIndex - 1;
				while(next >= 4 && saveFiles[next - 4] == null) {
					next--;
				}
				selectedIndex = (next < 4) ? 8 : next;
			}
		}
		if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
			if (selectedIndex == 8) {
				selectedIndex = 0; // From Back jump to Slot 1
			} else if (selectedIndex >= 0 && selectedIndex <= 3) {
				selectedIndex = (selectedIndex == 3) ? 8 : selectedIndex + 1; // Move down slots
			} else if (selectedIndex >= 4 && selectedIndex <= 7) {
				// Move down delete buttons
				int next = selectedIndex + 1;
				while(next <= 7 && saveFiles[next - 4] == null) {
					next++;
				}
				selectedIndex = (next > 7) ? 8 : next;
			}
		}
		if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
			if (selectedIndex >= 4 && selectedIndex <= 7) {
				selectedIndex -= 4; // Shift left from Delete to Slot
			}
		}
		if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
			if (selectedIndex >= 0 && selectedIndex <= 3) {
				if (saveFiles[selectedIndex] != null) {
					selectedIndex += 4; // Shift right from Slot to Delete
				}
			}
		}

		if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
			if (selectedIndex >= 0 && selectedIndex <= 8) {
				selectOption();
			}
		}
		if (keyCode == KeyEvent.VK_ESCAPE) {
			for(int i = 0; i < 4; i++) resetTimers[i] = 0;
			SystemController.setCurrentState(SystemController.GameState.MAIN_MENU);
		}
	}

	// --- OPTION EXECUTION ---
	private void selectOption() {
		if (selectedIndex >= 0 && selectedIndex < 4) {
			SaveFile activeSave;
			String mapFilePath;

			// EMPTY SLOT - CREATE NEW GAME
			if (saveFiles[selectedIndex] == null) {
				System.out.println("Starting a new game in slot " + (selectedIndex + 1));

				activeSave = new SaveFile(selectedIndex + 1);
				activeSave.nameOfLevel = "Green Path";

				SaveManager.saveToFile(activeSave);
				saveFiles[selectedIndex] = activeSave;
				mapFilePath = "src/Model/Maps/" + activeSave.nameOfLevel + ".txt";

				// EXISTING SLOT - LOAD GAME
			} else {
				System.out.println("Loading existing save in slot " + (selectedIndex + 1));

				activeSave = saveFiles[selectedIndex];
				mapFilePath = "src/Model/Maps/" + activeSave.nameOfLevel + ".txt";
			}

			// 1. Initialize Room.
			Knight knight = new Knight(500, 300);
			Room newRoom = new Room(knight, new ArrayList<>(), activeSave);
			knight.setRoom(newRoom);
			newRoom.panel = this.panel;
			Camera camera = new Camera(0, 0, 1520, 880);
			camera.setTarget(knight);
			CameraBoundingBox roomBounds = new CameraBoundingBox(0, 0, 4000, 880);
			camera.addBound(roomBounds);
			MyFrame.onlyPanel.camera = camera;
			if (activeSave.nameOfLevel != null && activeSave.nameOfLevel.equals("Crystal Peak")) {
				newRoom.nameOfLevel = "Crystal Peak";
				newRoom.song = "Crystal Peak.wav";
				knight.getPosition().x=150;
				knight.getPosition().y=150;
			}

			// 2. Read the Map file and spawn objects into the new Room
			ObjectSpawner.spawnFromFile(mapFilePath, newRoom);

			// 3. Pass the completely configured Room to the Panel
			if (this.panel != null) {
				this.panel.changeRoom(newRoom);
			} else {
				System.out.println("Warning: MyPanel reference is missing in StartGameUi.");
			}

			// 4. Change game state to start gameplay
			SystemController.setCurrentState(SystemController.GameState.PLAYING);

		} else if (selectedIndex >= 4 && selectedIndex < 8) {
			// DELETE BUTTON (Double Confirmation Mechanic)
			int slotIndex = selectedIndex - 4;

			if (resetTimers[slotIndex] > 0) {
				// Execute permanent delete[cite: 1]
				File file = new File("src/SaveFiles/file" + (slotIndex + 1) + ".txt");
				if (file.exists()) {
					file.delete();
				}
				// Wipe the local cache reference and UI focus
				saveFiles[slotIndex] = null;
				resetTimers[slotIndex] = 0;
				selectedIndex = slotIndex;
				System.out.println("Deleted Save in Slot " + (slotIndex + 1));
			} else {
				// Trigger the timer ("REALLY??")
				resetTimers[slotIndex] = 180;
			}

		} else if (selectedIndex == 8) {
			// BACK BUTTON pressed
			for(int i = 0; i < 4; i++) resetTimers[i] = 0; // Clear timers on backout
			SystemController.setCurrentState(SystemController.GameState.MAIN_MENU);
		}
	}

	public void loadAllSaves() {
		for (int i = 0; i < 4; i++) {
			saveFiles[i] = SaveManager.loadFromFile(i + 1);
		}
	}
}