package cs4322si.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class RegisterActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mUsername;
    private Button registerButton;
    private Button testingButton;

    private CoordinatorLayout messageBarLayout;

    private FirebaseAuth auth;
    private DatabaseReference mDatabase;

    private boolean usernametaken = false;  //used by ValueEventListener.

    private static final String TAG = "CreateAccount";

    private ConstraintLayout my_window;

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        messageBarLayout = findViewById(R.id.messageBarLayout);
        mEmailField = findViewById(R.id.emailEditText);
        mPasswordField = findViewById(R.id.passwordEditText);
        mUsername = findViewById(R.id.usernameEditText);
        registerButton = findViewById(R.id.registerButton);
        my_window = findViewById(R.id.my_window);

        registerButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });

        testingButton = findViewById(R.id.testButton);
        testingButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Query userNameQuery = mDatabase.child("usernames").orderByKey().equalTo("testuser1");
                //if (userNameQuery)

                //mDatabase.child("message").setValue("Hello, World!");
                mDatabase.child("users").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //if ((dataSnapshot != null) && (dataSnapshot.hasChild("testuser1"))) {
                                //if (dataSnapshot.exists()) {
                                //    Toast.makeText(RegisterActivity.this, "found!", Toast.LENGTH_LONG).show();
                                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                    //Toast.makeText(RegisterActivity.this, postSnapshot.getKey(), Toast.LENGTH_LONG).show();

                                    if (postSnapshot.child("username").exists()) {
                                        String usernameVal = (String) postSnapshot.child("username").getValue();
                                        Toast.makeText(RegisterActivity.this, usernameVal, Toast.LENGTH_LONG).show();
                                    }

                                    //if (postSnapshot.child("username").getValue().toString().equals("testusername")) {
                                    //     Toast.makeText(RegisterActivity.this, postSnapshot.getKey(), Toast.LENGTH_LONG).show();
                                    //}
                                    //}
                                    //|| (!dataSnapshot.child(username).exists())) {
                                }
                                //else {
                                //    Toast.makeText(RegisterActivity.this, "not found!", Toast.LENGTH_LONG).show();
                                //}
                            }
                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.w(TAG, "Failed to read value.", error.toException());
                                Toast.makeText(RegisterActivity.this, "failed to read value" + error.toException(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

    }

/*    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();

        //mAuth.signOut();

        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = auth.getCurrentUser();
        //updateUI(currentUser);
    }
    // [END on_start_check_user]*/

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        }
        /*
        else if (!email.matches(".*kennesaw.edu$")) //weak regex for demonstration porpoises.
        {
            mEmailField.setError("KSU email address required.");
            valid = false;
        }
        */
        else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        final String username = mUsername.getText().toString();
        if (TextUtils.isEmpty(username)) {
            mUsername.setError("Required.");
            valid = false;
        }
        return valid;
    }

