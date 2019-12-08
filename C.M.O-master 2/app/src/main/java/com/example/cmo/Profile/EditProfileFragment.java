package com.example.cmo.Profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cmo.R;
import com.example.cmo.Utils.UniversalImageLoader;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


public class EditProfileFragment extends Fragment {

    // FireBase Variable
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private View view;
    String currentUserID;

    private String newUserName;
    private String newFullName;
    private String newCountry;
    private ImageView mprofilephoto;
    private Button updateBtn;

    private TextView userNameText;
    private TextView fullNameText;
    private TextView countryText;
    private TextView changeProfileText;

    private Uri ImageUri;
    private StorageReference UserProfileImageRef;

    private StorageReference filePath;
    private DatabaseReference PostsRef;
    private DatabaseReference PostsRef_;
    private boolean changeImg = false;
    private HashMap updateMap_;
    private HashMap updateMap_2;

    final static int Gallery_Pick = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        mprofilephoto = (ImageView) view.findViewById(R.id.profile_photo_editprofile);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserID = mAuth.getCurrentUser().getUid();


        updateBtn = (Button) view.findViewById(R.id.btn_update);

        changeProfileText = (TextView) view.findViewById(R.id.changeProfilePhoto);

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initImageLoader();

        setProfileImage();

        setUserInfo();

        // when "update" button is clicked, it will update the FileBase data
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            newUserName = userNameText.getText().toString();
                            newFullName = fullNameText.getText().toString();
                            newCountry = countryText.getText().toString();

                            // create the new map and replace the old data in FireBase
                            HashMap updateMap = new HashMap();
                            updateMap.put("username", newUserName);
                            updateMap.put("fullname", newFullName);
                            updateMap.put("country", newCountry);

                            // add the listener so the Firebase is able to update the data
                            UsersRef.child(currentUserID).updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(getActivity(), "New information store to firebase...",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(getActivity(), "Failed to update the info...",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                // ****

                // If user want to update the profile image
                if(changeImg){
                    Log.d(getClass().getSimpleName(), "debug - if(changeImg){" + "\n");
                    filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                    //storing img to FireBase
                    filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), "Successful! The new profile image is added.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(getActivity(), "Failed to update profile image, " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                newFullName = fullNameText.getText().toString();

                // for Post Reference purpose
                updateMap_ = new HashMap();
                updateMap_.put("fullname", newFullName);

                // update the post's information of the user here
                PostsRef.addValueEventListener(new ValueEventListener() {
                @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                                if(currentUserID.equals(userSnapshot.child("uid").getValue(String.class))
                                        && userSnapshot.child("fullname").getValue(String.class) != newFullName) {

                                    PostsRef.child(userSnapshot.getKey()).updateChildren(updateMap_)
                                            .addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if (task.isSuccessful()) {

                                                    }
                                                    else {
                                                        Log.d(getClass().getSimpleName(), "failed to update username in posts..." + "\n");
                                                    }
                                                }
                                            });
                                } // end if(currentUserID.equals(userSnapshot.child("uid").getValue(String.class))...

                                // if the post has comment, also update the username inside the Comments
                                if(userSnapshot.hasChild("Comments")){
                                    Log.d(getClass().getSimpleName(), "PostsRef_ - if(userSnapshot.hasChild(Comments)" + "\n");
                                    newUserName = userNameText.getText().toString();
                                    Log.d(getClass().getSimpleName(), "newUserName: " + newUserName + "\n");

                                    updateMap_2 = new HashMap();
                                    updateMap_2.put("username", newUserName);

                                    PostsRef_ = PostsRef.child(userSnapshot.getKey()).child("Comments"); // sth wrong here
                                    PostsRef_.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Log.d(getClass().getSimpleName(), "PostsRef_ - onDataChange" + "\n");
                                            for (DataSnapshot userSnapshot_ : dataSnapshot.getChildren()) {
                                                if(currentUserID.equals(userSnapshot_.child("uid").getValue(String.class))
                                                        && userSnapshot_.child("username").getValue(String.class) != newUserName){

                                                    Log.d(getClass().getSimpleName(), "PostsRef_ - onDataChange - if happens..." + "\n");

                                                    Log.d(getClass().getSimpleName(), "currentUserID: " + currentUserID + "\n");
                                                    Log.d(getClass().getSimpleName(), "userSnapshot_.child(\"uid\").getValue(String.class): " + userSnapshot_.child("uid").getValue(String.class) + "\n");
                                                    Log.d(getClass().getSimpleName(), "userSnapshot_.getKey(): " + userSnapshot_.getKey() + "\n");

                                                    PostsRef_.child(userSnapshot_.getKey()).updateChildren(updateMap_2).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d(getClass().getSimpleName(), "PostsRef_ - onDataChange - onComplete" + "\n");
                                                            }
                                                            else {
                                                                Log.d(getClass().getSimpleName(), "failed to update username in comments..." + "\n");
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }

                                    }); // end PostsRef_.addValueEventListener

                                }// end if(userSnapshot.hasChild("Comments")
                            }
                        }
                    } // end PostsRef - onDataChange

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                }); // end PostsRef.addValueEventListener

            } // end Onclick


        }); //updateBtn.setOnClickListener

        changeProfileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);

                // set it to true if user do change the profile image
                changeImg = true;
            }
        });

        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                startActivity(intent);
//                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(getActivity());

            mprofilephoto.setImageURI(ImageUri);
        }
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void setProfileImage(){

//        String imgURL = "www.androidcentral.com/sites/androidcentral.com/files/styles/xlarge/public/article_images/2016/08/ac-lloyd.jpg?itok=bb72IeLf";
//        UniversalImageLoader.setImage(imgURL, mprofilephoto, null, "https://");

        // Access the FireBase reference
        FirebaseStorage storage_ = FirebaseStorage.getInstance();
        StorageReference currentUserRef = storage_.getReference();
        currentUserRef = currentUserRef.child("Profile Images").child(currentUserID + ".jpg");

        currentUserRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String img_uri = uri.toString();
                Picasso.get().load(img_uri).into(mprofilephoto);
            }
        });
    }

    private void setUserInfo(){
        UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    String fullName = userSnapshot.child("fullname").getValue(String.class);
                    String userName = userSnapshot.child("username").getValue(String.class);
                    String country = userSnapshot.child("country").getValue(String.class);

                    // find the following place to put the text
                    userNameText = (TextView) view.findViewById(R.id.username);
                    fullNameText = (TextView) view.findViewById(R.id.display_name);
                    countryText = (TextView) view.findViewById(R.id.country);

                    // display the text to the phone screen
                    userNameText.setText(userName);
                    fullNameText.setText(fullName);
                    countryText.setText(country);

                    if(userSnapshot.getKey().equals(currentUserID)){
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
