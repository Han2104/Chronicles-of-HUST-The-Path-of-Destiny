package com.hust.game.ui.panels;

import com.hust.game.core.GameManager;
import com.hust.game.models.entities.Player;
import com.hust.game.models.items.Seed;

import javax.swing.*;
import java.awt.*;

/**
 * ShopDialog - Giao diện cửa hàng mua sắm hạt giống.
 */
public class ShopDialog extends JDialog {
    private StatsPanel statsPanel;

    public ShopDialog(Frame parent, StatsPanel statsPanel) {
        super(parent, "Cửa hàng vật phẩm Sơn La", true);
        this.statsPanel = statsPanel;
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("🛒 CHÀO MỪNG ĐẾN VỚI CỬA HÀNG", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(header, BorderLayout.NORTH);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        for (Seed seed : Seed.values()) {
            addItemRow(itemsPanel, seed);
        }

        add(new JScrollPane(itemsPanel), BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        add(btnClose, BorderLayout.SOUTH);
    }

    private void addItemRow(JPanel panel, Seed seed) {
        Player player = GameManager.getInstance().getPlayer();
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(350, 50));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel nameLabel = new JLabel(String.format("%s (%s VNĐ) [Đang có: %d]", 
                seed.getName(), seed.getBuyPrice(), player.getItemCount(seed.getName())));
        JButton btnBuy = new JButton("Mua");

        btnBuy.addActionListener(e -> {
            if (player.getFinance() >= seed.getBuyPrice()) {
                player.addFinance(-seed.getBuyPrice());
                player.addItem(seed.getName(), 1);
                statsPanel.updateStats();
                // Refresh shop UI
                dispose();
                new ShopDialog((Frame)getParent(), statsPanel).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Bạn không đủ tiền!");
            }
        });

        row.add(nameLabel, BorderLayout.WEST);
        row.add(btnBuy, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }
}
