package org.herac.tuxguitar.song.models;

import org.herac.tuxguitar.song.factory.TGFactory;
import org.herac.tuxguitar.song.models.effects.TGEffectBend;
import org.herac.tuxguitar.song.models.effects.TGEffectGrace;
import org.herac.tuxguitar.song.models.effects.TGEffectHarmonic;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloBar;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloPicking;
import org.herac.tuxguitar.song.models.effects.TGEffectTrill;

public abstract class TGNoteEffect {

	public static final int SLIDE_NONE = 0x00;
	public static final int SLIDE_SHIFT_TO = 0x01;
	public static final int SLIDE_LEGATO_TO = 0x02;
	public static final int SLIDE_OUT_DOWN = 0x04;
	public static final int SLIDE_OUT_UP = 0x08;
	public static final int SLIDE_IN_FROM_BELOW = 0x10;
	public static final int SLIDE_IN_FROM_ABOVE = 0x20;

	private TGEffectBend bend;
	private TGEffectHarmonic harmonic;
	private TGEffectGrace grace;
	private TGEffectTrill trill;
	private TGEffectTremoloPicking tremoloPicking;
	private int slideType;
	private boolean vibrato;
	private boolean deadNote;
	private boolean hammer;
	private boolean ghostNote;
	private boolean accentuatedNote;
	private boolean heavyAccentuatedNote;
	private boolean palmMute;
	private boolean staccato;
	private boolean letRing;

	public TGNoteEffect() {
		this.bend = null;
		this.harmonic = null;
		this.grace = null;
		this.trill = null;
		this.tremoloPicking = null;
	}

	public boolean isDeadNote() {
		return this.deadNote;
	}

	public void setDeadNote(boolean deadNote) {
		this.deadNote = deadNote;
		//если true, удалить несовместимые эффекты
		if (this.isDeadNote()) {
			this.bend = null;
			this.trill = null;
			this.hammer = false;
			this.tremoloPicking = null;
		}
	}

	public boolean isVibrato() {
		return this.vibrato;
	}

	public void setVibrato(boolean vibrato) {
		this.vibrato = vibrato;
		//если не null, удаляю несовместимые эффекты
		if (this.isVibrato()) {
			this.trill = null;
			this.tremoloPicking = null;
		}
	}

	public TGEffectBend getBend() {
		return this.bend;
	}

	public void setBend(TGEffectBend bend) {
		this.bend = bend;
		//если не null, удаляю несовместимые эффекты
		if (this.isBend()) {
			this.trill = null;
			this.deadNote = false;
			this.hammer = false;
			this.tremoloPicking = null;
		}
	}

	public boolean isBend() {
		return (this.bend != null && !this.bend.getPoints().isEmpty());
	}

	public TGEffectTrill getTrill() {
		return this.trill;
	}

	public void setTrill(TGEffectTrill trill) {
		this.trill = trill;
		//если true, удалить несовместимые эффекты
		if (this.isTrill()) {
			this.bend = null;
			this.tremoloPicking = null;
			//this.slide = false;
			this.hammer = false;
			this.deadNote = false;
			this.vibrato = false;
		}
	}

	public boolean isTrill() {
		return (this.trill != null);
	}

	public TGEffectTremoloPicking getTremoloPicking() {
		return this.tremoloPicking;
	}

	public void setTremoloPicking(TGEffectTremoloPicking tremoloPicking) {
		this.tremoloPicking = tremoloPicking;
		//если true, удалить несовместимые эффекты
		if (this.isTremoloPicking()) {
			this.trill = null;
			this.bend = null;
			//this.slide = false;
			this.hammer = false;
			this.deadNote = false;
			this.vibrato = false;
		}
	}

	public boolean isTremoloPicking() {
		return (this.tremoloPicking != null);
	}

	public boolean isHammer() {
		return this.hammer;
	}

