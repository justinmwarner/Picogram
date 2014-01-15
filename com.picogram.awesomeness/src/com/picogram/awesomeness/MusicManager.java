
package com.picogram.awesomeness;

import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;

//From: http://www.rbgrn.net/content/307-light-racer-20-days-61-64-completion
public class MusicManager {
	private static final String TAG = "MusicManager";
	public static final int MUSIC_PREVIOUS = -1;
	public static final int MUSIC_MENU = 0;
	public static final int MUSIC_GAME = 1;
	public static final int MUSIC_END_GAME = 2;

	private static HashMap players = new HashMap();
	private static int currentMusic = -1;
	private static int previousMusic = -1;

	public static float getMusicVolume(Context context) {
		return new Float(1).floatValue();
	}

	public static void start(Context context) {
		start(context, Util.getPreferences(context).getString("music", "None"));
	}

	public static void start(Context context, String music) {
		start(context, music, false);
	}

	public static void start(Context context, String music, boolean force) {
		if (!force && currentMusic > -1) {
			// already playing some music and not forced to change
			return;
		}

		int prefsMusicId = 0;
		if (music.equals("Classical")) {
			prefsMusicId = 1;
		} else if (music.equals("Dubwub")) {
			prefsMusicId = 2;
		} else if (music.equals("Synth")) {
			prefsMusicId = 3;
		} else if (music.equals("Country")) {
			prefsMusicId = 4;
		}
		if (currentMusic == prefsMusicId) {
			// already playing this music
			return;
		}
		if (currentMusic != -1) {
			previousMusic = currentMusic;
			// Log.d(TAG, "Previous music was [" + previousMusic + "]");
			// playing some other music, pause it and change
			pause();
		}
		currentMusic = prefsMusicId;
		// Log.d(TAG, "Current music is now [" + currentMusic + "]");
		MediaPlayer mp = (MediaPlayer) players.get(music);
		if (mp != null) {
			if (!mp.isPlaying()) {
				mp.start();
			}
		} else {
			if (music.equals("Classical")) {
				mp = MediaPlayer.create(context, R.raw.classical);
			} else if (music.equals("Dubwub")) {
				mp = MediaPlayer.create(context, R.raw.dubstep);
			} else if (music.equals("Synth")) {
				mp = MediaPlayer.create(context, R.raw.chill);
			} else if (music.equals("Country")) {
				mp = MediaPlayer.create(context, R.raw.country);
			} else {
				Log.e(TAG, "unsupported music number - " + music);
				return;
			}
			players.put(music, mp);
			float volume = getMusicVolume(context);
			// Log.d(TAG, "Setting music volume to " + volume);
			mp.setVolume(volume, volume);
			if (mp == null) {
				Log.e(TAG, "player was not created successfully");
			} else {
				try {
					mp.setLooping(true);
					mp.start();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
	}

	public static void pause() {
		Collection<MediaPlayer> mps = players.values();
		for (MediaPlayer p : mps) {
			if (p.isPlaying()) {
				p.pause();
			}
		}
		// previousMusic should always be something valid
		if (currentMusic != -1) {
			previousMusic = currentMusic;
			// Log.d(TAG, "Previous music was [" + previousMusic + "]");
		}
		currentMusic = -1;
		// Log.d(TAG, "Current music is now [" + currentMusic + "]");
	}

	public static void updateVolumeFromPrefs(Context context) {
		try {
			float volume = getMusicVolume(context);
			// Log.d(TAG, "Setting music volume to " + volume);
			Collection<MediaPlayer> mps = players.values();
			for (MediaPlayer p : mps) {
				p.setVolume(volume, volume);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public static void release() {
		Log.d(TAG, "Releasing media players");
		Collection<MediaPlayer> mps = players.values();
		for (MediaPlayer mp : mps) {
			try {
				if (mp != null) {
					if (mp.isPlaying()) {
						mp.stop();
					}
					mp.release();
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		mps.clear();
		if (currentMusic != -1) {
			previousMusic = currentMusic;
			// Log.d(TAG, "Previous music was [" + previousMusic + "]");
		}
		currentMusic = -1;
		// Log.d(TAG, "Current music is now [" + currentMusic + "]");
	}
}
