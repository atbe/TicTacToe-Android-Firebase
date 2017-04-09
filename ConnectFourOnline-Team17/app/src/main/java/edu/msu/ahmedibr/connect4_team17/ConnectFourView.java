package edu.msu.ahmedibr.connect4_team17;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import edu.msu.ahmedibr.connect4_team17.Activities.GameActivity;

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
     * The id of the winning player. 0 by default indicating no winner.
     */
    private int winningPlayerId = 0;

    /**
     * Used to check if the game has been won.
     * The game is won if the id of the winner is diff from 0.
     *
     * @return True if the game has a winner.
     */
    public boolean isGameWon() {
        return winningPlayerId != 0;
    }

    /**
     * Getter for the id of the winning player.
     *
     * @return The id of the player who won.
     */
    public int getWinningPlayerId() {
        return winningPlayerId;
    }

    /**
     * Getter for the current players id. The player who's turn it currently is.
     *
     * @return 1 or 2 depending on who is playing. 0 if game is not initialized yet.
     */
    public int getCurrentPlayerId() {
        return game.getCurrentPlayerId();
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
        String playerOneName = parentBundle.getString(GameActivity.PLAYER_ONE_NAME_BUNDLE_KEY);
        String playerTwoName = parentBundle.getString(GameActivity.PLAYER_TWO_NAME_BUNDLE_KEY);

        mGameActivity = (GameActivity) getContext();

        game = new ConnectFourGame(mGameActivity, playerOneName, playerTwoName);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        game.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isHandled = game.onTouchEvent(this, event);
        invalidate();
        return isHandled;
    }

    /**
     * Allows the parent activity to tell the game it's the next players turn
     */
    public void onMoveDone() {
        // TODO: Check for win here so we can report it to the parent activity
        game.beginNextPlayersTurn();

        int winnerIdNumber = game.checkForWin();
        if (winnerIdNumber != 0) {
            winningPlayerId = winnerIdNumber;
        }

        setCurrentPlayerName();
        invalidate();
    }

    /**
     * USed to initialize the game once the activity has finished loading.
     *
     * @param currentPlayerNameTextView The textview that holds the current players name
     */
    public void beginGame(TextView currentPlayerNameTextView) {
        // safety check, otherwise I want the app to crash because something went wrong
        assert currentPlayerNameTextView != null;

        mCurrentPlayerNameTextView = currentPlayerNameTextView;
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
}

