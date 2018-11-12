package org.herac.tuxguitar.player.base;

import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.song.models.TGDuration;
import org.herac.tuxguitar.song.models.TGMeasure;
import org.herac.tuxguitar.song.models.TGMeasureHeader;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.song.models.TGStroke;
import org.herac.tuxguitar.song.models.TGTrack;
import org.herac.tuxguitar.song.models.TGVoice;

public class MidiParserTools {

    private TGSongManager manager;

    public MidiParserTools(TGSongManager manager) {
        this.manager = manager;
    }

    public long getNoteStart(MidiSequenceHelper sh, MidiNoteHelper nh) {
        int vIndex = nh.getNote().getBeat().getVoice().getIndex();
        TGNote note = nh.getNote();
        TGBeat beat = note.getBeat();
        int bIndex = beat.getVoice().getBeats().indexOf(beat);
        TGTrack track = beat.getVoice().getMeasure().getTrack();
        MidiTickHelper th = checkTripletFeel(note.getBeat(), bIndex);
        long startMove = sh.getMeasureHelper(sh.getIndex()).getMove();
        MidiBeatHelper bh = getPrevBeat(sh, track, vIndex, bIndex);
        TGBeat prevBeat = bh != null ? bh.getBeat() : null;
        int[] stroke = getStroke(beat, prevBeat, new int[track.stringCount()]);
        return applyStrokeStart(note, (th.getStart() + startMove), stroke);
    }

    public MidiBeatHelper getPrevBeat(MidiSequenceHelper sh, TGTrack track, int vIndex, int bIndex) {
        int prevBIndex = bIndex - 1;
        for (int i = sh.getIndex(); i >= 0; i--) {
            MidiMeasureHelper mh = sh.getMeasureHelper(i);

            TGMeasure measure = track.getMeasure(mh.getIndex());
            TGVoice voice = measure.getVoice(vIndex);
            if (prevBIndex >= 0) {
                return new MidiBeatHelper(mh, voice.getBeat(prevBIndex));
            }
            if (i == 0) {
                return null;
            }
            prevBIndex = track.getMeasure(sh.getMeasureHelper(i - 1).getIndex()).getVoice(vIndex).countBeats() - 1;
        }
        return null;
    }

    public long getNoteDuration(MidiSequenceHelper sh, MidiNoteHelper nh) {
        int vIndex = nh.getNote().getBeat().getVoice().getIndex();
        TGNote note = nh.getNote();
        TGBeat beat = note.getBeat();
        TGTrack track = beat.getVoice().getMeasure().getTrack();
        int bIndex = beat.getVoice().getBeats().indexOf(beat);
        MidiTickHelper th = checkTripletFeel(note.getBeat(), bIndex);
        MidiBeatHelper bh = getPrevBeat(sh, track, vIndex, bIndex);
        TGBeat prevBeat = bh != null ? bh.getBeat() : null;
        int[] stroke = getStroke(beat, prevBeat, new int[track.stringCount()]);
        return applyStrokeDuration(note, getRealNoteDuration(sh, track, note, th.getDuration(),
                bIndex), stroke);
    }

    public MidiBeatHelper getNextBeat(MidiSequenceHelper sh, TGTrack track, int vIndex, int bIndex) {
        int nextBIndex = bIndex + 1;
        int measures = sh.getMeasureHelpers().size();
        for (int i = sh.getIndex(); i < measures; i++) {
            MidiMeasureHelper mh = sh.getMeasureHelper(i);

            TGMeasure measure = track.getMeasure(mh.getIndex());
            TGVoice voice = measure.getVoice(vIndex);
            if (nextBIndex < voice.countBeats()) {
                return new MidiBeatHelper(mh, voice.getBeat(nextBIndex));
            }
            nextBIndex = 0;
        }
        return null;
    }

    public MidiNoteHelper getNextNote(MidiSequenceHelper sh, TGNote note, TGTrack track, int bIndex,
                                 boolean breakAtRest) {
        int nextBIndex = (bIndex + 1);
        int measureCount = sh.getMeasureHelpers().size();
        for (int m = sh.getIndex(); m < measureCount; m++) {
            MidiMeasureHelper mh = sh.getMeasureHelper(m);

            TGMeasure measure = track.getMeasure(mh.getIndex());
            TGVoice voice = measure.getVoice(note.getBeat().getVoice().getIndex());
            int beatCount = voice.countBeats();
            for (int b = nextBIndex; b < beatCount; b++) {
                TGBeat beat = voice.getBeat(b);
                if (!beat.isEmpty()) {
                    int noteCount = beat.countNotes();
                    for (int n = 0; n < noteCount; n++) {
                        TGNote nextNote = beat.getNote(n);
                        if (nextNote.getString() == note.getString()) {
                            return new MidiNoteHelper(mh, nextNote);
                        }
                    }
                    if (breakAtRest) {
                        return null;
                    }
                }
            }
            nextBIndex = 0;
        }
        return null;
    }

