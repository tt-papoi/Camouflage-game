package com.example.camouflagegame.Activity;

import static com.example.camouflagegame.Activity.DeployEquipmentActivity.convertDpToPixels;
import static com.example.camouflagegame.Activity.DeployEquipmentActivity.gameInfo;
import static com.example.camouflagegame.Activity.DeployEquipmentActivity.rotateBitmap;
import static com.example.camouflagegame.Activity.LobbyActivity.client;
import static com.example.camouflagegame.Activity.LobbyActivity.isHost;
import static com.example.camouflagegame.Activity.LobbyActivity.server;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.camouflagegame.Commander.Commander;
import com.example.camouflagegame.Game.AudioController;
import com.example.camouflagegame.Game.GameInfo;
import com.example.camouflagegame.Game.TankInfo;
import com.example.camouflagegame.Message.ChatArrayAdapter;
import com.example.camouflagegame.Message.ChatMessage;
import com.example.camouflagegame.R;

import java.util.ArrayList;
import java.util.Objects;

public class GameActivity extends AppCompatActivity {
    public static boolean isRunning = false;
    ImageView commanderAvatar;
    ImageButton btnCloseChat, btnSendMessage, btnExpand, btnSetting;
    ConstraintLayout layoutAvatar, layoutChat, layoutYourTurn, layoutEnemyTurn, layoutGame;
    ConstraintLayout layoutEnemyEquipment, layoutEnemyMap, layoutYourEquipment, layoutYourMap;
    View signal;
    EditText editTextMessage;
    ArrayList<View> enemyBullets;
    ArrayList<View> yourBullets;
    ArrayList<View> yourBulletsInEnemyEquipment;
    ArrayList<View> enemyBulletsInYourEquipment;
    float valueOf1DInPixel;
    public ArrayList<TankInfo> tankInfos;
    public View[] yourEquipments;
    public View[] tanksInEnemyTurn;
    Handler gameHandler;
    public ChatArrayAdapter chatArrayAdapter;
    public ArrayList<ChatMessage> chatMessages;
    public int totalHits;
    public int totalMisses;
    ProgressBar manaProgressBar;
    public static int myCurrentMana;
    final int MAXMANA = 10;
    TextView manaTextView;
    private boolean hasShot = false;

