package com.hust.game.ui;

import com.hust.game.ui.panels.FarmingPanel;
import com.hust.game.ui.panels.StatsPanel;
import com.hust.game.ui.panels.WorldMapPanel;
import com.hust.game.ui.panels.C2Panel;
import com.hust.game.ui.panels.B1LobbyPanel;
import com.hust.game.ui.panels.B1Panel;
import com.hust.game.ui.panels.C2LectureHallPanel;
import com.hust.game.ui.panels.CFPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * GameWindow - Cửa sổ chính điều phối giao diện sử dụng CardLayout.
 *
 * Bảng đăng ký Card:
 *  "WORLD_MAP"    → WorldMapPanel
 *  "MAP_SONLA"    → FarmingPanel
 *  "MAP_C2"       → C2Panel
 *  "MAP_D9"       → D9Panel
 *  "MAP_B1"       → B1LobbyPanel  ← Sảnh chờ đấu trường
 *  "B1_ARENA"     → B1Panel       ← Gameplay action (mới)
 *  "COMBAT_SCREEN"→ CombatPanel   (giữ lại tương thích)
 */
public class GameWindow extends JFrame {
    private StatsPanel   statsPanel;
    private JPanel       mainContainer;
    private CardLayout   cardLayout;
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

        // Liên kết StatsPanel + Window với GameManager
        com.hust.game.core.GameManager.getInstance().setStatsPanel(statsPanel);
        com.hust.game.core.GameManager.getInstance().setWindow(this);

        // 2. Container chính sử dụng CardLayout
        cardLayout    = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // --- Tạo tất cả panels ---
        WorldMapPanel worldMapPanel = new WorldMapPanel(this, statsPanel);
        FarmingPanel farmingPanel = new FarmingPanel(this, statsPanel);
        C2Panel c2Panel = new C2Panel(this, statsPanel);
        C2LectureHallPanel c2LectureHallPanel = new C2LectureHallPanel(this, statsPanel);
        com.hust.game.ui.panels.D9Panel d9Panel = new com.hust.game.ui.panels.D9Panel(this, statsPanel);
        B1LobbyPanel b1LobbyPanel = new B1LobbyPanel(this, statsPanel);
        B1Panel b1Panel = new B1Panel(this, statsPanel);
        com.hust.game.ui.panels.ArenaPanel arenaPanel = new com.hust.game.ui.panels.ArenaPanel(this, statsPanel);
        this.combatPanel = new com.hust.game.ui.panels.CombatPanel(this, statsPanel);
        CFPanel cfPanel = new CFPanel(this, statsPanel);

        registerPanel("WORLD_MAP", worldMapPanel);
        registerPanel("MAP_SONLA", farmingPanel);
        registerPanel("MAP_C2", c2Panel);
        registerPanel("MAP_C2_HALL", c2LectureHallPanel);
        registerPanel("MAP_D9", d9Panel);
        registerPanel("MAP_B1", b1LobbyPanel);
        registerPanel("B1_ARENA", b1Panel);
        registerPanel("MAP_ARENA_ALT", arenaPanel);
        registerPanel("COMBAT_SCREEN", combatPanel);
        registerPanel("MAP_CF", cfPanel);
        
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
        // --- Gọi onHidden cho panel cũ ---
        if (currentPanelName != null && !currentPanelName.equals(name)) {
            JComponent prev = panels.get(currentPanelName);
            if (prev instanceof com.hust.game.ui.panels.D9Panel) {
                ((com.hust.game.ui.panels.D9Panel) prev).onHidden();
            } else if (prev instanceof B1Panel) {
                ((B1Panel) prev).onHidden();
            }
        }

        System.out.println("[Navigation] Switching to panel: " + name);
        cardLayout.show(mainContainer, name);
        currentPanelName = name;

        // --- Gọi onShown / requestFocus cho panel mới ---
        JComponent panel = panels.get(name);
        if (panel instanceof com.hust.game.ui.panels.D9Panel) {
            ((com.hust.game.ui.panels.D9Panel) panel).onShown();
            SwingUtilities.invokeLater(panel::requestFocusInWindow);
        } else if (panel instanceof B1Panel) {
            ((B1Panel) panel).onShown();
            SwingUtilities.invokeLater(panel::requestFocusInWindow);
        } else if (panel instanceof B1LobbyPanel) {
            ((B1LobbyPanel) panel).refreshStats();
            SwingUtilities.invokeLater(panel::requestFocusInWindow);
        } else if (panel instanceof C2Panel) {
            ((C2Panel) panel).onShown();
        } else if (panel instanceof CFPanel) {
            ((CFPanel) panel).onShown();
        } else if (panel != null) {
            panel.requestFocusInWindow();
        }
    }

    public com.hust.game.ui.panels.CombatPanel getCombatPanel() {
        return combatPanel;
    }
}
