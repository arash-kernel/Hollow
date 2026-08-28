package Model.Game;

public class Line {
	public Double x1, x2, y1, y2;

	public Vector2D normalRight(){
		Vector2D ans = new Vector2D(y2-y1,x1-x2);
		ans.normal();
		return ans;
	}


	public boolean isFloor() {
		if(normalRight().getY()<=-0.9848)
			return true;
		return false;
	}

	public boolean intersect(Vector2D position, double width, double height) {
		double rectX= position.x;
		double rectY= position.y;
		double rectX2 = rectX + width;
		double rectY2 = rectY + height;

		if ((x1 >= rectX && x1 <= rectX2 && y1 >= rectY && y1 <= rectY2) ||
				(x2 >= rectX && x2 <= rectX2 && y2 >= rectY && y2 <= rectY2)) {
			return true;
		}
		return lineIntersectsSegment(x1, y1, x2, y2, rectX, rectY, rectX2, rectY) ||
				lineIntersectsSegment(x1, y1, x2, y2, rectX2, rectY, rectX2, rectY2) ||
				lineIntersectsSegment(x1, y1, x2, y2, rectX2, rectY2, rectX, rectY2) ||
				lineIntersectsSegment(x1, y1, x2, y2, rectX, rectY2, rectX, rectY);
	}

	private boolean lineIntersectsSegment(double x1, double y1, double x2, double y2,
										  double x3, double y3, double x4, double y4) {
		double den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (den == 0) return false;
		double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
		double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / den;
		return (t >= 0 && t <= 1) && (u >= 0 && u <= 1);
	}
}