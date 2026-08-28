package View.Uis;

import Controller.GeneralSave;
import Controller.SystemController;
import View.Animations;

import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class AchievementsUi {
	private int screenWidth = 1620;
	private int screenHeight = 880;

	// Menu state
	private Rectangle backButtonRect;
	private boolean isBackHovered = false;

	// Fonts and Animations
	private static Font trajan;
	private static final Animations achAnimations = new Animations();

	// Popup System State
	private Queue<PopupData> popupQueue = new LinkedList<>();
	private PopupData currentPopup = null;
	private int popupTimer = 0;
	private static final int POPUP_DURATION = 180; // Frames the popup stays on screen

	// Data Structure for the 5 Achievements
	private final AchievementDef[] achievements = {
			new AchievementDef("Ach_FalseKnight", "False Knight", "Defeat the False Knight.", 0),
			new AchievementDef("Ach_Completion", "The Master", "Defeat the False Knight and Unite the VOID.", 1),
			new AchievementDef("Ach_TrueHunter", "True Hunter", "Cleanse the land of All its Beasts", 2),
			new AchievementDef("Ach_Zote", "Talkative", "Talk to Zotebote the Mighty.", 3),
			new AchievementDef("Ach_Speedrun", "Speedrun", "Complete the game in under 15 minutes.", 4)
	};

	private static class AchievementDef {
		String id, name, desc;
		int index;
		AchievementDef(String id, String name, String desc, int index) {
			this.id = id; this.name = name; this.desc = desc; this.index = index;
		}
	}

	private static class PopupData {
		String id;
		String name;
		PopupData(String id, String name) { this.id = id; this.name = name; }
	}

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
			System.out.println("Failed to load Trajan font in AchievementsUi: " + e.getMessage());
		}

		// 2. Pre-load background, achievement icons, and menu sounds
		try {
			achAnimations.addAnimation("MenuBG", "src/View/BackGrounds/");
			achAnimations.addAnimation("Ach_FalseKnight", "src/View/UiFrames/Achievements/");
			achAnimations.addAnimation("Ach_Completion", "src/View/UiFrames/Achievements/");
			achAnimations.addAnimation("Ach_Speedrun", "src/View/UiFrames/Achievements/");
			achAnimations.addAnimation("Ach_TrueHunter", "src/View/UiFrames/Achievements/");
			achAnimations.addAnimation("Ach_Zote", "src/View/UiFrames/Achievements/");

			// Load Main Menu Synchronized Sounds
			achAnimations.addSound("Hover", "src/View/MenuSounds/");
			achAnimations.addSound("Click", "src/View/MenuSounds/");
		} catch (Exception e) {
			System.out.println("Failed to load animations/sounds in AchievementsUi: " + e.getMessage());
		}
	}

	public AchievementsUi() {
		backButtonRect = new Rectangle(50, 50, 150, 50);
	}

	// --- MENU RENDERING ---
	public void paint(Graphics g, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 1. Background
		String bgName = GeneralSave.currentTheme != null ? GeneralSave.currentTheme : "MenuBG";
		if (achAnimations.animations.containsKey(bgName)) {
			achAnimations.paint(g2d, 0, 0, screenWidth, screenHeight, bgName, 0);
		} else {
			GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 25), 0, screenHeight, new Color(5, 5, 8));
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, screenWidth, screenHeight);
		}

		// 2. Title
		g2d.setFont(trajan.deriveFont(Font.BOLD, 54f));
		g2d.setColor(new Color(235, 235, 240));
		String title = "ACHIEVEMENTS";
		int titleX = (screenWidth - g2d.getFontMetrics().stringWidth(title)) / 2;
		g2d.drawString(title, titleX, 100);

		// 3. Draw Back Button
		g2d.setFont(trajan.deriveFont(Font.BOLD, 24f));
		g2d.setColor(isBackHovered ? Color.WHITE : new Color(160, 160, 170));
		g2d.drawString("BACK", backButtonRect.x + 20, backButtonRect.y + 35);

		// 4. Draw Achievements List
		int startY = 180;
		int itemHeight = 100;
		int spacing = 20;
		int listWidth = 800;
		int listX = (screenWidth - listWidth) / 2;

		for (int i = 0; i < achievements.length; i++) {
			AchievementDef ach = achievements[i];
			boolean unlocked = isUnlocked(ach.index);

			int currentY = startY + (i * (itemHeight + spacing));

			// Background Panel
			g2d.setColor(new Color(30, 30, 35, 180));
			g2d.fillRect(listX, currentY, listWidth, itemHeight);
			g2d.setColor(unlocked ? new Color(150, 150, 160) : new Color(60, 60, 70));
			g2d.drawRect(listX, currentY, listWidth, itemHeight);

			// Icon
			int iconSize = 80;
			int iconX = listX + 10;
			int iconY = currentY + 10;

			// Draw Icon (Full opacity if unlocked, faded if locked)
			int opacity = unlocked ? 255 : 80;
			achAnimations.paint(g2d, iconX, iconY, iconSize, iconSize, ach.id, 0, opacity);

			// Texts
			int textX = iconX + iconSize + 30;
			g2d.setFont(trajan.deriveFont(Font.BOLD, 28f));
			g2d.setColor(unlocked ? Color.WHITE : new Color(100, 100, 100));
			g2d.drawString(unlocked ? ach.name : "???", textX, currentY + 45);

			g2d.setFont(trajan.deriveFont(Font.PLAIN, 18f));
			g2d.setColor(unlocked ? new Color(200, 200, 210) : new Color(80, 80, 80));
			g2d.drawString(unlocked ? ach.desc : "Keep playing to reveal this achievement.", textX, currentY + 75);
		}
	}

	// --- POPUP SYSTEM ---
	public void updateAndDrawPopups(Graphics2D g2d, int screenWidth, int screenHeight) {
		// 1. Check for newly unlocked achievements
		checkPopupTriggers();

		// 2. Manage Queue - This guarantees only ONE shows at a time!
		if (currentPopup == null && !popupQueue.isEmpty()) {
			currentPopup = popupQueue.poll();
			popupTimer = 0;
		}

		// 3. Render Current Popup
		if (currentPopup != null) {
			popupTimer++;

			// Slide animation math (Horizontal from the right)
			int popupWidth = 350;
			int targetX = screenWidth - popupWidth;
			int currentX = targetX; // Default fully on screen
			int currentY = screenHeight - 120; // Fixed Y position

			if (popupTimer < 20) {
				// Slide left (in from the right edge)
				currentX = screenWidth - (int)((popupTimer / 20.0) * popupWidth);
			} else if (popupTimer > POPUP_DURATION - 20) {
				// Slide right (out towards the right edge)
				int fadeOutTimer = POPUP_DURATION - popupTimer;
				currentX = screenWidth - (int)((fadeOutTimer / 20.0) * popupWidth);
			}

			int imgSize = 80;
			int imgX = currentX;

			// Gray transparent rectangle extending right
			int rectX = imgX + (imgSize / 2);
			int rectY = currentY + 15;
			int rectWidth = screenWidth - rectX; // Extends fully to the right edge
			int rectHeight = 50;

			g2d.setColor(new Color(50, 50, 50, 200));
			g2d.fillRect(rectX, rectY, rectWidth, rectHeight);

			// Draw text inside rectangle (SMALLER TEXT)
			g2d.setFont(trajan.deriveFont(Font.BOLD, 14f)); // Reduced from 18f
			g2d.setColor(Color.WHITE);
			g2d.drawString("Unlocked: " + currentPopup.name, imgX + imgSize + 15, rectY + 30);

			// Draw Image over the rectangle
			achAnimations.paint(g2d, imgX, currentY, imgSize, imgSize, currentPopup.id, 0, 255);

			// Clear it when done to allow the next one in the queue to show
			if (popupTimer >= POPUP_DURATION) {
				currentPopup = null;
			}
		}
	}
	private void checkPopupTriggers() {
		if (GeneralSave.talkToZote && !GeneralSave.shownTalkToZote) {
			popupQueue.add(new PopupData("Ach_Zote", "Talkative"));
			GeneralSave.shownTalkToZote = true;
		}
		if (GeneralSave.trueHunter && !GeneralSave.shownTrueHunter) {
			popupQueue.add(new PopupData("Ach_TrueHunter", "True Hunter"));
			GeneralSave.shownTrueHunter = true;
		}
		if (GeneralSave.defeatFalseKnight && !GeneralSave.shownDefeatFalseKnight) {
			popupQueue.add(new PopupData("Ach_FalseKnight", "False Knight"));
			GeneralSave.shownDefeatFalseKnight = true;
		}
		if (GeneralSave.completion && !GeneralSave.shownCompletion) {
			popupQueue.add(new PopupData("Ach_Completion", "The Master"));
			GeneralSave.shownCompletion = true;
		}
		if (GeneralSave.speedrun && !GeneralSave.shownSpeedrun) {
			popupQueue.add(new PopupData("Ach_Speedrun", "Speedrun"));
			GeneralSave.shownSpeedrun = true;
		}
	}

	private boolean isUnlocked(int index) {
		switch(index) {
			case 0: return GeneralSave.defeatFalseKnight;
			case 1: return GeneralSave.completion;
			case 2: return GeneralSave.speedrun;
			case 3: return GeneralSave.trueHunter;
			case 4: return GeneralSave.talkToZote;
			default: return false;
		}
	}

	// --- INPUT HANDLING ---
	public void handleMouseMove(int x, int y) {
		boolean previousHovered = isBackHovered;
		isBackHovered = backButtonRect.contains(x, y);

		// Play sound only on initial entrance boundary transition
		if (isBackHovered && !previousHovered) {
			achAnimations.playSound("Hover");
		}
	}

	public void handleMouseClick(int x, int y) {
		if (backButtonRect.contains(x, y)) {
			achAnimations.playSound("Click");
			SystemController.setCurrentState(SystemController.GameState.MAIN_MENU);
		}
	}
}