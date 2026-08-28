import Controller.SaveFile;
import Controller.SaveManager;
import Model.Game.*;
import Model.Game.Enemies.*;
import Model.Game.FalseKnight.FalseKnight;
import Model.Game.Hazards.Hazard;
import Model.Game.Knight.Knight;
import Model.Game.ZoteBote.ZoteBote;
import View.MyFrame;
import View.Ui;

import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		SaveManager.loadGlobalSettings();
		// 1. Spawning the Knight safely in the middle-left of the room
		Knight knight = new Knight(500, 200);
		ArrayList<Line> boundaries = new ArrayList<>();

		// =================================================================
		// BOUNDARIES: SINGLE SCREEN ROOM (1920 x 880)
		// (Winding Order Context Applied)
		// =================================================================

		// Outer Perimeter Walls
		boundaries.add(createLine(0.0, 0.0, 0.0, 880.0));       // Left Wall (Normal Right)
		boundaries.add(createLine(1540.0, 880.0, 1540.0, 0.0));  // Right Wall (Normal Left)
		boundaries.add(createLine(1920.0, 0.0, 0.0, 0.0));       // Ceiling (Normal Down)
		boundaries.add(createLine(0.0, 880.0, 3020.0, 880.0));   // Floor (Normal Up)

		// Interior Platforms for jumping/navigating
		//boundaries.add(createLine(300.0, 700.0, 700.0, 700.0));    // Low platform left
		boundaries.add(createLine(900.0, 550.0, 1400.0, 550.0));   // Elevated platform middle
		boundaries.add(createLine(300.0, 400.0, 700.0, 400.0));    // High ledge left

		// =================================================================
		// CAMERA & CAMERA BOUNDING BOX INITIALIZATION
		// =================================================================

		// Viewport setup matching your (1920x880) dimensions
		Camera camera = new Camera(0, 0, 1920, 880);
		camera.setTarget(knight);

		// Single bounding box pinning the camera explicitly to the room size
		CameraBoundingBox roomBounds = new CameraBoundingBox(0, 0, 3000, 880);
		camera.addBound(roomBounds);

		// =================================================================
		// LEVEL RUNTIME INTEGRATION
		// =======================================================ad==========

		Room room = new Room(knight, boundaries,new SaveFile(1));
		Ui ui=new Ui(room);
		MyFrame myframe = new MyFrame(room,ui);

		// Link the camera to the view panel
		myframe.onlyPanel.camera = camera;

		// =================================================================
		// ENTITY SPAWNING
		// =================================================================

//		MossFly fly = new MossFly(500, 300, room);
//		MossCreep creep = new MossCreep(400, 830, room);
		CrystalCrawler creeper = new CrystalCrawler(950, 500, room);
		CrystalHunter hunt = new CrystalHunter(1500, 300, room);
//		Crystallized boi = new Crystallized(500, 800, room);
//		HornedHusk horn = new HornedHusk(750, 830, room);
		FalseKnight boss =new FalseKnight(750,750,room);

		ArrayList<ArrayList<String>> h = new ArrayList<>(List.of(
				new ArrayList<>(List.of("Welcome to the single-screen arena.", "Prepare for trouble.")),
				new ArrayList<>(List.of("The camera is locked to these bounds.", "No running away!"))
		));
		//ZoteBote zote = new ZoteBote(400, 700, room, h,true,true);
		//SecretDoor door=new SecretDoor(0,700,room);
		Hazard hazard = new Hazard(700,700,room,true);
		// Walker Turnaround limits to keep entities pacing along the floors
		room.getTurnArounds().add(new WalkerTurnAround(50, 830, 20, 50, true));
		room.getTurnArounds().add(new WalkerTurnAround(1850, 830, 20, 50, false));
		room.panel=myframe.onlyPanel;
		// Core Game Loop
		while (true) {
			myframe.onlyPanel.repaint();

			try {
				Thread.sleep((int)(Units.TICK.number * 1000));
			} catch (Exception e) {
				System.out.println("Loop exception: " + e.getMessage());
			}
		}
	}

	private static Line createLine(double x1, double y1, double x2, double y2) {
		Line l = new Line();
		l.x1 = x1;
		l.y1 = y1;
		l.x2 = x2;
		l.y2 = y2;
		return l;
	}
}