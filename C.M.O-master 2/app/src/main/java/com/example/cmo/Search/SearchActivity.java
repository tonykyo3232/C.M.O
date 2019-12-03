package com.example.cmo.Search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cmo.Home.ClickPostActivity;
import com.example.cmo.Home.CommentsActivity;
import com.example.cmo.Home.MainActivity;
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

public class SearchActivity extends AppCompatActivity {

    private Context mContext = SearchActivity.this;
    private static final int ACTIVITY_NUM = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    Boolean LikeChecker = false;
    private RecyclerView postList;
    private String saveCurrentDate, saveCurrentTime, postRandomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
//        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        postList = (RecyclerView) findViewById(R.id.default_all_users_like_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        Button searhBtn =(Button) findViewById(R.id.search_button);
        setupBottomNavigationView();

        searhBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayAllUsersPosts();
            }
        });
    }

    // Bottom navigation view set up
    private void setupBottomNavigationView()
    {
        Log.d(SearchActivity.class.getSimpleName(), "SearchActivity - setupBottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);

        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


    // *************
    private void DisplayAllUsersPosts()
    {
        //deceding order
        Query SortPostsInDecedningOrder = PostsRef.orderByChild("counter");

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                PostsViewHolder.class,
                                //replace postsRef
                                SortPostsInDecedningOrder
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
                    {
                        final String PostKey = getRef(position).getKey();

                        String img_url = model.getImage_url();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileimage(getApplicationContext(), model.getUid());

                        // new
                        viewHolder.setPostLocation(model.getLocation());
                        viewHolder.setPostimage(getApplicationContext(), img_url);
                        viewHolder.setLikeButtonStatus(PostKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent (SearchActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentsInent = new Intent(SearchActivity.this, CommentsActivity.class);
                                commentsInent.putExtra("PostKey",PostKey);
                                startActivity(commentsInent);
                            }
                        });

                        viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
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

                                                // ======= from PostActivity
                                                Calendar calendarDate = Calendar.getInstance();

                                                SimpleDateFormat currentDate = new SimpleDateFormat("dd/MMMM/yyyy");
                                                saveCurrentDate = currentDate.format(calendarDate.getTime());

                                                Calendar calendarTime = Calendar.getInstance();
                                                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                                                saveCurrentTime = currentTime.format(calendarTime.getTime());
//                                                postRandomName = saveCurrentDate + ", " + saveCurrentTime;

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
        private String saveCurrentDate_, saveCurrentTime_, postRandomName_;

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
            // by website
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

        // new
        public void setPostLocation(String location)
        {
            TextView PostLocation = (TextView) mView.findViewById(R.id.post_location);
            PostLocation.setText(" \n" + location);
        }

    }
    // *************
}
