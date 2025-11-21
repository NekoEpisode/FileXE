package io.github.nekosora.api.achievement;

import io.github.nekosora.api.sound.Sound;
import io.github.nekosora.api.sound.SoundEngine;
import io.github.nekosora.api.sound.Sounds;
import io.github.nekosora.utils.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementManager {
    private static final Map<Namespace, Achievement> achievementMap = new ConcurrentHashMap<>();
    private static final Map<Namespace, Achievement> completedAchievements = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(AchievementManager.class);

    public static void registerAchievement(Achievement achievement) {
        achievementMap.put(achievement.getNamespace(), achievement);
    }

    public static void achievementComplete(Namespace namespace) {
        Achievement achievement = achievementMap.get(namespace);
        if (achievement != null && !isCompleted(namespace)) {
            // 创建已完成成就的副本并设置解锁时间
            Achievement completedAchievement = createCompletedAchievement(achievement);

            Sound customSound = completedAchievement.getOnCompleteSound();
            SoundEngine.playSound(Objects.requireNonNullElse(customSound, Sounds.ACHIEVEMENT_COMPLETED));

            completedAchievements.put(namespace, completedAchievement);
            AchievementManager.log.info("Achievement completed: {} - {}", namespace, completedAchievement.getName());
        }
    }

    private static Achievement createCompletedAchievement(Achievement original) {
        Achievement completed = new Achievement(
                original.getNamespace(),
                original.getName(),
                original.getDescription(),
                original.getOnCompleteSound(),
                original.getAchievementAttribute()
        );
        completed.setUnlockTime(LocalDateTime.now());
        return completed;
    }

    public static Achievement getAchievement(Namespace namespace) {
        return achievementMap.get(namespace);
    }

    public static Achievement getCompletedAchievement(Namespace namespace) {
        return completedAchievements.get(namespace);
    }

    public static boolean hasAchievement(Namespace namespace) {
        return achievementMap.containsKey(namespace);
    }

    public static boolean isCompleted(Namespace namespace) {
        return completedAchievements.containsKey(namespace);
    }

    public static boolean isCompleted(Achievement achievement) {
        return completedAchievements.containsKey(achievement.getNamespace());
    }

    public static List<Achievement> getCompletedAchievements() {
        return new ArrayList<>(completedAchievements.values());
    }

    public static List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievementMap.values());
    }

    public static void save(File saveFile) throws IOException {
        if (!saveFile.exists()) {
            saveFile.getParentFile().mkdirs();
            saveFile.createNewFile();
        }

        AchievementFile file = new AchievementFile(saveFile);
        // 只保存已完成的成就
        for (Achievement achievement : completedAchievements.values()) {
            file.addAchievement(achievement);
        }
        file.save();
    }

    public static void load(File saveFile) throws IOException {
        if (!saveFile.exists()) {
            return;
        }

        AchievementFile file = AchievementFile.read(saveFile);
        completedAchievements.clear();
        for (Achievement achievement : file.getAchievements()) {
            // 验证加载的成就是否在已注册的成就列表中
            if (achievementMap.containsKey(achievement.getNamespace())) {
                completedAchievements.put(achievement.getNamespace(), achievement);
            } else {
                log.warn("Loaded achievement {} is not registered, skipping", achievement.getNamespace());
            }
        }
    }

    public static void clearCompletedAchievements() {
        completedAchievements.clear();
    }

    public static int getCompletionProgress() {
        if (achievementMap.isEmpty()) {
            return 0;
        }
        return (completedAchievements.size() * 100) / achievementMap.size();
    }

    public static Map<Namespace, Achievement> getAchievementMap() {
        return achievementMap;
    }

    public static Map<Namespace, Achievement> getCompletedAchievementMap() {
        return completedAchievements;
    }

    /**
     * 获取按解锁时间排序的已完成成就列表
     */
    public static List<Achievement> getCompletedAchievementsSortedByTime() {
        List<Achievement> sorted = new ArrayList<>(completedAchievements.values());
        sorted.sort((a1, a2) -> {
            LocalDateTime time1 = a1.getUnlockTime();
            LocalDateTime time2 = a2.getUnlockTime();
            if (time1 == null && time2 == null) return 0;
            if (time1 == null) return 1;
            if (time2 == null) return -1;
            return time1.compareTo(time2);
        });
        return sorted;
    }
}