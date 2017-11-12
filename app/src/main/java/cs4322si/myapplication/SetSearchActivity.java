package cs4322si.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetSearchActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private Spinner mCategory;

    private EditText mEndDate, mStartDate, mSearchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_search);

        mCategory = findViewById(R.id.mCategory);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
                R.array.owlCategories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategory.setAdapter(adapter);

        mStartDate = findViewById(R.id.mStartDate);
        mEndDate = findViewById(R.id.mEndDate);
        mSearchText = findViewById(R.id.mSearchText);

        SetDate startDate = new SetDate(mStartDate, this);
        SetDate endDate = new SetDate(mEndDate, this);

    }

    protected void btnClick(View view) {
        Intent returnIntent;
        switch (view.getId()) {
            case R.id.btnSearch:
                int catNumber = mCategory.getSelectedItemPosition();
                String searchText = mSearchText.getText().toString();

                Date startDate, endDate;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    startDate = sdf.parse(mStartDate.getText().toString());
                } catch (ParseException ex) {
                    startDate = null;
                }
                try {
                    endDate = sdf.parse(mEndDate.getText().toString());
                } catch (ParseException ex) {
                    endDate = null;
                }


                if ((catNumber == 0) && (TextUtils.isEmpty(searchText)) && (endDate == null) && (startDate == null)) {
                    Toast.makeText(this, "No search parameters detected.  Please select at least one search parameter, or click Cancel.", Toast.LENGTH_LONG).show();
                }
                else {
                    returnIntent = new Intent();
                    returnIntent.putExtra("searchCategory", catNumber);
                    returnIntent.putExtra("searchText", searchText);
                    if ((endDate != null) && (startDate != null) && (startDate.after(endDate))) {
                        returnIntent.putExtra("searchStartDate", endDate);
                        returnIntent.putExtra("searchEndDate", startDate);
                    } else {
                        returnIntent.putExtra("searchStartDate", startDate);
                        returnIntent.putExtra("searchEndDate", endDate);
                    }
                    setResult(Activity.RESULT_OK, returnIntent);
                    //setResult(Activity.RESULT_OK);
                    finish();
                }
                break;
            case R.id.btnCancel:
                //returnIntent = new Intent();
                //setResult(Activity.RESULT_CANCELED, returnIntent);
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }
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


}
