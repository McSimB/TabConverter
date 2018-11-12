package org.herac.tuxguitar.player.base;

import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.song.models.TGChannel;
import org.herac.tuxguitar.song.models.TGDuration;
import org.herac.tuxguitar.song.models.TGMeasure;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.song.models.TGNoteEffect;
import org.herac.tuxguitar.song.models.TGSong;
import org.herac.tuxguitar.song.models.TGTempo;
import org.herac.tuxguitar.song.models.TGTrack;
import org.herac.tuxguitar.song.models.TGVelocities;
import org.herac.tuxguitar.song.models.TGVoice;
import org.herac.tuxguitar.song.models.effects.TGEffectBend;
import org.herac.tuxguitar.song.models.effects.TGEffectBend.BendPoint;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloBar;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloBar.TremoloBarPoint;

import java.util.List;


public class MidiSequenceParser {

    private static final int DEFAULT_DURATION_PM = 60;
    private static final int DEFAULT_DURATION_DEAD = 30;
    private static final int DEFAULT_BEND = 8192;
    private static final float DEFAULT_BEND_SEMI_TONE = DEFAULT_BEND / 12f;
    private static final int DEFAULT_BEND_DIVISION = 20;

    private static final int VELOCITY_E = 100;
    private static final int VELOCITY_HAMMER_E = 50;
    private static final int PALM_MUTE_E = 45;
    private static final int DEAD_E = 0;
    // Values of strings selection controller (1...6 strings) (cc #32)
    private static final int[] STRINGS_E = new int[]{120, 100, 80, 60, 40, 20};

    private MidiParserTools tools;
    private TGSong song;
    private TGSongManager songManager;
    private int infoTrack;
    private int firstTickMove;
    private int tempoPercent;
    private int transpose;
    private int prevPM_E;
    private int prevString_E;
    /*
     ---Electri6city---
     Velocity -- dead(1 - 12); P.M.(13 - 69); sustain(70 - 127)
     CC -- dead(0 - 9); P.M.(10 - 59); sustain(60 - 127)
     Playing | Slide Mode | Threshold => 100ms
     Playing | Switch Sustain To Hammer On/Pull Off => Off
     */
    private boolean isGuitar_E;
    // Real LPC
    //private boolean isGuitar_L;
    // Shreddage
    //private boolean isGuitar_S;
    // Real Acoustic
    //private boolean isGuitar_A;

    public MidiSequenceParser(TGSong song, TGSongManager songManager) {
        this.song = song;
        this.songManager = songManager;
        this.tools = new MidiParserTools(songManager);
        this.tempoPercent = 100;
        this.transpose = 0;
        this.firstTickMove = (int) -TGDuration.QUARTER_TIME;
    }

    public int getInfoTrack() {
        return this.infoTrack;
    }

    public void setTranspose(int transpose) {
        this.transpose = transpose;
    }

    public void parse(MidiSequenceHandler sequence) {
        this.infoTrack = 0;

        MidiSequenceHelper helper = new MidiSequenceHelper(sequence);
        MidiRepeatController controller = new MidiRepeatController(this.song);
        while (!controller.finished()) {
            int index = controller.getIndex();
            long move = controller.getRepeatMove();
            controller.process();
            if (controller.shouldPlay()) {
                helper.addMeasureHelper(new MidiMeasureHelper(index, move));
            }
        }

        for (int i = 0; i < this.song.countTracks(); i++) {
            addTrack(helper, this.song.getTrack(i));
        }
        sequence.notifyFinish();
    }

