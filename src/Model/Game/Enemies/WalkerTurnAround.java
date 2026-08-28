package Model.Game.Enemies;

import Model.Game.Vector2D;

public class WalkerTurnAround {
	public Vector2D position;
	public double width;
	public double height;
	public boolean right=false;
	public WalkerTurnAround(double x, double y, double width, double height) {
		this.position = new Vector2D(x, y);
		this.width = width;
		this.height = height;
	}
	public WalkerTurnAround(double x, double y, double width, double height,boolean right) {
		this.position = new Vector2D(x, y);
		this.width = width;
		this.height = height;
		this.right=right;
	}
	
	public boolean intersects(Vector2D entityPos, double entityWidth, double entityHeight) {
		return (entityPos.x < this.position.x + this.width &&
				entityPos.x + entityWidth > this.position.x &&
				entityPos.y < this.position.y + this.height &&
				entityPos.y + entityHeight > this.position.y);
	}
}