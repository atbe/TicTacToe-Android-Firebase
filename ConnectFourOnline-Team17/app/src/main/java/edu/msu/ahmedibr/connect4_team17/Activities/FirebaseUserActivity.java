package edu.msu.ahmedibr.connect4_team17.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by abe on 4/9/17.
 */

public class FirebaseUserActivity extends AppCompatActivity {
    /// authentication agent
    protected FirebaseAuth mAuth;

    // Listener for sign-in status changes
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // firebase authentication init
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // unhook the authentication listener
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // hook re authentication listener
        mAuth.addAuthStateListener(mAuthListener);

        // TODO: If the user is logged in go straight to game search activity
    }

    public void setAuthStateListener(FirebaseAuth.AuthStateListener listner) {
        mAuthListener = listner;
    }
}
