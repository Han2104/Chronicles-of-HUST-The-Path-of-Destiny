package com.hust.game.models.entities;

public class B1Player {
    private double x;
    private double y;
    
    private double vx;
    private double vy;
    
    private double moveSpeed;
    
    private int hp;
    private int maxHp;
    
    private int energy;
    private int maxEnergy;

    public B1Player(double startX, double startY, double moveSpeed, int maxHp, int maxEnergy) {
        this.x = startX;
        this.y = startY;
        this.moveSpeed = moveSpeed;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.maxEnergy = maxEnergy;
        this.energy = maxEnergy;
    }

    public void update() {
        this.x += this.vx;
        this.y += this.vy;
    }

    public void move(double dx, double dy) {
        // Chuẩn hóa (Normalize) vector để tránh việc đi chéo nhanh hơn đi thẳng
        if (dx != 0 && dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            dx = dx / length;
            dy = dy / length;
        }
        
        this.vx = dx * moveSpeed;
        this.vy = dy * moveSpeed;
    }

    public void takeDamage(int amount) {
        if (amount > 0) {
            this.hp -= amount;
            if (this.hp < 0) {
                this.hp = 0;
            }
        }
    }

    public void heal(int amount) {
        if (amount > 0) {
            this.hp += amount;
            if (this.hp > this.maxHp) {
                this.hp = this.maxHp;
            }
        }
    }

    // --- Getters & Setters ---

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getVx() { return vx; }
    public double getVy() { return vy; }

    public double getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(double moveSpeed) { this.moveSpeed = moveSpeed; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = Math.min(Math.max(hp, 0), maxHp); }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = Math.min(Math.max(energy, 0), maxEnergy); }

    public int getMaxEnergy() { return maxEnergy; }
    public void setMaxEnergy(int maxEnergy) { this.maxEnergy = maxEnergy; }
}
