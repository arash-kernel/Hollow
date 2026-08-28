package Model.Game;

import java.awt.*;

public class CameraBoundingBox extends Entity {

	public boolean active=true;
	public CameraBoundingBox(double x, double y, double width, double height) {
		super(x, y);
		this.width = width;
		this.height = height;
	}

	@Override
	public void movements() {
		
	}

	@Override
	public void paint(Graphics g) {
		
        /*
        g.setColor(Color.MAGENTA);
        g.drawRect((int)position.x, (int)position.y, (int)width, (int)height);
        */
	}
}