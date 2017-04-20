package edu.msu.ahmedibr.connect4_team17.ConnectFour;

/**
 * Created by abe on 3/5/17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Class used to model a player in the game
 */
public class ConnectFourPlayer {
    /**
     * Name of the player
     */
    private String mName;

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    /**
     * Resource id of the disk belonging to the player.
     */
    private int mDiskImageResourceId;

    public int getDiskImageResourceId() {
        return mDiskImageResourceId;
    }

    /**
     * Used to uniquely identify the player. Either 1 or 2.
     */
    private int mPlayerId;

    public int getPlayerId() {
        return mPlayerId;
    }

    private String mPlayerUid;

    /**
     * Constructor of a ConnectFourPlayer.
     *
     * @param mName Name of the player.
     * @param diskImageResourceId The resourceid of the disk used for this player.
     * @param playerId The unique id of the player.
     */
    public ConnectFourPlayer(String mName, int diskImageResourceId, int playerId, String uid, Context context) {
        this.mName = mName;
        this.mDiskImageResourceId = diskImageResourceId;
        this.mPlayerId = playerId;
        this.mPlayerUid = uid;
    }

    public String getPlayerUid() {
        return mPlayerUid;
    }

    public void setPlayerUid(String mPlayerUid) {
        this.mPlayerUid = mPlayerUid;
    }
}
