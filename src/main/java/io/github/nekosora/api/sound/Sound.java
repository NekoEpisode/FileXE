package io.github.nekosora.api.sound;

import com.google.gson.JsonObject;

public class Sound {
    private final String path;
    private final double speed;
    private final double volume;
    private final double balance;

    public Sound(String path, double speed, double volume, double balance) {
        this.path = path;
        this.speed = speed;
        this.volume = volume;
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public double getSpeed() {
        return speed;
    }

    public double getVolume() {
        return volume;
    }

    public String getPath() {
        return path;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("path", path);
        jsonObject.addProperty("speed", speed);
        jsonObject.addProperty("volume", volume);
        jsonObject.addProperty("balance", balance);
        return jsonObject;
    }

    public static Sound fromJson(JsonObject jsonObject) {
        return new Sound(
                jsonObject.get("path").getAsString(),
                jsonObject.get("speed").getAsDouble(),
                jsonObject.get("volume").getAsDouble(),
                jsonObject.get("balance").getAsDouble()
        );
    }
}
