package com.example.cmo.Profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmo.Home.ClickPostActivity;
import com.example.cmo.Home.CommentsActivity;
import com.example.cmo.Post.Posts;
import com.example.cmo.R;
import com.example.cmo.Utils.BottomNavigationViewHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.models.User;

public class ProfileActivity extends AppCompatActivity {

    private Context mContext = ProfileActivity.this;
    private ProgressBar mprogressBar;
    //    private static final int ACTIVITY_NUM = 3;
    private static final int ACTIVITY_NUM = 4;


    // For the posts
    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
//    private StorageReference storageRef = storage.getReference();

    //    private StorageReference storageRefProfile = storage.getReference();
    Boolean LikeChecker = false;
    private String currentUserID, temp;
    private int tempInt;
    private boolean isUserPost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity-onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
//        setContentView(R.layout.activity_main);

        // implement later
//        mprogressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
//        mprogressBar.setVisibility(View.GONE);
        setupBottomNavigationView();
        setupToolBar();

        // For the posts
        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUserID = mAuth.getCurrentUser().getUid();
        postList = (RecyclerView) findViewById(R.id.current_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        // try later
        // access info for user profile image
        FirebaseStorage storage_ = FirebaseStorage.getInstance();
        StorageReference currentUserRef = storage_.getReference();
        currentUserRef = currentUserRef.child("Profile Images").child(currentUserID + ".jpg");
        Log.d(ProfileActivity.class.getSimpleName(), "find me currentUserRef: " + currentUserRef.toString());
        // =======
        currentUserRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(ProfileActivity.class.getSimpleName(), "find_me");
                Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity-onCreate - onSuccess");
                String img_uri = uri.toString();
                Log.d(ProfileActivity.class.getSimpleName(), " find me url: " + img_uri + "\n");
                ImageView PostImage = (ImageView) findViewById(R.id.profile_image);
                Picasso.get().load(img_uri).into(PostImage);
            }
        });
        // =======


        // ---------------------------
        UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Log.d(ProfileActivity.class.getSimpleName(), "find_me\n");
                    Log.d(ProfileActivity.class.getSimpleName(), "onCreate - UsersRef's onDataChange");
                    String country = userSnapshot.child("country").getValue(String.class);
                    String fullName = userSnapshot.child("fullname").getValue(String.class);
                    String userName = userSnapshot.child("username").getValue(String.class);


                    Log.d(ProfileActivity.class.getSimpleName(), "country: " + country);
                    Log.d(ProfileActivity.class.getSimpleName(), "\nfullname: " + fullName);
                    Log.d(ProfileActivity.class.getSimpleName(), "\nusername: " + userName);


                    // find the following place to put the text
                    TextView fullNameText = (TextView) findViewById(R.id.display_name);
                    TextView countryText = (TextView) findViewById(R.id.display_origin);
                    TextView userNameText = (TextView) findViewById(R.id.user_name);
                    TextView topUserNameText = (TextView) findViewById(R.id.profileName);

                    // display the text to the phone screen
                    countryText.setText(country);
                    fullNameText.setText(fullName);
                    userNameText.setText(userName);
                    topUserNameText.setText(fullName);

                    if(userSnapshot.getKey().equals(currentUserID)){
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d(ProfileActivity.class.getSimpleName(), "Before DisplayAllUsersPosts");
        DisplayAllUsersPosts(currentUserID);
        Log.d(ProfileActivity.class.getSimpleName(), "After DisplayAllUsersPosts");
    }


    private void setupToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);

        ImageView profileMenu = (ImageView) findViewById(R.id.profileMenu);
        profileMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity-onCreate-setupToolBar-onClick");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });

//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()){
//                    case R.id.profileMenu:
//
//                }
//                return false;
//            }
//        });
    }

    // Bottom navigation view set up
    private void setupBottomNavigationView()
    {
        Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity - setupBottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
        Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity - setupBottomNavigationView - finish");
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.profile_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    /**********************************
     // functions for the posts
     **********************************/
    private void DisplayAllUsersPosts(String currentUserID)
    {

        //deceding order
        Query SortPostsInDecedningOrder = PostsRef.orderByChild("counter");
        Query firebaseSearchQuery = PostsRef.orderByChild("uid").equalTo(currentUserID);

        Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity - DisplayAllUsersPosts - begin");
        FirebaseRecyclerAdapter<Posts, ProfileActivity.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, ProfileActivity.PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                ProfileActivity.PostsViewHolder.class,
                                firebaseSearchQuery
                        )
                {
                    @Override
                    protected void populateViewHolder(ProfileActivity.PostsViewHolder viewHolder, Posts model, int position)
                    {
                        Log.d(ProfileActivity.class.getSimpleName(), "DisplayAllUsersPosts __> ProfileActivity - populateViewHolder");
                        final String PostKey = getRef(position).getKey();
                        String img_url = model.getImage_url();

//                        Log.d(ProfileActivity.class.getSimpleName(), "currentUserID: " + currentUserID + "\n");
//                        Log.d(ProfileActivity.class.getSimpleName(), "model.getUid(): " + model.getUid() + "\n");
//                        temp = model.getUid();

//                        if(model.getUid().equals(currentUserID))
//                        {
//                            Log.d(ProfileActivity.class.getSimpleName(), "[ if(model.getUid() == currentUserID) ]:" + position);
//                            isUserPost = true;

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setPostLocation(model.getLocation());
                        viewHolder.setPostimage(getApplicationContext(), img_url);
                        viewHolder.setProfileimage(getApplicationContext(), model.getUid());
                        viewHolder.setLikeButtonStatus(PostKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(ProfileActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentsInent = new Intent(ProfileActivity.this, CommentsActivity.class);
                                commentsInent.putExtra("PostKey",PostKey);
                                startActivity(commentsInent);
                            }
                        });

                        viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LikeChecker = true;
                                Log.d(ProfileActivity.class.getSimpleName(), "--------Like button clicked---------");
                                Log.d(ProfileActivity.class.getSimpleName(), "LikeChecker status " + LikeChecker + ":  ");

                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (LikeChecker.equals(true)) {
                                            if (dataSnapshot.child(PostKey).hasChild(mAuth.getCurrentUser().getUid())) {
                                                LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                LikeChecker = false;
                                                Log.d(ProfileActivity.class.getSimpleName(), "--------Inside Like button clicked---------");
                                            } else {
                                                LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).setValue(true);
                                                LikeChecker = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                }); // end LikesRef.addValueEventListener
                            }
                        }); // end viewHolder.LikePostButton.setOnClickListener