    public MidiNoteHelper getPrevNote(MidiSequenceHelper sh, TGNote note, TGTrack track, int bIndex,
                                 boolean breakAtRest) {
        int prevBIndex = bIndex;
        for (int m = sh.getIndex(); m >= 0; m--) {
            MidiMeasureHelper mh = sh.getMeasureHelper(m);

            TGMeasure measure = track.getMeasure(mh.getIndex());
            TGVoice voice = measure.getVoice(note.getBeat().getVoice().getIndex());
            prevBIndex = (prevBIndex < 0 ? voice.countBeats() : prevBIndex);
            for (int b = (prevBIndex - 1); b >= 0; b--) {
                TGBeat beat = voice.getBeat(b);
                if (!beat.isEmpty()) {
                    int noteCount = beat.countNotes();
                    for (int n = 0; n < noteCount; n++) {
                        TGNote current = beat.getNote(n);
                        if (current.getString() == note.getString()) {
                            return new MidiNoteHelper(mh, current);
                        }
                    }
                    if (breakAtRest) {
                        return null;
                    }
                }
            }
            prevBIndex = -1;
        }
        return null;
    }

    public double interpolate(double d) {
        return d * d * d * (d * (d * 6 - 15) + 10);
    }

    private MidiTickHelper checkTripletFeel(TGBeat beat, int bIndex) {
        long bStart = beat.getStart();
        long bDuration = beat.getDuration().getTime();
        if (beat.getVoice().getMeasure().getTripletFeel() == TGMeasureHeader.TRIPLET_FEEL_EIGHTH) {
            if (beat.getDuration().isEqual(newDuration(TGDuration.EIGHTH))) {
                //first time
                if ((bStart % TGDuration.QUARTER_TIME) == 0) {
                    TGBeat b = getNextBeatInVoice(beat, bIndex);
                    if (b == null || (b.getStart() > (bStart + beat.getDuration().getTime()) ||
                            b.getDuration().isEqual(newDuration(TGDuration.EIGHTH)))) {
                        TGDuration duration = newDuration(TGDuration.EIGHTH);
                        duration.getDivision().setEnters(3);
                        duration.getDivision().setTimes(2);
                        bDuration = (duration.getTime() * 2);
                    }
                } else if ((bStart % (TGDuration.QUARTER_TIME / 2)) == 0) {
                    TGBeat b = getPrevBeatInVoice(beat, bIndex);
                    if (b == null || (b.getStart() < (bStart - beat.getDuration().getTime()) ||
                            b.getDuration().isEqual(newDuration(TGDuration.EIGHTH)))) {
                        TGDuration duration = newDuration(TGDuration.EIGHTH);
                        duration.getDivision().setEnters(3);
                        duration.getDivision().setTimes(2);
                        bStart = ((bStart - beat.getDuration().getTime()) + (duration.getTime() * 2));
                        bDuration = duration.getTime();
                    }
                }
            }
        } else if (beat.getVoice().getMeasure().getTripletFeel() == TGMeasureHeader.TRIPLET_FEEL_SIXTEENTH) {
            if (beat.getDuration().isEqual(newDuration(TGDuration.SIXTEENTH))) {
                //first time
                if ((bStart % (TGDuration.QUARTER_TIME / 2)) == 0) {
                    TGBeat b = getNextBeatInVoice(beat, bIndex);
                    if (b == null || (b.getStart() > (bStart + beat.getDuration().getTime()) ||
                            b.getDuration().isEqual(newDuration(TGDuration.SIXTEENTH)))) {
                        TGDuration duration = newDuration(TGDuration.SIXTEENTH);
                        duration.getDivision().setEnters(3);
                        duration.getDivision().setTimes(2);
                        bDuration = (duration.getTime() * 2);
                    }
                } else if ((bStart % (TGDuration.QUARTER_TIME / 4)) == 0) {
                    TGBeat b = getPrevBeatInVoice(beat, bIndex);
                    if (b == null || (b.getStart() < (bStart - beat.getDuration().getTime()) ||
                            b.getDuration().isEqual(newDuration(TGDuration.SIXTEENTH)))) {
                        TGDuration duration = newDuration(TGDuration.SIXTEENTH);
                        duration.getDivision().setEnters(3);
                        duration.getDivision().setTimes(2);
                        bStart = ((bStart - beat.getDuration().getTime()) + (duration.getTime() * 2));
                        bDuration = duration.getTime();
                    }
                }
            }
        }
        return new MidiTickHelper(bStart, bDuration);
    }

