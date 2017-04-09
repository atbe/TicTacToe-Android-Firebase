package edu.msu.ahmedibr.connect4_team17.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.msu.ahmedibr.connect4_team17.R;

import static edu.msu.ahmedibr.connect4_team17.Activities.LoginActivity.LOGIN_STATUS_HANGED_TAG;

public class GameRoomActivity extends FirebaseUserActivity {

    private TextView mUsernameTextView;
    private TextView mWhenCreatedAccountTextView;

    /**
     * Firebase database references
     */
    private DatabaseReference mRootRef;
    private DatabaseReference mGamesRef;

    /**
     * List of all the open games the current player can join
     */
    private ListView mOpenGameList;

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
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mGamesRef = mRootRef.child("games").getRef();

        setAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//                    mUsernameTextView.setText(mAuth.getCurrentUser().getEmail().concat("\n"));
//                    mWhenCreatedAccountTextView.setText(mAuth.getCurrentUser().getUid());

               } else {
                    // User is signed out
                    Log.d(LOGIN_STATUS_HANGED_TAG, "onAuthStateChanged:signed_out");
                    // TODO: Add actions to remove user data from preferences close game, and return
                    finish();
                }
            }
        });

        initViews();

        mOpenGamesAdapter = new FirebaseListAdapter<Game>(this, Game.class, android.R.layout.two_line_list_item, mGamesRef.orderByChild("state").equalTo(0)) {
            @Override
            protected void populateView(View view, Game game, int position) {
                Log.d("PopulatingGameList", String.format("Incoming game from creator '%s' with state '%d'", game.getCreator(), game.getState()));
                if (game.getCreatorId().equals(mAuth.getCurrentUser().getUid())) {
                    ((TextView)view.findViewById(android.R.id.text1)).setText("(ME) ".concat(game.getCreator()));
                } else {
                    ((TextView)view.findViewById(android.R.id.text1)).setText(game.getCreator());
                }
            }
        };
        mOpenGameList.setAdapter(mOpenGamesAdapter);
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

    public void onCreateGame(View view) {
        final String username = mAuth.getCurrentUser().getDisplayName();
        final String uid = mAuth.getCurrentUser().getUid();
        final Game game = new Game(username, mAuth.getCurrentUser().getUid());
        Log.d("CreateGame", String.format("Creating game for user '%s'", username));

        // check if there is already a game this user created
        mGamesRef.orderByChild("creatorId").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    makeSnack(R.string.already_created_game, Snackbar.LENGTH_LONG);
                } else {
                    mGamesRef.push().setValue(game);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public static class Game {
        public enum State { OPEN, JOINED, STARTED }

        private String mCreator;
        private String mCreatorId;
        private int mState = State.OPEN.ordinal();

        // no-arg ctor needed for firebase
        public Game() {
            mState = State.OPEN.ordinal();
        }

        public Game(String creator, String creatorId) {
            mCreator = creator;
            mCreatorId = creatorId;
            mState = State.OPEN.ordinal();
        }

        public String getCreatorId() {
            return mCreatorId;
        }

        public void setCreatorId(String creatorId) {
            this.mCreatorId = creatorId;
        }

        public int getState() {
            return mState;
        }

        public void setState(int state) {
            this.mState = state;
        }

        public String getCreator() {
            return mCreator;
        }

        public void setCreator(String creator) {
            this.mCreator = creator;
        }
    }
}
