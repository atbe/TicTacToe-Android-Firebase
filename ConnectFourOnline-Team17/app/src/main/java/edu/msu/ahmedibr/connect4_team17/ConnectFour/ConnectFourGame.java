package edu.msu.ahmedibr.connect4_team17.ConnectFour;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import edu.msu.ahmedibr.connect4_team17.Activities.GameActivity;
import edu.msu.ahmedibr.connect4_team17.R;

import static edu.msu.ahmedibr.connect4_team17.Constants.CHOSEN_CELL_JSON_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.CURRENT_PLAYER_JSON_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.LAST_CHOSEN_CELL_JSON_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.OWNERSHIP_TABLE_JSON_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_ONE_JSON_KEY;
import static edu.msu.ahmedibr.connect4_team17.Constants.PLAYER_TWO_JSON_KEY;

public class ConnectFourGame {
    /**
     * Used to identify player one.
     */
    public static final int PLAYER_ONE_ID = 1;

    /**
     * Used to identify player two.
     */
    public static final int PLAYER_TWO_ID = 2;

    /**
     * Percentage of the display width or height that
     * is occupied by the puzzle.
     */
     private float SCALE_IN_VIEW = 0.88f;

    /**
     * The number of columns used in our game.
     */
    private final int NUMBER_OF_COLUMNS = 7;

    /**
     * The number of rows used in our game.
     */
    private final int NUMBER_OF_ROWS = 6;

    /**
     * Left margin in pixels
     */
    private int marginX;

    /**
     * Top margin in pixels
     */
    private int marginY;

    /**
     * Initialize once per session for dumping/loading
     */
    private Gson gson = new Gson();

    /**
     * The cells in our board.
     *
     * This is a 2d array of the columns.
     *
     * mGridColumns[0][0] is the first column, first (top) row.
     * mGridColumns[NUMBER_OF_COLUMNS-1][NUMBER_OF_ROWS-1] last column, last row (bottom right cell)
     */
    public ArrayList<ArrayList<ConnectFourGameCell>> mGridColumns = new ArrayList<>();

    private int puzzleSize = 0;
    private int puzzleHeight = 0;

    private float pieceScale;

    /**
     * The context the game was used in.
     */
    private GameActivity mParentActivityContext;

    /**
     * Player one in the game.
     */
    private ConnectFourPlayer mPlayerOne = null;

    /**
     * Player two in the game.
     */
    private ConnectFourPlayer mPlayerTwo = null;

    /**
     * Used to track the current player who has the move
     */
    private ConnectFourPlayer mCurrentPlayer = null;

    /**
     * Used to track the last chosen cell of a move.
     */
    private ConnectFourGameCell mChosenCell = null;

    /**
     * Used to store the cell from the last move, used to check for wins.
     */
    private ConnectFourGameCell mLastChosenCell = null;

    /**
     * Getter for the current players name.
     *
     * @return The String which is the current players name.
     */
    String getCurrentPlayerName() {
        if (mCurrentPlayer != null) {
            return mCurrentPlayer.getName();
        }
        return null;
    }

    /**
     * Getter for the current players id.
     *
     * @return The String which is the current players name.
     */
    int getCurrentPlayerId() {
        if (mCurrentPlayer != null) {
            return mCurrentPlayer.getPlayerId();
        }
        return 0;
    }

    /**
     * Should be called only once to choose a starting player and begin their move
     */
    void beginGame() {
        // Randomly choose a starting player
        int randomNum = ThreadLocalRandom.current().nextInt(0, 2);
        mCurrentPlayer = randomNum == 1 ? mPlayerOne : mPlayerTwo;

        beginPlayerMove();
    }

