package cs4322si.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ItemDetailActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    //private FloatingActionButton fabBack;
    private FloatingActionButton fabChat;
    private TextView dTitle, dCategory, dPostDate, dPoster, dDescription;
    private ImageView dPicture;


    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;

    private static final String TAG = "ItemDetailActivity";

    private Owlitem currentItem;

    //private String itemKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        dTitle = findViewById(R.id.dTitle);
        dCategory = findViewById(R.id.dCategory);
        dPostDate = findViewById(R.id.dPostDate);
        dPoster = findViewById(R.id.dPoster);
        dDescription = findViewById(R.id.dDescription);
        dPicture = findViewById(R.id.dPicture);

        //Bitmap mainImage = getIntent().getParcelableExtra("mainPicture");
        //dPicture.setImageBitmap(mainImage);
        //Glide.with(getBaseContext()).

/*        FloatingActionButton fabBack = findViewById(R.id.fabBack);

        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        FloatingActionButton fabChat = findViewById(R.id.fabChats);

        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnChatClick();
            }
        });

    }

    private void btnChatClick() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {

            if (user.getUid().equals(currentItem.ownerKey)) {
                //check if messagelist exists, if current user is owner of this item...
                ValueEventListener messageListListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                        //if (dataSnapshot.hasChildren()) {
                            //open the messageScreen.
                            startChatActivity(true);
                        }
                        else {
                            Toast.makeText(getBaseContext(), "No messages yet.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting user failed, log a message
                        Log.w(TAG, "Read user chats list failed", databaseError.toException());
                        // ...
                    }
                };
                mDatabase.child("users").child(user.getUid()).child("myMessageList").child(currentItem.itemId).addListenerForSingleValueEvent(messageListListener);

            }
            else {
                //for everyone else, just open the message list.
                startChatActivity(false);
            }
        }
    }


    private void startChatActivity (boolean isOwner) {
        Intent i = new Intent(getBaseContext(), ItemChatActivity.class);
        //i.putExtra("isOwner", isOwner);
        //i.putExtra("itemKey", itemKey);
        i.putExtra("currentItem", (Parcelable) currentItem);
        startActivity(i);
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
        //if (isSignedIn()) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentItem = getIntent().getExtras().getParcelable("currentItem");

        //itemKey = (String)getIntent().getExtras().get("itemKey");
        //itemKey = currentItem.itemId;
        dTitle.setText("Title: " + currentItem.title);
        dCategory.setText("Category: " + currentItem.category);
        dPoster.setText("Posted by: " + currentItem.username);
        dDescription.setText("Additional Description: " + currentItem.description);

        Date datePosted = new Date(currentItem.datePosted);
        SimpleDateFormat spf= new SimpleDateFormat("MMM dd yyyy");
        dPostDate.setText("Posted: " + spf.format(datePosted));

        storage = FirebaseStorage.getInstance();
        StorageReference gsReference = storage.getReferenceFromUrl(currentItem.imageLoc);

        //Toast.makeText(getBaseContext(), currentItem.imageLoc, Toast.LENGTH_SHORT).show();

        Glide.with(getBaseContext())
                .using(new FirebaseImageLoader())
                .load(gsReference)
                .into(dPicture);
        dPicture.setBackgroundColor(Color.WHITE);
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

}