    //Các button về skill
    ImageButton btnUltimate, btnCancel, btnSkill1, btnSkill2, btnSkill3;
    TextView skillDescription;
    boolean isSkillHorizontal = false;
    boolean isSkillVertical = false;
    boolean isSkillSquare = false;
    boolean isSkillDeadShot = false;
    boolean isSkill1 = false, isSkill2 = false, isSkill3 = false, is6Shot = false, isUsedSkill = false;
    int countMultipleShot = 0;
    int countBullet3x3Skill = 0;
    boolean canShot = false;
    Commander currentCommander;
    int timer = 30;
    Thread timerThread;
    TextView textViewTimer;
    public void stopTimerThread()
    {
        if(timerThread != null)
        {
            timerThread.interrupt();
            timerThread = null;
        }
    }
    public void startTimerThread()
    {
        stopTimerThread();
        timer = 30;
        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && timer >= 0)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewTimer.setVisibility(View.VISIBLE);
                        }
                    });
                    if(timer == -1)
                    {
                        break;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewTimer.setText(timer + "s");
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    timer -=1;
                    if(timer == 0)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideBtnCancel();
                                isSkill1 = isSkill2 = isSkill3 = isSkillDeadShot = isSkillSquare = isSkillHorizontal = is6Shot = isSkillVertical = false;
                                int[] autoShootPos = gameInfo.getRandomBullet();
                                switch (addBullet(autoShootPos[1], autoShootPos[0], layoutEnemyMap, yourBullets, "YOUR_TURN")) {
                                    case 1:
                                        gameInfo.isYourTurn = true;
                                        gameInfo.TYPE = gameInfo.SEND_MAP;
                                        gameInfo.posBullet[0] = autoShootPos[0];
                                        gameInfo.posBullet[1] = autoShootPos[1];
                                        if (isHost) {
                                            server.sendInfo(new GameInfo(gameInfo));
                                        } else {
                                            client.sendInfo(new GameInfo(gameInfo));
                                        }
                                        layoutEnemyMap.setEnabled(false);
                                        if(gameInfo.isWin())
                                        {
                                            GameActivity.this.runOnUiThread(() -> {
                                                signal.setBackgroundResource(R.drawable.you_win);
                                                ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                                animation.setDuration(1000);
                                                animation.start();
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                                        intent.putExtra("RESULT", true);
                                                        intent.putExtra("TOTAL_HITS", totalHits);
                                                        intent.putExtra("TOTAL_MISSES", totalMisses);
                                                        startActivity(intent);
                                                    }
                                                }, 3500);
                                            });
                                            break;
                                        }
                                        new Handler().postDelayed(() ->
                                                layoutEnemyMap.setEnabled(true), 2000);

                                        timer = 30;
                                        break;
                                    case 0:
                                        gameInfo.isYourTurn = false;
                                        gameInfo.TYPE = gameInfo.SEND_MAP;
                                        gameInfo.posBullet[0] = autoShootPos[0];
                                        gameInfo.posBullet[1] = autoShootPos[1];
                                        if (isHost)
                                        {
                                            server.sendInfo(new GameInfo(gameInfo));
                                        } else
                                        {
                                            client.sendInfo(new GameInfo(gameInfo));
                                        }
                                        textViewTimer.setVisibility(View.INVISIBLE);
                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                        hasShot = false;
                                        layoutEnemyMap.setEnabled(false);
                                }
                            }
                        });
                    }
                }
            }
        });
        timerThread.start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.game_screen);
        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameInfo = null;

    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
    @SuppressLint("SetTextI18n")
    public void initialize()
    {
        textViewTimer = findViewById(R.id.textViewTimer_GameScreen);
        enemyBullets = new ArrayList<>();
        yourBullets = new ArrayList<>();
        yourBulletsInEnemyEquipment = new ArrayList<>();
        enemyBulletsInYourEquipment = new ArrayList<>();

        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnCloseChat = findViewById(R.id.btnCloseChat);
        btnExpand = findViewById(R.id.btnExpand);

        layoutAvatar = findViewById(R.id.avatar_ingame_layout);
        layoutChat = findViewById(R.id.chat_layout);
        layoutYourTurn = findViewById(R.id.your_turn_layout);
        layoutEnemyTurn = findViewById(R.id.enemy_turn_layout);
        layoutGame = findViewById(R.id.game_layout);

        layoutEnemyEquipment = findViewById(R.id.enemyEquipment_enemyTurnLayout);
        layoutEnemyMap = findViewById(R.id.enemyMap_yourTurnLayout);
        layoutYourEquipment = findViewById(R.id.yourEquipment_yourTurnLayout);
        layoutYourMap = findViewById(R.id.yourMap_enemyTurnLayout);
        btnSetting = findViewById(R.id.btnSetting_GameScreen);

        signal = findViewById(R.id.turn_signal);
        signal.setVisibility(View.VISIBLE);

        manaProgressBar = findViewById(R.id.manaProgressBar);
        myCurrentMana = 0;
        manaTextView = findViewById(R.id.manaTextView);
        manaTextView.setText("Mana: " + myCurrentMana + "/10");
        editTextMessage = findViewById(R.id.editTextChatMessage_gameScreen);
        btnSkill1 = findViewById(R.id.btnSkill1);
        btnSkill2 = findViewById(R.id.btnSkill2);
        btnSkill3 = findViewById(R.id.btnSkill3);

        //Khai báo các nút skill ultimate

        btnUltimate = findViewById(R.id.btnUltimate);
        btnCancel = findViewById(R.id.btnCancel);

        commanderAvatar = findViewById(R.id.imageViewCommander_gameScreen);
        SharedPreferences sharedPreferences = this.getSharedPreferences("CommanderPrefs", Context.MODE_PRIVATE);
        commanderAvatar.setBackgroundResource(sharedPreferences.getInt("thumbnailID", R.drawable.commander02));

        currentCommander = Commander.loadFromSharedPreferences(this);

        ListView listMsg = findViewById(R.id.listViewChatMsg);
        chatMessages = new ArrayList<>();
        chatArrayAdapter = new ChatArrayAdapter(GameActivity.this, R.layout.chat_msg_row, chatMessages);
        listMsg.setAdapter(chatArrayAdapter);

        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, SettingActivity.class);
            intent.putExtra("GameScreen","GameScreen");
            startActivity(intent);
        });

        // kiểm tra lượt đi đầu tiên
        if(gameInfo.goFirst) {
            signal.setBackgroundResource(R.drawable.your_turn_signal);
            layoutEnemyTurn.setVisibility(View.INVISIBLE);
            layoutYourTurn.setVisibility(View.VISIBLE);
            gameInfo.isYourTurn = true;
            textViewTimer.setVisibility(View.VISIBLE);
        }
        else
        {
            textViewTimer.setVisibility(View.INVISIBLE);
            gameInfo.isYourTurn = false;
            signal.setBackgroundResource(R.drawable.enemy_turn_signal);
            layoutYourTurn.setVisibility(View.INVISIBLE);
            layoutEnemyTurn.setVisibility(View.VISIBLE);
            ObjectAnimator animationBtnSkill1 = ObjectAnimator.ofFloat(btnSkill1, "alpha", 0,0,0,1);
            animationBtnSkill1.setDuration(4000);
            animationBtnSkill1.start();

            ObjectAnimator animationBtnSkill2 = ObjectAnimator.ofFloat(btnSkill2, "alpha", 0,0,0,1);
            animationBtnSkill2.setDuration(4000);
            animationBtnSkill2.start();


            ObjectAnimator animationBtnSkill3 = ObjectAnimator.ofFloat(btnSkill3, "alpha", 0,0,0,1);
            animationBtnSkill3.setDuration(4000);
            animationBtnSkill3.start();
        }

        btnCloseChat.setEnabled(false);
        btnSendMessage.setEnabled(false);
        editTextMessage.setEnabled(false);
        layoutYourMap.setEnabled(false);
        layoutEnemyMap.setEnabled(false);

        layoutGame.setVisibility(View.INVISIBLE);

        // hiệu ứng thông báo lượt đi
        ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1, 1, 1, 1, 1, 0);
        animation.setDuration(2000);
        animation.start();

        // hiệu ứng hiển thị map
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            GameActivity.this.runOnUiThread(() -> {
                drawYourEquipments();
                layoutGame.setVisibility(View.VISIBLE);
                ObjectAnimator animationLayoutGame = ObjectAnimator.ofFloat(layoutGame, "alpha", 0, 1);
                animationLayoutGame.setDuration(1000);
                animationLayoutGame.start();
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            GameActivity.this.runOnUiThread(() -> {
                btnCloseChat.setEnabled(true);
                btnSendMessage.setEnabled(true);
                editTextMessage.setEnabled(true);
                layoutYourMap.setEnabled(true);
                layoutEnemyMap.setEnabled(true);
                valueOf1DInPixel = DeployEquipmentActivity.convertDpToPixels(1f, GameActivity.this);
                if(gameInfo.goFirst)
                {
                    startTimerThread();
                }
            });
        }).start();

        handleBtnCloseChatClicked();
        handleBtnSendMessageClicked();
        handleBtnExpandClicked();
        handleEnemyMapClicked();
        handleSkillClicked(currentCommander.getUidSkill());
        createGameHandler();

    }
    public void drawYourEquipments()
    {
        int sizeCellOfLayoutYourEquipment = (int) (12 * convertDpToPixels(1f, GameActivity.this));
        yourEquipments = new View[5];
        yourEquipments[0] = findViewById(R.id.tank1_yourEquipment);
        yourEquipments[1] = findViewById(R.id.tank2_yourEquipment);
        yourEquipments[2] = findViewById(R.id.tank3_yourEquipment);
        yourEquipments[3] = findViewById(R.id.tank4_yourEquipment);
        yourEquipments[4] = findViewById(R.id.tank5_yourEquipment);

        int sizeCellOfLayoutEnemyTurn = (int) (30 * convertDpToPixels(1f, GameActivity.this));
        tanksInEnemyTurn = new View[5];
        tanksInEnemyTurn[0] = findViewById(R.id.tank1_EnemyTurn);
        tanksInEnemyTurn[1] = findViewById(R.id.tank2_EnemyTurn);
        tanksInEnemyTurn[2] = findViewById(R.id.tank3_EnemyTurn);
        tanksInEnemyTurn[3] = findViewById(R.id.tank4_EnemyTurn);
        tanksInEnemyTurn[4] = findViewById(R.id.tank5_EnemyTurn);

        tankInfos =  new ArrayList<>();
        tankInfos = getIntent().getParcelableArrayListExtra("TANK_INFO");

        for(int i = 0; i < (tankInfos != null ? tankInfos.size() : 0); i++)
        {
            int x = tankInfos.get(i).x;
            int y = tankInfos.get(i).y;

            if(!tankInfos.get(i).isVertical)
            {
                tanksInEnemyTurn[i].getLayoutParams().height = tanksInEnemyTurn[i].getWidth();
                tanksInEnemyTurn[i].getLayoutParams().width = tanksInEnemyTurn[i].getHeight();
                tanksInEnemyTurn[i].requestLayout();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) tanksInEnemyTurn[i].getBackground();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                bitmap = rotateBitmap(bitmap, 90);
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                tanksInEnemyTurn[i].setBackground(drawable);
            }
            tanksInEnemyTurn[i].animate()
                    .x(x * sizeCellOfLayoutEnemyTurn)
                    .y(y * sizeCellOfLayoutEnemyTurn)
                    .start();


            if(!tankInfos.get(i).isVertical)
            {
                yourEquipments[i].getLayoutParams().height = yourEquipments[i].getWidth();
                yourEquipments[i].getLayoutParams().width = yourEquipments[i].getHeight();
                yourEquipments[i].requestLayout();
            }
            yourEquipments[i].animate()
                    .x(x * sizeCellOfLayoutYourEquipment)
                    .y(y * sizeCellOfLayoutYourEquipment)
                    .start();
        }
    }

    public void createGameHandler()
    {
        gameHandler = new Handler(msg -> {
            switch (msg.what)
            {
                case -1:
                    if (!isFinishing()) {
                        new AlertDialog.Builder(this)
                                .setMessage("Lost connection between two players")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finishAffinity();
                                        Intent intent = new Intent(GameActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                }).show();
                    }
                    break;
                case 0: // received message
                    String receivedMsg = "[Enemy]: " + gameInfo.message;
                    gameInfo.resetMessage();
                    chatArrayAdapter.add(new ChatMessage(receivedMsg));
                    chatArrayAdapter.notifyDataSetChanged();
                    break;
                case 2: // received map
                    int x = gameInfo.posBullet[1];
                    int y = gameInfo.posBullet[0];

                    if(addBullet(x, y, layoutYourMap, enemyBullets, "ENEMY_TURN") == 1)
                    {
                        increaseMana(1);
                    }
                    if(gameInfo.isLose())
                    {
                        GameActivity.this.runOnUiThread(() -> {
                            signal.setBackgroundResource(R.drawable.you_lose);
                            ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                            animation.setDuration(1000);
                            animation.start();
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                intent.putExtra("RESULT", false);
                                intent.putExtra("TOTAL_HITS", totalHits);
                                intent.putExtra("TOTAL_MISSES", totalMisses);
                                startActivity(intent);
                            }, 3500);

                        });

                        break;
                    }

                    if(gameInfo.isYourTurn)
                    {
                        switchTurn(layoutEnemyTurn, layoutYourTurn, "YOUR_TURN");
                        increaseMana(1);
                    }

                    break;
            }
            return true;
        });
        if(isHost)
        {
            server.setGameHandler(gameHandler);
        }
        else
        {
            client.setGameHandler(gameHandler);
        }
    }
    public void handleBtnCloseChatClicked() {
        btnCloseChat.setOnClickListener(v -> {
            layoutChat.setVisibility(View.INVISIBLE);
            btnExpand.setVisibility(View.VISIBLE);
        });
    }
    public void handleBtnSendMessageClicked()
    {
        btnSendMessage.setOnClickListener(v -> {
            if(editTextMessage.getText().toString().equals(""))
            {
                return;
            }
            String msg = editTextMessage.getText().toString();
            gameInfo.message = msg;
            gameInfo.TYPE = gameInfo.SEND_MESSAGE;
            if(isHost)
            {
                server.sendInfo(new GameInfo(gameInfo));
            }
            else
            {
                client.sendInfo(new GameInfo(gameInfo));
            }
            gameInfo.resetMessage();
            editTextMessage.setText("");
            msg = "[You]: " + msg;
            chatArrayAdapter.add(new ChatMessage(msg));
            chatArrayAdapter.notifyDataSetChanged();
        });
    }
    public void handleBtnExpandClicked()
    {
        btnExpand.setOnClickListener(v -> {
            btnExpand.setVisibility(View.INVISIBLE);
            layoutChat.setVisibility(View.VISIBLE);
            layoutAvatar.setVisibility(View.VISIBLE);
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    public void handleEnemyMapClicked()
    {
        layoutEnemyMap.setOnTouchListener((view, event) -> {
            int lenCell = 30;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int x = (int) (event.getX() / (lenCell * valueOf1DInPixel));
                int y = (int) (event.getY() / (lenCell * valueOf1DInPixel));

                //Nếu đã nhấn nút thì lấy vị trí [x,y] nhấn vừa xong để xử lí.

                //double shot
                if (isSkill1)
                {
                    //Kiểm tra mana đủ hay không
                    if(!isUsedSkill)
                    {
                        if(myCurrentMana < 2){
                            Toast.makeText(getApplicationContext(), "Not enough mana", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        decreaseMana(2);
                        stopTimerThread();
                    }
                    stopTimerThread();
                    btnCancel.setVisibility(View.INVISIBLE);
                    isUsedSkill = true;
                    switch (addBullet(x, y, layoutEnemyMap, yourBullets, "YOUR_TURN")) {
                        case 0:
                        case 1:
                            countMultipleShot++;
                            if (countMultipleShot == 2) {
                                gameInfo.isYourTurn = false;
                                countMultipleShot = 0;
                                //Gán lại false là chưa nhấn skill.
                                isSkill1 = false;
                                hideBtnCancel();
                                switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                hasShot = false;
                                layoutEnemyMap.setEnabled(false);
                            } else {
                                gameInfo.isYourTurn = true;
                            }

                            gameInfo.TYPE = gameInfo.SEND_MAP;
                            gameInfo.posBullet[0] = y;
                            gameInfo.posBullet[1] = x;
                            if (isHost) {
                                server.sendInfo(new GameInfo(gameInfo));
                            } else {
                                client.sendInfo(new GameInfo(gameInfo));
                            }
                            if(gameInfo.isWin())
                            {
                                GameActivity.this.runOnUiThread(() -> {
                                    signal.setBackgroundResource(R.drawable.you_win);
                                    ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                    animation.setDuration(1000);
                                    animation.start();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                            intent.putExtra("RESULT", true);
                                            intent.putExtra("TOTAL_HITS", totalHits);
                                            intent.putExtra("TOTAL_MISSES", totalMisses);
                                            startActivity(intent);
                                            stopTimerThread();
                                        }
                                    },2000);
                                });
                            }
                            break;
                    }
                    return false;
                }

                //scan shot
                if (isSkill2){
                    //Kiểm tra mana đủ hay không
                    if(myCurrentMana < 3){
                        Toast.makeText(getApplicationContext(), "Not enough mana", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!canShot){
                        Toast.makeText(getApplicationContext(), "There are no missed bullet in the map", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    stopTimerThread();
                    decreaseMana(3);
                    btnCancel.setVisibility(View.INVISIBLE);
                    isUsedSkill = true;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (getTypeBullet(y,x,"YOUR_TURN") == 10) {
                                int countTank = 0;

                                for (int i = x - 1; i <= x + 1; i++)
                                    for (int j = y - 1; j <= y + 1; j++){
                                        if (i > -1 && j > -1 && i < 10 && j < 10){
                                            if (gameInfo.enemyMap[j][i] > 0 && gameInfo.enemyMap[j][i] < 6){
                                                countTank++;
                                            }
                                            if (gameInfo.enemyMap[j][i] < 0 && gameInfo.enemyMap[j][i] > -6){
                                                countTank++;
                                            }
                                        }
                                    }

                                int finalCountTank = countTank;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        drawBullet(x,y, "DETECT_" + finalCountTank,layoutEnemyMap,yourBullets,false);
                                    }
                                });

                                gameInfo.isYourTurn = false;
                                gameInfo.TYPE = gameInfo.SEND_MAP;
                                if (isHost) {
                                    server.sendInfo(new GameInfo(gameInfo));
                                } else {
                                    client.sendInfo(new GameInfo(gameInfo));
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Gán lại false là chưa nhấn skill.
                                        isSkill2 = false;
                                        hideBtnCancel();
                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                        hasShot = false;
                                        layoutEnemyMap.setEnabled(false);
                                    }
                                });
                            }

                            //block thread hiện tại
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    thread.start();

                    //Thoát khỏi listenner
                    return false;
                }

                //four shots
                if (isSkill3){

                    if(!isUsedSkill)
                    {
                        //Kiểm tra mana đủ hay không
                        if(myCurrentMana < 5){
                            Toast.makeText(getApplicationContext(), "Not enough mana", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        stopTimerThread();
                        decreaseMana(5);
                    }
                    btnCancel.setVisibility(View.INVISIBLE);
                    isUsedSkill = true;
                    switch (addBullet(x, y, layoutEnemyMap, yourBullets, "YOUR_TURN")) {
                        case 0:
                        case 1:
                            countMultipleShot++;
                            if (countMultipleShot == 4) {
                                gameInfo.isYourTurn = false;
                                countMultipleShot = 0;
                                //Gán lại false là chưa nhấn skill.
                                isSkill3 = false;
                                hideBtnCancel();
                                switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                hasShot = false;
                                layoutEnemyMap.setEnabled(false);
                            } else {
                                gameInfo.isYourTurn = true;
                            }

                            gameInfo.TYPE = gameInfo.SEND_MAP;
                            gameInfo.posBullet[0] = y;
                            gameInfo.posBullet[1] = x;
                            if (isHost) {
                                server.sendInfo(new GameInfo(gameInfo));
                            } else {
                                client.sendInfo(new GameInfo(gameInfo));
                            }
                            if(gameInfo.isWin())
                            {
                                GameActivity.this.runOnUiThread(() -> {
                                    signal.setBackgroundResource(R.drawable.you_win);
                                    ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                    animation.setDuration(1000);
                                    animation.start();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                            intent.putExtra("RESULT", true);
                                            intent.putExtra("TOTAL_HITS", totalHits);
                                            intent.putExtra("TOTAL_MISSES", totalMisses);
                                            startActivity(intent);
                                            stopTimerThread();
                                        }
                                    },2000);
                                });
                            }
                            break;
                    }
                    return false;
                }

                //six shots
                if (is6Shot){
                    if(!isUsedSkill)
                    {
                        //Kiểm tra mana đủ hay không
                        if(myCurrentMana < 7){
                            Toast.makeText(getApplicationContext(), "Not enough mana", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        stopTimerThread();
                        decreaseMana(7);
                    }
                    btnCancel.setVisibility(View.INVISIBLE);
                    isUsedSkill = true;
                    switch (addBullet(x, y, layoutEnemyMap, yourBullets, "YOUR_TURN")) {
                        case 0:
                        case 1:
                            countMultipleShot++;
                            if (countMultipleShot == 6) {
                                gameInfo.isYourTurn = false;
                                countMultipleShot = 0;
                                //Gán lại false là chưa nhấn skill.
                                is6Shot = false;
                                hideBtnCancel();
                                switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                hasShot = false;
                                layoutEnemyMap.setEnabled(false);
                            } else {
                                gameInfo.isYourTurn = true;
                            }

                            gameInfo.TYPE = gameInfo.SEND_MAP;
                            gameInfo.posBullet[0] = y;
                            gameInfo.posBullet[1] = x;
                            if (isHost) {
                                server.sendInfo(new GameInfo(gameInfo));
                            } else {
                                client.sendInfo(new GameInfo(gameInfo));
                            }
                            if(gameInfo.isWin())
                            {
                                GameActivity.this.runOnUiThread(() -> {
                                    signal.setBackgroundResource(R.drawable.you_win);
                                    ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                    animation.setDuration(1000);
                                    animation.start();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                            intent.putExtra("RESULT", true);
                                            intent.putExtra("TOTAL_HITS", totalHits);
                                            intent.putExtra("TOTAL_MISSES", totalMisses);
                                            startActivity(intent);
                                            stopTimerThread();
                                        }
                                    },2000);
                                });
                            }
                            break;
                    }
                    return false;
                }

                //Vertival Shot
                if (isSkillVertical){
                    if(!isUsedSkill)
                    {
                        //Kiểm tra mana đủ hay không
                        if(myCurrentMana < 10){
                            Toast.makeText(getApplicationContext(), "Not enough mana", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (y != 0 && y != 9){
                            return false;
                        }
                        stopTimerThread();
                        decreaseMana(10);
                    }
                    btnCancel.setVisibility(View.INVISIBLE);
                    isUsedSkill = true;
                    layoutEnemyMap.setEnabled(false);

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (y == 0)
                            {
                                for (int i = 0; i < 10; i++)
                                {
                                    int finalI = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (addBullet(x, finalI, layoutEnemyMap, yourBullets, "YOUR_TURN"))
                                            {
                                                case 0:
                                                    if (finalI == 9) {
                                                        gameInfo.isYourTurn = false;
                                                        //Gán lại false là chưa nhấn skill.
                                                        isSkillVertical = false;
                                                        hideBtnCancel();
                                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                        hasShot = false;
                                                        layoutEnemyMap.setEnabled(false);
                                                    } else {
                                                        gameInfo.isYourTurn = true;
                                                    }
                                                    gameInfo.TYPE = gameInfo.SEND_MAP;
                                                    gameInfo.posBullet[0] = finalI;
                                                    gameInfo.posBullet[1] = x;
                                                    if (isHost) {
                                                        server.sendInfo(new GameInfo(gameInfo));
                                                    } else {
                                                        client.sendInfo(new GameInfo(gameInfo));
                                                    }
                                                    break;
                                                case 1:
                                                    gameInfo.isYourTurn = false;
                                                    //Gán lại false là chưa nhấn skill.
                                                    hideBtnCancel();
                                                    if(gameInfo.isWin())
                                                    {
                                                        isSkillVertical = false;
                                                        GameActivity.this.runOnUiThread(() -> {
                                                            signal.setBackgroundResource(R.drawable.you_win);
                                                            ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                                            animation.setDuration(1000);
                                                            animation.start();
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                                                    intent.putExtra("RESULT", true);
                                                                    intent.putExtra("TOTAL_HITS", totalHits);
                                                                    intent.putExtra("TOTAL_MISSES", totalMisses);
                                                                    startActivity(intent);
                                                                    stopTimerThread();
                                                                }
                                                            },2500);
                                                        });
                                                    }
                                                    gameInfo.TYPE = gameInfo.SEND_MAP;
                                                    gameInfo.posBullet[0] = finalI;
                                                    gameInfo.posBullet[1] = x;
                                                    if (isHost) {
                                                        server.sendInfo(new GameInfo(gameInfo));
                                                    } else {
                                                        client.sendInfo(new GameInfo(gameInfo));
                                                    }
                                                    if(isSkillVertical)
                                                    {
                                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                        hasShot = false;
                                                        layoutEnemyMap.setEnabled(false);
                                                    }
                                                    isSkillVertical = false;
                                                    break;
                                            }
                                        }
                                    });
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                    if(!isSkillVertical) break;
                                }
                            }
                            if(y == 9)
                            {
                                for (int i = 9; i >= 0; i--)
                                {
                                    int finalI = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (addBullet(x, finalI, layoutEnemyMap, yourBullets, "YOUR_TURN"))
                                            {
                                                case 0:
                                                    if (finalI == 0) {
                                                        gameInfo.isYourTurn = false;
                                                        //Gán lại false là chưa nhấn skill.
                                                        isSkillVertical = false;
                                                        hideBtnCancel();
                                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                        hasShot = false;
                                                        layoutEnemyMap.setEnabled(false);
                                                    } else {
                                                        gameInfo.isYourTurn = true;
                                                    }
                                                    gameInfo.TYPE = gameInfo.SEND_MAP;
                                                    gameInfo.posBullet[0] = finalI;
                                                    gameInfo.posBullet[1] = x;
                                                    if (isHost) {
                                                        server.sendInfo(new GameInfo(gameInfo));
                                                    } else {
                                                        client.sendInfo(new GameInfo(gameInfo));
                                                    }
                                                    break;
                                                case 1:
                                                    gameInfo.isYourTurn = false;
                                                    //Gán lại false là chưa nhấn skill.
                                                    hideBtnCancel();
                                                    if(gameInfo.isWin())
                                                    {
                                                        isSkillVertical = false;
                                                        GameActivity.this.runOnUiThread(() -> {
                                                            signal.setBackgroundResource(R.drawable.you_win);
                                                            ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                                            animation.setDuration(1000);
                                                            animation.start();
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                                                    intent.putExtra("RESULT", true);
                                                                    intent.putExtra("TOTAL_HITS", totalHits);
                                                                    intent.putExtra("TOTAL_MISSES", totalMisses);
                                                                    startActivity(intent);
                                                                    stopTimerThread();
                                                                }
                                                            },2500);
                                                        });
                                                    }
                                                    gameInfo.TYPE = gameInfo.SEND_MAP;
                                                    gameInfo.posBullet[0] = finalI;
                                                    gameInfo.posBullet[1] = x;
                                                    if (isHost) {
                                                        server.sendInfo(new GameInfo(gameInfo));
                                                    } else {
                                                        client.sendInfo(new GameInfo(gameInfo));
                                                    }
                                                    if(isSkillVertical)
                                                    {
                                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                        hasShot = false;
                                                        layoutEnemyMap.setEnabled(false);
                                                    }
                                                    isSkillVertical = false;
                                                    break;
                                            }
                                        }
                                    });
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                    if(!isSkillVertical) break;
                                }
                            }

                        }
                    });
                    thread.start();
                    layoutEnemyMap.setEnabled(false);

                    return false;
                }

                //Horizontal Shot
                if (isSkillHorizontal){
                    if(!isUsedSkill)
                    {
                        //Kiểm tra mana đủ hay không
                        if(myCurrentMana < 10){
                            Toast.makeText(getApplicationContext(), "Not enough mana", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (x != 0 && x != 9){
                            return false;
                        }
                        stopTimerThread();
                        decreaseMana(10);
                    }
                    btnCancel.setVisibility(View.INVISIBLE);
                    isUsedSkill = true;
                    layoutEnemyMap.setEnabled(false);

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (x == 0)
                            {
                                for (int i = 0; i < 10; i++)
                                {
                                    int finalI = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (addBullet(finalI, y, layoutEnemyMap, yourBullets, "YOUR_TURN"))
                                            {
                                                case 0:
                                                    if (finalI == 9) {
                                                        gameInfo.isYourTurn = false;
                                                        //Gán lại false là chưa nhấn skill.
                                                        isSkillHorizontal = false;
                                                        hideBtnCancel();
                                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                        hasShot = false;
                                                        layoutEnemyMap.setEnabled(false);
                                                    } else {
                                                        gameInfo.isYourTurn = true;
                                                    }
                                                    gameInfo.TYPE = gameInfo.SEND_MAP;
                                                    gameInfo.posBullet[0] = y;
                                                    gameInfo.posBullet[1] = finalI;
                                                    if (isHost) {
                                                        server.sendInfo(new GameInfo(gameInfo));
                                                    } else {
                                                        client.sendInfo(new GameInfo(gameInfo));
                                                    }
                                                    break;
                                                case 1:
                                                    gameInfo.isYourTurn = false;
                                                    //Gán lại false là chưa nhấn skill.
                                                    hideBtnCancel();
                                                    if(gameInfo.isWin())
                                                    {
                                                        isSkillHorizontal = false;
                                                        GameActivity.this.runOnUiThread(() -> {
                                                            signal.setBackgroundResource(R.drawable.you_win);
                                                            ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                                            animation.setDuration(1000);
                                                            animation.start();
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                                                    intent.putExtra("RESULT", true);
                                                                    intent.putExtra("TOTAL_HITS", totalHits);
                                                                    intent.putExtra("TOTAL_MISSES", totalMisses);
                                                                    startActivity(intent);
                                                                    stopTimerThread();
                                                                }
                                                            },2500);
                                                        });
                                                    }
                                                    gameInfo.TYPE = gameInfo.SEND_MAP;
                                                    gameInfo.posBullet[0] = y;
                                                    gameInfo.posBullet[1] = finalI;
                                                    if (isHost) {
                                                        server.sendInfo(new GameInfo(gameInfo));
                                                    } else {
                                                        client.sendInfo(new GameInfo(gameInfo));
                                                    }
                                                    if(isSkillHorizontal)
                                                    {
                                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                        hasShot = false;
                                                        layoutEnemyMap.setEnabled(false);
                                                    }
                                                    isSkillHorizontal = false;
                                                    break;
                                            }
                                        }
                                    });
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                    if(!isSkillHorizontal) break;
                                }
                            }
                            if(x == 9)
                            {
                                for (int i = 9; i >= 0; i--)
                                {
                                    int finalI = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (addBullet(finalI, y, layoutEnemyMap, yourBullets, "YOUR_TURN"))
                                            {
                                                case 0:
                                                    if (finalI == 0) {
                                                        gameInfo.isYourTurn = false;
                                                        //Gán lại false là chưa nhấn skill.
                                                        isSkillHorizontal = false;
                                                        hideBtnCancel();
                                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                        hasShot = false;
                                                        layoutEnemyMap.setEnabled(false);
                                                    } else {
                                                        gameInfo.isYourTurn = true;
                                                    }
                                                    gameInfo.TYPE = gameInfo.SEND_MAP;
                                                    gameInfo.posBullet[0] = y;
                                                    gameInfo.posBullet[1] = finalI;
                                                    if (isHost) {
                                                        server.sendInfo(new GameInfo(gameInfo));
                                                    } else {
                                                        client.sendInfo(new GameInfo(gameInfo));
                                                    }
                                                    break;
                                                case 1:
                                                    gameInfo.isYourTurn = false;
                                                    //Gán lại false là chưa nhấn skill.
                                                    hideBtnCancel();
                                                    if(gameInfo.isWin())
                                                    {
                                                        isSkillHorizontal = false;
                                                        GameActivity.this.runOnUiThread(() -> {
                                                            signal.setBackgroundResource(R.drawable.you_win);
                                                            ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                                            animation.setDuration(1000);
                                                            animation.start();
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                                                    intent.putExtra("RESULT", true);
                                                                    intent.putExtra("TOTAL_HITS", totalHits);
                                                                    intent.putExtra("TOTAL_MISSES", totalMisses);
                                                                    startActivity(intent);
                                                                    stopTimerThread();
                                                                }
                                                            },2500);
                                                        });
                                                    }
                                                    gameInfo.TYPE = gameInfo.SEND_MAP;
                                                    gameInfo.posBullet[0] = y;
                                                    gameInfo.posBullet[1] = finalI;
                                                    if (isHost) {
                                                        server.sendInfo(new GameInfo(gameInfo));
                                                    } else {
                                                        client.sendInfo(new GameInfo(gameInfo));
                                                    }
                                                    if(isSkillHorizontal)
                                                    {
                                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                        hasShot = false;
                                                        layoutEnemyMap.setEnabled(false);
                                                    }
                                                    isSkillHorizontal = false;
                                                    break;
                                            }
                                        }
                                    });
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                    if(!isSkillHorizontal) break;
                                }
                            }

                        }
                    });
                    thread.start();
                    layoutEnemyMap.setEnabled(false);
                    return false;
                }

                //3x3 Shot
                if (isSkillSquare){

                    countBullet3x3Skill = 0;
                    if(!isUsedSkill)
                    {
                        //Kiểm tra mana đủ hay không
                        if(myCurrentMana < 10){
                            Toast.makeText(getApplicationContext(), "Not enough mana", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        stopTimerThread();
                        decreaseMana(10);
                    }
                    layoutEnemyMap.setEnabled(false);
                    btnCancel.setVisibility(View.INVISIBLE);
                    isUsedSkill = true;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = x - 1; i <= x + 1; i++)
                            {
                                if(!isSkillSquare) break;
                                for (int j = y - 1; j <= y + 1; j++) {

                                    if(!isSkillSquare) break;
                                    if (i > -1 && j > -1 && i < 10 && j < 10)
                                    {
                                        int finalI = i;
                                        int finalJ = j;
                                        countBullet3x3Skill++;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                switch (addBullet(finalI, finalJ, layoutEnemyMap, yourBullets, "YOUR_TURN"))
                                                {
                                                    case 0:
                                                        if (countBullet3x3Skill == 9) {
                                                            gameInfo.isYourTurn = false;
                                                            //Gán lại false là chưa nhấn skill.
                                                            isSkillSquare = false;
                                                            hideBtnCancel();
                                                            switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                            hasShot = false;
                                                            layoutEnemyMap.setEnabled(false);
                                                        } else {
                                                            gameInfo.isYourTurn = true;
                                                        }
                                                        gameInfo.TYPE = gameInfo.SEND_MAP;
                                                        gameInfo.posBullet[0] = finalJ;
                                                        gameInfo.posBullet[1] = finalI;
                                                        if (isHost) {
                                                            server.sendInfo(new GameInfo(gameInfo));
                                                        } else {
                                                            client.sendInfo(new GameInfo(gameInfo));
                                                        }
                                                        break;
                                                    case 1:
                                                        if(gameInfo.isWin())
                                                        {
                                                            gameInfo.isYourTurn = true;
                                                            gameInfo.TYPE = gameInfo.SEND_MAP;
                                                            gameInfo.posBullet[0] = finalJ;
                                                            gameInfo.posBullet[1] = finalI;
                                                            if (isHost) {
                                                                server.sendInfo(new GameInfo(gameInfo));
                                                            } else {
                                                                client.sendInfo(new GameInfo(gameInfo));
                                                            }
                                                            isSkillSquare = false;
                                                            GameActivity.this.runOnUiThread(() -> {
                                                                signal.setBackgroundResource(R.drawable.you_win);
                                                                ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                                                animation.setDuration(1000);
                                                                animation.start();
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                                                        intent.putExtra("RESULT", true);
                                                                        intent.putExtra("TOTAL_HITS", totalHits);
                                                                        intent.putExtra("TOTAL_MISSES", totalMisses);
                                                                        startActivity(intent);
                                                                        stopTimerThread();
                                                                    }
                                                                },2500);
                                                            });
                                                            break;
                                                        }
                                                        if(!isSkillSquare)
                                                        {
                                                            break;
                                                        }
                                                        if (countBullet3x3Skill == 9) {
                                                            gameInfo.isYourTurn = false;
                                                            //Gán lại false là chưa nhấn skill.
                                                            isSkillSquare = false;
                                                            hideBtnCancel();
                                                            switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                                            hasShot = false;
                                                            layoutEnemyMap.setEnabled(false);
                                                        } else {
                                                            gameInfo.isYourTurn = true;
                                                        }
                                                        gameInfo.TYPE = gameInfo.SEND_MAP;
                                                        gameInfo.posBullet[0] = finalJ;
                                                        gameInfo.posBullet[1] = finalI;
                                                        if (isHost) {
                                                            server.sendInfo(new GameInfo(gameInfo));
                                                        } else {
                                                            client.sendInfo(new GameInfo(gameInfo));
                                                        }
                                                        break;
                                                }
                                            }
                                        });
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            break;
                                        }
                                    }
                                }
                            }

                        }
                    });
                    thread.start();
                    return false;
                }

                //Dead Shot
                if (isSkillDeadShot){
                    int type = gameInfo.enemyMap[y][x];
                    if(!isUsedSkill)
                    {
                        //Kiểm tra mana đủ hay không
                        if(myCurrentMana < 6){
                            Toast.makeText(getApplicationContext(), "Not enough mana", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if(type < 0 || type == 10)
                        {
                            return false;
                        }
                        stopTimerThread();
                        decreaseMana(6);
                    }
                    layoutEnemyMap.setEnabled(false);
                    btnCancel.setVisibility(View.INVISIBLE);
                    isUsedSkill = true;

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(type == 0)
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addBullet(x, y, layoutEnemyMap, yourBullets, "YOUR_TURN");
                                        gameInfo.isYourTurn = false;
                                        //Gán lại false là chưa nhấn skill.
                                        isSkillDeadShot = false;
                                        hideBtnCancel();
                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                        hasShot = false;
                                        layoutEnemyMap.setEnabled(false);
                                        gameInfo.TYPE = gameInfo.SEND_MAP;
                                        gameInfo.posBullet[0] = y;
                                        gameInfo.posBullet[1] = x;
                                        if (isHost) {
                                            server.sendInfo(new GameInfo(gameInfo));
                                        } else {
                                            client.sendInfo(new GameInfo(gameInfo));
                                        }
                                    }
                                });

                            }
                            if(type < 6 && type > 0)
                            {
                                for (int i = 0; i < 10; i++)
                                {
                                    for (int j = 0; j < 10; j++)
                                    {
                                        if (gameInfo.enemyMap[i][j] == type)
                                        {
                                            int finalI = i;
                                            int finalJ = j;
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addBullet(finalJ, finalI, layoutEnemyMap, yourBullets, "YOUR_TURN");
                                                    gameInfo.isYourTurn = true;
                                                    gameInfo.TYPE = gameInfo.SEND_MAP;
                                                    gameInfo.posBullet[0] = finalI;
                                                    gameInfo.posBullet[1] = finalJ;
                                                    if (isHost) {
                                                        server.sendInfo(new GameInfo(gameInfo));
                                                    } else {
                                                        client.sendInfo(new GameInfo(gameInfo));
                                                    }
                                                    if(gameInfo.isWin())
                                                    {
                                                        isSkillDeadShot = false;
                                                        GameActivity.this.runOnUiThread(() -> {
                                                            signal.setBackgroundResource(R.drawable.you_win);
                                                            ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                                            animation.setDuration(1000);
                                                            animation.start();
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                                                    intent.putExtra("RESULT", true);
                                                                    intent.putExtra("TOTAL_HITS", totalHits);
                                                                    intent.putExtra("TOTAL_MISSES", totalMisses);
                                                                    startActivity(intent);
                                                                    stopTimerThread();
                                                                }
                                                            },2500);
                                                        });
                                                    }
                                                }
                                            });
                                            try {
                                                Thread.sleep(500);
                                            } catch (InterruptedException e) {
                                                isSkillDeadShot = false;
                                            }
                                        }
                                    }
                                    if(!isSkillDeadShot) break;
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        gameInfo.isYourTurn = false;
                                        gameInfo.TYPE = gameInfo.SEND_MAP;
                                        if (isHost) {
                                            server.sendInfo(new GameInfo(gameInfo));
                                        } else {
                                            client.sendInfo(new GameInfo(gameInfo));
                                        }
                                        isSkillDeadShot = false;
                                        hideBtnCancel();
                                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                                        hasShot = false;
                                        layoutEnemyMap.setEnabled(false);
                                    }
                                });

                            }
                        }
                    });
                    thread.start();
                    return false;
                }

                //Bắn thường
                switch (addBullet(x, y, layoutEnemyMap, yourBullets, "YOUR_TURN")) {
                    case 1:
                        hasShot = true;
                        gameInfo.isYourTurn = true;
                        gameInfo.TYPE = gameInfo.SEND_MAP;
                        gameInfo.posBullet[0] = y;
                        gameInfo.posBullet[1] = x;
                        if (isHost) {
                            server.sendInfo(new GameInfo(gameInfo));
                        } else {
                            client.sendInfo(new GameInfo(gameInfo));
                        }
                        layoutEnemyMap.setEnabled(false);
                        if(gameInfo.isWin())
                        {
                            GameActivity.this.runOnUiThread(() -> {
                                signal.setBackgroundResource(R.drawable.you_win);
                                ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 0, 1);
                                animation.setDuration(1000);
                                animation.start();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                                        intent.putExtra("RESULT", true);
                                        intent.putExtra("TOTAL_HITS", totalHits);
                                        intent.putExtra("TOTAL_MISSES", totalMisses);
                                        startActivity(intent);
                                        stopTimerThread();
                                    }
                                }, 3500);
                            });
                            break;
                        }
                        new Handler().postDelayed(() ->
                                layoutEnemyMap.setEnabled(true), 2000);
                        stopTimerThread();
                        startTimerThread();
                        break;
                    case 0:
                        gameInfo.isYourTurn = false;
                        gameInfo.TYPE = gameInfo.SEND_MAP;
                        gameInfo.posBullet[0] = y;
                        gameInfo.posBullet[1] = x;
                        if (isHost) {
                            server.sendInfo(new GameInfo(gameInfo));
                        } else {
                            client.sendInfo(new GameInfo(gameInfo));
                        }
                        stopTimerThread();
                        switchTurn(layoutYourTurn, layoutEnemyTurn, "ENEMY_TURN");
                        hasShot = false;
                        layoutEnemyMap.setEnabled(false);
                }
            }
            return false;
        });
    }
    // Kiểm tra các skill ultimate đã được nhấn hay chưa.
    public void handleSkillClicked(String uidSkill){
        skillDescription = findViewById(R.id.textViewDescribeSkill);
        hideBtnCancel();
        //handleSkillAppear
        btnSkill1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //Kiểm tra đã bắn phát nào trước đó hay chưa
                if(hasShot){
                    Toast.makeText(getApplicationContext(), "You have already performed a normal shot", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Đạt 2 điều kiện trên thì gán true là đã kích hoạt skill và thông báo Toast.
                isSkill1 = true;
                skillDescription.setText("Shoot 2 cells - Need 2 mana");

                //Trừ đi mana cần thiết.
                hideBtnSkill();
                countMultipleShot = 0;
            }
        });
        btnSkill2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //Kiểm tra đã bắn phát nào trước đó hay chưa
                if(hasShot){
                    Toast.makeText(getApplicationContext(), "You have already performed a normal shot", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Đạt 2 điều kiện trên thì gán true là đã kích host skill và thông báo Toast.
                isSkill2 = true;
                skillDescription.setText("Scan around the missed cells - Need 3 mana");

                hideBtnSkill();
            }
        });
        btnSkill3.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //Kiểm tra đã bắn phát nào trước đó hay chưa
                if(hasShot){
                    Toast.makeText(getApplicationContext(), "You have already performed a normal shot", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Đạt 2 điều kiện trên thì gán true là đã kích hoạt skill và thông báo Toast.
                isSkill3 = true;
                skillDescription.setText("Shoot any 4 cells - Need 5 mana");

                countMultipleShot = 0;
                hideBtnSkill();
            }
        });
        btnUltimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBtnSkill();
                setBtnUltimateListener(uidSkill);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBtnCancel();
                isSkill1 = isSkill2 = isSkill3 = isSkillDeadShot = isSkillSquare = isSkillHorizontal = is6Shot = isSkillVertical = false;
            }
        });
    }
    public void hideBtnSkill()
    {
        btnCancel.setVisibility(View.VISIBLE);
        skillDescription.setVisibility(View.VISIBLE);
        btnUltimate.setVisibility(View.INVISIBLE);
        btnSkill1.setVisibility(View.INVISIBLE);
        btnSkill2.setVisibility(View.INVISIBLE);
        btnSkill3.setVisibility(View.INVISIBLE);
    }
    public void hideBtnCancel()
    {
        isUsedSkill = false;
        skillDescription.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnUltimate.setVisibility(View.VISIBLE);
        btnSkill1.setVisibility(View.VISIBLE);
        btnSkill2.setVisibility(View.VISIBLE);
        btnSkill3.setVisibility(View.VISIBLE);
    }
    @SuppressLint("SetTextI18n")
    public void setBtnUltimateListener(String uidSkill)
    {
        switch (uidSkill){
            case "001":
                set6ShotsUltimate();
                skillDescription.setText("Shoot any 6 cells - Need 7 mana");
                break;
            case "002":
                setDeadShotUltimate();
                skillDescription.setText("If you hit an enemy, it will cause the enemy's weapon to explode - Need 6 mana");
                break;
            case "003":
                setVerticalUltimate();
                skillDescription.setText("Fires a series of bullets in a vertical line until it hits an enemy - Need 10 mana");
                break;
            case "004":
                set3x3Ultimate();
                skillDescription.setText("Shoot 9 cells around (3x3) - Need 10 mana");
                break;
            case "005":
                setHorizontalUltimate();
                skillDescription.setText("Fires a series of bullets in a straight horizontal line until it hits an enemy - Need 10 mana");
                break;
        }
    }
    public void setHorizontalUltimate()
    {
        //Kiểm tra đã bắn phát nào trước đó hay chưa
        if(hasShot){
            Toast.makeText(getApplicationContext(), "You have already performed a normal shot", Toast.LENGTH_SHORT).show();
            return;
        }
        isSkillHorizontal = true;
    }
    public void setVerticalUltimate()
    {
        //Kiểm tra đã bắn phát nào trước đó hay chưa
        if(hasShot){
            Toast.makeText(getApplicationContext(), "You have already performed a normal shot", Toast.LENGTH_SHORT).show();
            return;
        }
        //Đạt 2 điều kiện trên thì gán true là đã kích hoạt skill và thông báo Toast.
        isSkillVertical = true;

    }
    public void set3x3Ultimate()
    {
        //Kiểm tra đã bắn phát nào trước đó hay chưa
        if(hasShot){
            Toast.makeText(getApplicationContext(), "You have already performed a normal shot", Toast.LENGTH_SHORT).show();
            return;
        }
        isSkillSquare = true;
    }
    public void set6ShotsUltimate()
    {
        //Kiểm tra đã bắn phát nào trước đó hay chưa
        if(hasShot){
            Toast.makeText(getApplicationContext(), "You have already performed a normal shot", Toast.LENGTH_SHORT).show();
            return;
        }
        //Đạt 2 điều kiện trên thì gán true là đã kích hoạt skill và thông báo Toast.
        is6Shot = true;
        countMultipleShot = 0;
    }
    public void setDeadShotUltimate()
    {
        //Kiểm tra đã bắn phát nào trước đó hay chưa
        if(hasShot){
            Toast.makeText(getApplicationContext(), "You have already performed a normal shot", Toast.LENGTH_SHORT).show();
            return;
        }
        isSkillDeadShot = true;
    }
    public void switchTurn(ConstraintLayout firstLayout, ConstraintLayout secondLayout, String type)
    {
        new Handler().postDelayed(() ->
        {
            if(type.equals("ENEMY_TURN"))
            {
                signal.setBackgroundResource(R.drawable.enemy_turn_signal);
                textViewTimer.setVisibility(View.INVISIBLE);
                stopTimerThread();
            }
            else
            {
                signal.setBackgroundResource(R.drawable.your_turn_signal);
            }
            ObjectAnimator animationTurnSignal = ObjectAnimator.ofFloat(signal, "alpha", 0,1);
            animationTurnSignal.setDuration(2000);
            animationTurnSignal.start();

            ObjectAnimator animationFirstLayout = ObjectAnimator.ofFloat(firstLayout, "alpha", 1,0);
            animationFirstLayout.setDuration(2000);
            animationFirstLayout.start();

            ObjectAnimator animationSecondLayout = ObjectAnimator.ofFloat(secondLayout, "alpha", 0, 0, 0,1);
            animationSecondLayout.setDuration(4000);
            animationSecondLayout.start();
        }, 2000);
        new Handler().postDelayed(() ->
        {
            if(type.equals("ENEMY_TURN"))
            {
                layoutYourTurn.setVisibility(View.INVISIBLE);
                layoutEnemyTurn.setVisibility(View.VISIBLE);
                layoutYourMap.setEnabled(true);
            }
            else
            {
                layoutYourTurn.setVisibility(View.VISIBLE);
                layoutEnemyTurn.setVisibility(View.INVISIBLE);
                layoutEnemyMap.setEnabled(true);
                textViewTimer.setVisibility(View.VISIBLE);
                startTimerThread();
            }

            ObjectAnimator animation = ObjectAnimator.ofFloat(signal, "alpha", 1,0);
            animation.setDuration(2000);
            animation.start();
        }, 4000);
    }
    public int addBullet(int x, int y, ConstraintLayout layout, ArrayList<View> bulletsList, String type)
    {
        // bản đồ bằng mảng 2 chiều bị đổi thứ tự x, y so với x, y trên giao diện
        int typeBullet = getTypeBullet(y, x, type);
        switch (typeBullet)
        {
            case -1:
                return -1;
            case 10:
                return 10;
            case 0:
                drawBullet(x, y, "FAIL", layout, bulletsList, false);
                if(type.equals("YOUR_TURN"))
                {
                    gameInfo.enemyMap[y][x] = 10;
                    drawBullet(x, y, "FAIL", layoutEnemyEquipment, yourBulletsInEnemyEquipment, true);
                    totalMisses++;

                    canShot = true; // skill 2 có thể dùng khi ít nhất bắn hụt 1 phát
                }
                else
                {
                    gameInfo.yourMap[y][x] = 10;
                    drawBullet(x, y, "FAIL", layoutYourEquipment, enemyBulletsInYourEquipment, true);
                }
                return 0;
            case 1:
                drawBullet(x, y, "SUCCESS", layout, bulletsList, false);
                if(type.equals("YOUR_TURN"))
                {
                    gameInfo.enemyMap[y][x] = -gameInfo.enemyMap[y][x];
                    drawBullet(x, y, "SUCCESS", layoutEnemyEquipment, yourBulletsInEnemyEquipment, true);
                    totalHits++;
                }
                else
                {
                    gameInfo.yourMap[y][x] = -gameInfo.yourMap[y][x];
                    drawBullet(x, y, "SUCCESS", layoutYourEquipment, enemyBulletsInYourEquipment, true);
                }
                return 1;
        }
        return 0;
    }


    public void drawBullet(int x, int y, String bulletType, ConstraintLayout layout, ArrayList<View> bulletsList, boolean isOnMiniMap)
    {
        float diameter = 23f;
        float margin = 2.5f;
        float _1DpInPixel = DeployEquipmentActivity.convertDpToPixels(1f, GameActivity.this);
        float lenCell = 30f;
        if(isOnMiniMap)
        {
            diameter = 9.2f;
            margin = 1.1f;
            _1DpInPixel = DeployEquipmentActivity.convertDpToPixels(1f, GameActivity.this);
            lenCell = 12f;
        }

        View bullet = new View(GameActivity.this);
        if(Objects.equals(bulletType, "SUCCESS"))
        {
            bullet.setBackgroundResource(R.drawable.success_bullet);
            AudioController.playHitSound();
        }
        else if (Objects.equals(bulletType, "FAIL"))
        {
            bullet.setBackgroundResource(R.drawable.false_bullet);
            AudioController.playMissSound();
        }
        else if(Objects.equals(bulletType, "DETECT_0"))
        {
            bullet.setBackgroundResource(R.drawable.detect0);
            AudioController.playMissSound();
        }
        else if(Objects.equals(bulletType, "DETECT_1"))
        {
            bullet.setBackgroundResource(R.drawable.detect1);
            AudioController.playMissSound();
        }
        else if(Objects.equals(bulletType, "DETECT_2"))
        {
            bullet.setBackgroundResource(R.drawable.detect2);
            AudioController.playMissSound();
        }
        else if(Objects.equals(bulletType, "DETECT_3"))
        {
            bullet.setBackgroundResource(R.drawable.detect3);
            AudioController.playMissSound();
        }
        else if(Objects.equals(bulletType, "DETECT_4"))
        {
            bullet.setBackgroundResource(R.drawable.detect4);
            AudioController.playMissSound();
        }
        else if(Objects.equals(bulletType, "DETECT_5"))
        {
            bullet.setBackgroundResource(R.drawable.detect5);
            AudioController.playMissSound();
        }
        else if(Objects.equals(bulletType, "DETECT_6"))
        {
            bullet.setBackgroundResource(R.drawable.detect6);
            AudioController.playMissSound();
        }
        else if(Objects.equals(bulletType, "DETECT_7"))
        {
            bullet.setBackgroundResource(R.drawable.detect7);
            AudioController.playMissSound();
        }
        else if(Objects.equals(bulletType, "DETECT_8"))
        {
            bullet.setBackgroundResource(R.drawable.detect8);
            AudioController.playMissSound();
        }

        bullet.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT));


        bullet.getLayoutParams().height = (int) (_1DpInPixel * diameter);
        bullet.getLayoutParams().width = (int) (_1DpInPixel * diameter);
        layout.addView(bullet);

        bullet.animate()
                .x(x * valueOf1DInPixel * lenCell + valueOf1DInPixel * margin)
                .y(y * valueOf1DInPixel * lenCell + valueOf1DInPixel * margin)
                .setDuration(0)
                .start();
        ObjectAnimator animation = ObjectAnimator.ofFloat(bullet, "alpha", 0, 1, 1, 1);
        animation.setDuration(2000);
        animation.start();
        bulletsList.add(bullet);
    }
    public int getTypeBullet(int x, int y, String type)
    {
        if(type.equals("ENEMY_TURN"))
        {
            switch (gameInfo.yourMap[x][y])
            {
                case 10: // ô đã bị bắn
                    return 10;
                case 0: // ô trống
                    return 0;
                case 1: // 1-5 vị trí của thiết bị
                case 2:
                case 3:
                case 4:
                case 5:
                    return 1;
                case -1: // vị trí bắn trúng giá trị là số đối của giá trị ô chứa thiết bị
                case -2:
                case -3:
                case -4:
                case -5:
                    return -1;
            }
        }
        else
        {
            switch (gameInfo.enemyMap[x][y])
            {
                case 10: // ô đã bị bắn
                    return 10;
                case 0: // ô trống
                    return 0;
                case 1: // 1-5 vị trí của thiết bị
                case 2:
                case 3:
                case 4:
                case 5:
                    return 1;
                case -1: // vị trí bắn trúng giá trị là số đối của giá trị ô chứa thiết bị
                case -2:
                case -3:
                case -4:
                case -5:
                    return -1;
            }
        }
        return 20;
    }

    @SuppressLint("SetTextI18n")
    public void increaseMana(int manaNumber){
        if (myCurrentMana >= MAXMANA){
            return;
        }
        int newMana = myCurrentMana + manaNumber;
        changeInterfaceManaProgressBar(newMana, myCurrentMana);
        myCurrentMana = newMana;
        manaTextView.setText("Mana: " + myCurrentMana + "/10");
    }

    @SuppressLint("SetTextI18n")
    public void decreaseMana (int manuNumber){
        if (myCurrentMana <= 0){
            return;
        }
        int newMana = myCurrentMana - manuNumber;
        changeInterfaceManaProgressBar(newMana, myCurrentMana);
        myCurrentMana = newMana;
        manaTextView.setText("Mana: " + myCurrentMana + "/10");
    }

    public void loadManaProgressBar(int currentMana){
        final int MAX = 100;
        manaProgressBar.setMax(MAX);
        final int currentManaPer100 = currentMana * 10;
        manaProgressBar.setProgress(currentManaPer100);

    }
    public void changeInterfaceManaProgressBar(int currentMana, int beforeMana){
        Handler handler = new Handler();
        final int MAX = 100;
        manaProgressBar.setMax(MAX);
        int currentManaPer100 = currentMana * 10;
        int beforeManaPer100 = beforeMana * 10;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(beforeManaPer100 <= currentManaPer100) {
                    for (int i = beforeManaPer100; i <= currentManaPer100; i++) {
                        final int progress = i;
                        SystemClock.sleep(30);

                        //update interface
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                manaProgressBar.setProgress(progress);
                            }
                        });
                    }
                }
                else{
                    for (int i = beforeManaPer100; i >= currentManaPer100; i--) {
                        final int progress = i;
                        SystemClock.sleep(30);

                        //update interface
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                manaProgressBar.setProgress(progress);
                            }
                        });
                    }
                }
            }
        });
        thread.start();
    }
}