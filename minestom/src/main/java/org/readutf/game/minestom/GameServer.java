package org.readutf.game.minestom;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.togar2.pvp.MinestomPvP;
import io.github.togar2.pvp.feature.CombatFeatureSet;
import io.github.togar2.pvp.feature.CombatFeatures;

import java.nio.file.Path;

import me.lucko.spark.minestom.SparkMinestom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.readutf.game.minestom.game.GameManager;
import org.readutf.tnttag.commands.CommandHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class GameServer {

    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);

    public GameServer() {
        MinecraftServer server = MinecraftServer.init();

        setupLimbo();
        startSpark();

        MinestomPvP.init();
        CombatFeatureSet featureSet = CombatFeatures.modernVanilla();
        MinecraftServer.getGlobalEventHandler().addChild(featureSet.createNode());

        for (Command command : CommandHelper.commands) {
            MinecraftServer.getCommandManager().register(command);
        }

        String dbHost = System.getenv("DATABASE_HOST");
        String dbPort = System.getenv("DATABASE_PORT");
        String dbName = System.getenv("DATABASE_NAME");
        String dbUser = System.getenv("DATABASE_USER");
        String dbPassword = System.getenv("DATABASE_PASSWORD");

        HikariDataSource database = getDatabase(
                dbHost != null ? dbHost : "localhost",
                dbPort != null ? Integer.parseInt(dbPort) : 5432,
                dbName != null ? dbName : "minestom",
                dbUser != null ? dbUser : "postgres",
                dbPassword != null ? dbPassword : ""
        );

        S3Client s3Client = getAwsClient(
                System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY"),
                System.getenv("AWS_ENDPOINT") != null ? System.getenv("AWS_ENDPOINT") : "http://localhost:9000"
        );

        GameManager gameManager = new GameManager(s3Client, database);

        MinecraftServer.getCommandManager().setUnknownCommandCallback((sender, command) -> {
            sender.sendMessage(Component.text("Unknown command.", NamedTextColor.RED));
        });


        logger.info("Starting Minestom Server");


        server.start("0.0.0.0", 25565);
    }

    private static void startSpark() {
        Path directory = Path.of("spark");
        SparkMinestom spark = SparkMinestom.builder(directory)
                .commands(true) // enables registration of Spark commands
                .permissionHandler((sender, permission) -> true) // allows all command senders to execute all commands
                .enable();
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
