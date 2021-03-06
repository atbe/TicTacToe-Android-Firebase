package edu.msu.ahmedibr.connect4_team17.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import edu.msu.ahmedibr.connect4_team17.DatabaseModels;
import edu.msu.ahmedibr.connect4_team17.R;

import static edu.msu.ahmedibr.connect4_team17.Constants.ALREADY_JOINED_CANT_CREATE_GAME;
import static edu.msu.ahmedibr.connect4_team17.Constants.AM_CREATOR_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.CREATOR_DATA_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.CURRENT_GAME_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.GAME_POOL_STATE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.JOINER_DATA_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.JOINING_GAME_MESSAGE;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_ONE_DISPLAYNAME_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_ONE_UID_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_TWO_DISPLAYNAME_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_TWO_UID_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.USER_ID_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.WAITING_FOR_CREATOR_MESSAGE;

public class GameRoomActivity extends FirebaseUserActivity {

    /**
     * List of all the open games the current player can join
     */
    private ListView mOpenGameList;

    /**
     * Listview adapter for the open games.
     */
    private FirebaseListAdapter mOpenGamesAdapter;

    private boolean mAmGameCreator = false;

    ValueEventListener mCreatedGamesListener;
    ValueEventListener mJoinedGamesListener;

    private void setAmGameCreator(boolean f) {
        mAmGameCreator = f;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // re-subscribe the listeners
        mGamesDatabaseRef.orderByChild(CREATOR_DATA_KEY.concat("/").concat(USER_ID_KEY))
                .equalTo(mAuth.getCurrentUser().getUid())
                .limitToFirst(1)
                .addValueEventListener(mCreatedGamesListener);
        mGamesDatabaseRef.orderByChild(JOINER_DATA_KEY.concat("/").concat(USER_ID_KEY))
                .equalTo(mAuth.getCurrentUser().getUid())
                .limitToFirst(1)
                .addValueEventListener(mJoinedGamesListener);
    }

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
//                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    monitorMyGameState();

                } else {
//                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_out");
                    // close the game room
                    finish();
                }
            }
        });

        initViews();

        createDatabaseListeners();

        // This is the adapter for the list of open games
        mOpenGamesAdapter = new FirebaseListAdapter<DatabaseModels.Game>(this, DatabaseModels.Game.class,
                android.R.layout.simple_list_item_1, mGamesDatabaseRef.orderByChild(GAME_POOL_STATE_KEY).equalTo(0)) {
            @Override
            protected void populateView(View view, DatabaseModels.Game game, int position) {
                // fills in the list of games with the users name (that's all for now)
                Log.d("PopulatingGameList", String.format("Incoming game from creator '%s' with state '%d'", game.getCreator(), game.getState()));
                if (game.getCreator().getId().equals(mAuth.getCurrentUser().getUid())) {
                    ((TextView)view.findViewById(android.R.id.text1))
                            .setText("(ME) ".concat(game.getCreator().getDisplayName()));
                } else if(game.getJoiner() != null && game.getJoiner().getId().equals(mAuth.getCurrentUser().getUid())
                        && game.getState() == DatabaseModels.Game.State.JOINED.ordinal()) {
                    // TODO: This will never show because we are filtering on state.equalTo(0) (open games)
                    ((TextView)view.findViewById(android.R.id.text1))
                            .setText("(Joining...) ".concat(game.getCreator().getDisplayName()));
                } else {
                    ((TextView)view.findViewById(android.R.id.text1))
                            .setText(game.getCreator().getDisplayName());
                }
            }
        };
        mOpenGameList.setAdapter(mOpenGamesAdapter);

        // when the user clicks on a game in the list have the user join that game
        mOpenGameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                DatabaseModels.Game game = (DatabaseModels.Game) adapterView.getItemAtPosition(position);
                makeSnack(JOINING_GAME_MESSAGE, Snackbar.LENGTH_INDEFINITE);

                joinGame(game, mOpenGamesAdapter.getRef(position).getKey());

                // TODO: Join game and update database, start game activity
            }
        });
    }

    /**
     * Initializes the event listeners for the database.
     */
    private void createDatabaseListeners() {
        mCreatedGamesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }
                // should only be in one game as the creator
                assert 1 == dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mCurrentGame = snapshot.getValue(DatabaseModels.Game.class);
                    if (mCurrentGame == null || mCurrentGame.getState() == DatabaseModels.Game.State.ENDED.ordinal()) {
                        return;
                    }
                    mCurrentGameKey = snapshot.getKey();

                    setAmGameCreator(true);

                    if (shouldGameBegin()) {
                        mGamesDatabaseRef.removeEventListener(this);
                        beginGameActivity();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        mJoinedGamesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                // should only be in one game as the joiner
                assert 1 == dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mCurrentGame = snapshot.getValue(DatabaseModels.Game.class);
                    if (mCurrentGame == null || mCurrentGame.getState() == DatabaseModels.Game.State.ENDED.ordinal()) {
                        return;
                    }
                    mCurrentGameKey = snapshot.getKey();

                    setAmGameCreator(false);

                    if (shouldGameBegin()) {
                        mGamesDatabaseRef.removeEventListener(this);
                        beginGameActivity();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOpenGamesAdapter.cleanup();
    }

    private void initViews() {
        mOpenGameList = (ListView) findViewById(R.id.gameList);
    }

    private void makeSnack(int stringId, int length) {
        Snackbar.make(
                findViewById(R.id.game_waitroom_activity_coordinator_layout),
                getResources().getText(stringId),
                length).show();
    }

    private void makeSnack(String string, int length) {
        Snackbar.make(
                findViewById(R.id.game_waitroom_activity_coordinator_layout),
                string,
                length).show();
    }


    /**
     * Allow the user to create a game and place it in the pool of open games.
     *
     * @param view
     */
    public void onCreateGame(View view) {
        final String username = mAuth.getCurrentUser().getDisplayName();
        final String uid = mAuth.getCurrentUser().getUid();
        final DatabaseModels.Game game = new DatabaseModels.Game(new DatabaseModels.User(username, uid, null));
//        Log.d("CreateGame", String.format("Creating game for user '%s'", username));

        // check if there is already a game this user created
        // match by creator/id
        mGamesDatabaseRef.orderByChild(CREATOR_DATA_KEY.concat("/").concat(USER_ID_KEY))
                .equalTo(uid) // equal to the current users id
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // try to load up the game, if it was pushed
                        if (mCurrentGame == null) {
                            mCurrentGame = dataSnapshot.getValue(DatabaseModels.Game.class);
                        }

                        // If the game doesn't exist or the game exists but has ended, allow the player to create another game.
                        if (mCurrentGame == null || (mCurrentGame.getState() == DatabaseModels.Game.State.ENDED.ordinal())) {
                            mGamesDatabaseRef.runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData) {
                                    // safety against callbacks while activity is shutting down
                                    mGamesDatabaseRef.push().setValue(game);
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                    mCurrentGame = game;
                                }
                            });
                        } else {
                            // has someone joined this game?
                            if (mCurrentGame.getJoiner() != null) {
                                // if we are the ones who joined
                                if (mCurrentGame.getJoiner().getId().equals(uid)) {
                                    makeSnack(String.format(ALREADY_JOINED_CANT_CREATE_GAME,
                                            mCurrentGame.getCreator().getDisplayName()), Snackbar.LENGTH_INDEFINITE);
                                }
                            } else {
                                // we are the creator waiting for someone to join
                                makeSnack(R.string.already_created_game, Snackbar.LENGTH_LONG);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // un-subscribe the listeners
        if (mCreatedGamesListener != null) {
            mGamesDatabaseRef.removeEventListener(mCreatedGamesListener);
        }
        if (mJoinedGamesListener != null) {
            mGamesDatabaseRef.removeEventListener(mJoinedGamesListener);
        }
    }

    /**
     * Watches for the current user becoming a Joiner or Creator on a game that is about to begin
     */
    public void monitorMyGameState() {
        String uid = mAuth.getCurrentUser().getUid();

        // check if user is in a game as creator
        mGamesDatabaseRef.orderByChild(CREATOR_DATA_KEY.concat("/").concat(USER_ID_KEY))
                .equalTo(uid)
                .limitToFirst(1)
                .addValueEventListener(mCreatedGamesListener);

        // check if user is in a game as joiner
        mGamesDatabaseRef.orderByChild(JOINER_DATA_KEY.concat("/").concat(USER_ID_KEY))
                .equalTo(uid)
                .limitToFirst(1)
                .addValueEventListener(mJoinedGamesListener);
    }

    /**
     * Checks if the game should begin on this device depending on the state. The user could already
     * have finished the game but the state is still STARTED until both players see the game result.
     */
    private boolean shouldGameBegin() {
        if (mCurrentGame == null) {
            return false;
        }

        // we should not start the game again if we already saw the result
        DatabaseModels.User me = mAmGameCreator ? mCurrentGame.getCreator() : mCurrentGame.getJoiner();
        if (me == null) {
            return false;
        }
        if (me.getIsWinner() != null) {
            return false;
        }

        if (mCurrentGame.getState() == DatabaseModels.Game.State.JOINED.ordinal() && !mAmGameCreator) {
            // TODO: In the future it would be stupid to not allow the player to leave the game in-case
            // the creator never returns to the app.
            if (mCurrentGame.getCreator() != null) {
                makeSnack(String.format(WAITING_FOR_CREATOR_MESSAGE,
                        mCurrentGame.getCreator().getDisplayName()), Snackbar.LENGTH_INDEFINITE);
            }
        }

        // if the game has been JOINED AND you are the creator, start the game
        if (mCurrentGame.getState() == DatabaseModels.Game.State.JOINED.ordinal() && mAmGameCreator) {
            return true;
        } else if (mCurrentGame.getState() == DatabaseModels.Game.State.STARTED.ordinal()) {
            // if the game has started, make sure we haven't already finished
            if (mAmGameCreator) {
                return mCurrentGame.getCreator().getIsWinner() == null;
            } else {
                return mCurrentGame.getJoiner().getIsWinner() == null;
            }
        }

        return false;
    }

    /**
     * Handles the joining of a game. Sets the state of the chosen game to JOINED.
     *
     * @param game The game object which was taped on the list.
     * @param gameId The id of the game that was taped (as seen by the database)
     */
    private void joinGame(DatabaseModels.Game game, final String gameId) {
        if (game.getCreator().getId().equals(mAuth.getCurrentUser().getUid())) {
            makeSnack(R.string.cannot_join_your_own_game, Snackbar.LENGTH_LONG);
            return;
        }

        // The joining user needs to be added to the database entry for the game
        final DatabaseModels.User joiningUser = new DatabaseModels.User(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getUid(), null);
        // TODO: if there is currently an open game and I am the creator, remove it
        mGamesDatabaseRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                mutableData.child(gameId).child(JOINER_DATA_KEY).setValue(joiningUser);

                mutableData.child(gameId).child(GAME_POOL_STATE_KEY).setValue(DatabaseModels.Game.State.JOINED.ordinal());
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                mCurrentGame = dataSnapshot.child(gameId).getValue(DatabaseModels.Game.class);
                shouldGameBegin();
            }
        });

    }

    /**
     * Starts the game where the user will stay until the game ends (game state MUST change)
     */
    private void beginGameActivity() {
        // TODO: Launch into the next activity
        Intent launchGame = new Intent(this, GameActivity.class);
        launchGame.putExtra(CURRENT_GAME_BUNDLE_KEY, mCurrentGameKey);
        launchGame.putExtra(AM_CREATOR_BUNDLE_KEY, mAmGameCreator);
        launchGame.putExtra(PLAYER_ONE_DISPLAYNAME_BUNDLE_KEY, mCurrentGame.getCreator().getDisplayName());
        launchGame.putExtra(PLAYER_ONE_UID_BUNDLE_KEY, mCurrentGame.getCreator().getId());
        launchGame.putExtra(PLAYER_TWO_DISPLAYNAME_BUNDLE_KEY, mCurrentGame.getJoiner().getDisplayName());
        launchGame.putExtra(PLAYER_TWO_UID_BUNDLE_KEY, mCurrentGame.getJoiner().getId());

        // remove the game
        mCurrentGame = null;

        startActivity(launchGame);

        // do not finish. if the user ends the game they should come back to the list of players
        finish();
    }
}
