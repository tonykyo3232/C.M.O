package com.example.cmo;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.ml.vision.common.FirebaseVisionImage; // doesn't work...
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
{
    //private NavigationView navigationView;
    //private DrawerLayout drawerLayout;
    //private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    //private Toolbar mToolbar;

    private Button LogoutButton; // custom design...


    private ImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;

    // by tony
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();


    //private FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

    String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        //currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        // By Tony
//        ImageRef = FirebaseDatabase.getInstance().getReference().child("Image_urls");


        //mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        //setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle("Home");


        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);
        LogoutButton = (Button) findViewById (R.id.logout_button);

        //drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        //actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        //drawerLayout.addDrawerListener(actionBarDrawerToggle);
        //actionBarDrawerToggle.syncState();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //navigationView = (NavigationView) findViewById(R.id.navigation_view);


        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        //View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        //NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        //NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);


//        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot)
//            {
//                if(dataSnapshot.exists())
//                {
//                    if(dataSnapshot.hasChild("fullname"))
//                    {
//                        String fullname = dataSnapshot.child("fullname").getValue().toString();
//                        NavProfileUserName.setText(fullname);
//                    }
//                    if(dataSnapshot.hasChild("profileimage"))
//                    {
//                        String image = dataSnapshot.child("profileimage").getValue().toString();
//                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
//                    }
//                    else
//                    {
//                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item)
//            {
//                UserMenuSelector(item);
//                return false;
//            }
//        });

        LogoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(MainActivity.class.getSimpleName(), "==============\nLogoutButton.setOnClickListener\n===============");
                mAuth.signOut();
                SendUserToLoginActivity();
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToPostActivity();
            }
        });

        DisplayAllUsersPosts();
    }

    private void DisplayAllUsersPosts()
    {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                PostsViewHolder.class,
                                PostsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
                    {
                        Log.d(MainActivity.class.getSimpleName(), "MainActivity - populateViewHolder");
                        final String PostKey = getRef(position).getKey();


                        //String detail = model.getUid() + model.getDate() + model.getTime();
                        //DatabaseReference user_detail = PostsRef.child("Posts").child(detail);
                        //String user_id = user_detail.child("postimage").toString();
                        String img_url = model.getImage_url();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());

                        viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());

//                        viewHolder.setPostimage(getApplicationContext(), model.getPostimage());
                        viewHolder.setPostimage(getApplicationContext(), img_url);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent (MainActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickPostIntent);
                            }
                        });
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        DatabaseReference PostsRef_ = FirebaseDatabase.getInstance().getReference().child("Posts");

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
//            username.setText("Tony Lee");
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
//            ImageView image = (ImageView) mView.findViewById(R.id.post_image);
//            Picasso.get().load(profileimage).into(image);
//            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
//            PostTime.setText("    " + "4:00");
            PostTime.setText(" " + time);
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
            Log.d(MainActivity.class.getSimpleName(), "MainActivity - setPostimage");
            //ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
//            Picasso.with(ctx1).load(postimage).into(PostImage);
            //Log.d(MainActivity.class.getSimpleName(), "==============\npostimagee: [" + postimage + "]\n===============");

            //==================================================================

//            // by website
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("Posts Images").child(postimage);
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String img_uri = uri.toString();
                    Log.d(MainActivity.class.getSimpleName(), "MainActivity - setPostimage - onSuccess --> " + img_uri + "\n");
                    ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
                    Picasso.get().load(img_uri).into(PostImage);
                }
            });

//            // useless code
//            StorageReference gsReference = storage.getReferenceFromUrl(storageRef.toString());
//            Log.d(MainActivity.class.getSimpleName(), "gsReference: [" + storageRef.toString() + "]\n===============");

            Log.d(MainActivity.class.getSimpleName(), "==============\nstorageRef: [" + storageRef.toString() + "]\n===============");

            //==================================================================

            // works, but hard-coding
//            String str = "https://firebasestorage.googleapis.com/v0/b/cmofirebaseproject.appspot.com/o/Posts%20Images%2Fimage%3A8810-November-201900%3A48.jpg?alt=media&token=c1d6471d-2426-4614-aea1-dbc238551ede";
//            Picasso.get().load(str).into(PostImage);
            //Picasso.get().load(postimage).into(PostImage);

        }
    }


    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
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