//                        }
//                        else{
////                            position++;
//                            tempInt = position;
//                        }
                    } // end populateViewHolder
                };
//        if(isUserPost)

//        Log.d(ProfileActivity.class.getSimpleName(), "...When setAdapter...\n");
//        Log.d(ProfileActivity.class.getSimpleName(), "Position: "+ tempInt + "\n");
//        Log.d(ProfileActivity.class.getSimpleName(), "currentUserID: " + currentUserID + "\n");
//        Log.d(ProfileActivity.class.getSimpleName(), "model.getUid(): " + temp + "\n");

        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        DatabaseReference PostsRef_ = FirebaseDatabase.getInstance().getReference().child("Posts");

        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;

            LikePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView) mView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));

                    }
                    else{
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
//            username.setText("Tony Lee");
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String userId)
        {
            Log.d(ProfileActivity.class.getSimpleName(), "!!setProfileimage!!\n");

            // by website
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRefProfile = storage.getReference();

            // access info for user profile image
            storageRefProfile = storageRefProfile.child("Profile Images").child(userId + ".jpg");

            Log.d(ProfileActivity.class.getSimpleName(), "setProfileimage - before [if (storageRefProfile != null)] ...\n");
            Log.d(ProfileActivity.class.getSimpleName(), "storageRefProfile: [ " + storageRefProfile.toString() + "]\n");
            Log.d(ProfileActivity.class.getSimpleName(), "currentUserId: [ " + userId + "]\n");
            // be to be careful when storageRefProfile is null reference, meaning that
            if (storageRefProfile != null){
                Log.d(ProfileActivity.class.getSimpleName(), "setProfileimage - INSIDE [if (storageRefProfile != null)] ...\n");
                storageRefProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(ProfileActivity.class.getSimpleName(), "setProfileimage's onSuccess\n");
                        Log.d(ProfileActivity.class.getSimpleName(), "setProfileimage's uri.toString(): [" + uri.toString() + "]\n===============");
                        String img_uri = uri.toString();
                        Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity - setProfileimage's - onSuccess --> " + img_uri + "\n");
                        ImageView PostImage = (ImageView) mView.findViewById(R.id.postpro);
                        Picasso.get().load(img_uri).into(PostImage); // crash
                    }
                });
            }
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText( " " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
//            PostDate.setText("    " + "11/10/2019");
            PostDate.setText("  " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
//            PostDescription.setText("This is a post...");
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1, String postimage)
        {
            Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity - setPostimage");
            // by website
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("Posts Images").child(postimage);
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(ProfileActivity.class.getSimpleName(), "onSuccess\n");
                    Log.d(ProfileActivity.class.getSimpleName(), "uri.toString(): [" + uri.toString() + "]\n===============");
                    String img_uri = uri.toString();
                    Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity - setPostimage - onSuccess --> " + img_uri + "\n");
                    ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
                    Picasso.get().load(img_uri).into(PostImage);
                }
            });

//            // useless code
//            StorageReference gsReference = storage.getReferenceFromUrl(storageRef.toString());
//            Log.d(MainActivity.class.getSimpleName(), "gsReference: [" + storageRef.toString() + "]\n===============");

            Log.d(ProfileActivity.class.getSimpleName(), "==============\nstorageRef: [" + storageRef.toString() + "]\n===============");

            //==================================================================

            // works, but hard-coding
//            String str = "https://firebasestorage.googleapis.com/v0/b/cmofirebaseproject.appspot.com/o/Posts%20Images%2Fimage%3A8810-November-201900%3A48.jpg?alt=media&token=c1d6471d-2426-4614-aea1-dbc238551ede";
//            Picasso.get().load(str).into(PostImage);
            //Picasso.get().load(postimage).into(PostImage);

        }

        // new
        public void setPostLocation(String location)
        {
            Log.d(ProfileActivity.class.getSimpleName(), "setPostLocation - " + location);
            TextView PostLocation = (TextView) mView.findViewById(R.id.post_location);
            PostLocation.setText(" \n" + location);
        }
    }

}