package cs4322si.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PostNewItemActivity extends AppCompatActivity {

    private Button btnCamera, btnPost;
    private Spinner mCategory;
    private EditText mItemTitle, mDescription;
    private ImageView mPicture;

    private FirebaseAuth auth;
    private DatabaseReference mDatabase;

    String mCurrentPhotoPath;       //filepath saved here

    private static final String TAG = "PostNewItem";

    private static final int REQUEST_IMAGE_CAPTURE = 456;
    private static final int REQUEST_IMAGE_FILESAVE = 789;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_new_item);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        auth = FirebaseAuth.getInstance();

        mCategory = findViewById(R.id.mCategory);

/*        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item) {
            @Override
            public boolean isEnabled(int position){
                // Disable the first item from Spinner
                // First item will be use for hint
                return (!(position == 0));
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }


        };*/

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

    private void dispatchTakePictureIntent2() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg",/* suffix */
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
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    mPicture.setImageBitmap(myBitmap);
                }

            }
            else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mPicture.setImageBitmap(imageBitmap);
                Toast.makeText(PostNewItemActivity.this, "took a picture...", Toast.LENGTH_LONG).show();
            }
        }
    }



    private void submitPost() {

        boolean valid = true;
        Bitmap picture;

        String title = mItemTitle.getText().toString();
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
                String owner = user.getUid();
                String description = mDescription.getText().toString();
                long currDate = System.currentTimeMillis();     //use mDate = Date(currDate) to get it back.
                String category = mCategory.getSelectedItem().toString();

                Owlitem newItem = new Owlitem(owner, description, category, currDate);
                //mDatabase.child("items").push().setValue(newItem);
                mDatabase.child("items").push().setValue(newItem, new DatabaseReference.CompletionListener() {
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

            }
        }

    }
}
