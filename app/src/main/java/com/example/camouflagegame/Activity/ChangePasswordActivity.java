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
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edTxt_Password_ChangePass, edTxt_Confirm_Password_ChangePass;
    private ImageButton btnChangePass, btnBackToSetting;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_screen);

        edTxt_Password_ChangePass = findViewById(R.id.editTextNewPassword_ChangePasswordScreen);
        edTxt_Confirm_Password_ChangePass = findViewById(R.id.editTextRetypePassword_ChangePasswordScreen);

        btnChangePass = findViewById(R.id.btnConfirm_ChangePasswordScreen);
        btnBackToSetting = findViewById(R.id.btnBack_ChangePasswordScreen);

        progressDialog = new ProgressDialog(this);


        ClickChangePass();

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

    private void ClickChangePass(){
        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Password = edTxt_Password_ChangePass.getText().toString().trim();
                String ConfirmPassword = edTxt_Confirm_Password_ChangePass.getText().toString().trim();

                if (!Password.equals(ConfirmPassword)) {

                    Toast.makeText(ChangePasswordActivity.this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                }

                else {
                    progressDialog.show();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    user.updatePassword(Password)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(ChangePasswordActivity.this, "Change password success!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            });
                }
            }
        });

    }
}