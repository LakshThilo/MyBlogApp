package uk.ac.wlv.myblogapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import id.zelory.compressor.Compressor;


public class HomeActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FloatingActionButton addPostBtn;
    private FirebaseFirestore firebaseFirestore;
    private BottomNavigationView bottomNavigationView;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    private String current_userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // initialize FireBase auth-> getting instance of FireBase

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        mainToolbar = findViewById(R.id.main_toolBar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("BLOGGER");

        // before any other thing happen first load the Home Fragment First
       // replaceFragment(homeFragment);

        // initializing fragments
        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        replaceFragement(homeFragment);

        // OnClick Listener for addPost
        addPostBtn = findViewById(R.id.addPostBtn);
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, NewPostActivity.class);
                startActivity(intent);
            }
        });

        // setOnNavigationItemSelectedListener for Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_Navi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.bottom_navi_home:
                        replaceFragement(homeFragment);
                        return true;

                    case R.id.bottom_navi_notification:
                        replaceFragement(notificationFragment);
                        return true;

                    case R.id.bottom_navi_account:
                        replaceFragement(accountFragment);
                        return true;

                    default:
                        return false;
                }

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_account_logout:

                // when user press logout button in tool bat this Logout method will call
                logOut();
                return true;
            case R.id.action_account_setting:
                loggingToAccountSettings();
            default:
            return false;
        }
    }

    private void loggingToAccountSettings() {
        Intent intent = new Intent(HomeActivity.this, UserAccountActivity.class);
        startActivity(intent);
    }

    private void logOut() {

        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser ==null){

            sendToLogin();
        }else{

            // In this section what doing is error handling when Uer try to post New Post without entering personal details app automatically sent to the User Account page
            current_userID = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){

                        if(!task.getResult().exists()){

                            Intent intent = new Intent(HomeActivity.this, UserAccountActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else{
                        String error = task.getException().getMessage();
                        Toast.makeText(HomeActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }


    // using this method we can replacing the Fragment
    private void replaceFragement(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment_container,fragment);
        fragmentTransaction.commit();

        // now we need to trigger this action when the click button


    }
}
