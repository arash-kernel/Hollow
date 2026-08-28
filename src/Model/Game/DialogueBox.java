package Model.Game;

import Model.Game.Knight.Knight;
import View.Animations;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DialogueBox {

	



	private ArrayList<Message> messages = new ArrayList<>();
	private int currentMessageIndex = 0;

	private double charIndex = 0;

	

	private static Font trajan;



	private Room room;
	private Knight knight;

	
	private static Animations animations = new Animations();
	private String animState = "Spawn";
	private int animFrame = 0;
	private int hold = 4;
	private int messageHold = 3;

	private boolean anyKeyHeld = false;
	private boolean advanceTriggered = false;

	static {

		animations.addAnimation("Spawn", "src/Model/Game/DialogueBoxAnimations/");
		animations.addAnimation("Idle", "src/Model/Game/DialogueBoxAnimations/");
		try {
			File fontFile = new File("src/Model/Fonts/TrajanPro-Regular.ttf");
			trajan = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(20f);

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(trajan);

		} catch (IOException | FontFormatException e) {
			trajan = new Font("Arial", Font.PLAIN, 20);
		}
	}
	


	public DialogueBox(ArrayList<?> input, Room room, Knight knight) {
		this.room = room;
		this.knight = knight;

		knight.setDialogueLocked(true);

		if (!input.isEmpty() && input.get(0) instanceof String) {
			for (Object o : input) {
				messages.add(new Message((String) o, null, false));
			}
		} else {
			this.messages = (ArrayList<Message>) input;
		}
		playCurrentSound();
	}

	public void setInputs(boolean anyKeyHeld, boolean advancePressed) {
		this.anyKeyHeld = anyKeyHeld;
	}

	public void setAdvanceTriggered(boolean triggered) {
		this.advanceTriggered = triggered;
	}

	

	private void playCurrentSound() {
		Message msg = messages.get(currentMessageIndex);
		if (msg == null || msg.sound == null) return;

		try {
			msg.sound.setFramePosition(0);

			if (msg.loop) {
				msg.sound.loop(Clip.LOOP_CONTINUOUSLY);
			} else {
				msg.sound.start();
			}

		} catch (Exception ignored) {}
	}

	private void stopCurrentSound() {
		Message msg = messages.get(currentMessageIndex);
		if (msg != null && msg.sound != null) {
			msg.sound.stop();
		}
	}

	

	public void movements() {

		hold++;
		if (hold >= Units.HOLD.number) {
			animFrame++;
			hold = 0;
		}

		if (animState.equalsIgnoreCase("Spawn")) {
			int maxFrames = animations.animations.get("Spawn").size();

			if (animFrame >= maxFrames - 1) {
				animState = "Idle";
				animFrame = 0;
			}
			return;
		}
		
		if (currentMessageIndex >= messages.size()) {
			closeDialogue();
			return;
		}
		Message current = messages.get(currentMessageIndex);

		
		if (charIndex < current.text.length()) {

			if (advanceTriggered) advanceTriggered = false;

			if (messageHold >= hold) {
				charIndex += anyKeyHeld ? 3 : 1;
				messageHold = 0;
			} else {
				messageHold++;
			}

			if (charIndex > current.text.length()) {
				charIndex = current.text.length();
			}

		} else {

			
			if (advanceTriggered) {
				advanceTriggered = false;
				stopCurrentSound();

				
				if (currentMessageIndex + 1 < messages.size()) {
					currentMessageIndex++;
					charIndex = 0;
					playCurrentSound();
				} else {
					
					closeDialogue();
				}
			}
		}
	}

	public void closeDialogue() {
		stopCurrentSound();
		knight.setDialogueLocked(false);
		room.endDialogue();
	}

	

	public void paint(Graphics g) {

		int x = 200;
		int y = 100;
		int boxWidth = 1200;
		int boxHeight = 600;
		x+=room.panel.camera.getOffsetX();
		y+=room.panel.camera.getOffsetY();

		animations.paint(g, x, y, boxWidth, boxHeight, animState, animFrame);

		if (animState.equalsIgnoreCase("Idle")) {

			g.setColor(Color.WHITE);
			g.setFont(trajan);

			Message current = messages.get(currentMessageIndex);

			int maxChars = (int) Math.min(charIndex, current.text.length());
			String text = current.text.substring(0, maxChars);

			g.drawString(text, x + 200, y + 250);
		}
	}
}