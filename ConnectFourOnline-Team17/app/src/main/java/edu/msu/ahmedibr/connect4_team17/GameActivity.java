package edu.msu.ahmedibr.connect4_team17;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // set the GameView and set the current player textview
        mConnectFourView = (ConnectFourView)findViewById(R.id.connectFourView);
        mConnectFourView.beginGame((TextView)findViewById(R.id.current_player_name_textview));

        /*
         * Restore any state
         */
        if(savedInstanceState != null) {
            mConnectFourView.getFromBundle(savedInstanceState);
            // TODO: If activity needs to restore anything.
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
                        int winningPlayerId = mConnectFourView.getCurrentPlayerId() == ConnectFourGame.PLAYER_ONE_ID ?
                                ConnectFourGame.PLAYER_TWO_ID : ConnectFourGame.PLAYER_ONE_ID;

                        setWinnerAndLoserNames(winningPlayerId);
                        moveToWinnerActivity();
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
     * Called when the 'Done' button is pressed.
     *
     * @param view The button.
     */
    public void onMoveDone(View view) {
        mConnectFourView.onMoveDone();

        if (mConnectFourView.isGameWon()) {
           setWinnerAndLoserNames(mConnectFourView.getWinningPlayerId());
           moveToWinnerActivity();
        }
        else if (mConnectFourView.isThereATie()) {
            this.moveToWinnerActivityWithTie();
        }
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
}
