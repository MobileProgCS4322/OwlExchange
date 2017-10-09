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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button registerButton;

    private CoordinatorLayout messageBarLayout;

    private FirebaseAuth auth;

    private static final String TAG = "CreateAccount";

    private ConstraintLayout my_window;

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        messageBarLayout = findViewById(R.id.messageBarLayout);
        mEmailField = findViewById(R.id.emailEditText);
        mPasswordField = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        my_window = findViewById(R.id.my_window);

        registerButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
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
        else if (!email.matches(".*kennesaw.edu$")) {
            mEmailField.setError("KSU email address required.");
            valid = false;
        }
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

        return valid;
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
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
        // [END create_user_with_email]
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
