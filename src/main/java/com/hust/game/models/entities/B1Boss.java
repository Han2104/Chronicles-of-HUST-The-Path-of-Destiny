package com.hust.game.models.entities;

/**
 * B1Boss — Boss duy nhất: Đại Giáo Sư Tổng Hợp (HUST OVERLORD).
 * HP: 600. 3 phase với pattern di chuyển và tốc độ bắn khác nhau.
 */
public class B1Boss {

    private double x;
    private double y;
    private double baseSpeed;
    private double currentSpeedMultiplier = 1.0;
    private long   slowEndTime            = 0;

    private int width  = 70;
    private int height = 70;

    private int hp;
    private int maxHp;
    private int phase = 1;

    private long lastAttackTime;
    private long attackCooldown;

    // Sine wave movement tracking
    private double startX;
    private double startY;
    private double angle = 0;

    // 8-directional movement tracking
    private int moveTimer = 0;
    private double dirX = 0;
    private double dirY = 0;
    private static final double[][] DIRECTIONS = {
        {0, -1}, {0, 1}, {-1, 0}, {1, 0}, // Up, Down, Left, Right
        {-0.707, -0.707}, {0.707, -0.707}, {-0.707, 0.707}, {0.707, 0.707} // Diagonals
    };

    // =========================================================
    public B1Boss(double startX, double startY) {
        this.startX = startX;
        this.startY = startY;
        this.x      = startX;
        this.y      = startY;

        this.maxHp         = 600;
        this.hp            = maxHp;
        this.baseSpeed     = 2.0;
        this.attackCooldown = 1500;
        this.lastAttackTime = System.currentTimeMillis();
    }

    // =========================================================
    // UPDATE
    // =========================================================
    public void update() {
        // Kiểm tra hết slow debuff
        long now = System.currentTimeMillis();
        if (slowEndTime > 0 && now >= slowEndTime) {
            currentSpeedMultiplier = 1.0;
            slowEndTime            = 0;
            System.out.println("⏳ Boss hết bị chậm.");
        }

        updatePhase();
        updateMovement();
    }

    private void updatePhase() {
        if (hp <= maxHp * 0.3 && phase < 3) {
            phase           = 3;
            baseSpeed       = 4.0;
            attackCooldown  = 500;
            System.out.println("☠ Boss Phase 3: BERSERK!");
        } else if (hp <= maxHp * 0.6 && phase < 2) {
            phase           = 2;
            baseSpeed       = 3.0;
            attackCooldown  = 1000;
            System.out.println("⚡ Boss Phase 2: AGGRESSIVE!");
        }
    }

    private void updateMovement() {
        double effectiveSpeed = baseSpeed * currentSpeedMultiplier;
        
        moveTimer--;
        if (moveTimer <= 0) {
            // Chọn ngẫu nhiên 1 trong 8 hướng di chuyển
            int dirIndex = (int)(Math.random() * 8);
            if (Math.random() < 0.15) {
                // Tỷ lệ đứng yên một khoảng thời gian
                dirX = 0;
                dirY = 0;
            } else {
                double[] dir = DIRECTIONS[dirIndex];
                dirX = dir[0];
                dirY = dir[1];
            }
            // Đổi hướng sau 1 đến 2.5 giây (tương đương 60 đến 150 frames ở 60fps)
            moveTimer = 60 + (int)(Math.random() * 90);
        }

        // Cập nhật vị trí mới
        x += dirX * effectiveSpeed;
        y += dirY * effectiveSpeed;

        // Giới hạn Boss di chuyển trong ranh giới map (Arena 1000x700, kích thước boss 70x70)
        int arenaW = 1000;
        int arenaH = 700;
        int bossW = 70;
        int bossH = 70;
        int padding = 50;

        if (x < padding) { 
            x = padding; 
            dirX = -dirX; 
        }
        if (x > arenaW - bossW - padding) { 
            x = arenaW - bossW - padding; 
            dirX = -dirX; 
        }
        if (y < padding) { 
            y = padding; 
            dirY = -dirY; 
        }
        // Giới hạn trục Y của boss ở phần nửa trên đấu trường (tránh đè lên player)
        if (y > arenaH / 2.0 + 50) { 
            y = arenaH / 2.0 + 50; 
            dirY = -dirY; 
        }
    }

    // =========================================================
    // ATTACK
    // =========================================================
    public boolean canAttack() {
        long now         = System.currentTimeMillis();
        long effectiveCd = (long)(attackCooldown / currentSpeedMultiplier);
        if (now - lastAttackTime >= effectiveCd) {
            lastAttackTime = now;
            return true;
        }
        return false;
    }

    // =========================================================
    // DEBUFFS
    // =========================================================
    /**
     * Áp dụng slow debuff: tốc độ di chuyển và tấn công bị nhân với multiplier trong durationMs.
     * @param multiplier    VD: 0.8 để giảm 20%
     * @param durationMs    thời gian hiệu lực (ms)
     */
    public void applySlowDebuff(double multiplier, long durationMs) {
        this.currentSpeedMultiplier = multiplier;
        this.slowEndTime            = System.currentTimeMillis() + durationMs;
        System.out.println("🐢 Boss bị chậm " + (int)((1 - multiplier)*100) + "% trong " + durationMs/1000 + "s");
    }

    // =========================================================
    // DAMAGE
    // =========================================================
    public void takeDamage(int amount) {
        if (amount > 0) {
            this.hp = Math.max(0, this.hp - amount);
        }
    }

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================
    public double getX()    { return x; }
    public void   setX(double x) { this.x = x; }

    public double getY()    { return y; }
    public void   setY(double y) { this.y = y; }

    public int  getWidth()  { return width; }
    public int  getHeight() { return height; }

    public int  getHp()     { return hp; }
    public void setHp(int hp) { this.hp = Math.min(Math.max(hp, 0), maxHp); }

    public int  getMaxHp()  { return maxHp; }
    public int  getPhase()  { return phase; }

    public double getCurrentSpeedMultiplier() { return currentSpeedMultiplier; }
}
