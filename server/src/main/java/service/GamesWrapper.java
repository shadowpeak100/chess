package service;

import model.GameData;
import java.util.ArrayList;
import java.util.List;

public class GamesWrapper{
    public ArrayList<GameData> games;

    public GamesWrapper(ArrayList<GameData> games){
        this.games = games;
    }

    public List<GameData> getGames() {
        return games;
    }
}
