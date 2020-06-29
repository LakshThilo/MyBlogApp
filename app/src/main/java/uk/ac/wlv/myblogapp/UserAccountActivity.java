package uk.ac.wlv.myblogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAccountActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView profile_image;
    private EditText userName;
    private EditText userPhoneNo;
    private Button saveUserDetails;
    private ProgressBar progressBar;

    private String user_id;
    private StorageReference storageReference;
    private StorageReference storageReferenceForImage;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;


    private Uri mainImageUri = null;
    private boolean isChanged= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        storageReferenceForImage = FirebaseStorage.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.user_account_toolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");

        profile_image = findViewById(R.id.profile_image);
        userName = findViewById(R.id.user_account_username);
        userPhoneNo = findViewById(R.id.user_account_phone_number);
        saveUserDetails = findViewById(R.id.save_account_settings);
        progressBar = findViewById(R.id.account_progressBar);

        progressBar.setVisibility(View.VISIBLE);
        saveUserDetails.setEnabled(false);

        // Retrieving data from database in Authentication get() means only once we don't need it multiple times
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    // check whether data exist or not in this current path ->(firebaseFirestore.collection("Users").document(user_id).get())
                    if(task.getResult().exists()){

                        Toast.makeText(UserAccountActivity.this,"Data Exist: ", Toast.LENGTH_SHORT).show();

                        //getting image and Name
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);
                        userName.setText(name);

                        // creating place holder for image
                        RequestOptions placeHolder = new RequestOptions();
                        placeHolder.placeholder(R.drawable.default_image);

                        // Using Glide we going store our image into Circle image place holder
                        Glide.with(UserAccountActivity.this).setDefaultRequestOptions(placeHolder).load(image).into(profile_image);


                    }/* else{
                        Toast.makeText(UserAccountActivity.this,"Data Doesn't Exist: ", Toast.LENGTH_SHORT).show();
                    }*/

                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(UserAccountActivity.this,"Firebase Retrieve Error: "+error, Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
                saveUserDetails.setEnabled(true);
            }
        });


        // When user press save account setting button
        saveUserDetails.setOnClickListener(new View.OnClickListener() {

                                               @Override
                                               public void onClick(View v) {

                                                   final String username = userName.getText().toString();
                                                   final String phoneNo = userPhoneNo.getText().toString();

                                                   if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(phoneNo) && profile_image != null) {
                                                   progressBar.setVisibility(View.VISIBLE);

                                                   if(isChanged){



                                                               user_id = mAuth.getCurrentUser().getUid();


                                                               final StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");

                                                               image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                                       if (task.isSuccessful()) {

                                                                           saveToFireStore(task, username);

                                                                       } else {

                                                                           String error = task.getException().getMessage();
                                                                           Toast.makeText(UserAccountActivity.this, "Image Error: " + error, Toast.LENGTH_SHORT).show();

                                                                           progressBar.setVisibility(View.INVISIBLE);

                                                                       }
                                                                   }
                                                               });

                                                           } else{

                                                       saveToFireStore(null,username);

                                                   }
                                                   }
                                               }
                                           }
        );

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if user running Marshmallow or grater version then permission is reqired
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    // check whether permission granted or not
                    if(ContextCompat.checkSelfPermission(UserAccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(UserAccountActivity.this,"Permission Denied.", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(UserAccountActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    }else {

                        Toast.makeText(UserAccountActivity.this,"You already have Permission.", Toast.LENGTH_LONG).show();

                        // using this piece of code we can crop the image got it from github(Int)
                        cropImagePicker();
                    }
                }
                else{

                    cropImagePicker();
                }
            }

        });

    }

    private void saveToFireStore(@NonNull Task<UploadTask.TaskSnapshot> task, String username) {

        Uri download_uri;

        if (task != null) {

        download_uri = task.getResult().getUploadSessionUri();

    } else{

            download_uri = mainImageUri;

        }
        Map<String, String> imageMap = new HashMap<>();
        imageMap.put("name",username);
        imageMap.put("image",download_uri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(imageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(UserAccountActivity.this,"The User settings are updated.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(UserAccountActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();

                }else{

                    String error = task.getException().getMessage();
                    Toast.makeText(UserAccountActivity.this,"FireStore Error: "+error, Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void storeFireStore(Task<UploadTask.TaskSnapshot> task, String username) {


        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        // getting uri from the task and
        Uri download_uri = task.getResult().getUploadSessionUri();

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name",username);
        //userMap.put("phoneNumber",phoneNo);
        userMap.put("image",download_uri.toString());

        // store to the fireBase FireStore
        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Toast.makeText(UserAccountActivity.this," User setting are updated ", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserAccountActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {

                    String error = task.getException().getMessage();
                    Toast.makeText(UserAccountActivity.this,"Error: "+error, Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void cropImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(UserAccountActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                profile_image.setImageURI(mainImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
}
