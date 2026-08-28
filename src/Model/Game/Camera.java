package Model.Game;

import java.awt.*;
import java.util.ArrayList;

public class Camera extends Entity {
	private Entity target;
	private ArrayList<CameraBoundingBox> boundsList = new ArrayList<>();
	private CameraBoundingBox currentBox = null;
	private double viewportWidth;
	private double viewportHeight;
	private boolean isFirstFrame = true;
	private int shakeRemaining = 0;
	private int shakeIntensity = 0;
	private double currentShakeX = 0;
	private double currentShakeY = 0;

	public Camera(double x, double y, double viewportWidth, double viewportHeight) {
		super(x, y);
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		this.width = viewportWidth;
		this.height = viewportHeight;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public void addBound(CameraBoundingBox box) {
		this.boundsList.add(box);
	}

	public ArrayList<CameraBoundingBox> getBoundsList() {
		return boundsList;
	}

	
	public void triggerShake(int frames, int intensity) {
		this.shakeRemaining = frames;
		this.shakeIntensity = intensity;
	}

	@Override
	public void movements() {
		if (target != null) {
			
			double desiredX = target.getPosition().x + (target.getWidth() / 2) - (viewportWidth / 2);
			double desiredY = target.getPosition().y + (target.getHeight() / 2) - (viewportHeight / 2);

			
			CameraBoundingBox bestBox = null;
			double minPenalty = Double.MAX_VALUE;

			for (CameraBoundingBox box : boundsList) {
				if (box.active && isTargetInBox(target, box)) {
					double clampedX = calculateClampedX(desiredX, box);
					double clampedY = calculateClampedY(desiredY, box);
					double penalty = Math.pow(desiredX - clampedX, 2) + Math.pow(desiredY - clampedY, 2);

					if (penalty < minPenalty) {
						minPenalty = penalty;
						bestBox = box;
					}
				}
			}

			
			if (bestBox != null) {
				currentBox = bestBox;
			} else if (currentBox == null && !boundsList.isEmpty()) {
				for (CameraBoundingBox box : boundsList) {
					if (box.active) {
						currentBox = box;
						break;
					}
				}
			} else if (currentBox != null && !currentBox.active) {
				currentBox = null;
			}

			
			double targetCamX = desiredX;
			double targetCamY = desiredY;

			if (currentBox != null) {
				targetCamX = calculateClampedX(desiredX, currentBox);
				targetCamY = calculateClampedY(desiredY, currentBox);
			}

			
			if (isFirstFrame) {
				position.x = targetCamX;
				position.y = targetCamY;
				isFirstFrame = false;
			} else {
				position.x += (targetCamX - position.x) * 0.15;
				position.y += (targetCamY - position.y) * 0.15;
			}
		}

		
		if (shakeRemaining > 0) {
			
			currentShakeX = (Math.random() - 0.5) * 2 * shakeIntensity;
			currentShakeY = (Math.random() - 0.5) * 2 * shakeIntensity;
			shakeRemaining--;
		} else {
			
			currentShakeX = 0;
			currentShakeY = 0;
		}
	}

	private boolean isTargetInBox(Entity t, CameraBoundingBox b) {
		double txCenter = t.getPosition().x + t.getWidth() / 2;
		double tyCenter = t.getPosition().y + t.getHeight() / 2;

		return txCenter >= b.getPosition().x &&
				txCenter <= b.getPosition().x + b.getWidth() &&
				tyCenter >= b.getPosition().y &&
				tyCenter <= b.getPosition().y + b.getHeight();
	}

	private double calculateClampedX(double desiredX, CameraBoundingBox bounds) {
		double minX = bounds.getPosition().x;
		double maxX = bounds.getPosition().x + bounds.getWidth() - viewportWidth;
		if (maxX < minX) maxX = minX;
		return Math.max(minX, Math.min(desiredX, maxX));
	}

	private double calculateClampedY(double desiredY, CameraBoundingBox bounds) {
		double minY = bounds.getPosition().y;
		double maxY = bounds.getPosition().y + bounds.getHeight() - viewportHeight;
		if (maxY < minY) maxY = minY;
		return Math.max(minY, Math.min(desiredY, maxY));
	}

	@Override
	public void paint(Graphics g) {
		
	}
	public void setBoundsList(ArrayList<CameraBoundingBox> boundsList) {
		this.boundsList = boundsList;
	}

	public int getOffsetX() { return (int) (position.x + currentShakeX); }
	public int getOffsetY() { return (int) (position.y + currentShakeY); }
}