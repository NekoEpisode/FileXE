package io.github.nekosora;

import io.github.nekosora.api.file.FileEventCatcher;
import io.github.nekosora.api.file.FileEventType;
import io.github.nekosora.settings.GameSettings;
import io.github.nekosora.utils.CrashUtils;
import io.github.nekosora.utils.GameFileUtils;
import io.github.nekosora.utils.GameUtils;

import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            if (!GameFileUtils.checkAndCreateMainDir(GameSettings.mainDir)) return;
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(GameSettings.mainDir);
            }

            FileEventCatcher exitGameCatcher = new FileEventCatcher(new File(GameSettings.mainDir, "ExitGame.off"), (data) -> {
                File newFile;
                if (data != null && data[3] instanceof File file)
                    newFile = file;
                else
                    throw new InternalError("RENAME data[3] should be of type File");
                if ("ExitGame.on".equals(newFile.getName().trim())) {
                    GameUtils.exitGame(GameUtils.ExitReason.exitGameFileDetected);
                }
            }, FileEventType.RENAMED);
            exitGameCatcher.start();

            FileEventCatcher singleplayerCatcher = new FileEventCatcher(new File(new File(GameSettings.mainDir, "Play"), "Singleplayer.off"), (data) -> {
                File newFile;
                if (data != null && data[3] instanceof File file)
                    newFile = file;
                else
                    throw new InternalError("RENAME data[3] should be of type File");
                if ("Singleplayer.on".equals(newFile.getName().trim())) {
                    // TODO: 开始单人剧情
                    throw new NullPointerException("SINGLEPLAYER_NODE0 is null!");
                }
            }, FileEventType.RENAMED);
            singleplayerCatcher.start();

            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            CrashUtils.crash(e);
        }
    }
}
