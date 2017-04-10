package edu.msu.ahmedibr.connect4_team17.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import edu.msu.ahmedibr.connect4_team17.Activities.GameRoomActivity;
import edu.msu.ahmedibr.connect4_team17.R;

import static edu.msu.ahmedibr.connect4_team17.Constants.FAKE_EMAIL_DOMAIN_URL;

public class CreateAccountActivity extends AppCompatActivity {

    /**
     * Username field
     */
    private EditText mUsernameEditText;

    private String mUsername;

    /**
     * Password field
     */
    private EditText mPasswordEditText;

    /**
     * Verify password field
     */
    private EditText mPasswordVerifyText;

    /// authentication agent
    private FirebaseAuth mAuth;

    // Listener for sign-in status changes
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Tags
     */
    public static final String LOGIN_STATUS_CHANGED_TAG = "LOGIN_STATUS_CHANGED";
    public static final String AUTH_STATUS_TAG = "AUTH_STATUS";

    @Override
    protected void onStop() {
        super.onStop();
        // unhook the authentication listener
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initViews();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(mUsername).build();
                    user.updateProfile(profileChangeRequest);

                    Intent gameWaitroom = new Intent(getBaseContext(), GameRoomActivity.class);
                    startActivity(gameWaitroom);

                    finish();
                } else {
                    // User is signed out
                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_out");
                    // TODO: Add actions to remove user data from preferences close game, and return
                    // to login screen.
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // hook re authentication listener
        if (mAuth != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    /**
     * Creates references to view objects
     */
    private void initViews() {
        mUsernameEditText = (EditText) findViewById(R.id.usernameInput);
        mPasswordEditText = (EditText) findViewById(R.id.passwordInput);
        mPasswordVerifyText = (EditText) findViewById(R.id.password_verify_input);
    }

    private void makeSnack(int stringId, int length) {
        Snackbar.make(
                findViewById(R.id.create_account_coordinator_layout),
                getResources().getText(stringId),
                length).show();
    }

    private boolean createAccountFieldsAreEmpty() {
        if (mUsernameEditText.getText().length() == 0) {
            makeSnack(R.string.username_cannot_be_empty, Snackbar.LENGTH_LONG);
            return true;
        }
        else if (mPasswordEditText.getText().length() == 0) {
            makeSnack(R.string.password_cannot_be_empty, Snackbar.LENGTH_LONG);
            return true;
        }
        else if (mPasswordVerifyText.getText().length() == 0) {
            makeSnack(R.string.verify_password_cannot_be_empty, Snackbar.LENGTH_LONG);
            return true;
        }
        return false;
    }

    public void onCreateAccount(View view) {
        if (createAccountFieldsAreEmpty()) {
            return;
        }

        String username = mUsernameEditText.getText().toString();
        mUsername = username;
        String password = mPasswordEditText.getText().toString();
        String verifyPassword = mPasswordVerifyText.getText().toString();

        if (!password.equals(verifyPassword)) {
            makeSnack(R.string.password_and_verify_must_match, Snackbar.LENGTH_LONG);
            return;
        }

        // user no real email authntication for this project, and firebase requires *some* email
        mAuth.createUserWithEmailAndPassword(username + FAKE_EMAIL_DOMAIN_URL, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                makeSnack(R.string.password_not_complex_enough, Snackbar.LENGTH_LONG);
                                return;
                            } catch (FirebaseAuthUserCollisionException e) {
                                makeSnack(R.string.email_address_invalid, Snackbar.LENGTH_LONG);
                                return;
                            } catch (Exception e) {
                                makeSnack(R.string.unhandled_error, Snackbar.LENGTH_LONG);
                                return;
                            }
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                        }

                        Log.d(AUTH_STATUS_TAG, "createUserWithEmail:onComplete:" + task.getResult());

                        makeSnack(R.string.account_created, Snackbar.LENGTH_LONG);
                    }
                });

    }
}
