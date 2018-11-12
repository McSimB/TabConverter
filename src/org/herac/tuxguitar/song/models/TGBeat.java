package org.herac.tuxguitar.song.models;

import org.herac.tuxguitar.song.factory.TGFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class TGBeat {

	public static final int BEAT_EMPTY = 0x0;
	public static final int BEAT_NORMAL = 0x1;
	public static final int BEAT_REST = 0x2;

	private int type;
	private long start;
	private TGDuration duration;
	private TGVoice voice;
	private TGChord chord;
	private TGText text;
	private List<TGNote> notes;
	private TGBeatEffect effect;

	public TGBeat(TGFactory factory) {
		this.duration = factory.newDuration();
		this.notes = new ArrayList<TGNote>();
		this.effect = factory.newBeatEffect();
		this.effect.setStroke(factory.newStroke());
		this.start = TGDuration.QUARTER_TIME;
	}

	public long getStart() {
		return this.start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public TGDuration getDuration() {
		return this.duration;
	}

	public void setDuration(TGDuration duration) {
		this.duration = duration;
	}

	public TGChord getChord() {
		return this.chord;
	}

	public void setChord(TGChord chord) {
		this.chord = chord;
		this.chord.setBeat(this);
	}

	public TGText getText() {
		return this.text;
	}

	public void setText(TGText text) {
		this.text = text;
		this.text.setBeat(this);
	}

	public boolean isChordBeat() {
		return (this.chord != null);
	}

	public boolean isTextBeat() {
		return (this.text != null);
	}

	public boolean isRest() {
		return this.type == BEAT_REST;
	}

	public boolean isEmpty() {
		return this.type == BEAT_EMPTY;
	}

	public TGVoice getVoice() {
		return this.voice;
	}

	public void setVoice(TGVoice voice) {
		this.voice = voice;
	}

	public TGBeatEffect getEffect() {
		return effect;
	}

	public void setEffect(TGBeatEffect effect) {
		this.effect = effect;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<TGNote> getNotes() {
		return this.notes;
	}

	public TGNote getNote(int index) {
		if (index >= 0 && index < countNotes()) {
			return this.notes.get(index);
		}
		return null;
	}

	public int countNotes() {
		return this.notes.size();
	}

	public void addNote(TGNote note) {
		note.setBeat(this);
		this.notes.add(note);
	}
}