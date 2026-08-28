package View.Uis;

import Controller.GeneralSave;
import Controller.SystemController;
import View.Animations;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class RemappingUi {
	private int selectedIndex = 0;
	private boolean isWaitingForKey = false; // Tracks if we are actively mapping a key
	private int resetTimer = 0; // Tracks the 180 tick (3-second) confirmation timer

	// Added "RESET" and adjusted to a 13-item array
	private final String[] menuOptions = {
			"Up", "Down",
			"Left", "Right",
			"Dash", "Attack",
			"QuickCast", "Focus",
			"Jump", "Inventory",
			"Pause", // Index 10
			"RESET", // Index 11
			"BACK"   // Index 12
	};

	private int screenWidth = 1920;
	private int screenHeight = 1080;

	private static final Animations menuAnimations = new Animations();
	private static Font trajan;

	static {
		// 1. Load Font Safely
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

		// 2. Pre-load local assets and sounds
		try {
			menuAnimations.addAnimation("Idle2", "src/Model/Game/DialogueBoxAnimations/");
			menuAnimations.addAnimation("EDP", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Crystal Peak", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Green Path", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Is This A Hazbin Hotel", "src/View/BackGrounds/");
			menuAnimations.addAnimation("S!LK S0NG", "src/View/BackGrounds/");

			// NEW: Added Menu Sounds
			menuAnimations.addSound("Hover","src/View/MenuSounds/");
			menuAnimations.addSound("Click","src/View/MenuSounds/");
		} catch (Exception e) {
			System.out.println("Failed to load animations in RemappingUi: " + e.getMessage());
		}
	}

	public void paint(Graphics g, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		// Decrement reset timer every tick (assuming 1 tick = 16ms, 180 ticks = ~3 seconds)
		if (resetTimer > 0) {
			resetTimer--;
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// --- 1. DYNAMIC BACKGROUND ---
		if (SettingsUi.previousState == SystemController.GameState.MAIN_MENU) {
			String bgAnimationName = GeneralSave.currentTheme;
			if (bgAnimationName != null && !bgAnimationName.isEmpty() &&
					menuAnimations.animations != null && menuAnimations.animations.containsKey(bgAnimationName)) {
				menuAnimations.paint(g2d, 0, 0, screenWidth, screenHeight, bgAnimationName, 0);
			} else {
				GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 25), 0, screenHeight, new Color(5, 5, 8));
				g2d.setPaint(gp);
				g2d.fillRect(0, 0, screenWidth, screenHeight);
			}
		} else {
			g2d.setColor(new Color(0, 0, 0, 220));
			g2d.fillRect(0, 0, screenWidth, screenHeight);
		}

		// --- 2. TITLE ---
		String title = "CONTROLS";
		g2d.setFont(trajan.deriveFont(Font.BOLD, 54f));
		FontMetrics titleMetrics = g2d.getFontMetrics();
		int titleWidth = titleMetrics.stringWidth(title);
		int titleBoxWidth = titleWidth + 80;
		int titleBoxHeight = 145;
		int titleBoxX = (screenWidth - titleBoxWidth) / 2;
		int titleBoxY = 40;

		menuAnimations.paint(g2d, titleBoxX, titleBoxY, titleBoxWidth, titleBoxHeight, "Idle2", 0);
		g2d.setColor(new Color(235, 235, 240));
		g2d.drawString(title, (screenWidth - titleWidth) / 2, 125);

		// --- 3. TWO-COLUMN BUTTON GRID & CENTERED SELECTIONS ---
		int buttonWidth = 380;
		int buttonHeight = 55;
		int startY = 220;
		int rowSpacing = 85;
		int colSpacing = 420;

		int startX = (screenWidth / 2) - (colSpacing / 2) - (buttonWidth / 2);

		for (int i = 0; i < menuOptions.length; i++) {
			int btnX, btnY;

			if (i < 10) {
				// 0-9: Standard Grid
				int row = i / 2;
				int col = i % 2;
				btnX = startX + (col * colSpacing);
				btnY = startY + (row * rowSpacing);
			} else if (i == 10) {
				// 10: Center Pause button (Row 5)
				btnX = (screenWidth - buttonWidth) / 2;
				btnY = startY + (5 * rowSpacing);
			} else if (i == 11) {
				// 11: RESET (Aligned to Left Column)
				btnX = startX;
				btnY = startY + (5 * rowSpacing) + 130;
			} else {
				// 12: BACK (Aligned to Right Column)
				btnX = startX + colSpacing;
				btnY = startY + (5 * rowSpacing) + 130;
			}

			boolean isSelected = (i == selectedIndex);

			if (isSelected) {
				menuAnimations.paint(g2d, btnX, btnY, buttonWidth, buttonHeight, "Idle2", 0);
			}

			g2d.setFont(trajan.deriveFont(Font.PLAIN, 22f));

			String displayText;
			if (i == 11) {
				displayText = (resetTimer > 0) ? "REALLY??" : "RESET";
			} else if (i == 12) {
				displayText = "BACK";
			} else {
				String actionName = menuOptions[i];
				if (isSelected && isWaitingForKey) {
					displayText = actionName.toUpperCase() + ": [ PRESS ANY KEY ]";
				} else {
					int currentKey = GeneralSave.keybinds.getOrDefault(actionName, 0);
					String keyName = KeyEvent.getKeyText(currentKey).toUpperCase();
					displayText = actionName.toUpperCase() + ": " + keyName;
				}
			}

			FontMetrics fm = g2d.getFontMetrics();
			int textX = btnX + (buttonWidth - fm.stringWidth(displayText)) / 2;
			int textY = btnY + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent();

			// Color Coding for Reset functionality and standard behavior
			if (i == 11 && resetTimer > 0) {
				g2d.setColor(new Color(255, 80, 80)); // Red tint to signify destructive action
			} else if (isSelected && isWaitingForKey) {
				g2d.setColor(new Color(255, 215, 0));
			} else if (isSelected) {
				g2d.setColor(new Color(255, 255, 255));
			} else {
				g2d.setColor(new Color(160, 160, 170));
			}

			g2d.drawString(displayText, textX, textY);
		}
	}

	// --- MOUSE LISTENERS ---
	public void handleMouseMove(int mouseX, int mouseY) {
		if (isWaitingForKey) return;

		int buttonWidth = 380, buttonHeight = 55, startY = 220, rowSpacing = 85, colSpacing = 420;
		int startX = (screenWidth / 2) - (colSpacing / 2) - (buttonWidth / 2);
		boolean foundHover = false;

		int previousIndex = selectedIndex; // Track index before changes

		for (int i = 0; i < menuOptions.length; i++) {
			int btnX, btnY;

			if (i < 10) {
				int row = i / 2;
				int col = i % 2;
				btnX = startX + (col * colSpacing);
				btnY = startY + (row * rowSpacing);
			} else if (i == 10) {
				btnX = (screenWidth - buttonWidth) / 2;
				btnY = startY + (5 * rowSpacing);
			} else if (i == 11) {
				btnX = startX;
				btnY = startY + (5 * rowSpacing) + 130;
			} else {
				btnX = startX + colSpacing;
				btnY = startY + (5 * rowSpacing) + 130;
			}

			if (mouseX >= btnX && mouseX <= btnX + buttonWidth && mouseY >= btnY && mouseY <= btnY + buttonHeight) {
				selectedIndex = i;
				foundHover = true;
				break;
			}
		}
		if (!foundHover) selectedIndex = -1;

		// Play sound ONLY if we hovered onto a new button
		if (selectedIndex != previousIndex && selectedIndex != -1) {
			menuAnimations.playSound("Hover");
		}
	}

	public void handleMouseClick(int mouseX, int mouseY) {
		if (isWaitingForKey) return;

		if (selectedIndex >= 0 && selectedIndex < menuOptions.length) {
			menuAnimations.playSound("Click"); // Play click sound
			selectOption();
		}
	}

	// --- KEYBOARD LISTENERS ---
	public void handleKeyPressed(KeyEvent e) {
		// 1. Swap/Remap handling logic
		if (isWaitingForKey) {
			String actionToRemap = menuOptions[selectedIndex];
			int newKeyCode = e.getKeyCode();
			int oldKeyCode = GeneralSave.keybinds.getOrDefault(actionToRemap, 0);

			// Find if another keybind uses this incoming key code
			String conflictingAction = null;
			for (String key : GeneralSave.keybinds.keySet()) {
				if (!key.equals(actionToRemap) && GeneralSave.keybinds.get(key) == newKeyCode) {
					conflictingAction = key;
					break;
				}
			}

			// If a duplicate action key code exists, swap them out
			if (conflictingAction != null) {
				GeneralSave.keybinds.put(conflictingAction, oldKeyCode);
			}

			GeneralSave.keybinds.put(actionToRemap, newKeyCode);
			isWaitingForKey = false;
			return;
		}

		// 2. Normal Grid Navigation
		int keyCode = e.getKeyCode();
		int previousIndex = selectedIndex; // Track index before changes

		if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
			if (selectedIndex == 11 || selectedIndex == 12) {
				selectedIndex = 10; // Jump up to Pause
			} else if (selectedIndex == 10) {
				selectedIndex = 8;  // Jump up to Jump
			} else if (selectedIndex >= 2) {
				selectedIndex -= 2; // Normal upward movement
			}
		}
		if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
			if (selectedIndex < 8) {
				selectedIndex += 2; // Normal downward movement
			} else if (selectedIndex == 8 || selectedIndex == 9) {
				selectedIndex = 10; // Funnel into Pause
			} else if (selectedIndex == 10) {
				selectedIndex = 11; // Drop into Reset (Left side default)
			}
		}
		if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
			if (selectedIndex == 12) {
				selectedIndex = 11; // Move from BACK to RESET
			} else if (selectedIndex % 2 != 0 && selectedIndex < 10) {
				selectedIndex--; // Move left within standard grid
			}
		}
		if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
			if (selectedIndex == 11) {
				selectedIndex = 12; // Move from RESET to BACK
			} else if (selectedIndex % 2 == 0 && selectedIndex < 10) {
				selectedIndex++; // Move right within standard grid
			}
		}

		// Play sound if index was changed via keyboard
		if (selectedIndex != previousIndex && selectedIndex != -1) {
			menuAnimations.playSound("Hover");
		}

		if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
			if (selectedIndex >= 0) {
				menuAnimations.playSound("Click"); // Play click sound
				selectOption();
			}
		}
	}

	private void selectOption() {
		if (selectedIndex == 12) { // BACK
			resetTimer = 0; // Ensure timer is reset on exit
			SystemController.setCurrentState(SystemController.GameState.SETTINGS);
		} else if (selectedIndex == 11) { // RESET
			if (resetTimer > 0) {
				GeneralSave.resetKeybinds();
				resetTimer = 0; // Turn off prompt immediately after resetting
			} else {
				resetTimer = 180; // Trigger "REALLY??" for 3 seconds
			}
		} else {
			isWaitingForKey = true;
		}
	}
}