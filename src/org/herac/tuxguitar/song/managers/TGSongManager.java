package org.herac.tuxguitar.song.managers;

import org.herac.tuxguitar.song.factory.TGFactory;
import org.herac.tuxguitar.song.models.TGChannel;
import org.herac.tuxguitar.song.models.TGChannelNames;
import org.herac.tuxguitar.song.models.TGSong;
import org.herac.tuxguitar.song.models.TGTrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TGSongManager {

	public static final short MAX_CHANNELS = 16;

	public static final int[][] DEFAULT_TUNING_VALUES = {
			{43, 38, 33, 28},
			{43, 38, 33, 28, 23},
			{64, 59, 55, 50, 45, 40},
			{64, 59, 55, 50, 45, 40, 35},
	};

	private TGFactory factory;

	public TGSongManager() {
		this(new TGFactory());
	}

	public TGSongManager(TGFactory factory) {
		this.factory = factory;
	}

	public TGFactory getFactory() {
		return this.factory;
	}

	public void setFactory(TGFactory factory) {
		this.factory = factory;
	}

	public TGChannel getChannel(TGSong song, int channelId) {
		Iterator<TGChannel> it = song.getChannels();
		while (it.hasNext()) {
			TGChannel channel = it.next();
			if (channel.getChannelId() == channelId) {
				return channel;
			}
		}
		return null;
	}

	public String createChannelNameFromProgram(TGSong song, TGChannel channel) {
		if (channel.getProgram() >= 0 && channel.getProgram() < TGChannelNames.DEFAULT_NAMES.length) {
			return this.createChannelName(song, TGChannelNames.DEFAULT_NAMES[channel.getProgram()]);
		}
		return this.createDefaultChannelName(song);
	}

	public String createChannelName(TGSong song, String prefix) {
		int number = 0;

		String unusedName = null;
		while (unusedName == null) {
			number++;
			String name = (prefix + " " + number);
			if (this.findChannelsByName(song, name).isEmpty()) {
				unusedName = name;
			}
		}
		return unusedName;
	}

	public String createDefaultChannelName(TGSong song) {
		return this.createChannelName(song, "Unnamed");
	}

	public List<TGChannel> findChannelsByName(TGSong song, String name) {
		List<TGChannel> channels = new ArrayList<TGChannel>();

		Iterator<TGChannel> it = song.getChannels();
		while (it.hasNext()) {
			TGChannel channel = it.next();
			if (channel.getName().equals(name)) {
				channels.add(channel);
			}
		}

		return channels;
	}
}