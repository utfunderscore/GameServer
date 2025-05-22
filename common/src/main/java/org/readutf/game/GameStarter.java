package org.readutf.game;

import java.util.List;
import java.util.UUID;

public abstract class GameStarter {

    private final int neededPlayers;

    public GameStarter(int neededPlayers) {
        this.neededPlayers = neededPlayers;
    }

    public abstract void startGame(List<UUID> players) throws Exception;

    public int getNeededPlayers() {
        return neededPlayers;
    }
}
