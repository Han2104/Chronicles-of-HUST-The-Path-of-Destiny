package com.hust.game.models.entities;

import java.awt.Rectangle;

public class B1Bullet {
    private double x;
    private double y;
    private double dx;
    private double dy;
    private double speed;
    private int radius;
    private int damage;
    private boolean isPlayerBullet;

    public B1Bullet(double x, double y, double dx, double dy, double speed, int radius, int damage, boolean isPlayerBullet) {
        this.x = x;
        this.y = y;
        
        // Chuẩn hóa vector hướng đi (Normalize)
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length != 0) {
            this.dx = dx / length;
            this.dy = dy / length;
        } else {
            this.dx = 0;
            this.dy = 1; // Default bay xuống dưới nếu không có hướng
        }
        
        this.speed = speed;
        this.radius = radius;
        this.damage = damage;
        this.isPlayerBullet = isPlayerBullet;
    }

    public void update() {
        this.x += this.dx * this.speed;
        this.y += this.dy * this.speed;
    }

    // Hỗ trợ va chạm Rectangle AABB
    public Rectangle getBounds() {
        return new Rectangle((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);
    }
    
    // --- Getters ---
    public double getX() { return x; }
    public double getY() { return y; }
    public int getRadius() { return radius; }
    public int getDamage() { return damage; }
    public boolean isPlayerBullet() { return isPlayerBullet; }
}
