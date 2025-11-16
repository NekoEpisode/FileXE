package io.github.nekosora.utils;

import io.github.nekosora.context.GameContext;
import io.github.nekosora.settings.GameSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GameUtils {
    private static final Logger log = LoggerFactory.getLogger(GameUtils.class);

    public static void exitGame(ExitReason exitReason) {
        GameContext.isExiting = true;
        log.info("Exiting game...");

        if (exitReason == ExitReason.exitGameFileDetected) {
            File exitGame = new File(GameSettings.mainDir, "GameExit.on");
            if (exitGame.exists()) {
                if (!exitGame.delete()) {
                    log.error("Could not delete {}", exitGame.getAbsolutePath());
                }
            }
        }

        GameFileUtils.cleanupMarkedFiles();

        log.info("Bye!");
        System.exit(0);
    }

    public enum ExitReason {
        exitGameFileDetected,
        crashed
    }
}
