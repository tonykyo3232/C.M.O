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

import com.example.cmo.Account.SetupActivity;
import com.example.cmo.Post.PostActivity;
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

    //===
    private CropImage.ActivityResult result; // not sure about this
    private Uri resultUri;
    private StorageReference filePath;
    //===

    final static int Gallery_Pick = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        mprofilephoto = (ImageView) view.findViewById(R.id.profile_photo_editprofile);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserID = mAuth.getCurrentUser().getUid();

        // By Tony
        updateBtn = (Button) view.findViewById(R.id.btn_update);

        // 12/6
        changeProfileText = (TextView) view.findViewById(R.id.changeProfilePhoto);

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initImageLoader();

        setProfileImage();

        setUserInfo();

        // when "update" button is clicked, it will update the FileBase data
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ****
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

                            // this can be dangerous
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

                filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                //storing img to FireBase...
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
                //-------

            } // end Onclick
        });


        // ********************recently added (12/6)
        changeProfileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        // ********************recently added (12/6)

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

        Log.d(getClass().getSimpleName(), "tonykyo3232 - debug" + "\n");
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void setProfileImage(){

//        String imgURL = "www.androidcentral.com/sites/androidcentral.com/files/styles/xlarge/public/article_images/2016/08/ac-lloyd.jpg?itok=bb72IeLf";
//        UniversalImageLoader.setImage(imgURL, mprofilephoto, null, "https://");

        // By Tony -----------------------------------------------
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
        // By Tony -----------------------------------------------
    }


    // By Tony -----------------------------------------------
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
