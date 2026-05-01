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
        
        setSize(550, 500);
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

        // Thêm Gói Cơm Nắm (8 VNĐ, +5 Energy)
        addConsumableRow(itemsPanel, "Gói Cơm Nắm", 8, 5, 0);

        // Thêm Bình Nước Sơn La (15 VNĐ, +8 Energy, +10% Willpower)
        addConsumableRow(itemsPanel, "Bình Nước Sơn La", 15, 8, 0.10);

        // Thêm Cuốc Đất Sắt (Miễn phí - Thưởng quest)
        addEquipmentRow(itemsPanel, "Cuốc Đất Sắt", 0);

        add(new JScrollPane(itemsPanel), BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        add(btnClose, BorderLayout.SOUTH);
    }

    private void addItemRow(JPanel panel, Seed seed) {
        Player player = GameManager.getInstance().getPlayer();
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(500, 60));
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

    private void addConsumableRow(JPanel panel, String name, int price, int energyGain, double wpBuff) {
        Player player = GameManager.getInstance().getPlayer();
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(500, 60));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        String labelText = String.format("%s (%d VNĐ) [+%d⚡]", name, price, energyGain);
        if (wpBuff > 0) labelText += " [+10% 💪]";
        
        JLabel nameLabel = new JLabel(labelText);
        JButton btnBuy = new JButton("Mua & Dùng");

        btnBuy.addActionListener(e -> {
            if (player.getFinance() >= price) {
                player.addFinance(-price);
                player.addEnergy(energyGain);
                if (wpBuff > 0) {
                    player.startWillpowerBuff(1.0 + wpBuff, 180000); // 3 phút = 180,000ms
                }
                System.out.println("🍴 Đã dùng " + name + "!");
                statsPanel.updateStats();
                dispose();
                new ShopDialog((Frame)getParent(), statsPanel).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Bạn không đủ tiền!");
            }
        });

        row.add(nameLabel, BorderLayout.WEST);
        row.add(btnBuy, BorderLayout.EAST);
        panel.add(row);
    }

    private void addEquipmentRow(JPanel panel, String name, int price) {
        Player player = GameManager.getInstance().getPlayer();
        if (player.isIronHoeEquipped()) return; // Đã trang bị rồi thì không hiện nữa

        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(500, 60));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel nameLabel = new JLabel(name + " (MIỄN PHÍ) [+25% 🌾]");
        JButton btnGet = new JButton("Nhận trang bị");

        btnGet.addActionListener(e -> {
            player.setIronHoeEquipped(true);
            JOptionPane.showMessageDialog(this, "Chúc mừng! Bạn đã trang bị " + name + ".");
            dispose();
            new ShopDialog((Frame)getParent(), statsPanel).setVisible(true);
        });

        row.add(nameLabel, BorderLayout.WEST);
        row.add(btnGet, BorderLayout.EAST);
        panel.add(row);
    }
}
