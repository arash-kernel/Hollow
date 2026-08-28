package View.Uis;

import Controller.GeneralSave;
import Controller.SystemController;
import View.Animations;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class SettingsUi {
	// Tracks where we came from so we can render the correct background and return properly
	public static SystemController.GameState previousState = SystemController.GameState.MAIN_MENU;

	private int selectedIndex = 0;

	// UPDATED: Added "THEME" right before "BACK"
	private final String[] menuOptions = {"MUSIC", "SOUND", "BRIGHTNESS", "REMAPPING", "LANGUAGE", "THEME", "BACK"};

	// NEW: Available themes pool matching your loaded animations
	private static final String[] THEMES = {"S!LK S0NG","Crystal Peak", "Green Path", "Is This A Hazbin Hotel"};

	private int screenWidth = 1920;
	private int screenHeight = 1080;

	private static final Animations menuAnimations = new Animations();
	private static Font trajan;

	// Setting Variables
	public static boolean musicMuted = false;
	public static boolean soundMuted = false;
	public static float musicVolume = 1.0f;
	public static float soundVolume = 1.0f;
	public static float savedMusicVolume = 1.0f;
	public static float savedSoundVolume = 1.0f;
	public static float brightness = 1.0f;
	public static boolean fancyLanguage = false;

	// Mouse Tracking
	private boolean draggingMusic = false;
	private boolean draggingSound = false;
	private boolean draggingBrightness = false;

	// Easter Egg Variables
	private int showEasterEgg = 0;


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

		try {
			menuAnimations.addAnimation("Idle2", "src/Model/Game/DialogueBoxAnimations/");
			menuAnimations.addAnimation("EDP", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Crystal Peak", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Green Path", "src/View/BackGrounds/");
			menuAnimations.addAnimation("Is This A Hazbin Hotel", "src/View/BackGrounds/");
			menuAnimations.addAnimation("S!LK S0NG", "src/View/BackGrounds/");
			menuAnimations.addAnimation("SliderHandle", "src/View/UiFrames/");

			// Pre-load audio assets to match MainMenuUi
			menuAnimations.addSound("Hover", "src/View/MenuSounds/");
			menuAnimations.addSound("Click", "src/View/MenuSounds/");
		} catch (Exception e) {
			System.out.println("Failed to load animations/sounds in SettingsUi: " + e.getMessage());
		}
	}

	public SettingsUi(){
		musicVolume=GeneralSave.musicVolume;
		soundVolume=GeneralSave.sfxVolume;
		brightness=GeneralSave.brightness;
	}
	public void paint(Graphics g, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// --- 1. DYNAMIC BACKGROUND ---
		if (previousState == SystemController.GameState.MAIN_MENU) {
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
			g2d.setColor(new Color(0, 0, 0, 200));
			g2d.fillRect(0, 0, screenWidth, screenHeight);
		}

		// --- 2. TITLE ---
		String title = "SETTINGS";
		g2d.setFont(trajan.deriveFont(Font.BOLD, 54f));
		FontMetrics titleMetrics = g2d.getFontMetrics();
		int titleWidth = titleMetrics.stringWidth(title);
		int titleBoxWidth = titleWidth + 80;
		int titleBoxHeight = 145;
		int titleBoxX = (screenWidth - titleBoxWidth) / 2;
		int titleBoxY = 60;

		menuAnimations.paint(g2d, titleBoxX, titleBoxY, titleBoxWidth, titleBoxHeight, "Idle2", 0);
		g2d.setColor(new Color(235, 235, 240));
		g2d.drawString(title, (screenWidth - titleWidth) / 2, 145);

		// --- 3. MENU BUTTONS & SLIDERS ---
		int buttonWidth = 320;
		int buttonHeight = 55;
		int startY = 250;
		int spacing = 80;

		int sliderX = (screenWidth / 2) + 50;
		int sliderWidth = 250;

		Stroke defaultStroke = g2d.getStroke();

		for (int i = 0; i < menuOptions.length; i++) {
			int btnY = startY + i * spacing;

			int btnX = (i <= 2) ? (screenWidth / 2) - 300 : (screenWidth - buttonWidth) / 2;

			boolean isSelected = (i == selectedIndex);

			if (isSelected) {
				menuAnimations.paint(g2d, btnX, btnY, buttonWidth, buttonHeight, "Idle2", 0);
			}

			g2d.setFont(trajan.deriveFont(Font.PLAIN, 22f));

			String displayText = menuOptions[i];
			if (i == 4) {
				displayText = "LANGUAGE: " + (fancyLanguage ? "FANCY ENGLISH" : "ENGLISH");
			}
			// UPDATED: Dynamic text display for the Theme option
			else if (i == 5) {
				String currentThemeDisplay = (GeneralSave.currentTheme == null) ? "DEFAULT" : GeneralSave.currentTheme.toUpperCase();
				displayText = "THEME: " + currentThemeDisplay;
			}

			FontMetrics fm = g2d.getFontMetrics();
			int textX = btnX + (buttonWidth - fm.stringWidth(displayText)) / 2;
			int textY = btnY + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent();

			g2d.setColor(isSelected ? new Color(255, 255, 255) : new Color(160, 160, 170));
			g2d.drawString(displayText, textX, textY);

			if ((i == 0 && musicMuted) || (i == 1 && soundMuted)) {
				g2d.setColor(Color.RED);
				g2d.setStroke(new BasicStroke(3));
				g2d.drawLine(textX, textY - fm.getAscent() / 3, textX + fm.stringWidth(displayText), textY - fm.getAscent() / 3);
				g2d.setStroke(defaultStroke);
			}

			if (i <= 2) {
				int sliderY = btnY + buttonHeight / 2;
				float currentVal = 0.0f;

				if (i == 0) currentVal = musicMuted ? 0.0f : musicVolume;
				if (i == 1) currentVal = soundMuted ? 0.0f : soundVolume;
				if (i == 2) currentVal = brightness;

				g2d.setColor(new Color(60, 60, 70));
				g2d.fillRect(sliderX, sliderY - 3, sliderWidth, 6);
				g2d.setColor(Color.WHITE);
				g2d.drawRect(sliderX, sliderY - 3, sliderWidth, 6);

				g2d.setColor(new Color(180, 180, 200));
				g2d.fillRect(sliderX, sliderY - 2, (int) (currentVal * sliderWidth), 4);

				int handleX = sliderX + (int) (currentVal * sliderWidth) - 15;
				menuAnimations.paint(g2d, handleX, sliderY - 15, 30, 30, "SliderHandle", 0);
			}
		}

		// --- 4. EASTER EGG LOGIC ---
		showEasterEgg--;

		if (showEasterEgg > 0) {
			String msg = "Really ? no REALLY?";
			g2d.setFont(trajan.deriveFont(Font.ITALIC, 36f));
			FontMetrics msgMetrics = g2d.getFontMetrics();

			g2d.setColor(Color.BLACK);
			g2d.drawString(msg, (screenWidth - msgMetrics.stringWidth(msg)) / 2 + 2, screenHeight - 98);

			g2d.setColor(new Color(255, 100, 100));
			g2d.drawString(msg, (screenWidth - msgMetrics.stringWidth(msg)) / 2, screenHeight - 100);
		}
	}

	// --- MOUSE LISTENERS ---
	public void handleMouseMove(int mouseX, int mouseY) {
		int btnWidth = 320, btnHeight = 55, startY = 250, spacing = 80;
		boolean foundHover = false;
		int previousIndex = selectedIndex; // Track index before updates

		for (int i = 0; i < menuOptions.length; i++) {
			int btnY = startY + i * spacing;

			int btnX = (i <= 2) ? (screenWidth / 2) - 300 : (screenWidth - btnWidth) / 2;

			if (mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
				selectedIndex = i;
				foundHover = true;
				break;
			}
		}
		if (!foundHover && !draggingMusic && !draggingSound && !draggingBrightness) {
			selectedIndex = -1;
		}

		// Play sound ONLY if we hovered onto a new valid button selection
		if (selectedIndex != previousIndex && selectedIndex != -1) {
			menuAnimations.playSound("Hover");
		}
	}

	public void handleMousePressed(int mouseX, int mouseY) {
		int sliderX = (screenWidth / 2) + 50;
		int sliderWidth = 250;
		int startY = 250, spacing = 80, btnHeight = 55;

		int musicY = startY + 0 * spacing + btnHeight / 2;
		if (!musicMuted && mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= musicY - 15 && mouseY <= musicY + 15) draggingMusic = true;

		int soundY = startY + 1 * spacing + btnHeight / 2;
		if (!soundMuted && mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= soundY - 15 && mouseY <= soundY + 15) draggingSound = true;

		int brightY = startY + 2 * spacing + btnHeight / 2;
		if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= brightY - 15 && mouseY <= brightY + 15) draggingBrightness = true;
	}

	public void handleMouseDragged(int mouseX, int mouseY) {
		int sliderX = (screenWidth / 2) + 50;
		int sliderWidth = 250;

		float newValue = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - sliderX) / sliderWidth));

		if (draggingMusic) {
			musicVolume = newValue;
			savedMusicVolume = newValue;
			GeneralSave.musicVolume = newValue;
		}
		if (draggingSound) {
			soundVolume = newValue;
			savedSoundVolume = newValue;
			GeneralSave.sfxVolume = newValue;
		}
		if (draggingBrightness) {
			brightness = newValue;
			GeneralSave.brightness = newValue;
		}
	}

	public void handleMouseReleased(int mouseX, int mouseY) {
		draggingMusic = false;
		draggingSound = false;
		draggingBrightness = false;
	}

	public void handleMouseClick(int mouseX, int mouseY) {
		int btnWidth = 320, btnHeight = 55, startY = 250, spacing = 80;

		for (int i = 0; i < menuOptions.length; i++) {
			int btnY = startY + i * spacing;

			int btnX = (i <= 2) ? (screenWidth / 2) - 300 : (screenWidth - btnWidth) / 2;

			if (mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
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
			if (selectedIndex >= menuOptions.length) selectedIndex = 0;
		}

		// Play sound if selection changed via keyboard
		if (selectedIndex != previousIndex && selectedIndex != -1) {
			menuAnimations.playSound("Hover");
		}

		if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
			if (selectedIndex >= 0) {
				menuAnimations.playSound("Click"); // Play click sound
				selectOption();
			}
		}
		if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) adjustSlider(-0.05f);
		if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) adjustSlider(0.05f);
	}

	private void adjustSlider(float amount) {
		if (selectedIndex == 0 && !musicMuted) {
			musicVolume = Math.max(0.0f, Math.min(1.0f, musicVolume + amount));
			savedMusicVolume = musicVolume;
		} else if (selectedIndex == 1 && !soundMuted) {
			soundVolume = Math.max(0.0f, Math.min(1.0f, soundVolume + amount));
			savedSoundVolume = soundVolume;
		} else if (selectedIndex == 2) {
			brightness = Math.max(0.0f, Math.min(1.0f, brightness + amount));
		}
	}

	private void selectOption() {
		switch (selectedIndex) {
			case 0: // MUSIC
				musicMuted = !musicMuted;
				if (!musicMuted) {
					musicVolume = savedMusicVolume;
					GeneralSave.musicVolume = savedMusicVolume;
				} else {
					GeneralSave.musicVolume = 0.0f;
				}
				break;
			case 1: // SOUND
				soundMuted = !soundMuted;
				if (!soundMuted) {
					soundVolume = savedSoundVolume;
					GeneralSave.sfxVolume = savedSoundVolume;
				} else {
					GeneralSave.sfxVolume = 0.0f;
				}
				break;
			case 2: // BRIGHTNESS
				showEasterEgg = 312;
				break;
			case 3: // REMAPPING
				SystemController.setCurrentState(SystemController.GameState.REMAPPING);
				break;
			case 4: // LANGUAGE
				fancyLanguage = !fancyLanguage;
				break;
			case 5: // THEME
				String current = (GeneralSave.currentTheme == null) ? "Default" : GeneralSave.currentTheme;
				int currentThemeIdx = 0;

				for (int t = 0; t < THEMES.length; t++) {
					if (THEMES[t].equalsIgnoreCase(current)) {
						currentThemeIdx = t;
						break;
					}
				}

				currentThemeIdx = (currentThemeIdx + 1) % THEMES.length;
				GeneralSave.currentTheme = THEMES[currentThemeIdx].equals("Default") ? null : THEMES[currentThemeIdx];
				break;
			case 6: // BACK
				SystemController.setCurrentState(previousState);
				showEasterEgg = 0;
				break;
		}
	}
}