    /** Constructor for our game.
     *
     * @param context: Context of the activity
     * @param playerOneName: name of player 1
     * @param playerTwoName: name of player 2
     */
    public ConnectFourGame(GameActivity context, String playerOneName, String playerOneUid,
                           String playerTwoName, String playerTwoUid) {
        // create the players
        // TODO: Player one always green disk?
        mPlayerOne = new ConnectFourPlayer(playerOneName, R.drawable.spartan_green_player_one,
                PLAYER_ONE_ID, playerOneUid);
        mPlayerTwo = new ConnectFourPlayer(playerTwoName, R.drawable.spartan_white_player_two,
                PLAYER_TWO_ID, playerTwoUid);

        mParentActivityContext = context;

        // initialize the grid cells
        for(int col = 0; col < NUMBER_OF_COLUMNS; col++) {
            // add the row
            mGridColumns.add(new ArrayList<ConnectFourGameCell>());
            for(int row = 0; row < NUMBER_OF_ROWS; row++) {
                // add cells to the row
                mGridColumns.get(col).add(
                        new ConnectFourGameCell(
                            mParentActivityContext,
                            (float)(col + 0.5) / NUMBER_OF_COLUMNS,
                            (float)(row + 0.5) / NUMBER_OF_ROWS * (6/7.0f),
                            row,
                            col)
                );
            }
        }
    }

    /**
     * This method is used to initialize a players turn.
     */
    private void beginPlayerMove() {
//        Log.i("ConnectFourGame", String.format("Beginning next players move: %s", mCurrentPlayer.getName()));
        // chosen cell must be reset
        mLastChosenCell = mChosenCell;
        mChosenCell = null;
    }

    /** Checks if it's valid to change turns yet, and if so, changes turns
     *
     */
    public boolean beginNextPlayersTurn() {
        if (mChosenCell == null) {
            Snackbar.make(mParentActivityContext.findViewById(R.id.game_activity_coordinator_layout),
                    mParentActivityContext.getResources().getText(R.string.no_move_played), Snackbar.LENGTH_LONG).show();
//            Log.d("ConnectFourGame", "User clicked done without making move!");
            return false;
        }

        mCurrentPlayer = mCurrentPlayer.getPlayerId() == mPlayerOne.getPlayerId() ? mPlayerTwo : mPlayerOne;
        beginPlayerMove();
        return true;
    }

    /** Draws the Grid and tells the cells to draw themselves
     *
     * @param canvas
     */
    public void draw(Canvas canvas) {
        int wid = canvas.getWidth();
        int hit = canvas.getHeight();

        // Determine the minimum of the two dimensions
        int minDim = wid < hit ? wid : hit;
        // only set the size when in portrait so landscape has the same size
        // if the app starts in landscape, make the calculation then
        // TODO: Come back and see if this works when the app state is saved
//        if ((puzzleHeight == 0 && puzzleSize == 0))
//        {
            puzzleSize = (int)(minDim * SCALE_IN_VIEW);
            puzzleHeight = puzzleSize * NUMBER_OF_ROWS/NUMBER_OF_COLUMNS;
//        }

        // Compute the margins so we center the puzzle
        marginX = (wid - puzzleSize) / 2;
        marginY = (hit - puzzleHeight) / 2;

        float gridRealWid = NUMBER_OF_COLUMNS * ConnectFourGameCell.EMPTY_CELL_WIDTH;
        pieceScale = (float) puzzleSize / gridRealWid;

        for(ArrayList<ConnectFourGameCell> col : mGridColumns) {
            for (ConnectFourGameCell cell : col) {
                cell.draw(canvas, marginX, marginY, puzzleSize, pieceScale);
            }
        }
    }

    /**
     * Handle a touch event from the view.
     * @param view The view that is the source of the touch
     * @param event The motion event describing the touch
     * @return true if the touch is handled.
     */

