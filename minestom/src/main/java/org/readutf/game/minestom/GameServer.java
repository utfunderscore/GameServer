package org.readutf.game.minestom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.readutf.game.GameStarter;
import org.readutf.tnttag.TagGameStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer {

    private final Logger logger = LoggerFactory.getLogger(GameServer.class);

    public GameServer() {
        MinecraftServer server = MinecraftServer.init();

        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkSupplier(LightingChunk::new);
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));

        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkRange.chunksInRange(0, 0, 32, (x, z) -> chunks.add(instance.loadChunk(x, z)));

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            LightingChunk.relight(instance, instance.getChunks());
        });

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
        });

        GameStarter starter = new TagGameStarter();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().teleport(new Pos(0, 45, 0));

            Collection<@NotNull Player> onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (onlinePlayers.size() == starter.getNeededPlayers()) {
                try {
                    starter.startGame(onlinePlayers.stream().map(Entity::getUuid).toList());
                } catch (Exception e) {
                    logger.error("Failed to start game", e);
                }
            }
        });


        server.start("0.0.0.0", 25565);
    }

    public static void main(String[] args) {
        new GameServer();
    }


}
