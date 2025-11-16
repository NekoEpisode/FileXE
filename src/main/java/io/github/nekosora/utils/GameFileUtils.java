package io.github.nekosora.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class GameFileUtils {
    // 存储需要删除的文件和目录
    private static final Set<File> filesToDelete = Collections.synchronizedSet(new HashSet<>());
    // 存储需要保留的文件和目录（白名单）
    private static final Set<File> filesToKeep = Collections.synchronizedSet(new HashSet<>());

    public static boolean checkAndCreateMainDir(File root) throws IOException {
        // 清空之前的列表
        filesToDelete.clear();
        filesToKeep.clear();

        // 创建目录结构
        File settingsDir = createDirectory(root, "Settings");
        File generalOptions = createDirectory(settingsDir, "General");
        File aboutDir = createDirectory(root, "About");
        File playDir = createDirectory(root, "Play");

        if (settingsDir == null || generalOptions == null || aboutDir == null) {
            return false;
        }

        markForDeletion(settingsDir);
        markForDeletion(generalOptions);
        markForDeletion(aboutDir);
        markForDeletion(playDir);
        markForDeletion(root);

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
            // 默认情况下，新创建的文件标记为删除
            markForDeletion(entry.getKey());
        }

        File exitGameOn = new File(root, "ExitGame.on");
        if (exitGameOn.exists()) {
            if (!exitGameOn.delete()) {
                System.err.println("Could not delete " + exitGameOn.getAbsolutePath());
                return false;
            }
        }

        File exitGameOff = new File(root, "ExitGame.off");
        if (!exitGameOff.exists()) {
            if (!exitGameOff.createNewFile()) {
                System.err.println("Could not create " + exitGameOff.getAbsolutePath());
                return false;
            }
            Files.writeString(exitGameOff.toPath(), "Would you want to exit game?");
        }

        markForDeletion(exitGameOff);
        markForDeletion(exitGameOn);

        System.out.println("All directories and files were checked and created successfully");
        return true;
    }

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

    // 执行删除操作
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
                    System.out.println("Deleted: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("Failed to delete: " + file.getAbsolutePath() + " - " + e.getMessage());
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
            System.err.println("Error creating directory: " + dir.getAbsolutePath());
            return null;
        }
        return dir;
    }

    private static boolean createFileWithContent(File file, String content) {
        try {
            if (!file.exists() && !file.createNewFile()) {
                System.err.println("Error creating file: " + file.getAbsolutePath());
                return false;
            }
            Files.writeString(file.toPath(), content);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to file: " + file.getAbsolutePath() + " - " + e.getMessage());
            return false;
        }
    }
}