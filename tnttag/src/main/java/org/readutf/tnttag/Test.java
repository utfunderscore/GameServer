package org.readutf.tnttag;

import java.io.File;
import java.io.IOException;
import org.readutf.buildformat.common.exception.BuildFormatException;
import org.readutf.buildformat.common.format.BuildFormatManager;
import org.readutf.tnttag.positions.GamePositions;

public class Test {

    public static void main(String[] args) throws BuildFormatException, IOException {
        BuildFormatManager.save(new File(System.getProperty("user.dir")), "tnttag", BuildFormatManager.getValidators(GamePositions.class));
    }

}
