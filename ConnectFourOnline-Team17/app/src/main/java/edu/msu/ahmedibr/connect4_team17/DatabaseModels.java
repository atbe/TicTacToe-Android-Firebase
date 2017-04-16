package edu.msu.ahmedibr.connect4_team17;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by abe on 4/9/17.
 */

public class DatabaseModels {
    /**
     * Used to mode a game pool object
     */
    @IgnoreExtraProperties
    public static class Game {

        public enum State {OPEN, JOINED, STARTED, ENDED}

        private int mState;

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

    /**
     * Used to model a user in a game (in the eyes of the database)
     */
    @IgnoreExtraProperties
    public static class User {
        private String displayName;
        private String id;
        /**
         * When isWinner is null, either the game has not ended or the user is yet to hear of the win.
         */
        private Boolean isWinner = null;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String mDisplayName) {
            this.displayName = mDisplayName;
        }

        public String getId() {
            return id;
        }

        public void setId(String mId) {
            this.id = mId;
        }

        public Boolean getIsWinner() { return isWinner; }

        public void setIsWinner(Boolean flag) { isWinner = flag; }

        public User() {
        }

        public User(String displayName, String id, Boolean isWinner) {
            this.displayName = displayName;
            this.id = id;
            this.isWinner = isWinner;
        }
    }
}
