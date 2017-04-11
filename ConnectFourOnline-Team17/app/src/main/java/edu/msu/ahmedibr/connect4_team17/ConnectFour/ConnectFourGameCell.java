package edu.msu.ahmedibr.connect4_team17.ConnectFour;

/**
 * Created by abe on 2/21/17.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import edu.msu.ahmedibr.connect4_team17.Activities.GameActivity;
import edu.msu.ahmedibr.connect4_team17.R;

/**
 * This class represents one piece of our puzzle.
 */
public class ConnectFourGameCell {
    /**
     * The image for the actual piece.
     */
    transient private static Bitmap emptyCellImage;

    // TODO: Making this boy static would save some memory but would need one for green and one for white
    transient private Bitmap mDiskImage;

    /**
     * Row the cell is in.
     */
    private int mRow = 0;

    /**
     * Column the cell is in.
     */
    private int mColumn = 0;

    public int getRow() {
        return mRow;
    }

    public int getColumn() {
        return mColumn;
    }

    /**
     * x location when the puzzle is solved
     */
    private float finalX;

    /**
     * y location when the puzzle is solved
     */
    private float finalY;

    /**
     * Reference to the player who owns this disk
     */
    private ConnectFourPlayer mOwningPlayer = null;

    transient private GameActivity mContext;

    /**
     * Gets the id of the player who owns this cell.
     *
     * @return 1 or 2 if owned by player and 0 if not owned by player.
     */
    public int getOwningPlayerIdNumber() {
        if (mOwningPlayer == null) {
            // 0 to indicate the cell is not owned
            return 0;
        }

        return mOwningPlayer.getPlayerId();
    }

    /**
     * Setter for the owning player.
     * mOwningPlayer MUST be null when this is called. Throws exception otherwise.
     *
     * @param player The player who owns this disk.
     */
    public void setOwningPlayer(ConnectFourPlayer player) {
        // safety check, this function should only be called on free cells
        assert mOwningPlayer == null;
        mOwningPlayer = player;

        // Load the disk image from the players id
        // TODO: Maybe loading the bitmap once in the game class would be more resource efficient??
        mDiskImage = BitmapFactory.decodeResource(mContext.getResources(), mOwningPlayer.getDiskImageResourceId());
    }

    /**
     * Checks the association between the player and the disk.
     *
     * @return True if a player owns this disk, false otherwise.
     */
    public boolean isOwnedByPlayer() {
        return mOwningPlayer != null;
    }

    public static float EMPTY_CELL_HEIGHT;
    public static float EMPTY_CELL_WIDTH;

    /**
     * Constructor for a grid cell.
     *
     * @param context The context of the activity holding the game.
     * @param finalX The x position of the cell.
     * @param finalY The y position of the cell.
     * @param row The row the cell lives in. 0 is top row.
     * @param column The col the cell lives in. 0 is the far left column.
     */
    public ConnectFourGameCell(GameActivity context, float finalX, float finalY, int row, int column) {
        this.finalX = finalX;
        this.finalY = finalY;
        this.mRow = row;
        this.mColumn = column;
        this.mContext = context;

        // one time initialization of image when first ConnectFourGameCell object is created
        if (ConnectFourGameCell.emptyCellImage == null) {
            ConnectFourGameCell.emptyCellImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.grid_empty_space);
        }

        EMPTY_CELL_WIDTH = emptyCellImage.getWidth();
        EMPTY_CELL_HEIGHT = emptyCellImage.getHeight();
    }

    /** Draws the game cell
     *
     * @param canvas: canvas we're drawing on
     * @param marginX: X Margin
     * @param marginY: Y Margin
     * @param gridSize: Size of the grid
     * @param scaleFactor: Scale factor of the screen
     */
    public void draw(Canvas canvas, int marginX, int marginY,
                     int gridSize, float scaleFactor) {
        canvas.save();

        // Convert x,y to pixels and add the margin, then draw
        canvas.translate(marginX + finalX * gridSize, marginY + finalY * gridSize);

        // Scale it to the right size
        canvas.scale(scaleFactor, scaleFactor);

        // This magic code makes the center of the piece at 0, 0
        canvas.translate(-emptyCellImage.getWidth() / 2, -emptyCellImage.getHeight() / 2);

        // Draw the bitmap
        canvas.drawBitmap(emptyCellImage, 0, 0, null);

        if (mOwningPlayer != null) {
            // Draw the bitmap
            canvas.drawBitmap(mDiskImage, 0, 0, null);
        }

        canvas.restore();
    }

    /**
     * Test to see if we have touched a puzzle piece
     * @param testX X location as a normalized coordinate (0 to 1)
     * @param testY Y location as a normalized coordinate (0 to 1)
     * @param puzzleSize the size of the puzzle in pixels
     * @param scaleFactor the amount to scale a piece by
     * @return true if we hit the piece
     */
    public boolean hit(float testX, float testY,
                       int puzzleSize, float scaleFactor) {

        // Make relative to the location and size to the piece size
        int pX = (int)((testX - finalX) * puzzleSize / scaleFactor) +
                emptyCellImage.getWidth() / 2;
        int pY = (int)((testY - finalY) * (puzzleSize) / scaleFactor) +
                emptyCellImage.getHeight() / 2;

        if(pX < 0 || pX >= emptyCellImage.getWidth() ||
                pY < 0 || pY >= emptyCellImage.getHeight()) {
            return false;
        }

        // We are within the rectangle of the piece.
        // Are we touching actual picture?
        return (emptyCellImage.getPixel(pX, pY)) != 0;
    }
}
