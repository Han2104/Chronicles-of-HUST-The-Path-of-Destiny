package com.hust.game.ui.panels;

import com.hust.game.core.GameManager;
import com.hust.game.models.entities.Player;

import javax.swing.*;
import java.awt.*;

/**
 * StatsPanel - Thanh trạng thái hiển thị chỉ số của Vũ.
 */
public class StatsPanel extends JPanel {
    private JLabel lblLevel, lblExp, lblEnergy, lblFinance, lblInventory;

    public StatsPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY));

        lblLevel = createStatLabel("⭐ Cấp: ", new Color(128, 0, 128));
        lblExp = createStatLabel("📈 EXP: ", new Color(70, 130, 180));
        lblEnergy = createStatLabel("⚡ Năng lượng: ", Color.RED);
        lblFinance = createStatLabel("💰 Tài chính: ", new Color(0, 128, 0));
        lblInventory = createStatLabel("🎒 Hành trang: ", Color.DARK_GRAY);

        add(lblLevel);
        add(lblExp);
        add(lblEnergy);
        add(lblFinance);
        add(lblInventory);
        
        updateStats();
    }

    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(color);
        return label;
    }

    public void updateStats() {
        Player player = GameManager.getInstance().getPlayer();
        
        lblLevel.setText("⭐ Cấp: " + player.getLevel());
        
        int nextExp = player.getNextLevelExp();
        String expText = (nextExp == -1) ? "MAX" : player.getExp() + "/" + nextExp;
        lblExp.setText("📈 EXP: " + expText);
        
        lblEnergy.setText("⚡ Năng lượng: " + player.getEnergy() + "/" + player.getEnergy()); // Showing current/max conceptually
        lblFinance.setText("💰 Tài chính: " + String.format("%.1f", player.getFinance()) + " VNĐ");
        
        String invText = String.format("🎒 Ngô: %d | Lúa: %d", 
                player.getItemCount("Hạt giống Ngô"), 
                player.getItemCount("Hạt giống Lúa"));
        lblInventory.setText(invText);
    }
}
