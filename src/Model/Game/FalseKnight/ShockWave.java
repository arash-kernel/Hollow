package Model.Game.FalseKnight;

import Model.Game.*;
import Model.Game.Enemies.GettingHit;
import Model.Game.Knight.Knight;
import View.Animations;

import java.awt.*;

public class ShockWave extends Entity implements GettingHit {
	private static Animations animations = new Animations();
	private int ticker=0;
	private boolean dying=false;
	static {
		animations.addAnimation("ShockWave Spurt", "src/Model/Game/FalseKnight/ShockWaveAnimations/");
	}
	public ShockWave(double x, double y, Room room) {
		super(x, y, room);
		room.getEntities().add(this);
		width=50;
		height=130;
		hold=16;
		speed.x=2* Units.LENGTH.number * (isFlipped?1:-1);
		speed.y=0;

	}

	@Override
	public void paint(Graphics g) {
		double scale = this.width / 47;

		int paintW = (int) (88 * scale);
		int paintH = (int) (202 * scale);

		int paintX = (int) (position.x - 20);
		int paintY = (int) (position.y - 80);

		if (!isFlipped) {
			animations.paintFlipped(g, paintX, paintY, paintW, paintH, "ShockWave Spurt", frame);
		} else {
			animations.paint(g, paintX, paintY, paintW, paintH, "ShockWave Spurt", frame);
		}
	}

	@Override
	public void movements() {
		if(speed.getLength()<0.5*Units.LENGTH.number) {
			room.getEntities().remove(this);
			return;
		}
		speed.x=2* Units.LENGTH.number * (isFlipped?1:-1);
		ticker++;
		if(ticker>=hold){
			ticker=0;
			frame++;
			if(frame>2 && !dying){
				frame=2;
			}

		}
		if(dying&&frame<3){
			frame=3;
		}
		if(frame==3){
			speed.multiply(0.5);
		}
		if(frame==4){
			speed.multiply(0.25);
		}
		if(wallAhead()){
			dying=true;
		}
		if(frame==5){
			room.getEntities().remove(this);
		}
		doDamage();
	}

	@Override
	public void takeDamage(int damage, Vector2D knockback) {
		return;
	}

	@Override
	public void doDamage() {
		Knight knight = room.getKnight();
		if(frame!=3)
			return;
		
		if (knight == null || state.startsWith("Death") || hp <= 0) {
			return;
		}

		
		double eX = position.x + 2;
		double eY = position.y + 2;
		double eW = width - 4;
		double eH = height - 4;

		
		double kX = knight.getPosition().x + 5;
		double kY = knight.getPosition().y + 10;
		double kW = knight.getWidth() - 10;
		double kH = knight.getHeight() - 10;

		
		if (eX < kX + kW && eX + eW > kX && eY < kY + kH && eY + eH > kY) {
			knight.takeDamage(1);
		}
	}

	private boolean wallAhead() {
		double check = 200;
		double x = isFlipped ? position.x + width : position.x - check;
		for (Line line : room.getBoundaries()) {
			Vector2D normal = line.normalRight();

			if (Math.abs(normal.x) < 0.9) continue;
			if (isFlipped && normal.x >= 0) continue;
			if (!isFlipped && normal.x <= 0) continue;

			if (line.intersect(new Vector2D(x, position.y + 2), check, height - 4)) {
				return true;
			}
		}
		return false;
	}
}
