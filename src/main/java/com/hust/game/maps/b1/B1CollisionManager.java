package com.hust.game.maps.b1;

import com.hust.game.models.entities.B1Boss;
import com.hust.game.models.entities.B1Bullet;
import com.hust.game.models.entities.B1Player;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * B1CollisionManager — Kiểm tra va chạm giữa player, boss, và đạn.
 * Hỗ trợ shield state từ item SO_GHI_CHEP.
 */
public class B1CollisionManager {

    public static class CollisionResult {
        public boolean       playerHitBoss;
        public List<B1Bullet> bulletsHitPlayer;
        public List<B1Bullet> bulletsHitBoss;

        public CollisionResult() {
            playerHitBoss    = false;
            bulletsHitPlayer = new ArrayList<>();
            bulletsHitBoss   = new ArrayList<>();
        }
    }

    /**
     * @param shieldActive nếu true, đạn boss sẽ không được thêm vào bulletsHitPlayer
     */
    public CollisionResult checkCollisions(B1Player player, B1Boss boss,
                                            List<B1Bullet> bullets, boolean shieldActive) {
        CollisionResult result = new CollisionResult();
        if (player == null || boss == null) return result;

        Rectangle playerBounds = new Rectangle((int) player.getX(), (int) player.getY(), 30, 30);
        Rectangle bossBounds   = new Rectangle((int) boss.getX(), (int) boss.getY(),
                boss.getWidth(), boss.getHeight());

        // Player vs Boss body
        if (playerBounds.intersects(bossBounds)) {
            result.playerHitBoss = true;
        }

        if (bullets == null) return result;

        for (B1Bullet bullet : bullets) {
            Rectangle bulletBounds = bullet.getBounds();

            // Đạn Boss trúng Player (bỏ qua nếu shield đang active)
            if (!bullet.isPlayerBullet() && !shieldActive
                    && playerBounds.intersects(bulletBounds)) {
                result.bulletsHitPlayer.add(bullet);
            }

            // Đạn Player trúng Boss
            if (bullet.isPlayerBullet() && bossBounds.intersects(bulletBounds)) {
                // Apply damage trực tiếp vào boss
                boss.takeDamage(bullet.getDamage());
                result.bulletsHitBoss.add(bullet);
            }
        }

        return result;
    }

    // Overload backwards-compat (không có shield)
    public CollisionResult checkCollisions(B1Player player, B1Boss boss, List<B1Bullet> bullets) {
        return checkCollisions(player, boss, bullets, false);
    }
}
