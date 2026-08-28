package View.Uis;

import Controller.SystemController;
import Controller.SaveFile;
import Model.Game.Units;
import View.Animations;

import java.awt.*;
import java.io.File;

public class VictoryUi {
	public SaveFile saveFile;
	private final Animations animations = new Animations();
	private static Font trajan;

	private int screenWidth = 1620;
	private int screenHeight = 880;

	private int hoveredIndex = -1;
	private final String[] options = {"CONTINUE", "MAIN MENU"};
	private boolean tick=false;
	// Ticker for the animation frame (0 to 222)
	private int ticker = 0;

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

	public VictoryUi(SaveFile saveFile) {
		this.saveFile = saveFile;
		try {
			animations.addAnimation("Idle2", "src/Model/Game/DialogueBoxAnimations/");
		} catch (Exception e) {
			System.out.println("Failed to load animations in VictoryUi: " + e.getMessage());
		}
		try {
			animations.addGif("frame","src/View/BackGrounds/VictoryDance/");
		} catch (Exception e) {
			System.out.println("Could not load Victory GIF");
		}
	}

	public void paint(Graphics g, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int currentFrame = ticker % 223;
		if(tick) {
			ticker++;
			tick=false;
		}
		else{
			tick=true;
		}
		// 1. Transparent Dark Overlay
		g2d.setColor(new Color(0, 0, 0, 210));
		g2d.fillRect(0, 0, screenWidth, screenHeight);

		// 2. Header Title
		String title = "VICTORY ACHIEVED";
		g2d.setFont(trajan.deriveFont(Font.BOLD, 54f));
		g2d.setColor(new Color(255, 215, 0)); // Gold color for victory
		FontMetrics titleMetrics = g2d.getFontMetrics();
		g2d.drawString(title, (screenWidth - titleMetrics.stringWidth(title)) / 2, 150);

		// 3. Draw Side GIFs
		int gifWidth = 360;
		int gifHeight = 400;
		int yPos = (screenHeight - gifHeight) / 2;

		// Left side (normal)
		animations.paint(g2d, 150, yPos, gifWidth, gifHeight, "frame", currentFrame);

		// Right side (flipped using your custom paintflipped method)
		int rightX = screenWidth - 150 - gifWidth;
		animations.paintFlipped(g2d, rightX, yPos, gifWidth, gifHeight, "frame", currentFrame);

		// 4. Draw Stats Panel
		g2d.setFont(trajan.deriveFont(Font.PLAIN, 28f));
		g2d.setColor(Color.WHITE);

		// Format Time
		int totalSeconds = (int) (saveFile.time * Units.TICK.number);
		int hours = totalSeconds / 3600;
		int minutes = (totalSeconds % 3600) / 60;
		int seconds = totalSeconds % 60;

		String timeString;
		if (hours > 0) {
			timeString = String.format("%d:%02d:%02d", hours, minutes, seconds);
		} else {
			timeString = String.format("%02d:%02d", minutes, seconds);
		}

		String[] stats = {
				"DEATH TOLL: " + saveFile.deathCount,
				"ENEMIES VANQUISHED: " + saveFile.totalEnemyKilled,
				"TIME SPENT: " + timeString
		};

		int statsStartY = 280;
		int statsSpacing = 50;
		FontMetrics statsMetrics = g2d.getFontMetrics();

		for (int i = 0; i < stats.length; i++) {
			int textX = (screenWidth - statsMetrics.stringWidth(stats[i])) / 2;
			g2d.drawString(stats[i], textX, statsStartY + (i * statsSpacing));
		}

		// 5. Draw Buttons
		int btnWidth = 320;
		int btnHeight = 55;
		int btnStartY = 550;
		int btnSpacing = 25;

		for (int i = 0; i < options.length; i++) {
			int btnX = (screenWidth - btnWidth) / 2;
			int btnY = btnStartY + i * (btnHeight + btnSpacing);

			if (i == hoveredIndex) {
				animations.paint(g2d, btnX, btnY, btnWidth, btnHeight, "Idle2", 0);
				g2d.setColor(Color.WHITE);
			} else {
				g2d.setColor(new Color(160, 160, 170));
			}

			g2d.setFont(trajan.deriveFont(Font.PLAIN, 22f));
			FontMetrics fm = g2d.getFontMetrics();
			int textX = btnX + (btnWidth - fm.stringWidth(options[i])) / 2;
			int textY = btnY + ((btnHeight - fm.getHeight()) / 2) + fm.getAscent();
			g2d.drawString(options[i], textX, textY);
		}
	}

	public void handleMouseMove(int mouseX, int mouseY) {
		int btnWidth = 320;
		int btnHeight = 55;
		int btnStartY = 550;
		int btnSpacing = 25;
		hoveredIndex = -1;

		for (int i = 0; i < options.length; i++) {
			int btnX = (screenWidth - btnWidth) / 2;
			int btnY = btnStartY + i * (btnHeight + btnSpacing);
			if (mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
				hoveredIndex = i;
				break;
			}
		}
	}

	public void handleMouseClick(int mouseX, int mouseY) {
		if (hoveredIndex != -1) {
			switch (hoveredIndex) {
				case 0: // CONTINUE
					SystemController.setCurrentState(SystemController.GameState.PLAYING);
					break;
				case 1: // MAIN MENU
					SystemController.setCurrentState(SystemController.GameState.MAIN_MENU);
					break;
			}
		}
	}
}