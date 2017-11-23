package cs4322si.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BasicActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private DatabaseReference mDatabase;
    private FirebaseStorage storage;

    private TextView mEmptyListMessage;
    private FloatingActionButton fabSearch;

    private RecyclerView mRecyclerView;
    //private FirebaseRecyclerAdapter  adapter;
    private OwlitemAdapter owlitemAdapter;
    private ArrayList<Owlitem> owlitemList = new ArrayList<>();

    private List<String> categories;

    private Query query;
    private ValueEventListener myQueryListener;
    //private static final Query query = FirebaseDatabase.getInstance().getReference().child("items").limitToLast(25);


    private static final String TAG = "MainPage";
    private static final int SET_SEARCH_FILTER = 987;
    private boolean searchFilterOn = false;
    int searchCat;
    String searchText;
    Date searchStartDate, searchEndDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        categories = Arrays.asList(getResources().getStringArray(R.array.owlCategories));
        mEmptyListMessage = findViewById(R.id.emptyTextView);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecyclerView = findViewById(R.id.itemsRecycler);

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
        fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);

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
                //mFirebaseRecyclerViewAdapter.cleanup()
                attachRecyclerViewAdapter();
                //owlitemAdapter.notifyDataSetChanged();
                if (owlitemAdapter != null) {
                    mRecyclerView.smoothScrollToPosition(owlitemAdapter.getItemCount());
                }
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!searchFilterOn) {
                    Intent i = new Intent(getBaseContext(), SetSearchActivity.class);
                    startActivityForResult(i, SET_SEARCH_FILTER);
                }
                else {
                    fabSearch.setImageResource(R.drawable.ic_search_black_24dp);
                    searchFilterOn = false;
                    attachRecyclerViewAdapter();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isSignedIn()) {
            storage = FirebaseStorage.getInstance();
            attachRecyclerViewAdapter();
            FirebaseAuth.getInstance().addAuthStateListener(this);
        }
        else {
            startActivity(new Intent(getBaseContext(), StartActivity.class));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        if (!isSignedIn()) {
            startActivity(new Intent(getBaseContext(), StartActivity.class));
            finish();
        }
    }

    private void attachRecyclerViewAdapter() {

        if (myQueryListener != null) {
            query.removeEventListener(myQueryListener);
        }
        if (!searchFilterOn) {
            query = mDatabase.child("items");
        } else {
            if (searchStartDate != null) {
                if (searchEndDate == null) {        //filter by start date
                    //Toast.makeText(this, Long.toString(searchStartDate.getTime()), Toast.LENGTH_SHORT).show();
                    query = mDatabase.child("items").orderByChild("datePosted").startAt(searchStartDate.getTime());
                    //Toast.makeText(this, "Dates selected", Toast.LENGTH_SHORT).show();
                } else {                              //filter by start date and end date
                    //add a day to the end date (we want everything before this date)
                    Calendar c = Calendar.getInstance();
                    c.setTime(searchEndDate);
                    c.add(Calendar.DATE, 1);  // number of days to add
                    long realSearchEndDate = c.getTimeInMillis();

                    query = mDatabase.child("items").orderByChild("datePosted").startAt(searchStartDate.getTime()).endAt(realSearchEndDate);
                }
            } else if (searchEndDate != null) {       //filter by end date
                //add a day to the end date (we want everything before this date)
                Calendar c = Calendar.getInstance();
                c.setTime(searchEndDate);
                c.add(Calendar.DATE, 1);  // number of days to add
                long realSearchEndDate = c.getTimeInMillis();

                query = mDatabase.child("items").orderByChild("datePosted").endAt(realSearchEndDate);
            } else if (searchCat != 0) { //filter by category
                query = mDatabase.child("items").orderByChild("category").equalTo(categories.get(searchCat));
            } else {  //filter exists, but it's not category or date - must be a search string.
                //Toast.makeText(this, "No dates selected", Toast.LENGTH_SHORT).show();
                query = mDatabase.child("items");
            }
        }

        owlitemAdapter = new OwlitemAdapter(owlitemList);
        mRecyclerView.setAdapter(owlitemAdapter);

        myQueryListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getBaseContext(), "calling onDataChange", Toast.LENGTH_SHORT).show();
                mEmptyListMessage.setVisibility(!dataSnapshot.hasChildren() ? View.VISIBLE : View.GONE);

                owlitemList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Owlitem item = postSnapshot.getValue(Owlitem.class);

                    if (!searchFilterOn) {
                        owlitemList.add(item);
                    } else if ((searchStartDate != null) || (searchEndDate != null)) {
                        //filtered on date search
                        //need to check if there is a category search and a keyword search.
                        if (searchCat == 0) {
                            if (searchText.length() != 0) {
                                if (item.title.toLowerCase().contains(searchText) ||
                                        item.description.toLowerCase().contains(searchText)) {
                                    owlitemList.add(item);
                                }
                            } else {
                                owlitemList.add(item);
                            }
                        } else {
                            //there is a category search
                            if (searchText.length() == 0) {     //no keyword search
                                if (item.category.equals(categories.get(searchCat))) {
                                    owlitemList.add(item);
                                }
                            } else {                  //category AND keyword search
                                if ((item.category.equals(categories.get(searchCat))) &&
                                        (item.title.toLowerCase().contains(searchText) ||
                                                item.description.toLowerCase().contains(searchText))) {
                                    owlitemList.add(item);
                                }
                            }
                        }
                    } else if (searchCat != 0) {
                        //no dates.  filtered on category.
                        //need to check if there is a category search
                        if (searchText.length() != 0) {
                            if (item.title.toLowerCase().contains(searchText) ||
                                    item.description.toLowerCase().contains(searchText)) {
                                owlitemList.add(item);
                            }
                        } else {
                            owlitemList.add(item);
                        }
                    } else {
                        //no date, no category.  filter on keyword
                        if (item.title.toLowerCase().contains(searchText) ||
                                item.description.toLowerCase().contains(searchText)) {
                            owlitemList.add(item);
                        }
                    }
                }

                owlitemAdapter.updateList(owlitemList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
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
            //startActivity(new Intent(getBaseContext(), StartActivity.class));
            //finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SET_SEARCH_FILTER) {
            if (resultCode == Activity.RESULT_OK){
                searchCat  = (int) data.getExtras().get("searchCategory");
                searchText = (String) data.getExtras().get("searchText");
                searchStartDate  = (Date) data.getExtras().get("searchStartDate");
                searchEndDate  = (Date) data.getExtras().get("searchEndDate");
                searchFilterOn = true;
                fabSearch.setImageResource(R.drawable.ic_cancel_black_24dp);
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                searchFilterOn = false;
                fabSearch.setImageResource(R.drawable.ic_search_black_24dp);
            }
        }
    }//onActivityResult

}
