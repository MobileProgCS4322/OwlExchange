package cs4322si.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ItemChatActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private TextView mEmptyListMessage;
    private EditText mMessageEdit;
    private Button sendButton;
    private Spinner sendToSpinner;

    private RecyclerView mRecyclerView;
    //private FirebaseRecyclerAdapter adapter;
    private OwlmessageAdapter owlmessageAdapter;
    private ArrayList<Owlmessage> owlmessageList = new ArrayList<>();

    private Query query;
    private ValueEventListener myQueryListener;

    private DatabaseReference mDatabase;
    private FirebaseAuth auth;

    private static final String TAG = "ItemChatActivity";

    //private String itemKey;
    //private boolean isOwner;
    private Owlitem currentItem;
    private String username;
    private String currUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_chat);

        mEmptyListMessage = findViewById(R.id.emptyChatView);

        sendToSpinner = findViewById(R.id.sendToSpinner);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        //auth = FirebaseAuth.getInstance();

        mRecyclerView = findViewById(R.id.messagesList);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager myLayoutMgr = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(myLayoutMgr);

        mMessageEdit = findViewById(R.id.messageEdit);

        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = mMessageEdit.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    //String currUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Owlmessage newMsg = new Owlmessage(username, currUserId,
                            currentItem.username, currentItem.ownerKey, currentItem.itemId, currentItem.description, msg);
                    mDatabase.child("messages").child(currentItem.itemId).push().setValue(newMsg);
                    mDatabase.child("users").child(currentItem.ownerKey).child("myMessageList").child(currentItem.itemId).setValue(true);
                    mDatabase.child("users").child(currUserId).child("myMessageList").child(currentItem.itemId).setValue(true);
                    mMessageEdit.setText("");
                }
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currUserId = user.getUid();
            ValueEventListener userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Owluser currUser = dataSnapshot.getValue(Owluser.class);
                    username = currUser.username;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting user failed, log a message
                    Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                    // ...
                }
            };
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(userListener);

            //itemKey = (String)getIntent().getExtras().get("itemKey");
            //isOwner = (boolean)getIntent().getExtras().get("isOwner");
            currentItem = getIntent().getExtras().getParcelable("currentItem");
            attachRecyclerViewAdapter();

            if (currUserId.equals(currentItem.ownerKey)) {
                sendToSpinner.setVisibility(View.VISIBLE);
            }
            else {
                sendToSpinner.setVisibility(View.GONE);
            }

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

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }


    private void attachRecyclerViewAdapter() {

        owlmessageAdapter = new OwlmessageAdapter(owlmessageList);
        mRecyclerView.setAdapter(owlmessageAdapter);

        // Scroll to bottom on new messages
        owlmessageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mRecyclerView.smoothScrollToPosition(owlmessageAdapter.getItemCount());
            }
        });

        if (myQueryListener != null) {
            query.removeEventListener(myQueryListener);
        }

        query = mDatabase.child("messages").child(currentItem.itemId);
        myQueryListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                owlmessageList = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Owlmessage owlmessage = postSnapshot.getValue(Owlmessage.class);

                    //if the current user is the owner of the item - show all the messages.
                    if (currUserId.equals(currentItem.ownerKey)) {
                        owlmessageList.add(owlmessage);
                    }
                    //otherwise, only show messages sent by or to the current user. OR if message sent by owner to owner (everyone)
                    else if ((currUserId.equals(owlmessage.senderUserid)) ||
                            (currUserId.equals(owlmessage.receiverUserid)) ||
                            (currentItem.ownerKey.equals(owlmessage.senderUserid) && currentItem.ownerKey.equals(owlmessage.receiverUserid)))
                    {
                        owlmessageList.add(owlmessage);
                    }
                }

                //Toast.makeText(getBaseContext(), "number of messages: " + owlmessageList.size(), Toast.LENGTH_SHORT).show();

                // If there are no messages, show a view that invites the user to add a message.
                mEmptyListMessage.setVisibility(owlmessageList.size() == 0 ? View.VISIBLE : View.GONE);

                owlmessageAdapter.updateList(owlmessageList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

/*    protected FirebaseRecyclerAdapter newAdapter() {

        //private static final Query query = FirebaseDatabase.getInstance().getReference().child("items").limitToLast(25);
        //Query query = mDatabase.child("items").limitToLast(25);
        //mDatabase.child("users").child(user.getUid()).child("myMessageList")
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        Query query;
        query = mDatabase.child("messages").child(currentItem.itemId);

        //query.addValueEventListener()
//        if (isOwner) {  //get all messages

//        }
//        else {
//            query = mDatabase.child("messages").child(itemKey);
//        }

        FirebaseRecyclerOptions<Owlmessage> options =
                new FirebaseRecyclerOptions.Builder<Owlmessage>()
                        .setQuery(query, Owlmessage.class)
                        .setLifecycleOwner(this)
                        .build();

        return new FirebaseRecyclerAdapter<Owlmessage, OwlmessageHolder>(options) {

            @Override
            public OwlmessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message, parent, false);

                return new OwlmessageHolder(view);
            }

            @Override
            protected void onBindViewHolder(OwlmessageHolder holder, int position, Owlmessage msg) {
                // Bind the Owlmessage object to the holder
                holder.bind(msg, currentItem);
            }

            @Override
            public void onDataChanged() {
                // If there are no items, show a view that invites the user to add an item.
                mEmptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            //@Override
            //public void onError(DatabaseError e) {
                // Called when there is an error getting data. You may want to update
                // your UI to display an error message to the user.
                // ...
            //}

        };
    }*/




}
