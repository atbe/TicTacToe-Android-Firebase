package edu.msu.ahmedibr.connect4_team17.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.msu.ahmedibr.connect4_team17.R;

public class WinnerActivity extends FirebaseUserActivity {
    public static final String WINNING_PLAYER_NAME_BUNDLE_KEY = "com.cse476.team17.winning_player_name";
    public static final String LOSING_PLAYER_NAME_BUNDLE_KEY = "com.cse476.team17.losing_player_name";

    public static final String TIE_GAME_BUNDLE_KEY = "com.cse476.team17.tie_game";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);

        // Check if the game was won or tied
        if (getIntent().getExtras().containsKey(TIE_GAME_BUNDLE_KEY)) {
            this.setTieGame();
        } else if (getIntent().getExtras().containsKey(WINNING_PLAYER_NAME_BUNDLE_KEY)) {
            this.setWinnerAndLoserNames();
        } else {
            throw new RuntimeException("WinnerActivity did not receive a valid bundle!");
        }

        setAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
//                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
//                    Log.d(LOGIN_STATUS_CHANGED_TAG, "onAuthStateChanged:signed_out");
                    // close the game room
                    finish();
                }
            }
        });
    }

    /**
     * Sets the textviews for the winner and loser.
     */
    private void setWinnerAndLoserNames() {
        String winnerPlayerName = getIntent().getStringExtra(WINNING_PLAYER_NAME_BUNDLE_KEY);
        String loserPlayerName = getIntent().getStringExtra(LOSING_PLAYER_NAME_BUNDLE_KEY);

        ((TextView)findViewById(R.id.nameOfWinner)).setText(winnerPlayerName);
        ((TextView)findViewById(R.id.nameOfLoser)).setText(loserPlayerName);
    }

    /**
     * Sets the layout to show only a tie game message.
     *
     */
    private void setTieGame() {
        // Hide the winner and loser sayings and the loser name
        ((TextView)findViewById(R.id.youWon)).setText(R.string.tied);
        ((TextView)findViewById(R.id.youLost)).setText(R.string.tied);
        ((TextView)findViewById(R.id.nameOfLoser)).setText("");
        this.setWinnerAndLoserNames();
    }

    /**
     * Handles starting a new game
     * @param view:
     */
    public void onNewGame(View view) {
        // by calling finish we send the user to the GameRoomActivity
        Intent gameWaitroom = new Intent(getBaseContext(), GameRoomActivity.class);
        startActivity(gameWaitroom);
        finish();
    }
}
