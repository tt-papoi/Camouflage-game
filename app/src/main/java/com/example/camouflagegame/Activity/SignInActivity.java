package com.example.camouflagegame.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camouflagegame.Commander.Commander;
import com.example.camouflagegame.R;
import com.example.camouflagegame.Game.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {

    private EditText edTxt_Email, edTxt_Password;
    private ImageButton btnSignIn, btnBack;
    private TextView txtSignUpNow, txtForgotPass;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        edTxt_Email = findViewById(R.id.editTextUsername_LoginScreen);
        edTxt_Password = findViewById(R.id.editTextPassword_LoginScreen);
        btnSignIn = findViewById(R.id.btnLogin_LoginScreen);
        txtForgotPass = findViewById(R.id.txtForgotPassword);
        txtSignUpNow = findViewById(R.id.txtRegisterNow);
        btnBack = findViewById(R.id.btnBack_LoginScreen);

        progressDialog = new ProgressDialog(this);

        ClickToBack();

        SignInClick();

        SignUpNowViewTxt();

        ClickForgotPassword();
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

    private void SignUpNowViewTxt(){
        txtSignUpNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // chuyen qua Sign up
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public interface OnTaskCompleted {
        void onTaskCompleted();
    }
    private void saveDataFromCloudDB(OnTaskCompleted callback){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("list_user");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String pathObject = String.valueOf(auth.getUid());

        reference.child(pathObject).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user1 = snapshot.getValue(User.class);
                user1.saveToSharedPreferences(SignInActivity.this);
                callback.onTaskCompleted();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SignInClick(){
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = edTxt_Email.getText().toString().trim();
                String password = edTxt_Password.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    // Kiểm tra xem bất kỳ trường nào chưa được nhập
                    Toast.makeText(SignInActivity.this, "Please enter full email and password", Toast.LENGTH_SHORT).show();
                }
                else{

                    FirebaseAuth auth = FirebaseAuth.getInstance();

                    progressDialog.show();
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {

                                        // neu dang nhap account khac ma UID o local da co thi lay du lieu cua cloud ve
                                        saveDataFromCloudDB(new OnTaskCompleted() {
                                            @Override
                                            public void onTaskCompleted() {
                                                Commander commander =  new Commander("002", "002", "Napole Ponale", R.drawable.commander02, 0);
                                                commander.saveToSharedPreferences(SignInActivity.this);
                                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finishAffinity();
                                            }
                                        });
                                    }

                                    else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(SignInActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void ClickForgotPassword(){
        txtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}