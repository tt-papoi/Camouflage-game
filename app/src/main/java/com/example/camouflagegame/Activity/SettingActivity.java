package com.example.camouflagegame.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.camouflagegame.Commander.Commander;
import com.example.camouflagegame.Connection.NetworkUtil;
import com.example.camouflagegame.Game.AudioController;
import com.example.camouflagegame.Game.User;
import com.example.camouflagegame.R;
import com.example.camouflagegame.sharedpreferences.AppSharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

public class SettingActivity extends AppCompatActivity {
    private ImageButton btnBack, btnSignOut, btnLogin, btnSignUp, btnUpLoad, btnDownload, btnChangePass, btnExit;
    private Switch switchMusic, switchSFX;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private String pathObject;
    private ProgressDialog progressDialog;
    private Handler handler;
    private String isInGameScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_screen);
        btnSignOut = findViewById(R.id.btnSignOut_SettingScreen);
        btnBack = findViewById(R.id.btnBack_SettingScreen);
        btnLogin = findViewById(R.id.btnLogin_SettingScreen);
        btnDownload = findViewById(R.id.btnDownloadData_SettingScreen);
        btnUpLoad = findViewById(R.id.btnUpLoadData_SettingScreen);
        btnChangePass = findViewById(R.id.btnChangePassword_SettingScreen);
        btnSignUp = findViewById(R.id.btnSignUp_SettingScreen);
        btnExit = findViewById(R.id.btnExitFromGameScreen);
        isInGameScreen = getIntent().getStringExtra("GameScreen");
        initialize();

    }

    public void initialize()
    {
        switchSFX = findViewById(R.id.switchSFX);
        switchMusic = findViewById(R.id.switchMusic);

        AppSharedPreferences appSharedPreferences = new AppSharedPreferences(this);

        switchMusic.setChecked(appSharedPreferences.getMusicStatus());
        switchSFX.setChecked(appSharedPreferences.getSoundStatus());

        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AudioController.playMusic(MainActivity.mainActivityContext);
            } else {
                switchMusic.setChecked(false);
                AudioController.stopMusic();
            }
        });

        switchSFX.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AudioController.initSoundPool(MainActivity.mainActivityContext);
            } else {
                switchSFX.setChecked(false);
                AudioController.releaseSoundPool();
            }
        });

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("list_user");
        auth = FirebaseAuth.getInstance();
        pathObject = String.valueOf(auth.getUid());
        progressDialog = new ProgressDialog(this);

        // Nhan thong bao khi trang thai dang nhap cua nguoi dung thay doi
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null){
                    btnSignUp.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.GONE);
                    btnDownload.setVisibility(View.VISIBLE);
                    btnUpLoad.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.VISIBLE);
                    btnChangePass.setVisibility(View.VISIBLE);
                }
                else{
                    btnSignUp.setVisibility(View.VISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                    btnDownload.setVisibility(View.GONE);
                    btnUpLoad.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.GONE);
                    btnChangePass.setVisibility(View.GONE);
                }
                if(isInGameScreen != null && isInGameScreen.equals("GameScreen"))
                {
                    btnSignUp.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.GONE);
                    btnDownload.setVisibility(View.GONE);
                    btnUpLoad.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.GONE);
                    btnChangePass.setVisibility(View.GONE);
                    btnExit.setVisibility(View.VISIBLE);
                }
            }
        });

        handleClickedButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void handleClickedButton()
    {
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setMessage("Do you really want to quit the game?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            finishAffinity();
                            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel());
                final AlertDialog alert = builder.create();
                alert.show();
        }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventOfSignoutButton();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.isNetworkAvailable(SettingActivity.this)) {
                    Intent intent = new Intent(SettingActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(SettingActivity.this, "No internet, please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkUtil.isNetworkAvailable(SettingActivity.this)) {
                    Intent intent = new Intent(SettingActivity.this, SignUpActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(SettingActivity.this, "No internet, please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUpLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.isNetworkAvailable(SettingActivity.this)) {
                    saveDataFromLocalToCloud();
                }
                else{
                    Toast.makeText(SettingActivity.this, "No internet, please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnDownload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                eventOfDownloadButton();
            }
        });

        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, LogInToAuthenticationActivity.class);
                startActivity(intent);
            }
        });
    }


    // Sử lý sự kiện cho Sign out Button
    private void eventOfSignoutButton(){
        reference.child(pathObject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User value = snapshot.getValue(User.class);
                User currentUser = User.loadFromSharedPreferences(SettingActivity.this);
                if (value.getExp() < currentUser.getExp()) {
                    warningDialog(SettingActivity.this, "WARNING",
                            "You have not saved the data, if you log out you will lose the data",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    progressDialog.show();
                                    handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            User user = new User("0", 0, 0);
                                            user.saveToSharedPreferences(SettingActivity.this);
                                            Commander commander =  new Commander("002", "002", "Napole Ponale", R.drawable.commander02, 0);
                                            commander.saveToSharedPreferences(SettingActivity.this);
                                            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            FirebaseAuth auth = FirebaseAuth.getInstance();
                                            auth.signOut();
                                            finishAffinity();
                                        }
                                    }, 1000);

                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                }
                else {
                    progressDialog.show();
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            User user = new User("0",0,0);
                            user.saveToSharedPreferences(SettingActivity.this);
                            Commander commander =  new Commander("002", "002", "Napole Ponale", R.drawable.commander02, 0);
                            commander.saveToSharedPreferences(SettingActivity.this);
                            Intent intent = new Intent(SettingActivity.this, MainActivity.class );
                            startActivity(intent);
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            auth.signOut();
                            finishAffinity();
                        }
                    }, 1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Xử lý sự kiện cho DownLoad Button
    private void eventOfDownloadButton(){
        reference.child(pathObject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                User currentUser = User.loadFromSharedPreferences(SettingActivity.this);

                if(user.getExp() < currentUser.getExp()){
                    successDialog(SettingActivity.this, "Notice",
                            "Your current data is already the latest, you don't need to download the data, instead you can upload the data to avoid loss.");
                }
                else{
                    if (NetworkUtil.isNetworkAvailable(SettingActivity.this)) { // neu thiet bi co ket noi mang moi thuc hien duoc
                        loadDataFromCloudToLocal();
                    }
                    else{
                        Toast.makeText(SettingActivity.this, "No internet, please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void loadDataFromCloudToLocal(){
        reference.child(pathObject).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                User currentUser = User.loadFromSharedPreferences(SettingActivity.this);


                if (user.getUserID().equals(currentUser.getUserID())){ // neu UID tren cloud = UID tren local thi moi thuc hien load data ve
                    user.saveToSharedPreferences(SettingActivity.this);
                    EventBus.getDefault().post("loadDataFromRef");
                    Toast.makeText(SettingActivity.this,"Downloaded data from cloud", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void saveDataFromLocalToCloud(){
        reference.child(pathObject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                User currentUser = User.loadFromSharedPreferences(SettingActivity.this);

                if (user.getUserID().equals(currentUser.getUserID())){
                    // neu UID tren cloud = UID tren local thi moi thuc hien load data len
                    reference.child(pathObject).setValue(currentUser);
                    Toast.makeText(SettingActivity.this,"Saved data to cloud", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SettingActivity.this,"UID do not match", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Hiển thị cửa sổ cảnh báo
    public static void warningDialog(Context context, String title, String message,
                                     DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View customLayout = LayoutInflater.from(context).inflate(R.layout.layout_warning_dialog, null);

        TextView textViewTitle = customLayout.findViewById(R.id.textTitleWarningDialog);
        TextView textViewMessage = customLayout.findViewById(R.id.textMessageWarningDialog);
        Button btnOK = customLayout.findViewById(R.id.btnOKWarningDialog);
        Button btnCancel = customLayout.findViewById(R.id.btnCancelWarningDialog);

        textViewTitle.setText(title);
        textViewMessage.setText(message);

        builder.setView(customLayout);
        AlertDialog dialog = builder.create();
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(okListener!=null){
                    okListener.onClick(dialog,DialogInterface.BUTTON_POSITIVE);
                }
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cancelListener!=null){
                    cancelListener.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public static void successDialog(Context context, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View customLayout = LayoutInflater.from(context).inflate(R.layout.layout_success_dialog, null);

        TextView textViewTitle = customLayout.findViewById(R.id.textTitleSuccessDialog);
        TextView textViewMessage = customLayout.findViewById(R.id.textMessageSuccessDialog);
        Button btnOK = customLayout.findViewById(R.id.btnOkSuccessDialog);

        textViewTitle.setText(title);
        textViewMessage.setText(message);

        builder.setView(customLayout);
        AlertDialog dialog = builder.create();

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppSharedPreferences appSharedPreferences = new AppSharedPreferences(this);
        appSharedPreferences.putMusicStatus(switchMusic.isChecked());
        appSharedPreferences.putSoundStatus(switchSFX.isChecked());
    }

}