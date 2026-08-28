package View.Uis;

import Controller.GeneralSave;
import Controller.SystemController;
import View.Animations;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class GuideUi {
	private int selectedIndex = -1; // Default to -1 so it doesn't automatically show a border
	private final String[] menuOptions = {"BACK"};

	// Track screen dimensions to accurately calculate mouse hitboxes
	private int screenWidth = 1920;
	private int screenHeight = 1080;

	// Static fields for assets and custom typography
	private static final Animations menuAnimations = new Animations();
	private static Font trajan;

	private int animTick = 0;

	static {
		try {
			File fontFile = new File("src/Model/Fonts/TrajanPro-Regular.ttf");
			trajan = Font.createFont(Font.TRUETYPE_FONT, fontFile);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(trajan);
		} catch (IOException | FontFormatException e) {
			trajan = new Font("Arial", Font.PLAIN, 20);
		}

		// Pre-load backgrounds and animations
		menuAnimations.addAnimation("Idle2", "src/Model/Game/DialogueBoxAnimations/");
		menuAnimations.addAnimation("EDP", "src/View/BackGrounds/");
		menuAnimations.addAnimation("Crystal Peak", "src/View/BackGrounds/");
		menuAnimations.addAnimation("Green Path", "src/View/BackGrounds/");
		menuAnimations.addAnimation("Is This A Hazbin Hotel", "src/View/BackGrounds/");
		menuAnimations.addAnimation("S!LK S0NG", "src/View/BackGrounds/");
		menuAnimations.addSound("Hover", "src/View/MenuSounds/");
		menuAnimations.addSound("Click", "src/View/MenuSounds/");

		// Load Knight Sprites locally
		String knightPath = "src/Model/Game/Knight/KnightAnimations/";
		menuAnimations.addAnimation("Double Jump", knightPath);
		menuAnimations.addAnimation("Scream", knightPath);
		menuAnimations.addAnimation("Wall Slide", knightPath);
		menuAnimations.addAnimation("Focus", knightPath);
	}

	public GuideUi() {
	}

	private String getKeyName(String action) {
		int keyCode = GeneralSave.keybinds.getOrDefault(action, 0);
		return KeyEvent.getKeyText(keyCode).toUpperCase();
	}

	private int getBtnX() {
		int boxWidth = 1400;
		int boxX = (this.screenWidth - boxWidth) / 2;
		int buttonWidth = 320;
		return boxX + boxWidth - buttonWidth ;
	}

	private int getBtnY() {
		int boxY = 50;
		int textStartY = boxY + 160;
		int numAbilities = 10;
		int lineSpacing = 46;

		int soulBlockY = textStartY + (numAbilities * lineSpacing) + 20;
		int hpBlockY = soulBlockY + 85;

		return hpBlockY - 10;
	}

	public void paint(Graphics g, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		animTick++;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 1. Draw Theme-Based Background
		String bgAnimationName = GeneralSave.currentTheme;
		if (menuAnimations.animations.containsKey(bgAnimationName)) {
			menuAnimations.paint(g2d, 0, 0, screenWidth, screenHeight, bgAnimationName, 0);
		} else {
			GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 25), 0, screenHeight, new Color(5, 5, 8));
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, screenWidth, screenHeight);
		}

		// 2. Translucent Box Frame
		int boxWidth = 1400;
		int boxHeight = 900;
		int boxX = (screenWidth - boxWidth) / 2;
		int boxY = 50;

		g2d.setColor(new Color(15, 15, 20, 230));
		g2d.fillRect(boxX, boxY, boxWidth, boxHeight);
		g2d.setColor(new Color(135, 110, 75, 180));
		g2d.drawRect(boxX, boxY, boxWidth, boxHeight);

		// 3. Header title Text
		g2d.setFont(trajan.deriveFont(Font.BOLD, 42f));
		g2d.setColor(new Color(230, 225, 215));
		g2d.drawString("ADVENTURE GUIDE", boxX + 60, boxY + 85);

		// 4. Abilities & Controls Column Setup
		int textX = boxX + 70;
		int textStartY = boxY + 160;
		int lineSpacing = 46;

		g2d.setFont(trajan.deriveFont(Font.PLAIN, 20f));
		String[] abilities = {
				"MOVE UP: " + getKeyName("Up"),
				"MOVE DOWN: " + getKeyName("Down"),
				"MOVE LEFT: " + getKeyName("Left"),
				"MOVE RIGHT: " + getKeyName("Right"),
				"JUMP / DOUBLE JUMP: " + getKeyName("Jump"),
				"DASH (AIR OR GROUND): " + getKeyName("Dash"),
				"ATTACK (NAIL SLASH): " + getKeyName("Attack"),
				"FOCUS SOUL (HEAL): " + getKeyName("Focus"),
				"QUICK CAST (VENOMOUS BLAST): " + getKeyName("QuickCast"),
				"OPEN / CLOSE INVENTORY: " + getKeyName("Inventory")
		};

		for (int i = 0; i < abilities.length; i++) {
			g2d.setColor(new Color(155, 155, 165));
			String[] parts = abilities[i].split(": ");
			g2d.drawString(parts[0] + ":", textX, textStartY + (i * lineSpacing));
			g2d.setColor(new Color(225, 195, 125));
			g2d.drawString(parts[1], textX + 340, textStartY + (i * lineSpacing));
		}

		// 5. Soul vessel mechanics explanation block
		int soulBlockY = textStartY + (abilities.length * lineSpacing) + 20;
		g2d.setFont(trajan.deriveFont(Font.BOLD, 22f));
		g2d.setColor(new Color(210, 205, 195));
		g2d.drawString("THE SOUL VESSEL", textX, soulBlockY);

		g2d.setFont(new Font("Arial", Font.PLAIN, 15));
		g2d.setColor(new Color(165, 170, 175));
		g2d.drawString("Striking hostile creatures with your nail harvests their life-force, condensing it as SOUL inside your vessel.", textX, soulBlockY + 28);
		g2d.drawString("Hold " + getKeyName("Focus") + " to utilize gathered SOUL to mend your shell, or tap " + getKeyName("QuickCast") + " to instantly fire a heavy magic strike.", textX, soulBlockY + 50);

		// 6. Health & Mask system explanation block
		int hpBlockY = soulBlockY + 95;
		g2d.setFont(trajan.deriveFont(Font.BOLD, 22f));
		g2d.setColor(new Color(210, 205, 195));
		g2d.drawString("VITALITY & MASKS", textX, hpBlockY);

		g2d.setFont(new Font("Arial", Font.PLAIN, 15));
		g2d.setColor(new Color(165, 170, 175));
		g2d.drawString("Your resilience is tracked via structural Mask segments. Receiving physical harm breaks a mask away instantly.", textX, hpBlockY + 28);
		g2d.drawString("Should your total masks deplete to absolute zero, you dissolve into the VOID and return back into the bench.", textX, hpBlockY + 50);
		g2d.drawString("Take breaks at resting benches across Hallownest to recover fully and lock in active save parameters.", textX, hpBlockY + 72);

		// 7. Right hand layout panel for animations
		int animBoxX = boxX + 850;
		int animBoxY = boxY + 140;
		int animBoxW = 480;
		int animBoxH = 500;

		g2d.setColor(new Color(10, 10, 12, 160));
		g2d.fillRect(animBoxX, animBoxY, animBoxW, animBoxH);
		g2d.setColor(new Color(65, 60, 55));
		g2d.drawRect(animBoxX, animBoxY, animBoxW, animBoxH);

		// Switch renders based on loop frames
		int frameIdx = (animTick / 6) % 6;
		String currentAnim = "Focus";
		int cycle = (animTick / 36) % 4;

		if (cycle == 0) currentAnim = "Focus";
		else if (cycle == 1) currentAnim = "Double Jump";
		else if (cycle == 2) currentAnim = "Wall Slide";
		else currentAnim = "Scream";

		if (menuAnimations.animations.containsKey(currentAnim)) {
			menuAnimations.paint(g2d, animBoxX + (animBoxW - 190) / 2, animBoxY + (animBoxH - 190) / 2, 190, 190, currentAnim, frameIdx);
		}

		g2d.setFont(trajan.deriveFont(Font.PLAIN, 16f));
		g2d.setColor(new Color(140, 135, 125));
		int strW = g2d.getFontMetrics().stringWidth(currentAnim);
		g2d.drawString(currentAnim, animBoxX + (animBoxW - strW) / 2, animBoxY + animBoxH - 35);

		// 8. Render standard layout UI controls
		int buttonWidth = 320;
		int buttonHeight = 55;
		int btnX = getBtnX();
		int btnY = getBtnY();

		boolean isSelected = (selectedIndex == 0);
		if (isSelected) {
			menuAnimations.paint(g2d, btnX, btnY, buttonWidth, buttonHeight, "Idle2", 0);
		}

		g2d.setFont(trajan.deriveFont(Font.PLAIN, 22f));
		int tx = btnX + (buttonWidth - g2d.getFontMetrics().stringWidth(menuOptions[0])) / 2;
		int ty = btnY + ((buttonHeight - g2d.getFontMetrics().getHeight()) / 2) + g2d.getFontMetrics().getAscent();

		g2d.setColor(isSelected ? Color.WHITE : new Color(160, 160, 170));
		g2d.drawString(menuOptions[0], tx, ty);
	}

	public void handleMouseMove(int mouseX, int mouseY) {
		int buttonWidth = 320;
		int buttonHeight = 55;
		int btnX = getBtnX();
		int btnY = getBtnY();

		int previousIndex = selectedIndex;
		if (mouseX >= btnX && mouseX <= btnX + buttonWidth &&
				mouseY >= btnY && mouseY <= btnY + buttonHeight) {
			selectedIndex = 0;
		} else {
			selectedIndex = -1;
		}

		if (selectedIndex != previousIndex && selectedIndex != -1) {
			menuAnimations.playSound("Hover");
		}
	}

	public void handleMouseClick(int mouseX, int mouseY) {
		int buttonWidth = 320;
		int buttonHeight = 55;
		int btnX = getBtnX();
		int btnY = getBtnY();

		if (mouseX >= btnX && mouseX <= btnX + buttonWidth &&
				mouseY >= btnY && mouseY <= btnY + buttonHeight) {
			selectedIndex = 0;
			menuAnimations.playSound("Click");
			selectOption();
		}
	}

	public void handleKeyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int previousIndex = selectedIndex;

		if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP ||
				keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN ||
				keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT ||
				keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
			if (selectedIndex == -1) selectedIndex = 0;
		}

		if (selectedIndex != previousIndex && selectedIndex != -1) {
			menuAnimations.playSound("Hover");
		}

		if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
			if (selectedIndex == 0) {
				menuAnimations.playSound("Click");
				selectOption();
			}
		}
		if (keyCode == KeyEvent.VK_ESCAPE) {
			menuAnimations.playSound("Click");
			SystemController.setCurrentState(SystemController.GameState.MAIN_MENU);
		}
	}

	private void selectOption() {
		if (selectedIndex == 0) {
			SystemController.setCurrentState(SystemController.GameState.MAIN_MENU);
		}
	}
}