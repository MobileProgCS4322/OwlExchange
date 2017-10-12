package cs4322si.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;
    private CoordinatorLayout messageBarLayout;
    private ConstraintLayout my_window;

    private FirebaseAuth auth;

    private static final int RC_SIGN_IN = 123;
    //private static final int RC_REGISTER = 456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // [START initialize_auth]
        auth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        messageBarLayout = findViewById(R.id.messageBarLayout);
        loginButton = findViewById(R.id.signInButton);
        registerButton = findViewById(R.id.registerButton);
        my_window = findViewById(R.id.my_window);

        loginButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sign in
                startActivityForResult(
                        // Get an instance of AuthUI based on the default app
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setLogo(R.drawable.owl196)
                                //.setIsSmartLockEnabled(false)
                                .setAllowNewEmailAccounts(false)
                                .build(),
                        RC_SIGN_IN);

            }
        });

        registerButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // register
                Intent i = new Intent(getBaseContext(), RegisterActivity.class);
                //startActivityForResult(i, RC_REGISTER);
                startActivity(i);
            }
        });

        auth.signOut();
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // already signed in
            if (user.isEmailVerified()) {
                //startActivity(new Intent(getBaseContext(), MainActivity.class));
                startActivity(new Intent(getBaseContext(), BasicActivity.class));
                finish();
            }
            else {
                //not verified.
                startActivity(new Intent(getBaseContext(), EmailVerificationActivity.class));
                finish();
            }
        }
    }
    // [END on_start_check_user]

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    if (user.isEmailVerified()) {
                        //startActivity(new Intent(getBaseContext(), MainActivity.class));
                        startActivity(new Intent(getBaseContext(), BasicActivity.class));
                        finish();
                        return;
                    } else {
                        //not verified.
                        startActivity(new Intent(getBaseContext(), EmailVerificationActivity.class));
                        finish();
                    }
                }

            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.unknown_error);
                    return;
                }
            }

            showSnackbar(R.string.unknown_sign_in_response);
        }
/*        else if (requestCode == RC_REGISTER) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

        }*/

    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        //hide keyboard
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(my_window.getWindowToken(), 0);

        Snackbar.make(messageBarLayout, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

}
