package com.example.camouflagegame.Commander;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.camouflagegame.Game.User;
import com.example.camouflagegame.R;

public class Commander {
    private String id;
    private String uidSkill;
    private String name;
    private int thumbnailID;
    private boolean isLock = true;
    private int openLevel;


    public Commander(String id, String uidSkill, String name, int thumbnailID, int openLevel) {
        this.id = id;
        this.uidSkill = uidSkill;
        this.name = name;
        this.thumbnailID = thumbnailID;
        this.openLevel = openLevel;
    }

    public Commander(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThumbnailID() {
        return thumbnailID;
    }

    public void setThumbnailID(int thumbnailID) {
        this.thumbnailID = thumbnailID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUidSkill() {
        return uidSkill;
    }

    public void setUidSkill(String uidSkill) {
        this.uidSkill = uidSkill;
    }

    public void setLock(boolean lock) {
        isLock = lock;
    }

    public boolean getIsLock(){
        return  isLock;
    }

    public int getOpenLevel() {
        return openLevel;
    }

    public void setOpenLevel(int openLevel) {
        this.openLevel = openLevel;
    }

    public void saveToSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("CommanderPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("id", id);
        editor.putString("uidSkill", uidSkill);
        editor.putString("name", name);
        editor.putInt("thumbnailID", thumbnailID);
        editor.putBoolean("isLock", isLock);
        editor.putInt("openLevel", openLevel);
        editor.apply();
    }

    public static Commander loadFromSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("CommanderPrefs", Context.MODE_PRIVATE);
        Commander commander = new Commander();
        commander.setId(prefs.getString("id", "002"));
        commander.setUidSkill(prefs.getString("uidSkill","002"));
        commander.setName(prefs.getString("name", "Napole Ponale"));
        commander.setThumbnailID(prefs.getInt("thumbnailID", R.drawable.commander02));
        commander.setLock(prefs.getBoolean("isLock", false));
        commander.setOpenLevel(prefs.getInt("openLevel", 0));
        return commander;
    }
}