	public void setHammer(boolean hammer) {
		this.hammer = hammer;
		//если true, удалить несовместимые эффекты
		if (this.isHammer()) {
			this.trill = null;
			this.bend = null;
			this.deadNote = false;
			//this.slide = false;
			this.tremoloPicking = null;
		}
	}

	public boolean isSlide() {
		return this.slideType != 0;
	}

	public int getSlideType() {
		return this.slideType;
	}

	public void setSlideType(int slideType) {
		this.slideType = slideType;
		if ((slideType & SLIDE_SHIFT_TO) != 0) {
			this.deadNote = false;
		}
		if ((slideType & SLIDE_LEGATO_TO) != 0) {
			this.deadNote = false;
		}
	}

	public boolean isGhostNote() {
		return this.ghostNote;
	}

	public void setGhostNote(boolean ghostNote) {
		this.ghostNote = ghostNote;
	}

	public boolean isAccentuatedNote() {
		return this.accentuatedNote;
	}

	public void setAccentuatedNote(boolean accentuatedNote) {
		this.accentuatedNote = accentuatedNote;
	}

	public boolean isHeavyAccentuatedNote() {
		return this.heavyAccentuatedNote;
	}

	public void setHeavyAccentuatedNote(boolean heavyAccentuatedNote) {
		this.heavyAccentuatedNote = heavyAccentuatedNote;
	}

	public void setHarmonic(TGEffectHarmonic harmonic) {
		this.harmonic = harmonic;
	}

	public TGEffectHarmonic getHarmonic() {
		return this.harmonic;
	}

	public boolean isHarmonic() {
		return (this.harmonic != null);
	}

	public TGEffectGrace getGrace() {
		return this.grace;
	}

	public void setGrace(TGEffectGrace grace) {
		this.grace = grace;
	}

	public boolean isGrace() {
		return (this.grace != null);
	}

	public boolean isPalmMute() {
		return this.palmMute;
	}

	public void setPalmMute(boolean palmMute) {
		this.palmMute = palmMute;
	}

	public boolean isStaccato() {
		return this.staccato;
	}

	public void setStaccato(boolean staccato) {
		this.staccato = staccato;
	}

	public boolean isLetRing() {
		return this.letRing;
	}

	public void setLetRing(boolean letRing) {
		this.letRing = letRing;
		if (this.isLetRing()) {
			this.staccato = false;
			this.palmMute = false;
		}
	}

	public boolean hasAnyEffect() {
		return (isBend() ||
			isHarmonic() ||
			isGrace() ||
			isTrill() ||
			isTremoloPicking() ||
			isVibrato() ||
			isDeadNote() ||
			isSlide() ||
			isHammer() ||
			isGhostNote() ||
			isAccentuatedNote() ||
			isHeavyAccentuatedNote() ||
			isPalmMute() ||
			isLetRing() ||
			isStaccato());
	}

	public TGNoteEffect clone(TGFactory factory) {
		TGNoteEffect effect = factory.newNoteEffect();
		effect.setVibrato(isVibrato());
		effect.setDeadNote(isDeadNote());
		effect.setSlideType(getSlideType());
		effect.setHammer(isHammer());
		effect.setGhostNote(isGhostNote());
		effect.setAccentuatedNote(isAccentuatedNote());
		effect.setHeavyAccentuatedNote(isHeavyAccentuatedNote());
		effect.setPalmMute(isPalmMute());
		effect.setLetRing(isLetRing());
		effect.setStaccato(isStaccato());
		effect.setBend(isBend() ? this.bend.clone(factory): null);
		effect.setHarmonic(isHarmonic() ? this.harmonic.clone(factory): null);
		effect.setGrace(isGrace() ? this.grace.clone(factory): null);
		effect.setTrill(isTrill() ? this.trill.clone(factory): null);
		effect.setTremoloPicking(isTremoloPicking() ? this.tremoloPicking.clone(factory): null);
		return effect;
	}
}
