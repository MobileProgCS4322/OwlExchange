package cs4322si.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

    private CheckBox cbSold, cbDelete;

    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;

    private static final String TAG = "ItemDetailActivity";

    private Owlitem currentItem;

    private boolean checkFlag = false;

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

        cbSold = findViewById(R.id.cbSold);
        cbDelete = findViewById(R.id.cbDelete);

        cbSold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!checkFlag) {
                    if (isChecked) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(ItemDetailActivity.this);
                        //AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this, Theme_Material_Dialog_Alert);
                        adb.setTitle("Are you sure?");
                        adb.setMessage("This will mark the item as sold.");
                        adb.setCancelable(true);
                        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkFlag = true;
                                cbSold.setChecked(false);
                            }
                        });
                        // positive button will carry out the deletion
                        adb.setPositiveButton("Mark as Sold", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentItem.traded = true;
                                mDatabase.child("items").child(currentItem.itemId).child("traded").setValue(true);
                            }
                        });
                        AlertDialog confirmDialog = adb.create();
                        confirmDialog.show();
                    } else {
                        AlertDialog.Builder adb = new AlertDialog.Builder(ItemDetailActivity.this);
                        //AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this, Theme_Material_Dialog_Alert);
                        adb.setTitle("Are you sure?");
                        adb.setMessage("This will mark the item as NOT sold.");
                        adb.setCancelable(true);
                        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkFlag = true;
                                cbSold.setChecked(true);
                            }
                        });
                        // positive button will carry out the deletion
                        adb.setPositiveButton("Unmark as sold", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentItem.traded = true;
                                mDatabase.child("items").child(currentItem.itemId).child("traded").setValue(false);
                            }
                        });
                        AlertDialog confirmDialog = adb.create();
                        confirmDialog.show();
                    }
                }
                else {
                    //to avoid endlessly calling setChecked logic over and over.
                    checkFlag = false;
                }
            }
        });

        cbDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!checkFlag) {
                    if (isChecked) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(ItemDetailActivity.this);
                        //AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this, Theme_Material_Dialog_Alert);
                        adb.setTitle("Are you sure?");
                        adb.setMessage("This will delete the item.");
                        adb.setCancelable(true);
                        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkFlag = true;
                                cbDelete.setChecked(false);
                            }
                        });
                        // positive button will carry out the deletion
                        adb.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentItem.deleted = true;
                                mDatabase.child("items").child(currentItem.itemId).child("deleted").setValue(true);
                                finish();
                            }
                        });
                        AlertDialog confirmDialog = adb.create();
                        confirmDialog.show();
                    } else {
                        AlertDialog.Builder adb = new AlertDialog.Builder(ItemDetailActivity.this);
                        //AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this, Theme_Material_Dialog_Alert);
                        adb.setTitle("Are you sure?");
                        adb.setMessage("This will mark the item as NOT deleted.");
                        adb.setCancelable(true);
                        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkFlag = true;
                                cbDelete.setChecked(true);
                            }
                        });
                        // positive button will carry out the deletion
                        adb.setPositiveButton("Undelete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentItem.deleted = true;
                                mDatabase.child("items").child(currentItem.itemId).child("deleted").setValue(false);
                            }
                        });
                        AlertDialog confirmDialog = adb.create();
                        confirmDialog.show();
                    }
                }
                else {
                    //to avoid endlessly calling setChecked logic over and over.
                    checkFlag = false;
                }

            }
        });

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

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(currentItem.ownerKey)) {
            cbDelete.setVisibility(View.VISIBLE);
            cbSold.setVisibility(View.VISIBLE);

            checkFlag = true;       //to avoid triggering the checkbox messages/logic
            cbSold.setChecked(currentItem.traded);
            checkFlag = true;
            cbDelete.setChecked(currentItem.deleted);
            checkFlag = false;
        }
        else {
            cbDelete.setVisibility(View.GONE);
            cbSold.setVisibility(View.GONE);
        }

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
