package View;

import Controller.GeneralSave;
import Controller.SystemController;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import javax.sound.sampled.*;

public class Animations {
	public HashMap<String, ArrayList<Image>> animations=new HashMap<>();
	
	public HashMap<String, Clip[]> sounds = new HashMap<>();

	
	public HashMap<String, Integer> soundIndexes = new HashMap<>();
	public void addAnimation(String name,String address){
		ArrayList<Image> frames = new ArrayList<>();
		for(int i=0;;i++){
			Path path = Paths.get(address + name + "_" + String.format("%03d", i) + ".png");
			if(Files.exists(path)){
				frames.add(new ImageIcon(address + name + "_" + String.format("%03d", i) + ".png").getImage());
				continue;
			}
			break;
		}
		animations.put(name,frames);
		System.out.println("animation name: "+name+" address: "+address+" was loaded");
	}
	public void addGif(String name,String address){
		ArrayList<Image> frames = new ArrayList<>();
		for(int i=0;;i++){
			Path path = Paths.get(address + name + "_" + String.format("%03d", i)+"_delay-0.05s" + ".png");
			if(Files.exists(path)){
				frames.add(new ImageIcon(address + name + "_" + String.format("%03d", i)+"_delay-0.05s" + ".png").getImage());
				continue;
			}
			break;
		}
		animations.put(name,frames);
		System.out.println("animation name: "+name+" address: "+address+" was loaded");
	}
	public void addSound(String name, String address) {
		try {
			File file = new File(address + name + ".wav");
			Clip[] clipPool = new Clip[5]; 

			
			for (int i = 0; i < 5; i++) {
				AudioInputStream stream = AudioSystem.getAudioInputStream(file);
				Clip clip = AudioSystem.getClip();
				clip.open(stream);
				stream.close(); 
				clipPool[i] = clip;
			}

			
			sounds.put(name, clipPool);
			soundIndexes.put(name, 0);

			System.out.println("Sound pool created: " + name + " (5 clips loaded)");
		} catch (Exception e) {
			System.err.println("Failed to load sound pool: " + name);
			e.printStackTrace();
		}
	}

	

	public void paint(Graphics g, int x, int y, int width, int height, String name, int frame) {
		paint(g, x, y, width, height, name, frame, 255);
	}

	
	public void paintFlipped(Graphics g, int x, int y, int width, int height, String name, int frame) {
		paintFlipped(g, x, y, width, height, name, frame, 255);
	}

	
	public void paint(Graphics g, int x, int y, int width, int height, String name, int frame, int opacity) {
		Graphics2D g2D = (Graphics2D) g;
		Composite originalComposite = g2D.getComposite(); 

		try {
			
			float alpha = Math.max(0.0f, Math.min(1.0f, opacity / 255f));
			g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

			if (animations.containsKey(name) && !animations.get(name).isEmpty()) {
				int safeFrame = frame % animations.get(name).size();
				g2D.drawImage(animations.get(name).get(safeFrame), x, y, width, height, null);
			} else {
				
				g2D.setColor(Color.YELLOW);
				g2D.fillRect(x, y, width, height);
			}
		} finally {
			g2D.setComposite(originalComposite); 
		}
	}

	
	public void paintFlipped(Graphics g, int x, int y, int width, int height, String name, int frame, int opacity) {
		Graphics2D g2D = (Graphics2D) g;
		Composite originalComposite = g2D.getComposite(); 

		try {
			float alpha = Math.max(0.0f, Math.min(1.0f, opacity / 255f));
			g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

			if (animations.containsKey(name) && !animations.get(name).isEmpty()) {
				int safeFrame = frame % animations.get(name).size();
				Image img = animations.get(name).get(safeFrame);

				g2D.drawImage(img,
						x + width, y,           
						x, y + height,          
						0, 0,                   
						img.getWidth(null),     
						img.getHeight(null),    
						null
				);
			} else {
				g2D.setColor(Color.YELLOW);
				g2D.fillRect(x, y, width, height);
			}
		} finally {
			g2D.setComposite(originalComposite); 
		}
	}
	public int getFrameCount(String name) {
		return animations.get(name).size();
	}
	public void playSound(String name) {
		Clip[] clipPool = sounds.get(name);

		
		if (clipPool == null) {
			System.err.println("Attempted to play unloaded sound: " + name);
			return;
		}

		
		int currentIndex = soundIndexes.get(name);

		
		Clip clip = clipPool[currentIndex];

		
		clip.stop();
		clip.setFramePosition(0);

		
		if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float soundPercent = GeneralSave.sfxVolume;

			float volumePercentage = Math.max(0.0f, Math.min(1.0f, soundPercent));

			if (volumePercentage == 0.0f) {
				gainControl.setValue(gainControl.getMinimum());
			} else {
				float dB = (float) (20.0 * Math.log10(volumePercentage));
				dB = Math.min(dB, gainControl.getMaximum());
				gainControl.setValue(dB);
			}
		}

		
		clip.start();

		
		soundIndexes.put(name, (currentIndex + 1) % 5);
	}

}
