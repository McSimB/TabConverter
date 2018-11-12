package org.herac.tuxguitar.song.models;

import org.herac.tuxguitar.song.factory.TGFactory;

public abstract class TGMeasure {

	public static final int MAX_VOICES = 2;

	public static final int CLEF_TREBLE = 1;
	public static final int CLEF_BASS = 2;
	public static final int CLEF_TENOR = 3;
	public static final int CLEF_ALTO = 4;

	public static final int DEFAULT_CLEF = CLEF_TREBLE;
	public static final int DEFAULT_KEY_SIGNATURE = 0;

	private TGMeasureHeader header;
	private TGTrack track;
	private int clef;
	private int keySignature;
	private TGVoice[] voices;

	public TGMeasure(TGFactory factory, TGMeasureHeader header) {
		this.header = header;
		this.clef = DEFAULT_CLEF;
		this.keySignature = DEFAULT_KEY_SIGNATURE;
		this.voices = new TGVoice[MAX_VOICES];
		for (int i = 0; i < MAX_VOICES; i++) {
			this.setVoice(i, factory.newVoice(i));
		}
	}

	public void setVoice(int index, TGVoice voice) {
			this.voices[index] = voice;
			this.voices[index].setMeasure(this);
	}

	public TGTrack getTrack() {
		return this.track;
	}

	public void setTrack(TGTrack track) {
		this.track = track;
	}

	public int getClef() {
		return this.clef;
	}

	public void setClef(int clef) {
		this.clef = clef;
	}

	public int getKeySignature() {
		return this.keySignature;
	}

	public void setKeySignature(int keySignature) {
		this.keySignature = keySignature;
	}

	public TGMeasureHeader getHeader() {
		return this.header;
	}

	public void setHeader(TGMeasureHeader header) {
		this.header = header;
	}

	public int getNumber() {
		return this.header.getNumber();
	}

	public int getRepeatClose() {
		return this.header.getRepeatClose();
	}

	public long getStart() {
		return this.header.getStart();
	}

	public TGTempo getTempo() {
		return this.header.getTempo();
	}

	public TGTimeSignature getTimeSignature() {
		return this.header.getTimeSignature();
	}

	public boolean isRepeatOpen() {
		return this.header.isRepeatOpen();
	}

	public int getTripletFeel() {
		return this.header.getTripletFeel();
	}

	public long getLength() {
		return this.header.getLength();
	}

	public TGMarker getMarker() {
		return this.header.getMarker();
	}

	public boolean hasMarker() {
		return this.header.hasMarker();
	}

	public TGVoice[] getVoices() {
		return voices;
	}

	public TGVoice getVoice(int index) {
		if (index >= 0 && index < this.voices.length) {
			return this.voices[index];
		}
		return null;
	}

	public int countVoices() {
		return this.voices.length;
	}
}
