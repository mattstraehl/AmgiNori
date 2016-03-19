package com.matthias.android.amginori;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;

public final class AudioPlayer {

    private static final String TAG = "AudioPlayer";

    private static final String SOUNDS_FOLDER = "sounds";
    private static final int MAX_SOUNDS = 5;

    private final AssetManager mAssets;
    private final SoundPool mSoundPool;
    private Sound mMatch;
    private Sound mClash;

    public AudioPlayer(Context context) {
        mAssets = context.getAssets();
        // This old constructor is deprecated but use it for compatibility.
        //noinspection deprecation
        mSoundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        loadSounds();
    }

    public void playMatchSound() {
        play(mMatch);
    }

    public void playClashSound() {
        play(mClash);
    }

    private void play(Sound sound) {
        Integer soundId = sound.getSoundId();
        if (soundId == null) {
            return;
        }
        mSoundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    private void loadSounds() {
        try {
            mMatch = load(SOUNDS_FOLDER + "/match.wav");
        } catch (IOException ioe) {
            Log.e(TAG, "Could not list assets", ioe);
        }
        try {
            mClash = load(SOUNDS_FOLDER + "/clash.wav");
        } catch (IOException ioe) {
            Log.e(TAG, "Could not list assets", ioe);
        }
    }

    public void release() {
        mSoundPool.release();
    }

    private Sound load(String assetPath) throws IOException {
        Sound sound = new Sound(assetPath);
        AssetFileDescriptor afd = mAssets.openFd(sound.getAssetPath());
        int soundId = mSoundPool.load(afd, 1);
        sound.setSoundId(soundId);
        return sound;
    }
}
