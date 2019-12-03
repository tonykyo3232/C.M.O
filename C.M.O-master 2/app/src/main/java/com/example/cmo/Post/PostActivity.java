package com.example.cmo.Post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.cmo.Home.MainActivity;
import com.example.cmo.Profile.ProfileActivity;
import com.example.cmo.R;
import com.example.cmo.Account.SetupActivity;
import com.example.cmo.Utils.BottomNavigationViewHelper;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    //private Toolbar mToolbar;
    //private ProgressDialog loadingBar;

    private Button UpdatePostButton;
    private EditText PostDescription, PostLocation;
    private ImageButton SelectPostImage;

    private static final int Gallery_Pick = 1;

    private Uri ImageUri;
    private String Description; // Description
    private StorageReference PostImageReference; // PostImageRefernce
    private DatabaseReference UserRef, PostsRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;

    //
    private long coutPosts = 0;

    // By Tony
    private String image_url_detail, Location;
    private Context mContext = PostActivity.this;
    private static final int ACTIVITY_NUM = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(PostActivity.class.getSimpleName(), "==============\nPostActivity - onCreate\n===============");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
//        setContentView(R.layout.activity_main);

        // new: 11/26
        setupBottomNavigationView();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        PostImageReference = FirebaseStorage.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        SelectPostImage = findViewById(R.id.UploadImage);
        UpdatePostButton = findViewById(R.id.UpdatePostButton);
        PostDescription = findViewById(R.id.PostText);
        PostLocation = findViewById(R.id.location);

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                ValidatePostInfo();
            }
        });

        Log.d(PostActivity.class.getSimpleName(), "==============\nPostActivity - onCreate - finish\n===============");
    }


    // Bottom navigation view set up
    private void setupBottomNavigationView()
    {
        Log.d(PostActivity.class.getSimpleName(), "ProfileActivity - setupBottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
        Log.d(ProfileActivity.class.getSimpleName(), "PostActivity - setupBottomNavigationView - finish");
    }

    private void ValidatePostInfo()
    {
        Log.d(PostActivity.class.getSimpleName(), "ValidatePostInfo\n");

        Description = PostDescription.getText().toString();
        Location = PostLocation.getText().toString();

        if(ImageUri == null){
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        }
        else{
            StoringImageToFirebaseStorage();
        }
        Log.d(PostActivity.class.getSimpleName(), "ValidatePostInfo - finish\n");
    }

    private void StoringImageToFirebaseStorage() {
        Log.d(PostActivity.class.getSimpleName(), "StoringImageToFirebaseStorage\n");
        Calendar calendarDate = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarDate.getTime());

        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendarTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = PostImageReference.child("Posts Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
        Log.d(PostActivity.class.getSimpleName(), "filePath: "+ filePath.toString() + "\n");

        // By Tony
        image_url_detail = ImageUri.getLastPathSegment() + postRandomName + ".jpg";

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    // Version update!
                    downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                    Log.d(SetupActivity.class.getSimpleName(), "StoringImageToFirebaseStorage - onComplete, the downloadUrl is [" + downloadUrl+ "]");
                    //downloadUrl = task.getResult().getDownloadUrl().toString();

                    Toast.makeText(PostActivity.this, "Successful! The new post is added.", Toast.LENGTH_SHORT).show();

                    SavingPostInformationToDatabase();
                }else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Fail, " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingPostInformationToDatabase() {
        Log.d(PostActivity.class.getSimpleName(), "SavingPostInformationToDatabase\n");

        //descending order
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    coutPosts = dataSnapshot.getChildrenCount();
                }
                else{
                    coutPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        UserRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(PostActivity.class.getSimpleName(), "+++++onDataChange+++++\n");
//                if(dataSnapshot.getChildrenCount() > 0)
                if(dataSnapshot.exists())
                {
                    Log.d(PostActivity.class.getSimpleName(), "+++++onDataChange - if(dataSnapshot.exists())+++++\n");
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    Log.d(PostActivity.class.getSimpleName(), "+*+*+*+*+*+*+" + userFullName + "+*+*+*+*+*+*+\n");

                    // This doesn't work since sth wrong about the profile image (it shows null....)
                    //String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    //Log.d(PostActivity.class.getSimpleName(), "+*+*+*+*+*+*+" + userProfileImage + "+*+*+*+*+*+*+\n");

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage", downloadUrl);
                    //postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);

                    // By Tony
                    postsMap.put("image_url", image_url_detail);
                    postsMap.put("location", Location);

                    //descending order
                    postsMap.put("counter",coutPosts);

                    // By Tony no used
                    //public Posts(String uid, String time, String date, String postimage, String description, String profileimage, String fullname)
                    //Posts userPost = new Posts(current_user_id, saveCurrentTime, saveCurrentDate, downloadUrl, Description, "", userFullName);

                    Log.d(PostActivity.class.getSimpleName(), "postRandomName :[ "+ postRandomName + "]\n");
                    Log.d(PostActivity.class.getSimpleName(), "current_user_id + postRandomName :[ "+ current_user_id + postRandomName + "]\n");

                    // userID + date + time
                    // Example: ClUQj4aEolO9JuZPid6iIAibA9n210-November-201915:02
                    PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(Task task) {
                            if(task.isSuccessful()){
                                Log.d(PostActivity.class.getSimpleName(), "+++++SavingPostInformationToDatabase - if(task.isSuccessful())+++++\n");

                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New Posts2 is updated sucessfully", Toast.LENGTH_SHORT).show();
                                //loadingbar.dismiss();
                            }
                            else{
                                Toast.makeText(PostActivity.this, "Error Occured while updating your post.", Toast.LENGTH_SHORT).show();
                                //loadingbar.dismiss();
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // do nothing yet
            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(PostActivity.class.getSimpleName(), "onActivityResult\n");
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);

            // by Tony
            image_url_detail = ImageUri.toString();
        }
        Log.d(PostActivity.class.getSimpleName(), "onActivityResult - finish\n");
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
