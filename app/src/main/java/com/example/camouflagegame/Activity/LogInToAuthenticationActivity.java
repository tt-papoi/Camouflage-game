package com.example.camouflagegame.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.camouflagegame.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogInToAuthenticationActivity extends AppCompatActivity {


    private EditText edTxt_Email_Authentication, edTxt_Password_Authentication;
    private ImageButton btnLogInToAuthentication, btnBackToSetting;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_to_authentication);

        edTxt_Email_Authentication = findViewById(R.id.editTextUsername_Authentication);
        edTxt_Password_Authentication = findViewById(R.id.editTextPassword_Authentication);
        btnBackToSetting = findViewById(R.id.btnBackToSettingScreenFromAuthen);
        btnLogInToAuthentication = findViewById(R.id.btnLogin_Authentication);
        progressDialog = new ProgressDialog(this);

        ClickLogInToAuthentication();

        ClickBackToSetting();

    }


    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    private void ClickBackToSetting(){
        btnBackToSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void ClickLogInToAuthentication(){
        btnLogInToAuthentication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailAu = edTxt_Email_Authentication.getText().toString().trim();
                String passwordAu = edTxt_Password_Authentication.getText().toString().trim();

                if (emailAu.isEmpty() || passwordAu.isEmpty()) {
                    // Kiểm tra xem bất kỳ trường nào chưa được nhập
                    Toast.makeText(LogInToAuthenticationActivity.this, "Vui lòng nhập đủ email, password", Toast.LENGTH_SHORT).show();
                }
                else{

                    FirebaseAuth auth = FirebaseAuth.getInstance();

                    progressDialog.show();
                    auth.signInWithEmailAndPassword(emailAu, passwordAu)
                            .addOnCompleteListener(LogInToAuthenticationActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {

                                        // Sign in success, update UI with the signed-in user's information
                                        Intent intent = new Intent(LogInToAuthenticationActivity.this, ChangePasswordActivity.class);
                                        startActivity(intent);

                                        finish();
                                    }

                                    else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(LogInToAuthenticationActivity.this, "Authentication failed, please check again.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}