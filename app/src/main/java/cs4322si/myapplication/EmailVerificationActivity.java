package cs4322si.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private Button verifyButton;
    private Button signOutButton;
    private TextView emailTextView;

    private static final String TAG = "VerifyAccount";

    //private static boolean emailSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        emailTextView = findViewById(R.id.emailTextView);
        emailTextView.setText(
                TextUtils.isEmpty(user.getEmail()) ? "No email" : user.getEmail());

        verifyButton = findViewById(R.id.verifyButton);
        verifyButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendEmailVerification();
                //if (emailSent) {
                //}
            }
        });

        signOutButton = findViewById(R.id.signoutBtn);
        signOutButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent i = new Intent(getBaseContext(), StartActivity.class);
                startActivity(i);
                finish();
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
                                    //emailSent = true;
                                    Log.d(TAG, "sentEmailVerification:success");
                                    Toast.makeText(EmailVerificationActivity.this,
                                            "Verification email sent to " + user.getEmail() + ".  Please click the link to activate your account.",
                                            Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                    Intent i = new Intent(getBaseContext(), StartActivity.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    //emailSent = false;
                                    Log.e(TAG, "sendEmailVerification", task.getException());
                                    Toast.makeText(EmailVerificationActivity.this,
                                            //"Failed to send verification email: " + task.getException().getMessage(),
                                            "Failed to send verification email",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                // [END send_email_verification]
            }
        }
    }
}

