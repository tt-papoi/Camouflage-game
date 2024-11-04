package com.example.camouflagegame.Game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import com.example.camouflagegame.R;

public class AudioController {
    private static MediaPlayer mediaPlayer;
    private static SoundPool soundPool;
    private static int hitSound, missSound;

    public static void playMusic(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.music);
            mediaPlayer.setLooping(true);
        }

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public static void stopMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public static void initSoundPool(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        }

        hitSound = soundPool.load(context, R.raw.explosion, 1);
        missSound = soundPool.load(context, R.raw.missed_explision, 1);
    }

    public static void playHitSound() {
        if (soundPool != null) {
            soundPool.play(hitSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public static void playMissSound() {
        if (soundPool != null) {
            soundPool.play(missSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public static void releaseSoundPool() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}

