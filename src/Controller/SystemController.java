package Controller;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class SystemController {

	public enum GameState {
		MAIN_MENU, PLAYING, PAUSED, INVENTORY,
		SETTINGS, START_GAME, GUIDE, ACHIEVEMENTS, REMAPPING,VICTORY
	}
	public static GameState currentState = GameState.MAIN_MENU;



	public static GameState getCurrentState() { return currentState; }
	public static void setCurrentState(GameState state) { currentState = state; }

}