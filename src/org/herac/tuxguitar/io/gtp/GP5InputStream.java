package org.herac.tuxguitar.io.gtp;

import org.herac.tuxguitar.gm.GMChannelRoute;
import org.herac.tuxguitar.io.base.TGFileFormat;
import org.herac.tuxguitar.io.base.TGFileFormatException;
import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.*;
import org.herac.tuxguitar.song.models.effects.TGEffectBend;
import org.herac.tuxguitar.song.models.effects.TGEffectGrace;
import org.herac.tuxguitar.song.models.effects.TGEffectHarmonic;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloBar;
import org.herac.tuxguitar.song.models.effects.TGEffectTremoloPicking;
import org.herac.tuxguitar.song.models.effects.TGEffectTrill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GP5InputStream extends GTPInputStream {

	public static final TGFileFormat FILE_FORMAT = new TGFileFormat("Guitar Pro 5", "audio/x-gtp", new String[]{"gp5"});

	public static final GTPFileFormatVersion[] SUPPORTED_VERSIONS = new GTPFileFormatVersion[]{
		new GTPFileFormatVersion(FILE_FORMAT, "FICHIER GUITAR PRO v5.00", 0),
		new GTPFileFormatVersion(FILE_FORMAT, "FICHIER GUITAR PRO v5.10", 1),
	};

	private static final float GP_BEND_SEMITONE = 25f;
	private static final float GP_BEND_POSITION = 60f;

	private int keySignature;

	public GP5InputStream(GTPSettings settings) {
		super(settings, SUPPORTED_VERSIONS);
	}

	public TGFileFormat getFileFormat() {
		return FILE_FORMAT;
	}

	public TGSong readSong() throws TGFileFormatException {
		try {
			readVersion();

			TGSong song = getFactory().newSong();

			readInfo(song);

			//lyrics
			int lyricTrack = readInt();
			TGLyric lyric = readLyrics();

			readPageSetup();

			int tempoValue = readInt();

			if (getVersion().getVersionCode() > 0) {
				skip(1);
			}

			this.keySignature = readKeySignature();
			this.skip(3);

			readByte(); //octave

			List<TGChannel> channels = readChannels();

			readDirections(song);

			skip(4); // master reverb

			int measures = readInt();
			int tracks = readInt();

			readMeasureHeaders(song, measures);
			readTracks(song, tracks, channels, lyric, lyricTrack);
			readMeasures(song, tempoValue);

			this.close();

			return song;
		}
		catch (GTPFormatException gtpFormatException) {
			throw gtpFormatException;
		}
		catch (Throwable throwable) {
			throw new TGFileFormatException(throwable);
		}
	}

	private void readInfo(TGSong song) throws IOException {
		song.setName(readStringByteSizeOfInteger());
		readStringByteSizeOfInteger();
		song.setArtist(readStringByteSizeOfInteger());
		song.setAlbum(readStringByteSizeOfInteger());
		song.setAuthor(readStringByteSizeOfInteger());
		readStringByteSizeOfInteger();
		song.setCopyright(readStringByteSizeOfInteger());
		song.setWriter(readStringByteSizeOfInteger());
		readStringByteSizeOfInteger();
		int comments = readInt();
		for (int i = 0; i < comments; i++) {
			song.setComments(song.getComments() + readStringByteSizeOfInteger());
		}
	}

	private TGLyric readLyrics() throws IOException {
		TGLyric lyric = getFactory().newLyric();
		lyric.setFrom(readInt());
		lyric.setLyrics(readStringInteger());
		for (int i = 0; i < 4; i++) {
			readInt();
			readStringInteger();
		}
		return lyric;
	}

	private void readPageSetup() throws IOException {
		skip((getVersion().getVersionCode() > 0 ? 49 : 30));
		for (int i = 0; i < 11; i++) {
			skip(4);
			readStringByte(0);
		}
	}

	private int readKeySignature() throws IOException {
		// 0: C 1: G, -1: F
		int keySignature = readByte();
		if (keySignature < 0) {
			keySignature = 7 - keySignature; // translate -1 to 8, etc.
		}

		return keySignature;
	}

	private List<TGChannel> readChannels() throws IOException {
		List<TGChannel> channels = new ArrayList<TGChannel>();
		for (int i = 0; i < 64; i++) {
			TGChannel channel = getFactory().newChannel();
			channel.setProgram((short) readInt());
			channel.setVolume(toChannelShort(readByte()));
			channel.setBalance(toChannelShort(readByte()));
			channel.setChorus(toChannelShort(readByte()));
			channel.setReverb(toChannelShort(readByte()));
			channel.setPhaser(toChannelShort(readByte()));
			channel.setTremolo(toChannelShort(readByte()));
			channel.setBank(i == 9 ? TGChannel.DEFAULT_PERCUSSION_BANK : TGChannel.DEFAULT_BANK);
			if (channel.getProgram() < 0) {
				channel.setProgram((short) 0);
			}
			channels.add(channel);
			skip(2);
		}
		return channels;
	}

	/**
	 * Directions is a list of 19 short integers each pointing at the number of measure.
	 * Directions are read in the following order:
	 * <p>
	 * <ul>
	 * <li> - Coda
	 * <li> - Double Coda
	 * <li> - Segno
	 * <li> - Segno Segno
	 * <li> - Fine
	 * </ul>
	 * <ul>
	 * <li> - Da Capo
	 * <li> - Da Capo al Coda
	 * <li> - Da Capo al Double Coda
	 * <li> - Da Capo al Fine
	 * <li> - Da Segno
	 * <li> - Da Segno al Coda
	 * <li> - Da Segno al Double Coda
	 * <li> - Da Segno al Fine
	 * <li> - Da Segno Segno
	 * <li> - Da Segno Segno al Coda
	 * <li> - Da Segno Segno al Double Coda
	 * <li> - Da Segno Segno al Fine
	 * <li> - Da Coda
	 * <li> - Da Double Coda
	 * <ul>
	 */
	private void readDirections(TGSong song) throws IOException {
		int[] signs = new int[]{
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort()
		};
		int[] fromSigns = new int[]{
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort(),
			readShort()
		};
		song.setSigns(signs);
		song.setFromSigns(fromSigns);
	}

	private void readMeasureHeaders(TGSong song, int count) throws IOException {
		TGTimeSignature timeSignature = getFactory().newTimeSignature();
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				skip(1);
			}
			song.addMeasureHeader(readMeasureHeader(i, timeSignature));
		}
	}

	private void readTracks(TGSong song, int count, List<TGChannel> channels, TGLyric lyric, int lyricTrack) throws IOException {
		for (int number = 1; number <= count; number++) {
			song.addTrack(readTrack(song, number, channels, (number == lyricTrack) ? lyric : getFactory().newLyric()));
		}
		skip((getVersion().getVersionCode() == 0 ? 2 : 1));
	}

	private void readMeasures(TGSong song, int tempoValue) throws IOException {
		TGTempo tempo = getFactory().newTempo();
		tempo.setValue(tempoValue);
		long start = TGDuration.QUARTER_TIME;
		for (TGMeasureHeader header : song.getMeasureHeaders()) {
			header.setStart(start);
			for (TGTrack track : song.getTracks()) {
				TGMeasure measure = getFactory().newMeasure(header);
				track.addMeasure(measure);
				readMeasure(measure, tempo);
			}
			header.getTempo().copyFrom(tempo);
			start += header.getLength();
		}
	}

	private short toChannelShort(byte b) {
		short value = (short) ((b * 8) - 1);
		return (short) Math.max(value, 0);
	}

	private TGMeasureHeader readMeasureHeader(int index, TGTimeSignature timeSignature) throws IOException {
		int flags = readUnsignedByte();
		TGMeasureHeader header = getFactory().newHeader();
		header.setNumber((index + 1));
		header.setStart(0);
		header.getTempo().setValue(120);
		header.setRepeatOpen(((flags & 0x04) != 0));
		if ((flags & 0x01) != 0) {
			timeSignature.setNumerator(readByte());
		}
		if ((flags & 0x02) != 0) {
			timeSignature.getDenominator().setValue(readByte());
		}
		header.getTimeSignature().copyFrom(timeSignature);
		if ((flags & 0x08) != 0) {
			header.setRepeatClose(((readByte() & 0xff) - 1));
		}
		if ((flags & 0x20) != 0) {
			header.setMarker(readMarker(header.getNumber()));
		}
		if ((flags & 0x10) != 0) {
			header.setRepeatAlternative(readUnsignedByte());
		}
		if ((flags & 0x40) != 0) {
			this.keySignature = readKeySignature();
			this.skip(1);
		}
		if ((flags & 0x01) != 0 || (flags & 0x02) != 0) {
			skip(4);
		}
		if ((flags & 0x10) == 0) {
			skip(1);
		}
		int tripletFeel = readByte();
		if (tripletFeel == 1) {
			header.setTripletFeel(TGMeasureHeader.TRIPLET_FEEL_EIGHTH);
		} else if (tripletFeel == 2) {
			header.setTripletFeel(TGMeasureHeader.TRIPLET_FEEL_SIXTEENTH);
		} else {
			header.setTripletFeel(TGMeasureHeader.TRIPLET_FEEL_NONE);
		}
		return header;
	}

	private TGTrack readTrack(TGSong song, int number, List<TGChannel> channels, TGLyric lyrics) throws IOException {
		readUnsignedByte();
		if (number == 1 || getVersion().getVersionCode() == 0) {
			skip(1);
		}
		TGTrack track = getFactory().newTrack();
		track.setNumber(number);
		track.setLyrics(lyrics);
		track.setName(readStringByte(40));
		int stringCount = readInt();
		for (int i = 0; i < 7; i++) {
			int tuning = readInt();
			if (stringCount > i) {
				TGString string = getFactory().newString();
				string.setNumber(i + 1);
				string.setValue(tuning);
				track.getStrings().add(string);
			}
		}
		readInt();
		readChannel(song, track, channels);
		readInt();
		track.setOffset(readInt());
		readColor(track.getColor());
		skip((getVersion().getVersionCode() > 0) ? 49 : 44);
		if (getVersion().getVersionCode() > 0) {
			readStringByteSizeOfInteger();
			readStringByteSizeOfInteger();
		}
		return track;
	}

	private void readMeasure(TGMeasure measure, TGTempo tempo) throws IOException {
		long start = measure.getStart();
		for (TGVoice voice : measure.getVoices()) {
			readVoice(start, tempo, voice);
		}

		skip(1); // LineBreak

		/*List<TGBeat> emptyBeats = new ArrayList<TGBeat>();
		 for (int i = 0; i < measure.countBeats(); i++) {
		 TGBeat beat = measure.getBeat(i);
		 boolean empty = true;
		 for (int v = 0; v < beat.countVoices(); v++) {
		 if (!beat.getVoice(v).isEmpty()) {
		 empty = false;
		 }
		 }
		 if (empty) {
		 emptyBeats.add(beat);
		 }
		 }
		 for (TGBeat beat : emptyBeats) {
		 measure.removeBeat(beat);
		 }*/
		measure.setClef(getClef(measure.getTrack()));
		measure.setKeySignature(this.keySignature);
	}

	private TGMarker readMarker(int measure) throws IOException {
		TGMarker marker = getFactory().newMarker();
		marker.setMeasure(measure);
		marker.setTitle(readStringByteSizeOfInteger());
		readColor(marker.getColor());
		return marker;
	}

	private void readChannel(TGSong song, TGTrack track, List<TGChannel> channels) throws IOException {
		int gmChannel1 = (readInt() - 1);
		int gmChannel2 = (readInt() - 1);
		if (gmChannel1 >= 0 && gmChannel1 < channels.size()) {
			TGChannel channel = getFactory().newChannel();
			TGChannelParameter gmChannel1Param = getFactory().newChannelParameter();
			TGChannelParameter gmChannel2Param = getFactory().newChannelParameter();

			gmChannel1Param.setKey(GMChannelRoute.PARAMETER_GM_CHANNEL_1);
			gmChannel1Param.setValue(Integer.toString(gmChannel1));
			gmChannel2Param.setKey(GMChannelRoute.PARAMETER_GM_CHANNEL_2);
			gmChannel2Param.setValue(Integer.toString(gmChannel1 != 9 ? gmChannel2 : gmChannel1));

			channel.copyFrom(getFactory(), channels.get(gmChannel1));

			//------------------------------------------//
			for (int i = 0; i < song.countChannels(); i++) {
				TGChannel channelAux = song.getChannel(i);
				for (int n = 0; n < channelAux.countParameters(); n++) {
					TGChannelParameter channelParameter = channelAux.getParameter(n);
					if (channelParameter.getKey().equals(GMChannelRoute.PARAMETER_GM_CHANNEL_1)) {
						if (Integer.toString(gmChannel1).equals(channelParameter.getValue())) {
							channel.setChannelId(channelAux.getChannelId());
						}
					}
				}
			}
			if (channel.getChannelId() <= 0) {
				channel.setChannelId(song.countChannels() + 1);
				channel.setName(new TGSongManager(getFactory()).createChannelNameFromProgram(song, channel));
				channel.addParameter(gmChannel1Param);
				channel.addParameter(gmChannel2Param);
				song.addChannel(channel);
			}
			track.setChannelId(channel.getChannelId());
		}
	}

	private void readColor(TGColor color) throws IOException {
		color.setR(readUnsignedByte());
		color.setG(readUnsignedByte());
		color.setB(readUnsignedByte());
		skip(1);
	}

	private void readVoice(long start, TGTempo tempo, TGVoice voice) throws IOException {
		int beats = readInt();
		for (int i = 0; i < beats; i++) {
			start += readBeat(start, tempo, voice);
		}
	}

	private int getClef(TGTrack track) {
		if (!isPercussionChannel(track.getSong(), track.getChannelId())) {
			for (TGString string : track.getStrings()) {
				if (string.getValue() <= 34) {
					return TGMeasure.CLEF_BASS;
				}
			}
		}
		return TGMeasure.CLEF_TREBLE;
	}

	/**
	 * First, beat is read as in Guitar Pro 3: meth:`guitarpro.gp3.readBeat`.
	 * The first byte is the beat flags. It lists the data present in the current beat:
	 * <p>
	 * - *0x01*: dotted notes
	 * - *0x02*: presence of a chord diagram
	 * - *0x04*: presence of a text
	 * - *0x08*: presence of effects
	 * - *0x10*: presence of a mix table change event
	 * - *0x20*: the beat is a n-tuplet
	 * - *0x40*: status: True if the beat is empty of if it is a rest
	 * - *0x80*: *blank*
	 * <p>
	 * Flags are followed by:
	 * <p>
	 * - Status: :ref:`byte`. If flag at *0x40* is true, read one byte.
	 * If value of the byte is *0x00* then beat is empty, if value is
	 * *0x02* then the beat is rest.
	 * <p>
	 * - Beat duration: :ref:`byte`. See :meth:`readDuration`.
	 * <p>
	 * - Chord diagram. See :meth:`readChord`.
	 * <p>
	 * - Text. See :meth:`readText`.
	 * <p>
	 * - Beat effects. See :meth:`readBeatEffects`.
	 * <p>
	 * - Mix table change effect. See :meth:`readMixTableChange`.
	 * <p>
	 * Then it is followed by set of flags stored in :ref:`short`.
	 * <p>
	 * - *0x0001*: break beams
	 * - *0x0002*: direct beams down
	 * - *0x0004*: force beams
	 * - *0x0008*: direct beams up
	 * - *0x0010*: ottava (8va)
	 * - *0x0020*: ottava bassa (8vb)
	 * - *0x0040*: quindicesima (15ma)
	 * - *0x0100*: quindicesima bassa (15mb)
	 * - *0x0200*: start tuplet bracket here
	 * - *0x0400*: end tuplet bracket here
	 * - *0x0800*: break secondary beams
	 * - *0x1000*: break secondary tuplet
	 * - *0x2000*: force tuplet bracket
	 * <p>
	 * - Break secondary beams: :ref:`byte`. Appears if flag at
	 * *0x0800* is set. Signifies how much beams should be broken.
	 */
	private long readBeat(long start, TGTempo tempo, TGVoice voice) throws IOException {
		int flags = readUnsignedByte();
		TGBeat beat = getBeat(voice, start);
		if ((flags & 0x40) != 0) {
			int beatType = readUnsignedByte();
			beat.setType(beatType);
		} else {
			beat.setType(TGBeat.BEAT_NORMAL);
		}
		TGDuration duration = readDuration(flags);
		TGNoteEffect noteEffect = getFactory().newNoteEffect();
		if ((flags & 0x02) != 0) {
			readChord(voice.getMeasure().getTrack().stringCount(), beat);
		}
		if ((flags & 0x04) != 0) {
			readText(beat);
		}
		if ((flags & 0x08) != 0) {
			readBeatEffects(beat.getEffect());
		}
		if ((flags & 0x10) != 0) {
			readMixChange(tempo);
		}

		readNotes(voice.getMeasure().getTrack(), beat, duration, noteEffect);

		skip(1);
		int read = readByte();
		if ((read & 0x08) != 0) {
			skip(1);
		}

		return !(beat.getType() == TGBeat.BEAT_EMPTY) ? duration.getTime() : 0;
	}

	private boolean isPercussionChannel(TGSong song, int channelId) {
		Iterator<TGChannel> it = song.getChannels();
		while (it.hasNext()) {
			TGChannel channel = it.next();
			if (channel.getChannelId() == channelId) {
				return channel.isPercussionChannel();
			}
		}
		return false;
	}

	private TGBeat getBeat(TGVoice voice, long start) {
		for (TGBeat beat : voice.getBeats()) {
			if (beat.getStart() == start) {
				return beat;
			}
		}
		TGBeat newBeat = getFactory().newBeat();
		newBeat.setStart(start);
		voice.addBeat(newBeat);
		return newBeat;
	}

	private TGDuration readDuration(int flags) throws IOException {
		TGDuration duration = getFactory().newDuration();
		duration.setValue((int) (Math.pow(2, (readByte() + 4)) / 4));
		duration.setDotted(((flags & 0x01) != 0));
		if ((flags & 0x20) != 0) {
			int divisionType = readInt();
			switch (divisionType) {
				case 3:
					duration.getDivision().setEnters(3);
					duration.getDivision().setTimes(2);
					break;
				case 5:
					duration.getDivision().setEnters(5);
					duration.getDivision().setTimes(4);
					break;
				case 6:
					duration.getDivision().setEnters(6);
					duration.getDivision().setTimes(4);
					break;
				case 7:
					duration.getDivision().setEnters(7);
					duration.getDivision().setTimes(4);
					break;
				case 9:
					duration.getDivision().setEnters(9);
					duration.getDivision().setTimes(8);
					break;
				case 10:
					duration.getDivision().setEnters(10);
					duration.getDivision().setTimes(8);
					break;
				case 11:
					duration.getDivision().setEnters(11);
					duration.getDivision().setTimes(8);
					break;
				case 12:
					duration.getDivision().setEnters(12);
					duration.getDivision().setTimes(8);
					break;
				case 13:
					duration.getDivision().setEnters(13);
					duration.getDivision().setTimes(8);
					break;
			}
		}
		return duration;
	}

	private void readChord(int strings, TGBeat beat) throws IOException {
		TGChord chord = getFactory().newChord(strings);
		this.skip(17);
		chord.setName(readStringByte(21));
		this.skip(4);
		chord.setFirstFret(readInt());
		for (int i = 0; i < 7; i++) {
			int fret = readInt();
			if (i < chord.countStrings()) {
				chord.addFretValue(i, fret);
			}
		}
		this.skip(32);
		if (chord.countNotes() > 0) {
			beat.setChord(chord);
		}
	}

	private void readText(TGBeat beat) throws IOException {
		TGText text = getFactory().newText();
		text.setValue(readStringByteSizeOfInteger());
		beat.setText(text);
	}

	/**
	 * Read beat effects. Beat effects are read using two byte flags. The first byte of flags is:
	 * <ul>
	 * <li> - *0x01*: *blank*
	 * <li> - *0x02*: wide vibrato
	 * <li> - *0x04*: *blank*
	 * <li> - *0x08*: *blank*
	 * <li> - *0x10*: fade in
	 * <li> - *0x20*: slap effect
	 * <li> - *0x40*: beat stroke
	 * <li> - *0x80*: *blank*
	 * <p><p>
	 * The second byte of flags is:
	 * <p>
	 * <li> - *0x01*: rasgueado
	 * <li> - *0x02*: pick stroke
	 * <li> - *0x04*: tremolo bar
	 * <li> - *0x08*: *blank*
	 * <li> - *0x10*: *blank*
	 * <li> - *0x20*: *blank*
	 * <li> - *0x40*: *blank*
	 * <li> - *0x80*: *blank*
	 * <p><p>
	 * Flags are followed by:
	 * <p>
	 * <li> - Slap effect: :ref:`signed-byte`. For value mapping see: class:`guitarpro.models.SlapEffect`.
	 * <li> - Tremolo bar. See :meth:`readTremoloBar`.
	 * <li> - Beat stroke. See :meth:`readBeatStroke`.
	 * <li> - Pick stroke: :ref:`signed-byte`. For value mapping see: class:`guitarpro.models.BeatStrokeDirection`.
	 * <ul>
	 */
	private void readBeatEffects(TGBeatEffect beatEffect) throws IOException {
		int flags1 = readUnsignedByte();
		int flags2 = readUnsignedByte();
		beatEffect.setVibrato(((flags1 & 0x02) != 0));
		beatEffect.setFadeIn(((flags1 & 0x10) != 0));
		if ((flags1 & 0x20) != 0) {
			int effect = readUnsignedByte();
			beatEffect.setTapping(effect == 1);
			beatEffect.setSlapping(effect == 2);
			beatEffect.setPopping(effect == 3);
		}
		if ((flags2 & 0x04) != 0) {
			readTremoloBar(beatEffect);
		}
		if ((flags1 & 0x40) != 0) {
			int strokeUp = readByte();
			int strokeDown = readByte();
			if (strokeUp > 0) {
				beatEffect.getStroke().setDirection(TGStroke.STROKE_UP);
				beatEffect.getStroke().setValue(toStrokeValue(strokeUp));
			} else if (strokeDown > 0) {
				beatEffect.getStroke().setDirection(TGStroke.STROKE_DOWN);
				beatEffect.getStroke().setValue(toStrokeValue(strokeDown));
			}
		}
		if ((flags2 & 0x01) != 0) {
			beatEffect.setRasgueado(true);
		}
		if ((flags2 & 0x02) != 0) {
			beatEffect.setPickStroke(readByte());
		}
	}

	private void readMixChange(TGTempo tempo) throws IOException {
		readByte(); //instrument

		skip(16);
		int volume = readByte();
		int pan = readByte();
		int chorus = readByte();
		int reverb = readByte();
		int phaser = readByte();
		int tremolo = readByte();
		readStringByteSizeOfInteger(); //tempoName
		int tempoValue = readInt();
		if (volume >= 0) {
			readByte();
		}
		if (pan >= 0) {
			readByte();
		}
		if (chorus >= 0) {
			readByte();
		}
		if (reverb >= 0) {
			readByte();
		}
		if (phaser >= 0) {
			readByte();
		}
		if (tremolo >= 0) {
			readByte();
		}
		if (tempoValue >= 0) {
			tempo.setValue(tempoValue);
			skip(1);
			if (getVersion().getVersionCode() > 0) {
				skip(1);
			}
		}
		readByte();
		skip(1);
		if (getVersion().getVersionCode() > 0) {
			readStringByteSizeOfInteger();
			readStringByteSizeOfInteger();
		}
	}

	/**
	 * Read notes.
	 * <p>
	 * First byte lists played strings:
	 * <p>
	 * - *0x01*: 7th string
	 * - *0x02*: 6th string
	 * - *0x04*: 5th string
	 * - *0x08*: 4th string
	 * - *0x10*: 3th string
	 * - *0x20*: 2th string
	 * - *0x40*: 1th string
	 * - *0x80*: *blank*
	 */

	private void readNotes(TGTrack track, TGBeat beat, TGDuration duration, TGNoteEffect noteEffect) throws IOException {
		int stringFlags = readUnsignedByte();
		for (int i = 6; i >= 0; i--) {
			if ((stringFlags & (1 << i)) != 0 && (6 - i) < track.stringCount()) {
				TGString string = track.getString((6 - i) + 1).clone(getFactory());
				TGNote note = readNote(string, track, noteEffect);
				beat.addNote(note);
			}
			beat.getDuration().copyFrom(duration);
		}
	}

	private void readTremoloBar(TGBeatEffect effect) throws IOException {
		skip(5);
		TGEffectTremoloBar tremoloBar = getFactory().newEffectTremoloBar();
		int numPoints = readInt();
		for (int i = 0; i < numPoints; i++) {
			int position = readInt();
			int value = readInt();
			boolean vibrato = readBoolean();

			float pointPosition = position * TGEffectTremoloBar.MAX_POSITION_LENGTH / GP_BEND_POSITION;
			int pointValue = Math.round(value / (GP_BEND_SEMITONE * 2f));
			tremoloBar.addPoint(pointPosition, pointValue, vibrato);
		}
		if (!tremoloBar.getPoints().isEmpty()) {
			effect.setTremoloBar(tremoloBar);
		}
	}

	private int toStrokeValue(int value) {
		if (value == 1) {
			return TGDuration.HUNDRED_TWENTY_EIGHT;
		} else if (value == 2) {
			return TGDuration.SIXTY_FOURTH;
		} else if (value == 3) {
			return TGDuration.THIRTY_SECOND;
		} else if (value == 4) {
			return TGDuration.SIXTEENTH;
		} else if (value == 5) {
			return TGDuration.EIGHTH;
		} else if (value == 6) {
			return TGDuration.QUARTER;
		}
		return TGDuration.SIXTY_FOURTH;
	}

	private TGNote readNote(TGString string, TGTrack track, TGNoteEffect effect) throws IOException {
		int flags = readUnsignedByte();
		TGNote note = getFactory().newNote();
		note.setString(string.getNumber());
		note.setEffect(effect);
		note.getEffect().setAccentuatedNote(((flags & 0x40) != 0));
		note.getEffect().setHeavyAccentuatedNote(((flags & 0x02) != 0));
		note.getEffect().setGhostNote(((flags & 0x04) != 0));
		if ((flags & 0x20) != 0) {
			int noteType = readUnsignedByte();
			note.setTiedNote((noteType == 0x02));
			note.getEffect().setDeadNote((noteType == 0x03));
		}
		if ((flags & 0x10) != 0) {
			note.setVelocity((TGVelocities.MIN_VELOCITY + (TGVelocities.VELOCITY_INCREMENT * readByte())) - TGVelocities.VELOCITY_INCREMENT);
		}
		if ((flags & 0x20) != 0) {
			int fret = readByte();
			int value = (note.isTiedNote() ? getTiedNoteValue(string.getNumber(), track) : fret);
			note.setValue(value >= 0 && value < 100 ? value : 0);
		}
		if ((flags & 0x80) != 0) {
			skip(2);
		}
		if ((flags & 0x01) != 0) {
			skip(8);
		}
		skip(1);
		if ((flags & 0x08) != 0) {
			readNoteEffects(note.getEffect());
		}
		return note;
	}

	private int getTiedNoteValue(int string, TGTrack track) {
		int measureCount = track.countMeasures();
		if (measureCount > 0) {
			for (int m = measureCount - 1; m >= 0; m--) {
				TGMeasure measure = track.getMeasure(m);
				for (int v = 0; v < measure.countVoices(); v++) {
					TGVoice voice = measure.getVoice(v);
					int countsBeat = voice.countBeats();
					if (countsBeat > 0) {
						for (int b = countsBeat - 1; b >= 0; b--) {
							TGBeat beat = voice.getBeat(b);
							if (!beat.isEmpty()) {
								for (int n = 0; n < beat.countNotes(); n++) {
									TGNote note = beat.getNote(n);
									if (note.getString() == string) {
										return note.getValue();
									}
								}
							}
						}
					}
				}
			}
		}
		return -1;
	}

	private void readNoteEffects(TGNoteEffect noteEffect) throws IOException {
		int flags1 = readUnsignedByte();
		int flags2 = readUnsignedByte();
		if ((flags1 & 0x01) != 0) {
			readBend(noteEffect);
		}
		if ((flags1 & 0x10) != 0) {
			readGrace(noteEffect);
		}
		if ((flags2 & 0x04) != 0) {
			readTremoloPicking(noteEffect);
		}
		if ((flags2 & 0x08) != 0) {
			int slideType = readByte();
			noteEffect.setSlideType(slideType);
		}
		if ((flags2 & 0x10) != 0) {
			readArtificialHarmonic(noteEffect);
		}
		if ((flags2 & 0x20) != 0) {
			readTrill(noteEffect);
		}
		noteEffect.setHammer(((flags1 & 0x02) != 0));
		noteEffect.setLetRing(((flags1 & 0x08) != 0));
		noteEffect.setVibrato(((flags2 & 0x40) != 0) || noteEffect.isVibrato());
		noteEffect.setPalmMute(((flags2 & 0x02) != 0));
		noteEffect.setStaccato(((flags2 & 0x01) != 0));
	}

	private void readBend(TGNoteEffect effect) throws IOException {
		skip(5);
		TGEffectBend bend = getFactory().newEffectBend();
		int numPoints = readInt();
		for (int i = 0; i < numPoints; i++) {
			int bendPosition = readInt();
			int bendValue = readInt();
			boolean vibrato = readBoolean();

			float pointPosition = bendPosition * TGEffectBend.MAX_POSITION_LENGTH / GP_BEND_POSITION;
			int pointValue = Math.round(bendValue * TGEffectBend.SEMITONE_LENGTH / GP_BEND_SEMITONE);
			bend.addPoint(pointPosition, pointValue, vibrato);
		}
		if (!bend.getPoints().isEmpty()) {
			effect.setBend(bend);
		}
	}

	private void readGrace(TGNoteEffect effect) throws IOException {
		int fret = readUnsignedByte();
		int dynamic = readUnsignedByte();
		int transition = readByte();
		int duration = readUnsignedByte();
		int flags = readUnsignedByte();
		TGEffectGrace grace = getFactory().newEffectGrace();
		grace.setFret(fret);
		grace.setDynamic((TGVelocities.MIN_VELOCITY + (TGVelocities.VELOCITY_INCREMENT * dynamic)) - TGVelocities.VELOCITY_INCREMENT);
		grace.setDuration(duration);
		grace.setDead((flags & 0x01) != 0);
		grace.setOnBeat((flags & 0x02) != 0);
		if (transition == 0) {
			grace.setTransition(TGEffectGrace.TRANSITION_NONE);
		} else if (transition == 1) {
			grace.setTransition(TGEffectGrace.TRANSITION_SLIDE);
		} else if (transition == 2) {
			grace.setTransition(TGEffectGrace.TRANSITION_BEND);
		} else if (transition == 3) {
			grace.setTransition(TGEffectGrace.TRANSITION_HAMMER);
		}
		effect.setGrace(grace);
	}

	private void readTremoloPicking(TGNoteEffect effect) throws IOException {
		int value = readUnsignedByte();
		TGEffectTremoloPicking tp = getFactory().newEffectTremoloPicking();
		if (value == 1) {
			tp.getDuration().setValue(TGDuration.EIGHTH);
			effect.setTremoloPicking(tp);
		} else if (value == 2) {
			tp.getDuration().setValue(TGDuration.SIXTEENTH);
			effect.setTremoloPicking(tp);
		} else if (value == 3) {
			tp.getDuration().setValue(TGDuration.THIRTY_SECOND);
			effect.setTremoloPicking(tp);
		}
	}

	private void readArtificialHarmonic(TGNoteEffect effect) throws IOException {
		int type = readByte();
		TGEffectHarmonic harmonic = getFactory().newEffectHarmonic();
		harmonic.setData(0);
		if (type == 1) {
			harmonic.setType(TGEffectHarmonic.TYPE_NATURAL);
			effect.setHarmonic(harmonic);
		} else if (type == 2) {
			skip(3);
			harmonic.setType(TGEffectHarmonic.TYPE_ARTIFICIAL);
			effect.setHarmonic(harmonic);
		} else if (type == 3) {
			skip(1);
			harmonic.setType(TGEffectHarmonic.TYPE_TAPPED);
			effect.setHarmonic(harmonic);
		} else if (type == 4) {
			harmonic.setType(TGEffectHarmonic.TYPE_PINCH);
			effect.setHarmonic(harmonic);
		} else if (type == 5) {
			harmonic.setType(TGEffectHarmonic.TYPE_SEMI);
			effect.setHarmonic(harmonic);
		}
	}

	private void readTrill(TGNoteEffect effect) throws IOException {
		byte fret = readByte();
		byte period = readByte();
		TGEffectTrill trill = getFactory().newEffectTrill();
		trill.setFret(fret);
		if (period == 1) {
			trill.getDuration().setValue(TGDuration.SIXTEENTH);
			effect.setTrill(trill);
		} else if (period == 2) {
			trill.getDuration().setValue(TGDuration.THIRTY_SECOND);
			effect.setTrill(trill);
		} else if (period == 3) {
			trill.getDuration().setValue(TGDuration.SIXTY_FOURTH);
			effect.setTrill(trill);
		}
	}
}