    private void addTrack(MidiSequenceHelper sh, TGTrack track) {
        TGChannel tgChannel = this.songManager.getChannel(this.song, track.getChannelId());
        if (tgChannel != null) {
            isGuitar_E = track.getName().endsWith("_E");
            //isGuitar_L = track.getName().endsWith("_L");
            //isGuitar_S = track.getName().endsWith("_S");
            //isGuitar_A = track.getName().endsWith("_A");

            this.prevPM_E = 127;
            this.prevString_E = 0;

            TGMeasure previous = null;

            addBend(sh, track.getNumber(), TGDuration.QUARTER_TIME, DEFAULT_BEND, tgChannel.getChannelId());
            addChannel(sh, tgChannel, track.getNumber());

            int mCount = sh.getMeasureHelpers().size();
            for (int mIndex = 0; mIndex < mCount; mIndex++) {
                MidiMeasureHelper mh = sh.getMeasureHelper(mIndex);
                sh.setIndex(mIndex);

                TGMeasure measure = track.getMeasure(mh.getIndex());
                if (track.getNumber() == 1) {
                    addTimeSignature(sh, measure, previous, mh.getMove());
                    addTempo(sh, measure, previous, mh.getMove());
                }
                for (TGVoice voice : measure.getVoices()) {
                    addBeats(sh, tgChannel, track, measure, voice);
                }

                previous = measure;
            }
        }
    }

    private void addChannel(MidiSequenceHelper sh, TGChannel channel, int track) {
        int channelId = channel.getChannelId();
        long tick = getTick(TGDuration.QUARTER_TIME);
        sh.getSequence().addTrackName(tick, track, this.song.getTrack(track - 1).getName());
        sh.getSequence().addProgramChange(tick, track, channelId, fix(channel.getProgram()));
        sh.getSequence().addControlChange(tick, track, channelId, MidiControllers.RPN_MSB, 0);
        sh.getSequence().addControlChange(tick, track, channelId, MidiControllers.RPN_LSB, 0);
        sh.getSequence().addControlChange(tick, track, channelId, MidiControllers.DATA_ENTRY_MSB, 12);
        sh.getSequence().addControlChange(tick, track, channelId, MidiControllers.DATA_ENTRY_LSB, 0);
        sh.getSequence().addControlChange(tick, track, channelId, MidiControllers.EXPRESSION, 127);
        if (isGuitar_E) {
            sh.getSequence().addControlChange(tick, track, channelId, 1, 127);
            // Strum Time
            sh.getSequence().addControlChange(tick, track, channelId, 23, 64);
            // Strum Direction
            sh.getSequence().addControlChange(tick, track, channelId, 24, 127);
            // Pick Direction
            sh.getSequence().addControlChange(tick, track, channelId, 25, 127);
            // Pick Position
            sh.getSequence().addControlChange(tick, track, channelId, 26, 64);
            // Vibrato Strength
            sh.getSequence().addControlChange(tick, track, channelId, 28, 0);
            // Vibrato Speed
            sh.getSequence().addControlChange(tick, track, channelId, 29, 64);
            //-------------------------------------------------------------
            // Tone | Hammer Ons/Pull Offs => HO/PO Played After Sustain Note
            sh.getSequence().addControlChange(tick, track, channelId, 39, 127);
            // Playing | Trills => Speed=1/32
            sh.getSequence().addControlChange(tick, track, channelId, 49, 64);
            // Playing | Tremolo Picking => Speed=1/32
            sh.getSequence().addControlChange(tick, track, channelId, 50, 64);
            // Playing | Guitar Chords => Off
            sh.getSequence().addControlChange(tick, track, channelId, 47, 0);
            // Strumming | Speed | Mode => Absolute Time (per chord)
            sh.getSequence().addControlChange(tick, track, channelId, 63, 127);
            // Vibrato | Control Mode => by CC
            sh.getSequence().addControlChange(tick, track, channelId, 72, 0);
            // Vibrato | Max. Strength => 1 Semitones
            sh.getSequence().addControlChange(tick, track, channelId, 91, 0);
        } else {
            sh.getSequence().addControlChange(tick, track, channelId, MidiControllers.VOLUME,
                    fix(channel.getVolume()));
            sh.getSequence().addControlChange(tick, track, channelId, MidiControllers.BALANCE,
                    fix(channel.getBalance()));
        }
    }

