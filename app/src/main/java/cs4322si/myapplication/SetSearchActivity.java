package cs4322si.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SetSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_search);
    }

    protected void btnClick(View view) {
        Intent returnIntent;
        switch (view.getId()) {
            case R.id.btnSearch:
                //returnIntent = new Intent();
                //setResult(Activity.RESULT_OK, returnIntent);
                setResult(Activity.RESULT_OK);
                finish();
                break;
            case R.id.btnCancel:
                //returnIntent = new Intent();
                //setResult(Activity.RESULT_CANCELED, returnIntent);
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }
    }
}
