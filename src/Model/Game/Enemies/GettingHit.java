package Model.Game.Enemies;

import Model.Game.Vector2D;

public interface GettingHit {
	void takeDamage(int damage, Vector2D knockback);
	void doDamage();
}