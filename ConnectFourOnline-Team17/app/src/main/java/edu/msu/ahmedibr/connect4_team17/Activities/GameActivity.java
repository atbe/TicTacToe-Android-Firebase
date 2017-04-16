package edu.msu.ahmedibr.connect4_team17.Activities;

import android.content.DialogInterface;
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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import edu.msu.ahmedibr.connect4_team17.ConnectFour.ConnectFourGame;
import edu.msu.ahmedibr.connect4_team17.ConnectFour.ConnectFourView;
import edu.msu.ahmedibr.connect4_team17.DatabaseModels;
import edu.msu.ahmedibr.connect4_team17.R;

import static edu.msu.ahmedibr.connect4_team17.Constants.AM_CREATOR_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.CREATOR_DATA_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.CURRENT_GAME_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.GAME_STATE_JSON_DUMP_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.GAME_POOL_STATE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.IS_WINNER_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.JOINER_DATA_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.LOGIN_STATUS_CHANGED_TAG;

public class GameActivity extends FirebaseUserActivity {
    public static final String PLAYER_ONE_NAME_BUNDLE_KEY = "com.cse476.team17.player_one_name_bundle";
    public static final String PLAYER_TWO_NAME_BUNDLE_KEY = "com.cse476.team17.player_two_name_bundle";

    /**
     * Reference to the ConnectFourView used to make function calls and check for game state.
     */
    ConnectFourView mConnectFourView;

    /**
     * The name of the winning player, if any.
     */
    String mWinnerName = null;

    /**
     * The name of the losing player, if any.
     */
    String mLoserName = null;

    private String mCurrentGameKey;

