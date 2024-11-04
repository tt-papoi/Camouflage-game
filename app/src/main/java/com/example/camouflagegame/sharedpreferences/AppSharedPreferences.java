package com.example.camouflagegame.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreferences {
    private static final String APP_SHARED_PREFERENCES = "APP_SHARED_PREFERENCES";
    private static final String KEY_MUSIC_STATUS = "KEY_MUSIC_STATUS";
    private static final String KEY_SOUND_STATUS = "KEY_SOUND_STATUS";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;

    public AppSharedPreferences(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(APP_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void putMusicStatus(boolean isMusicOn) {
        editor.putBoolean(KEY_MUSIC_STATUS, isMusicOn);
        editor.apply();
    }

    public boolean getMusicStatus() {
        return sharedPreferences.getBoolean(KEY_MUSIC_STATUS, true);
    }

    public void putSoundStatus(boolean isSoundOn) {
        editor.putBoolean(KEY_SOUND_STATUS, isSoundOn);
        editor.apply();
    }

    public boolean getSoundStatus() {
        return sharedPreferences.getBoolean(KEY_SOUND_STATUS, true);
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}
