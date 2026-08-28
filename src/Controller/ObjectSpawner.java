package Model.Game;

import Model.Game.Enemies.*;
import Model.Game.FalseKnight.FalseKnight;
import Model.Game.Hazards.Hazard;
import Model.Game.Hazards.Lamp;
import Model.Game.Knight.Knight;
import Model.Game.ZoteBote.ZoteBote;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjectSpawner {

	private String command = null;
	private Room room;

	private static Clip[] preloadedClips = new Clip[6];


	static {
		try {
			preloadedClips[0] = loadClip("src/Model/Game/Zotebote/ZoteBoteAnimations/Sounds/Zote_00.wav");
			preloadedClips[1] = loadClip("src/Model/Game/Zotebote/ZoteBoteAnimations/Sounds/Zote_01.wav");
			preloadedClips[2] = loadClip("src/Model/Game/Zotebote/ZoteBoteAnimations/Sounds/Zote_02.wav");
			preloadedClips[3] = loadClip("src/Model/Game/Zotebote/ZoteBoteAnimations/Sounds/Zote_03.wav");
			preloadedClips[4] = loadClip("src/Model/Game/Zotebote/ZoteBoteAnimations/Sounds/Zote_04.wav");
			preloadedClips[5] = loadClip("src/Model/Game/Zotebote/ZoteBoteAnimations/Sounds/Zote_05.wav");
		} catch (Exception e) {
			System.out.println("Failed to preload ZoteBote clips: " + e.getMessage());
		}
	}
	public ObjectSpawner(String command, Room room) {
		this.command = command;
		this.room = room;
	}

	public void spawn() {
		if (command == null || command.trim().isEmpty()) {
			return;
		}

		String[] parts = command.trim().split("\\s+");
		if (parts.length < 3 && !parts[0].equals("Boundary")) {
			System.out.println("Invalid spawn command format.");
			return;
		}

		String type = parts[0];

		try {
			switch (type) {
				case "Knight":
					Knight knight = new Knight(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "MossFly":
					new MossFly(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "MossCreep":
					new MossCreep(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "CrystalCrawler":
					new CrystalCrawler(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "CrystalHunter":
					new CrystalHunter(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "Crystallized":
					new Crystallized(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "HornedHusk":
					new HornedHusk(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "FalseKnight":
					new FalseKnight(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "Hazard":
					if (parts.length >= 4) {
						boolean isThorn = Boolean.parseBoolean(parts[3]);
						new Hazard(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room, isThorn);
					} else {
						System.out.println("Hazard needs: X Y isThorn(true/false)");
					}
					break;
				case "SecretDoor":
					new SecretDoor(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "Lamp":
					new Lamp(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), room);
					break;
				case "Turn":
					if (parts.length >= 6) {
						int x = Integer.parseInt(parts[1]);
						int y = Integer.parseInt(parts[2]);
						int w = Integer.parseInt(parts[3]);
						int h = Integer.parseInt(parts[4]);
						boolean isRight = Boolean.parseBoolean(parts[5]);
						room.getTurnArounds().add(new WalkerTurnAround(x, y, w, h, isRight));
					} else {
						System.out.println("WalkerTurnAround needs: X Y Width Height isRight(true/false)");
					}
					break;
				case "Box":
					if (parts.length >= 5) {
						double x = Double.parseDouble(parts[1]);
						double y = Double.parseDouble(parts[2]);
						double w = Double.parseDouble(parts[3]);
						double h = Double.parseDouble(parts[4]);

						Line left = new Line();
						left.x1 = x; left.y1 = y + h;
						left.x2 = x; left.y2 = y;
						room.getBoundaries().add(left);

						Line right = new Line();
						right.x1 = x + w; right.y1 = y;
						right.x2 = x + w; right.y2 = y + h;
						room.getBoundaries().add(right);

						Line top = new Line();
						top.x1 = x; top.y1 = y;
						top.x2 = x + w; top.y2 = y;
						room.getBoundaries().add(top);

						Line bottom = new Line();
						bottom.x1 = x + w; bottom.y1 = y + h;
						bottom.x2 = x; bottom.y2 = y + h;
						room.getBoundaries().add(bottom);
					} else {
						System.out.println("Box needs: X Y Width Height");
					}
					break;
				case "Boundary":
					if (parts.length >= 5) {
						Line l = new Line();
						l.x1 = Double.parseDouble(parts[1]);
						l.y1 = Double.parseDouble(parts[2]);
						l.x2 = Double.parseDouble(parts[3]);
						l.y2 = Double.parseDouble(parts[4]);
						room.getBoundaries().add(l);
					} else {
						System.out.println("Boundary needs: X1 Y1 X2 Y2");
					}
					break;
				default:
					System.out.println("Unknown or restricted object type: " + type);
					break;
			}
		} catch (Exception e) {
			System.out.println("Error parsing command '" + command + "': " + e.getMessage());
		}
	}


	public static void spawnFromFile(String filePath, Room room) {
		boolean parsingZote = false;
		List<String> zoteLines = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
					continue;
				}

				if (line.equals("$")) {
					if (!parsingZote) {
						parsingZote = true;
						zoteLines.clear();
					} else {
						parsingZote = false;
						spawnZoteBote(zoteLines, room);
					}
					continue;
				}

				if (parsingZote) {
					zoteLines.add(line);
				} else {

					try {
						ObjectSpawner spawner = new ObjectSpawner(line, room);
						spawner.spawn();
					} catch (Exception e) {
						System.out.println("Failed to spawn object from line: '" + line + "'. Error: " + e.getMessage());
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading spawn file: " + e.getMessage());
		}
	}


	private static void spawnZoteBote(List<String> lines, Room room) {
		if (lines.size() < 1) return;

		try {
			String[] header = lines.get(0).trim().split("\\s+");
			double x = Double.parseDouble(header[0]);
			double y = Double.parseDouble(header[1]);
			boolean voidHeart = Boolean.parseBoolean(header[2]);
			boolean transfer = Boolean.parseBoolean(header[3]);

			ArrayList<ArrayList<Message>> dialogueSets = new ArrayList<>();
			ArrayList<Message> currentSet = null;

			for (int i = 1; i < lines.size(); i++) {
				String msgLine = lines.get(i).trim();

				if (msgLine.equals("&")) {
					if (currentSet != null && !currentSet.isEmpty()) {
						dialogueSets.add(currentSet);
					}
					currentSet = new ArrayList<>();
				} else {
					String[] msgParts = msgLine.split(" ", 2);

					if (msgParts.length == 2) {
						int clipIndex = Integer.parseInt(msgParts[0]);
						String text = msgParts[1];

						Clip soundClip = null;
						if (clipIndex >= 0 && clipIndex < preloadedClips.length) {
							soundClip = preloadedClips[clipIndex];
						}

						if (currentSet == null) {
							currentSet = new ArrayList<>();
						}

						currentSet.add(new Message(text, soundClip, false));
					}
				}
			}

			if (currentSet != null && !currentSet.isEmpty()) {
				dialogueSets.add(currentSet);
			}

			new ZoteBote(x, y, room, dialogueSets, voidHeart, transfer);

		} catch (Exception e) {
			System.out.println("Error spawning ZoteBote from block: " + e.getMessage());
		}
	}


	private static Clip loadClip(String path) {
		try {
			File audioFile = new File(path);
			if (audioFile.exists()) {
				Clip clip = AudioSystem.getClip();
				clip.open(AudioSystem.getAudioInputStream(audioFile));
				return clip;
			}
		} catch (Exception e) {
			System.out.println("Error loading clip at " + path);
		}
		return null;
	}
}