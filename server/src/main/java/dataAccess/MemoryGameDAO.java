package dataAccess;

import chess.ChessGame;

import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{

    final private HashMap<Integer, ChessGame> games = new HashMap<>();

    @Override
    public void clear() {
        games.clear();
    }

    @Override
    public void getGame() {

    }

    @Override
    public void listGames() {

    }

    @Override
    public void updateGame() {

    }
}
