package org.herac.tuxguitar.player.base;

public class MidiMeasureHelper {

    private int index;
    private long move;

    public MidiMeasureHelper(int index, long move) {
        this.index = index;
        this.move = move;
    }

    public int getIndex() {
        return this.index;
    }

    public long getMove() {
        return this.move;
    }
}
