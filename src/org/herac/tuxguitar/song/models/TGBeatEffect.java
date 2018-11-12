package org.herac.tuxguitar.song.models;

import org.herac.tuxguitar.song.factory.TGFactory;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloBar;

public abstract class TGBeatEffect {

	private boolean fadeIn;
	private boolean vibrato;
	private boolean tapping;
	private boolean slapping;
	private boolean popping;
	private boolean rasgueado;
	private int pickStroke;
	private TGStroke stroke;
	private TGEffectTremoloBar tremoloBar;

	public TGBeatEffect(TGFactory factory) {
		this.stroke = factory.newStroke();
		this.tremoloBar = null;
	}

	public void setStroke(TGStroke stroke) {
		this.stroke = stroke;
	}

	public TGStroke getStroke() {
		return this.stroke;
	}

	public boolean isFadeIn() {
		return this.fadeIn;
	}

	public void setFadeIn(boolean fadeIn) {
		this.fadeIn = fadeIn;
	}

	public boolean isVibrato() {
		return vibrato;
	}

	public void setVibrato(boolean vibrato) {
		this.vibrato = vibrato;
	}

	public boolean isPopping() {
		return this.popping;
	}

	public void setPopping(boolean popping) {
		this.popping = popping;
	}

	public boolean isSlapping() {
		return this.slapping;
	}

	public void setSlapping(boolean slapping) {
		this.slapping = slapping;
	}

	public boolean isTapping() {
		return this.tapping;
	}

	public void setTapping(boolean tapping) {
		this.tapping = tapping;
	}

	public boolean isTremoloBar() {
		return (this.tremoloBar != null);
	}

	public TGEffectTremoloBar getTremoloBar() {
		return this.tremoloBar;
	}

	public void setTremoloBar(TGEffectTremoloBar tremoloBar) {
		this.tremoloBar = tremoloBar;
	}

	public boolean isRasgueado() {
		return rasgueado;
	}

	public void setRasgueado(boolean rasgueado) {
		this.rasgueado = rasgueado;
	}

	public int getPickStroke() {
		return pickStroke;
	}

	public void setPickStroke(int pickStroke) {
		this.pickStroke = pickStroke;
	}
}
