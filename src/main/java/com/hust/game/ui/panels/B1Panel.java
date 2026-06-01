package com.hust.game.ui.panels;

import com.hust.game.core.GameManager;
import com.hust.game.maps.b1.B1Engine;
import com.hust.game.maps.b1.B1ItemManager;
import com.hust.game.models.entities.B1Bullet;
import com.hust.game.models.entities.Player;
import com.hust.game.ui.GameWindow;
import com.hust.game.util.AssetLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * B1Panel — Màn hình đấu trường B1: Top-down Action Shoot 'em up.
 * Đăng ký trong GameWindow với key "B1_ARENA".
 */
public class B1Panel extends JPanel implements ActionListener, KeyListener, MouseMotionListener {

    // === References ===
    private GameWindow window;
    private StatsPanel statsPanel;

    // === Engine ===
    private B1Engine engine;
    private Timer gameTimer;

    // === Input ===
    private boolean upPressed    = false;
    private boolean downPressed  = false;
    private boolean leftPressed  = false;
    private boolean rightPressed = false;

    // Mouse aim (world coords relative to player)
    private int mouseX = 500;
    private int mouseY = 300;

    // === Camera ===
    private int cameraX = 0;
    private int cameraY = 0;

    // === Sprites (Nhân vật Vũ) ===
    private BufferedImage[][] moveFrames;  // [dir 0=up,1=down,2=left,3=right][frame 0-3]
    private BufferedImage[]   standFront;  // idle front (1-3)
    private BufferedImage     bossSprite;
    private BufferedImage     backgroundImage;

    private int    animDir      = 1;   // default: facing down
    private int    animFrame    = 0;
    private int    animTick     = 0;
    private static final int ANIM_SPEED = 8; // frames between animation advances

    // === State cảnh báo ===
    private String warningMessage = "";
    private int    warningTimer   = 0;

    // === Death counter (cho ENDING 3) ===
    private int deathCount = 0;

    // === Arena dimensions ===
    private static final int ARENA_W = 1000;
    private static final int ARENA_H = 700;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public B1Panel(GameWindow window, StatsPanel statsPanel) {
        this.window     = window;
        this.statsPanel = statsPanel;

        setBackground(Color.BLACK);
        setFocusable(true);
        setRequestFocusEnabled(true);
        setFocusTraversalKeysEnabled(false);

        addKeyListener(this);
        addMouseMotionListener(this);

        loadSprites();
        resetEngine();

        // Game loop at 60 FPS
        gameTimer = new Timer(1000 / 60, this);
    }

    // =========================================================
    // ENGINE INIT / RESET
    // =========================================================
    private void resetEngine() {
        engine = new B1Engine("Đại Giáo Sư Tổng Hợp", 600);
        deathCount = 0;
        upPressed = downPressed = leftPressed = rightPressed = false;
    }

