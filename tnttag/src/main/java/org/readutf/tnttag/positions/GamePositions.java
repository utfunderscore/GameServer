package org.readutf.tnttag.positions;

import java.util.List;
import org.readutf.buildformat.common.format.BuildFormat;
import org.readutf.buildformat.common.format.requirements.Requirement;
import org.readutf.buildformat.common.markers.Position;

public record GamePositions(
        @Requirement(startsWith = "spawn") List<Position> positions
) implements BuildFormat {
}
