package com.example.cmo.Likes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cmo.R;
import com.example.cmo.Utils.BottomNavigationViewHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Picasso;

public class AlertActivity extends AppCompatActivity {

    private Context mContext = AlertActivity.this;
    private static final int ACTIVITY_NUM = 3;
    private RecyclerView likeList;
    private DatabaseReference LikesRef, UsersRef, PostsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        // find the reference from the FireBase
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        setupBottomNavigationView();

        // ideas from MainActivity
        likeList = (RecyclerView) findViewById(R.id.all_users_like_list);
        likeList.setHasFixedSize(true);

        // not sure this part
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        likeList.setLayoutManager(linearLayoutManager);

        // use an adaptor to print all the likes activity as notification
        DisplayAllUsersLikes();
    }

    // Bottom navigation view set up
    private void setupBottomNavigationView() {
        Log.d(AlertActivity.class.getSimpleName(), "AlertActivity - setupBottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx); // error!

        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void DisplayAllUsersLikes() {
        Log.d(AlertActivity.class.getSimpleName(), "AlertActivity - DisplayAllUsersLikes");
        FirebaseRecyclerAdapter<Likes, LikesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Likes, LikesViewHolder>
                (
                        Likes.class,
                        R.layout.all_likes_layout,
                        LikesViewHolder.class,
                        LikesRef
                ) {
            @Override
            protected void populateViewHolder(final LikesViewHolder viewHolder, final Likes model, int position) {
                Log.d(AlertActivity.class.getSimpleName(), "AlertActivity - populateViewHolder");

                // find the posts that have likes
                final String LikeKey = getRef(position).getKey();

                Log.d(AlertActivity.class.getSimpleName(), "LikeKey: " + LikeKey + "\n");

                // use the key to find the following likes via LikesRef
                // by this, we can find who likes this post by userID
                LikesRef.child(LikeKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot child: dataSnapshot.getChildren()){

                            String userID = child.getKey();
                            String likeTime = child.getValue(String.class);

                            // display on the phone screen
                            viewHolder.setUsername(userID);
                            viewHolder.setLikMsg();
                            viewHolder.setTime(likeTime);
                            viewHolder.setLikeUserImg(userID);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        likeList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class LikesViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public LikesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(final String currUserID){
            Log.d(AlertActivity.class.getSimpleName(), "setUsername" + "\n");
            TextView tvUsername = mView.findViewById(R.id.likeuser);

            // local val of FireBase
            DatabaseReference UsersRef_ = FirebaseDatabase.getInstance().getReference().child("Users");
            UsersRef_.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        TextView tvUsername = mView.findViewById(R.id.likeuser);
                        if (userSnapshot.getKey().equals(currUserID)){
                            tvUsername.setText(userSnapshot.child("username").getValue(String.class));
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setLikMsg(){
            TextView tvLikeMsg = mView.findViewById(R.id.likemsg);
            tvLikeMsg.setText("likes your post.");
        }

        public void setTime(String likeTime){
            TextView tvTime = mView.findViewById(R.id.likedate);
            tvTime.setText(likeTime);
        }

        public void setLikeUserImg(String userID){

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRefProfile = storage.getReference();

            // access info for user profile image
            storageRefProfile = storageRefProfile.child("Profile Images").child(userID + ".jpg");

            // be to be careful when storageRefProfile is null reference, meaning that
            if (storageRefProfile != null){
                storageRefProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String img_uri = uri.toString();
                        ImageView tvImg = mView.findViewById(R.id.likeuserphoto);
                        Picasso.get().load(img_uri).into(tvImg);
                    }
                });
            }
        }

    } // end public static class LikesViewHolder extends RecyclerView.ViewHolder

} // end public class AlertActivity extends AppCompatActivity
