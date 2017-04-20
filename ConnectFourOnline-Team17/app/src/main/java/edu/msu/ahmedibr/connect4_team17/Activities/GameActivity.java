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
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_ONE_DISPLAYNAME_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_ONE_UID_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_TWO_DISPLAYNAME_BUNDLE_KEY;

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
                        if (!dataSnapshot.exists()) {
                            return;
                        }

                        // TODO: Other player made a change to game state,
                        // load it
//                        Log.d("GameStateChange", "The game state has changed.");
//                        makeSnack("SHOULD REFRESH GAME", Snackbar.LENGTH_LONG);

                        // this can be null if the game activity is still listening for events but
                        // the game ended (activity still being killed)
                        Integer gameState = dataSnapshot.child(GAME_POOL_STATE_KEY).getValue(Integer.class);
                        if (gameState == null) {
                            return;
                        }

                        // this is the case in which the current player is the creator and the game is
                        // yet to be created
                        if (gameState == DatabaseModels.Game.State.JOINED.ordinal()
                                && mAmCreator) {
                            // will run in transaction
                            initializeFirebaseGame();
//                            Log.d("GameStateChange", "Initializing game");
                        } else {
                            String jsonData = dataSnapshot.child(GAME_STATE_JSON_DUMP_KEY).getValue(String.class);
                            if (jsonData != null) {
                                loadGameFromJson(jsonData);
                            }
                            mConnectFourView.invalidate();
                        }

                        // in-progress game, let's make sure the game has not ended
                        if (mConnectFourView.isGameWon() &&
                                !(dataSnapshot.child(GAME_POOL_STATE_KEY).getValue(Integer.class).equals(
                                        DatabaseModels.Game.State.ENDED.ordinal()))) {
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
    private void handleGameEndActivity() {
        setWinnerAndLoserNames(mConnectFourView.getWinningPlayerId());

        if (mConnectFourView.isThereATie()) {
            moveToWinnerActivityWithTie();
        } else {
            moveToWinnerActivity();
        }
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
     * This method will send the current players win state to the server. It will write whether this
     * current user won the game or not. Once the write to the database is finished it fires the winner
     * game activity to show the players the results and then begins to update the state of the game
     * to ended. Once the state updating finishes a call to finish() is made to kill the activity.
     */
    void sendFirebaseWinningState() {
        // get our users tree key for the database, either joiner or creator
        final String position = mAmCreator ? CREATOR_DATA_KEY : JOINER_DATA_KEY;

        mGamesDatabaseRef.child(mCurrentGameKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                // don't update the state if the game is over
                Integer gameState = dataSnapshot.child(GAME_POOL_STATE_KEY).getValue(Integer.class);
                if (gameState == null) {
                    return;
                }
                if (gameState.equals(DatabaseModels.Game.State.ENDED.ordinal())) {
                    return;
                }

                mGamesDatabaseRef.child(mCurrentGameKey)
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {

                        mutableData.child(position)
                                .child(IS_WINNER_KEY)
                                .setValue(didIWin());
                        mutableData.child(GAME_STATE_JSON_DUMP_KEY)
                                .setValue(gameToJsonString());

                        handleGameEnd();
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        if (!b) {
                            Log.e("sendFirebaseWinning", String.format(
                                    "Error updating the game state with win information!\n%s", databaseError.getMessage()));
                            return;
                        }
                        // try to update the game state
                        handleGameEndActivity();
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * This function will write the new state of the game (ENDED) to the database.
     *
     * There are two cases when this function is called, either the other player has already been
     * made aware that the game has ended or they have not. If they have been notified, it is safe
     * to archive the game.
     */
    private void handleGameEnd() {
        Log.e("HandleGameEnd", "Game is ending now!");
        final String otherPlayerPosition = mAmCreator ? JOINER_DATA_KEY : CREATOR_DATA_KEY;
        mGamesDatabaseRef.child(mCurrentGameKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshotGamesDb) {
                        // if the other player has not seen the game result, do not mark game as ended
                        if (!dataSnapshotGamesDb.exists()) {
                            mGamesDatabaseRef.removeEventListener(this);
                            return;
                        }

                        // don't update the state if the other game has not seen the results
                        if (!dataSnapshotGamesDb.child(otherPlayerPosition).child(IS_WINNER_KEY).exists()) {
                            return;
                        }

                        // first update the state in the games root
                        mGamesDatabaseRef.runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                mutableData.child(mCurrentGameKey).child(GAME_POOL_STATE_KEY).setValue(DatabaseModels.Game.State.ENDED.ordinal());
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, final DataSnapshot dataSnapshotGames) {
                                handleGameEndArchive();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    /**
     * This function handles the archiving of the game. It moves the game from the games tree to the
     * archived_games tree in the database. This will only happen if both players are aware that
     * the game has ended so that we can prevent a player who is not in the app from missing the
     * results of the game.
     */
    private void handleGameEndArchive() {
        final String otherPlayerPosition = mAmCreator ? JOINER_DATA_KEY : CREATOR_DATA_KEY;
        mGamesDatabaseRef.child(mCurrentGameKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // if the other player has not seen the game result, do not mark game as ended
                if (!dataSnapshot.child(otherPlayerPosition).child(IS_WINNER_KEY).exists() ||
                        !dataSnapshot.exists()) {
                    Log.d("HandleArchive", "SHOULD NOT RUN");
                    mGamesDatabaseRef.removeEventListener(this);
                    return;
                }

                // we can archive the game now
                mArchivedGamesDatabaseRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        mutableData.child(mCurrentGameKey).setValue(dataSnapshot.getValue());
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        // now that the game is archived, remove it
                        mGamesDatabaseRef.child(mCurrentGameKey).getRef().removeValue();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
            return;
        }
        else if (mConnectFourView.isThereATie()) {
            sendFirebaseWinningState();
            return;
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

        mWinnerName = getIntent().getStringExtra(PLAYER_ONE_UID_BUNDLE_KEY).equals(mConnectFourView.getWinningPlayerUid()) ?
            getIntent().getStringExtra(PLAYER_ONE_DISPLAYNAME_BUNDLE_KEY) : getIntent().getStringExtra(PLAYER_TWO_DISPLAYNAME_BUNDLE_KEY);
        mLoserName = getIntent().getStringExtra(PLAYER_ONE_UID_BUNDLE_KEY).equals(mConnectFourView.getWinningPlayerUid()) ?
                getIntent().getStringExtra(PLAYER_TWO_DISPLAYNAME_BUNDLE_KEY) : getIntent().getStringExtra(PLAYER_ONE_DISPLAYNAME_BUNDLE_KEY);
        intent.putExtra(WinnerActivity.WINNING_PLAYER_NAME_BUNDLE_KEY, mWinnerName);
        intent.putExtra(WinnerActivity.LOSING_PLAYER_NAME_BUNDLE_KEY, mLoserName);

        startActivity(intent);
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

        mWinnerName = getIntent().getStringExtra(PLAYER_ONE_DISPLAYNAME_BUNDLE_KEY);
        mLoserName = getIntent().getStringExtra(PLAYER_TWO_DISPLAYNAME_BUNDLE_KEY);
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
