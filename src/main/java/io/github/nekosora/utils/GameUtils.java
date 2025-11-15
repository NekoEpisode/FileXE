package io.github.nekosora.utils;

import io.github.nekosora.context.GameContext;
import io.github.nekosora.settings.GameSettings;

import java.io.File;

public class GameUtils {
    public static void exitGame(ExitReason exitReason) {
        GameContext.isExiting = true;
        System.out.println("Exiting game...");

        if (exitReason == ExitReason.exitGameFileDetected) {
            File exitGame = new File(GameSettings.mainDir, "GameExit.on");
            if (exitGame.exists()) {
                if (!exitGame.delete()) {
                    System.err.println("Could not delete " + exitGame.getAbsolutePath());
                }
            }
        }

        GameFileUtils.cleanupMarkedFiles();

        System.out.println("Bye!");
        System.exit(0);
    }

    public enum ExitReason {
        exitGameFileDetected,
        crashed
    }
}
