package edu.msu.ahmedibr.connect4_team17.Activities;

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

import edu.msu.ahmedibr.connect4_team17.R;

import static edu.msu.ahmedibr.connect4_team17.Activities.LoginActivity.LOGIN_STATUS_HANGED_TAG;
import static edu.msu.ahmedibr.connect4_team17.Constants.CREATOR_DATA_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.GAME_POOL_STATE_KEY;
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
               } else {
                    // User is signed out
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_out");

                    // close the game room
                    finish();
                }
            }
        });

        initViews();

        mOpenGamesAdapter = new FirebaseListAdapter<Game>(this, Game.class,
                android.R.layout.two_line_list_item, mGamesDatabaseRef.orderByChild(GAME_POOL_STATE_KEY).equalTo(0)) {
            @Override
            protected void populateView(View view, Game game, int position) {
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
                Game game = (Game) adapterView.getItemAtPosition(position);
                makeSnack(String.format("You pressed game from creator '%s'", game.getCreator()), Snackbar.LENGTH_LONG);

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


    public void onCreateGame(View view) {
        final String username = mAuth.getCurrentUser().getDisplayName();
        final String uid = mAuth.getCurrentUser().getUid();
        final Game game = new Game(new Game.User(username, uid));
        Log.d("CreateGame", String.format("Creating game for user '%s'", username));

        // check if there is already a game this user created
        // match by creator/id
        mGamesDatabaseRef.orderByChild(CREATOR_DATA_KEY.concat("/").concat(USER_ID_KEY))
                .equalTo(uid) // equal to the current users id
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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

    public static class Game {
        // Model a user
        public static class User {
            private String mDisplayName;
            private String mId;

            public String getDisplayName() {
                return mDisplayName;
            }

            public void setDisplayName(String mDisplayName) {
                this.mDisplayName = mDisplayName;
            }

            public String getId() {
                return mId;
            }

            public void setId(String mId) {
                this.mId = mId;
            }

            public User() {
            }

            public User(String displayName, String id) {
                mDisplayName = displayName;
                mId = id;
            }
        }

        public enum State { OPEN, JOINED, STARTED }

        private int mState = State.OPEN.ordinal();

        private User mCreator;
        private User mJoiner = null;

        public Game() {
            // no-arg ctor needed for firebase
        }

        public Game(User creator) {
            mCreator = creator;
        }

        public User getCreator() {
            return mCreator;
        }

        public void setCreator(User mCreator) {
            this.mCreator = mCreator;
        }

        public User getJoiner() {
            return mJoiner;
        }

        public void setJoiner(User mJoiner) {
            this.mJoiner = mJoiner;
        }

        public int getState() {
            return mState;
        }

        public void setState(int state) {
            this.mState = state;
        }
    }
}
