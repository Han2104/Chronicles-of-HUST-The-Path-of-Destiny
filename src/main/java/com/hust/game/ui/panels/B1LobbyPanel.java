package com.hust.game.ui.panels;

import com.hust.game.core.GameManager;
import com.hust.game.models.entities.Player;
import com.hust.game.ui.GameWindow;
import com.hust.game.util.AssetLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * B1LobbyPanel — Sảnh chờ đấu trường B1.
 * Hiển thị thông tin về HUST OVERLORD, stats của Player,
 * và nút "VÀO ĐẤU TRƯỜNG" để chuyển sang B1_ARENA.
 *
 * Card key: "MAP_B1"
 */
public class B1LobbyPanel extends JPanel {

    private GameWindow window;
    private StatsPanel statsPanel;
    private BufferedImage bossImage;

    // Labels cần refresh khi hiển thị
    private JLabel lblLevel;
    private JLabel lblDiscipline;
    private JLabel lblSolutionSkill;
    private JLabel lblFinance;
    private JLabel lblWarning;

    public B1LobbyPanel(GameWindow window, StatsPanel statsPanel) {
        this.window     = window;
        this.statsPanel = statsPanel;

        bossImage = AssetLoader.loadImage("assets/boss.png");

        setLayout(new BorderLayout());
        setBackground(new Color(12, 8, 20));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        buildUI();
    }

