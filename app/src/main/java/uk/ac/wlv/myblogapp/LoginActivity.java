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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Button login_login_btn;
    private Button login_create_account;
    private EditText login_email;
    private EditText login_password;
    private ProgressBar login_ProcessBar;

    private FirebaseAuth mFirebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();

        login_login_btn = findViewById(R.id.login_login_account);
        login_create_account = findViewById(R.id.login_create_account);
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        login_ProcessBar = findViewById(R.id.login_progressBar);




        // login to account
        login_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String loginEmail = login_email.getText().toString();
                String loginPassword = login_password.getText().toString();

                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)){
                    login_ProcessBar.setVisibility(View.VISIBLE);

                    mFirebaseAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                sendToMain();
                            }else{
                                String errorMessage = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error: "+ errorMessage,Toast.LENGTH_SHORT).show();
                            }
                            login_ProcessBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }



            }
        });

        // create a new account
        login_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        if(currentUser != null){

            sendToMain();
        }
    }

    private void sendToMain() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
