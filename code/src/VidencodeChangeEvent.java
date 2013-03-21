package com.github.dnsev.videncode;



public class VidencodeChangeEvent {
	public enum EventType {
		VIDEO_FILE_CHANGED,
		IMAGE_FILE_CHANGED,
		AUDIO_FILE_CHANGED,
		VIDEO_PREVIEW_CHANGED,
		AUDIO_PREVIEW_CHANGED,

		VIDEO_FILE_ENCODING_PROGRESS,
		AUDIO_FILE_ENCODING_PROGRESS,

		IMAGE_FILE_ENCODING_LOG,
		VIDEO_FILE_ENCODING_LOG,
		AUDIO_FILE_ENCODING_LOG,

		ENCODE_STATUS,
	};

	public static final int SOURCE = 1;
	public static final int TEMPORARY = 2;
	public static final int UPDATING = 4;
	public static final int FIRST = 8;
	public static final int LAST = 16;
	public static final int CLEAR = 32;
	public static final int ERROR = 64;
	public static final int MUXING = 128;
	public static final int RESET = 256;
	public static final int COMPLETE = 512;

	private Videncode videncode = null;
	private EventType eventType;
	private int flags = 0;
	private double value = 0.0;
	private String[] text = new String[]{ null , null , null };

	public VidencodeChangeEvent(final Videncode videncode, final EventType eventType, final int flags) {
		this.videncode = videncode;
		this.eventType = eventType;
		this.flags = flags;
	}

	public final Videncode getVidencode() {
		return this.videncode;
	}

	public final EventType getEventType() {
		return this.eventType;
	}

	public final int getEventFlags() {
		return this.flags;
	}

	public final double getValue() {
		return this.value;
	}
	public final VidencodeChangeEvent setValue(double value) {
		this.value = value;

		return this;
	}

	public final String getText(int id) {
		return this.text[id];
	}
	public final VidencodeChangeEvent setText(int id, String text) {
		this.text[id] = text;

		return this;
	}

}


