package com.hust.game.maps.d9;

/**
 * D9Player - Player logic cho D9
 */
public class D9Player {
    private double x, y;
    private double vx, vy;
    private final int width;
    private final int height;
    private int currentFloor = 1;
    private String lastCheckpointId = "spawn_1";
    private PlayerState state = PlayerState.STAND;
    private boolean facingRight = true;
    private boolean onGround = false;
    private double chargePower = 0;

    private final D9Settings settings;

    public enum PlayerState {
        STAND, CHARGE, JUMP
    }

    public D9Player(D9Settings settings) {
        this.settings = settings;
        this.width = settings.getPlayerHitboxWidth();
        this.height = settings.getPlayerHitboxHeight();
    }

    // Physics update
    public void update(D9CollisionManager collisionManager) {
        boolean wasOnGround = onGround;

        if (state == PlayerState.CHARGE) {
            vy = 0;
        } else if (!wasOnGround || vy != 0) {
            vy += settings.getGravity();
            if (vy > settings.getMaxFallSpeed()) {
                vy = settings.getMaxFallSpeed();
            }
        }

        x = collisionManager.resolveHorizontal(x, y, width, height, vx);

        if (vx != 0 && collisionManager.isColliding(x + Math.signum(vx), y, width, height)) {
            vx = 0;
        }

        double previousY = y;
        D9CollisionManager.VerticalMoveResult vertical = collisionManager.resolveVertical(x, previousY, width, height, vy);
        y = vertical.y;
        vy = vertical.velocityY;
        onGround = vertical.onGround || collisionManager.isOnGround(x, y, width, height);

        if (vertical.landed) {
            if (state != PlayerState.CHARGE) {
                state = PlayerState.STAND;
            }
            chargePower = 0;
        } else if (state == PlayerState.CHARGE) {
            state = PlayerState.CHARGE;
        } else if (!onGround) {
            state = PlayerState.JUMP;
        } else {
            state = PlayerState.STAND;
        }
    }

    // Input handling
    public void moveLeft() {
        vx = -settings.getMoveSpeed();
        facingRight = false;
    }

    public void moveRight() {
        vx = settings.getMoveSpeed();
        facingRight = true;
    }

    public void stopMoving() {
        vx = 0;
    }

    public boolean startCharge(D9CollisionManager collisionManager) {
        if (collisionManager != null && collisionManager.isOnGround(x, y, width, height)) {
            state = PlayerState.CHARGE;
            onGround = true;
            chargePower = settings.getMinJumpPower();
            return true;
        }
        return false;
    }

    public void updateCharge() {
        if (state == PlayerState.CHARGE) {
            chargePower += settings.getChargeRate();
            if (chargePower > settings.getMaxJumpPower()) {
                chargePower = settings.getMaxJumpPower();
            }
        }
    }

    public boolean jump() {
        if (state == PlayerState.CHARGE && chargePower > 0) {
            vy = -chargePower;
            onGround = false;
            state = PlayerState.JUMP;
            chargePower = 0;
            return true;
        }
        return false;
    }

    public void teleportTo(double x, double y, int floor) {
        this.x = x;
        this.y = y;
        this.currentFloor = floor;
        this.vx = 0;
        this.vy = 0;
        this.onGround = true;
        this.chargePower = 0;
        this.state = PlayerState.STAND;
    }

    public void resetVelocity() {
        this.vx = 0;
        this.vy = 0;
    }

    // Getters/Setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getCurrentFloor() { return currentFloor; }
    public void setCurrentFloor(int currentFloor) { this.currentFloor = currentFloor; }

    public String getLastCheckpointId() { return lastCheckpointId; }
    public void setLastCheckpointId(String lastCheckpointId) { this.lastCheckpointId = lastCheckpointId; }

    public PlayerState getState() { return state; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public boolean isOnGround() { return onGround; }
    public double getChargePower() { return chargePower; }

    public D9Map.Rectangle getBounds() {
        return new D9Map.Rectangle((int) x, (int) y, width, height);
    }
}
