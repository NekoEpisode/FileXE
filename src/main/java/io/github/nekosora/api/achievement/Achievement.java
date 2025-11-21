package io.github.nekosora.api.achievement;

import io.github.nekosora.api.sound.Sound;
import io.github.nekosora.utils.Namespace;

import java.time.LocalDateTime;
import java.util.Objects;

public class Achievement {
    private final Namespace namespace;
    private final String name;
    private final String description;
    private final AchievementAttribute[] achievementAttribute;
    private final Sound onCompleteSound;
    private LocalDateTime unlockTime;

    public Achievement(Namespace namespace, String name, String description, AchievementAttribute... attributes) {
        this.namespace = namespace;
        this.name = name;
        this.description = description;
        this.achievementAttribute = attributes;
        this.onCompleteSound = null;
        this.unlockTime = null;
    }

    public Achievement(Namespace namespace, String name, String description,
                       Sound onCompleteSound, AchievementAttribute... attributes) {
        this.namespace = namespace;
        this.name = name;
        this.description = description;
        this.onCompleteSound = onCompleteSound;
        this.achievementAttribute = attributes;
        this.unlockTime = null;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Sound getOnCompleteSound() {
        return onCompleteSound;
    }

    public AchievementAttribute[] getAchievementAttribute() {
        return achievementAttribute;
    }

    public LocalDateTime getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(LocalDateTime unlockTime) {
        this.unlockTime = unlockTime;
    }

    public boolean isUnlocked() {
        return unlockTime != null;
    }

    @Override
    public String toString() {
        return "Achievement{" +
                "namespace=" + namespace +
                ", name='" + name + '\'' +
                ", unlocked=" + isUnlocked() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Achievement that = (Achievement) o;
        return Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(namespace);
    }
}