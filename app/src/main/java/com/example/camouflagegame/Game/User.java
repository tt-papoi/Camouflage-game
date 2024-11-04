package com.example.camouflagegame.Game;

import android.content.Context;
import android.content.SharedPreferences;

public class User {
    private String userID;
    private int level;
    private int exp;

    public User() {
    }

    public User(String userID, int level, int exp){
        this.userID=userID;
        this.level=level;
        this.exp=exp;
    }

    // Ví dụ phương thức để lưu dữ liệu User vào SharedPreferences
    public void saveToSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("id", userID);
        editor.putInt("exp", exp);
        editor.putInt("level", level);
        editor.apply();
    }

    // Ví dụ phương thức để đọc dữ liệu User từ SharedPreferences
    public static User loadFromSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        User user = new User();
        user.setUserID(prefs.getString("id", "0"));
        user.setExp(prefs.getInt("exp", 0));
        user.setLevel(prefs.getInt("level", 0));
        return user;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", level=" + level +
                ", exp=" + exp +
                '}';
    }
}