    private void addBeats(MidiSequenceHelper sh, TGChannel channel, TGTrack track, TGMeasure measure, TGVoice voice) {

        for (int bIndex = 0; bIndex < voice.countBeats(); bIndex++) {
            TGBeat beat = voice.getBeat(bIndex);

            //---Fade In---
            if (beat.getEffect().isFadeIn()) {
                addFadeIn(sh, track.getNumber(), beat.getStart(), beat.getDuration().getTime(),
                        channel.getChannelId());
            }

            //---TremoloBar---
            if (beat.getEffect().isTremoloBar()) {
                addTremoloBar(sh, track.getNumber(), beat.getStart(), beat.getDuration().getTime(),
                        beat.getEffect().getTremoloBar(), channel.getChannelId());
            }


            addNotes(sh, channel, track, beat, measure.getTempo(), bIndex);
        }
    }

    private void addNotes(MidiSequenceHelper sh, TGChannel tgChannel, TGTrack track, TGBeat beat, TGTempo tempo,
                          int bIndex) {

        int string = 0;

        for (TGNote note : beat.getNotes()) {
            long[] extra = new long[]{0L, 0L};

            if (!note.isTiedNote()) {
                int key = this.transpose + track.getOffset() + note.getValue() +
                        track.getStrings().get(note.getString() - 1).getValue();

                MidiNoteHelper nh = new MidiNoteHelper(sh.getMeasureHelper(sh.getIndex()), note);
                long start = tools.getNoteStart(sh, nh);
                long duration = applyDurationEffects(note, tempo, tools.getNoteDuration(sh, nh));

                int velocity = applyNoteVelocity(sh, note, track, tgChannel, bIndex);

                int channel = tgChannel.getChannelId();
                boolean percussionChannel = tgChannel.isPercussionChannel();

                if (isGuitar_E) {
                    //---String selection controller---
                    if (note.getString() > string) string = note.getString();
                    if (string != this.prevString_E && !beat.isRest()) {
                        sh.getSequence().addControlChange(getTick(start), track.getNumber(), channel, 32,
                                STRINGS_E[string - 1]);
                        this.prevString_E = string;
                    }
                }

                //---Palm Mute---
                if (isGuitar_E) {
                    if (note.getEffect().isPalmMute() && this.prevPM_E != PALM_MUTE_E) {
                        sh.getSequence().addControlChange(getTick(start), track.getNumber(), channel, 1, PALM_MUTE_E);
                        this.prevPM_E = PALM_MUTE_E;
                    } else if (!note.getEffect().isPalmMute() && this.prevPM_E != 127) {
                        sh.getSequence().addControlChange(getTick(start), track.getNumber(), channel, 1, 127);
                        this.prevPM_E = 127;
                        //---Dead Note
                    } else if (note.getEffect().isDeadNote() && this.prevPM_E != DEAD_E) {
                        sh.getSequence().addControlChange(getTick(start), track.getNumber(), channel, 1, DEAD_E);
                        this.prevPM_E = DEAD_E;
                    }
                }

                //---Grace---
                if (note.getEffect().isGrace()) {
                    int graceKey = track.getOffset() + note.getEffect().getGrace().getFret() +
                            track.getStrings().get(note.getString() - 1).getValue();
                    int graceLength = note.getEffect().getGrace().getDurationTime();
                    int graceVelocity = isGuitar_E ? VELOCITY_E : note.getEffect().getGrace().getDynamic();
                    long graceDuration = ((note.getEffect().getGrace().isDead()) ? applyStaticDuration(tempo,
                            DEFAULT_DURATION_DEAD, graceLength) : graceLength);

                    if (note.getEffect().getGrace().isOnBeat() || (start - graceLength) < TGDuration.QUARTER_TIME) {
                        start += graceLength;
                        duration -= graceLength;
                    }
                    addNote(sh, track.getNumber(), graceKey, start - graceLength, graceDuration, graceVelocity,
                            channel);
                }

                //---Trill---
                if (note.getEffect().isTrill() && !percussionChannel) {
                    int trillKey = track.getOffset() + note.getEffect().getTrill().getFret() +
                            track.getStrings().get(note.getString() - 1).getValue();
                    long trillLength = note.getEffect().getTrill().getDuration().getTime();

                    boolean realKey = true;
                    long tick = start;
                    while (true) {
                        if (tick + 10 >= (start + duration)) {
                            break;
                        } else if ((tick + trillLength) >= (start + duration)) {
                            trillLength = ((start + duration) - tick) - 1;
                        }
                        addNote(sh, track.getNumber(), ((realKey) ? key : trillKey), tick, trillLength, velocity,
                                channel);
                        realKey = (!realKey);
                        tick += trillLength;
                    }
                    continue;
                }

                //---Tremolo Picking---
                if (note.getEffect().isTremoloPicking()) {
                    long tpLength = note.getEffect().getTremoloPicking().getDuration().getTime();
                    long tick = start;
                    while (true) {
                        if (tick + 10 >= (start + duration)) {
                            break;
                        } else if ((tick + tpLength) >= (start + duration)) {
                            tpLength = ((start + duration) - tick) - 1;
                        }
                        addNote(sh, track.getNumber(), key, tick, tpLength, velocity, channel);
                        tick += tpLength;
                    }
                    continue;
                }

                //---Bend---
                if (note.getEffect().isBend() && !percussionChannel) {
                    addBend(sh, track.getNumber(), start, duration, note.getEffect().getBend(), channel);
                }

                //---Vibrato---
                if (note.getEffect().isVibrato() && !percussionChannel) {
                    if (isGuitar_E) {
                        sh.getSequence().addControlChange(getTick(start), track.getNumber(), channel, 28, 127);
                        sh.getSequence().addControlChange(getTick(start + duration), track.getNumber(), channel, 28, 0);
                    } else {
                        addVibrato(sh, track.getNumber(), start, duration, channel);
                    }
                }

                //---Harmonic---
                if (note.getEffect().isHarmonic() && !percussionChannel) {

                }

                //---Slide---
                if (note.getEffect().isSlide() && !percussionChannel) {
                    extra = addSlide(sh, note, start, duration, track, bIndex, channel);
                }

                MidiBeatHelper nextBh = tools.getNextBeat(sh, track,
                        note.getBeat().getVoice().getIndex(), bIndex);
                MidiNoteHelper prevNh = tools.getPrevNote(sh, note, track, bIndex, true);

                if (prevNh != null && prevNh.getNote().getEffect().isHammer()) {
                    // Hammer On
                    if (prevNh.getNote().getValue() < note.getValue()) {
                        addNote(sh, track.getNumber(), 28, start, duration, 100, tgChannel.getChannelId());
                        // Pull Off
                    } else {
                        addNote(sh, track.getNumber(), 28, start, duration, 50, tgChannel.getChannelId());
                    }
                    if (!note.getEffect().isHammer()) {
                        long dur;
                        if (nextBh != null) {
                            dur = nextBh.getBeat().getDuration().getTime();
                            addNote(sh, track.getNumber(), 24, start + duration, dur, 100, tgChannel.getChannelId());
                        }
                    }
                }

                if (nextBh != null) {
                    for (TGNote nextNote : nextBh.getBeat().getNotes()) {
                        if ((nextNote.getEffect().getSlideType() &
                                (TGNoteEffect.SLIDE_IN_FROM_BELOW | TGNoteEffect.SLIDE_IN_FROM_ABOVE)) != 0) {
                            extra[1] -= Math.min(duration / 12, TGDuration.QUARTER_TIME / 12) * 3;
                        }
                        if (nextNote.getEffect().isGrace() && !nextNote.getEffect().getGrace().isOnBeat()) {

                        }
                    }
                }

                //---Normal Note---
                addNote(sh, track.getNumber(), Math.min(127, key), start + extra[0], duration + extra[1],
                        velocity, channel);
            }
        }
    }

