package org.herac.tuxguitar.player.base;

import org.herac.tuxguitar.song.models.TGNote;

public class MidiNoteHelper {

    private MidiMeasureHelper measureHelper;
    private TGNote note;

    public MidiNoteHelper(MidiMeasureHelper measureHelper, TGNote note) {
        this.measureHelper = measureHelper;
        this.note = note;
    }

    public MidiMeasureHelper getMeasureHelper() {
        return this.measureHelper;
    }

    public TGNote getNote() {
        return this.note;
    }
}
