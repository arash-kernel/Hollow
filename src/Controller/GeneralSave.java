package Controller;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class GeneralSave {
	public static String currentTheme = "EDP";
	public static float musicVolume = 1.0f;
	public static float sfxVolume = 0.5f;
	public static boolean isMuted = false;
	public static float brightness = 1.0f;
	public String actionToRemap = null;
	public static HashMap<String, Integer> keybinds = new HashMap<>();
	public static boolean noClip = false, emergencyHeal = false, godMode = false, infiniteCharms = false, showHitbox = false;
	public static boolean completion = false, speedrun = false, trueHunter = false, defeatFalseKnight = false, talkToZote = false;
	public static boolean shownCompletion = false, shownSpeedrun = false, shownTrueHunter = false, shownDefeatFalseKnight = false, shownTalkToZote = false;

	static {
		keybinds.put("Up", KeyEvent.VK_W);
		keybinds.put("Down", KeyEvent.VK_S);
		keybinds.put("Left", KeyEvent.VK_A);
		keybinds.put("Right", KeyEvent.VK_D);
		keybinds.put("Jump", KeyEvent.VK_SPACE);
		keybinds.put("Attack", KeyEvent.VK_J);
		keybinds.put("Dash", KeyEvent.VK_K);
		keybinds.put("Focus", KeyEvent.VK_CONTROL);
		keybinds.put("QuickCast", KeyEvent.VK_F);
		keybinds.put("Inventory", KeyEvent.VK_I);
		keybinds.put("Pause", KeyEvent.VK_ESCAPE);
	}

	public static void resetKeybinds() {
		keybinds.put("Up", KeyEvent.VK_W);
		keybinds.put("Down", KeyEvent.VK_S);
		keybinds.put("Left", KeyEvent.VK_A);
		keybinds.put("Right", KeyEvent.VK_D);
		keybinds.put("Jump", KeyEvent.VK_SPACE);
		keybinds.put("Attack", KeyEvent.VK_J);
		keybinds.put("Dash", KeyEvent.VK_K);
		keybinds.put("Focus", KeyEvent.VK_E);
		keybinds.put("QuickCast", KeyEvent.VK_F);
		keybinds.put("Inventory", KeyEvent.VK_I);
		keybinds.put("Pause", KeyEvent.VK_ESCAPE);
	}
}