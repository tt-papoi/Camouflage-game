package com.example.camouflagegame.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.camouflagegame.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edTxt_Email_ForgotPassword;
    private ImageButton btnSentEmailForResetPassword, btnBackToLogIn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_screen);

        edTxt_Email_ForgotPassword = findViewById(R.id.editTextEmail_ForgotPasswordScreen);
        btnSentEmailForResetPassword = findViewById(R.id.btnSend_ForgotPasswordScreen);
        btnBackToLogIn = findViewById(R.id.btnBack_ForgotPasswordScreen);

        progressDialog = new ProgressDialog(this);

        ClickSentToEmailForResetPassword();
        ClickBackToLogIn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void ClickSentToEmailForResetPassword(){
        btnSentEmailForResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = edTxt_Email_ForgotPassword.getText().toString().trim();
                progressDialog.show();

                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                progressDialog.dismiss();

                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Email sent, please check.", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(ForgotPasswordActivity.this, "Email does not exist, please check. ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    private void ClickBackToLogIn(){
        btnBackToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}