package org.herac.tuxguitar.player.base;

import java.util.ArrayList;
import java.util.List;

public class MidiSequenceHelper {

    private int index;
    private List<MidiMeasureHelper> measureHeaderHelpers;
    private MidiSequenceHandler sequence;

    public MidiSequenceHelper(MidiSequenceHandler sequence) {
        this.index = -1;
        this.sequence = sequence;
        this.measureHeaderHelpers = new ArrayList<MidiMeasureHelper>();
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public MidiSequenceHandler getSequence() {
        return this.sequence;
    }

    public void addMeasureHelper(MidiMeasureHelper helper) {
        this.measureHeaderHelpers.add(helper);
    }

    public List<MidiMeasureHelper> getMeasureHelpers() {
        return this.measureHeaderHelpers;
    }

    public MidiMeasureHelper getMeasureHelper(int index) {
        return this.measureHeaderHelpers.get(index);
    }
}