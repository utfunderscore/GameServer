package org.readutf.tnttag.stages.fighting.tasks;

import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.readutf.engine.GameException;
import org.readutf.engine.minestom.schedular.CountdownTask;
import org.readutf.tnttag.stages.fighting.FightingStage;
import org.readutf.tnttag.systems.TntHolderManager;

@Slf4j
public class ExplosionTask extends CountdownTask {

    private final TntHolderManager tntHolderManager;

    public ExplosionTask(TntHolderManager tntHolderManager) {
        super(FightingStage.EXPLOSION_DURATION, IntStream.range(0, 15).boxed().toList());
        this.tntHolderManager = tntHolderManager;
    }

    @Override
    public void handleInterval(int interval) {
        if (interval == 0) {
            log.info("Explosion task finished.");
            try {
                tntHolderManager.explode();
            } catch (GameException e) {
                log.error("", e);
            }
        }
    }

}
