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
import android.widget.TextView;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private Context mContext = ProfileActivity.this;
    private static final int ACTIVITY_NUM = 4;

    // For the posts
    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    Boolean LikeChecker = false;
    private String currentUserID;
    private String saveCurrentDate, saveCurrentTime, postRandomName;
    private int postCount = 0;
    private TextView postNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(ProfileActivity.class.getSimpleName(), "ProfileActivity-onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupBottomNavigationView();
        setupToolBar();

        postNum = (TextView) findViewById(R.id.tvPosts);
        postNum.setText(String.valueOf(postCount));

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

        // access info for user profile image
        FirebaseStorage storage_ = FirebaseStorage.getInstance();
        StorageReference currentUserRef = storage_.getReference();
        currentUserRef = currentUserRef.child("Profile Images").child(currentUserID + ".jpg");
        Log.d(ProfileActivity.class.getSimpleName(), "find me currentUserRef: " + currentUserRef.toString());

        currentUserRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String img_uri = uri.toString();
                ImageView PostImage = (ImageView) findViewById(R.id.profile_image);
                Picasso.get().load(img_uri).into(PostImage);
            }
        });

        UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    String country = userSnapshot.child("country").getValue(String.class);
                    String fullName = userSnapshot.child("fullname").getValue(String.class);
                    String userName = userSnapshot.child("username").getValue(String.class);

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
        DisplayAllUsersPosts(currentUserID);
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
    }

    /**********************************
     // functions for the posts
     **********************************/
    private void DisplayAllUsersPosts(String currentUserID)
    {
        //Descending order
//        Query SortPostsInDecedningOrder = PostsRef.orderByChild("counter");

        // filter based on the current user
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
                        postCount++;

                        // display number of posts
                        postNum.setText(String.valueOf(postCount));
                        final String PostKey = getRef(position).getKey();
                        String img_url = model.getImage_url();

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

                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(LikeChecker.equals(true)) {
                                            if(dataSnapshot.child(PostKey).hasChild(mAuth.getCurrentUser().getUid())) {

                                                // when user remove like
                                                LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                LikeChecker = false;
                                            } else {

                                                // when user type like
                                                Calendar calendarDate = Calendar.getInstance();

                                                SimpleDateFormat currentDate = new SimpleDateFormat("dd/MMMM/yyyy");
                                                saveCurrentDate = currentDate.format(calendarDate.getTime());

                                                Calendar calendarTime = Calendar.getInstance();
                                                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                                                saveCurrentTime = currentTime.format(calendarTime.getTime());

                                                String [] spiltDateResult = saveCurrentDate.split("/");
                                                String [] spiltTimeResult = saveCurrentTime.split(":");

                                                String day = spiltDateResult[0];
                                                String month = spiltDateResult[1];
                                                String year = spiltDateResult[2];

                                                String hours = spiltTimeResult[0];
                                                String mins = spiltTimeResult[1];
                                                String secs = spiltTimeResult[2];

                                                postRandomName = month + "/" + day + "/" + year + ", " + hours + ":" + mins;
                                                LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).setValue(postRandomName);
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

                    } // end populateViewHolder
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
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
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String userId)
        {
            // by website
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRefProfile = storage.getReference();

            // access info for user profile image
            storageRefProfile = storageRefProfile.child("Profile Images").child(userId + ".jpg");

            // be to be careful when storageRefProfile is null reference, meaning that
            if (storageRefProfile != null){
                storageRefProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String img_uri = uri.toString();
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
            PostDate.setText("  " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1, String postimage)
        {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("Posts Images").child(postimage);
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String img_uri = uri.toString();
                    ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
                    Picasso.get().load(img_uri).into(PostImage);
                }
            });
        }

        public void setPostLocation(String location)
        {
            TextView PostLocation = (TextView) mView.findViewById(R.id.post_location);
            PostLocation.setText(" \n" + location);
        }
    }

}