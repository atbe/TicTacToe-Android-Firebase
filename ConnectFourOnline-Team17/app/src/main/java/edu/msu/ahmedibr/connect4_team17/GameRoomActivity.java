package edu.msu.ahmedibr.connect4_team17;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static edu.msu.ahmedibr.connect4_team17.LoginActivity.LOGIN_STATUS_HANGED_TAG;

public class GameRoomActivity extends FirebaseUserActivity {

    private TextView mUsernameTextView;
    private TextView mWhenCreatedAccountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);

        FirebaseApp.initializeApp(this);

        setAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    mUsernameTextView.setText(mAuth.getCurrentUser().getEmail().concat("\n"));
                    mWhenCreatedAccountTextView.setText(mAuth.getCurrentUser().getUid());
                } else {
                    // User is signed out
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_out");
                    // TODO: Add actions to remove user data from preferences close game, and return
                    finish();
                }
            }
        });

        initViews();
    }

    private void initViews() {
        mUsernameTextView = (TextView) findViewById(R.id.username_textview);
        mWhenCreatedAccountTextView = (TextView) findViewById(R.id.created_account_time_textview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_sign_out:
                FirebaseAuth.getInstance().signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
