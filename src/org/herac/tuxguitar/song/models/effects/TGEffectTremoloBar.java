package org.herac.tuxguitar.song.models.effects;

import org.herac.tuxguitar.song.factory.TGFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class TGEffectTremoloBar {

	public static final int MAX_POSITION_LENGTH = 12;
	public static final int MAX_VALUE_LENGTH = 12;

	private List<TremoloBarPoint> points;

	public TGEffectTremoloBar() {
		this.points = new ArrayList<TremoloBarPoint>();
	}

	public void addPoint(float position, int value, boolean vibrato) {
		this.points.add(new TremoloBarPoint(position, value, vibrato));
	}

	public List<TremoloBarPoint> getPoints() {
		return this.points;
	}

	public TGEffectTremoloBar clone(TGFactory factory) {
		TGEffectTremoloBar effect = factory.newEffectTremoloBar();
		for (TremoloBarPoint point : getPoints()) {
			effect.addPoint(point.getPosition(), point.getValue(), point.getVibrato());
		}

		return effect;
	}

	public class TremoloBarPoint {
		private float position;
		private int value;
		private boolean vibrato;

		public TremoloBarPoint(float position, int value, boolean vibrato) {
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
