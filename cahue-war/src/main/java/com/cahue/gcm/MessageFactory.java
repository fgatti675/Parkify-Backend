package com.cahue.gcm;

import com.google.android.gcm.server.Message;

/**
 * @author Francesco
 */
public class MessageFactory {

    /**
     * This message is sent to users that have been invited to new games
     */
    public static final String GAME_INVITATION = "GAME_INVITATION";

    /**
     * This message is sent when a user has joined a game, to the other players.
     */
    public static final String GAME_UPDATE = "GAME_UPDATE";

    /**
     * Message parameters
     */
    public static final String GAME_ID = "GAME_ID";
    public static final String HOST_ID = "HOST_ID";
    public static final String USER_ID = "USER_ID";

    /**
     * Tell a user that they have been invited to a game and by who.
     * @return
     */
    public static Message getSayHelloMessage() {
        return new Message.Builder()
//                .collapseKey(GAME_INVITATION)
                .addData("TEST_KEY", "Hi from your really cool server")
                .build();
    }

}
