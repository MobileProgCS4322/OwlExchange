package cs4322si.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ItemDetailActivity extends AppCompatActivity {

    //private FloatingActionButton fabBack;
    private TextView dTitle, dCategory, dPostDate, dPoster, dDescription;
    private ImageView dPicture;
    private FirebaseStorage storage;

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

    }

    @Override
    public void onStart() {
        super.onStart();
        //if (isSignedIn()) {

        Owlitem currentItem = getIntent().getExtras().getParcelable("currentItem");

        dTitle.setText("Title: " + currentItem.title);
        dCategory.setText("Category: " + currentItem.category);
        dPoster.setText("Posted by: " + currentItem.username);
        dDescription.setText("Additional Description: " + currentItem.description);

        Date datePosted = new Date(currentItem.datePosted);
        SimpleDateFormat spf= new SimpleDateFormat("MMM dd yyyy");
        dPostDate.setText("Posted: " + spf.format(datePosted));

        storage = FirebaseStorage.getInstance();
        StorageReference gsReference = storage.getReferenceFromUrl(currentItem.imageLoc);

        Toast.makeText(getBaseContext(), currentItem.imageLoc, Toast.LENGTH_SHORT).show();

        Glide.with(getBaseContext())
                .using(new FirebaseImageLoader())
                .load(gsReference)
                .into(dPicture);
    }

}
