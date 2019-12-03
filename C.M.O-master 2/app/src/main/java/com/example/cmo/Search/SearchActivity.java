package com.example.cmo.Search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmo.Post.Posts;
import com.example.cmo.R;
import com.example.cmo.Utils.BottomNavigationViewHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.w3c.dom.Text;

public class SearchActivity extends AppCompatActivity {

    private Context mContext = SearchActivity.this;
    private static final int ACTIVITY_NUM = 1;

    private EditText mSearchField;
    private Button mSearchBtn;
    private RecyclerView mResultList;

    private DatabaseReference PostsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
//        setContentView(R.layout.activity_main);

        setupBottomNavigationView(); // error!


        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        mSearchField = (EditText) findViewById (R.id.search_title);
        mSearchBtn = (Button) findViewById(R.id.search_button);

        mResultList = (RecyclerView) findViewById(R.id.result_list);
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
                viewHolder.setDetails(getApplicationContext(),model.getFullname(),model.getDescription());

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

        public PostsViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setDetails(Context ctx, String post_user_name, String post_description){
            TextView user_name = (TextView) mView.findViewById(R.id.post_user_name);
            TextView user_post = (TextView) mView.findViewById(R.id.post_description);

            user_name.setText(post_user_name);
            user_post.setText(post_description);

        }
    }
}
