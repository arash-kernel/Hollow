package View.Uis;

import Controller.GeneralSave;
import Controller.SaveFile;
import Controller.SystemController;
import View.Animations;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InventoryUi {
	public SaveFile saveFile;

	// Layout boundaries
	private Rectangle[] equippedSlots; // Handled dynamically via checkEquippedSlots()
	private Rectangle[] baseSlots;
	private Rectangle closeButton;

	// Tracking variables
	private SaveFile.Charm hoveredCharm = null;
	private SaveFile.Charm displayedDescCharm = null;
	private boolean hoverClose = false;
	private int mouseX = -1;
	private int mouseY = -1;

	// Smooth movement tracking
	private Map<SaveFile.Charm, Point2D.Double> currentPositions = new HashMap<>();

	// Graphics & Animations
	private static final int CHARM_SIZE = 80;
	private static final int NOTCH_SIZE = 50;
	private static final int NOTCH_OFFSET = (CHARM_SIZE - NOTCH_SIZE) / 2;

	private Animations animations = new Animations();
	private static Font trajan;

	static {
		try {
			File fontFile = new File("src/Model/Fonts/TrajanPro-Regular.ttf");
			trajan = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(20f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(trajan);
		} catch (IOException | FontFormatException e) {
			trajan = new Font("Arial", Font.PLAIN, 20);
		}
	}

	public InventoryUi(SaveFile saveFile) {
		this.saveFile = saveFile;

		// Perform initial setup of equipped slots layout
		checkEquippedSlots();

		// Setup Base Slots dynamically split into 2 rows, aligned towards the left
		SaveFile.Charm[] allCharms = SaveFile.Charm.values();
		baseSlots = new Rectangle[allCharms.length];

		int charmsPerLine = (int) Math.ceil(allCharms.length / 2.0);
		int startXBottom = 150;
		int startYBottom = 560;
		int rowSpacing = 95;

		for (int i = 0; i < allCharms.length; i++) {
			int row = i / charmsPerLine;
			int col = i % charmsPerLine;

			int x = startXBottom + (col * 90);
			int y = startYBottom + (row * rowSpacing);

			baseSlots[i] = new Rectangle(x, y, CHARM_SIZE, CHARM_SIZE);
		}

		// Adjusted width/height and moved slightly left to fit the asset style
		closeButton = new Rectangle(1280, 50, 200, 55);

		animations.addAnimation("Idle2", "src/Model/Game/DialogueBoxAnimations/");
		animations.addAnimation("CharmNotch", "src/View/InventoryAnimations/");

		for (int i = 0; i < allCharms.length; i++) {
			SaveFile.Charm charm = allCharms[i];
			int equippedIndex = saveFile.currentCharms.indexOf(charm);

			// Position initial setup safely respecting the state rules
			if (equippedIndex != -1 && equippedIndex < equippedSlots.length) {
				currentPositions.put(charm, new Point2D.Double(equippedSlots[equippedIndex].x, equippedSlots[equippedIndex].y));
			} else {
				currentPositions.put(charm, new Point2D.Double(baseSlots[i].x, baseSlots[i].y));
			}
			animations.addAnimation(charm.name(), "src/View/InventoryAnimations/");
		}
	}

	/**
	 * Checks if the equipped slots structure matches the active infiniteCharms setting.
	 * Regenerates array layout dynamically if a state change is detected outside this menu.
	 */
	private void checkEquippedSlots() {
		int expectedMax = GeneralSave.infiniteCharms ? 8 : 3;

		if (equippedSlots == null || equippedSlots.length != expectedMax) {
			this.equippedSlots = new Rectangle[expectedMax];
			int startXTop = 150;
			for (int i = 0; i < expectedMax; i++) {
				equippedSlots[i] = new Rectangle(startXTop + (i * 100), 150, CHARM_SIZE, CHARM_SIZE);
			}
		}
	}

	public void movements() {
		checkEquippedSlots(); // Ensure layout matches settings state immediately
		SaveFile.Charm[] allCharms = SaveFile.Charm.values();

		for (int i = 0; i < allCharms.length; i++) {
			SaveFile.Charm charm = allCharms[i];

			int equippedIndex = saveFile.currentCharms.indexOf(charm);
			double targetX = baseSlots[i].x;
			double targetY = baseSlots[i].y;

			// Only route to an equipped slot if it fits within the active layout bounds
			if (equippedIndex != -1 && equippedIndex < equippedSlots.length) {
				targetX = equippedSlots[equippedIndex].x;
				targetY = equippedSlots[equippedIndex].y;
			}

			Point2D.Double currentPos = currentPositions.get(charm);

			if (Math.abs(targetX - currentPos.x) < 0.5) currentPos.x = targetX;
			else currentPos.x += (targetX - currentPos.x) * 0.2;

			if (Math.abs(targetY - currentPos.y) < 0.5) currentPos.y = targetY;
			else currentPos.y += (targetY - currentPos.y) * 0.2;
		}

		updateHover();
	}

	public void paint(Graphics g) {
		checkEquippedSlots(); // Sync slots length before attempting to draw frames
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2D.setColor(new Color(0, 0, 0, 200));
		g2D.fillRect(0, 0, 1620, 880);

		// Draw CLOSE Button matching MainMenuUi style
		if (hoverClose) {
			// Draw the local Idle2 asset when hovered
			animations.paint(g, closeButton.x, closeButton.y, closeButton.width, closeButton.height, "Idle2", 0);
			g2D.setColor(new Color(255, 255, 255)); // White text on hover
		} else {
			g2D.setColor(new Color(160, 160, 170)); // Menu unselected gray text
		}

		g2D.setFont(trajan.deriveFont(Font.PLAIN, 22f));
		String closeText = "CLOSE";
		FontMetrics fmBtn = g2D.getFontMetrics();

		// Center the text perfectly inside the button bounds
		int textX = closeButton.x + (closeButton.width - fmBtn.stringWidth(closeText)) / 2;
		int textY = closeButton.y + ((closeButton.height - fmBtn.getHeight()) / 2) + fmBtn.getAscent();

		g2D.drawString(closeText, textX, textY);

		// 1. DRAW NOTCHES (Painted first)
		for (Rectangle slot : equippedSlots) {
			animations.paint(g, slot.x + NOTCH_OFFSET, slot.y + NOTCH_OFFSET, NOTCH_SIZE, NOTCH_SIZE, "CharmNotch", 0);
		}
		for (Rectangle slot : baseSlots) {
			animations.paint(g, slot.x + NOTCH_OFFSET, slot.y + NOTCH_OFFSET, NOTCH_SIZE, NOTCH_SIZE, "CharmNotch", 0);
		}

		// 2. DRAW CHARMS
		for (SaveFile.Charm charm : SaveFile.Charm.values()) {
			boolean isLockedVoidHeart = (charm == SaveFile.Charm.VOID_HEART && !saveFile.voidHeart);

			Point2D.Double pos = currentPositions.get(charm);
			int drawX = (int) Math.round(pos.x);
			int drawY = (int) Math.round(pos.y);

			if (charm == hoveredCharm && !isLockedVoidHeart) {
				g2D.setColor(Color.YELLOW);
				g2D.drawRect(drawX - 2, drawY - 2, CHARM_SIZE + 4, CHARM_SIZE + 4);
			}

			int frame = isLockedVoidHeart ? 1 : 0;
			animations.paint(g, drawX, drawY, CHARM_SIZE, CHARM_SIZE, charm.name(), frame);
		}

		// 3. DRAW DESCRIPTION PANEL
		if (displayedDescCharm != null) {
			animations.paint(g, 1150, 250, 350, 400, "Idle2", 0);
			g2D.setColor(Color.WHITE);
			g2D.setFont(trajan.deriveFont(22f));
			g2D.drawString(displayedDescCharm.getDisplayName(), 1170, 400);
			g2D.setFont(trajan.deriveFont(16f));
			drawWrappedText(g2D, displayedDescCharm.getDescription(), 1170, 450, 310);
		}
	}

	public void handleMouseMove(int x, int y) {
		this.mouseX = x;
		this.mouseY = y;
		updateHover();
	}

	private void updateHover() {
		hoverClose = closeButton.contains(mouseX, mouseY);
		hoveredCharm = null;

		for (SaveFile.Charm charm : SaveFile.Charm.values()) {
			Point2D.Double pos = currentPositions.get(charm);
			Rectangle visualHitbox = new Rectangle((int) pos.x, (int) pos.y, CHARM_SIZE, CHARM_SIZE);

			if (visualHitbox.contains(mouseX, mouseY)) {
				hoveredCharm = charm;
				displayedDescCharm = charm;
				break;
			}
		}
	}

	public void handleMouseClick(int x, int y) {
		if (hoverClose) {
			SystemController.setCurrentState(SystemController.GameState.PLAYING);
			return;
		}

		if (hoveredCharm != null) {
			boolean isLockedVoidHeart = (hoveredCharm == SaveFile.Charm.VOID_HEART && !saveFile.voidHeart);
			if (isLockedVoidHeart) {
				return;
			}

			if (saveFile.currentCharms.contains(hoveredCharm)) {
				saveFile.currentCharms.remove(hoveredCharm);
			} else {
				checkEquippedSlots(); // Ensure validation array size is fully updated
				if (saveFile.currentCharms.size() < equippedSlots.length || GeneralSave.infiniteCharms) {
					saveFile.currentCharms.add(hoveredCharm);
				}
			}
		}

		updateHover();
	}

	private void drawWrappedText(Graphics2D g, String text, int x, int y, int width) {
		FontMetrics fm = g.getFontMetrics();
		String[] words = text.split(" ");
		String line = "";

		for (String word : words) {
			if (fm.stringWidth(line + word) < width) {
				line += word + " ";
			} else {
				g.drawString(line, x, y);
				y += fm.getHeight() + 5;
				line = word + " ";
			}
		}
		g.drawString(line, x, y);
	}
}