    private void addNote(MidiSequenceHelper sh, int track, int key, long start, long duration, int velocity, int channel) {
        sh.getSequence().addNoteOn(getTick(start), track, channel, fix(key), fix(velocity));
        if (duration > 0) {
            sh.getSequence().addNoteOff(getTick(start + duration), track, channel, fix(key), 0);
        }
    }

    private void addBend(MidiSequenceHelper sh,
                         int track, long tick, int bend, int channel) {
        sh.getSequence().addPitchBend(getTick(tick), track, channel, bend);
    }

    private void addVibrato(MidiSequenceHelper sh, int track, long start, long duration, int channel) {
        long nextStart = start;
        long end = nextStart + duration;

        while (nextStart < end) {
            nextStart = ((nextStart + 160 > end) ? end : nextStart + 160);
            addBend(sh, track, nextStart, DEFAULT_BEND, channel);
            nextStart = ((nextStart + 160 > end) ? end : nextStart + 160);
            addBend(sh, track, nextStart,
                    DEFAULT_BEND + (int) (DEFAULT_BEND_SEMI_TONE / 2.0f), channel);
        }
        addBend(sh, track, nextStart, DEFAULT_BEND, channel);
    }

    private void addBend(MidiSequenceHelper sh, int track, long start, long duration, TGEffectBend bend
            , int channel) {
        int prevValue = DEFAULT_BEND - 1;
        List<BendPoint> points = bend.getPoints();
        for (int i = 0; i < points.size() - 1; i++) {
            TGEffectBend.BendPoint point = points.get(i);
            TGEffectBend.BendPoint nextPoint = points.get(i + 1);
            long bendStart = start + point.getTime(duration);
            double bendLength = duration * (nextPoint.getPosition() - point.getPosition()) /
                    (double) TGEffectBend.MAX_POSITION_LENGTH;
            double bendWidth = DEFAULT_BEND / (TGEffectBend.MAX_VALUE_LENGTH * 2d) *
                    (nextPoint.getValue() - point.getValue());

            if (point.getVibrato()) {
                sh.getSequence().addControlChange(getTick(bendStart), track, channel, 28, 127);
                sh.getSequence().addControlChange(getTick(bendStart + (int) bendLength), track, channel, 28, 0);
            }

            for (int x = 0; x < bendLength; x += DEFAULT_BEND_DIVISION) {
                double y = tools.interpolate(x / bendLength);
                int value = (int) (bendWidth * y + DEFAULT_BEND / (TGEffectBend.MAX_VALUE_LENGTH * 2d) *
                        point.getValue() + DEFAULT_BEND);
                if (prevValue != value) {
                    addBend(sh, track, bendStart, value, channel);
                    prevValue = value;
                }
                bendStart += DEFAULT_BEND_DIVISION;
            }
        }
        if (prevValue != DEFAULT_BEND) {
            addBend(sh, track, start + duration, DEFAULT_BEND, channel);
        }
    }

