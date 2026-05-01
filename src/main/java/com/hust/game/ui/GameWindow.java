package com.hust.game.ui;

import com.hust.game.ui.panels.FarmingPanel;
import com.hust.game.ui.panels.StatsPanel;
import com.hust.game.ui.panels.WorldMapPanel;
import com.hust.game.ui.panels.C2Panel;

import javax.swing.*;
import java.awt.*;

/**
 * GameWindow - Cửa sổ chính điều phối giao diện sử dụng CardLayout.
 */
public class GameWindow extends JFrame {
    private StatsPanel statsPanel;
    private JPanel mainContainer;
    private CardLayout cardLayout;
    private com.hust.game.ui.panels.CombatPanel combatPanel;

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

        WorldMapPanel worldMapPanel = new WorldMapPanel(this, statsPanel);
        FarmingPanel farmingPanel = new FarmingPanel(this, statsPanel);
        C2Panel c2Panel = new C2Panel(this, statsPanel);
        com.hust.game.ui.panels.D9Panel d9Panel = new com.hust.game.ui.panels.D9Panel(this, statsPanel);
        com.hust.game.ui.panels.ArenaPanel arenaPanel = new com.hust.game.ui.panels.ArenaPanel(this, statsPanel);
        this.combatPanel = new com.hust.game.ui.panels.CombatPanel(this, statsPanel);

        mainContainer.add(worldMapPanel, "WORLD_MAP");
        mainContainer.add(farmingPanel, "MAP_SONLA");
        mainContainer.add(c2Panel, "MAP_C2");
        mainContainer.add(d9Panel, "MAP_D9");
        mainContainer.add(arenaPanel, "MAP_B1");
        mainContainer.add(combatPanel, "COMBAT_SCREEN");
        
        // Tạm thời để MAP_LIBRARY trỏ về D9 hoặc tạo panel mới nếu cần
        mainContainer.add(new JPanel(), "MAP_LIBRARY"); 

        add(mainContainer, BorderLayout.CENTER);

        showPanel("WORLD_MAP");
        setVisible(true);
    }

    public void showPanel(String name) {
        cardLayout.show(mainContainer, name);
    }

    public com.hust.game.ui.panels.CombatPanel getCombatPanel() {
        return combatPanel;
    }
}
