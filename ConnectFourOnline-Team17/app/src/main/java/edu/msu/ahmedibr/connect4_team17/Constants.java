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

    public static final String CREATOR_DATA_KEY = "creator";
    public static final String JOINER_DATA_KEY = "joiner";

    // user attribute keys
    public static final String USER_ID_KEY = "id";
    public static final String USER_DISPLAYNAME_KEY = "displayName";

    // game attribute keys
    public static final String GAME_POOL_STATE_KEY = "state";
    public static final String GAME_GAME_DUMP_KEY = "game";

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
    public static final String CHOSEN_CELL_JSON_KEY = "com.connectfour.chosen_cell.json.key";



}
