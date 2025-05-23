package org.readutf.game.minestom;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.readutf.buildformat.common.BuildManager;
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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {

    private final Logger logger = LoggerFactory.getLogger(GameServer.class);

    public GameServer() {
        MinecraftServer server = MinecraftServer.init();

        setupLimbo();

        HikariDataSource database = getDatabase("185.227.70.59", 5432, "builds", "readutf",
                "w4vA9mtVoC79eUoWKrsv0XycHExRiYRWTzrzQgwc65CP3g2GBgPOY2o9WXRQaZq8");

        S3Client s3Client = getAwsClient("xi0jokcSt6DdWvCsKq0Z", "CIq3mrQPEhugrUJH07sPpftiuCWvl7BuQtJXLX", "https://s3.utf.lol");

        PostgresDatabaseManager databaseManager = new PostgresDatabaseManager(database);
        BuildMetaStore metaStore = new PostgresMetaStore(databaseManager);
        BuildSchematicStore schematicStore = new S3BuildSchematicStore(s3Client, "builds");

        BuildManager buildManager = new BuildManager(metaStore, schematicStore);

        ArenaManager<Instance> arenaManager = new ArenaManager<>(buildManager, MinestomArenaPlatform.getInstance(), new AtomicInteger());

        GameStarter starter = new TagGameStarter(arenaManager);

        logger.info("Starting Minestom Server");

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().teleport(new Pos(0, 45, 0));

            Collection<@NotNull Player> onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (onlinePlayers.size() == starter.getNeededPlayers()) {
                try {
                    logger.info("Starting the game...");
                    starter.startGame(onlinePlayers.stream().map(Entity::getUuid).toList());
                } catch (Exception e) {
                    logger.error("Failed to start game", e);
                }
            }
        });


        server.start("0.0.0.0", 25565);
    }

    private static void setupLimbo() {
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
    }

    public static void main(String[] args) {
        new GameServer();
    }

    private @NotNull HikariDataSource getDatabase(@NotNull String host, int port, @NotNull String database, @NotNull String user, @NotNull String password) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:postgresql://%s:%s/%s".formatted(host, port, database));
        if (!password.isEmpty() && !user.isEmpty()) {
            hikariConfig.setPassword(password);
            hikariConfig.setUsername(user);
        }
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setMaximumPoolSize(16);
        return new HikariDataSource(hikariConfig);
    }

    public S3Client getAwsClient(@NotNull String accessKey, @NotNull String secretKey, @NotNull String endpoint) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3Configuration serviceConfiguration = S3Configuration.builder().pathStyleAccessEnabled(true).build();

        return S3Client.builder().endpointOverride(URI.create(endpoint)).credentialsProvider(StaticCredentialsProvider.create(credentials)).region(Region.of("auto")).serviceConfiguration(serviceConfiguration).build();
    }


}
