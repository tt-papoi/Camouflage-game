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
import android.widget.TextView;
import android.widget.Toast;

import com.example.camouflagegame.R;
import com.example.camouflagegame.Game.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignUpActivity extends AppCompatActivity {

    private EditText edTxt_Email, edTxt_Password, edTxt_Confirm_Password;
    private TextView txtSignInNow;
    private ImageButton btnSignUp, btnBack;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_screen);

        edTxt_Email = findViewById(R.id.editTextUsername_SignUpScreen);
        edTxt_Password = findViewById(R.id.editTextPassword_SignUpScreen);
        edTxt_Confirm_Password = findViewById(R.id.editTextRetypePassword_SignUpScreen);
        btnSignUp = findViewById(R.id.btnSignUp_SignUpScreen);
        txtSignInNow = findViewById(R.id.txtLoginNow);
        btnBack = findViewById(R.id.btnBack_SignUpScreen);

        progressDialog = new ProgressDialog(this);

        // Su kien click Sign up account!
        SignUpAccount_Click();

        ClickToBack();

        ClickToSignInNow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void ClickToBack(){
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void pushDataToCloudDB(User user){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("list_user");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String pathObject = String.valueOf(auth.getUid());

        user.setUserID(auth.getUid());

        reference.child(pathObject).setValue(user);
    }

    private void SignUpAccount_Click(){
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check chua nhap du thong tin thong bao nguoi dung
                // Lấy giá trị từ các trường nhập liệu
                String email = edTxt_Email.getText().toString();
                String password = edTxt_Password.getText().toString();
                String confirmPassword = edTxt_Confirm_Password.getText().toString();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {

                    Toast.makeText(SignUpActivity.this, "Please enter full email and password and confirm password", Toast.LENGTH_SHORT).show();
                }
                else if (!password.equals(confirmPassword)) {

                    Toast.makeText(SignUpActivity.this, "Confirmation password does not match", Toast.LENGTH_SHORT).show();
                }

                else {
                    // Tiến hành xử lý đăng ký khi đã nhập đủ thông tin
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String strEmail = edTxt_Email.getText().toString().trim();
                    String strPassword = edTxt_Password.getText().toString().trim();

                    progressDialog.show();

                    // tao account xu li tren firebase
                    auth.createUserWithEmailAndPassword(strEmail, strPassword)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {

                                        // day du lieu User len realtime database cua Firebase neu nhu la account moi
                                        // nghia la account co id = "0"
                                        User user = User.loadFromSharedPreferences(SignUpActivity.this);
                                        if (user.getUserID().equals("0")){
                                            pushDataToCloudDB(user);
                                            user.saveToSharedPreferences(SignUpActivity.this);
                                        }

                                        finishAffinity();

                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        startActivity(intent);

                                    }

                                    else {
                                        // If sign in fails, display a message to the user
                                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void ClickToSignInNow(){
        txtSignInNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}