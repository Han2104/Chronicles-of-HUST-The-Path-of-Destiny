package com.hust.game.maps.d9;

import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * D9Object - Đại diện cho object trong TMX
 */
public class D9Object {
    private final String name;
    private final String classType; // PlayerSpawn, Checkpoint, QuizDoor, etc.
    private final int x, y, width, height;
    private final Map<String, String> properties;

    public D9Object(String name, String classType, int x, int y, int width, int height, Map<String, String> properties) {
        this.name = name != null ? name : "";
        this.classType = classType != null ? classType : "";
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.properties = properties != null ? new LinkedHashMap<>(properties) : new LinkedHashMap<>();
    }

    // Getters
    public String getName() { return name; }
    public String getClassType() { return classType; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Map<String, String> getProperties() { return Collections.unmodifiableMap(properties); }

    // Helper methods
    public String getProperty(String key) {
        return properties.get(key);
    }

    public String getPropertyOrDefault(String key, String defaultValue) {
        String value = properties.get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public String getFirstProperty(String... keys) {
        if (keys == null) {
            return null;
        }
        for (String key : keys) {
            String value = properties.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public int getPropertyAsInt(String key, int defaultValue) {
        String value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getPropertyAsBoolean(String key, boolean defaultValue) {
        String value = properties.get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    // Specific getters for common properties
    public int getFloor() { return getPropertyAsInt("floor", 1); }
    public String getDirection() { return getPropertyOrDefault("direction", "right").trim(); }
    public boolean isActive() { return getPropertyAsBoolean("active", true); }
    public String getSpawnId() {
        String spawnId = getFirstProperty("spawnId", "spawId");
        return spawnId != null ? spawnId.trim() : "spawn_1";
    }

    public D9Map.Rectangle toRectangle() {
        return new D9Map.Rectangle(x, y, width, height);
    }
}
