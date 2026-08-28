package View.Uis;

import Controller.GeneralSave;
import Controller.SaveManager;
import Controller.SystemController;
import View.Animations;
import View.Ui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class MainMenuUi {
	private int selectedIndex = 0;
	private final String[] menuOptions = {"PLAY", "SETTINGS", "GUIDE", "ACHIEVEMENTS", "QUIT"};

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
			System.out.println("Failed to load Trajan font in MainMenuUi: " + e.getMessage());
		}

		// 2. Pre-load assets safely
		try {
			menuAnimations.addAnimation("Idle2", "src/Model/Game/DialogueBoxAnimations/");
			menuAnimations.addAnimation("EDP", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Crystal Peak", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Green Path", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Is This A Hazbin Hotel", "src/View/BackGrounds/");
			menuAnimations.addAnimation("S!LK S0NG", "src/View/BackGrounds/");
			menuAnimations.addSound("Hover","src/View/MenuSounds/");
			menuAnimations.addSound("Click","src/View/MenuSounds/");
		} catch (Exception e) {
			System.out.println("Failed to load animations in MainMenuUi: " + e.getMessage());
		}

	}

	public MainMenuUi(Animations animations) {}
	public MainMenuUi() {} // Fallback constructor

	public void paint(Graphics g, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 1. Draw Theme-Based Background (Safely)
		String bgAnimationName = GeneralSave.currentTheme;
		if (bgAnimationName != null && !bgAnimationName.isEmpty() &&
				menuAnimations.animations != null && menuAnimations.animations.containsKey(bgAnimationName)) {
			menuAnimations.paint(g2d, 0, 0, screenWidth, screenHeight, bgAnimationName, 0);
		} else {
			GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 25), 0, screenHeight, new Color(5, 5, 8));
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, screenWidth, screenHeight);
		}

		// 2. Title Setup
		String title = "MY SOUL BECAME HOLLOW";
		g2d.setFont(trajan.deriveFont(Font.BOLD, 54f));
		FontMetrics titleMetrics = g2d.getFontMetrics();
		int titleWidth = titleMetrics.stringWidth(title);

		int titleBoxWidth = titleWidth + 80;
		int titleBoxHeight = 145;
		int titleBoxX = (screenWidth - titleBoxWidth) / 2;
		int titleBoxY = 110;

		menuAnimations.paint(g2d, titleBoxX, titleBoxY, titleBoxWidth, titleBoxHeight, "Idle2", 0);

		g2d.setColor(new Color(235, 235, 240));
		int titleX = (screenWidth - titleWidth) / 2;
		g2d.drawString(title, titleX, 195);

		g2d.setColor(new Color(140, 140, 150));
		g2d.setFont(trajan.deriveFont(Font.ITALIC, 20f));
		String subtitle = "while making this game";
		int subX = (screenWidth - g2d.getFontMetrics().stringWidth(subtitle)) / 2;
		g2d.drawString(subtitle, subX, 235);

		// 3. Draw Menu Buttons
		int buttonWidth = 320;
		int buttonHeight = 55;
		int startY = 360;
		int spacing = 25;

		for (int i = 0; i < menuOptions.length; i++) {
			int btnX = (screenWidth - buttonWidth) / 2;
			int btnY = startY + i * (buttonHeight + spacing);

			boolean isSelected = (i == selectedIndex);

			if (isSelected) {
				menuAnimations.paint(g2d, btnX, btnY, buttonWidth, buttonHeight, "Idle2", 0);
			}

			g2d.setFont(trajan.deriveFont(Font.PLAIN, 22f));
			int textX = btnX + (buttonWidth - g2d.getFontMetrics().stringWidth(menuOptions[i])) / 2;
			int textY = btnY + ((buttonHeight - g2d.getFontMetrics().getHeight()) / 2) + g2d.getFontMetrics().getAscent();

			if (isSelected) {
				g2d.setColor(new Color(255, 255, 255));
			} else {
				g2d.setColor(new Color(160, 160, 170));
			}
			g2d.drawString(menuOptions[i], textX, textY);
		}
	}

	// --- MOUSE LISTENERS ---
	public void handleMouseMove(int mouseX, int mouseY) {
		int buttonWidth = 320;
		int buttonHeight = 55;
		int startY = 360;
		int spacing = 25;
		boolean foundHover = false;
		int previousIndex = selectedIndex; // Track index before changes

		for (int i = 0; i < menuOptions.length; i++) {
			int btnX = (this.screenWidth - buttonWidth) / 2;
			int btnY = startY + i * (buttonHeight + spacing);

			if (mouseX >= btnX && mouseX <= btnX + buttonWidth &&
					mouseY >= btnY && mouseY <= btnY + buttonHeight) {
				selectedIndex = i;
				foundHover = true;
				break;
			}
		}

		if (!foundHover) {
			selectedIndex = -1;
		}

		// Play sound ONLY if we hovered onto a new button (not if we went into empty space)
		if (selectedIndex != previousIndex && selectedIndex != -1) {
			menuAnimations.playSound("Hover");
		}
	}

	public void handleMouseClick(int mouseX, int mouseY) {
		int buttonWidth = 320;
		int buttonHeight = 55;
		int startY = 360;
		int spacing = 25;

		for (int i = 0; i < menuOptions.length; i++) {
			int btnX = (this.screenWidth - buttonWidth) / 2;
			int btnY = startY + i * (buttonHeight + spacing);

			if (mouseX >= btnX && mouseX <= btnX + buttonWidth &&
					mouseY >= btnY && mouseY <= btnY + buttonHeight) {
				selectedIndex = i;
				menuAnimations.playSound("Click"); // Play click sound
				selectOption();
				break;
			}
		}
	}

	// --- KEYBOARD LISTENERS ---
	public void handleKeyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int previousIndex = selectedIndex; // Track index before changes

		if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
			selectedIndex--;
			if (selectedIndex < 0) selectedIndex = menuOptions.length - 1;
		}
		if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
			selectedIndex++;
			if (selectedIndex >= menuOptions.length || selectedIndex < 0) selectedIndex = 0;
		}

		// Play sound if index was changed via keyboard
		if (selectedIndex != previousIndex && selectedIndex != -1) {
			menuAnimations.playSound("Hover");
		}

		if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
			if (selectedIndex >= 0 && selectedIndex < menuOptions.length) {
				menuAnimations.playSound("Click"); // Play click sound
				selectOption();
			}
		}
	}

	private void selectOption() {
		switch (selectedIndex) {
			case 0: // PLAY
				Ui.getStartGameUi().loadAllSaves();
				SystemController.setCurrentState(SystemController.GameState.START_GAME);
				break;
			case 1: // SETTINGS
				SettingsUi.previousState = SystemController.GameState.MAIN_MENU;
				SystemController.setCurrentState(SystemController.GameState.SETTINGS);
				break;
			case 2: // GUIDE
				SystemController.setCurrentState(SystemController.GameState.GUIDE);
				break;
			case 3: // ACHIEVEMENTS
				SystemController.setCurrentState(SystemController.GameState.ACHIEVEMENTS);
				break;
			case 4: // QUIT
				// Optional: If the game quits too fast to hear the sound, you can add a short delay here.
				// try { Thread.sleep(150); } catch (InterruptedException e) {}
				SaveManager.saveGlobalSettings();
				System.exit(0);
				break;
		}
	}
}