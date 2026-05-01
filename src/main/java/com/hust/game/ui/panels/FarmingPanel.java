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

    private JButton btnBack;
    private JButton btnShop;
    private JButton btnGardenArea;
    
    // Kích thước chuẩn khi thiết kế (Base resolution)
    private final double BASE_W = 1000.0;
    private final double BASE_H = 650.0; // Khoảng trống còn lại sau StatsPanel

    public FarmingPanel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;
        this.sonLaLogic = new SonLaMap();

        setLayout(null); 

        try {
            backgroundImage = ImageIO.read(new File("assets/sonla_map.png"));
        } catch (Exception e) {
            System.err.println("❌ Lỗi: Không tìm thấy assets/sonla_map.png");
        }

        // 1. Nút quay lại
        btnBack = new JButton("⬅ Về World Map");
        btnBack.addActionListener(e -> window.showPanel("WORLD_MAP"));
        add(btnBack);

        // 2. Vùng bấm "Khu vườn" (Bao phủ toàn bộ 48 ô đất)
        btnGardenArea = new JButton();
        btnGardenArea.setOpaque(false);
        btnGardenArea.setContentAreaFilled(false);
        btnGardenArea.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1)); // Viền mờ để nhận diện vùng
        btnGardenArea.setFocusPainted(false);
        btnGardenArea.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGardenArea.setToolTipText("Bấm để quản lý khu vườn");
        btnGardenArea.addActionListener(e -> selectPlotAndAct());
        add(btnGardenArea);

        // 3. Hotspot Shop Menu
        btnShop = createShopButton();
        add(btnShop);

        // Lắng nghe sự kiện thay đổi kích thước cửa sổ
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repositionComponents();
            }
        });
    }

    private void repositionComponents() {
        double scaleX = getWidth() / BASE_W;
        double scaleY = getHeight() / BASE_H;

        // Cập nhật vị trí nút Back
        btnBack.setBounds((int)(10 * scaleX), (int)(10 * scaleY), (int)(150 * scaleX), (int)(30 * scaleY));

        // Vùng vườn (Gốc: 60, 260, 750, 320)
        btnGardenArea.setBounds((int)(60 * scaleX), (int)(260 * scaleY), (int)(750 * scaleX), (int)(320 * scaleY));

        // Cập nhật vị trí nút Shop (Gốc: 770, 230, 110, 120)
        btnShop.setBounds((int)(770 * scaleX), (int)(230 * scaleY), (int)(110 * scaleX), (int)(120 * scaleY));
        
        revalidate();
        repaint();
    }

    private void selectPlotAndAct() {
        // Tạo danh sách 48 ô đất
        String[] plots = new String[48];
        for (int i = 0; i < 48; i++) {
            plots[i] = "Ô đất số " + (i + 1);
        }

        String selectedPlot = (String) JOptionPane.showInputDialog(this, 
                "Chọn ô đất bạn muốn quản lý:", 
                "Quản lý Khu vườn", 
                JOptionPane.PLAIN_MESSAGE, 
                null, 
                plots, 
                plots[0]);

        if (selectedPlot != null) {
            // Lấy index từ chuỗi "Ô đất số X"
            int plotIndex = Integer.parseInt(selectedPlot.replace("Ô đất số ", "")) - 1;
            showActionMenu(plotIndex);
        }
    }

    private JButton createShopButton() {
        JButton btnShop = new JButton();
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
        return btnShop;
    }

    private void showActionMenu(int plotIndex) {
        com.hust.game.models.farming.FarmPlot plot = sonLaLogic.getPlot(plotIndex);
        if (plot == null) return;

        String[] options = {"Cuốc đất (-2⚡)", "Gieo hạt Ngô", "Gieo hạt Lúa", "Thu hoạch", "Hủy"};
        String message = "Đang chọn: Ô đất số " + (plotIndex + 1) + "\nTrạng thái: " + plot.getStatusText();
        
        int choice = JOptionPane.showOptionDialog(this, message, 
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
