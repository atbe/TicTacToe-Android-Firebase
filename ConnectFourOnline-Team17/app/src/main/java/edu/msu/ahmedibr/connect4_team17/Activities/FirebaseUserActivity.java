package edu.msu.ahmedibr.connect4_team17.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.msu.ahmedibr.connect4_team17.DatabaseModels;

import static edu.msu.ahmedibr.connect4_team17.Constants.CREATOR_DATA_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.GAMES_DATABASE_ROOT_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.JOINER_DATA_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.USER_ID_KEY;

public class FirebaseUserActivity extends AppCompatActivity {
    /// authentication agent
    protected FirebaseAuth mAuth;

    // Listener for sign-in status changes
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Reference to the root of the database.
     */
    protected DatabaseReference mDatabaseRootRef;

    /**
     * Firebase games database references
     */
    protected DatabaseReference mGamesDatabaseRef;

    public void setCurrentGame(DatabaseModels.Game mCurrentGame) {
        this.mCurrentGame = mCurrentGame;
    }

    protected DatabaseModels.Game mCurrentGame = null;
    protected String mCurrentGameKey = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // firebase authentication init
        mAuth = FirebaseAuth.getInstance();

        // initialze the root ref
        mDatabaseRootRef = FirebaseDatabase.getInstance().getReference();
        mGamesDatabaseRef = mDatabaseRootRef.child(GAMES_DATABASE_ROOT_KEY).getRef();
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
