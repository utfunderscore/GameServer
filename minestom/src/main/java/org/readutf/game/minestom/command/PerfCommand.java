package org.readutf.game.minestom.command;

import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PerfCommand extends Command {

    public PerfCommand() {
        super("perf", "performance", "tps", "lag");

        addSyntax((sender, context) -> {

        });
    }
}
