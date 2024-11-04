package com.example.camouflagegame.Activity;

import static com.example.camouflagegame.Activity.LobbyActivity.client;
import static com.example.camouflagegame.Activity.LobbyActivity.isHost;
import static com.example.camouflagegame.Activity.LobbyActivity.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.camouflagegame.Game.GameInfo;
import com.example.camouflagegame.Game.TankInfo;
import com.example.camouflagegame.R;

import java.util.ArrayList;
import java.util.Random;


public class DeployEquipmentActivity extends AppCompatActivity {
    public static boolean isRunning = false;
    public static GameInfo gameInfo = null;
    ConstraintLayout deployEquipmentMap;
    private ImageButton btnReady;
    private ImageView[] tanks;
    public ArrayList<TankInfo> tankInfos;
    private int dX, dY;
    private int[] locationOnMap;
    int[] posMap;
    int[] sizeMap;
    int[] sizeCell;
    int isRotated = 0;
    Handler deployHandler;
    int timer = 30;
    Thread timerThread;
    TextView textViewTimer;
    public void startTimerThread()
    {
        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && timer > 0)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    timer -=1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            textViewTimer.setText(timer+"s");
                        }
                    });
                    if(timer == 0)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                saveTanksLocation();
                                btnReady.setEnabled(false);
                                gameInfo.isMyReadyButtonClicked = true;
                                gameInfo.TYPE = gameInfo.SEND_STARTED_MAP;
                                if(isHost)
                                {
                                    randomFirstPlayer();
                                    LobbyActivity.server.sendInfo(new GameInfo(gameInfo));
                                }
                                else
                                {
                                    LobbyActivity.client.sendInfo(new GameInfo(gameInfo));
                                }
                                Thread waitForEnemy = new Thread(() -> {
                                    while(true)
                                    {
                                        if(gameInfo.isReady())
                                        {

                                            DeployEquipmentActivity.this.runOnUiThread(() -> {
                                                Intent intent = new Intent(DeployEquipmentActivity.this, GameActivity.class);
                                                intent.putParcelableArrayListExtra("TANK_INFO", tankInfos);
                                                startActivity(intent);
                                            });
                                            break;
                                        }
                                    }
                                });
                                waitForEnemy.start();
                                getTankInfo();
                                if(timerThread != null)
                                {
                                    timerThread.interrupt();
                                    timerThread= null;
                                }
                                textViewTimer.setText("Ready");
                            }
                        });
                        break;
                    }
                }
            }
        });
        timerThread.start();
    }
    public void createDeployHandler()
    {
        deployHandler = new Handler(msg -> {
            switch (msg.what)
            {
                case -1:
                    new AlertDialog.Builder(DeployEquipmentActivity.this)
                            .setTitle("Lost connection")
                            .setMessage("Connection is not stable\n")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAffinity();
                                    Intent intent = new Intent(DeployEquipmentActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }).show();
            }
            return true;
        });
        if(isHost)
        {
            server.setDeployHandler(deployHandler);
        }
        else
        {
            client.setDeployHandler(deployHandler);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deploy_equipment_screen);
        initialize();
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initialize()
    {
        gameInfo = new GameInfo();
        tanks = new ImageView[5];
        tanks[0] = findViewById(R.id.tank1);
        tanks[1] = findViewById(R.id.tank2);
        tanks[2] = findViewById(R.id.tank3);
        tanks[3] = findViewById(R.id.tank4);
        tanks[4] = findViewById(R.id.tank5);
        textViewTimer = findViewById(R.id.textViewTimer_DeployEquipmentScreen);
        btnReady = findViewById(R.id.btnReady);

        deployEquipmentMap = findViewById(R.id.deploy_equipment_map);

        for (ImageView tank : tanks) {
            tank.setOnTouchListener(createTanksTouchListener());
        }

        posMap = new int[2];
        sizeMap = new int[2];
        sizeCell = new int[2];

        locationOnMap = new int[2];
        deployEquipmentMap.post(() -> {
            sizeMap[0] = deployEquipmentMap.getWidth();
            sizeMap[1] = deployEquipmentMap.getHeight();
            sizeCell[0] = sizeMap[0] / 10;
            sizeCell[1] = sizeMap[1] / 10;
        });
        handleClickedReadyButton();
        createDeployHandler();
        startTimerThread();

    }
    public void getTankInfo()
    {
        tankInfos = new ArrayList<>();
        for (ImageView imageView : tanks) {
            TankInfo tank = new TankInfo();
            tank.x = (int) (imageView.getX() / convertDpToPixels(30f, DeployEquipmentActivity.this));
            tank.y = (int) (imageView.getY() / convertDpToPixels(30f, DeployEquipmentActivity.this));
            tank.isVertical = imageView.getWidth() / (int) (convertDpToPixels(29f, DeployEquipmentActivity.this)) <= 1;
            //tank.drawable = (BitmapDrawable) imageView.getBackground();
            tankInfos.add(tank);
        }
    }
    public void handleClickedReadyButton()
    {
        btnReady.setOnClickListener(v -> {
            saveTanksLocation();
            btnReady.setEnabled(false);
            gameInfo.isMyReadyButtonClicked = true;
            gameInfo.TYPE = gameInfo.SEND_STARTED_MAP;
            if(isHost)
            {
                randomFirstPlayer();
                LobbyActivity.server.sendInfo(new GameInfo(gameInfo));
            }
            else
            {
                LobbyActivity.client.sendInfo(new GameInfo(gameInfo));
            }
            Thread waitForEnemy = new Thread(() -> {
                while(true)
                {
                    if(gameInfo.isReady())
                    {

                        DeployEquipmentActivity.this.runOnUiThread(() -> {
                            Intent intent = new Intent(DeployEquipmentActivity.this, GameActivity.class);
                            intent.putParcelableArrayListExtra("TANK_INFO", tankInfos);
                            startActivity(intent);
                        });
                        break;
                    }
                }
            });
            waitForEnemy.start();
            getTankInfo();
            if(timerThread != null)
            {
                timerThread.interrupt();
                timerThread= null;
            }
            textViewTimer.setText("Ready");
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener createTanksTouchListener() {
        return (view, event) -> {
            deployEquipmentMap.getLocationOnScreen(posMap);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = (int) (view.getX() - event.getRawX());
                    dY = (int) (view.getY() - event.getRawY());

                    isRotated = 0;
                    break;

                case MotionEvent.ACTION_MOVE:
                    isRotated++;
                    locationOnMap[0] = (int) (dX + event.getRawX());
                    locationOnMap[1] = (int) (dY + event.getRawY());

                    int heightTank = view.getHeight() + (int)convertDpToPixels(1,this);
                    int widthTank = view.getWidth() + (int)convertDpToPixels(1,this);

                    if (locationOnMap[0] < 0) locationOnMap[0] = 0;
                    if (locationOnMap[1] < 0) locationOnMap[1] = 0;

                    if ((locationOnMap[0] + widthTank) > sizeMap[0])
                        locationOnMap[0] = sizeMap[0] / 10 * (10 - widthTank / (sizeMap[0] / 10));
                    if ((locationOnMap[1] + heightTank) > sizeMap[1])
                        locationOnMap[1] = sizeMap[1] / 10 * (10 - heightTank / (sizeMap[1] / 10));

                    if (locationOnMap[0] - (locationOnMap[0] / (sizeMap[0] / 10)) * (sizeMap[0] / 10) > (sizeMap[0] / 10) / 2) {
                        locationOnMap[0] = locationOnMap[0] / (sizeMap[0] / 10);
                        locationOnMap[0] = (locationOnMap[0] + 1) * (sizeMap[0] / 10);
                    } else {
                        locationOnMap[0] = locationOnMap[0] / (sizeMap[0] / 10);
                        locationOnMap[0] = locationOnMap[0] * (sizeMap[0] / 10);
                    }

                    if (locationOnMap[1] - (locationOnMap[1] / (sizeMap[1] / 10)) * (sizeMap[1] / 10) > (sizeMap[1] / 10) / 2) {
                        locationOnMap[1] = locationOnMap[1] / (sizeMap[1] / 10);
                        locationOnMap[1] = (locationOnMap[1] + 1) * (sizeMap[1] / 10);
                    } else {
                        locationOnMap[1] = locationOnMap[1] / (sizeMap[1] / 10);
                        locationOnMap[1] = locationOnMap[1] * (sizeMap[1] / 10);
                    }
                    float valueOfDp = convertDpToPixels(1f, this);
                    view.animate()
                            .x((locationOnMap[0] / sizeCell[0]) * 30 * valueOfDp )
                            .y((locationOnMap[1] / sizeCell[1]) * 30 * valueOfDp )
                            .setDuration(0)
                            .start();
                    view.post(this::highlightInvalidTanks);
                    break;
                case MotionEvent.ACTION_UP:

                    locationOnMap[0] = (int) (dX + event.getRawX());
                    locationOnMap[1] = (int) (dY + event.getRawY());

                    heightTank = view.getHeight() + (int)convertDpToPixels(1,this);
                    widthTank = view.getWidth() + (int)convertDpToPixels(1,this);
                    if (isRotated < 5)
                    {
                        heightTank = view.getWidth() + (int) convertDpToPixels(1,this);
                        widthTank = view.getHeight() +(int) convertDpToPixels(1,this);
                        view.getLayoutParams().height = view.getWidth();
                        view.getLayoutParams().width = view.getHeight();
                        view.requestLayout();
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) view.getBackground();
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        bitmap = rotateBitmap(bitmap, 90);
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        view.setBackground(drawable);
                        isRotated = 0;
                    }

                    if (locationOnMap[0] < 0) locationOnMap[0] = 0;
                    if (locationOnMap[1] < 0) locationOnMap[1] = 0;

                    if ((locationOnMap[0] + widthTank) > sizeMap[0])
                        locationOnMap[0] = sizeMap[0] / 10 * (10 - widthTank / (sizeMap[0] / 10));
                    if ((locationOnMap[1] + heightTank) > sizeMap[1])
                        locationOnMap[1] = sizeMap[1] / 10 * (10 - heightTank / (sizeMap[1] / 10));

                    if (locationOnMap[0] - (locationOnMap[0] / (sizeMap[0] / 10)) * (sizeMap[0] / 10) > (sizeMap[0] / 10) / 2) {
                        locationOnMap[0] = locationOnMap[0] / (sizeMap[0] / 10);
                        locationOnMap[0] = (locationOnMap[0] + 1) * (sizeMap[0] / 10);
                    } else {
                        locationOnMap[0] = locationOnMap[0] / (sizeMap[0] / 10);
                        locationOnMap[0] = locationOnMap[0] * (sizeMap[0] / 10);
                    }

                    if (locationOnMap[1] - (locationOnMap[1] / (sizeMap[1] / 10)) * (sizeMap[1] / 10) > (sizeMap[1] / 10) / 2) {
                        locationOnMap[1] = locationOnMap[1] / (sizeMap[1] / 10);
                        locationOnMap[1] = (locationOnMap[1] + 1) * (sizeMap[1] / 10);
                    } else {
                        locationOnMap[1] = locationOnMap[1] / (sizeMap[1] / 10);
                        locationOnMap[1] = locationOnMap[1] * (sizeMap[1] / 10);
                    }
                    valueOfDp = convertDpToPixels(1f, this);
                    view.animate()
                            .x((locationOnMap[0] / sizeCell[0]) * 30 * valueOfDp )
                            .y((locationOnMap[1] / sizeCell[1]) * 30 * valueOfDp )
                            .setDuration(0)
                            .start();
                    isRotated = 0;
                    view.post(() -> btnReady.setEnabled(!highlightInvalidTanks()));
                    break;

                default:
                    return false;
            }

            return true;
        };
    }
    public static Bitmap rotateBitmap(Bitmap original, float degree)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(original,0,0, original.getWidth(), original.getHeight(), matrix, true);
    }
    public void randomFirstPlayer()
    {
        Random ran = new Random();
        gameInfo.goFirst = ran.nextBoolean();
    }
    public boolean isViewOverlapping(View v1, View v2) {
        Rect rect1 = new Rect((int)v1.getX() + 5, (int)v1.getY() + 5, (int)v1.getX() + v1.getWidth() - 5, (int)v1.getY() + v1.getHeight() - 5);
        Rect rect2 = new Rect((int)v2.getX() + 5, (int)v2.getY() + 5, (int)v2.getX() + v2.getWidth() - 5, (int)v2.getY() + v2.getHeight() - 5);
        return Rect.intersects(rect1, rect2);
    }
    public boolean highlightInvalidTanks()
    {
        boolean result = false;
        boolean isHighlighted = false;
        for(int i = 0; i < tanks.length ; i++)
        {
            for(int j = 0; j < tanks.length; j++)
            {
                if(i == j)
                {
                    continue;
                }
                if(isViewOverlapping(tanks[i], tanks[j]))
                {
                    tanks[i].setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.backgroundTintTanks));
                    tanks[i].setBackgroundTintMode(PorterDuff.Mode.DST_OVER);
                    isHighlighted = true;
                    result = true;
                }
            }
            if(!isHighlighted)
            {
                tanks[i].setBackgroundTintList(null);
            }
            isHighlighted = false;
        }
        return result;
    }
    public void saveTanksLocation()
    {
        int startX;
        int startY;
        int lenX;
        int lenY;
        gameInfo.resetYourMap();
        for (int i = 0; i < tanks.length; i++) {
            startX = (int) (tanks[i].getX() / sizeCell[0]);
            startY = (int) (tanks[i].getY() / sizeCell[1]);
            lenX = (int) ((tanks[i].getWidth() + convertDpToPixels(1, this)) / sizeCell[0]);
            lenY = (int) ((tanks[i].getHeight() + convertDpToPixels(1, this)) / sizeCell[1]);
            for(int x = 0; x < lenX; x++)
            {
                gameInfo.yourMap[startY][startX + x] = i + 1;
            }
            for(int y = 0; y < lenY; y++) {

                gameInfo.yourMap[startY + y][startX] = i + 1;
            }
        }
    }
    public static float convertDpToPixels(float dp, Context context) {
        return  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}
