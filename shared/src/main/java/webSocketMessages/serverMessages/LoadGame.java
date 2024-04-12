package webSocketMessages.serverMessages;

import chess.ChessGame;

public class LoadGame extends ServerMessage{
    public LoadGame(ServerMessageType type, String message, ChessGame game) {
        super(type);
        this.message = message;
        this.game = game;
    }

    private String message;
    private ChessGame game;
}
