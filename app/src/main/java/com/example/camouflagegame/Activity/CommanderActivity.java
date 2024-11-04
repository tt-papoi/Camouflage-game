package com.example.camouflagegame.Activity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.camouflagegame.Commander.Commander;
import com.example.camouflagegame.Commander.CustomAdapter;
import com.example.camouflagegame.Game.User;
import com.example.camouflagegame.R;

public class CommanderActivity extends AppCompatActivity {
    private RecyclerView recycleViewCommander;
    private ImageView commanderImageView;
    private TextView commanderNameTextView;
    private ImageButton btnBack, btnChange, btnLeft, btnRight;
    private Commander selectedCommander;
    private CustomAdapter customAdapter;
    private int selectedPosition = RecyclerView.NO_POSITION;
    Commander[] commanders;
    public static int currentCommanderPos;

    private WebView infoSkill;
    private TextView requireLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commander_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        infoSkill = findViewById(R.id.webviewCommander);
        recycleViewCommander = findViewById(R.id.recycleViewCommander);
        commanderImageView = findViewById(R.id.imageViewCommander_commanderScreen);
        commanderNameTextView = findViewById(R.id.commanderTextView);
        btnBack = findViewById(R.id.btnBack_commanderScreen);
        btnChange = findViewById(R.id.btnChange_commanderScreen);
        btnLeft = findViewById(R.id.btnGoToLeftItem);
        btnRight = findViewById(R.id.btnGoToRightItem);
        requireLevel = findViewById(R.id.requireLevelTextView);

        btnChange.setVisibility(View.INVISIBLE);

        currentCommanderPos = selectedPosition;


        commanders = new Commander[]{
                new Commander("001", "001", "Alexander X", R.drawable.commander01, 10),
                new Commander("002", "002", "Napole Ponale", R.drawable.commander02, 0),
                new Commander("003", "003", "Baldwin Alomxo", R.drawable.commander03, 5),
                new Commander("004", "004", "Cleopatra Venus", R.drawable.commander04, 15),
                new Commander("005", "005", "Sibylla Jett", R.drawable.commander05, 20),
        };

        User user = User.loadFromSharedPreferences(this);

        for (int i = 0; i < commanders.length; i++) {
            if (user.getLevel() >= commanders[i].getOpenLevel()){
                commanders[i].setLock(false);
            }
            else{
                commanders[i].setLock(true);
            }
        }
        customAdapter = new CustomAdapter(this, commanders);
        recycleViewCommander.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recycleViewCommander.setAdapter(customAdapter);
        //loadDataCommanderFromRef();
        updateUISelectedCommander();

        handlerButtonClick();

