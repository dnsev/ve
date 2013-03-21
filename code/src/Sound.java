package com.github.dnsev.videncode;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.Clip;



class Sound {
	private boolean valid = false;
	private Clip clip = null;
	private AudioFormat audioFormat = null;

	public Sound(final File source) {
		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(source);
			this.clip = AudioSystem.getClip();
			this.clip.open(audioStream);
		}
		catch (UnsupportedAudioFileException e) {
			return;
		}
		catch (IOException e) {
			return;
		}
		catch (LineUnavailableException e) {
			return;
		}

		this.valid = true;
	}

	public final void play() {
		if (!this.valid) return;

		this.stop();
		this.clip.start();
	}
	public final void stop() {
		this.clip.stop();
		this.clip.setFramePosition(0);
	}
}


