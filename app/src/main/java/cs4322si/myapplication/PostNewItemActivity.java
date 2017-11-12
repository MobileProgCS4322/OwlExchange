package cs4322si.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostNewItemActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener{

    private Button btnCamera, btnPost;
    private Spinner mCategory;
    private EditText mItemTitle, mDescription;
    private ImageView mPicture;

    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;

    String mCurrentPhotoPath;       //filepath saved here

    private static final String TAG = "PostNewItem";
    private Bitmap theImageBitmap;

    //private static final int REQUEST_IMAGE_CAPTURE = 456;
    private static final int REQUEST_IMAGE_FILESAVE = 789;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_new_item);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        mCategory = findViewById(R.id.mCategory);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
                R.array.owlCategories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategory.setAdapter(adapter);

        mItemTitle = findViewById(R.id.mItemTitle);
        mDescription = findViewById(R.id.mDescription);
        mPicture = findViewById(R.id.mPicture);

        btnCamera = (Button)findViewById(R.id.btnTakePicture);
        btnPost = (Button)findViewById(R.id.btnPost);

        btnCamera.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnPost.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                //...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "cs4322si.myapplication.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_FILESAVE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "BMP_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".bmp",/* suffix */
                storageDir/* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
           if (requestCode == REQUEST_IMAGE_FILESAVE) {
                File imgFile = new File(mCurrentPhotoPath);
                if (imgFile.exists()) {
                    //Glide.with(this).load(imgFile).into(mPicture);

                    Glide.with(this)
                            .load(imgFile)
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    mPicture.setImageBitmap(resource);
                                    theImageBitmap = resource;
                                }
                            });

                    //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    //mPicture.setImageBitmap(myBitmap);
                    mPicture.setBackgroundColor(Color.WHITE);
                }

            }
        }
    }



    private void submitPost() {

        boolean valid = true;
        Bitmap picture;

        final String title = mItemTitle.getText().toString();
        if (TextUtils.isEmpty(title)) {
            mItemTitle.setError("Required.");
            valid = false;
        }
        else {
            mItemTitle.setError(null);

            int catNumber = mCategory.getSelectedItemPosition();
            if (catNumber == 0) {
                Toast.makeText(this, "Please select a category.", Toast.LENGTH_SHORT).show();
                valid = false;
            }
            else {
                if (mPicture.getDrawable() == null) {
                    Toast.makeText(this, "Picture required.", Toast.LENGTH_SHORT).show();
                    valid = false;
                }
            }
        }

        if (valid) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                final String ownerKey = user.getUid();

                //nested code below because we want to store the username with the item as well - so we don't have to retrieve it later

                String username = null;
                ValueEventListener userListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Owluser currUser = dataSnapshot.getValue(Owluser.class);
                        String username = currUser.username;

                        String description = mDescription.getText().toString();
                        long currDate = System.currentTimeMillis();     //use mDate = Date(currDate) to get it back.
                        String category = mCategory.getSelectedItem().toString();

                        DatabaseReference newDatabaseReference = mDatabase.child("items").push();
                        //String key = mDatabase.child("items").push().getKey();
                        String itemKey = newDatabaseReference.getKey();

                        //create the storagepath for the imagefile in Firebase Storage
                        StorageReference storageRef = storage.getReference().child("images").child(ownerKey).child(itemKey);
                        String imageLoc = storageRef.toString();

                        // Get the data from an ImageView as bytes
                        //mPicture.setDrawingCacheEnabled(true);
                        //mPicture.buildDrawingCache();
                        //Bitmap theImageBitmap = mPicture.getDrawingCache();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        theImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask uploadTask = storageRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                //Toast.makeText(PostNewItemActivity.this, "Picture NOT saved!", Toast.LENGTH_LONG).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                //Toast.makeText(PostNewItemActivity.this, "Picture saved!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        Owlitem newItem = new Owlitem(username, ownerKey, title, description, category, currDate, imageLoc);
                        //Map<String, Object> itemValues = newItem.toMap();
                        //Map<String, Object> childUpdates = new HashMap<>();
                        //childUpdates.put("/items/"+key, childUpdates);

                        //mDatabase.child("items").push().setValue(newItem);
                        newDatabaseReference.setValue(newItem, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.e("ERROR", "Data could not be saved " + databaseError.getMessage());
                                } else {
                                    //Log.e("SUCCESS", "Data saved successfully.");
                                    Toast.makeText(PostNewItemActivity.this, "Item posted!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        //also add the item to the user's items list
                        mDatabase.child("users").child(ownerKey).child("myItems").child(itemKey).setValue(true);

                        //Map<String, Object> ownerItem = new HashMap<>();
                        //ownerItem.put(itemKey, true);
                        //Map<String, Object> childUpdates = new HashMap<>();
                        //childUpdates.put("/users/" + owner + "/myItems/", ownerItem);
                        //mDatabase.updateChildren(childUpdates);

                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting user failed, log a message
                        Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                        // ...
                    }
                };

                mDatabase.child("users").child(ownerKey).addListenerForSingleValueEvent(userListener);


            }
        }

    }
}
