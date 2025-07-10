package org.readutf.tnttag;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.GameException;
import org.readutf.engine.event.GameEventManager;
import org.readutf.engine.minestom.MinestomPlatform;
import org.readutf.engine.minestom.event.MinestomEventPlatform;
import org.readutf.engine.minestom.schedular.MinestomSchedular;
import org.readutf.engine.task.GameScheduler;
import org.readutf.engine.team.GameTeam;
import org.readutf.minigame.MinigameServer;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class TagGameServer extends MinigameServer {

    @NotNull
    private final MinestomPlatform platform;

    @NotNull
    private final GameScheduler scheduler;

    @NotNull
    private final GameEventManager eventManager;

    public TagGameServer(boolean production) {
        super("tnttag", production);
        this.platform = new MinestomPlatform();
        this.scheduler = new GameScheduler(new MinestomSchedular());
        this.eventManager = new GameEventManager(new MinestomEventPlatform());
    }

    @Override
    public UUID start(List<List<UUID>> teams) throws GameException {

        List<GameTeam> gameTeams = IntStream.range(0, teams.size()).mapToObj(i -> new GameTeam(String.valueOf(i))).toList();

        TagGame game = new TagGame(platform, scheduler, eventManager, playerId -> {
            List<UUID> team = teams.stream().filter(uuids -> uuids.contains(playerId)).findFirst().orElseThrow();
            return gameTeams.get(teams.indexOf(team));
        });

        game.start();

        return game.getId();
    }

    @Override
    public void cancel(UUID gameId) {

    }

    @Override
    public float getCapacity() {
        return 0;
    }

    public static void main(String[] args) {
        new TagGameServer(true);
    }
}
