package com.hust.game.models.entities;

import java.awt.Rectangle;

public class SurvivalPlayer {
    public double x;
    public double y;
    public double speed;
    public int width;
    public int height;
    
    public SurvivalPlayer(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 5.0;
        this.width = 30;
        this.height = 30;
    }
    
    public void move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        // Simple bounds checking could be done here, or in engine.
        // Assuming 800x600 window size for now
        if (this.x < 0) this.x = 0;
        if (this.y < 0) this.y = 0;
        if (this.x > 800 - this.width) this.x = 800 - this.width;
        if (this.y > 600 - this.height) this.y = 600 - this.height;
    }
    
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
}
