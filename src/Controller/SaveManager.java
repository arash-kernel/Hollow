package Controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SaveManager {

	
	private static final String SAVE_DIR = "src/SaveFiles/";

	/**
	 * Saves the SaveFile data to a specified text file based on its ID.
	 */
	public static void saveToFile(SaveFile save) {
		
		File dir = new File(SAVE_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		
		String filePath = SAVE_DIR + "file" + save.id + ".txt";
		Properties props = new Properties();

		
		props.setProperty("id", String.valueOf(save.id));
		props.setProperty("time", String.valueOf(save.time));
		props.setProperty("totalEnemyKilled", String.valueOf(save.totalEnemyKilled));
		props.setProperty("deathCount", String.valueOf(save.deathCount));
		props.setProperty("nameOfLevel", save.nameOfLevel != null ? save.nameOfLevel : "Green Path");

		
		props.setProperty("killedBoss", String.valueOf(save.killedBoss));
		props.setProperty("killedHunter", String.valueOf(save.killedHunter));
		props.setProperty("killedCrawler", String.valueOf(save.killedCrawler));
		props.setProperty("killedLaser", String.valueOf(save.killedLaser));
		props.setProperty("killedHorn", String.valueOf(save.killedHorn));
		props.setProperty("killedMoss", String.valueOf(save.killedMoss));
		props.setProperty("killedFly", String.valueOf(save.killedFly));
		props.setProperty("voidHeart", String.valueOf(save.voidHeart));

		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < save.currentCharms.size(); i++) {
			sb.append(save.currentCharms.get(i).name());
			if (i < save.currentCharms.size() - 1) {
				sb.append(",");
			}
		}
		props.setProperty("currentCharms", sb.toString());

		
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			props.store(out, "Hollow Knight Clone - Save File Data");
			System.out.println("Game saved successfully to: " + filePath);
		} catch (IOException e) {
			System.out.println("Failed to save game: " + e.getMessage());
		}
	}

	/**
	 * Loads the SaveFile data from a specified text file based on slot ID.
	 *
	 * @return A populated SaveFile object, or null if the file doesn't exist.
	 */
	public static SaveFile loadFromFile(int slotId) {
		String filePath = SAVE_DIR + "file" + slotId + ".txt";
		File file = new File(filePath);

		if (!file.exists()) {
			return null;
		}

		Properties props = new Properties();
		SaveFile loadedSave = new SaveFile(slotId);

		try (FileInputStream in = new FileInputStream(file)) {
			props.load(in);

			
			loadedSave.time = Integer.parseInt(props.getProperty("time", "0"));
			loadedSave.totalEnemyKilled = Integer.parseInt(props.getProperty("totalEnemyKilled", "0"));
			loadedSave.deathCount = Integer.parseInt(props.getProperty("deathCount", "0"));
			loadedSave.nameOfLevel = props.getProperty("nameOfLevel", "Green Path");

			
			loadedSave.killedBoss = Boolean.parseBoolean(props.getProperty("killedBoss", "false"));
			loadedSave.killedHunter = Boolean.parseBoolean(props.getProperty("killedHunter", "false"));
			loadedSave.killedCrawler = Boolean.parseBoolean(props.getProperty("killedCrawler", "false"));
			loadedSave.killedLaser = Boolean.parseBoolean(props.getProperty("killedLaser", "false"));
			loadedSave.killedHorn = Boolean.parseBoolean(props.getProperty("killedHorn", "false"));
			loadedSave.killedMoss = Boolean.parseBoolean(props.getProperty("killedMoss", "false"));
			loadedSave.killedFly = Boolean.parseBoolean(props.getProperty("killedFly", "false"));
			loadedSave.voidHeart = Boolean.parseBoolean(props.getProperty("voidHeart", "false"));

			
			String charmsData = props.getProperty("currentCharms", "");
			if (!charmsData.isEmpty()) {
				String[] charmNames = charmsData.split(",");
				for (String name : charmNames) {
					try {
						
						loadedSave.currentCharms.add(SaveFile.Charm.valueOf(name));
					} catch (IllegalArgumentException e) {
						System.err.println("Skipping invalid charm in save file: " + name);
					}
				}
			}

			System.out.println("Game loaded successfully from: " + filePath);
			return loadedSave;

		} catch (IOException | NumberFormatException e) {
			System.out.println("Save file is corrupted or unreadable. Error: " + e.getMessage());
			return null;
		}
	}
	


	private static final String GLOBAL_SETTINGS_FILE = SAVE_DIR + "globalSettings.txt";

	/**
	 * Saves all static variables from GeneralSave into a global settings file.
	 */


	public static void saveGlobalSettings() {
		File dir = new File(SAVE_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		Properties props = new Properties();

		
		props.setProperty("currentTheme", GeneralSave.currentTheme);
		props.setProperty("musicVolume", String.valueOf(GeneralSave.musicVolume));
		props.setProperty("sfxVolume", String.valueOf(GeneralSave.sfxVolume));
		props.setProperty("brightness", String.valueOf(GeneralSave.brightness));
		props.setProperty("isMuted", String.valueOf(GeneralSave.isMuted));

		
		props.setProperty("noClip", String.valueOf(GeneralSave.noClip));
		props.setProperty("emergencyHeal", String.valueOf(GeneralSave.emergencyHeal));
		props.setProperty("godMode", String.valueOf(GeneralSave.godMode));
		props.setProperty("infiniteCharms", String.valueOf(GeneralSave.infiniteCharms));
		props.setProperty("showHitbox", String.valueOf(GeneralSave.showHitbox));

		
		props.setProperty("completion", String.valueOf(GeneralSave.completion));
		props.setProperty("speedrun", String.valueOf(GeneralSave.speedrun));
		props.setProperty("trueHunter", String.valueOf(GeneralSave.trueHunter));
		props.setProperty("defeatFalseKnight", String.valueOf(GeneralSave.defeatFalseKnight));
		props.setProperty("talkToZote", String.valueOf(GeneralSave.talkToZote));

		props.setProperty("shownCompletion", String.valueOf(GeneralSave.shownCompletion));
		props.setProperty("shownSpeedrun", String.valueOf(GeneralSave.shownSpeedrun));
		props.setProperty("shownTrueHunter", String.valueOf(GeneralSave.shownTrueHunter));
		props.setProperty("shownDefeatFalseKnight", String.valueOf(GeneralSave.shownDefeatFalseKnight));
		props.setProperty("shownTalkToZote", String.valueOf(GeneralSave.shownTalkToZote));

		
		
		for (String action : GeneralSave.keybinds.keySet()) {
			props.setProperty("Key_" + action, String.valueOf(GeneralSave.keybinds.get(action)));
		}

		
		try (FileOutputStream out = new FileOutputStream(GLOBAL_SETTINGS_FILE)) {
			props.store(out, "Hollow Knight Clone - Global Settings & Achievements");
			System.out.println("Global settings saved successfully.");
		} catch (IOException e) {
			System.out.println("Failed to save global settings: " + e.getMessage());
		}
	}

	/**
	 * Loads all global settings back into the GeneralSave static variables.
	 */
	public static void loadGlobalSettings() {
		File file = new File(GLOBAL_SETTINGS_FILE);

		
		if (!file.exists()) {
			return;
		}

		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(file)) {
			props.load(in);

			
			GeneralSave.currentTheme = props.getProperty("currentTheme", "EDP");
			GeneralSave.musicVolume = Float.parseFloat(props.getProperty("musicVolume", "1.0"));
			GeneralSave.sfxVolume = Float.parseFloat(props.getProperty("sfxVolume", "0.5"));
			GeneralSave.brightness = Float.parseFloat(props.getProperty("brightness", "1.0"));
			GeneralSave.isMuted = Boolean.parseBoolean(props.getProperty("isMuted", "false"));

			
			GeneralSave.noClip = Boolean.parseBoolean(props.getProperty("noClip", "false"));
			GeneralSave.emergencyHeal = Boolean.parseBoolean(props.getProperty("emergencyHeal", "false"));
			GeneralSave.godMode = Boolean.parseBoolean(props.getProperty("godMode", "false"));
			GeneralSave.infiniteCharms = Boolean.parseBoolean(props.getProperty("infiniteCharms", "true"));
			GeneralSave.showHitbox = Boolean.parseBoolean(props.getProperty("showHitbox", "false"));

			
			GeneralSave.completion = Boolean.parseBoolean(props.getProperty("completion", "false"));
			GeneralSave.speedrun = Boolean.parseBoolean(props.getProperty("speedrun", "false"));
			GeneralSave.trueHunter = Boolean.parseBoolean(props.getProperty("trueHunter", "false"));
			GeneralSave.defeatFalseKnight = Boolean.parseBoolean(props.getProperty("defeatFalseKnight", "false"));
			GeneralSave.talkToZote = Boolean.parseBoolean(props.getProperty("talkToZote", "false"));

			GeneralSave.shownCompletion = Boolean.parseBoolean(props.getProperty("shownCompletion", "false"));
			GeneralSave.shownSpeedrun = Boolean.parseBoolean(props.getProperty("shownSpeedrun", "false"));
			GeneralSave.shownTrueHunter = Boolean.parseBoolean(props.getProperty("shownTrueHunter", "false"));
			GeneralSave.shownDefeatFalseKnight = Boolean.parseBoolean(props.getProperty("shownDefeatFalseKnight", "false"));
			GeneralSave.shownTalkToZote = Boolean.parseBoolean(props.getProperty("shownTalkToZote", "false"));

			
			
			for (String action : GeneralSave.keybinds.keySet()) {
				String keyProp = props.getProperty("Key_" + action);
				if (keyProp != null) {
					GeneralSave.keybinds.put(action, Integer.parseInt(keyProp));
				}
			}

			System.out.println("Global settings loaded successfully.");
		} catch (IOException | NumberFormatException e) {
			System.out.println("Global settings corrupted. Reverting to defaults. Error: " + e.getMessage());
		}
	}
}