    private void buildUI() {
        // ====== TOP: Title ======
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("⚔ ĐẤU TRƯỜNG B1 — HỘI TRƯỜNG HUYỀN THOẠI", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(220, 60, 60));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        topPanel.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Vũ đứng trước cổng hội trường B1. Bóng tối bao trùm. Tiếng sấm rền vang.", SwingConstants.CENTER);
        subtitle.setFont(new Font("Serif", Font.ITALIC, 14));
        subtitle.setForeground(new Color(180, 140, 220));
        topPanel.add(subtitle, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ====== CENTER: Boss info + Player stats ======
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill   = GridBagConstraints.BOTH;

        // --- Boss card ---
        JPanel bossCard = buildBossCard();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.55; gbc.weighty = 1.0;
        centerPanel.add(bossCard, gbc);

        // --- Player stats card ---
        JPanel statsCard = buildPlayerStatsCard();
        gbc.gridx = 1; gbc.weightx = 0.45;
        centerPanel.add(statsCard, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // ====== BOTTOM: buttons ======
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setOpaque(false);

        JButton btnEnter = createStyledButton("⚔  VÀO ĐẤU TRƯỜNG  ⚔", new Color(180, 30, 30), new Color(220, 60, 60));
        btnEnter.setPreferredSize(new Dimension(280, 50));
        btnEnter.addActionListener(e -> enterArena());

        JButton btnBack = createStyledButton("← Quay về World Map", new Color(50, 50, 80), new Color(80, 80, 120));
        btnBack.setPreferredSize(new Dimension(200, 50));
        btnBack.addActionListener(e -> {
            GameManager.getInstance().switchMap(0);
            window.showPanel("WORLD_MAP");
            statsPanel.updateStats();
        });

        bottomPanel.add(btnBack);
        bottomPanel.add(btnEnter);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // =========================================================
    // BOSS CARD
    // =========================================================
    private JPanel buildBossCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(new Color(25, 10, 35));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(140, 30, 30), 2),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Boss image
        JPanel bossImgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bossImage != null) {
                    int iw = Math.min(getWidth(), 260);
                    int ih = Math.min(getHeight(), 240);
                    int ix = (getWidth() - iw) / 2;
                    int iy = (getHeight() - ih) / 2;
                    g.drawImage(bossImage, ix, iy, iw, ih, this);
                } else {
                    g.setColor(new Color(80, 0, 80));
                    g.fillRect(20, 20, getWidth() - 40, getHeight() - 40);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    g.drawString("HUST OVERLORD", 30, getHeight() / 2);
                }
            }
        };
        bossImgPanel.setOpaque(false);
        bossImgPanel.setPreferredSize(new Dimension(280, 240));
        card.add(bossImgPanel, BorderLayout.CENTER);

        // Boss info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        addInfoLine(infoPanel, "☠  ĐẠI GIÁO SƯ TỔNG HỢP (HUST OVERLORD)", new Color(255, 80, 80), 16, Font.BOLD);
        addInfoLine(infoPanel, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", new Color(100, 100, 100), 11, Font.PLAIN);
        addInfoLine(infoPanel, "❤  HP: 600 / 600", new Color(220, 60, 60), 13, Font.BOLD);
        addInfoLine(infoPanel, "⚡ 3 Phase: Dần hung hãn khi mất máu", new Color(255, 180, 0), 12, Font.PLAIN);
        addInfoLine(infoPanel, "🔴 Bắn đạn tỏa tròn + nhắm thẳng Player", new Color(200, 200, 200), 12, Font.PLAIN);
        addInfoLine(infoPanel, " ", Color.WHITE, 6, Font.PLAIN);
        addInfoLine(infoPanel, "💡 Điều kiện sát thương đầy đủ:", new Color(255, 220, 100), 12, Font.BOLD);
        addInfoLine(infoPanel, "   Lv ≥ 8 và Điểm Rèn Luyện ≥ 80", new Color(200, 200, 200), 12, Font.PLAIN);
        addInfoLine(infoPanel, "   (Nếu không đạt → sát thương -80%)", new Color(180, 120, 120), 11, Font.ITALIC);

        card.add(infoPanel, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================
    // PLAYER STATS CARD
    // =========================================================
    private JPanel buildPlayerStatsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(15, 20, 35));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 80, 140), 2),
                new EmptyBorder(15, 18, 15, 18)
        ));

        addInfoLine(card, "📊 THỐNG KÊ NHÂN VẬT", new Color(100, 180, 255), 15, Font.BOLD);
        addInfoLine(card, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", new Color(60, 60, 90), 11, Font.PLAIN);

        Player p = GameManager.getInstance().getPlayer();

        lblLevel = createStatLabel("⭐ Cấp độ: " + p.getLevel(), p.getLevel() >= 8
                ? new Color(80, 220, 80) : new Color(255, 160, 50));
        card.add(lblLevel);

        lblDiscipline = createStatLabel("🏆 Rèn Luyện: " + p.getDisciplineScore() + "/100",
                p.getDisciplineScore() >= 80 ? new Color(80, 220, 80) : new Color(255, 160, 50));
        card.add(lblDiscipline);

        lblSolutionSkill = createStatLabel("🧠 Solution Skill: " + p.getSolutionSkill() + "/100",
                p.getSolutionSkill() >= 70 ? new Color(80, 220, 80) : new Color(255, 160, 50));
        card.add(lblSolutionSkill);

        lblFinance = createStatLabel("💰 Tài chính: " + (int)p.getFinance() + " VNĐ",
                p.getFinance() >= 100 ? new Color(80, 220, 80) : new Color(255, 160, 50));
        card.add(lblFinance);

        card.add(Box.createVerticalStrut(10));
        addInfoLine(card, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", new Color(60, 60, 90), 11, Font.PLAIN);
        addInfoLine(card, "🎒 VẬT PHẨM CHIẾN ĐẤU:", new Color(200, 200, 255), 13, Font.BOLD);

        boolean hasOOP  = p.hasOOPBook();
        boolean hasNote = p.hasStrategyNotebook();
        addInfoLine(card, (hasOOP  ? "✅" : "❌") + " Giáo Trình OOP  [1] Skill≥70 → -60HP Boss", hasOOP  ? new Color(80,220,80) : new Color(180,80,80), 11, Font.PLAIN);
        addInfoLine(card, (hasNote ? "✅" : "❌") + " Sổ Ghi Chép      [2] Rèn≥80  → Shield 4s", hasNote ? new Color(80,220,80) : new Color(180,80,80), 11, Font.PLAIN);
        addInfoLine(card, "✅ Học Bổng KKHT [3] Lv≥8&Tiền≥100 → -80HP+Heal", new Color(80,220,80), 11, Font.PLAIN);

        card.add(Box.createVerticalStrut(12));

        // Cảnh báo nếu stats chưa đủ
        lblWarning = new JLabel();
        lblWarning.setFont(new Font("Arial", Font.BOLD, 11));
        lblWarning.setForeground(new Color(255, 100, 100));
        lblWarning.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblWarning);
        updateWarningLabel(p);

        return card;
    }

    private JLabel createStatLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(3, 0, 3, 0));
        return lbl;
    }

    private void updateWarningLabel(Player p) {
        if (p.getLevel() < 8 || p.getDisciplineScore() < 80) {
            lblWarning.setText("<html>⚠ Chưa đủ điều kiện sát thương đầy đủ!<br>" +
                    "Cần: Lv≥8 (hiện: " + p.getLevel() + ") & Rèn≥80 (hiện: " + p.getDisciplineScore() + ")</html>");
        } else {
            lblWarning.setText("<html>✅ Đủ điều kiện! Sát thương đầy đủ.</html>");
            lblWarning.setForeground(new Color(80, 220, 80));
        }
    }

    // =========================================================
    // ENTER ARENA
    // =========================================================
    private void enterArena() {
        GameManager.getInstance().switchMap(4);
        statsPanel.updateStats();
        window.showPanel("B1_ARENA");
    }

    // =========================================================
    // REFRESH khi panel được hiển thị
    // =========================================================
    public void refreshStats() {
        Player p = GameManager.getInstance().getPlayer();
        if (lblLevel != null) {
            lblLevel.setText("⭐ Cấp độ: " + p.getLevel());
            lblLevel.setForeground(p.getLevel() >= 8 ? new Color(80,220,80) : new Color(255,160,50));
            lblDiscipline.setText("🏆 Rèn Luyện: " + p.getDisciplineScore() + "/100");
            lblDiscipline.setForeground(p.getDisciplineScore() >= 80 ? new Color(80,220,80) : new Color(255,160,50));
            lblSolutionSkill.setText("🧠 Solution Skill: " + p.getSolutionSkill() + "/100");
            lblSolutionSkill.setForeground(p.getSolutionSkill() >= 70 ? new Color(80,220,80) : new Color(255,160,50));
            lblFinance.setText("💰 Tài chính: " + (int)p.getFinance() + " VNĐ");
            lblFinance.setForeground(p.getFinance() >= 100 ? new Color(80,220,80) : new Color(255,160,50));
            updateWarningLabel(p);
        }
        repaint();
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private void addInfoLine(JPanel panel, String text, Color color, int fontSize, int style) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", style, fontSize));
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(2, 0, 2, 0));
        panel.add(lbl);
    }

    private JButton createStyledButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(bg);    }
        });
        return btn;
    }
}
