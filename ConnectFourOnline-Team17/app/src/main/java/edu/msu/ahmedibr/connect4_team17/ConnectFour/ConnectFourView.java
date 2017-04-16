package edu.msu.ahmedibr.connect4_team17.ConnectFour;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import edu.msu.ahmedibr.connect4_team17.Activities.GameActivity;

import static edu.msu.ahmedibr.connect4_team17.ConnectFour.ConnectFourGame.PLAYER_ONE_ID;
import static edu.msu.ahmedibr.connect4_team17.ConnectFour.ConnectFourGame.PLAYER_TWO_ID;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_ONE_DISPLAYNAME_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_ONE_UID_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_TWO_DISPLAYNAME_BUNDLE_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_TWO_UID_BUNDLE_KEY;

/**
 * TODO: document your custom view class.
 */
public class ConnectFourView extends View {

    /**
     * The game object.
     */
    ConnectFourGame game;

    GameActivity mGameActivity = null;

    private TextView mCurrentPlayerNameTextView = null;

    /**
     * The id of the winning player. null by default indicating no winner.
     */
    private String winningPlayerUid = null;

    private String mMyPlayerUid;

    private String mPlayerOneUid;
    private String mPlayerTwoUid;

    /**
     * Used to check if the game has been won.
     * The game is won if the id of the winner is diff from 0.
     *
     * @return True if the game has a winner.
     */
    public boolean isGameWon() {
        winningPlayerUid = game.getWinningPlayerUid();
        return winningPlayerUid != null;
    }

    /**
     * Getter for the uid of the winning player.
     *
     * @return The uid of the player who won.
     */
    public String getWinningPlayerUid() {
        return winningPlayerUid;
    }

    /**
     * Getter for the id of the winning player.
     *
     * @return The id of the player who won.
     */
    public int getWinningPlayerId() {
        return winningPlayerUid.equals(mPlayerOneUid) ? PLAYER_ONE_ID : PLAYER_TWO_ID;
    }

    /**
     * Getter for the current players id. The player who's turn it currently is.
     *
     * @return 1 or 2 depending on who is playing. 0 if game is not initialized yet.
     */
    public int getCurrentPlayerId() {
        return game.getCurrentPlayerId();
    }

    public String getCurrentPlayerUid() {
        return game.getCurrentPlayerUid();
    }

    public ConnectFourView(Context context) {
        super(context);
        initView(null, 0);
    }

    public ConnectFourView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs, 0);
    }

    public ConnectFourView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyle) {
        Bundle parentBundle = ((Activity)getContext()).getIntent().getExtras();

        // the game activity should have had the names of the players passed to it.
        String playerOneName = parentBundle.getString(PLAYER_ONE_DISPLAYNAME_BUNDLE_KEY);
        mPlayerOneUid = parentBundle.getString(PLAYER_ONE_UID_BUNDLE_KEY);
        String playerTwoName = parentBundle.getString(PLAYER_TWO_DISPLAYNAME_BUNDLE_KEY);
        mPlayerTwoUid = parentBundle.getString(PLAYER_TWO_UID_BUNDLE_KEY);

        mGameActivity = (GameActivity) getContext();

        game = new ConnectFourGame(mGameActivity, playerOneName, mPlayerOneUid, playerTwoName, mPlayerTwoUid);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        game.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isMyTurn()) {
            mGameActivity.makeSnack("It's not your turn!", Snackbar.LENGTH_LONG);
            return false;
        }

        boolean isHandled = game.onTouchEvent(this, event);
        invalidate();
        return isHandled;
    }

    /**
     * Allows the parent activity to tell the game it's the next players turn
     */
    public boolean onMoveDone() {
        // TODO: Check for win here so we can report it to the parent activity
        boolean nextPlayersTurn = game.beginNextPlayersTurn();

        // update winning player if there is one
        winningPlayerUid = getWinningPlayerUid();

        setCurrentPlayerName();
        invalidate();
        return nextPlayersTurn;
    }

    /**
     * USed to initialize the game once the activity has finished loading.
     *
     * @param currentPlayerNameTextView The textview that holds the current players name
     */
    public void beginGame(TextView currentPlayerNameTextView, String myPlayerUid) {
        // safety check, otherwise I want the app to crash because something went wrong
        assert currentPlayerNameTextView != null;

        mCurrentPlayerNameTextView = currentPlayerNameTextView;
        mMyPlayerUid = myPlayerUid;
        game.beginGame();

        setCurrentPlayerName();
        invalidate();
    }

    /**
     * Utility method sets the current players name in the parent view.
     */
    public void setCurrentPlayerName() {
        String currentPlayerName = game.getCurrentPlayerName();
        if (currentPlayerName != null) {
            if (mCurrentPlayerNameTextView != null) {
                mCurrentPlayerNameTextView.setText(String.format("%s's Turn", currentPlayerName));
            } else {
                Log.e("ConnectFourView", "setCurrentPlayerName textView was null!");
            }
        } else {
            Log.e("ConnectFourView", "setCurrentPlayerName name was null!");
        }
        invalidate();
    }

    /**
     * Used to store the state of the view.
     *
     * @param outstate The bundle to write the state to.
     */
    public void putToBundle(Bundle outstate) {
        game.putToBundle(outstate);

        // TODO: If view needs to save anything, do it here
    }

    /**
     * Used to restore the state of the view.
     *
     * @param savedInstanceState The bundle to restore from.
     */
    public void getFromBundle(Bundle savedInstanceState) {
        game.getFromBundle(savedInstanceState);

        setCurrentPlayerName();
    }

    /**
     * Checks if there is a tie.
     *
     * @return True if there is a tie, False if not.
     */
    public boolean isThereATie() {
        return game.isThereATie();
    }

    public String putStateToJson() {
        return game.toJsonString();
    }

    public void loadGameFromJson(String json) {
        game.loadFromJson(json);
        setCurrentPlayerName();
        invalidate();
    }

    public boolean isMyTurn() {
        return game.getCurrentPlayerUid().equals(mMyPlayerUid);
    }

    public void userSurrenders(String uid) {
        game.userSurrenders(uid);
        winningPlayerUid = game.getWinningPlayerUid();
    }
}