    private void addTremoloBar(MidiSequenceHelper sh, int track, long start, long duration,
                         TGEffectTremoloBar effect,
                              int channel) {
        int prevValue = DEFAULT_BEND - 1;
        List<TremoloBarPoint> points = effect.getPoints();
        for (int i = 0; i < points.size() - 1; i++) {
            TGEffectTremoloBar.TremoloBarPoint point = points.get(i);
            TGEffectTremoloBar.TremoloBarPoint nextPoint = points.get(i + 1);
            long effectStart = start + point.getTime(duration);
            double effectLength = duration * (nextPoint.getPosition() - point.getPosition()) /
                    (double) TGEffectTremoloBar.MAX_POSITION_LENGTH;
            double effectWidth = DEFAULT_BEND / (TGEffectTremoloBar.MAX_VALUE_LENGTH * 2d) *
                    (nextPoint.getValue() - point.getValue());

            if (point.getVibrato()) {
                sh.getSequence().addControlChange(getTick(effectStart), track, channel, 28, 127);
                sh.getSequence().addControlChange(getTick(effectStart + (int) effectLength), track, channel, 28, 0);
            }

            for (int x = 0; x < effectLength; x += DEFAULT_BEND_DIVISION) {
                double y = tools.interpolate(x / effectLength);
                int value = (int) (effectWidth * y + DEFAULT_BEND / (TGEffectTremoloBar.MAX_VALUE_LENGTH * 2d) *
                        point.getValue() + DEFAULT_BEND);
                if (prevValue != value) {
                    addBend(sh, track, effectStart, value, channel);
                    prevValue = value;
                }
                effectStart += DEFAULT_BEND_DIVISION;
            }
        }
        if (prevValue != DEFAULT_BEND) {
            addBend(sh, track, start + duration, DEFAULT_BEND, channel);
        }
    }

