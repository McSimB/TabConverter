package org.herac.tuxguitar.io.midi.base;

import org.herac.tuxguitar.io.midi.MidiMessageUtils;

import java.util.ArrayList;
import java.util.List;

public class MidiSequence {
	
	public static final float PPQ = 0.0f;
	public static final float SMPTE_24 = 24.0f;
	public static final float SMPTE_25 = 25.0f;
	public static final float SMPTE_30DROP = 29.97f;
	public static final float SMPTE_30 = 30.0f;
	
	protected float divisionType;
	protected int resolution;
	private List<MidiTrack> tracks;
	
	public MidiSequence(float divisionType, int resolution){
		this.divisionType = divisionType;
		this.resolution = resolution;
		this.tracks = new ArrayList<MidiTrack>();
	}
	
	public void addTrack(MidiTrack track){
		this.tracks.add(track);
	}
	
	public MidiTrack getTrack(int index){
		return this.tracks.get(index);
	}
	
	public int countTracks(){
		return this.tracks.size();
	}
	
	public float getDivisionType() {
		return this.divisionType;
	}
	
	public int getResolution() {
		return this.resolution;
	}
	
	public void sort(){
		for (MidiTrack track : this.tracks) {
			track.sort();
		}
	}
	
	public void finish(){
		for (MidiTrack track : this.tracks) {
			track.add(new MidiEvent(MidiMessageUtils.endOfTrack(), track.ticks()));
			track.sort();
		}
	}
}
 