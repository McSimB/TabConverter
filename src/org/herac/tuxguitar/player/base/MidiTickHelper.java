package org.herac.tuxguitar.player.base;

class MidiTickHelper {
    private long start;
    private long duration;

    public MidiTickHelper(long start, long duration) {
        this.start = start;
        this.duration = duration;
    }

    public long getDuration() {
        return this.duration;
    }

    public long getStart() {
        return this.start;
    }
}
