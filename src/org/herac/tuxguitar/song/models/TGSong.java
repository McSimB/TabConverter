package org.herac.tuxguitar.song.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class TGSong {

	private String name;
	private String artist;
	private String album;
	private String author;
	private String date;
	private String copyright;
	private String writer;
	private String transcriber;
	private String comments;
	private List<TGTrack> tracks;
	private List<TGMeasureHeader> measureHeaders;
	private List<TGChannel> channels;
	private int[] fromSigns;
	private int[] signs;

	public TGSong() {
		this.name = "";
		this.artist = "";
		this.album = "";
		this.author = "";
		this.date = "";
		this.copyright = "";
		this.writer = "";
		this.transcriber = "";
		this.comments = "";
		this.tracks = new ArrayList<TGTrack>();
		this.channels = new ArrayList<TGChannel>();
		this.measureHeaders = new ArrayList<TGMeasureHeader>();
		this.signs = new int[]{-1, -1, -1, -1, -1};
		this.fromSigns = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlbum() {
		return this.album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getArtist() {
		return this.artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getDate() {
		return this.date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCopyright() {
		return this.copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getWriter() {
		return this.writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public String getTranscriber() {
		return this.transcriber;
	}

	public void setTranscriber(String transcriber) {
		this.transcriber = transcriber;
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public int[] getFromSigns() {
		return this.fromSigns;
	}

	public void setFromSigns(int[] fromSigns) {
		this.fromSigns = fromSigns;
	}

	public int[] getSigns() {
		return this.signs;
	}

	public void setSigns(int[] signs) {
		this.signs = signs;
	}

	public void addMeasureHeader(TGMeasureHeader measureHeader) {
		this.addMeasureHeader(countMeasureHeaders(), measureHeader);
	}

	public void addMeasureHeader(int index, TGMeasureHeader measureHeader) {
		measureHeader.setSong(this);
		this.measureHeaders.add(index, measureHeader);
	}

	public int countMeasureHeaders() {
		return this.measureHeaders.size();
	}

	public void removeMeasureHeader(int index) {
		this.measureHeaders.remove(index);
	}

	public void removeMeasureHeader(TGMeasureHeader measureHeader) {
		this.measureHeaders.remove(measureHeader);
	}

	public TGMeasureHeader getMeasureHeader(int index) {
		return this.measureHeaders.get(index);
	}

	public List<TGMeasureHeader> getMeasureHeaders() {
		return this.measureHeaders;
	}

	public void addTrack(TGTrack track) {
		this.addTrack(countTracks(), track);
	}

	public void addTrack(int index, TGTrack track) {
		track.setSong(this);
		this.tracks.add(index, track);
	}

	public int countTracks() {
		return this.tracks.size();
	}

	public void moveTrack(int index, TGTrack track) {
		this.tracks.remove(track);
		this.tracks.add(index, track);
	}

	public TGTrack getTrack(int index) {
		return this.tracks.get(index);
	}

	public List<TGTrack> getTracks() {
		return this.tracks;
	}

	public void addChannel(TGChannel channel) {
		this.addChannel(countChannels(), channel);
	}

	public void addChannel(int index, TGChannel channel) {
		this.channels.add(index, channel);
	}

	public int countChannels() {
		return this.channels.size();
	}

	public void removeChannel(TGChannel channel) {
		this.channels.remove(channel);
	}

	public TGChannel getChannel(int index) {
		return this.channels.get(index);
	}

	public Iterator<TGChannel> getChannels() {
		return this.channels.iterator();
	}

	public boolean isEmpty() {
		return (countMeasureHeaders() == 0 || countTracks() == 0);
	}
}
