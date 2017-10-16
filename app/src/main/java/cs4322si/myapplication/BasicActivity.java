package cs4322si.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;

import static cs4322si.myapplication.R.id.imageView;

public class BasicActivity extends AppCompatActivity {

    //private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;

    private TextView mEmptyListMessage;

    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter  adapter;

    private static final Query query = FirebaseDatabase.getInstance().getReference().child("items").limitToLast(25);

    private static final String TAG = "MainPage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEmptyListMessage = findViewById(R.id.emptyTextView);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        //auth = FirebaseAuth.getInstance();
        //storage = FirebaseStorage.getInstance();

        mRecyclerView = findViewById(R.id.itemsRecycler);

        //final RecyclerView.Adapter adapter = newAdapter();

        // Scroll to bottom on new messages
        //adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
        //    @Override
        //    public void onItemRangeInserted(int positionStart, int itemCount) {
        //        mRecyclerView.smoothScrollToPosition(adapter.getItemCount());
        //    }
        //});

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager myLayoutMgr = new LinearLayoutManager(this);
        myLayoutMgr.setReverseLayout(true);
        myLayoutMgr.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(myLayoutMgr);

        FloatingActionButton fabMessaging = (FloatingActionButton) findViewById(R.id.fabMessaging);
        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        FloatingActionButton fabHome = (FloatingActionButton) findViewById(R.id.fabHome);
        FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), PostNewItemActivity.class);
                startActivity(i);
            }
        });

        fabMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (adapter != null) {

                //mFirebaseRecyclerViewAdapter.cleanup()

                attachRecyclerViewAdapter();
                adapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();
        if (isSignedIn()) {
            storage = FirebaseStorage.getInstance();
            attachRecyclerViewAdapter();
        }
        //FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //FirebaseAuth.getInstance().removeAuthStateListener(this);
    }



    private void attachRecyclerViewAdapter() {
        //RecyclerView.Adapter adapter = newAdapter();
        adapter = newAdapter();

        // Scroll to bottom on new messages
        //adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
        //    @Override
        //    public void onItemRangeInserted(int positionStart, int itemCount) {
        //        mRecyclerView.smoothScrollToPosition(adapter.getItemCount());
        //    }
        //});

        mRecyclerView.setAdapter(adapter);
    }

    protected FirebaseRecyclerAdapter newAdapter() {

/*
        //code to check if query/recycler was being loaded with items
        ValueEventListener itemListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                //Owlitem item = dataSnapshot.getValue(Owlitem.class);
                // ...
                String numItems = String.valueOf(dataSnapshot.getChildrenCount());
                Toast.makeText(BasicActivity.this, numItems, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        query.addValueEventListener(itemListener);

*/

        //Query query = mDatabase.child("items").limitToLast(25);

        FirebaseRecyclerOptions<Owlitem> options =
                new FirebaseRecyclerOptions.Builder<Owlitem>()
                        .setQuery(query, Owlitem.class)
                        .setLifecycleOwner(this)
                        .build();


        return new FirebaseRecyclerAdapter<Owlitem, OwlitemHolder>(options) {
            @Override
            public OwlitemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.itemlayout, parent, false);

                return new OwlitemHolder(view);
            }

            @Override
            protected void onBindViewHolder(final OwlitemHolder holder, int position, final Owlitem model) {
                // Bind the Owlitem object to the holder
                holder.bind(model, storage, getBaseContext());

                holder.setOnClickListener(new OwlitemHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //Toast.makeText(getBaseContext(), model.category, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getBaseContext(), ItemDetailActivity.class);
                        i.putExtra("currentItem", (Parcelable) model);
                        //BitmapDrawable drawable = (BitmapDrawable) holder.mPicture.getDrawable();
                        //Bitmap bitmap = drawable.getBitmap();

                        //Drawable dr = holder.mPicture.getDrawable();
                        //Bitmap bmp =  ((GlideBitmapDrawable)dr.getCurrent()).getBitmap();
                        //i.putExtra("mainPicture", bmp);

                        startActivity(i);
                    }

                   @Override
                    public void onItemLongClick(View view, int position) {
                        //Toast.makeText(getBaseContext(), "Item long clicked at " + position, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDataChanged() {
                // If there are no items, show a view that invites the user to add an item.
                mEmptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

/*

            @Override
            public void onError(DatabaseError e) {
                // Called when there is an error getting data. You may want to update
                // your UI to display an error message to the user.
                // ...
            }
*/

        };
    }



    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_basic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getBaseContext(), SettingsActivity.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.action_signout) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                auth.signOut();
            }
            startActivity(new Intent(getBaseContext(), StartActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
