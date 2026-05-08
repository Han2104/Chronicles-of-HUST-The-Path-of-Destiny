package com.hust.game.maps.d9;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * D9Map - Chứa dữ liệu map đã load từ TMX
 */
public class D9Map {
    private int width;
    private int height;
    private int tileWidth;
    private int tileHeight;

    // Layers
    private int[][] backgroundLayer;
    private int[][] platformsLayer;
    private int[][] decorLayer;
    private int[][] floorDividerLayer;
    private int[][] checkpointsLayer;

    // Objects
    private List<D9Object> objects = new ArrayList<>();

    // Collision boxes
    private List<Rectangle> platformCollisionBoxes = new ArrayList<>();
    private List<Rectangle> explicitBoundaryCollisionBoxes = new ArrayList<>();
    private List<Rectangle> fallbackBoundaryCollisionBoxes = new ArrayList<>();
    private List<Rectangle> boundaryCollisionBoxes = new ArrayList<>();
    private List<Rectangle> collisionBoxes = new ArrayList<>();

    // Tilesets
    private List<TilesetInfo> tilesets = new ArrayList<>();

    // Settings
    private D9Settings settings;

    public D9Map(int width, int height, int tileWidth, int tileHeight) {
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.settings = new D9Settings();
    }

    // Getters và setters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTileWidth() { return tileWidth; }
    public int getTileHeight() { return tileHeight; }

    public int[][] getBackgroundLayer() { return backgroundLayer; }
    public void setBackgroundLayer(int[][] backgroundLayer) { this.backgroundLayer = backgroundLayer; }

    public int[][] getPlatformsLayer() { return platformsLayer; }
    public void setPlatformsLayer(int[][] platformsLayer) { this.platformsLayer = platformsLayer; }

    public int[][] getDecorLayer() { return decorLayer; }
    public void setDecorLayer(int[][] decorLayer) { this.decorLayer = decorLayer; }

    public int[][] getFloorDividerLayer() { return floorDividerLayer; }
    public void setFloorDividerLayer(int[][] floorDividerLayer) { this.floorDividerLayer = floorDividerLayer; }

    public int[][] getCheckpointsLayer() { return checkpointsLayer; }
    public void setCheckpointsLayer(int[][] checkpointsLayer) { this.checkpointsLayer = checkpointsLayer; }

    public List<D9Object> getObjects() { return objects; }
    public void setObjects(List<D9Object> objects) { this.objects = objects != null ? objects : new ArrayList<>(); }

    public List<Rectangle> getPlatformCollisionBoxes() { return Collections.unmodifiableList(platformCollisionBoxes); }
    public void setPlatformCollisionBoxes(List<Rectangle> platformCollisionBoxes) {
        this.platformCollisionBoxes = platformCollisionBoxes != null ? new ArrayList<>(platformCollisionBoxes) : new ArrayList<>();
        rebuildSolidCollisionBoxes();
    }

    public List<Rectangle> getBoundaryCollisionBoxes() { return Collections.unmodifiableList(boundaryCollisionBoxes); }
    public void setBoundaryCollisionBoxes(List<Rectangle> boundaryCollisionBoxes) {
        this.explicitBoundaryCollisionBoxes = boundaryCollisionBoxes != null ? new ArrayList<>(boundaryCollisionBoxes) : new ArrayList<>();
        this.fallbackBoundaryCollisionBoxes = new ArrayList<>();
        rebuildBoundaryCollisionBoxes();
        rebuildSolidCollisionBoxes();
    }

    public List<Rectangle> getExplicitBoundaryCollisionBoxes() { return Collections.unmodifiableList(explicitBoundaryCollisionBoxes); }
    public List<Rectangle> getFallbackBoundaryCollisionBoxes() { return Collections.unmodifiableList(fallbackBoundaryCollisionBoxes); }
    public void setBoundaryCollisionBoxes(List<Rectangle> explicitBoundaryCollisionBoxes,
                                          List<Rectangle> fallbackBoundaryCollisionBoxes) {
        this.explicitBoundaryCollisionBoxes = explicitBoundaryCollisionBoxes != null
                ? new ArrayList<>(explicitBoundaryCollisionBoxes)
                : new ArrayList<>();
        this.fallbackBoundaryCollisionBoxes = fallbackBoundaryCollisionBoxes != null
                ? new ArrayList<>(fallbackBoundaryCollisionBoxes)
                : new ArrayList<>();
        rebuildBoundaryCollisionBoxes();
        rebuildSolidCollisionBoxes();
    }

