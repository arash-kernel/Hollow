package Controller;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MusicManager {
	private static Clip currentClip;
	private static String currentSong = "";


	public static void playMusic(String songName, float volume) {
		if (songName == null || songName.isEmpty()) return;

		if (songName.equalsIgnoreCase("None")) {
			if (currentClip != null) {
				currentClip.stop();
				currentClip.close();
			}
			currentSong = "";
			return;
		}

		if (songName.equals(currentSong) && currentClip != null && currentClip.isRunning()) {
			setVolume(volume);
			return;
		}

		if (currentClip != null) {
			currentClip.stop();
			currentClip.close();
		}

		try {
			File file = new File("src/Model/Music/" + songName);
			if (!file.exists()) {
				System.err.println("Music file not found: " + file.getPath());
				return;
			}

			AudioInputStream audioInput = AudioSystem.getAudioInputStream(file);
			currentClip = AudioSystem.getClip();
			currentClip.open(audioInput);

			setVolume(volume);
			currentClip.loop(Clip.LOOP_CONTINUOUSLY);
			currentClip.start();

			currentSong = songName;
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}


	public static void setVolume(float volume) {
		if (currentClip != null && currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);

			if (GeneralSave.isMuted || volume <= 0.001f) {
				gainControl.setValue(gainControl.getMinimum());
			} else {
				float dB = (float) (Math.log10(volume) * 20.0);
				gainControl.setValue(dB);
			}
		}
	}
}