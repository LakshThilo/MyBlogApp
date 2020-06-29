package uk.ac.wlv.myblogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {


    private Button reg_create_account_btn;
    private EditText register_email;
    private EditText register_password;
    private EditText register_conform_password;
    private EditText register_phoneNumber;
    private ProgressBar register_ProgressBar;

    private FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();

        register_email = findViewById(R.id.register_email);
        register_conform_password = findViewById(R.id.register_conform_password);
        register_password = findViewById(R.id.register_password);
        register_phoneNumber = findViewById(R.id.register_phone_number);
        register_ProgressBar = findViewById(R.id.register_progressBar);

        reg_create_account_btn = findViewById(R.id.register_create_account);

        reg_create_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = register_email.getText().toString();
                String phoneNo = register_phoneNumber.getText().toString();
                String password = register_password.getText().toString();
                String conform_password = register_conform_password.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(phoneNo) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(conform_password)){

                    // password and conform password both equals means User can create a account
                    if(password.equals(conform_password)){

                        register_ProgressBar.setVisibility(View.VISIBLE);
                        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    Intent registerIntent = new Intent(RegisterActivity.this, HomeActivity.class);
                                    startActivity(registerIntent);
                                    finish();
                                    //sendToHome();

                                }else{

                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"Error: "+ errorMessage, Toast.LENGTH_SHORT).show();
                                }
                                register_ProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }else{

                        Toast.makeText(RegisterActivity.this,"Conform Password and Password does not math,", Toast.LENGTH_SHORT).show();
                    }

                } else{
                    Toast.makeText(RegisterActivity.this,"All fields can not be empty!,", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if(firebaseUser != null){


            sendToHome();
        }
    }

    private void sendToHome() {

        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
