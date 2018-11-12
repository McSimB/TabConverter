package org.herac.tuxguitar.song.models;

import org.herac.tuxguitar.song.factory.TGFactory;

public abstract class TGNote {

	private TGBeat beat;
	private int value;
	private int velocity;
	private int string;
	private boolean tiedNote;
	private TGNoteEffect effect;

	public TGNote(TGFactory factory) {
		this.value = 0;
		this.velocity = TGVelocities.DEFAULT;
		this.string = 1;
		this.tiedNote = false;
		this.effect = factory.newNoteEffect();
	}

	public TGBeat getBeat() {
		return this.beat;
	}

	public void setBeat(TGBeat beat) {
		this.beat = beat;
	}

	public int getValue() {
		return this.value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getVelocity() {
		return this.velocity;
	}

	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	public int getString() {
		return this.string;
	}

	public void setString(int string) {
		this.string = string;
	}

	public boolean isTiedNote() {
		return this.tiedNote;
	}

	public void setTiedNote(boolean tiedNote) {
		this.tiedNote = tiedNote;
	}

	public TGNoteEffect getEffect() {
		return this.effect;
	}

	public void setEffect(TGNoteEffect effect) {
		this.effect = effect;
	}
}