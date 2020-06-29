package uk.ac.wlv.myblogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar postToolBar;
    private EditText newPostDesc;
    private Button newPostBtn;
    private ImageView newPostImage;
    private Uri newPostImageUri = null ;
    private ProgressBar newPostProgressBar;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseStorage;
    private FirebaseAuth mAuth;


    private String downloadImageUrl;
    private String desc ;
    private String current_userId;

    public Compressor compressorImageFile;

    private String saveCurrentDate,saveCurrentTime;

    private static final int MAX_LENGHT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        postToolBar = findViewById(R.id.newPost_toolBar);
        setSupportActionBar(postToolBar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       // storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        current_userId = mAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference().child("post_images");
        firebaseStorage = FirebaseFirestore.getInstance();

        newPostDesc = findViewById(R.id.description_txt);
        newPostImage = findViewById(R.id.image_post);
        newPostBtn = findViewById(R.id.post_button);
        newPostProgressBar = findViewById(R.id.newPost_progressBar);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1 )
                        .start(NewPostActivity.this);
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                desc = newPostDesc.getText().toString();
                if(!TextUtils.isEmpty(desc) && newPostImageUri != null){

                    newPostProgressBar.setVisibility(View.VISIBLE);

                    String randomName = UUID.randomUUID().toString();

                    //StorageReference filepath = storageReference.child("post_images").child(randomName+"jpg");
                    final StorageReference filepath = storageReference.child(newPostImageUri.getLastPathSegment()+randomName+".jpg");

                    final UploadTask uploadTask = filepath.putFile(newPostImageUri);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            String error = e.getMessage();
                            Toast.makeText(NewPostActivity.this,"Error: "+error, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(NewPostActivity.this,"Image uploaded successful", Toast.LENGTH_SHORT).show();

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                    if(!task.isSuccessful()){
                                        throw task.getException();
                                    }
                                    downloadImageUrl = filepath.getDownloadUrl().toString();
                                    Toast.makeText(NewPostActivity.this,downloadImageUrl, Toast.LENGTH_SHORT).show();
                                    return filepath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(NewPostActivity.this,"Image Url getting successful", Toast.LENGTH_SHORT).show();

                                        Calendar calendar = Calendar.getInstance();

                                        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM yy");
                                        saveCurrentDate = currentDate.format(calendar.getTime());

                                        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
                                        saveCurrentTime = currentTime.format(calendar.getTime());


                                        HashMap<String, Object> imageMap = new HashMap<>();
                                        imageMap.put("user_id",current_userId);
                                        imageMap.put("image",downloadImageUrl);
                                        imageMap.put("desc", desc);
                                        imageMap.put("date", saveCurrentDate);
                                        imageMap.put("time", saveCurrentTime);
                                        //imageMap.put("timestamp", FieldValue.serverTimestamp());

                                        firebaseStorage.collection("Posts").add(imageMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful()){

                                                    Toast.makeText(NewPostActivity.this,"Post was Added", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(NewPostActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();

                                                }else{


                                                }
                                                newPostProgressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });


                }

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                newPostImageUri = result.getUri();
                newPostImage.setImageURI(newPostImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

    public static String randomGenerator(){

        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGHT);
        char tempChar;
        for(int i=0; i<randomLength; i++){
            tempChar = (char)(generator.nextInt(96)+32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}



/*    filepath.putFile(newPostImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                String downloadUri = task.getResult().getDo

                            }else {


                            }
                        }
                 });*/