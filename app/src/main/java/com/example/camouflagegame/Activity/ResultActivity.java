package com.example.camouflagegame.Activity;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.camouflagegame.Game.User;
import com.example.camouflagegame.R;

public class ResultActivity extends AppCompatActivity {
    boolean isWin;
    public int totalHits;
    public int totalMisses;
    TextView txtExp, txtLevel, txtTotalHits, txtTotalMisses, result;
    ProgressBar expProgressBar;
    ImageButton btnExit;
    ConstraintLayout exp_level_layout;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.result_screen);
        isWin = getIntent().getBooleanExtra("RESULT", true);
        totalHits = getIntent().getIntExtra("TOTAL_HITS", 0);
        totalMisses = getIntent().getIntExtra("TOTAL_MISSES", 0);


        result = findViewById(R.id.txtViewResult);
        if(isWin)
        {
            result.setBackgroundResource(R.drawable.win);
        }
        else
        {
            result.setBackgroundResource(R.drawable.lose);
        }

        txtTotalHits = findViewById(R.id.txtViewTotalHits);
        txtTotalMisses = findViewById(R.id.txtViewTotalMisses);
        txtExp = findViewById(R.id.expTextViewResultScreen);
        txtLevel = findViewById(R.id.levelTextViewResultScreen);
        expProgressBar = findViewById(R.id.expProgressBarResultScreen);
        btnExit = findViewById(R.id.btnExit_resultScreen);
        exp_level_layout = findViewById(R.id.exp_level_layout);

        exp_level_layout.setVisibility(View.INVISIBLE);
        btnExit.setVisibility(View.INVISIBLE);
        txtTotalMisses.setVisibility(View.INVISIBLE);
        txtTotalHits.setVisibility(View.INVISIBLE);

        showResult();

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

    public void showResult()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                txtTotalHits.setVisibility(View.VISIBLE);
                txtTotalHits.setText(String.valueOf(totalHits));
                ObjectAnimator animation = ObjectAnimator.ofFloat(txtTotalHits, "alpha", 0, 1);
                animation.setDuration(1000);
                animation.start();
            }
        }, 1500);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                txtTotalMisses.setVisibility(View.VISIBLE);
                txtTotalMisses.setText(String.valueOf(totalMisses));
                ObjectAnimator animation = ObjectAnimator.ofFloat(txtTotalMisses, "alpha", 0, 1);
                animation.setDuration(1000);
                animation.start();
            }
        }, 2000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exp_level_layout.setVisibility(View.VISIBLE);
                ObjectAnimator animation = ObjectAnimator.ofFloat(exp_level_layout, "alpha", 0, 1);
                animation.setDuration(1000);
                animation.start();
            }
        }, 2500);
        showExpLevel();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnExit.setVisibility(View.VISIBLE);
                ObjectAnimator animation = ObjectAnimator.ofFloat(btnExit, "alpha", 0, 1);
                animation.setDuration(1000);
                animation.start();
            }
        }, 5000);
    }
    private void showExpLevel(){
        final int delayMillis = 4000;
        handler = new Handler();
        if (isWin){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    exp_level_layout.setVisibility(View.VISIBLE);
                    loadDataFromRef();
                    increaseExpFromRef(30);
                }}, delayMillis);

        }
        else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    exp_level_layout.setVisibility(View.VISIBLE);
                    loadDataFromRef();
                    increaseExpFromRef(10);
                }}, delayMillis);
        }
    }

    public void loadDataFromRef(){
        User user = User.loadFromSharedPreferences(this);
        txtExp.setText("EXP: " + user.getExp());
        txtLevel.setText("Level: " + user.getLevel());
        loadExpProgressBar(user.getExp());
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
}
