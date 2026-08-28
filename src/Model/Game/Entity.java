package Model.Game;

import Model.Game.Knight.Knight;

import java.awt.*;

public abstract class Entity {
	protected Vector2D position;
	protected Vector2D speed=new Vector2D(0,0);
	protected double width;
	protected double height;
	protected int hp;
	protected int curHp;
	protected int frame=0;
	protected String state ="Run";
	protected boolean isFlipped=false;
	protected Room room;
	protected boolean onSteepSlope = false;
	protected Vector2D lastSlopeNormal = new Vector2D(0, 0);
	protected int hold=(int)Units.HOLD.number;

	public Entity(double x, double y) {
		position=new Vector2D(x,y);
		speed=new Vector2D(0,0);
	}

	public int getFrame() {
		return frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public boolean isFlipped() {
		return isFlipped;
	}

	public void setFlipped(boolean flipped) {
		isFlipped = flipped;
	}

	public Entity(double x, double y,Room room) {
		position=new Vector2D(x,y);
		speed=new Vector2D(0,0);
		this.room=room;
	}

	public void tick() {
		move();
	}

	public Vector2D getPosition() {
		return position;
	}

	public Vector2D getSpeed() {
		return speed;
	}

	abstract public void paint(Graphics g);

	public void move() {
		
		onSteepSlope = false;
		if (this instanceof Knight) {
			((Knight) this).setOnGround(false);
			((Knight) this).setIsTouchingWall(0);
		}
		Vector2D step = new Vector2D(speed.x, speed.y);
		step.multiply(Units.TICK.number);
		position.add(step);

		for (Line line : room.getBoundaries()) {
			if (line.intersect(position, width, height)) {
				Vector2D normal = line.normalRight();
				double penetration = getPenetrationDepth(line);

				
				if (penetration > 0 && Math.abs(normal.y) < 0.5) {
					double minY = Math.min(line.y1, line.y2);
					double maxY = Math.max(line.y1, line.y2);
					double entityTop = position.y;
					double entityBottom = position.y + height;

					double overlapY = Math.min(entityBottom - minY, maxY - entityTop);

					if (overlapY > 0 && overlapY < 4.0 && overlapY < penetration) {
						continue;
					}
				}
				

				if (penetration > 0) {
					Vector2D pushVector = new Vector2D(normal.x, normal.y);
					if(normal.y<=-0.9848)
						pushVector=new Vector2D(0,-1);
					pushVector.multiply(penetration);
					position.add(pushVector); 

					
					if(normal.y==0 && this.getClass()== Knight.class){
						((Knight) this).resetDoubleJump();
						((Knight) this).resetJump();
						((Knight) this).resetDash();
						((Knight) this).setIsTouchingWall(normal.x>0 ? -1 : 1);
					}
					if (normal.y <= -0.9848) {
						if (this.getClass() == Knight.class) {
							((Knight) this).setOnGround(true);
							((Knight) this).resetDash();
							((Knight) this).resetJump();
							((Knight) this).resetDoubleJump();
						}
					}
					else if (normal.y > -0.9848 && normal.y < -0.05) {
						onSteepSlope = true;
						lastSlopeNormal = normal;

						Vector2D tangent = new Vector2D(-normal.y, +normal.x);
						if (tangent.y < 0) {
							tangent.x = -tangent.x;
							tangent.y = -tangent.y;
						}

						double slideVelocity = 40*Units.LENGTH.number;
						speed.x = tangent.x * slideVelocity;
						speed.y = tangent.y * slideVelocity;
					}
					else if (normal.y < -0.9 && this.getClass() == Knight.class) {
						((Knight) this).resetDash();
						((Knight) this).resetJump();
						((Knight) this).resetDoubleJump();
					}
				}

				
				double dotSpeed = speed.dot(normal);
				if(normal.y<=-0.9848)
					dotSpeed=speed.dot(new Vector2D(0,-1));
				if (dotSpeed < 0) {
					Vector2D projection = new Vector2D(normal.x, normal.y);
					projection.multiply(dotSpeed);
					speed.x -= projection.x;
					speed.y -= projection.y;
				}
			}
		}

		
		
		if (this instanceof Knight && room != null && room.getEntities() != null) {
			for (Entity e : room.getEntities()) {
				if (e instanceof SecretDoor) {
					
					if (position.x < e.position.x + e.width &&
							position.x + width > e.position.x &&
							position.y < e.position.y + e.height &&
							position.y + height > e.position.y) {

						
						double overlapLeft = (position.x + width) - e.position.x;
						double overlapRight = (e.position.x + e.width) - position.x;
						double overlapTop = (position.y + height) - e.position.y;
						double overlapBottom = (e.position.y + e.height) - position.y;

						
						double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
								Math.min(overlapTop, overlapBottom));

						
						if (minOverlap == overlapLeft) {
							position.x -= overlapLeft;
							if (speed.x > 0) speed.x = 0;
							((Knight) this).setIsTouchingWall(1);
						} else if (minOverlap == overlapRight) {
							position.x += overlapRight;
							if (speed.x < 0) speed.x = 0;
							((Knight) this).setIsTouchingWall(-1);
						} else if (minOverlap == overlapTop) {
							position.y -= overlapTop;
							if (speed.y > 0) speed.y = 0;
							
							((Knight) this).setOnGround(true);
							((Knight) this).resetDash();
							((Knight) this).resetJump();
							((Knight) this).resetDoubleJump();
						} else if (minOverlap == overlapBottom) {
							position.y += overlapBottom;
							if (speed.y < 0) speed.y = 0;
						}
					}
				}
			}
		}
		
	}

	protected double getPenetrationDepth(Line line) {
		Vector2D[] corners = {
				new Vector2D(position.x, position.y),
				new Vector2D(position.x + width, position.y),
				new Vector2D(position.x, position.y + height),
				new Vector2D(position.x + width, position.y + height)
		};

		Vector2D linePoint = new Vector2D(line.x1, line.y1);
		Vector2D normal = line.normalRight();
		double maxPenetration = 0;

		for (Vector2D corner : corners) {
			Vector2D toCorner = new Vector2D(corner.x - linePoint.x, corner.y - linePoint.y);
			double distance = toCorner.dot(normal);
			if (distance < 0 && Math.abs(distance) > maxPenetration) {
				maxPenetration = Math.abs(distance);
			}
		}
		return maxPenetration > 0 ? maxPenetration + 0.01 : 0;
	}

	public abstract void movements();

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public int getCurHp() {
		return curHp;
	}

	public void setCurHp(int curHp) {
		this.curHp = curHp;
	}
}