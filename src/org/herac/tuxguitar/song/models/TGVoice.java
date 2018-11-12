package org.herac.tuxguitar.song.models;

import java.util.ArrayList;
import java.util.List;

public abstract class TGVoice {

	public static final int DIRECTION_NONE = 0;
	public static final int DIRECTION_UP = 1;
	public static final int DIRECTION_DOWN = 2;

	private TGMeasure measure;
	private List<TGBeat> beats;
	private int direction;
	private int index;

	public TGVoice(int index) {
		this.index = index;
		this.beats = new ArrayList<TGBeat>();
		this.direction = DIRECTION_NONE;
	}

	public int getIndex() {
		return index;
	}

	public int getDirection() {
		return this.direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public TGBeat getBeat(int index) {
		if (index >= 0 && index < countBeats()) {
			return this.beats.get(index);
		}
		return null;
	}

	public int countBeats() {
		return this.beats.size();
	}

	public boolean isEmpty() {
		return this.beats.isEmpty();
	}

	public void addBeat(TGBeat beat) {
		beat.setVoice(this);
		this.beats.add(beat);
	}

	public List<TGBeat> getBeats() {
		return beats;
	}

	public TGMeasure getMeasure() {
		return measure;
	}

	public void setMeasure(TGMeasure measure) {
		this.measure = measure;
	}
}
