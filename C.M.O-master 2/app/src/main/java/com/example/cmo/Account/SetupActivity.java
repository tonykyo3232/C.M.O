package com.example.cmo.Account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmo.Home.MainActivity;
import com.example.cmo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, Country;
    private Button SaveInformation;
    private ImageView ProfileImage;
//    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    // Bt Tony
    private StorageReference storageRef;
    private FirebaseStorage storage;

    String currentUserID;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(SetupActivity.class.getSimpleName(), "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        // By Tony
        storage = FirebaseStorage.getInstance();

        // Tony: trying to work on
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        Country = (EditText) findViewById(R.id.setup_country);
        SaveInformation = (Button) findViewById(R.id.setup_button);

        // tony: trying to work on
        ProfileImage = (ImageView) findViewById(R.id.setup_image);

        SaveInformation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                saveAccountSetupInfo();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.d(SetupActivity.class.getSimpleName(), "==============\nSetupActivity - onDataChange\n===============");
                    //save profile image failed (video 15)
                    //String image = dataSnapshot.child("profileimage").getValue().toString();
                    //Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    Log.d(SetupActivity.class.getSimpleName(), "dataSnapshot.getChildren().toString(); => " + dataSnapshot.getChildren().toString());
                    Log.d(SetupActivity.class.getSimpleName(), "==============\nSetupActivity - onDataChange - if(dataSnapshot.exists()) == TRUE\n===============");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // By Tony
        // ******************************************
        if (storage.getReference() != null)
        {
            storageRef = storage.getReference().child("Profile Images").child(currentUserID + ".jpg");

            Log.d(SetupActivity.class.getSimpleName(), "currentUserID: " + currentUserID + ".jpg");

            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(SetupActivity.class.getSimpleName(), "==============\n[Before] onDataChange - onSuccess \n===============");
                    String img_uri = uri.toString();
                    Log.d(SetupActivity.class.getSimpleName(), "img_uri: " + img_uri + "\n");
                    ImageView PostImage = (ImageView) findViewById(R.id.setup_image);
                    Picasso.get().load(img_uri).into(PostImage);
                    Log.d(SetupActivity.class.getSimpleName(), "==============\n[After] onDataChange - onSuccess \n===============");
                }
            });
        }
        // ******************************************
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

//            // By Tony
//            ProfileImage.setImageURI(ImageUri);

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "image stored to firebase",Toast.LENGTH_SHORT).show();

                            // ==============================================================================================
                            final String downloadUrl = task.getResult().getUploadSessionUri().toString();
                            Log.d(SetupActivity.class.getSimpleName(), "downloadUrl: " + downloadUrl + "\n");


                            UsersRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);
                                                Toast.makeText(SetupActivity.this, "Profile image store to firebase",Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this,"Error Occured" + message,Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            // ==============================================================================================

                        }
                    }
                });
            }
            else{
                Toast.makeText(this, "Error occured: Image cant be cropped",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAccountSetupInfo() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String coutry = Country.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please write your username",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Please write your full name",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(coutry)){
            Toast.makeText(this, "Please write your country",Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", coutry);

            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account is created successfull",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this,"Error Occured" + message,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}


