package com.hust.game.maps.b1;

import com.hust.game.core.GameManager;
import com.hust.game.models.entities.B1Boss;
import com.hust.game.models.entities.B1Bullet;
import com.hust.game.models.entities.B1Player;
import com.hust.game.models.entities.Player;
import com.hust.game.maps.d9.D9QuestionBank;

import java.util.List;

/**
 * B1Engine — Logic lõi đấu trường B1: Top-down Action Shoot 'em up.
 * Tuân theo GDD V2.0 Section 6.
 *
 * <p>Hai loại quiz riêng biệt:</p>
 * <ul>
 *   <li>BulletQuiz : khi đạn boss trúng player (GDD 6.4A)</li>
 *   <li>DebuffQuiz : debuff ngẫu nhiên từ boss (GDD 6.4B)</li>
 * </ul>
 */
public class B1Engine {

    // =========================================================
    // STATE
    // =========================================================
    public enum State { PLAYING, QUIZ_MODE, DEBUFF_QUIZ_MODE, GAME_OVER, VICTORY }

    private State currentState = State.PLAYING;

    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final int  ARENA_W                = 1000;
    private static final int  ARENA_H                = 700;
    private static final long PLAYER_SHOOT_INTERVAL  = 250; // ms

    // =========================================================
    // ENTITIES
    // =========================================================
    private final B1Player player;
    private final B1Boss   boss;

    // =========================================================
    // MANAGERS
    // =========================================================
    private final B1BulletManager    bulletManager;
    private final B1CollisionManager collisionManager;
    private final B1ItemManager      itemManager;
    private final QuizManager        quizManager;
    private final B1DebuffManager    debuffManager;

    // =========================================================
    // QUIZ STATE (bullet hit)
    // =========================================================
    private D9QuestionBank.Question currentQuizQuestion;
    private int                     pendingBulletDamage = 0;

    // =========================================================
    // MISC
    // =========================================================
    private long   lastPlayerShootTime = 0;
    private String pendingWarning      = null;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public B1Engine(String bossName, int bossHp) {
        this.player = new B1Player(ARENA_W / 2.0 - 15, ARENA_H - 150, 4.5, 100, 100);

        this.boss = new B1Boss(ARENA_W / 2.0 - 35, ARENA_H / 2.0 - 35);
        this.boss.setHp(bossHp);

        this.bulletManager    = new B1BulletManager(ARENA_W, ARENA_H);
        this.collisionManager = new B1CollisionManager();
        this.itemManager      = new B1ItemManager();
        this.quizManager      = new QuizManager();

        // Đọc Thẻ B2 từ Player
        Player gp = GameManager.getInstance().getPlayer();
        boolean hasCardB2 = gp.hasStrategyNotebook(); // tạm dùng hasStrategyNotebook làm proxy cho Thẻ B2
        this.debuffManager = new B1DebuffManager(quizManager, hasCardB2);
    }

