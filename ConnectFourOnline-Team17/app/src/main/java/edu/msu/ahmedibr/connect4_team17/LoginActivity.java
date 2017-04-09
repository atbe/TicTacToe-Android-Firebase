package edu.msu.ahmedibr.connect4_team17;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    /**
     * Username field
     */
    private EditText mUsernameEditText;

    /**
     * Password field
     */
    private EditText mPasswordEditText;

    /// authentication agent
    private FirebaseAuth mAuth;

    // Listener for sign-in status changes
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Tags
     */
    public static final String LOGIN_STATUS_HANGED_TAG = "LOGIN_STATUS_CHANGED";
    public static final String AUTH_FAILED_TAG = "AUTH_FAILED";

    private final String FAKE_EMAIL_DOMAIN_URL = "@tictactoefor476appyo.com";

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
        setContentView(R.layout.activity_main);

        initViews();
        FirebaseApp.initializeApp(this);

        // firebase authentication init
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_out");
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
        mAuth.addAuthStateListener(mAuthListener);

        // TODO: If the user is logged in go straight to game search activity
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_instructions, menu);
        return true;
    }

    private boolean loginFieldsAreEmpty() {
        if (mUsernameEditText.getText().length() == 0) {
            Snackbar.make(
                    findViewById(R.id.login_coordinator_layout),
                    getResources().getText(R.string.username_cannot_be_empty),
                    Snackbar.LENGTH_LONG).show();
            return false;
        }
        else if (mPasswordEditText.getText().length() == 0) {
            Snackbar.make(
                    findViewById(R.id.login_coordinator_layout),
                    getResources().getText(R.string.password_cannot_be_empty),
                    Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
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

    }

    public void onStartCreateAccount(View view) {
        Intent createAccountIntent = new Intent(this, CreateAccountActivity.class);
        startActivity(createAccountIntent);
    }
}