    public List<Rectangle> getCollisionBoxes() { return Collections.unmodifiableList(collisionBoxes); }
    public void setCollisionBoxes(List<Rectangle> collisionBoxes) {
        this.platformCollisionBoxes = collisionBoxes != null ? new ArrayList<>(collisionBoxes) : new ArrayList<>();
        rebuildSolidCollisionBoxes();
    }

    public List<TilesetInfo> getTilesets() { return Collections.unmodifiableList(tilesets); }
    public void addTileset(TilesetInfo tileset) {
        if (tileset != null) {
            this.tilesets.add(tileset);
            this.tilesets.sort(Comparator.comparingInt(TilesetInfo::getFirstGid));
        }
    }

    public D9Settings getSettings() { return settings; }

    public int getPixelWidth() { return width * tileWidth; }
    public int getPixelHeight() { return height * tileHeight; }

    private void rebuildSolidCollisionBoxes() {
        collisionBoxes = new ArrayList<>(platformCollisionBoxes.size() + boundaryCollisionBoxes.size());
        collisionBoxes.addAll(platformCollisionBoxes);
        collisionBoxes.addAll(boundaryCollisionBoxes);
    }

    private void rebuildBoundaryCollisionBoxes() {
        boundaryCollisionBoxes = new ArrayList<>(explicitBoundaryCollisionBoxes.size() + fallbackBoundaryCollisionBoxes.size());
        boundaryCollisionBoxes.addAll(explicitBoundaryCollisionBoxes);
        boundaryCollisionBoxes.addAll(fallbackBoundaryCollisionBoxes);
    }

    // Helper methods
    public D9Object getPlayerSpawn() {
        if (objects == null) return null;
        D9Object namedSpawn = objects.stream()
                .filter(obj -> "spawn_floor_1".equals(obj.getName()) && "PlayerSpawn".equals(obj.getClassType()))
                .sorted(Comparator.comparingInt(D9Object::getY).thenComparingInt(D9Object::getX))
                .findFirst()
                .orElse(null);
        if (namedSpawn != null) {
            return namedSpawn;
        }
        return objects.stream()
                .filter(obj -> "PlayerSpawn".equals(obj.getClassType()))
                .sorted(Comparator.comparingInt(D9Object::getY).thenComparingInt(D9Object::getX))
                .findFirst()
                .orElse(null);
    }

    public List<D9Object> getPlayerSpawns() {
        if (objects == null) return Collections.emptyList();
        return objects.stream()
                .filter(obj -> "PlayerSpawn".equals(obj.getClassType()))
                .toList();
    }

    public List<D9Object> getCheckpoints() {
        if (objects == null) return Collections.emptyList();
        return objects.stream()
                .filter(obj -> "Checkpoint".equals(obj.getClassType()))
                .toList();
    }

    public List<D9Object> getQuizDoors() {
        if (objects == null) return Collections.emptyList();
        return objects.stream()
                .filter(obj -> "QuizDoor".equals(obj.getClassType()) || "FinalQuizDoor".equals(obj.getClassType()))
                .toList();
    }

    public List<D9Object> getFloorDividers() {
        if (objects == null) return Collections.emptyList();
        return objects.stream()
                .filter(obj -> "FloorDivider".equals(obj.getClassType()))
                .toList();
    }

    public List<D9Object> getMapBoundaries() {
        if (objects == null) return Collections.emptyList();
        return objects.stream()
                .filter(obj -> "MapBoundary".equals(obj.getClassType()))
                .toList();
    }

    public D9Object getObjectByName(String name) {
        if (objects == null || name == null) return null;
        return objects.stream()
                .filter(obj -> name.equals(obj.getName()))
                .findFirst()
                .orElse(null);
    }

    // Simple Rectangle class for collision
    public static class Rectangle {
        public int x, y, width, height;

        public Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean intersects(Rectangle other) {
            return x < other.x + other.width && x + width > other.x &&
                   y < other.y + other.height && y + height > other.y;
        }

        public int bottom() { return y + height; }
        public int right() { return x + width; }
    }

    public static class TilesetInfo {
        private final int firstGid;
        private final int tileCount;
        private final int columns;
        private final int tileWidth;
        private final int tileHeight;
        private final String imageSource;

        public TilesetInfo(int firstGid, int tileCount, int columns, int tileWidth, int tileHeight, String imageSource) {
            this.firstGid = firstGid;
            this.tileCount = tileCount;
            this.columns = columns;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.imageSource = imageSource;
        }

        public int getFirstGid() { return firstGid; }
        public int getTileCount() { return tileCount; }
        public int getColumns() { return columns; }
        public int getTileWidth() { return tileWidth; }
        public int getTileHeight() { return tileHeight; }
        public String getImageSource() { return imageSource; }
    }
}