    // =========================================================
    // SPRITE LOADING
    // =========================================================
    private void loadSprites() {
        moveFrames = new BufferedImage[4][4];
        String[] dirs = {"up", "down", "left", "right"};
        for (int d = 0; d < 4; d++) {
            for (int f = 1; f <= 4; f++) {
                String path = "assets/Vu/character_move_" + dirs[d] + " (" + f + ").png";
                moveFrames[d][f - 1] = AssetLoader.loadImage(path);
            }
        }

        standFront = new BufferedImage[3];
        for (int f = 1; f <= 3; f++) {
            standFront[f - 1] = AssetLoader.loadImage("assets/Vu/character_stand_front (" + f + ").png");
        }

        bossSprite = AssetLoader.loadImage("assets/boss.png");
        backgroundImage = AssetLoader.loadImage("assets/b1_map.png");
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================
    public void onShown() {
        resetEngine();
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
        setFocusable(true);
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    public void onHidden() {
        upPressed = downPressed = leftPressed = rightPressed = false;
        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    // =========================================================
    // GAME LOOP
    // =========================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        if (engine.getCurrentState() == B1Engine.State.QUIZ_MODE) {
            gameTimer.stop();
            showQuizDialog();
            return;
        }

        if (engine.getCurrentState() == B1Engine.State.DEBUFF_QUIZ_MODE) {
            gameTimer.stop();
            showDebuffQuizDialog();
            return;
        }

        if (engine.getCurrentState() == B1Engine.State.PLAYING) {
            // Tính hướng bắn từ vị trí chuột (world coords)
            double aimDX = (mouseX + cameraX) - engine.getPlayer().getX() - 15;
            double aimDY = (mouseY + cameraY) - engine.getPlayer().getY() - 15;

            engine.update(upPressed, downPressed, leftPressed, rightPressed, aimDX, aimDY);

            // Kiểm tra warning từ engine
            String warn = engine.consumeWarning();
            if (warn != null) {
                warningMessage = warn;
                warningTimer   = 120; // hiện 2 giây (120 frames)
            }
        } else if (engine.getCurrentState() == B1Engine.State.GAME_OVER) {
            gameTimer.stop();
            handleGameOver();
            return;
        } else if (engine.getCurrentState() == B1Engine.State.VICTORY) {
            gameTimer.stop();
            handleVictory();
            return;
        }

        updateAnimation();
        updateCamera();

        if (warningTimer > 0) warningTimer--;

        repaint();
    }

    // =========================================================
    // QUIZ DIALOG
    // =========================================================
    private void showQuizDialog() {
        com.hust.game.maps.d9.D9QuestionBank.Question q = engine.getCurrentQuizQuestion();
        if (q == null) {
            engine.answerQuiz(-1);
        } else {
            int choice = JOptionPane.showOptionDialog(this,
                    q.question,
                    "⚡ TRÚNG ĐẠN! Trả lời để kháng đòn!",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    q.options,
                    q.options[0]);
            engine.answerQuiz(choice);
        }

        // Reset input để tránh nhân vật trôi
        upPressed = downPressed = leftPressed = rightPressed = false;

        // Resume nếu game chưa kết thúc
        if (engine.getCurrentState() == B1Engine.State.PLAYING) {
            gameTimer.start();
        } else if (engine.getCurrentState() == B1Engine.State.GAME_OVER) {
            handleGameOver();
        } else if (engine.getCurrentState() == B1Engine.State.VICTORY) {
            handleVictory();
        }
    }

    // =========================================================
    // DEBUFF QUIZ DIALOG (GDD 6.4B)
    // =========================================================
    private void showDebuffQuizDialog() {
        com.hust.game.maps.b1.B1DebuffManager.PendingDebuffQuiz dq =
                engine.getDebuffManager().getPendingQuiz();

        if (dq == null || dq.question == null) {
            engine.answerDebuffQuiz(-1);
        } else {
            String title;
            switch (dq.type) {
                case CAMERA_SURVEILLANCE:
                    title = "📷 QUÉT CAMERA GIÁM SÁT! (Encapsulation) — Sai: Khóa bắn 6s";
                    break;
                case XOAY_AOE:
                    title = "🌀 XOÁY CƠ HỌC ĐẠI CƯƠNG! (Polymorphism) — Sai: Speed -50% 5s";
                    break;
                case TRUC_XUAT:
                    title = "📋 TRỰC XUẤT ĐIỂM RÈN LUYỆN! — Sai: -20 Điểm Rèn Luyện";
                    break;
                default:
                    title = "⚠ BOSS DEBUFF!";
            }

            com.hust.game.maps.d9.D9QuestionBank.Question q = dq.question;
            int choice = JOptionPane.showOptionDialog(this,
                    q.question,
                    title,
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    q.options,
                    q.options[0]);
            engine.answerDebuffQuiz(choice);
        }

        upPressed = downPressed = leftPressed = rightPressed = false;

        if (engine.getCurrentState() == B1Engine.State.PLAYING ||
            engine.getCurrentState() == B1Engine.State.DEBUFF_QUIZ_MODE) {
            // DEBUFF_QUIZ_MODE được reset về PLAYING bên trong engine
            gameTimer.start();
        } else if (engine.getCurrentState() == B1Engine.State.GAME_OVER) {
            handleGameOver();
        } else if (engine.getCurrentState() == B1Engine.State.VICTORY) {
            handleVictory();
        }
    }

    // =========================================================
    // GAME OVER / VICTORY
    // =========================================================
    private void handleGameOver() {
        deathCount++;
        if (deathCount >= 3) {
            // ENDING 3: Bad Ending
            JOptionPane.showMessageDialog(this,
                    "⚠️ ENDING 3: BỎ CUỘC\n\nVũ đã ngã xuống 3 lần tại đấu trường B1.\n" +
                    "Không chịu nổi áp lực, Vũ buộc phải nghỉ học...\n" +
                    "Quay về quê hương trồng ngô chăn lợn.",
                    "💀 BAD ENDING", JOptionPane.ERROR_MESSAGE);
            navigateToWorldMap();
        } else {
            int choice = JOptionPane.showConfirmDialog(this,
                    "⚔️ GAME OVER!\n\nVũ đã bị đánh bại! (Lần " + deathCount + "/3)\n" +
                    "Còn " + (3 - deathCount) + " lần nữa sẽ kích hoạt Bad Ending.\n\nThử lại?",
                    "💀 GAME OVER", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                resetEngine();
                gameTimer.start();
            } else {
                navigateToLobby();
            }
        }
    }

    private void handleVictory() {
        Player p = GameManager.getInstance().getPlayer();
        String endingTitle;
        String endingMsg;

        if (p.getLevel() >= 10 && p.getDisciplineScore() >= 80) {
            endingTitle = "🏆 TRUE ENDING: VỊ THẾ QUYỀN LỰC";
            endingMsg = "Vũ đã tiêu diệt Đại Giáo Sư Tổng Hợp!\n\n" +
                    "Với Cấp độ " + p.getLevel() + " và Điểm Rèn Luyện " + p.getDisciplineScore() + "/100,\n" +
                    "Vũ nhận được lời mời làm việc từ các tập đoàn công nghệ lớn.\n\n" +
                    "\"HỌC TẬP CHÍNH LÀ ĐỊNH MỆNH!\"";
        } else {
            endingTitle = "🎓 ENDING 2: TỐT NGHIỆP BÌNH THƯỜNG";
            endingMsg = "Vũ đã chiến thắng Boss!\n\n" +
                    "Nhưng chỉ số chưa đủ lý tưởng (Cấp: " + p.getLevel() + ", Rèn Luyện: " + p.getDisciplineScore() + ").\n" +
                    "Vũ có cuộc sống và công việc bình thường.\n\n" +
                    "Hãy thử lại để đạt True Ending!";
        }

        // Thưởng EXP
        p.addExp(300);
        statsPanel.updateStats();

        JOptionPane.showMessageDialog(this, endingMsg, endingTitle, JOptionPane.INFORMATION_MESSAGE);
        navigateToWorldMap();
    }

    private void navigateToLobby() {
        if (window != null) window.showPanel("MAP_B1");
        if (statsPanel != null) statsPanel.updateStats();
    }

    private void navigateToWorldMap() {
        GameManager.getInstance().switchMap(0);
        if (window != null) window.showPanel("WORLD_MAP");
        if (statsPanel != null) statsPanel.updateStats();
    }

    // =========================================================
    // ANIMATION & CAMERA
    // =========================================================
    private void updateAnimation() {
        boolean moving = upPressed || downPressed || leftPressed || rightPressed;
        if (moving) {
            if (upPressed)         animDir = 0;
            else if (downPressed)  animDir = 1;
            else if (leftPressed)  animDir = 2;
            else if (rightPressed) animDir = 3;

            animTick++;
            if (animTick >= ANIM_SPEED) {
                animTick = 0;
                animFrame = (animFrame + 1) % 4;
            }
        } else {
            animFrame = 0;
            animTick  = 0;
        }
    }

    private void updateCamera() {
        int screenW = getWidth();
        int screenH = getHeight();
        if (screenW > 0 && screenH > 0) {
            cameraX = (int) engine.getPlayer().getX() - screenW / 2 + 15;
            cameraY = (int) engine.getPlayer().getY() - screenH / 2 + 15;
        }
    }

    // =========================================================
    // RENDERING
    // =========================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // -- Áp dụng Camera --
        g2d.translate(-cameraX, -cameraY);
        renderWorld(g2d);
        g2d.translate(cameraX, cameraY);

        // -- HUD (không bị camera ảnh hưởng) --
        renderHUD(g2d);
    }

