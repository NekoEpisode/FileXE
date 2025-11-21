package io.github.nekosora.api.achievement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import io.github.nekosora.utils.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AchievementFile {
    private static final Logger log = LoggerFactory.getLogger(AchievementFile.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final File file;
    private final List<Achievement> achievements;
    private final Gson gson;

    public AchievementFile(File file) {
        this.file = file;
        this.achievements = new ArrayList<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void addAchievement(Achievement achievement) {
        achievements.add(achievement);
    }

    public void addAchievement(Achievement... achievements) {
        Collections.addAll(this.achievements, achievements);
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject root = new JsonObject();
            JsonArray achievementsArray = new JsonArray();

            for (Achievement achievement : achievements) {
                achievementsArray.add(serializeAchievement(achievement));
            }

            root.add("achievements", achievementsArray);
            gson.toJson(root, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save achievement file", e);
        }
    }

    public static AchievementFile read(File file) {
        AchievementFile achievementFile = new AchievementFile(file);

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = achievementFile.gson.fromJson(reader, JsonObject.class);
            if (root != null && root.has("achievements")) {
                JsonArray achievementsArray = root.getAsJsonArray("achievements");

                for (JsonElement element : achievementsArray) {
                    JsonObject achievementJson = element.getAsJsonObject();
                    Achievement achievement = achievementFile.deserializeAchievement(achievementJson);
                    if (achievement != null) {
                        achievementFile.addAchievement(achievement);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read achievement file", e);
        }

        return achievementFile;
    }

    private JsonObject serializeAchievement(Achievement achievement) {
        JsonObject json = new JsonObject();

        // 只保存命名空间和解锁时间
        json.addProperty("namespace", achievement.getNamespace().toString());

        // 序列化解锁时间
        if (achievement.getUnlockTime() != null) {
            json.addProperty("unlockTime", achievement.getUnlockTime().format(DATE_FORMATTER));
        }

        return json;
    }

    private Achievement deserializeAchievement(JsonObject json) {
        try {
            // 解析命名空间
            Namespace namespace = Namespace.fromString(json.get("namespace").getAsString());

            // 从已注册的成就中获取模板
            Achievement template = AchievementManager.getAchievement(namespace);
            if (template == null) {
                log.warn("No registered achievement found for namespace: {}, skipping", namespace);
                return null;
            }

            // 创建已完成成就的副本
            Achievement achievement = new Achievement(
                    namespace,
                    template.getName(),
                    template.getDescription(),
                    template.getOnCompleteSound(),
                    template.getAchievementAttribute()
            );

            // 设置解锁时间
            if (json.has("unlockTime")) {
                String unlockTimeStr = json.get("unlockTime").getAsString();
                LocalDateTime unlockTime = LocalDateTime.parse(unlockTimeStr, DATE_FORMATTER);
                achievement.setUnlockTime(unlockTime);
            }

            return achievement;

        } catch (Exception e) {
            log.error("Failed to deserialize achievement: {}", json, e);
            return null;
        }
    }

    public List<Achievement> getAchievements() {
        return Collections.unmodifiableList(achievements);
    }

    public void clear() {
        achievements.clear();
    }

    public boolean removeAchievement(Achievement achievement) {
        return achievements.remove(achievement);
    }
}