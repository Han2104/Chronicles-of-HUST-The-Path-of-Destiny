package com.hust.game.maps.b1;

import com.hust.game.models.entities.B1Bullet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * B1BulletManager — Quản lý tất cả đạn trong đấu trường B1.
 */
public class B1BulletManager {
    private List<B1Bullet> bullets;
    private int mapWidth;
    private int mapHeight;

    public B1BulletManager(int mapWidth, int mapHeight) {
        this.bullets   = new ArrayList<>();
        this.mapWidth  = mapWidth;
        this.mapHeight = mapHeight;
    }

    // Boss bắn nhắm thẳng vào Player
    public void bossSpawnBullet(double bossX, double bossY,
                                 double targetX, double targetY,
                                 double speed, int radius, int damage) {
        double dx = targetX - bossX;
        double dy = targetY - bossY;
        bullets.add(new B1Bullet(bossX, bossY, dx, dy, speed, radius, damage, false));
    }

    // Boss bắn toả tròn (spread shot)
    public void bossSpawnSpreadBullets(double bossX, double bossY,
                                        int numBullets, double speed,
                                        int radius, int damage, int unusedLegacyParam) {
        double angleStep = (2 * Math.PI) / numBullets;
        for (int i = 0; i < numBullets; i++) {
            double angle = i * angleStep;
            double dx    = Math.cos(angle);
            double dy    = Math.sin(angle);
            bullets.add(new B1Bullet(bossX, bossY, dx, dy, speed, radius, damage, false));
        }
    }

    // Overload ngắn gọn (không có legacyParam)
    public void bossSpawnSpreadBullets(double bossX, double bossY,
                                        int numBullets, double speed, int radius, int damage) {
        bossSpawnSpreadBullets(bossX, bossY, numBullets, speed, radius, damage, 0);
    }

    // Player bắn chưởng
    public void playerSpawnBullet(double playerX, double playerY,
                                   double dx, double dy,
                                   double speed, int radius, int damage) {
        bullets.add(new B1Bullet(playerX, playerY, dx, dy, speed, radius, damage, true));
    }

    public void update() {
        Iterator<B1Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            B1Bullet b = it.next();
            b.update();
            if (isOutOfBounds(b)) it.remove();
        }
    }

    private boolean isOutOfBounds(B1Bullet b) {
        return b.getX() < -60 || b.getX() > mapWidth + 60
            || b.getY() < -60 || b.getY() > mapHeight + 60;
    }

    public List<B1Bullet> getBullets() { return bullets; }

    public void removeBullet(B1Bullet bullet) { bullets.remove(bullet); }

    public void clearBullets() { bullets.clear(); }
}
