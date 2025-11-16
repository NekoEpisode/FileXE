package io.github.nekosora.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class GameMenuUtils {
    private static final Logger log = LoggerFactory.getLogger(GameMenuUtils.class);
    // 存储需要删除的文件和目录
    private static final Set<File> filesToDelete = Collections.synchronizedSet(new HashSet<>());
    // 存储需要保留的文件和目录（白名单）
    private static final Set<File> filesToKeep = Collections.synchronizedSet(new HashSet<>());

    /**
     * 初始化游戏主菜单目录结构
     * 可以随时调用，如果目录已存在则不会重复创建
     */
    public static boolean initializeGameMenu(File root) throws IOException {
        // 创建目录结构
        File settingsDir = createDirectory(root, "Settings");
        File generalOptions = createDirectory(settingsDir, "General");
        File aboutDir = createDirectory(root, "About");
        File playDir = createDirectory(root, "Play");

        if (settingsDir == null || generalOptions == null || aboutDir == null || playDir == null) {
            return false;
        }

        // 创建文件
        Map<File, String> filesToCreate = Map.of(
                new File(root, "FileXE - Main Menu"), "This is FileXE's main menu!",
                new File(generalOptions, "NothingHere..."), "I said, nothing here.",
                new File(aboutDir, "Authors.about"), """
                Authors:
                    - NekoSora
                    - CYsonHab
                """,
                new File(aboutDir, "GameVersion.about"), """
                FileXE Version 0.01-Beta
                
                2025 © Copyright NekoSora & CYsonHab, TeamFileXE.
                """,
                new File(playDir, "Singleplayer.off"), "Start singleplayer game.",
                new File(playDir, "Multiplayer.off"), "Start Multiplayer game."
        );

        for (Map.Entry<File, String> entry : filesToCreate.entrySet()) {
            if (!createFileWithContent(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        // 处理退出游戏文件
        File exitGameOn = new File(root, "ExitGame.on");
        File exitGameOff = new File(root, "ExitGame.off");

        // 如果存在.on文件，删除它
        if (exitGameOn.exists()) {
            if (!exitGameOn.delete()) {
                log.warn("Could not delete {}", exitGameOn.getAbsolutePath());
            }
        }

        // 创建.off文件（如果不存在）
        if (!exitGameOff.exists()) {
            if (!createFileWithContent(exitGameOff, "Would you want to exit game?")) {
                return false;
            }
        }

        log.info("Game menu initialized successfully at: {}", root.getAbsolutePath());
        return true;
    }

    /**
     * 检查游戏菜单是否已初始化
     */
    public static boolean isGameMenuInitialized(File root) {
        File[] requiredDirs = {
                new File(root, "Settings"),
                new File(root, "Settings/General"),
                new File(root, "About"),
                new File(root, "Play")
        };

        File[] requiredFiles = {
                new File(root, "FileXE - Main Menu"),
                new File(root, "Settings/General/NothingHere..."),
                new File(root, "About/Authors.about"),
                new File(root, "About/GameVersion.about"),
                new File(root, "Play/Singleplayer.off"),
                new File(root, "Play/Multiplayer.off"),
                new File(root, "ExitGame.off")
        };

        // 检查所有必需目录是否存在
        for (File dir : requiredDirs) {
            if (!dir.exists() || !dir.isDirectory()) {
                return false;
            }
        }

        // 检查所有必需文件是否存在
        for (File file : requiredFiles) {
            if (!file.exists() || !file.isFile()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 清理游戏菜单（选择性删除，不删除root目录）
     */
    public static boolean cleanupGameMenu(File root) {
        boolean success = true;

        // 定义要删除的文件和目录（不包括root）
        List<File> itemsToDelete = Arrays.asList(
                new File(root, "Settings"),
                new File(root, "About"),
                new File(root, "Play"),
                new File(root, "FileXE - Main Menu"),
                new File(root, "ExitGame.on"),
                new File(root, "ExitGame.off")
        );

        for (File item : itemsToDelete) {
            if (item.exists()) {
                try {
                    if (item.isDirectory()) {
                        deleteDirectoryRecursively(item);
                    } else {
                        Files.delete(item.toPath());
                    }
                    log.info("Deleted: {}", item.getAbsolutePath());
                } catch (IOException e) {
                    log.error("Failed to delete: {} - {}", item.getAbsolutePath(), e.getMessage());
                    success = false;
                }
            }
        }

        return success;
    }

    // 以下方法保持原有功能，但不再自动调用删除逻辑

    // 标记文件/目录为需要删除
    public static void markForDeletion(File file) {
        filesToDelete.add(file);
        filesToKeep.remove(file); // 确保不在保留列表中
    }

    // 标记文件/目录为需要保留
    public static void markForKeeping(File file) {
        filesToKeep.add(file);
        filesToDelete.remove(file); // 确保不在删除列表中
    }

    // 获取所有需要删除的文件（只读视图）
    public static Set<File> getFilesToDelete() {
        return Collections.unmodifiableSet(filesToDelete);
    }

    // 获取所有需要保留的文件（只读视图）
    public static Set<File> getFilesToKeep() {
        return Collections.unmodifiableSet(filesToKeep);
    }

    // 检查文件是否标记为删除
    public static boolean isMarkedForDeletion(File file) {
        return filesToDelete.contains(file);
    }

    // 检查文件是否标记为保留
    public static boolean isMarkedForKeeping(File file) {
        return filesToKeep.contains(file);
    }

    // 执行删除操作（手动调用）
    public static boolean cleanupMarkedFiles() {
        boolean success = true;
        List<File> deletedFiles = new ArrayList<>();

        // 先删除文件，再删除目录
        List<File> files = new ArrayList<>(filesToDelete);
        files.sort((f1, f2) -> Boolean.compare(f1.isDirectory(), f2.isDirectory()));

        for (File file : files) {
            try {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(file);
                    } else {
                        Files.delete(file.toPath());
                    }
                    deletedFiles.add(file);
                    log.info("Deleted: {}", file.getAbsolutePath());
                }
            } catch (IOException e) {
                log.error("Failed to delete: {} - {}", file.getAbsolutePath(), e.getMessage());
                success = false;
            }
        }

        // 从删除列表中移除已删除的文件
        deletedFiles.forEach(filesToDelete::remove);
        return success;
    }

    // 递归删除目录
    private static void deleteDirectoryRecursively(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursively(file);
                } else {
                    Files.delete(file.toPath());
                }
            }
        }
        Files.delete(dir.toPath());
    }

    private static File createDirectory(File parent, String dirName) {
        File dir = new File(parent, dirName);
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("Error creating directory: {}", dir.getAbsolutePath());
            return null;
        }
        return dir;
    }

    private static boolean createFileWithContent(File file, String content) {
        try {
            if (!file.exists() && !file.createNewFile()) {
                log.error("Error creating file: {}", file.getAbsolutePath());
                return false;
            }
            Files.writeString(file.toPath(), content);
            return true;
        } catch (IOException e) {
            log.error("Error writing to file: {} - {}", file.getAbsolutePath(), e.getMessage());
            return false;
        }
    }
}