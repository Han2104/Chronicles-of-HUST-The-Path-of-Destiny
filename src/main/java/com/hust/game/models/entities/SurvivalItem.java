package com.hust.game.models.entities;

import java.awt.Rectangle;

public class SurvivalItem {
    public double x;
    public double y;
    public int size;
    public int healAmount;
    
    public SurvivalItem(double x, double y) {
        this.x = x;
        this.y = y;
        this.size = 20;
        this.healAmount = 10;
    }
    
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, size, size);
    }
}
