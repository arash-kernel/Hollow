package Model.Game;

public class Vector2D {
	public double x, y;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void add(Vector2D other) {
		this.x += other.x;
		this.y += other.y;
	}

	public void multiply(double scalar) {
		this.x *= scalar;
		this.y *= scalar;
	}

	public double getLength() {
		return Math.sqrt(x * x + y * y);
	}
	public int getIntX(){
		return (int)x;
	}
	public int getIntY(){
		return (int)y;
	}

	public void normal(){
		double len=Math.sqrt(x*x+y*y);
		if(len==0)
			return;
		x/=len;
		y/=len;
	}
	public double dot(Vector2D other) {
		return this.x * other.x + this.y * other.y;
	}

	public static Vector2D rotateTowards(Vector2D A, Vector2D B, double degrees) {
		double radiansToRotate = Math.toRadians(degrees);

		
		double dot = A.x * B.x + A.y * B.y;
		double cross = A.x * B.y - A.y * B.x; 

		
		double angleBetween = Math.atan2(cross, dot);

		double magA = Math.sqrt(A.x * A.x + A.y * A.y);

		
		if (Math.abs(angleBetween) <= radiansToRotate) {
			
			double magB = Math.sqrt(B.x * B.x + B.y * B.y);
			if (magB < 1e-9) return new Vector2D(A.x, A.y); 

			
			return new Vector2D((B.x / magB) * magA, (B.y / magB) * magA);
		}

		
		
		double actualRotation = Math.copySign(radiansToRotate, angleBetween);

		
		double cos = Math.cos(actualRotation);
		double sin = Math.sin(actualRotation);

		double newX = A.x * cos - A.y * sin;
		double newY = A.x * sin + A.y * cos;

		return new Vector2D(newX, newY);
	}
}