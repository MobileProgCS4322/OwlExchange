package cs4322si.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class MyActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener{

    private DatabaseReference mDatabase;
    private String username;

    private TextView tvLoggedInAs;

    private LinearLayout sellingTab, soldTab;
    private View sellingLine, soldLine;
    private TextView sellingLabel, soldLabel;

    private RecyclerView myItemsRecyclerView;
    private MyOwlitemAdapter owlitemAdapter;
    private ArrayList<Owlitem> owlitemList = new ArrayList<>();

    private Query query;
    private ValueEventListener myQueryListener;


    private RecyclerView soldItemsRecyclerView;
    private MyOwlitemAdapter soldItemsAdapter;
    private ArrayList<Owlitem> soldItemsList = new ArrayList<>();

    private Query soldItemsQuery;
    private ValueEventListener soldItemsQueryListener;



    private static final String TAG = "MyActivityCenter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        tvLoggedInAs = findViewById(R.id.tvUsername);
        sellingTab = findViewById(R.id.sellingTab);
        soldTab = findViewById(R.id.soldTab);

        sellingLine = findViewById(R.id.sellingLine);
        soldLine  = findViewById(R.id.soldLine);


        sellingLabel = findViewById(R.id.sellingLabel);
        soldLabel = findViewById(R.id.soldLabel);

        myItemsRecyclerView = findViewById(R.id.myItemsList);

        myItemsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager myLayoutMgr = new LinearLayoutManager(this);
        myLayoutMgr.setReverseLayout(true);
        myLayoutMgr.setStackFromEnd(true);
        myItemsRecyclerView.setLayoutManager(myLayoutMgr);


        soldItemsRecyclerView = findViewById(R.id.soldItemsList);

        soldItemsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager soldItemsLayoutMgr = new LinearLayoutManager(this);
        soldItemsLayoutMgr.setReverseLayout(true);
        soldItemsLayoutMgr.setStackFromEnd(true);
        soldItemsRecyclerView.setLayoutManager(soldItemsLayoutMgr);


        sellingTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soldLine.setVisibility(View.INVISIBLE);
                soldLabel.setTypeface(Typeface.SANS_SERIF); //only font style
                soldLabel.setTextColor(getResources().getColor(R.color.material_gray_900));
                soldItemsRecyclerView.setVisibility(View.GONE);

                sellingLine.setVisibility(View.VISIBLE);
                sellingLabel.setTypeface(null,Typeface.BOLD); //only text style(only bold)
                sellingLabel.setTextColor(getResources().getColor(R.color.material_red_a200));
                myItemsRecyclerView.setVisibility(View.VISIBLE);

                //owlitemAdapter.notifyDataSetChanged();
            }
        });

        soldTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sellingLine.setVisibility(View.INVISIBLE);
                sellingLabel.setTypeface(Typeface.SANS_SERIF); //only font style
                sellingLabel.setTextColor(getResources().getColor(R.color.material_gray_900));
                myItemsRecyclerView.setVisibility(View.GONE);

                soldLine.setVisibility(View.VISIBLE);
                soldLabel.setTypeface(null,Typeface.BOLD); //only text style(only bold)
                soldLabel.setTextColor(getResources().getColor(R.color.material_red_a200));
                soldItemsRecyclerView.setVisibility(View.VISIBLE);

                //owlitemAdapter.notifyDataSetChanged();
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
        if (isSignedIn()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            ValueEventListener userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Owluser currUser = dataSnapshot.getValue(Owluser.class);
                    username = currUser.username;
                    tvLoggedInAs.setText("Logged in as: " + username);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting user failed, log a message
                    Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                    // ...
                }
            };
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(userListener);

            attachRecyclerViewAdapter();
            attachSoldRecyclerViewAdapter();
        }
        else {
            startActivity(new Intent(getBaseContext(), StartActivity.class));
            finish();
        }
    }


    private void attachRecyclerViewAdapter() {

        if (myQueryListener != null) {
            query.removeEventListener(myQueryListener);
        }
        query = mDatabase.child("items").orderByChild("ownerKey").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        owlitemAdapter = new MyOwlitemAdapter(owlitemList);
        myItemsRecyclerView.setAdapter(owlitemAdapter);

        myQueryListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getBaseContext(), "calling onDataChange", Toast.LENGTH_SHORT).show();
                owlitemList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Owlitem item = postSnapshot.getValue(Owlitem.class);
                    if (!item.traded && !item.deleted) {
                        owlitemList.add(item);
                    }
                }
                owlitemAdapter.updateList(owlitemList);
                //mEmptyListMessage.setVisibility(!dataSnapshot.hasChildren() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void attachSoldRecyclerViewAdapter() {

        if (soldItemsQuery != null) {
            soldItemsQuery.removeEventListener(soldItemsQueryListener);
        }
        soldItemsQuery = mDatabase.child("items").orderByChild("ownerKey").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        soldItemsAdapter = new MyOwlitemAdapter(soldItemsList);
        soldItemsRecyclerView.setAdapter(soldItemsAdapter);

        soldItemsQueryListener = soldItemsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getBaseContext(), "calling onDataChange", Toast.LENGTH_SHORT).show();
                soldItemsList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Owlitem item = postSnapshot.getValue(Owlitem.class);

                    if (item.traded && !item.deleted) {
                        soldItemsList.add(item);
                    }
                }
                soldItemsAdapter.updateList(soldItemsList);
                //mEmptyListMessage.setVisibility(!dataSnapshot.hasChildren() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }


/*

    private void attachInterestedInRecyclerViewAdapter() {

        if (interestedInQueryListener != null) {
            interestedInquery.removeEventListener(interestedInQueryListener);
        }
        interestedInquery = mDatabase.child("users").orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //returns the message list for the user
        interestedInAdapter = new ItemsInterestedInAdapter(interestedInList);
        interestedInRecyclerView.setAdapter(interestedInAdapter);

        interestedInQueryListener = interestedInquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getBaseContext(), "calling onDataChange", Toast.LENGTH_SHORT).show();
                interestedInList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    if (postSnapshot.child("myMessageList").hasChildren()) {
                        for (DataSnapshot itemsMessaged : postSnapshot.child("myMessageList").getChildren()) {
                            Query queryItem = mDatabase.child("items").orderByKey().equalTo(itemsMessaged.getKey());
                            queryItem.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Owlitem item = dataSnapshot.getValue(Owlitem.class);
                                    interestedInList.add(item);
                                    interestedInAdapter.updateList(interestedInList);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                    //Owlitem item = postSnapshot.getValue(Owlitem.class);
                    //interestedInList.add(item);
                }



                //mEmptyListMessage.setVisibility(!dataSnapshot.hasChildren() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

*/


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

}
