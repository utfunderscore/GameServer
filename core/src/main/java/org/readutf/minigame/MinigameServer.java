package org.readutf.minigame;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.readutf.minigame.production.GameDiscovery;

import java.util.List;
import java.util.UUID;

public abstract class MinigameServer {

    @NotNull
    private final String playlist;

    public MinigameServer(String playlist, boolean production) {
        this.playlist = playlist;
        if(production) {
            new GameDiscovery(this);
        } else {
            throw new UnsupportedOperationException("Production mode is required for MinigameServer.");
        }

        MinecraftServer server = MinecraftServer.init();

    }

    public abstract UUID start(List<List<UUID>> teams) throws Exception;

    public abstract void cancel(UUID gameId);

    public abstract float getCapacity();

    public @NotNull String getPlaylist() {
        return playlist;
    }
}
