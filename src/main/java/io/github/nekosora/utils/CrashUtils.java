package io.github.nekosora.utils;

import io.github.nekosora.context.GameContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;

public class CrashUtils {
    private static final Logger log = LoggerFactory.getLogger(CrashUtils.class);

    public static void crash(Throwable e) {
        try {
            if (GameContext.isExiting) return; // 如果游戏正在关闭，那就没必要显示错误了
            log.error("Oh no! Game crashed!!!!");
            File crashedLogDir = new File("./crash_reports/");
            if (!crashedLogDir.exists()) crashedLogDir.mkdir();
            File file = new File(crashedLogDir, "FileXE_crash_" + System.currentTimeMillis() + ".log");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            sw.write("Oops, it looks FileXE Crashed!\nStackTrace:\n");
            e.printStackTrace(pw);
            Files.write(file.toPath(), sw.toString().getBytes());
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
            GameUtils.exitGame(GameUtils.ExitReason.crashed);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.err.println("Oops, FileXE's crash handler crashed!\nStackTrace:\n" + sw);
            GameUtils.exitGame(GameUtils.ExitReason.crashed);
        }
    }
}
