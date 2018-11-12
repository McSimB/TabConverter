package org.herac.tuxguitar.player.base;

import org.herac.tuxguitar.song.models.TGBeat;

public class MidiBeatHelper {

    private MidiMeasureHelper measureHelper;
    private TGBeat beat;

    public MidiBeatHelper(MidiMeasureHelper measureHelper, TGBeat beat) {
        this.measureHelper = measureHelper;
        this.beat = beat;
    }

    public MidiMeasureHelper getMeasureHelper() {
        return this.measureHelper;
    }

    public TGBeat getBeat() {
        return this.beat;
    }
}