    private long[] addSlide(MidiSequenceHelper sh, TGNote note, long start, long duration, TGTrack track, int bIndex,
                            int channel) {
        long extraStart = 0L;
        long extraDuration = 0L;
        int value1 = note.getValue();

        // slide shift, slide legato
        if ((note.getEffect().getSlideType() & (TGNoteEffect.SLIDE_SHIFT_TO | TGNoteEffect.SLIDE_LEGATO_TO)) != 0) {
            MidiNoteHelper nextNote = tools.getNextNote(sh, note, track, bIndex, false);
            if (nextNote != null) {
                int value2 = nextNote.getNote().getValue();
                long tick2 = tools.getNoteStart(sh, nextNote);
                long duration2 = tools.getNoteDuration(sh, nextNote);

                int distance = Math.max(Math.abs(value2 - value1), 2);
                int direction = (value2 - value1) > 0 ? 1 : -1;
                long length = (tick2 - start) / 20 * distance;

                if (isGuitar_E) {
                    if ((note.getEffect().getSlideType() & TGNoteEffect.SLIDE_LEGATO_TO) != 0) {
                        if (note.getBeat().getNotes().size() == 1) {
                            addNote(sh, track.getNumber(), 33, tick2, duration2, 100, channel);
                        } else if (distance <= 2) {
                            int key = 0;
                            if (value2 - value1 == 1) {
                                key = 106;
                            } else if (value2 - value1 == 2) {
                                key = 15;
                            } else if (value2 - value1 == -1) {
                                key = 104;
                            } else if (value2 - value1 == -2) {
                                key = 14;
                            }
                            addNote(sh, track.getNumber(), key, tick2, duration2, 100, channel);
                        } else {
                            for (int i = 1; i < distance; i++) {
                                int bend = DEFAULT_BEND + ((int) (i * DEFAULT_BEND_SEMI_TONE)) * direction;
                                addBend(sh, track.getNumber(), tick2 - length + length / (distance - 1) * (i - 1),
                                        bend, channel);
                            }
                            int bend = DEFAULT_BEND + ((int) (distance * DEFAULT_BEND_SEMI_TONE)) * direction;
                            addBend(sh, track.getNumber(), tick2, bend, channel);
                            addBend(sh, track.getNumber(), tick2 + duration2, DEFAULT_BEND, channel);
                            // TODO : slide
                            nextNote.getNote().setTiedNote(true);
                            extraDuration = duration2;
                        }
                    }
                } else {
                    for (int i = 1; i < distance; i++) {
                        int bend = DEFAULT_BEND + ((int) (i * DEFAULT_BEND_SEMI_TONE)) * direction;
                        addBend(sh, track.getNumber(), tick2 - length + length / (distance - 1) * (i - 1),
                                bend, channel);
                    }
                }

                if ((note.getEffect().getSlideType() & TGNoteEffect.SLIDE_SHIFT_TO) != 0 && !isGuitar_E) {
                    addBend(sh, track.getNumber(), tick2, DEFAULT_BEND, channel);
                } else if ((note.getEffect().getSlideType() & TGNoteEffect.SLIDE_LEGATO_TO) != 0 && !isGuitar_E) {
                    int bend = DEFAULT_BEND + ((int) (distance * DEFAULT_BEND_SEMI_TONE)) * direction;
                    addBend(sh, track.getNumber(), tick2, bend, channel);
                    addBend(sh, track.getNumber(), tick2 + duration2, DEFAULT_BEND, channel);
                    // TODO : slide
                    nextNote.getNote().setTiedNote(true);
                    extraDuration = duration2;
                }
            }
        }
        // slide in
        if ((note.getEffect().getSlideType() & (TGNoteEffect.SLIDE_IN_FROM_BELOW | TGNoteEffect.SLIDE_IN_FROM_ABOVE)) != 0) {
            int distance = 3;
            long length = Math.min(duration / 12, TGDuration.QUARTER_TIME / 12);
            int direction = ((note.getEffect().getSlideType() & TGNoteEffect.SLIDE_IN_FROM_ABOVE) != 0) ? 1 : -1;
            for (int i = distance; i >= 0; i--) {
                int bend = DEFAULT_BEND + ((int) (i * DEFAULT_BEND_SEMI_TONE)) * direction;
                addBend(sh, track.getNumber(), start - length * i, bend, channel);
            }
            extraStart = -length * 3;
            extraDuration += length * 3;
        }
        // slide out
        if ((note.getEffect().getSlideType() & (TGNoteEffect.SLIDE_OUT_DOWN | TGNoteEffect.SLIDE_OUT_UP)) != 0) {
            int distance = 8;
            long length = (duration - duration / 4) / 7;
            int direction = ((note.getEffect().getSlideType() & TGNoteEffect.SLIDE_OUT_UP) != 0) ? 1 : -1;
            for (int i = 1; i < distance; i++) {
                int bend = DEFAULT_BEND + ((int) (i * DEFAULT_BEND_SEMI_TONE)) * direction;
                addBend(sh, track.getNumber(), start + (duration - length * 8) + length * i, bend, channel);
            }
            addBend(sh, track.getNumber(), start + duration, DEFAULT_BEND, channel);
        }
        return new long[]{extraStart, extraDuration};
    }