    private void renderWorld(Graphics2D g2d) {
        // Nền đấu trường
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, ARENA_W, ARENA_H, this);
        } else {
            g2d.setColor(new Color(20, 20, 35));
            g2d.fillRect(0, 0, ARENA_W, ARENA_H);

            // Lưới mờ dự phòng
            g2d.setColor(new Color(40, 40, 60, 100));
            g2d.setStroke(new BasicStroke(1));
            for (int i = 0; i < ARENA_W; i += 80) {
                g2d.drawLine(i, 0, i, ARENA_H);
            }
            for (int i = 0; i < ARENA_H; i += 80) {
                g2d.drawLine(0, i, ARENA_W, i);
            }
        }

        // Viền đấu trường
        g2d.setColor(new Color(80, 60, 120));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(2, 2, ARENA_W - 4, ARENA_H - 4);

        // Vẽ Boss
        renderBoss(g2d);

        // Vẽ Đạn
        renderBullets(g2d);

        // Vẽ Player
        renderPlayer(g2d);

        // Shield effect nếu item SO_GHI_CHEP đang active
        if (engine.isShieldActive()) {
            float alpha = 0.4f + 0.3f * (float) Math.sin(System.currentTimeMillis() * 0.01);
            g2d.setColor(new Color(0, 200, 255, (int)(alpha * 255)));
            int px = (int) engine.getPlayer().getX();
            int py = (int) engine.getPlayer().getY();
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(px - 20, py - 20, 70, 70);
        }
    }

    private void renderBoss(Graphics2D g2d) {
        int bx = (int) engine.getBoss().getX();
        int by = (int) engine.getBoss().getY();
        int bw = engine.getBoss().getWidth();
        int bh = engine.getBoss().getHeight();

        if (bossSprite != null) {
            g2d.drawImage(bossSprite, bx, by, bw, bh, this);
        } else {
            // Fallback: gradient rectangle
            GradientPaint gp = new GradientPaint(bx, by, new Color(180, 0, 180),
                    bx + bw, by + bh, new Color(80, 0, 80));
            g2d.setPaint(gp);
            g2d.fillRect(bx, by, bw, bh);
        }

        // Boss HP bar trên đầu Boss
        int barW = bw + 20;
        int barH = 8;
        int barX = bx - 10;
        int barY = by - 16;
        float hpRatio = (float) engine.getBoss().getHp() / engine.getBoss().getMaxHp();
        g2d.setColor(new Color(60, 0, 0));
        g2d.fillRect(barX, barY, barW, barH);
        g2d.setColor(new Color(220, 30, 30));
        g2d.fillRect(barX, barY, (int)(barW * hpRatio), barH);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(barX, barY, barW, barH);

        // Phase indicator
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Phase " + engine.getBoss().getPhase(), bx, by - 20);
    }

    private void renderBullets(Graphics2D g2d) {
        for (B1Bullet b : engine.getBullets()) {
            if (b.isPlayerBullet()) {
                // Chưởng của player: màu vàng/xanh
                GradientPaint gp = new GradientPaint(
                        (float) b.getX(), (float) b.getY(), new Color(255, 230, 50),
                        (float) b.getX() + b.getRadius(), (float) b.getY() + b.getRadius(), new Color(50, 200, 255));
                g2d.setPaint(gp);
            } else {
                // Đạn Boss: đỏ
                g2d.setColor(new Color(255, 60, 60, 200));
            }
            int bx = (int)(b.getX() - b.getRadius());
            int by = (int)(b.getY() - b.getRadius());
            int bd = b.getRadius() * 2;
            g2d.fillOval(bx, by, bd, bd);
        }
    }

    private void renderPlayer(Graphics2D g2d) {
        int px = (int) engine.getPlayer().getX();
        int py = (int) engine.getPlayer().getY();

        BufferedImage sprite = getPlayerSprite();
        if (sprite != null) {
            g2d.drawImage(sprite, px - 6, py - 6, 42, 42, this);
        } else {
            // Fallback hình chữ nhật xanh
            g2d.setColor(new Color(50, 150, 255));
            g2d.fillRect(px, py, 30, 30);
        }
    }

    private BufferedImage getPlayerSprite() {
        boolean moving = upPressed || downPressed || leftPressed || rightPressed;
        if (moving) {
            BufferedImage[] frames = moveFrames[animDir];
            if (frames != null && frames[animFrame % 4] != null) {
                return frames[animFrame % 4];
            }
        } else {
            if (standFront != null && standFront[animFrame % 3] != null) {
                return standFront[animFrame % 3];
            }
        }
        return null;
    }

    private void renderHUD(Graphics2D g2d) {
        Player p = GameManager.getInstance().getPlayer();

        // --- Boss HP bar lớn ở trên cùng ---
        renderBossHPBar(g2d);

        // --- Player HP bar ---
        int playerHp    = engine.getPlayer().getHp();
        int playerMaxHp = engine.getPlayer().getMaxHp();
        renderBar(g2d, 20, getHeight() - 90, 200, 14,
                playerHp, playerMaxHp,
                new Color(50, 220, 80), new Color(30, 80, 30), "HP: " + playerHp + "/" + playerMaxHp);

        // --- Player Energy ---
        int energy    = p.getEnergy();
        int maxEnergy = p.getMaxEnergy();
        renderBar(g2d, 20, getHeight() - 68, 200, 14,
                energy, maxEnergy,
                new Color(80, 180, 255), new Color(20, 50, 100), "Energy: " + energy + "/" + maxEnergy);

        // --- Stats compacte ---
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("Lv." + p.getLevel() + "  Rèn Luyện:" + p.getDisciplineScore()
                + "  Skill:" + p.getSolutionSkill(), 20, getHeight() - 50);

        // --- Item cooldown HUD ---
        renderItemHUD(g2d, p);

        // --- Cảnh báo item chưa đủ điều kiện ---
        if (warningTimer > 0) {
            float alpha = Math.min(1f, warningTimer / 30f);
            g2d.setColor(new Color(1f, 0.2f, 0.2f, alpha));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();
            int tw = fm.stringWidth(warningMessage);
            g2d.drawString(warningMessage, (getWidth() - tw) / 2, getHeight() / 2 - 40);
        }

        // --- Debuff status ---
        String debuffLabel = engine.getDebuffManager().getActiveDebuffLabel();
        if (!debuffLabel.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.setColor(new Color(255, 80, 80));
            FontMetrics dfm = g2d.getFontMetrics();
            int dw = dfm.stringWidth(debuffLabel);
            // Background tag
            g2d.setColor(new Color(60, 0, 0, 180));
            g2d.fillRoundRect((getWidth() - dw) / 2 - 8, getHeight() - 115, dw + 16, 22, 6, 6);
            g2d.setColor(new Color(255, 100, 100));
            g2d.drawString(debuffLabel, (getWidth() - dw) / 2, getHeight() - 99);
        }

        // --- Thẻ B2 charges ---
        int b2 = engine.getDebuffManager().getCardB2Charges();
        if (b2 > 0) {
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.setColor(new Color(0, 200, 255));
            g2d.drawString("🃏 Thẻ B2: " + b2 + " lần", getWidth() - 140, getHeight() - 100);
        }

        // --- Controls hint ---
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(new Color(120, 120, 120));
        g2d.drawString("WASD: Di chuyển  |  1: OOP Book  |  2: Sổ Ghi Chép  |  3: Học Bổng  |  ESC: Thoát", 10, getHeight() - 8);
    }

    private void renderBossHPBar(Graphics2D g2d) {
        int bossHp    = engine.getBoss().getHp();
        int bossMaxHp = engine.getBoss().getMaxHp();
        int barW      = getWidth() - 80;
        int barH      = 18;
        int barX      = 40;
        int barY      = 12;

        // Background
        g2d.setColor(new Color(40, 10, 10));
        g2d.fillRoundRect(barX - 2, barY - 2, barW + 4, barH + 4, 6, 6);

        // HP fill (gradient based on hp ratio)
        float ratio = (float) bossHp / bossMaxHp;
        Color hpColor = ratio > 0.5f ? new Color(220, 40, 40)
                : ratio > 0.3f       ? new Color(255, 140, 0)
                :                      new Color(255, 60, 200);
        GradientPaint gp = new GradientPaint(barX, barY, hpColor.brighter(),
                barX + (int)(barW * ratio), barY + barH, hpColor);
        g2d.setPaint(gp);
        g2d.fillRoundRect(barX, barY, (int)(barW * ratio), barH, 4, 4);

        // Border
        g2d.setColor(new Color(150, 50, 50));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(barX, barY, barW, barH, 4, 4);

        // Label
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.WHITE);
        String label = "☠ HUST OVERLORD — " + bossHp + " / " + bossMaxHp + " HP";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(label, barX + (barW - fm.stringWidth(label)) / 2, barY + 14);
    }

    private void renderBar(Graphics2D g2d, int x, int y, int w, int h,
                            int current, int max, Color fill, Color bg, String label) {
        g2d.setColor(bg);
        g2d.fillRoundRect(x, y, w, h, 4, 4);
        if (max > 0) {
            float ratio = Math.min(1f, (float) current / max);
            g2d.setColor(fill);
            g2d.fillRoundRect(x, y, (int)(w * ratio), h, 4, 4);
        }
        g2d.setColor(new Color(200, 200, 200, 180));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x, y, w, h, 4, 4);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2d.setColor(Color.WHITE);
        g2d.drawString(label, x + 4, y + h - 2);
    }

    private void renderItemHUD(Graphics2D g2d, Player p) {
        long now = System.currentTimeMillis();
        int startX = getWidth() - 320;
        int y      = getHeight() - 90;

        String[] labels   = {"[1] OOP Book", "[2] Sổ Chiến Lược", "[3] Học Bổng KKHT"};
        String[] reqs     = {"Skill≥70", "Rèn Luyện≥80", "Lv≥8 & Tiền≥100"};
        B1ItemManager.ItemType[] types = {
                B1ItemManager.ItemType.GIAO_TRINH_OOP,
                B1ItemManager.ItemType.SO_GHI_CHEP,
                B1ItemManager.ItemType.HOC_BONG_KKHT
        };

        for (int i = 0; i < 3; i++) {
            int x = startX + i * 108;
            B1ItemManager.ItemInfo info = engine.getItemManager().getItemInfo(types[i]);
            boolean ready = (info == null || info.isReady(now));
            boolean active = (info != null && info.isActive(now));

            // Box background
            Color boxColor = active   ? new Color(0, 60, 120, 200) :
                             ready    ? new Color(30, 30, 50, 200)  :
                                        new Color(20, 20, 30, 200);
            g2d.setColor(boxColor);
            g2d.fillRoundRect(x, y, 100, 56, 8, 8);

            Color borderColor = active ? new Color(0, 180, 255) :
                                ready  ? new Color(120, 120, 180) :
                                         new Color(60, 60, 80);
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(x, y, 100, 56, 8, 8);

            // Cooldown overlay
            if (!ready && info != null) {
                long elapsed  = now - info.lastActivationTime;
                float cdRatio = Math.min(1f, (float) elapsed / info.cooldownMs);
                g2d.setColor(new Color(0, 0, 0, 120));
                g2d.fillRect(x + 1, y + 1, 100 - (int)(99 * cdRatio), 54);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.setColor(new Color(255, 140, 0));
                long remaining = (info.cooldownMs - elapsed) / 1000;
                g2d.drawString(remaining + "s", x + 38, y + 36);
            }

            // Label
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.setColor(ready ? Color.WHITE : Color.GRAY);
            g2d.drawString(labels[i], x + 4, y + 14);

            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            g2d.setColor(new Color(180, 180, 180));
            g2d.drawString(reqs[i], x + 4, y + 28);

            if (active) {
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.setColor(new Color(0, 200, 255));
                g2d.drawString("ĐANG HOẠT ĐỘNG", x + 4, y + 50);
            } else if (ready) {
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.setColor(new Color(80, 220, 80));
                g2d.drawString("SẴN SÀNG", x + 4, y + 50);
            }
        }
    }

    // =========================================================
    // INPUT HANDLERS
    // =========================================================
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    upPressed    = true; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  downPressed  = true; break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  leftPressed  = true; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: rightPressed = true; break;

            // Items
            case KeyEvent.VK_1:
                handleItemActivation(B1ItemManager.ItemType.GIAO_TRINH_OOP);
                break;
            case KeyEvent.VK_2:
                handleItemActivation(B1ItemManager.ItemType.SO_GHI_CHEP);
                break;
            case KeyEvent.VK_3:
                handleItemActivation(B1ItemManager.ItemType.HOC_BONG_KKHT);
                break;

            // ESC: về lobby B1
            case KeyEvent.VK_ESCAPE:
                gameTimer.stop();
                int choice = JOptionPane.showConfirmDialog(this,
                        "Thoát về sảnh B1?", "Thoát", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    navigateToLobby();
                } else {
                    gameTimer.start();
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    upPressed    = false; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  downPressed  = false; break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  leftPressed  = false; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: rightPressed = false; break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    // =========================================================
    // ITEM ACTIVATION
    // =========================================================
    private void handleItemActivation(B1ItemManager.ItemType type) {
        if (engine.getCurrentState() != B1Engine.State.PLAYING) return;
        boolean success = engine.activateItem(type);
        if (!success) {
            warningMessage = "Trình độ học thuật chưa đủ để thấu hiểu và sử dụng bảo vật này!";
            warningTimer   = 120;
        } else {
            statsPanel.updateStats();
        }
    }
}
