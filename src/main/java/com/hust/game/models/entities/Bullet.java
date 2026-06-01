package com.hust.game.models.entities;

import java.awt.Rectangle;
import java.awt.Color;

public class Bullet {
    public double x;
    public double y;
    public double dx;
    public double dy;
    public double speed;
    public int radius;
    public boolean isPlayerBullet;
    public int damage;
    public Color color;

    public Bullet(double x, double y, double dx, double dy, double speed, int radius, boolean isPlayerBullet, int damage, Color color) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.radius = radius;
        this.isPlayerBullet = isPlayerBullet;
        this.damage = damage;
        this.color = color;
        
        // Normalize direction vector
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length != 0) {
            this.dx = dx / length;
            this.dy = dy / length;
        } else {
            this.dx = 0;
            this.dy = 0;
        }
    }

    public void update() {
        this.x += dx * speed;
        this.y += dy * speed;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);
    }
}
