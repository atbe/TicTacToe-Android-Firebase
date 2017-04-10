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
import com.google.firebase.database.ValueEventListener;

import edu.msu.ahmedibr.connect4_team17.DatabaseModels;
import edu.msu.ahmedibr.connect4_team17.R;

import static edu.msu.ahmedibr.connect4_team17.Activities.LoginActivity.LOGIN_STATUS_HANGED_TAG;
import static edu.msu.ahmedibr.connect4_team17.Constants.CREATOR_DATA_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.CURRENT_GAME_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.GAME_POOL_STATE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.JOINER_DATA_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.USER_ID_KEY;

public class GameRoomActivity extends FirebaseUserActivity {

    /**
     * List of all the open games the current player can join
     */
    private ListView mOpenGameList;

    /**
     * Listview adapter for the open games.
     */
    private FirebaseListAdapter mOpenGamesAdapter;

    @Override
    protected void onStart() {
        super.onStart();
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
                    // User is signed in
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    // poll to get a current game
                    getCurrentGame();

                } else {

                    // User is signed out
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_out");

                    // close the game room
                    finish();
                }
            }
        });

        initViews();

        mOpenGamesAdapter = new FirebaseListAdapter<DatabaseModels.Game>(this, DatabaseModels.Game.class,
                android.R.layout.simple_list_item_1, mGamesDatabaseRef.orderByChild(GAME_POOL_STATE_KEY).equalTo(0)) {
            @Override
            protected void populateView(View view, DatabaseModels.Game game, int position) {
                // fills in the list of games with the users name (thats all for now)
                Log.d("PopulatingGameList", String.format("Incoming game from creator '%s' with state '%d'", game.getCreator(), game.getState()));
                if (game.getCreator().getId().equals(mAuth.getCurrentUser().getUid())) {
                    ((TextView)view.findViewById(android.R.id.text1))
                            .setText("(ME) ".concat(game.getCreator().getDisplayName()));
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
                makeSnack(String.format("You pressed game from creator '%s'", game.getCreator()), Snackbar.LENGTH_LONG);

                joinGame(mOpenGamesAdapter.getRef(position).getKey());

                // TODO: Join game and update database, start game activity
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOpenGamesAdapter.cleanup();
    }

    private void initViews() {
//        mUsernameTextView = (TextView) findViewById(R.id.);
//        mWhenCreatedAccountTextView = (TextView) findViewById(R.id.created_account_time_textview);
        mOpenGameList = (ListView) findViewById(R.id.gameList);
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
        final DatabaseModels.Game game = new DatabaseModels.Game(new DatabaseModels.User(username, uid));
        Log.d("CreateGame", String.format("Creating game for user '%s'", username));

        // check if there is already a game this user created
        // match by creator/id
        mGamesDatabaseRef.orderByChild(CREATOR_DATA_KEY.concat("/").concat(USER_ID_KEY))
                .equalTo(uid) // equal to the current users id
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // TODO: if someone joins the game we need to jump to game activity

                        if (dataSnapshot.exists()) {
                            makeSnack(R.string.already_created_game, Snackbar.LENGTH_LONG);
                        } else {
                            mGamesDatabaseRef.push().setValue(game);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    /**
     * Getting the current game.
     */
    public void getCurrentGame() {
        String uid = mAuth.getCurrentUser().getUid();

        // check if user is in a game as creator
        mGamesDatabaseRef.orderByChild(CREATOR_DATA_KEY.concat("/").concat(USER_ID_KEY))
                .equalTo(uid)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        }
                        // should only be in one game as the creator
                        assert 1 == dataSnapshot.getChildrenCount();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            mCurrentGame = snapshot.getValue(DatabaseModels.Game.class);
                            mCurrentGameKey = snapshot.getKey();

//                            makeSnack("You're in a game you created", Snackbar.LENGTH_LONG);
                            checkGameState();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

        // check if user is in a game as joiner
        mGamesDatabaseRef.orderByChild(JOINER_DATA_KEY.concat("/").concat(USER_ID_KEY))
                .equalTo(uid)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        }
                        // should only be in one game as the creator
                        assert 1 == dataSnapshot.getChildrenCount();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            mCurrentGame = snapshot.getValue(DatabaseModels.Game.class);
                            mCurrentGameKey = snapshot.getKey();

                            checkGameState();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void resumeGame() {
        Log.d("checkGameState", "Resuming game");
        beginGameActivity();
    }

    private void checkGameState() {
        // if state is STARTED, we need to re-open that game
        if (mCurrentGame.getState() == DatabaseModels.Game.State.STARTED.ordinal()) {
            resumeGame();
        } else if (mCurrentGame.getState() == DatabaseModels.Game.State.JOINED.ordinal()) {
            // TODO: Anything needed here?
        }
    }

    private void joinGame(String gameId) {
        // TODO: if there is currently an open game and I am the creator, remove it

        DatabaseModels.User joiningUser = new DatabaseModels.User(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getUid());
        mGamesDatabaseRef.child(gameId).child(JOINER_DATA_KEY).setValue(joiningUser);
        mGamesDatabaseRef.child(gameId).child(GAME_POOL_STATE_KEY).setValue(DatabaseModels.Game.State.STARTED.ordinal());

        beginGameActivity();
    }

    /**
     * Starts the game where the user will stay until the game ends (game state MUST change)
     */
    private void beginGameActivity() {
        // TODO: Launch into the next activity
        mCurrentGame = null;
        Intent launchGame = new Intent(this, GameActivity.class);
        launchGame.putExtra(CURRENT_GAME_BUNDLE_KEY, mCurrentGameKey);

        startActivity(launchGame);
        // do not finish. if the user ends the game they should come back to the list of players
        finish();
    }
}