    private void addFadeIn(MidiSequenceHelper sh, int track, long start, long duration, int channel) {
        int expression = 30;
        int expressionIncrement = 1;
        long tick = start;
        long tickIncrement = (duration / ((127 - expression) / expressionIncrement));
        while (tick < (start + duration) && expression < 127) {
            sh.getSequence().addControlChange(getTick(tick), track, channel, MidiControllers.EXPRESSION, fix(expression));
            tick += tickIncrement;
            expression += expressionIncrement;
        }
        sh.getSequence().addControlChange(getTick((start + duration)), track, channel, MidiControllers.EXPRESSION, 127);
    }

    private void addTimeSignature(MidiSequenceHelper sh, TGMeasure currMeasure, TGMeasure prevMeasure,
                                  long startMove) {
        boolean addTimeSignature = false;
        if (prevMeasure == null) {
            addTimeSignature = true;
        } else {
            int currNumerator = currMeasure.getTimeSignature().getNumerator();
            int currValue = currMeasure.getTimeSignature().getDenominator().getValue();
            int prevNumerator = prevMeasure.getTimeSignature().getNumerator();
            int prevValue = prevMeasure.getTimeSignature().getDenominator().getValue();
            if (currNumerator != prevNumerator || currValue != prevValue) {
                addTimeSignature = true;
            }
        }
        if (addTimeSignature) {
            sh.getSequence().addTimeSignature(getTick(
                    currMeasure.getStart() + startMove), getInfoTrack(), currMeasure.getTimeSignature());
        }
    }