    private boolean mAmCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {

                    // User is signed out
                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_out");

                    // close the game room
                    finish();
                }
            }
        });

        // get the game key and whether I'm creator from the bundle
        mCurrentGameKey = getIntent().getStringExtra(CURRENT_GAME_BUNDLE_KEY);
        mAmCreator = getIntent().getBooleanExtra(AM_CREATOR_BUNDLE_KEY, false);

        // set the GameView and set the current player textview
        mConnectFourView = (ConnectFourView)findViewById(R.id.connectFourView);
        mConnectFourView.beginGame((TextView)findViewById(R.id.current_player_name_textview),
                mAuth.getCurrentUser().getUid());

        // setup the data listener and send the initial game state if needed
        mGamesDatabaseRef.child(mCurrentGameKey)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // TODO: Other player made a change to game state,
                        // load it
                        Log.d("GameStateChange", "The game state has changed.");
                        makeSnack("SHOULD REFRESH GAME", Snackbar.LENGTH_LONG);

                        // this is the case in which the current player is the creator and the game is
                        // yet to be created
                        if (dataSnapshot.child(GAME_POOL_STATE_KEY).getValue(Integer.class) == DatabaseModels.Game.State.JOINED.ordinal()
                                && mAmCreator) {
                            // will run in transaction
                            initializeFirebaseGame();
                            Log.d("GameStateChange", "Initializing game");
                        } else {
                            loadGameFromJson(dataSnapshot.child(GAME_STATE_JSON_DUMP_KEY).getValue(String.class));
                            mConnectFourView.invalidate();
                        }

                        // in-progress game, let's make sure the game has not ended
                        if (mConnectFourView.isGameWon()) {
                            sendFirebaseWinningState();
                            mGamesDatabaseRef.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

        /*
         * Restore any state
         */
        if(savedInstanceState != null) {
            mConnectFourView.getFromBundle(savedInstanceState);
            // TODO: If activity needs to restore anything.
        }
    }

    private boolean didIWin() {
        return mConnectFourView.getWinningPlayerUid().equals(mAuth.getCurrentUser().getUid());
    }

    /**
     * Each user will need to see the result of the game so they are sent here to get their status
     * updated then sent to the winner activity.
     */
    private void handleGameEnded() {
        setWinnerAndLoserNames(mConnectFourView.getWinningPlayerId());
        moveToWinnerActivity();
    }

    /**
     * This is run by the creator of the game. It starts a new ConnectFourGame and pushes the game
     * state to Firebase. Once the game is initialized the state should be
     */
    private void initializeFirebaseGame() {
        Log.d("initializeFirebaseGame", "Initializing...");

        mGamesDatabaseRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // dump the game to json string so we can push it to the database
                String jsonString = gameToJsonString();
                mutableData.child(mCurrentGameKey)
                        .child(GAME_STATE_JSON_DUMP_KEY)
                        .setValue(jsonString);
                // now the game hsa been started
                mutableData.child(mCurrentGameKey)
                        .child(GAME_POOL_STATE_KEY)
                        .setValue(DatabaseModels.Game.State.STARTED.ordinal());
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
        });
    }

    private String gameToJsonString() {
        return mConnectFourView.putStateToJson();
    }

    private void loadGameFromJson(String json) {
        mConnectFourView.loadGameFromJson(json);
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

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Quit Game")
                .setMessage("Are you sure you want to quit the current game? All progress will be lost.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(intent);

                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Handles the Surrender button click
     * @param view: TODO: either the game or Connect4View
     */
    public void onSurrender(View view) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Surrender")
                .setMessage("Are you sure you want to Surrender? The other player will win.")
                .setPositiveButton("Yes",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Surrender the game and go to Winner screen
                        mConnectFourView.userSurrenders(mAuth.getCurrentUser().getUid());
                        sendFirebaseWinningState();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * Called if the current user has either won or lost the current game.
     */
    void sendFirebaseWinningState() {
        // get our users tree key for the database, either joiner or creator
        final String position = mAmCreator ? CREATOR_DATA_KEY : JOINER_DATA_KEY;
        mGamesDatabaseRef.child(mCurrentGameKey)
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        mutableData.child(position)
                                .child(IS_WINNER_KEY)
                                .setValue(didIWin());
                        mutableData.child(GAME_STATE_JSON_DUMP_KEY)
                                .setValue(gameToJsonString());
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        handleGameEnded();
                    }
                });
    }

    /**
     * Called when the 'Done' button is pressed.
     *
     * @param view The button.
     */
    public void onMoveDone(View view) {
        if (!mConnectFourView.isMyTurn()) {
            makeSnack("It's not your turn!", Snackbar.LENGTH_LONG);
            return;
        }

        if (!mConnectFourView.onMoveDone()) {
            // do not update state as the move has not actually changed
            return;
        }

        if (mConnectFourView.isGameWon()) {
            sendFirebaseWinningState();
            // TODO: CHange game state in database
            // TODO: Send names to winning activity
        }
        else if (mConnectFourView.isThereATie()) {
            this.moveToWinnerActivityWithTie();
            // TODO: CHange game state in database
            // TODO: Send names to winning activity
        }

        // write out the new game state to the server in a transaction
        mGamesDatabaseRef.child(mCurrentGameKey)
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        mutableData.child(GAME_STATE_JSON_DUMP_KEY)
                                .setValue(gameToJsonString());
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    }
                });
    }

    /**
     * Used to set the winner and loser names based on the winner id.
     *
     * @param winningPlayerId The id of the winning player (1 or 2)
     */
    private void setWinnerAndLoserNames(int winningPlayerId) {
        mWinnerName = winningPlayerId == ConnectFourGame.PLAYER_ONE_ID ?
                getIntent().getStringExtra(PLAYER_ONE_NAME_BUNDLE_KEY) :
                getIntent().getStringExtra(PLAYER_TWO_NAME_BUNDLE_KEY);

        mLoserName = winningPlayerId == ConnectFourGame.PLAYER_ONE_ID ?
                getIntent().getStringExtra(PLAYER_TWO_NAME_BUNDLE_KEY) :
                getIntent().getStringExtra(PLAYER_ONE_NAME_BUNDLE_KEY);
    }

    /**
     * Loads the winner and loser into the intent bundle and launches the WinnerActivity.
     */
    private void moveToWinnerActivity() {
        Intent intent = new Intent(this, WinnerActivity.class);
        intent.putExtra(WinnerActivity.WINNING_PLAYER_NAME_BUNDLE_KEY, mWinnerName);
        intent.putExtra(WinnerActivity.LOSING_PLAYER_NAME_BUNDLE_KEY, mLoserName);
        startActivity(intent);
        finish();
    }

    /**
     * Used to store the state of the game.
     *
     * @param outState The bundle to store the state to.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mConnectFourView.putToBundle(outState);
    }

    /**
     * Launches the Winner activity and indicates that there is a tie.
     *
     * TODO: May be better to launch a dedicated activity for this instead of WinnerActivity.
     */
    private void moveToWinnerActivityWithTie() {
        Intent intent = new Intent(this, WinnerActivity.class);

        mWinnerName = getIntent().getStringExtra(PLAYER_ONE_NAME_BUNDLE_KEY);
        mLoserName = getIntent().getStringExtra(PLAYER_TWO_NAME_BUNDLE_KEY);
        intent.putExtra(WinnerActivity.TIE_GAME_BUNDLE_KEY, true);
        intent.putExtra(WinnerActivity.WINNING_PLAYER_NAME_BUNDLE_KEY, mWinnerName);
        intent.putExtra(WinnerActivity.LOSING_PLAYER_NAME_BUNDLE_KEY, mLoserName);

        startActivity(intent);
        finish();
    }


    public void makeSnack(String string, int length) {
        Snackbar.make(
                findViewById(R.id.game_activity_coordinator_layout),
                string,
                length).show();
    }
}
