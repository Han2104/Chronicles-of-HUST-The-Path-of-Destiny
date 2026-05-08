package com.hust.game.maps.d9;

/**
 * D9CollisionManager - Xử lý collision với platforms và boundaries
 */
public class D9CollisionManager {
    private final D9Map map;

    public D9CollisionManager(D9Map map) {
        this.map = map;
    }

    public boolean isColliding(double x, double y, int width, int height) {
        D9Map.Rectangle playerRect = rect(x, y, width, height);

        for (D9Map.Rectangle box : map.getCollisionBoxes()) {
            if (playerRect.intersects(box)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOnGround(double x, double y, int width, int height) {
        D9Map.Rectangle playerRect = rect(x, y + height, width, 2);

        for (D9Map.Rectangle box : map.getCollisionBoxes()) {
            if (playerRect.intersects(box)) {
                return true;
            }
        }
        return false;
    }

    public double resolveHorizontal(double x, double y, int width, int height, double velocityX) {
        if (velocityX == 0) {
            return x;
        }

        double nextX = x + velocityX;
        D9Map.Rectangle nextRect = rect(nextX, y, width, height);
        D9Map.Rectangle hit = null;
        for (D9Map.Rectangle box : map.getCollisionBoxes()) {
            if (!nextRect.intersects(box)) {
                continue;
            }
            if (velocityX > 0) {
                if (hit == null || box.x < hit.x) {
                    hit = box;
                }
            } else {
                if (hit == null || box.right() > hit.right()) {
                    hit = box;
                }
            }
        }

        if (hit == null) {
            return nextX;
        }
        if (velocityX > 0) {
            return hit.x - width;
        }
        return hit.right();
    }

    public VerticalMoveResult resolveVertical(double x, double y, int width, int height, double velocityY) {
        if (velocityY == 0) {
            return new VerticalMoveResult(y, 0, isOnGround(x, y, width, height), false);
        }

        double nextY = y + velocityY;
        if (velocityY > 0) {
            D9Map.Rectangle landing = findLandingCollision(x, y, nextY, width, height);
            if (landing != null) {
                return new VerticalMoveResult(landing.y - height, 0, true, true);
            }
            return new VerticalMoveResult(nextY, velocityY, false, false);
        }

        D9Map.Rectangle ceiling = findCeilingCollision(x, y, nextY, width, height);
        if (ceiling != null) {
            return new VerticalMoveResult(ceiling.bottom(), 0, false, false);
        }

        D9Map.Rectangle nextRect = rect(x, nextY, width, height);
        for (D9Map.Rectangle box : map.getCollisionBoxes()) {
            if (nextRect.intersects(box)) {
                return new VerticalMoveResult(y, 0, false, false);
            }
        }

        return new VerticalMoveResult(nextY, velocityY, false, false);
    }

    private D9Map.Rectangle findLandingCollision(double x, double previousY, double nextY, int width, int height) {
        double previousBottom = previousY + height;
        double nextBottom = nextY + height;
        D9Map.Rectangle best = null;
        int left = (int) Math.floor(x);
        int right = (int) Math.ceil(x + width);

        for (D9Map.Rectangle box : map.getCollisionBoxes()) {
            if (!overlapsHorizontally(left, right, box)) {
                continue;
            }
            if (previousBottom <= box.y && nextBottom >= box.y) {
                if (best == null || box.y < best.y) {
                    best = box;
                }
            }
        }
        return best;
    }

    private D9Map.Rectangle findCeilingCollision(double x, double previousY, double nextY, int width, int height) {
        double previousTop = previousY;
        double nextTop = nextY;
        D9Map.Rectangle best = null;
        int left = (int) Math.floor(x);
        int right = (int) Math.ceil(x + width);

        for (D9Map.Rectangle box : map.getCollisionBoxes()) {
            if (!overlapsHorizontally(left, right, box)) {
                continue;
            }
            if (previousTop >= box.y && nextTop <= box.y) {
                if (best == null || box.y > best.y) {
                    best = box;
                }
            }
        }
        return best;
    }

    public SnapResult snapObjectToNearestPlatform(D9Object zone, int playerWidth, int playerHeight) {
        if (zone == null) {
            return snapToNearestPlatform(100, 100, 32, 32, playerWidth, playerHeight);
        }
        return snapToNearestPlatform(zone.getX(), zone.getY(), zone.getWidth(), zone.getHeight(), playerWidth, playerHeight);
    }

    public SnapResult snapToNearestPlatform(int zoneX, int zoneY, int zoneWidth, int zoneHeight,
                                            int playerWidth, int playerHeight) {
        System.out.println("[D9Collision] TASK 4 - Snapping to platform from " + map.getPlatformCollisionBoxes().size() + " collision boxes");
        int desiredX = zoneX + zoneWidth / 2 - playerWidth / 2;
        int searchLeft = zoneX;
        int searchRight = zoneX + zoneWidth;
        int searchTop = zoneY;
        int searchBottom = zoneY + zoneHeight;
        D9Map.Rectangle platform = null;
        D9Map.Rectangle nearestPlatform = findNearestPlatform(zoneX + zoneWidth / 2, zoneY + zoneHeight / 2);
        boolean foundUnder = false;
        int platformsNear = countPlatformsNearZone(zoneX, zoneY, zoneWidth, zoneHeight);

        for (D9Map.Rectangle box : map.getPlatformCollisionBoxes()) {
            if (!overlapsHorizontally(searchLeft, searchRight, box)) {
                continue;
            }
            boolean belowOrOverlappingZone = box.y >= searchTop || (box.bottom() >= searchTop && box.y <= searchBottom);
            if (belowOrOverlappingZone) {
                if (platform == null || box.y < platform.y) {
                    platform = box;
                    foundUnder = true;
                }
            }
        }

        if (platform == null) {
            platform = nearestPlatform;
        }

        if (platform == null) {
            return new SnapResult(desiredX, zoneY, null, nearestPlatform, false, platformsNear);
        }

        int snappedX = clamp(desiredX, platform.x, Math.max(platform.x, platform.right() - playerWidth));
        int snappedY = platform.y - playerHeight;
        return new SnapResult(snappedX, snappedY, platform, nearestPlatform, foundUnder, platformsNear);
    }

    public SnapResult snapToNearestSafePlatform(double x, double y, int playerWidth, int playerHeight) {
        D9Map.Rectangle platform = findNearestPlatform((int) Math.round(x), (int) Math.round(y));
        if (platform == null) {
            return new SnapResult((int) Math.round(x), (int) Math.round(y), null, null, false, 0);
        }
        int snappedX = clamp((int) Math.round(x), platform.x, Math.max(platform.x, platform.right() - playerWidth));
        return new SnapResult(snappedX, platform.y - playerHeight, platform, platform, false, 1);
    }

    private D9Map.Rectangle findNearestPlatform(int centerX, int centerY) {
        D9Map.Rectangle best = null;
        long bestDistance = Long.MAX_VALUE;
        for (D9Map.Rectangle box : map.getPlatformCollisionBoxes()) {
            long boxCenterX = box.x + box.width / 2L;
            long boxCenterY = box.y + box.height / 2L;
            long dx = boxCenterX - centerX;
            long dy = boxCenterY - centerY;
            long distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = box;
            }
        }
        return best;
    }

    private int countPlatformsNearZone(int zoneX, int zoneY, int zoneWidth, int zoneHeight) {
        int margin = Math.max(map.getTileWidth(), map.getTileHeight()) * 6;
        int left = zoneX - margin;
        int right = zoneX + zoneWidth + margin;
        int top = zoneY - margin;
        int bottom = zoneY + zoneHeight + margin;
        int count = 0;
        for (D9Map.Rectangle box : map.getPlatformCollisionBoxes()) {
            if (left < box.right() && right > box.x && top < box.bottom() && bottom > box.y) {
                count++;
            }
        }
        return count;
    }

    private boolean overlapsHorizontally(int left, int right, D9Map.Rectangle box) {
        return left < box.right() && right > box.x;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private D9Map.Rectangle rect(double x, double y, int width, int height) {
        return new D9Map.Rectangle((int) Math.floor(x), (int) Math.floor(y), width, height);
    }

    public static class VerticalMoveResult {
        public final double y;
        public final double velocityY;
        public final boolean onGround;
        public final boolean landed;

        public VerticalMoveResult(double y, double velocityY, boolean onGround, boolean landed) {
            this.y = y;
            this.velocityY = velocityY;
            this.onGround = onGround;
            this.landed = landed;
        }
    }

    public static class SnapResult {
        public final int x;
        public final int y;
        public final D9Map.Rectangle platform;
        public final D9Map.Rectangle nearestPlatform;
        public final boolean foundPlatformUnderZone;
        public final int platformsNearZone;

        public SnapResult(int x, int y, D9Map.Rectangle platform, D9Map.Rectangle nearestPlatform,
                          boolean foundPlatformUnderZone, int platformsNearZone) {
            this.x = x;
            this.y = y;
            this.platform = platform;
            this.nearestPlatform = nearestPlatform;
            this.foundPlatformUnderZone = foundPlatformUnderZone;
            this.platformsNearZone = platformsNearZone;
        }
    }
}
