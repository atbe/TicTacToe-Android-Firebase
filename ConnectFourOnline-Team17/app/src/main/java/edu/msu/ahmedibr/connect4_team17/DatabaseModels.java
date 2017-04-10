package edu.msu.ahmedibr.connect4_team17;

/**
 * Created by abe on 4/9/17.
 */

public class DatabaseModels {
    /**
     * Used to mode a game pool object
     */
    public static class Game {

        public enum State {OPEN, JOINED, STARTED}

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
    public static class User {
        private String mDisplayName;
        private String mId;

        public String getDisplayName() {
            return mDisplayName;
        }

        public void setDisplayName(String mDisplayName) {
            this.mDisplayName = mDisplayName;
        }

        public String getId() {
            return mId;
        }

        public void setId(String mId) {
            this.mId = mId;
        }

        public User() {
        }

        public User(String displayName, String id) {
            mDisplayName = displayName;
            mId = id;
        }
    }
}
