package edu.msu.ahmedibr.connect4_team17;

/**
 * Created by abe on 4/9/17.
 */

public class Constants {

    /**
     * Database key constants
     */
    // root nodes (tables)
    public static final String GAMES_DATABASE_ROOT_KEY = "games";
    public static final String ARCHIVED_DATABASE_ROOT_KEY = "archived_games";

    // user root nodes (creator or joiner) for two player game
    public static final String CREATOR_DATA_KEY = "creator";
    public static final String JOINER_DATA_KEY = "joiner";

    // user attribute keys
    public static final String USER_ID_KEY = "id";
    public static final String USER_DISPLAYNAME_KEY = "displayName";
    public static final String IS_WINNER_KEY = "isWinner";

    // game attribute keys
    public static final String GAME_POOL_STATE_KEY = "state";
    public static final String GAME_STATE_JSON_DUMP_KEY = "game";

    public static final String FAKE_EMAIL_DOMAIN_URL = "@tictactoefor476appyo.com";

    /**
     * Bundle keys
     */
    public static final String CURRENT_GAME_BUNDLE_KEY = "com.connectfour.currentgame.id.bundle";
    public static final String AM_CREATOR_BUNDLE_KEY = "com.connectfour.amcreator.id.bundle";
    public static final String PLAYER_ONE_DISPLAYNAME_BUNDLE_KEY = "com.connectfour.playerone_displayname.bundle";
    public static final String PLAYER_ONE_UID_BUNDLE_KEY = "com.connectfour.playerone_uid.bundle";
    public static final String PLAYER_TWO_DISPLAYNAME_BUNDLE_KEY = "com.connectfour.playertwo_displayname.bundle";
    public static final String PLAYER_TWO_UID_BUNDLE_KEY = "com.connectfour.playertwo_uid.bundle";

    /**
     * json dump keys
     */
    public static final String PLAYER_ONE_JSON_KEY = "com.connectfour.player_one.json.key";
    public static final String PLAYER_TWO_JSON_KEY = "com.connectfour.player_two.json.key";
    public static final String OWNERSHIP_TABLE_JSON_KEY = "com.connectfour.ownership.json.key";
    public static final String CURRENT_PLAYER_JSON_KEY = "com.connectfour.current_player.json.key";
    public static final String LAST_CHOSEN_CELL_JSON_KEY = "com.connectfour.last_chosen_cell.json.key";
    public static final String WINNING_PLAYER_JSON_KEY = "com.connectfour.winning_player.json.key";

    /**
     * Exception strings
     */
    public static final String WEAK_PASSWORD_EXCEPTION_FIREBASE = "WEAK_PASSWORD";
    public static final String EMAIL_BADLY_FORMATTED_EXCEPTION_FIREBASE = "The email address is badly formatted.";

    /**
     * Logging tags
     */
    public static final String LOGIN_STATUS_CHANGED_TAG = "LOGIN_STATUS_CHANGED";
    public static final String AUTH_FAILED_TAG = "AUTH_FAILED";
    public static final String CREATE_ACCOUNT = "CREATE_ACCOUNT";
    public static final String AUTH_STATUS_TAG = "AUTH_STATUS";
    public static final String CONNECTIVITY_BROADCAST_TAG = "CONNECTIVITY_BROADCAST";

    /**
     * Messages
     */
    public static final String JOINING_GAME_MESSAGE = "Joining the game...";
    public static final String WAITING_FOR_CREATOR_MESSAGE = "Joined successfully, waiting for %s to start the game.";
    public static final String ALREADY_JOINED_CANT_CREATE_GAME = "You've already joined a game, we're waiting for %s to start the game.";
}