//        else
//        {
            //note - username operation needs to be atomicized
            //easy, logical way to do this: set username on the next screen - after initial registration
            //(or, use more complex transaction logic to create user and username at same time)
            //also, reading/writing to database before Registration - had to set read:true for anonymous users!

            //we need a synchronous result *right now*. hence, this query.
            //mDatabase.child("usernames")

            /*
            mDatabase.child("users").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            usernametaken = false;
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                if (postSnapshot.child("username").exists()) {
                                    String usernameInDB = (String) postSnapshot.child("username").getValue();
                                    //Toast.makeText(RegisterActivity.this, usernameVal, Toast.LENGTH_LONG).show();
                                    if (usernameInDB.equals(username)) {
                                        usernametaken = true;
                                        break;
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                            //Toast.makeText(RegisterActivity.this, "failed to read value" + error.toException(), Toast.LENGTH_SHORT).show();
                        }
                    });
*/
/*
            if (usernametaken) {
                valid = false;
                mUsername.setError("Username already taken.");

            }
            else {
                valid = true;
                mUsername.setError(null);
                Toast.makeText(RegisterActivity.this, "everything okay", Toast.LENGTH_SHORT).show();
            }
        }


        return valid;
    }*/

    private void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        //first, check validity of username.
        final String username = mUsername.getText().toString();
        mDatabase.child("users").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        usernametaken = false;
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            if (postSnapshot.child("username").exists()) {
                                String usernameInDB = (String) postSnapshot.child("username").getValue();
                                //Toast.makeText(RegisterActivity.this, usernameVal, Toast.LENGTH_LONG).show();
                                if (usernameInDB.equals(username)) {
                                    usernametaken = true;
                                    break;
                                }
                            }
                        }
                        if (usernametaken) {
                            mUsername.setError("Username already taken.");
                        }
                        else {
                            mUsername.setError(null);
                            // [START create_user_with_email]
                            showProgressDialog();
                            auth.createUserWithEmailAndPassword(email, password)
                                    //.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                //note - username operation needs to be atomicized
                                                //easy, logical way to do this: set username on the next screen - after initial registration
                                                //(or, use more complex transaction logic to create user and username at same time)
                                                //also, reading/writing to database before Registration - had to set read:true for anonymous users!
                                                String userKey = auth.getCurrentUser().getUid();
                                                //String username = mUsername.getText().toString();
                                                mDatabase.child("users").child(userKey).child("username").setValue(username);
                                                //for speed, keep a separate table of usernames. *just for usernames*
                                                //mDatabase.child("usernames").child(userName).setValue(userKey);

                                                // registration success
                                                Log.d(TAG, "createUserWithEmail:success");
                                                showSnackbar(R.string.account_creation_success);

                                                sendEmailVerification();
                                                auth.signOut();
                                                hideProgressDialog();

                                                Intent i = new Intent(getBaseContext(), StartActivity.class);
                                                startActivity(i);
                                                finish();

                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                //Log.i("Response","Failed to create user:"+task.getException().getMessage());
                                                showSnackbar("Error:"+ task.getException().getMessage());

                                                //Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                                //        Toast.LENGTH_SHORT).show();
                                            }

                                            hideProgressDialog();
                                        }
                                    });
                            // [END create_user_with_email]*/



                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                        //Toast.makeText(RegisterActivity.this, "failed to read value" + error.toException(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void sendEmailVerification() {
        // [START send_email_verification]
        final FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (!user.isEmailVerified()) {
                user.sendEmailVerification()
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // [START_EXCLUDE]
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "sentEmailVerification:success");

/*                                    AlertDialog.Builder bld = new AlertDialog.Builder(RegisterActivity.this);
                                    bld.setTitle(getResources().getString(R.string.verification_sent_title));
                                    bld.setMessage(getResources().getString(R.string.verification_sent_text));
                                    bld.setPositiveButton(getResources().getString(R.string.OK), null);
                                    AlertDialog successDialog = bld.create();
                                    successDialog.show();*/

                                    Toast.makeText(RegisterActivity.this,
                                            "Verification email sent to " + user.getEmail() + ".  Please click the link to activate your account.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Log.e(TAG, "sendEmailVerification", task.getException());

/*                                    AlertDialog.Builder bld = new AlertDialog.Builder(RegisterActivity.this);
                                    bld.setTitle(getResources().getString(R.string.verification_not_sent_title));
                                    bld.setMessage(getResources().getString(R.string.verification_not_sent_text));
                                    bld.setPositiveButton(getResources().getString(R.string.OK), null);
                                    AlertDialog failDialog = bld.create();
                                    failDialog.show();*/

                                    Toast.makeText(RegisterActivity.this,
                                            //"Failed to send verification email: " + task.getException().getMessage(),
                                            "Failed to send verification email",
                                            Toast.LENGTH_LONG).show();
                                }
                                // [END_EXCLUDE]
                            }
                        });
                // [END send_email_verification]
            }
        }

    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }



    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        //hide keyboard
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(my_window.getWindowToken(), 0);

        Snackbar.make(messageBarLayout, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }



    @MainThread
    private void showSnackbar(String theMessage) {
        //hide keyboard
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(my_window.getWindowToken(), 0);

        Snackbar.make(messageBarLayout, theMessage, Snackbar.LENGTH_LONG).show();
    }

}
