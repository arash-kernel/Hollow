package Model.Game.Hazards;

import Model.Game.Enemies.GettingHit;
import Model.Game.Entity;
import Model.Game.Room;
import Model.Game.Vector2D;
import Model.Game.Knight.Knight; 
import View.Animations;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Hazard extends Entity implements GettingHit {

	private static Animations animations = new Animations();

	static {
		animations.addAnimation("Thorn", "src/Model/Game/Hazards/HazardAnimations/");
		animations.addAnimation("Crystal", "src/Model/Game/Hazards/HazardAnimations/");
	}
	public Hazard(double x, double y, Room room,boolean Thorn) {
		super(x, y, room);
		room.getEntities().add(this);
		width=332;
		height=100;
		state="Crystal";
		if(Thorn)
			state="Thorn";
	}

	@Override
	public void takeDamage(int damage, Vector2D knockback) {
		

	}

	@Override
	public void doDamage() {
		Knight knight = room.getKnight();

		
		if (knight == null) {
			return;
		}

		
		double hX = position.x;
		double hY = position.y;
		double hW = width;
		double hH = height;

		
		double kX = knight.getPosition().x + 5;
		double kY = knight.getPosition().y + 10;
		double kW = knight.getWidth() - 10;
		double kH = knight.getHeight() - 10;

		
		if (hX < kX + kW && hX + hW > kX && hY < kY + kH && hY + hH > kY) {
			knight.takeDamageHazard(1);
		}
	}

	@Override
	public void paint(Graphics g) {
		animations.paint(g, position.getIntX(), position.getIntY(),(int)width,(int)height,state,0);
	}

	@Override
	public void movements() {
		
		doDamage();
	}

	@Override
	public void move(){
		return;
	}
}