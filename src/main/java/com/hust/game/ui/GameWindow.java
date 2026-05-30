package com.hust.game.ui;

import com.hust.game.ui.panels.FarmingPanel;
import com.hust.game.ui.panels.StatsPanel;
import com.hust.game.ui.panels.WorldMapPanel;
import com.hust.game.ui.panels.C2Panel;
import com.hust.game.ui.panels.C2LectureHallPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * GameWindow - Cửa sổ chính điều phối giao diện sử dụng CardLayout.
 */
public class GameWindow extends JFrame {
    private StatsPanel statsPanel;
    private JPanel mainContainer;
    private CardLayout cardLayout;
    private com.hust.game.ui.panels.CombatPanel combatPanel;
    private final Map<String, JComponent> panels = new HashMap<>();
    private String currentPanelName;

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
        C2LectureHallPanel c2LectureHallPanel = new C2LectureHallPanel(this, statsPanel);
        com.hust.game.ui.panels.D9Panel d9Panel = new com.hust.game.ui.panels.D9Panel(this, statsPanel);
        com.hust.game.ui.panels.ArenaPanel arenaPanel = new com.hust.game.ui.panels.ArenaPanel(this, statsPanel);
        this.combatPanel = new com.hust.game.ui.panels.CombatPanel(this, statsPanel);

        registerPanel("WORLD_MAP", worldMapPanel);
        registerPanel("MAP_SONLA", farmingPanel);
        registerPanel("MAP_C2", c2Panel);
        registerPanel("MAP_C2_HALL", c2LectureHallPanel);
        registerPanel("MAP_D9", d9Panel);
        registerPanel("MAP_B1", arenaPanel);
        registerPanel("COMBAT_SCREEN", combatPanel);
        
        // Tạm thời để MAP_LIBRARY trỏ về D9 hoặc tạo panel mới nếu cần
        registerPanel("MAP_LIBRARY", new JPanel());

        add(mainContainer, BorderLayout.CENTER);

        showPanel("WORLD_MAP");
        setVisible(true);
    }

    private void registerPanel(String name, JComponent panel) {
        panel.setName(name);
        panels.put(name, panel);
        mainContainer.add(panel, name);
    }

    public void showPanel(String name) {
        if (currentPanelName != null && !currentPanelName.equals(name)) {
            JComponent previousPanel = panels.get(currentPanelName);
            if (previousPanel instanceof com.hust.game.ui.panels.D9Panel) {
                ((com.hust.game.ui.panels.D9Panel) previousPanel).onHidden();
            }
        }

        if ("MAP_D9".equals(name)) {
            System.out.println("[Navigation] Switching to panel: D9");
        } else {
            System.out.println("[Navigation] Switching to panel: " + name);
        }
        cardLayout.show(mainContainer, name);
        currentPanelName = name;

        JComponent panel = panels.get(name);
        if (panel instanceof com.hust.game.ui.panels.D9Panel) {
            ((com.hust.game.ui.panels.D9Panel) panel).onShown();
            SwingUtilities.invokeLater(panel::requestFocusInWindow);
        } else if (panel instanceof C2Panel) {
            ((C2Panel) panel).onShown();
        } else if (panel != null) {
            panel.requestFocusInWindow();
        }
    }

    public com.hust.game.ui.panels.CombatPanel getCombatPanel() {
        return combatPanel;
    }
}
