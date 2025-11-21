package io.github.nekosora.settings;

import java.io.File;

public class GameSettings {
    public static File mainDir = new File("./FileXE");
    public static final File saveDir = createSaveDir();
    public static final File achievementSaveFile = new File(saveDir, "achievements.json");

    private static File createSaveDir() {
        File dir = switch (System.getProperty("os.name").toLowerCase()) {
            case String s when s.contains("win") -> new File(System.getProperty("user.home"), "Documents/FileXE");
            case String s when s.contains("mac") -> new File(System.getProperty("user.home"), "Library/Application Support/FileXE");
            case String s when s.contains("nix") || s.contains("nux") || s.contains("aix") -> new File(System.getProperty("user.home"), ".filexe");
            case String s when s.contains("sunos") -> new File(System.getProperty("user.home"), ".filexe");
            default -> new File(System.getProperty("user.home"), "FileXE");
        };

        // 确保目录存在
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