    private int[] getStroke(TGBeat beat, TGBeat prevBeat, int[] stroke) {
        int direction = beat.getEffect().getStroke().getDirection();
        if (prevBeat == null || !(direction == TGStroke.STROKE_NONE &&
                prevBeat.getEffect().getStroke().getDirection() == TGStroke.STROKE_NONE)) {
            if (direction == TGStroke.STROKE_NONE) {
                for (int i = 0; i < stroke.length; i++) {
                    stroke[i] = 0;
                }
            } else {
                int stringUseds = 0;
                int stringCount = 0;
                for (int nIndex = 0; nIndex < beat.countNotes(); nIndex++) {
                    TGNote note = beat.getNote(nIndex);
                    if (!note.isTiedNote()) {
                        stringUseds |= stringUseds | 0x01 << (note.getString() - 1);
                        stringCount++;
                    }
                }
                if (stringCount > 0) {
                    int strokeMove = 0;
                    int strokeIncrement = beat.getEffect().getStroke().getIncrementTime(beat);
                    for (int i = 0; i < stroke.length; i++) {
                        int index = (direction == TGStroke.STROKE_DOWN ? (stroke.length - 1) - i : i);
                        if ((stringUseds & (0x01 << index)) != 0) {
                            stroke[index] = strokeMove;
                            strokeMove += strokeIncrement;
                        }
                    }
                }
            }
        }
        return stroke;
    }

    private long applyStrokeStart(TGNote note, long start, int[] stroke) {
        return (start + stroke[note.getString() - 1]);
    }

    private TGDuration newDuration(int value) {
        TGDuration duration = this.manager.getFactory().newDuration();
        duration.setValue(value);
        return duration;
    }

    private TGBeat getNextBeatInVoice(TGBeat beat, int bIndex) {
        TGBeat next = null;
        for (int b = bIndex + 1; b < beat.getVoice().countBeats(); b++) {
            TGBeat current = beat.getVoice().getBeat(b);
            if (current.getStart() > beat.getStart()) {
                if (next == null || current.getStart() < next.getStart()) {
                    next = current;
                }
            }
        }
        return next;
    }

    private TGBeat getPrevBeatInVoice(TGBeat beat, int bIndex) {
        TGBeat previous = null;
        for (int b = bIndex - 1; b >= 0; b--) {
            TGBeat current = beat.getVoice().getBeat(b);
            if (current.getStart() < beat.getStart()) {
                if (previous == null || current.getStart() > previous.getStart()) {
                    previous = current;
                }
            }
        }
        return previous;
    }

    private long applyStrokeDuration(TGNote note, long duration, int[] stroke) {
        return (duration > stroke[note.getString() - 1] ?
                (duration - stroke[note.getString() - 1]) : duration);
    }

    private long getRealNoteDuration(MidiSequenceHelper sh, TGTrack track, TGNote note,
                                     long duration, int bIndex) {
        int mIndex = sh.getIndex();
        int vIndex = note.getBeat().getVoice().getIndex();
        boolean letRing = (note.getEffect().isLetRing());
        boolean letRingBeatChanged = false;
        long lastEnd = (note.getBeat().getStart() + note.getBeat().getDuration().getTime() +
                sh.getMeasureHelper(mIndex).getMove());
        long realDuration = duration;
        int nextBIndex = (bIndex + 1);
        int mCount = sh.getMeasureHelpers().size();
        for (int m = mIndex; m < mCount; m++) {
            MidiMeasureHelper mh = sh.getMeasureHelper(m);
            TGMeasure measure = track.getMeasure(mh.getIndex());
            TGVoice voice = measure.getVoice(vIndex);
            int beatCount = voice.countBeats();
            for (int b = nextBIndex; b < beatCount; b++) {
                TGBeat beat = voice.getBeat(b);
                if (!beat.isEmpty()) {
                    if (beat.isRest()) {
                        return realDuration;
                    }
                    int noteCount = beat.countNotes();
                    for (int n = 0; n < noteCount; n++) {
                        TGNote nextNote = beat.getNote(n);
                        if (!nextNote.equals(note) || mIndex != m) {
                            if (nextNote.getString() == note.getString()) {
                                if (nextNote.isTiedNote()) {
                                    realDuration += (mh.getMove() + beat.getStart() - lastEnd) +
                                            (nextNote.getBeat().getDuration().getTime());
                                    lastEnd = (mh.getMove() + beat.getStart() + beat.getDuration().getTime());
                                    letRing = (nextNote.getEffect().isLetRing());
                                    letRingBeatChanged = true;
                                } else {
                                    return realDuration;
                                }
                            }
                        }
                    }
                    if (letRing && !letRingBeatChanged) {
                        realDuration += (beat.getDuration().getTime());
                    }
                    letRingBeatChanged = false;
                }
            }
            nextBIndex = 0;
        }
        return realDuration;
    }
}