    private void addTempo(MidiSequenceHelper sh, TGMeasure currMeasure, TGMeasure prevMeasure, long startMove) {
        boolean addTempo = false;
        if (prevMeasure == null) {
            addTempo = true;
        } else {
            if (currMeasure.getTempo().getInUSQ() != prevMeasure.getTempo().getInUSQ()) {
                addTempo = true;
            }
        }
        if (addTempo) {
            int usq = (int) (currMeasure.getTempo().getInUSQ() * 100.00 / this.tempoPercent);
            sh.getSequence().addTempoInUSQ(getTick(currMeasure.getStart() + startMove), getInfoTrack(), usq);
        }
    }

    private int applyNoteVelocity(MidiSequenceHelper sh, TGNote note, TGTrack tgTrack,
                                  TGChannel tgChannel, int bIndex) {
        int velocity;
        if (isGuitar_E) {
            velocity = VELOCITY_E;
        } else {
            velocity = note.getVelocity();
        }

        if (!tgChannel.isPercussionChannel()) {
            //Check for Hammer effect
            MidiNoteHelper prevNote = tools.getPrevNote(sh, note, tgTrack, bIndex, false);
            if (prevNote != null && prevNote.getNote().getEffect().isHammer()) {
                if (isGuitar_E) {
                    velocity = VELOCITY_HAMMER_E;
                } else {
                    velocity = Math.max(TGVelocities.MIN_VELOCITY, (velocity - 25));
                }
            }
            if (prevNote != null) {
                if (prevNote.getNote().getEffect().isSlide() &&
                        (prevNote.getNote().getEffect().getSlideType() & TGNoteEffect.SLIDE_LEGATO_TO) != 0) {
                    if (isGuitar_E) {
                        velocity = VELOCITY_HAMMER_E;
                    }
                }
            }
        }

        //Check for GhostNote effect
        if (note.getEffect().isGhostNote()) {
            if (isGuitar_E) {
                velocity = 5;
            } else {
                velocity = Math.max(TGVelocities.MIN_VELOCITY, (velocity - TGVelocities.VELOCITY_INCREMENT));
            }
        } else if (note.getEffect().isAccentuatedNote()) {
            if (!isGuitar_E) {
                velocity = Math.max(TGVelocities.MIN_VELOCITY, (velocity + TGVelocities.VELOCITY_INCREMENT));
            }
        } else if (note.getEffect().isHeavyAccentuatedNote()) {
            if (!isGuitar_E) {
                velocity = Math.max(TGVelocities.MIN_VELOCITY, (velocity + (TGVelocities.VELOCITY_INCREMENT * 2)));
            }
        }

        return ((velocity > 127) ? 127 : velocity);
    }

    private long applyDurationEffects(TGNote note, TGTempo tempo, long duration) {
        //dead note
        if (note.getEffect().isDeadNote()) {
            if (isGuitar_E) {
                return TGDuration.QUARTER_TIME / TGDuration.THIRTY_SECOND;
            } else {
                return applyStaticDuration(tempo, DEFAULT_DURATION_DEAD, duration);
            }
        }
        //palm mute
        if (note.getEffect().isPalmMute()) {
            if (!isGuitar_E) {
                return applyStaticDuration(tempo, DEFAULT_DURATION_PM, duration);
            }
        }
        //staccato
        if (note.getEffect().isStaccato()) {
            return (long) (duration * 50f / 100f);
        }
        return duration;
    }

    private long applyStaticDuration(TGTempo tempo, long duration, long maximum) {
        long value = (tempo.getValue() * duration / 60);
        return (value < maximum ? value : maximum);
    }

    private long getTick(long tick) {
        return (tick + this.firstTickMove);
    }

    private int fix(int value) {
        return Math.max(0, Math.min(127, value));
    }
}