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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private EditText mSearchField;
    private Button mSearchBtn;
    private RecyclerView mResultList;

    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef, LikesRef;
    Boolean LikeChecker = false;
    private String saveCurrentDate, saveCurrentTime, postRandomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupBottomNavigationView();
        mAuth = FirebaseAuth.getInstance();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mSearchField = (EditText) findViewById (R.id.search_bar);
        mSearchBtn = (Button) findViewById(R.id.search_button);

        mResultList = (RecyclerView) findViewById(R.id.all_users_post_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        mSearchBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String searchText = mSearchField.getText().toString();
                firebasePostSearch(searchText);
            }
        });
    }

    private void firebasePostSearch(String searchText) {

        Toast.makeText(SearchActivity.this, "Started Search", Toast.LENGTH_SHORT).show();

        Query firebaseSearchQuery = PostsRef.orderByChild("location").startAt(searchText).endAt(searchText + "\uf8ff");

        Log.d(SearchActivity.class.getSimpleName(), "=============================");
        Log.d(SearchActivity.class.getSimpleName(), "============"+firebaseSearchQuery+ "===============");
        Log.d(SearchActivity.class.getSimpleName(), "============"+searchText+ "===============");



        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(
                Posts.class,
                R.layout.all_posts_layout,
                PostsViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position) {

                final String PostKey = getRef(position).getKey();

                viewHolder.setDetails(getApplicationContext(),model.getFullname(),model.getDescription(), model.getTime(), model.getDate(), model.getLocation());
                viewHolder.setPostimage(model.getImage_url());
                viewHolder.setProfileimage(model.getUid());

                viewHolder.setLikeButtonStatus(PostKey);

                //===
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
                        Log.d(MainActivity.class.getSimpleName(), "--------Like button clicked---------");
                        Log.d(MainActivity.class.getSimpleName(), "LikeChecker status "+ LikeChecker + ":  ");

                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(LikeChecker.equals(true)){
                                    if(dataSnapshot.child(PostKey).hasChild(mAuth.getCurrentUser().getUid())){
                                        // when user remove like
                                        LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        LikeChecker = false;
                                        Log.d(MainActivity.class.getSimpleName(), "--------Inside Like button clicked---------");

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

                                        String [] spiltDateResult = saveCurrentDate.split("/");
                                        String [] spiltTimeResult = saveCurrentTime.split(":");

                                        String day = spiltDateResult[0];
                                        String month = spiltDateResult[1];
                                        String year = spiltDateResult[2];

                                        String hours = spiltTimeResult[0];
                                        String mins = spiltTimeResult[1];
                                        String secs = spiltTimeResult[2];

                                        // debug
                                        Log.d(MainActivity.class.getSimpleName(), "day: " + day + "\n");
                                        Log.d(MainActivity.class.getSimpleName(), "Month: " + month + "\n");
                                        Log.d(MainActivity.class.getSimpleName(), "year: " + year + "\n");
                                        Log.d(MainActivity.class.getSimpleName(), "hours: " + hours + "\n");
                                        Log.d(MainActivity.class.getSimpleName(), "mins: " + mins + "\n");
                                        Log.d(MainActivity.class.getSimpleName(), "secs: " + secs + "\n");

                                        postRandomName = month + "/" + day + "/" + year + ", " + hours + ":" + mins;

                                        // use this parent name to differentiate the like from users and posts
//                                                outerParent = month + ":" + day + ":" + year + ":" + hours + ":" + mins + ":" + secs;
                                        // =======


                                        LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).setValue(postRandomName);



                                        // ===== by Tony, for debug only (will delete later)
                                        LikesRef.child(PostKey).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot child: dataSnapshot.getChildren()){
                                                    Log.d(MainActivity.class.getSimpleName(), "Tony's debug place" + "\n");
                                                    Log.d(MainActivity.class.getSimpleName(), "child.toString(): " + child.getKey() + "\n");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        // ===== by Tony, for debug only

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
                //===
            }
        };

        mResultList.setAdapter(firebaseRecyclerAdapter);
    }

    // Bottom navigation view set up
    private void setupBottomNavigationView()
    {
        Log.d(SearchActivity.class.getSimpleName(), "SearchActivity - setupBottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx); // error!

        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        //===
        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;
        //===

        public PostsViewHolder(View itemView){
            super(itemView);
            mView = itemView;

            //===
            LikePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView) mView.findViewById(R.id.display_no_of_likes);

            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            //===
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

        public void setDetails(Context ctx, String post_user_name, String post_description, String post_time, String post_date, String post_location){
            TextView user_name = (TextView) mView.findViewById(R.id.post_user_name);
            TextView user_date = (TextView) mView.findViewById(R.id.post_date);
            TextView user_time = (TextView) mView.findViewById(R.id.post_time);
            TextView user_location = (TextView) mView.findViewById(R.id.post_location);
            TextView user_post = (TextView) mView.findViewById(R.id.post_description);

            user_name.setText(post_user_name);
            user_date.setText(post_date);
            user_time.setText(post_time);
            user_location.setText(post_location);
            user_post.setText(post_description);
        }

        public void setProfileimage(String userId)
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
                        Picasso.get().load(img_uri).into(PostImage);
                    }
                });
            }
        }

        public void setPostimage(String postimage_path)
        {
            // by website
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("Posts Images").child(postimage_path);
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String img_uri = uri.toString();
                    ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
                    Picasso.get().load(img_uri).into(PostImage);
                }
            });
        }
    }
}
