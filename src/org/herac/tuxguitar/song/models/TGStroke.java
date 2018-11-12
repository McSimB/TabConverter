package org.herac.tuxguitar.song.models;

public abstract class TGStroke {

	public static final int STROKE_NONE = 0x0;
	public static final int STROKE_UP = 0x1;
	public static final int STROKE_DOWN = 0x2;

	private int direction;
	private int value;

	public TGStroke() {
		this.direction = STROKE_NONE;
	}

	public int getDirection() {
		return this.direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getValue() {
		return this.value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getIncrementTime(TGBeat beat) {
		long duration = 0;
		if (this.value > 0) {
			if (!beat.isEmpty() && !beat.isRest()) {
				long currentDuration = beat.getDuration().getTime();
				duration = (currentDuration <= TGDuration.QUARTER_TIME ? currentDuration : TGDuration.QUARTER_TIME);
			}
			if (duration > 0) {
				return Math.round(((duration / 8.0f) * (4.0f / this.value)));
			}
		}
		return 0;
	}
}
