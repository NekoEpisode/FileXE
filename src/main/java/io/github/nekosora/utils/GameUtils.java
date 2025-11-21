package io.github.nekosora.utils;

import io.github.nekosora.api.achievement.AchievementManager;
import io.github.nekosora.api.file.widgets.FileButton;
import io.github.nekosora.api.sound.SoundEngine;
import io.github.nekosora.context.GameContext;
import io.github.nekosora.settings.GameSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GameUtils {
    private static final Logger log = LoggerFactory.getLogger(GameUtils.class);

    public static void exitGame(ExitReason exitReason) {
        try {
            GameContext.isExiting = true;
            log.info("Exiting game... Reason: " + exitReason.name());

            if (exitReason == ExitReason.exitGameFileDetected) {
                File exitGame = new File(GameSettings.mainDir, "GameExit.on");
                if (exitGame.exists()) {
                    if (!exitGame.delete()) {
                        log.error("Could not delete {}", exitGame.getAbsolutePath());
                    }
                }
            }

            try {
                GameMenuUtils.cleanupGameMenu(GameSettings.mainDir);
                GameSettings.mainDir.delete();
            } catch (Exception e) {
                log.error("Error on exiting game: {}", String.valueOf(e));
            }

            try {
                FileButton.shutdown();
            } catch (Exception e) {
                log.error("Error on exiting game: {}", String.valueOf(e));
            }

            try {
                SoundEngine.shutdown();
            } catch (Exception e) {
                log.error("Error on exiting game: {}", String.valueOf(e));
            }

            try {
                AchievementManager.save(GameSettings.achievementSaveFile);
            } catch (Exception e) {
                log.error("Error on exiting game: {}", String.valueOf(e));
            }

            log.info("Bye!");
            System.exit(0);
        } catch (Exception e) {
            log.error("Error on exiting game: {}", String.valueOf(e));
            System.exit(0);
        }
    }

    public enum ExitReason {
        exitGameFileDetected,
        crashed
    }
}
