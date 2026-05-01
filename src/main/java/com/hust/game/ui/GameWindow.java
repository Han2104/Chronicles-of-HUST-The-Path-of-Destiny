package com.hust.game.ui;

import com.hust.game.ui.panels.FarmingPanel;
import com.hust.game.ui.panels.StatsPanel;
import com.hust.game.ui.panels.WorldMapPanel;

import javax.swing.*;
import java.awt.*;

/**
 * GameWindow - Cửa sổ chính điều phối giao diện sử dụng CardLayout.
 */
public class GameWindow extends JFrame {
    private StatsPanel statsPanel;
    private JPanel mainContainer;
    private CardLayout cardLayout;

    public GameWindow() {
        setTitle("Chronicles of HUST - Vũ Em Đi Học");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());

        // 1. Thanh trạng thái cố định ở trên
        statsPanel = new StatsPanel();
        add(statsPanel, BorderLayout.NORTH);
        
        // Liên kết StatsPanel với GameManager để hỗ trợ cập nhật UI khi hồi năng lượng
        com.hust.game.core.GameManager.getInstance().setStatsPanel(statsPanel);
        com.hust.game.core.GameManager.getInstance().setWindow(this);

        // 2. Container chính sử dụng CardLayout để chuyển cảnh
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(new WorldMapPanel(this, statsPanel), "WORLD_MAP");
        mainContainer.add(new FarmingPanel(this, statsPanel), "MAP_SONLA");

        add(mainContainer, BorderLayout.CENTER);

        showPanel("WORLD_MAP");
        setVisible(true);
    }

    public void showPanel(String name) {
        cardLayout.show(mainContainer, name);
    }
}
