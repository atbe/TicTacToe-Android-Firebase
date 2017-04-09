package edu.msu.ahmedibr.connect4_team17.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

import edu.msu.ahmedibr.connect4_team17.R;

public class LoginActivity extends FirebaseUserActivity {

    /**
     * Username field
     */
    private EditText mUsernameEditText;

    /**
     * Password field
     */
    private EditText mPasswordEditText;


    /**
     * Tags
     */
    public static final String LOGIN_STATUS_HANGED_TAG = "LOGIN_STATUS_CHANGED";
    public static final String AUTH_FAILED_TAG = "AUTH_FAILED";
    public static final String AUTH_STATUS_TAG = "AUTH_STATUS";

    private final String FAKE_EMAIL_DOMAIN_URL = "@tictactoefor476appyo.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        FirebaseApp.initializeApp(this);

        setAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    Intent gameWaitroom = new Intent(getBaseContext(), GameRoomActivity.class);
                    startActivity(gameWaitroom);

                    finish();
                } else {
                    // User is signed out
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_out");
                    // TODO: Add actions to remove user data from preferences close game, and return
                    // to login screen.
                }
            }
        });
    }

    /**
     * Creates references to view objects
     */
    void initViews() {
        mUsernameEditText = (EditText) findViewById(R.id.usernameInput);
        mPasswordEditText = (EditText) findViewById(R.id.passwordInput);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Dialog box for instructions
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.instructions)
                .setMessage(R.string.dialog_message)
                .setPositiveButton("OK", null)
                .show();
        return true;
    }

    private void makeSnack(int stringId, int length) {
        Snackbar.make(
                findViewById(R.id.login_coordinator_layout),
                getResources().getText(stringId),
                length).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_instructions, menu);
        return true;
    }

    private boolean loginFieldsAreEmpty() {
        if (mUsernameEditText.getText().length() == 0) {
            makeSnack(R.string.username_cannot_be_empty, Snackbar.LENGTH_LONG);
            return true;
        }
        else if (mPasswordEditText.getText().length() == 0) {
            makeSnack(R.string.password_cannot_be_empty, Snackbar.LENGTH_LONG);
            return true;
        }
        return false;
    }

    /**
     * Click handler for the login button.
     *
     * @param view
     */
    public void onLogin(View view) {
        if (loginFieldsAreEmpty()) {
            return;
        }

        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        // TODO: Handle login

        mAuth.signInWithEmailAndPassword(username + FAKE_EMAIL_DOMAIN_URL, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOGIN_STATUS_HANGED_TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(AUTH_FAILED_TAG, "signInWithEmail:failed", task.getException());
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                makeSnack(R.string.login_failed_snackbar, Snackbar.LENGTH_LONG);
                                return;
                            } catch (Exception e) {
                                makeSnack(R.string.unhandled_error, Snackbar.LENGTH_LONG);
                                return;
                            }
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                        }

                        makeSnack(R.string.login_succeeded, Snackbar.LENGTH_LONG);
                    }
                });
    }

    public void onStartCreateAccount(View view) {
        Intent createAccountIntent = new Intent(this, CreateAccountActivity.class);
        startActivity(createAccountIntent);
    }
}
