package Model.Game;

import javax.sound.sampled.Clip;

public class Message {
	public String text;
	public Clip sound;
	public boolean loop;

	public Message(String text, Clip sound, boolean loop) {
		this.text = text;
		this.sound = sound;
		this.loop = loop;
	}
}