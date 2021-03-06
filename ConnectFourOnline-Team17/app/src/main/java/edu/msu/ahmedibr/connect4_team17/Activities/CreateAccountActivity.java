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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import edu.msu.ahmedibr.connect4_team17.R;

import static edu.msu.ahmedibr.connect4_team17.Constants.CREATE_ACCOUNT;
import static edu.msu.ahmedibr.connect4_team17.Constants.EMAIL_BADLY_FORMATTED_EXCEPTION_FIREBASE;
import static edu.msu.ahmedibr.connect4_team17.Constants.FAKE_EMAIL_DOMAIN_URL;
import static edu.msu.ahmedibr.connect4_team17.Constants.LOGIN_STATUS_CHANGED_TAG;
import static edu.msu.ahmedibr.connect4_team17.Constants.WEAK_PASSWORD_EXCEPTION_FIREBASE;

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

        // firebase authentication init
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
//                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(mUsername).build();
                    user.updateProfile(profileChangeRequest);

                    Intent gameWaitroom = new Intent(getBaseContext(), GameRoomActivity.class);
                    startActivity(gameWaitroom);

                    finish();
                } else {
                    // User is signed out
                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // hook re authentication listener
        if (mAuthListener != null) {
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

        // user no real email authentication for this project, and firebase requires *some* email
        mAuth.createUserWithEmailAndPassword(username + FAKE_EMAIL_DOMAIN_URL, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(CREATE_ACCOUNT, "createUserWithEmailAndPassword:failed", task.getException());
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                makeSnack(R.string.password_not_complex_enough, Snackbar.LENGTH_LONG);
                                return;
                            } catch (FirebaseAuthUserCollisionException e) {
                                makeSnack(R.string.email_address_invalid, Snackbar.LENGTH_LONG);
                                return;
                            } catch (Exception e) {
                                // firebase is weird and does not throw FirebaseAuthWeakPasswordException so this is a fail-safe
                                if (task.getException().getLocalizedMessage().contains(WEAK_PASSWORD_EXCEPTION_FIREBASE)) {
                                    makeSnack(R.string.password_not_complex_enough, Snackbar.LENGTH_LONG);
                                    return;
                                } else if (task.getException().getLocalizedMessage().contains(EMAIL_BADLY_FORMATTED_EXCEPTION_FIREBASE)) {
                                    makeSnack(R.string.username_contains_invalid_chars, Snackbar.LENGTH_LONG);
                                    return;
                                }
                                makeSnack(R.string.unhandled_error, Snackbar.LENGTH_LONG);
                                return;
                            }
                        }

                        Log.d(CREATE_ACCOUNT, "createUserWithEmail:onComplete:" + task.getResult());

                        makeSnack(R.string.account_created, Snackbar.LENGTH_LONG);
                    }
                });
    }
}
