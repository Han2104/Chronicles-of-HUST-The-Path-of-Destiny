package com.hust.game.ui.panels;

import com.hust.game.ui.GameWindow;
import com.hust.game.models.combat.Boss;
import com.hust.game.core.GameManager;

import javax.swing.*;
import java.awt.*;

/**
 * ArenaPanel - Khu vực Hội trường B1, nơi thách đấu các Boss.
 */
public class ArenaPanel extends JPanel {
    private GameWindow window;
    private StatsPanel statsPanel;

    public ArenaPanel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;
        
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));

        JLabel title = new JLabel("HỘI TRƯỜNG B1: ĐẤU TRƯỜNG GIÁO SƯ", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.RED);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel bossListPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        bossListPanel.setOpaque(false);
        bossListPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        bossListPanel.add(createBossCard("Thầy Tạ Hải Tùng", "Boss 1 - Kỹ thuật Lập trình", Boss.createBossTung()));
        bossListPanel.add(createBossCard("GS. Nguyễn Cảnh Lương", "Boss 2 - Cơ học ứng dụng", Boss.createBossLuong()));
        bossListPanel.add(createBossCard("GS. Hoàng Minh Sơn", "Boss 3 - Boss Cuối", Boss.createBossSon()));

        add(bossListPanel, BorderLayout.CENTER);

        JButton btnBack = new JButton("⬅ Quay lại World Map");
        btnBack.addActionListener(e -> window.showPanel("WORLD_MAP"));
        add(btnBack, BorderLayout.SOUTH);
    }

    private JPanel createBossCard(String name, String sub, Boss boss) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(60, 60, 60));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        JLabel lblName = new JLabel(name, SwingConstants.CENTER);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel lblSub = new JLabel(sub, SwingConstants.CENTER);
        lblSub.setForeground(Color.LIGHT_GRAY);

        JButton btnChallenge = new JButton("THÁCH ĐẤU");
        btnChallenge.setBackground(new Color(150, 0, 0));
        btnChallenge.setForeground(Color.WHITE);
        btnChallenge.setFont(new Font("Arial", Font.BOLD, 14));
        
        btnChallenge.addActionListener(e -> {
            window.showPanel("COMBAT_SCREEN");
            // Truy cập combatPanel qua GameWindow
            window.getCombatPanel().startCombat(boss);
        });

        card.add(lblName, BorderLayout.NORTH);
        card.add(lblSub, BorderLayout.CENTER);
        card.add(btnChallenge, BorderLayout.SOUTH);
        return card;
    }
}