    public boolean onTouchEvent(View view, MotionEvent event) {
        float relX = (event.getX() - marginX) / puzzleSize;
        float relY = (event.getY() - marginY) / puzzleSize;
        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                return onTouched(relX, relY);

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                return onReleased(view, relX, relY);

        }
        return false;
    }

    /**
     * Handle a touch message. This is when we get an initial touch
     * @param x x location for the touch, relative to the puzzle - 0 to 1 over the puzzle
     * @param y y location for the touch, relative to the puzzle - 0 to 1 over the puzzle
     * @return true if the touch is handled
     */
    private boolean onTouched(float x, float y) {
//        Log.i("onTouched", String.format("Handing touch for x=%f y=%f", x, y));

        // Check each piece to see if it has been hit
        // We do this in reverse order so we find the pieces in front
        for (int col = 0; col < mGridColumns.size(); col++) {
            for (int row = 0; row < mGridColumns.get(col).size(); row++) {
                if (mGridColumns.get(col).get(row).hit(x, y, puzzleSize, pieceScale)) {

                    // reward the player a disk in this column
                    rewardPlayerDisk(col);

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Rewards a player a cell in the grid depending on the chosen column.
     * Will not reward if column is full. Will not reward if user already chose a disk this move.
     *
     * @param colNumber The column the player chose to place a disk at.
     */
    private void rewardPlayerDisk(int colNumber) {
        // check if the player already selected a disk
        if (mChosenCell != null) {
            Snackbar.make(mParentActivityContext.findViewById(R.id.game_activity_coordinator_layout),
                    mParentActivityContext.getResources().getText(R.string.already_made_move), Snackbar.LENGTH_LONG).show();
            return;
        }

        // check if the column is full by checking the top disk
        if (mGridColumns.get(colNumber).get(0).isOwnedByPlayer()) {
            Snackbar.make(mParentActivityContext.findViewById(R.id.game_activity_coordinator_layout),
                    mParentActivityContext.getResources().getText(R.string.column_full), Snackbar.LENGTH_LONG).show();
            Log.d("ConnectFourGame", "Player chose a column that is full!");
            return;
        }

        // loop in reverse to find columns from the bottom up
        for (int i = mGridColumns.get(colNumber).size() - 1; i >= 0; i--) {
            ConnectFourGameCell cell = mGridColumns.get(colNumber).get(i);

            // find the first empty cell and reward it to player
            if (!cell.isOwnedByPlayer()) {
                mChosenCell = cell;
                mChosenCell.setOwningPlayer(mCurrentPlayer);
                break;
            }
        }
    }

    /**
     * Checks for a winner given the last move.
     *
     * Used for reference: http://stackoverflow.com/questions/32770321/connect-4-check-for-a-win-algorithm
     *
     * @return 0 if no winner, 1 or 2 otherwise indicating the player who won.
     */
    public int checkForWin() {
        // if it is the first move
        if (mLastChosenCell == null)
        {
            return 0;
        }
        int lastPlayerRow = mLastChosenCell.getRow();
        int lastPlayerColumn = mLastChosenCell.getColumn();

        int playerIdNumber = mGridColumns.get(lastPlayerColumn).get(lastPlayerRow).getOwningPlayerIdNumber();
        int playerCount = 0;

        // Horizontal check
        for (int col = 0; col < NUMBER_OF_COLUMNS; col++)
        {
            if (mGridColumns.get(col).get(lastPlayerRow).getOwningPlayerIdNumber() == playerIdNumber) {
                playerCount++;
            } else {
                playerCount = 0;
            }

            if (playerCount >= 4) {
                return playerIdNumber;
            }
        }

        playerCount = 0;

        //Vertical check
        for (int row = 0; row < NUMBER_OF_ROWS; row++)
        {
            if (mGridColumns.get(lastPlayerColumn).get(row).getOwningPlayerIdNumber() == playerIdNumber) {
                playerCount++;
            } else {
                playerCount = 0;
            }

            if (playerCount >= 4) {
                return playerIdNumber;
            }
        }

        // ascendingDiagonalCheck
        for (int col = 3; col < NUMBER_OF_COLUMNS; col++) {
            for (int row = 0; row < NUMBER_OF_ROWS - 3; row++) {
                if (mGridColumns.get(col).get(row).getOwningPlayerIdNumber() == playerIdNumber &&
                        mGridColumns.get(col - 1).get(row + 1).getOwningPlayerIdNumber() == playerIdNumber &&
                        mGridColumns.get(col - 2).get(row + 2).getOwningPlayerIdNumber() == playerIdNumber &&
                        mGridColumns.get(col - 3).get(row + 3).getOwningPlayerIdNumber() == playerIdNumber) {
                    return playerIdNumber;
                }
            }
        }

        // decendingDiagonalCheck
        for (int col = 3; col < NUMBER_OF_COLUMNS; col++) {
            for (int row = 3; row < NUMBER_OF_ROWS; row++) {
                if (mGridColumns.get(col).get(row).getOwningPlayerIdNumber() == playerIdNumber &&
                        mGridColumns.get(col - 1).get(row - 1).getOwningPlayerIdNumber() == playerIdNumber &&
                        mGridColumns.get(col - 2).get(row - 2).getOwningPlayerIdNumber() == playerIdNumber &&
                        mGridColumns.get(col - 3).get(row - 3).getOwningPlayerIdNumber() == playerIdNumber) {
                    return playerIdNumber;
                }
            }
        }

        // no winner
        return 0;
    }

    // Key constants used to store the state into a bundle
    public static final String CURRENT_PLAYER_SAVED_INSTACE_KEY = "com.connect4.team17.lastplayer.savedinstance";
    public static final String OWNERSHIP_GRID_SAVED_INSTANCE_KEY = "com.connect4.team17.playertwo.savedinstance";
    public static final String LAST_MOVE_POSITION_ROW_INSTANCE_KEY = "com.connect4_team17.lastmoverow.savedinstace";
    public static final String LAST_MOVE_POSITION_COLUMN_INSTANCE_KEY = "com.connect4_team17.lastmovecolumn.savedinstace";
    public static final String CHOSEN_MOVE_POSITION_ROW_INSTANCE_KEY = "com.connect4_team17.chosenmoverow.savedinstace";
    public static final String CHOSEN_MOVE_POSITION_COLUMN_INSTANCE_KEY = "com.connect4_team17.chosenmovecolumn.savedinstace";

    private void loadOwnershipGrid(ArrayList<ArrayList<Integer>> gridOwnershipTable) {
        // restore the grid and disks
        for (int col = 0; col < NUMBER_OF_COLUMNS; col++) {
            for (int row = 0; row < NUMBER_OF_ROWS; row++) {
                // if a player owns this cell
                int ownerId = gridOwnershipTable.get(col).get(row);
                if (ownerId != 0) {
                    mGridColumns.get(col).get(row).setOwningPlayer(
                            ownerId == PLAYER_ONE_ID ? mPlayerOne : mPlayerTwo
                    );
                }
            }
        }
    }

    /**
     * Used to restore the game state.
     *
     * @param savedInstanceState The bundle to restore the state from.
     */
    public void getFromBundle(Bundle savedInstanceState) {

        // set the current player
        mCurrentPlayer = savedInstanceState.getInt(CURRENT_PLAYER_SAVED_INSTACE_KEY)
                == PLAYER_ONE_ID ? mPlayerOne : mPlayerTwo;

        loadOwnershipGrid((ArrayList<ArrayList<Integer>>)
                savedInstanceState.getSerializable(OWNERSHIP_GRID_SAVED_INSTANCE_KEY));

        // begin the players move
        beginPlayerMove();

        // restore the last chosen cell
        if (savedInstanceState.containsKey(LAST_MOVE_POSITION_COLUMN_INSTANCE_KEY)) {
            int lastCellColumn = savedInstanceState.getInt(LAST_MOVE_POSITION_COLUMN_INSTANCE_KEY);
            int lastCellRow = savedInstanceState.getInt(LAST_MOVE_POSITION_ROW_INSTANCE_KEY);
            mLastChosenCell = mGridColumns.get(lastCellColumn).get(lastCellRow);
        }

        // restore the currently selected cell
        if (savedInstanceState.containsKey(CHOSEN_MOVE_POSITION_COLUMN_INSTANCE_KEY)) {
            int chosenCellColumn = savedInstanceState.getInt(CHOSEN_MOVE_POSITION_COLUMN_INSTANCE_KEY);
            int lastCellRow = savedInstanceState.getInt(CHOSEN_MOVE_POSITION_ROW_INSTANCE_KEY);
            mChosenCell = mGridColumns.get(chosenCellColumn).get(lastCellRow);
        }
    }

    private ArrayList<ArrayList<Integer>>  buildOwnerShipGrid() {
        ArrayList<ArrayList<Integer>> gridOwnershipTable = new ArrayList<>();
        for (int col = 0; col < NUMBER_OF_COLUMNS; col++) {
            ArrayList<Integer> columnOwnership = new ArrayList<>();
            for (int row = 0; row < NUMBER_OF_ROWS; row++) {
                Integer cellOwner = mGridColumns.get(col).get(row).getOwningPlayerIdNumber();
                columnOwnership.add(cellOwner);
            }
            gridOwnershipTable.add(columnOwnership);
        }
        return gridOwnershipTable;
    }

    /**
     * Used to store the state of the game.
     *
     * @param outState The bundle to store the state to.
     */
    public void putToBundle(Bundle outState) {
        outState.putSerializable(OWNERSHIP_GRID_SAVED_INSTANCE_KEY, buildOwnerShipGrid());

        outState.putSerializable(CURRENT_PLAYER_SAVED_INSTACE_KEY, mCurrentPlayer.getPlayerId());

        if (mLastChosenCell != null) {
            outState.putInt(LAST_MOVE_POSITION_COLUMN_INSTANCE_KEY, mLastChosenCell.getColumn());
            outState.putInt(LAST_MOVE_POSITION_ROW_INSTANCE_KEY, mLastChosenCell.getRow());
        }

        if (mChosenCell != null) {
            outState.putInt(CHOSEN_MOVE_POSITION_COLUMN_INSTANCE_KEY, mChosenCell.getColumn());
            outState.putInt(CHOSEN_MOVE_POSITION_ROW_INSTANCE_KEY, mChosenCell.getRow());
        }
    }

    /**
     * Checks for a full game resulting in a tie
     *
     * @return true for a full board resulting in a tie, and false if the board isn't full
     */
    public boolean isThereATie(){
        // Loop through every tile and see if it's owned by a player or not
        for (int col = 0; col < NUMBER_OF_COLUMNS; col++) {
            // If the top of a column is not owned, the board is not full
            if (!mGridColumns.get(col).get(0).isOwnedByPlayer()) {
                return false;
            }
        }

        // Tie Game
        return true;
    }

    public String toJsonString() {
        JsonObject json = new JsonObject();

        // save the players
        json.add(PLAYER_ONE_JSON_KEY, gson.toJsonTree(mPlayerOne));
        json.add(PLAYER_TWO_JSON_KEY, gson.toJsonTree(mPlayerTwo));
        json.add(CURRENT_PLAYER_JSON_KEY, gson.toJsonTree(mCurrentPlayer));
        // and game elements
        json.add(OWNERSHIP_TABLE_JSON_KEY, gson.toJsonTree(buildOwnerShipGrid()));
        json.add(LAST_CHOSEN_CELL_JSON_KEY, gson.toJsonTree(mLastChosenCell));
        json.add(CHOSEN_CELL_JSON_KEY, gson.toJsonTree(mChosenCell));

        return json.toString();
    }

    public void loadFromJson(String json) {
        gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        mPlayerOne = gson.fromJson(jsonObject.get(PLAYER_ONE_JSON_KEY).toString(), ConnectFourPlayer.class);
        mPlayerTwo = gson.fromJson(jsonObject.get(PLAYER_TWO_JSON_KEY).toString(), ConnectFourPlayer.class);
        mCurrentPlayer = gson.fromJson(jsonObject.get(CURRENT_PLAYER_JSON_KEY).toString(), ConnectFourPlayer.class);
        loadOwnershipGrid( (ArrayList<ArrayList<Integer>>) gson.fromJson(
                jsonObject.get(OWNERSHIP_TABLE_JSON_KEY).toString(), new TypeToken<ArrayList<ArrayList<Integer>>>(){}.getType()));
        if (jsonObject.has(CHOSEN_CELL_JSON_KEY)) {
            mChosenCell = gson.fromJson(jsonObject.get(CHOSEN_CELL_JSON_KEY).toString(), ConnectFourGameCell.class);
        }
        if (jsonObject.has(LAST_CHOSEN_CELL_JSON_KEY)) {
            mLastChosenCell = gson.fromJson(jsonObject.get(LAST_CHOSEN_CELL_JSON_KEY).toString(), ConnectFourGameCell.class);
        }
    }

    public String getCurrentPlayerUid() {
        if (mCurrentPlayer != null) {
            return mCurrentPlayer.getPlayerUid();
        }
        return null;
    }
}
