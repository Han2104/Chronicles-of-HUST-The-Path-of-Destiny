package com.hust.game.ui.panels;

import com.hust.game.maps.sonla.SonLaMap;
import com.hust.game.models.items.Seed;
import com.hust.game.ui.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * FarmingPanel - Giao diện nông trại Sơn La với 48 ô đất.
 */
public class FarmingPanel extends JPanel {
    private BufferedImage backgroundImage;
    private SonLaMap sonLaLogic;
    private StatsPanel statsPanel;
    private GameWindow window;
    private JPanel gridPanel;

    public FarmingPanel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;
        this.sonLaLogic = new SonLaMap();

        setLayout(null); // Absolute Layout

        try {
            backgroundImage = ImageIO.read(new File("assets/sonla_map.png"));
        } catch (Exception e) {
            System.err.println("❌ Lỗi: Không tìm thấy assets/sonla_map.png");
        }

        // 1. Nút quay lại
        JButton btnBack = new JButton("⬅ Về World Map");
        btnBack.setBounds(10, 10, 150, 30);
        btnBack.addActionListener(e -> window.showPanel("WORLD_MAP"));
        add(btnBack);

        // 2. Lưới 48 ô đất (Tiếp tục đẩy lên cao)
        gridPanel = new JPanel(new GridLayout(6, 8, 2, 2)); 
        gridPanel.setOpaque(false);
        // Tọa độ mới tinh chỉnh: Y=260
        gridPanel.setBounds(60, 260, 750, 320); 

        setupPlots();
        add(gridPanel);

        // 3. Hotspot Shop Menu (Tiếp tục dịch trái và lên)
        createShopButton();
    }

    private void setupPlots() {
        for (int i = 0; i < 48; i++) {
            final int index = i;
            JButton plotBtn = new JButton();
            plotBtn.setOpaque(false);
            plotBtn.setContentAreaFilled(false);
            
            // Bỏ hoàn toàn mọi khung viền và hiệu ứng mặc định
            plotBtn.setBorder(null);
            plotBtn.setBorderPainted(false);
            plotBtn.setFocusPainted(false);
            plotBtn.setFocusable(false); // Ngăn không cho nút nhận focus để tránh hiện ô xám
            plotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            plotBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    // Dùng màu nền mờ để báo hiệu nhưng không bật contentAreaFilled để tránh lỗi xám
                    plotBtn.setBackground(new Color(255, 255, 255, 40)); 
                    plotBtn.setOpaque(false); 
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    plotBtn.setContentAreaFilled(false);
                }
            });

            plotBtn.addActionListener(e -> showActionMenu(index));
            gridPanel.add(plotBtn);
        }
    }

    private void createShopButton() {
        JButton btnShop = new JButton();
        // Vị trí tinh chỉnh: X=770, Y=230
        btnShop.setBounds(770, 230, 110, 120);
        btnShop.setOpaque(false);
        btnShop.setContentAreaFilled(false);
        // Bỏ hoàn toàn viền và hiệu ứng mặc định
        btnShop.setBorder(null); 
        btnShop.setBorderPainted(false);
        btnShop.setFocusPainted(false);
        btnShop.setFocusable(false); // Ngăn không cho nút nhận focus để tránh hiện ô xám
        btnShop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShop.setToolTipText("Mở Cửa hàng hạt giống");

        btnShop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnShop.setBackground(new Color(255, 255, 255, 30));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnShop.setBackground(new Color(0, 0, 0, 0));
            }
        });

        btnShop.addActionListener(e -> {
            ShopDialog dialog = new ShopDialog((Frame) SwingUtilities.getWindowAncestor(this), statsPanel);
            dialog.setVisible(true);
        });
        add(btnShop);
    }

    private void showActionMenu(int plotIndex) {
        com.hust.game.models.farming.FarmPlot plot = sonLaLogic.getPlot(plotIndex);
        if (plot == null) return;

        String[] options = {"Cuốc đất (-2⚡)", "Gieo hạt Ngô", "Gieo hạt Lúa", "Thu hoạch", "Hủy"};
        int choice = JOptionPane.showOptionDialog(this, "Hành động cho ô đất " + (plotIndex + 1), 
                "Quản lý Nông trại", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0: 
                sonLaLogic.interactWithPlot(plotIndex, "till", null); 
                break;
            case 1: // Gieo hạt Ngô
                sonLaLogic.interactWithPlot(plotIndex, "plant", Seed.CORN);
                break;
            case 2: // Gieo hạt Lúa
                sonLaLogic.interactWithPlot(plotIndex, "plant", Seed.PADDY);
                break;
            case 3: 
                sonLaLogic.interactWithPlot(plotIndex, "harvest", null); 
                break;
        }
        statsPanel.updateStats();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
