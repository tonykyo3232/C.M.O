package com.example.cmo.Home;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmo.Account.LoginActivity;
import com.example.cmo.Post.PostActivity;
import com.example.cmo.Post.Posts;
import com.example.cmo.R;
import com.example.cmo.Account.SetupActivity;
import com.example.cmo.Utils.BottomNavigationViewHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MainActivity extends AppCompatActivity
{
    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;

    private Context mContext = MainActivity.this;
    private static final int ACTIVITY_NUM = 0;
    private String saveCurrentDate, saveCurrentTime, postRandomName;

    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBottomNavigationView();

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        DisplayAllUsersPosts();
    }

    private void DisplayAllUsersPosts()
    {
        //Descending order
        Query SortPostsInDecedningOrder = PostsRef.orderByChild("counter");

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                PostsViewHolder.class,
                                SortPostsInDecedningOrder
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
                    {
                        Log.d(MainActivity.class.getSimpleName(), "MainActivity - populateViewHolder");

                        final String PostKey = getRef(position).getKey();
                        String img_url = model.getImage_url();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileimage(getApplicationContext(), model.getUid());
                        viewHolder.setPostLocation(model.getLocation());
                        viewHolder.setPostimage(getApplicationContext(), img_url);
                        viewHolder.setLikeButtonStatus(PostKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent (MainActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentsInent = new Intent(MainActivity.this, CommentsActivity.class);
                                commentsInent.putExtra("PostKey",PostKey);
                                startActivity(commentsInent);
                            }
                        });

                        viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v){

                                LikeChecker = true;

                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(LikeChecker.equals(true)){
                                            if(dataSnapshot.child(PostKey).hasChild(mAuth.getCurrentUser().getUid())){

                                                // when user remove like
                                                LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                LikeChecker = false;

                                            }
                                            else{

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

                                                // debug
//                                                Log.d(MainActivity.class.getSimpleName(), "day: " + day + "\n");
//                                                Log.d(MainActivity.class.getSimpleName(), "Month: " + month + "\n");
//                                                Log.d(MainActivity.class.getSimpleName(), "year: " + year + "\n");
//                                                Log.d(MainActivity.class.getSimpleName(), "hours: " + hours + "\n");
//                                                Log.d(MainActivity.class.getSimpleName(), "mins: " + mins + "\n");
//                                                Log.d(MainActivity.class.getSimpleName(), "secs: " + secs + "\n");

                                                postRandomName = month + "/" + day + "/" + year + ", " + hours + ":" + mins;
                                                LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).setValue(postRandomName);


//                                                // ===== by Tony, for debug only (will delete later)
//                                                LikesRef.child(PostKey).addValueEventListener(new ValueEventListener() {
//                                                    @Override
//                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                        for (DataSnapshot child: dataSnapshot.getChildren()){
//                                                            Log.d(MainActivity.class.getSimpleName(), "Tony's debug place" + "\n");
//                                                            Log.d(MainActivity.class.getSimpleName(), "child.toString(): " + child.getKey() + "\n");
//                                                        }
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                                    }
//                                                });
//                                                // ===== by Tony, for debug only
                                                LikeChecker = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                    }
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

            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
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

            // be to be careful when storageRefProfile is null reference
            if (storageRefProfile != null){
                storageRefProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String img_uri = uri.toString();
                        ImageView PostImage = (ImageView) mView.findViewById(R.id.postpro);
                        Picasso.get().load(img_uri).into(PostImage);
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
            // by website
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("Posts Images").child(postimage);
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(MainActivity.class.getSimpleName(), "onSuccess\n");
                    Log.d(MainActivity.class.getSimpleName(), "uri.toString(): [" + uri.toString() + "]\n===============");
                    String img_uri = uri.toString();
                    Log.d(MainActivity.class.getSimpleName(), "MainActivity - setPostimage - onSuccess --> " + img_uri + "\n");
                    ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
                    Picasso.get().load(img_uri).into(PostImage);
                }
            });

            // works, but hard-coding
//            String str = "https://firebasestorage.googleapis.com/v0/b/cmofirebaseproject.appspot.com/o/Posts%20Images%2Fimage%3A8810-November-201900%3A48.jpg?alt=media&token=c1d6471d-2426-4614-aea1-dbc238551ede";
//            Picasso.get().load(str).into(PostImage);
            //Picasso.get().load(postimage).into(PostImage);

        }

        // new
        public void setPostLocation(String location)
        {
            Log.d(MainActivity.class.getSimpleName(), "setPostLocation - " + location);
            TextView PostLocation = (TextView) mView.findViewById(R.id.post_location);
            PostLocation.setText(" \n" + location);
        }

    }

    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    // Bottom navigation view set up
    private void setupBottomNavigationView()
    {
        Log.d(MainActivity.class.getSimpleName(), "MainActivity - setupBottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);

        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            //CheckUserExistence();
        }
    }



//    private void CheckUserExistence()
//    {
//        final String current_user_id = mAuth.getCurrentUser().getUid();
//
//        UsersRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot)
//            {
//                if(!dataSnapshot.hasChild(current_user_id))
//                {
//                    SendUserToSetupActivity();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
