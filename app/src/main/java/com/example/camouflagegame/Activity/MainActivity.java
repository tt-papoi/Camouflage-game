package com.example.camouflagegame.Activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.camouflagegame.Commander.Commander;
import com.example.camouflagegame.Game.AudioController;
import com.example.camouflagegame.Game.User;
import com.example.camouflagegame.R;
import com.example.camouflagegame.sharedpreferences.AppSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnPlay, btnGuide, btnCommander, btnSetting;
    TextView txtExp;
    TextView txtLevel;
    ProgressBar expProgressBar;
    ImageView commanderImgView;
    private Handler handler;
    private Commander commander;
    private static final int REQUEST_CODE_COMMANDER = 100;
    public static Context mainActivityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        mainActivityContext = this;

        AudioController.playMusic(mainActivityContext);
        AppSharedPreferences appSharedPreferences = new AppSharedPreferences(this);

        if(appSharedPreferences.getMusicStatus()){
            AudioController.playMusic(mainActivityContext);
        }
        else{
            AudioController.stopMusic();
        }
        AudioController.initSoundPool(mainActivityContext);

        /*commander = new Commander("Napole Ponale", R.drawable.commander02);
        commander.saveToSharedPreferences(this, commander.getName(), commander.getThumbnailID());*/

        btnPlay = findViewById(R.id.btnPlay);
        btnGuide = findViewById(R.id.btnGuide);
        btnCommander = findViewById(R.id.btnCommander);
        btnSetting = findViewById(R.id.btnSetting);
        txtExp = findViewById(R.id.expTextView);
        txtLevel = findViewById(R.id.levelTextView);
        expProgressBar = findViewById(R.id.expProgressBar);
        commanderImgView = findViewById(R.id.imageViewCommander_mainScreen);

        //commanderImgView.setImageResource(commander.getThumbnailID());
        EventBus.getDefault().register(this);

        loadDataFromRef();

        ClickPlay();

        ClickGuide();

        ClickCommander();

        ClickSettings();

        // test sound effect

        loadDataFromRef();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loadDataFromRef();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String event){
        if(event.equals("loadDataFromRef")){
            loadDataFromRef();
        }
    }

    public void loadDataFromRef(){
        User user = User.loadFromSharedPreferences(this);
        txtExp.setText("EXP: " + user.getExp());
        txtLevel.setText("Level: " + user.getLevel());
        loadExpProgressBar(user.getExp());

        commander = Commander.loadFromSharedPreferences(this);
        if (user.getLevel() >= commander.getOpenLevel()){
            commander.setLock(false);
        }
        else {
            commander.setLock(true);
        }

        if (commander.getIsLock() == true){
            commander = new Commander("002", "002", "Napole Ponale", R.drawable.commander02, 0);
            commander.saveToSharedPreferences(this);
        }

        commanderImgView.setImageResource(commander.getThumbnailID());
    }

    public void increaseExpFromRef(int expNumber){
        User user = User.loadFromSharedPreferences(this);
        int currentExperience = user.getExp();
        int currentLevel = user.getLevel();

        int newExperience = currentExperience + expNumber;

        currentLevel = newExperience / 100;

        // Cập nhật kinh nghiệm và cấp độ mới vào Ref
        user.setExp(newExperience);
        user.setLevel(currentLevel);
        user.saveToSharedPreferences(this);

        txtExp.setText("EXP: " + user.getExp());
        txtLevel.setText("Level: " + user.getLevel());
        increaseExpProgressBar(newExperience, currentExperience);
    }

    public void loadExpProgressBar(int currentExp){
        final int MAX = 100;
        expProgressBar.setMax(MAX);
        final int currentExpPer100 = currentExp % 100;

        expProgressBar.setProgress(currentExpPer100);

    }
    public void increaseExpProgressBar(int currentExp, int beforeExp){
        handler = new Handler();
        final int MAX = 100;
        expProgressBar.setMax(MAX);
        int currentExpPer100 = currentExp % 100;
        int beforeExpPer100 = beforeExp % 100;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(beforeExpPer100 <= currentExpPer100) {
                    for (int i = beforeExpPer100; i <= currentExpPer100; i++) {
                        final int progress = i;
                        SystemClock.sleep(30);

                        //update interface
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                expProgressBar.setProgress(progress);
                            }
                        });
                    }
                }
                else{
                    for (int i = beforeExpPer100; i <= 100; i++) {
                        final int progress = i;
                        SystemClock.sleep(30);

                        //update interface
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                expProgressBar.setProgress(progress);
                            }
                        });
                    }

                    for (int i = 0; i <= currentExpPer100; i++) {
                        final int progress = i;
                        SystemClock.sleep(30);

                        //update interface
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                expProgressBar.setProgress(progress);
                            }
                        });
                    }
                }
            }
        });
        thread.start();
    }

    private void ClickPlay(){
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LobbyActivity.class);
                startActivity(intent);
            }
        });
    }

    private void ClickGuide(){
        btnGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GuideActivity.class);
                startActivity(intent);
            }
        });
    }

    private void ClickCommander(){
        btnCommander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CommanderActivity.class );
                intent.putExtra("COMMANDER_THUMNAILID", Integer.toString(commander.getThumbnailID()));
                intent.putExtra("COMMANDER_NAME", commander.getName());
                startActivityForResult(intent, REQUEST_CODE_COMMANDER);
            }
        });
    }

    private void ClickSettings(){
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class );
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_COMMANDER && resultCode == RESULT_OK && data != null) {
            String selectedCommanderThumbnail = data.getStringExtra("SELECTED_COMMANDER_THUMBNAIL");
            String selectedCommanderName = data.getStringExtra("SELECTED_COMMANDER_NAME");

            // Update UI with the selected commander data
            commander.setThumbnailID(Integer.parseInt(selectedCommanderThumbnail));
            commander.setName(selectedCommanderName);
            commander.saveToSharedPreferences(this);
            commanderImgView.setImageResource(commander.getThumbnailID());
        }
    }
}