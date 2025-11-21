package io.github.nekosora;

import io.github.nekosora.api.achievement.Achievement;
import io.github.nekosora.api.achievement.AchievementAttribute;
import io.github.nekosora.api.achievement.AchievementFile;
import io.github.nekosora.api.achievement.AchievementManager;
import io.github.nekosora.api.file.widgets.FileSwitch;
import io.github.nekosora.api.sound.Sound;
import io.github.nekosora.api.sound.SoundEngine;
import io.github.nekosora.api.story.node.NodeIDs;
import io.github.nekosora.api.story.node.StoryNode;
import io.github.nekosora.api.story.node.StoryNodeRegistry;
import io.github.nekosora.settings.GameSettings;
import io.github.nekosora.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            log.info("FileXE is starting...");

            AchievementManager.registerAchievement(new Achievement(Namespace.fromString("test:hello"), "TestingHello", "Hello World!", AchievementAttribute.Rare));
            AchievementManager.registerAchievement(new Achievement(Namespace.fromString("test:test1"), "TestingHello", "Hello World!", AchievementAttribute.Hidden));

            AchievementManager.load(GameSettings.achievementSaveFile);
            System.out.println(AchievementManager.getCompletedAchievements());

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

            AchievementManager.achievementComplete(Namespace.fromString("test:test1"));

            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            CrashUtils.crash(e);
        }
    }
}
