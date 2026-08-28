package Controller;

import Model.Game.Room;
import java.util.ArrayList;

public class SaveFile {

	public enum Charm {
		SOUL_CATCHER("Soul Catcher", "Gather more SOUL when striking enemies with the nail."),
		DASH_MASTER("Dash Master", "The bearer will be able to dash more often."),
		UNBREAKABLE_STRENGTH("Unbreakable Strength", "Greatly increases the damage of the nail."),
		QUICK_SLASH("Quick Slash", "Allows the bearer to strike much more rapidly."),
		QUICK_FOCUS("Quick Focus", "Increases the speed of focusing SOUL, allowing faster healing."),
		HEAVY_BLOW("Heavy Blow", "Increases the force of the nail, causing enemies to recoil further."),
		SHARP_SHADOW("Sharp Shadow", "Transform your shadow into a deadly weapon when dashing."),
		VOID_HEART("Void Heart", "Unifies the void under the bearer's will.");

		private final String displayName;
		private final String description;

		Charm(String displayName, String description) {
			this.displayName = displayName;
			this.description = description;
		}

		public String getDisplayName() { return this.displayName; }
		public String getDescription() { return this.description; }

		@Override
		public String toString() {
			return this.displayName;
		}
	}

	private Room room;
	public boolean killedBoss = false, killedHunter = false, killedCrawler = false,
			killedLaser = false, killedHorn = false, killedMoss = false, killedFly = false, voidHeart = false;
	public int time=0;
	public int id;
	public int totalEnemyKilled=0;
	public int deathCount=0;
	public String nameOfLevel="Green Path";

	public ArrayList<Charm> currentCharms = new ArrayList<>();
	public SaveFile(int id,Room room){
		this.room=room;
		this.id=id;
	}
	public SaveFile(int id){
		this.id=id;
	}
}