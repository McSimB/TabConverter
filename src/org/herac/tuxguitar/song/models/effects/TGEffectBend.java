package org.herac.tuxguitar.song.models.effects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.herac.tuxguitar.song.factory.TGFactory;

public abstract class TGEffectBend {

	public static final int SEMITONE_LENGTH = 1;
	public static final int MAX_POSITION_LENGTH = 12;
	public static final int MAX_VALUE_LENGTH = (SEMITONE_LENGTH * 12);

	private List<BendPoint> points;

	public TGEffectBend() {
		this.points = new ArrayList<BendPoint>();
	}

	public void addPoint(float position, int value, boolean vibrato) {
		this.points.add(new BendPoint(position, value, vibrato));
	}

	public List<BendPoint> getPoints() {
		return this.points;
	}

	public TGEffectBend clone(TGFactory factory){
		TGEffectBend effect = factory.newEffectBend();
		Iterator<BendPoint> it = getPoints().iterator();
		while(it.hasNext()){
			BendPoint point = it.next();
			effect.addPoint(point.getPosition(),point.getValue(), point.getVibrato());
		}
		return effect;
	}
	
	public class BendPoint {

		private float position;
		private int value;
		private boolean vibrato;

		public BendPoint(float position, int value, boolean vibrato) {
			this.position = position;
			this.value = value;
			this.vibrato = vibrato;
		}

		public float getPosition() {
			return this.position;
		}

		public int getValue() {
			return this.value;
		}

		public boolean getVibrato() {
			return this.vibrato;
		}

		public long getTime(long duration) {
			return (long) (duration * getPosition() / MAX_POSITION_LENGTH);
		}
	}
}
