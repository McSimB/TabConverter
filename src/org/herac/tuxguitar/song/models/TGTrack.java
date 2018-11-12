package org.herac.tuxguitar.song.models;

import org.herac.tuxguitar.song.factory.TGFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class TGTrack {

	public static final int MAX_STRINGS = 25;
	public static final int MIN_STRINGS = 1;
	public static final int MAX_OFFSET = 24;
	public static final int MIN_OFFSET = -24;

	private int number;
	private int offset;
	private int channelId;
	private boolean solo;
	private boolean mute;
	private String name;
	private List<TGMeasure> measures;
	private List<TGString> strings;
	private TGColor color;
	private TGLyric lyrics;
	private TGSong song;

	public TGTrack(TGFactory factory) {
		this.number = 0;
		this.offset = 0;
		this.channelId = -1;
		this.solo = false;
		this.mute = false;
		this.name = "";
		this.measures = new ArrayList<TGMeasure>();
		this.strings = new ArrayList<TGString>();
		this.color = factory.newColor();
		this.lyrics = factory.newLyric();
	}

	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<TGMeasure> getMeasures() {
		return this.measures;
	}

	public void addMeasure(TGMeasure measure) {
		measure.setTrack(this);
		this.measures.add(measure);
	}

	public TGMeasure getMeasure(int index) {
		if (index >= 0 && index < countMeasures()) {
			return this.measures.get(index);
		}
		return null;
	}

	public int countMeasures() {
		return this.measures.size();
	}

	public List<TGString> getStrings() {
		return this.strings;
	}

	public void setStrings(List<TGString> strings) {
		this.strings = strings;
	}

	public TGColor getColor() {
		return this.color;
	}

	public void setColor(TGColor color) {
		this.color = color;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOffset() {
		return this.offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public boolean isSolo() {
		return this.solo;
	}

	public void setSolo(boolean solo) {
		this.solo = solo;
	}

	public boolean isMute() {
		return this.mute;
	}

	public void setMute(boolean mute) {
		this.mute = mute;
	}

	public int getChannelId() {
		return this.channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public TGLyric getLyrics() {
		return this.lyrics;
	}

	public void setLyrics(TGLyric lyrics) {
		this.lyrics = lyrics;
	}

	public TGString getString(int number) {
		return this.strings.get(number - 1);
	}

	public int stringCount() {
		return this.strings.size();
	}

	public TGSong getSong() {
		return this.song;
	}

	public void setSong(TGSong song) {
		this.song = song;
	}
}
