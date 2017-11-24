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
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class MyActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener{

    private DatabaseReference mDatabase;
    private String username;

    private TextView tvLoggedInAs;

    private LinearLayout sellingTab, buyingTab;
    private View sellingLine, buyingLine;
    private TextView sellingLabel, buyingLabel;

    private static final String TAG = "MyActivityCenter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        tvLoggedInAs = findViewById(R.id.tvUsername);
        sellingTab = findViewById(R.id.sellingTab);
        buyingTab = findViewById(R.id.buyingTab);

        sellingLine = findViewById(R.id.sellingLine);
        buyingLine  = findViewById(R.id.buyingLine);


        sellingLabel = findViewById(R.id.sellingLabel);
        buyingLabel = findViewById(R.id.buyingLabel);

        sellingTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyingLine.setVisibility(View.INVISIBLE);
                buyingLabel.setTypeface(Typeface.SANS_SERIF); //only font style
                buyingLabel.setTextColor(getResources().getColor(R.color.material_gray_900));

                sellingLine.setVisibility(View.VISIBLE);
                sellingLabel.setTypeface(null,Typeface.BOLD); //only text style(only bold)
                sellingLabel.setTextColor(getResources().getColor(R.color.material_red_a200));

            }
        });

        buyingTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sellingLine.setVisibility(View.INVISIBLE);
                sellingLabel.setTypeface(Typeface.SANS_SERIF); //only font style
                sellingLabel.setTextColor(getResources().getColor(R.color.material_gray_900));

                buyingLine.setVisibility(View.VISIBLE);
                buyingLabel.setTypeface(null,Typeface.BOLD); //only text style(only bold)
                buyingLabel.setTextColor(getResources().getColor(R.color.material_red_a200));
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


}