    // =========================================================
    // UPDATE
    // =========================================================
    /**
     * Gọi mỗi frame (60 FPS) khi State == PLAYING.
     *
     * @param up,down,left,right  trạng thái phím WASD
     * @param aimDX, aimDY        hướng bắn (world coords từ cursor - player position)
     */
    public void update(boolean up, boolean down, boolean left, boolean right,
                       double aimDX, double aimDY) {
        if (currentState != State.PLAYING) return;

        long now = System.currentTimeMillis();

        // 1. Đọc debuff state ảnh hưởng tới player
        B1DebuffManager.DebuffState debuff = debuffManager.getState();
        double speedMult = debuff.getSpeedMultiplier();

        // 2. Di chuyển player (có nerf speed nếu bị debuff)
        double dx = 0, dy = 0;
        if (up)    dy -= 1;
        if (down)  dy += 1;
        if (left)  dx -= 1;
        if (right) dx += 1;

        // Nhân tốc độ gốc với speed multiplier từ debuff
        double savedSpeed = player.getMoveSpeed();
        player.setMoveSpeed(savedSpeed * speedMult);
        player.move(dx, dy);
        player.setMoveSpeed(savedSpeed); // restore
        player.update();
        clampPlayerToBounds();

        // 3. Player bắn chưởng (chỉ nếu không bị Camera lock)
        boolean canShoot = !debuff.shootLocked;
        if (canShoot && now - lastPlayerShootTime >= PLAYER_SHOOT_INTERVAL) {
            double len = Math.sqrt(aimDX * aimDX + aimDY * aimDY);
            if (len > 1.0) {
                int dmg = (int)(calcPlayerDamage() * debuff.getDamageMultiplier());
                bulletManager.playerSpawnBullet(
                        player.getX() + 15, player.getY() + 15,
                        aimDX, aimDY, 7.0, 6, dmg);
                lastPlayerShootTime = now;
            }
        }

        // 4. Update item manager (duration effects & slow on boss)
        itemManager.update(player, boss, now);

        // 5. Update boss
        boss.update();
        if (boss.canAttack()) spawnBossAttack();

        // 6. Update bullets
        bulletManager.update();

        // 7. Va chạm
        boolean shieldOn = isShieldActive();
        B1CollisionManager.CollisionResult result =
                collisionManager.checkCollisions(player, boss, bulletManager.getBullets(), shieldOn);

        // Xóa đạn player đã trúng boss (damage đã apply trong CollisionManager)
        for (B1Bullet hit : result.bulletsHitBoss) {
            bulletManager.removeBullet(hit);
        }

        // Đạn boss trúng player → Bullet Quiz (GDD 6.4A)
        if (!result.bulletsHitPlayer.isEmpty()) {
            B1Bullet hitBullet = result.bulletsHitPlayer.get(0);
            bulletManager.removeBullet(hitBullet);
            triggerBulletQuiz(hitBullet.getDamage());
            return; // Pause update frame này
        }

        // 8. Debuff quiz check (GDD 6.4B)
        B1DebuffManager.PendingDebuffQuiz dq = debuffManager.update(now);
        if (dq != null) {
            currentState = State.DEBUFF_QUIZ_MODE;
            return;
        }

        // 9. Check end conditions
        checkEndConditions();
    }

    // =========================================================
    // BOSS ATTACK PATTERNS
    // =========================================================
    private void spawnBossAttack() {
        double bx = boss.getX() + boss.getWidth()  / 2.0;
        double by = boss.getY() + boss.getHeight() / 2.0;
        int phase = boss.getPhase();

        switch (phase) {
            case 1:
                bulletManager.bossSpawnSpreadBullets(bx, by, 8,  3.5, 8, 10);
                break;
            case 2:
                bulletManager.bossSpawnSpreadBullets(bx, by, 12, 4.5, 8, 15);
                bulletManager.bossSpawnBullet(bx, by, player.getX() + 15, player.getY() + 15, 5.0, 10, 15);
                break;
            case 3:
                bulletManager.bossSpawnSpreadBullets(bx, by, 16, 5.5, 8, 20);
                bulletManager.bossSpawnBullet(bx, by, player.getX() + 15, player.getY() + 15, 6.0, 10, 20);
                bulletManager.bossSpawnBullet(bx, by, player.getX() + 15, player.getY() + 15, 6.5, 10, 20);
                break;
        }
    }

    // =========================================================
    // DAMAGE CALCULATION (GDD 6.2)
    // =========================================================
    /** Sát thương gốc. Nếu Level<8 hoặc Discipline<80 → -80%. */
    private int calcPlayerDamage() {
        Player p = GameManager.getInstance().getPlayer();
        int base = 10;
        if (p.getLevel() >= 8 && p.getDisciplineScore() >= 80) return base;
        return Math.max(1, (int)(base * 0.2));
    }

    // =========================================================
    // BULLET QUIZ (GDD 6.4A)
    // =========================================================
    private void triggerBulletQuiz(int bulletDamage) {
        currentState        = State.QUIZ_MODE;
        currentQuizQuestion = quizManager.getRandomQuestion();
        pendingBulletDamage = bulletDamage;
    }

