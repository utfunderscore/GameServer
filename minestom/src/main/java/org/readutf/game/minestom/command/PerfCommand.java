package org.readutf.game.minestom.command;

import net.minestom.server.command.builder.Command;

public class PerfCommand extends Command {

    public PerfCommand() {
        super("perf", "performance", "tps", "lag");

        addSyntax((sender, context) -> {

        });
    }
}
