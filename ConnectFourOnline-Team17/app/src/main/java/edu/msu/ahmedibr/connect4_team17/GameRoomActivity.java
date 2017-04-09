package edu.msu.ahmedibr.connect4_team17;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static edu.msu.ahmedibr.connect4_team17.LoginActivity.LOGIN_STATUS_HANGED_TAG;

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
    private ListView mGameList;

    private FirebaseListAdapter mGameAdapter;


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

        mGameAdapter = new FirebaseListAdapter<Game>(this, Game.class, android.R.layout.two_line_list_item, mGamesRef) {
            @Override
            protected void populateView(View view, Game game, int position) {
                Log.d("PopulatingGameList", String.format("Incoming game from creator '%s'", game.getCreator()));
                ((TextView)view.findViewById(android.R.id.text1)).setText(game.getCreator());
            }
        };
        mGameList.setAdapter(mGameAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGameAdapter.cleanup();
    }

    private void initViews() {
//        mUsernameTextView = (TextView) findViewById(R.id.);
//        mWhenCreatedAccountTextView = (TextView) findViewById(R.id.created_account_time_textview);
        mGameList = (ListView) findViewById(R.id.gameList);
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

    public void onCreateGame(View view) {
    }

    public static class Game {
        private String mCreator;

        public Game() {
            // needed for firebase
        }

        public String getCreator() {
            return mCreator;
        }

        public void setCreator(String creator) {
            this.mCreator = creator;
        }

        public Game(String creator) {
            mCreator = creator;
        }
    }
}