        customAdapter.setOnItemClickListener(new CustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Commander commander, int position) {
                // Update UI
                commanderImageView.setImageResource(commander.getThumbnailID());
                commanderImageView.setAlpha(1f);
                commanderImageView.setColorFilter(null);
                requireLevel.setVisibility(View.INVISIBLE);
                commanderNameTextView.setText(commander.getName());
                animateSelection(commander);
                selectedCommander = commander;
                selectedPosition = position;
                btnChange.setVisibility(View.VISIBLE);
                customAdapter.setSelectedItemPosition(selectedPosition);

                if (selectedPosition == currentCommanderPos){
                    btnChange.setVisibility(View.INVISIBLE);
                }

                if (commander.getIsLock() == true){
                    btnChange.setVisibility(View.INVISIBLE);
                    commanderImageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    requireLevel.setVisibility(View.VISIBLE);
                    requireLevel.setText("Require Level." + Integer.toString(commanders[selectedPosition].getOpenLevel()));
                }

                loadSkillInfo();
            }
        });
    }
    private void handlerButtonClick(){
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCommander.saveToSharedPreferences(CommanderActivity.this);
                currentCommanderPos = selectedPosition;
                finish();
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToLeftItem();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToRightItem();
            }
        });
    }
    private void loadDataCommanderFromRef(){
        Commander commander = Commander.loadFromSharedPreferences(this);
        commanderNameTextView.setText(commander.getName());
        commanderImageView.setImageResource(commander.getThumbnailID());
    }


    private void loadSkillInfo(){
        String description = "";
        switch(commanders[selectedPosition].getId()){
            case "001":
                description = getString(R.string.commander01_description);
                break;
            case "002":
                description = getString(R.string.commander02_description);
                break;
            case "003":
                description = getString(R.string.commander03_description);
                break;
            case "004":
                description = getString(R.string.commander04_description);
                break;
            case "005":
                description = getString(R.string.commander05_description);
                break;
            default: description = "";
                break;

        }

        infoSkill.setBackgroundColor(Color.TRANSPARENT);
        infoSkill.loadDataWithBaseURL(null, description, "text/html", "UTF-8", null);
    }


    private void updateUISelectedCommander(){
        // Tìm vị trí của tướng trong danh sách
        for (int i = 0; i < commanders.length; i++) {
            if (commanders[i].getId().equals(Commander.loadFromSharedPreferences(CommanderActivity.this).getId())
                    ) {
                selectedPosition = i;
                break;
            }
        }

        // Chọn tướng và cập nhật UI
        if (selectedPosition != RecyclerView.NO_POSITION) {
            loadDataCommanderFromRef();
            btnChange.setVisibility(View.INVISIBLE);
            commanderImageView.setAlpha(1f);
            commanderImageView.setColorFilter(null);
            requireLevel.setVisibility(View.INVISIBLE);
            customAdapter.setSelectedItemPosition(selectedPosition);
            currentCommanderPos = selectedPosition;

            if (commanders[selectedPosition].getIsLock() == true){
                btnChange.setVisibility(View.INVISIBLE);
                //commanderImageView.setAlpha(0.3f);
                commanderImageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                requireLevel.setVisibility(View.VISIBLE);
                requireLevel.setText("Require Level." + Integer.toString(commanders[selectedPosition].getOpenLevel()));
            }

            loadSkillInfo();
        }

    }

    private void moveToRightItem() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recycleViewCommander.getLayoutManager();
        int currentPosition = selectedPosition;
        int itemCount = layoutManager.getItemCount();
        if (currentPosition < 0 || currentPosition == itemCount - 1) {
            selectedPosition = 0;
            recycleViewCommander.smoothScrollToPosition(0);
            customAdapter.setSelectedItemPosition(0);
        }
        else  {
            selectedPosition = currentPosition + 1;
        }
        commanderImageView.setImageResource(commanders[selectedPosition].getThumbnailID());
        commanderImageView.setAlpha(1f);
        commanderImageView.setColorFilter(null);
        requireLevel.setVisibility(View.INVISIBLE);
        commanderNameTextView.setText(commanders[selectedPosition].getName());
        animateSelection(commanders[selectedPosition]);
        selectedCommander = commanders[selectedPosition];
        btnChange.setVisibility(View.VISIBLE);
        recycleViewCommander.smoothScrollToPosition(selectedPosition);
        customAdapter.setSelectedItemPosition(selectedPosition);

        if (currentCommanderPos == selectedPosition){
            btnChange.setVisibility(View.INVISIBLE);
        }
        if (commanders[selectedPosition].getIsLock() == true){
            btnChange.setVisibility(View.INVISIBLE);
            commanderImageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            requireLevel.setVisibility(View.VISIBLE);
            requireLevel.setText("Require Level." + Integer.toString(commanders[selectedPosition].getOpenLevel()));
        }

        loadSkillInfo();
    }

    private void moveToLeftItem() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recycleViewCommander.getLayoutManager();
        int currentPosition = selectedPosition;
        int itemCount = layoutManager.getItemCount();
        if (currentPosition <= 0) {
            selectedPosition = itemCount - 1;
            recycleViewCommander.smoothScrollToPosition(selectedPosition);
            customAdapter.setSelectedItemPosition(selectedPosition);
        }
        else  {
            selectedPosition = currentPosition - 1;
        }
        commanderImageView.setImageResource(commanders[selectedPosition].getThumbnailID());
        commanderImageView.setAlpha(1f);
        commanderImageView.setColorFilter(null);
        requireLevel.setVisibility(View.INVISIBLE);
        commanderNameTextView.setText(commanders[selectedPosition].getName());
        animateSelection(commanders[selectedPosition]);
        selectedCommander = commanders[selectedPosition];
        btnChange.setVisibility(View.VISIBLE);
        recycleViewCommander.smoothScrollToPosition(selectedPosition);
        customAdapter.setSelectedItemPosition(selectedPosition);

        if (currentCommanderPos == selectedPosition){
            btnChange.setVisibility(View.INVISIBLE);
        }
        if (commanders[selectedPosition].getIsLock() == true){
            btnChange.setVisibility(View.INVISIBLE);
            commanderImageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            requireLevel.setVisibility(View.VISIBLE);
            requireLevel.setText("Require Level." + Integer.toString(commanders[selectedPosition].getOpenLevel()));
        }

        loadSkillInfo();
    }
    private void animateSelection(Commander commander) {
        // Fade in animation
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500); // Set the duration of the animation (milliseconds)

        // Apply the animation to your UI elements
        commanderImageView.startAnimation(fadeIn);
        commanderNameTextView.startAnimation(fadeIn);

        // Update UI after the animation completes
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Actions to perform when the animation starts
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Actions to perform when the animation ends
                commanderImageView.setImageResource(commander.getThumbnailID());
                commanderNameTextView.setText(commander.getName());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Actions to perform when the animation repeats
            }
        });
    }
}

