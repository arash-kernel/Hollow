package Model.Game.Knight;

import Model.Game.Entity;
import Model.Game.Room;
import View.Animations;

import java.awt.*;

public class Blast	extends Entity{
	private static Animations animations=new Animations();
	static {
		String[] animNames = {
				"Blast"
		};
		for (String name : animNames) {
			animations.addAnimation(name, "src/Model/Game/Knight/BlastAnimations/");
		}
	}

	public Blast(double x, double y, Room room) {
		super(x, y,room);
		state="Blast";
		width=50;
		height=120;
	}
	int hold=0;
	@Override
	public void paint(Graphics g) {
		hold++;
		if(hold==4){
			frame++;
			hold=0;
		}
		if(frame==8) {
			room.getEntities().remove(this);
			return;
		}
		if(!isFlipped)
			animations.paint(g,position.getIntX(), position.getIntY(),(int)width,(int)height,state,frame);
		else
			animations.paintFlipped(g,position.getIntX(), position.getIntY(),(int)width,(int)height,state,frame);
	}

	@Override
	public void movements() {

	}
	@Override
	public void move(){

	}
}
