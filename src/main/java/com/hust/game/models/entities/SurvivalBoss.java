package com.hust.game.models.entities;

import java.awt.Rectangle;

public class SurvivalBoss {
    public double x;
    public double y;
    public double speed;
    public int width;
    public int height;
    public int hp;
    public int maxHp;
    public String name;
    
    public SurvivalBoss(String name, double startX, double startY, int maxHp) {
        this.name = name;
        this.x = startX;
        this.y = startY;
        this.hp = maxHp;
        this.maxHp = maxHp;
        this.speed = 2.0;
        this.width = 60;
        this.height = 60;
    }
    
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
}
