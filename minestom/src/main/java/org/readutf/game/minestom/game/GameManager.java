package org.readutf.game.minestom.game;

import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.common.meta.BuildMetaStore;
import org.readutf.buildformat.common.schematic.BuildSchematicStore;
import org.readutf.buildformat.s3.S3BuildSchematicStore;
import org.readutf.buildstore.PostgresDatabaseManager;
import org.readutf.buildstore.PostgresMetaStore;
import org.readutf.engine.arena.ArenaManager;
import org.readutf.engine.minestom.arena.MinestomArenaPlatform;
import org.readutf.game.GameStarter;
import org.readutf.tnttag.TagGameStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;


public class GameManager {

    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);

    private @NotNull final ArenaManager<Instance> arenaManager;

    public GameManager(S3Client s3Client, HikariDataSource database) {
        @NotNull PostgresDatabaseManager databaseManager = new PostgresDatabaseManager(database);
        @NotNull BuildMetaStore metaStore = new PostgresMetaStore(databaseManager);
        @NotNull BuildSchematicStore schematicStore = new S3BuildSchematicStore(s3Client, "builds");
        File cacheDirectory = new File("arena-cache");
        cacheDirectory.mkdirs();
        @NotNull MinestomArenaPlatform minestomArenaPlatform = new MinestomArenaPlatform(schematicStore, cacheDirectory);
        this.arenaManager = new ArenaManager<>(metaStore, minestomArenaPlatform, new AtomicInteger());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            if(!event.isFirstSpawn()) return;

            event.getPlayer().teleport(new Pos(0, 45, 0));

            Collection<@NotNull Player> onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (onlinePlayers.size() == 2) {
                try {
                    logger.info("Starting the game...");
                    new Thread(() -> {
                        try {
                            start(onlinePlayers.stream().map(Entity::getUuid).toList());
                        } catch (Exception e) {
                            logger.error("Failed to start game", e);
                        }
                    }).start();
                } catch (Exception e) {
                    logger.error("Failed to start game", e);
                }
            }
        });
    }

    public void start(List<UUID> players) throws Exception {
        GameStarter gameStarter = new TagGameStarter(arenaManager);

        gameStarter.startGame(players);
    }
}