    /** Gọi từ B1Panel sau khi người chơi chọn đáp án cho bullet quiz */
    public void answerQuiz(int selectedIndex) {
        boolean correct = quizManager.checkAnswer(currentQuizQuestion, selectedIndex);
        Player  p       = GameManager.getInstance().getPlayer();

        if (correct) {
            // Phản đòn boss
            int counterDmg = 20 + p.getSolutionSkill() / 5;
            boss.takeDamage(counterDmg);
            System.out.println("✅ Kháng đạn thành công! Phản đòn: -" + counterDmg + " HP Boss");
        } else {
            // Nhận sát thương + trừ energy
            int dmg = Math.max(5, Math.min(15, pendingBulletDamage));
            player.takeDamage(dmg);
            p.setEnergy(p.getEnergy() - Math.max(1, dmg / 2));
            System.out.println("❌ Trả lời sai! -" + dmg + " HP, -" + Math.max(1, dmg/2) + " Energy");
        }

        checkEndConditions();
        // Nếu game vẫn chạy → về PLAYING
        if (currentState == State.QUIZ_MODE) currentState = State.PLAYING;
    }

    // =========================================================
    // DEBUFF QUIZ (GDD 6.4B)
    // =========================================================
    /** Gọi từ B1Panel sau khi người chơi chọn đáp án cho debuff quiz */
    public void answerDebuffQuiz(int selectedIndex) {
        Player p = GameManager.getInstance().getPlayer();
        debuffManager.answerDebuffQuiz(selectedIndex, p, player);

        checkEndConditions();
        if (currentState == State.DEBUFF_QUIZ_MODE) currentState = State.PLAYING;
    }

    // =========================================================
    // ITEM ACTIVATION (GDD 6.3)
    // =========================================================
    public boolean activateItem(B1ItemManager.ItemType type) {
        long   now     = System.currentTimeMillis();
        Player gPlayer = GameManager.getInstance().getPlayer();

        if (!checkItemRequirement(type, gPlayer)) {
            pendingWarning = "Trình độ học thuật chưa đủ để thấu hiểu và sử dụng bảo vật này!";
            return false;
        }

        boolean success = itemManager.activateItem(type, player, boss, gPlayer, now);
        if (!success) pendingWarning = "Vật phẩm đang hồi chiêu!";
        return success;
    }

    /** Kiểm tra điều kiện chỉ số theo GDD V2.0 Section 6.3 */
    private boolean checkItemRequirement(B1ItemManager.ItemType type, Player p) {
        switch (type) {
            case GIAO_TRINH_OOP: return p.getSolutionSkill() >= 70;
            case SO_GHI_CHEP:    return p.getDisciplineScore() >= 80;
            case HOC_BONG_KKHT:  return p.getLevel() >= 8 && p.getFinance() >= 100;
            default:             return false;
        }
    }

    // =========================================================
    // UTILITY
    // =========================================================
    private void checkEndConditions() {
        if (boss.getHp() <= 0)   currentState = State.VICTORY;
        else if (player.getHp() <= 0) currentState = State.GAME_OVER;
    }

    private void clampPlayerToBounds() {
        int margin = 5;
        if (player.getX() < margin)                   player.setX(margin);
        if (player.getX() > ARENA_W - 30 - margin)    player.setX(ARENA_W - 30 - margin);
        if (player.getY() < margin)                   player.setY(margin);
        if (player.getY() > ARENA_H - 30 - margin)    player.setY(ARENA_H - 30 - margin);
    }

    public boolean isShieldActive() {
        return itemManager.isItemActive(B1ItemManager.ItemType.SO_GHI_CHEP, System.currentTimeMillis());
    }

    // =========================================================
    // GETTERS
    // =========================================================
    public State    getCurrentState()      { return currentState; }
    public B1Player getPlayer()            { return player; }
    public B1Boss   getBoss()              { return boss; }
    public List<B1Bullet> getBullets()     { return bulletManager.getBullets(); }
    public D9QuestionBank.Question getCurrentQuizQuestion() { return currentQuizQuestion; }
    public B1ItemManager  getItemManager() { return itemManager; }
    public B1DebuffManager getDebuffManager() { return debuffManager; }

    /** Lấy và xóa cảnh báo pending */
    public String consumeWarning() {
        String w = pendingWarning;
        pendingWarning = null;
        return w;
    }
}
