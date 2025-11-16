package io.github.nekosora;

import io.github.nekosora.api.file.widgets.FileSwitch;
import io.github.nekosora.api.story.node.NodeIDs;
import io.github.nekosora.api.story.node.StoryNodeRegistry;
import io.github.nekosora.settings.GameSettings;
import io.github.nekosora.utils.CrashUtils;
import io.github.nekosora.utils.GameMenuUtils;
import io.github.nekosora.utils.GameUtils;
import io.github.nekosora.utils.StoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            log.info("FileXE is starting...");

            if (!GameMenuUtils.initializeGameMenu(GameSettings.mainDir)) return;
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(GameSettings.mainDir);
            }

            FileSwitch exitGame = new FileSwitch((b) -> {
                if (b) {
                    GameUtils.exitGame(GameUtils.ExitReason.exitGameFileDetected);
                } else {
                    log.error("cond error");
                }
            }, new File(GameSettings.mainDir, "ExitGame.off"));
            exitGame.register();

            FileSwitch singleplayer = new FileSwitch((b) -> {
                if (b) {
                    GameMenuUtils.cleanupGameMenu(GameSettings.mainDir);
                    StoryNodeRegistry.ROOT.execute(); // 开始单人剧情
                } else {
                    log.error("cond error");
                }
            }, new File(new File(GameSettings.mainDir, "Play"), "Singleplayer.off"));
            singleplayer.register();

            StoryUtils.init();

            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            CrashUtils.crash(e);
        }
    }
}
