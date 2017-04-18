package edu.msu.ahmedibr.connect4_team17.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.msu.ahmedibr.connect4_team17.DatabaseModels;

import static edu.msu.ahmedibr.connect4_team17.Constants.CONNECTIVITY_BROADCAST_TAG;
import static edu.msu.ahmedibr.connect4_team17.Constants.GAMES_DATABASE_ROOT_KEY;

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

    private Dialog mConnectionLostDialog;

    private BroadcastReceiver mConnectionReciever;

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

        // initialize the connection receiver and dialog
        initConnectionBroadcastReciever();
        initConnectionLostDialog();
        attachWifiSuicideReciever();
    }

    private void initConnectionLostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Woops! Your internet connection has been lost. Please " +
                "reopen the game after restoring the connection.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent);

                        finish();
                    }
                });
        mConnectionLostDialog = builder.create();
    }

    private void initConnectionBroadcastReciever() {
        mConnectionReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(CONNECTIVITY_BROADCAST_TAG, "New event recieved.");
                if (!isConnected()) {
                    notifyUserAndExit();
                }
            }
        };
    }

    /**
     * Displays an alert for the user that the internet connection has been lost and kills the app.
     */
    void notifyUserAndExit() {
        mConnectionLostDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (mConnectionLostDialog.isShowing()) {
            mConnectionLostDialog.dismiss();
        }

        unregisterReceiver(mConnectionReciever);

        super.onDestroy();
    }

    boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Subscribes to internet connectivity changes and exits app if internet ever disconnects.
     */
    private void attachWifiSuicideReciever() {
        // first check if we're connected at all
        if (!isConnected()) {
            notifyUserAndExit();
        }

        // subscribe to any connectivity events
        registerReceiver(mConnectionReciever,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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
