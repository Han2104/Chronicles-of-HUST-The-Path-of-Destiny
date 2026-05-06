package com.hust.game.ui.panels;

import com.hust.game.core.GameManager;
import com.hust.game.ui.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * C2Panel - Giao diện Ký túc xá C2 với các hoạt động Check-in và Làm thêm.
 */
public class C2Panel extends JPanel {
    private BufferedImage backgroundImage;
    private BufferedImage[] playerSprites = new BufferedImage[4]; // 0: Down, 1: Left, 2: Right, 3: Up
    private int currentDir = 0; 
    private StatsPanel statsPanel;
    private GameWindow window;

    private JButton btnBack;
    private JButton btnCheckIn;
    private JButton btnWork;
    private JButton btnShop;
    
    // Tọa độ nhân vật Vũ (Tỉ lệ 1000x650)
    private int playerX = 250;
    private int playerY = 480;
    private final int playerSpeed = 15;
    
    private final double BASE_W = 500.0;
    private final double BASE_H = 550.0;

    public C2Panel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;

        setLayout(null); 
        setFocusable(true);

        try {
            backgroundImage = ImageIO.read(new File("assets/c2_map.png"));
            
            // Nạp từng ảnh từ thư mục assets/Vu/ (Lưu ý chữ V viết hoa)
            playerSprites[0] = ImageIO.read(new File("assets/Vu/character_stand_front (1).png")); // Down
            playerSprites[1] = ImageIO.read(new File("assets/Vu/character_stand_left (1).png"));  // Left
            playerSprites[2] = ImageIO.read(new File("assets/Vu/character_stand_right (1).png")); // Right
            playerSprites[3] = ImageIO.read(new File("assets/Vu/character_stand_back (1).png"));  // Up
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi: Không tìm thấy ảnh trong assets/Vu/. Hãy kiểm tra lại tên tệp.");
        }
        
        // ... (Giữ nguyên các nút bấm)

        // 1. Nút quay lại
        btnBack = new JButton("⬅ Về World Map");
        btnBack.addActionListener(e -> window.showPanel("WORLD_MAP"));
        add(btnBack);

        // 2. Nút Check-in (Tòa Bách Khoa)
        btnCheckIn = createTransparentButton("Check-in Chuyên cần", "Đến gần tòa Bách Khoa để điểm danh");
        btnCheckIn.addActionListener(e -> {
            if (isPlayerNear(350, 80, 250, 220)) {
                GameManager.getInstance().handleMap2Actions("CHECKIN");
            } else {
                showProximityWarning("Tòa Bách Khoa");
            }
        });
        add(btnCheckIn);

        // 3. Nút Làm thêm (Khu sân cỏ/ghế đá bên trái)
        btnWork = createTransparentButton("Làm thêm", "Đến gần khu vực ghế đá để làm thêm");
        btnWork.addActionListener(e -> {
            if (isPlayerNear(20, 400, 380, 250)) {
                GameManager.getInstance().handleMap2Actions("WORK");
            } else {
                showProximityWarning("khu vực Ghế đá");
            }
        });
        add(btnWork);

        // 4. Nút Cửa hàng C2 (Tòa Shop bên phải)
        btnShop = createTransparentButton("Cửa hàng C2", "Đến gần tòa Shop để mua đồ");
        btnShop.addActionListener(e -> {
            if (isPlayerNear(620, 150, 350, 300)) {
                ShopDialog dialog = new ShopDialog((Frame) SwingUtilities.getWindowAncestor(this), statsPanel);
                dialog.setVisible(true);
            } else {
                showProximityWarning("Cửa hàng");
            }
        });
        add(btnShop);

        setupMovement();

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repositionComponents();
            }
        });
    }

    private void setupMovement() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("UP"), "moveUp");
        im.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        im.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");

        am.put("moveUp", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { movePlayer(0, -playerSpeed, 3); } });
        am.put("moveDown", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { movePlayer(0, playerSpeed, 0); } });
        am.put("moveLeft", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { movePlayer(-playerSpeed, 0, 1); } });
        am.put("moveRight", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { movePlayer(playerSpeed, 0, 2); } });
    }

    private void movePlayer(int dx, int dy, int dir) {
        this.currentDir = dir;
        playerX = Math.max(50, Math.min(950, playerX + dx));
        playerY = Math.max(100, Math.min(600, playerY + dy));
        repaint();
    }

    private boolean isPlayerNear(int x, int y, int w, int h) {
        // Kiểm tra xem tâm nhân vật có nằm gần vùng tương tác không
        int centerX = playerX;
        int centerY = playerY;
        
        // Khoảng cách tối đa để tương tác (Tăng lên 150 pixel cho dễ bấm)
        int threshold = 150;
        
        // Tìm điểm gần nhất trong HCN tới (centerX, centerY)
        int nearestX = Math.max(x, Math.min(centerX, x + w));
        int nearestY = Math.max(y, Math.min(centerY, y + h));
        
        double dist = Math.sqrt(Math.pow(centerX - nearestX, 2) + Math.pow(centerY - nearestY, 2));
        return dist <= threshold;
    }

    private void showProximityWarning(String area) {
        JOptionPane.showMessageDialog(this, "🏃 Bạn đứng quá xa! Hãy di chuyển Vũ tới gần " + area + " để tương tác.");
    }

    private void repositionComponents() {
        double scaleX = getWidth() / BASE_W;
        double scaleY = getHeight() / BASE_H;

        btnBack.setBounds((int)(10 * scaleX), (int)(10 * scaleY), (int)(150 * scaleX), (int)(30 * scaleY));
        
        // Cập nhật vị trí Hotspot dựa trên ảnh thực tế của User
        // Tòa Bách Khoa (Check-in) - Nằm phía trên bên trái/giữa
        btnCheckIn.setBounds((int)(350 * scaleX), (int)(100 * scaleY), (int)(250 * scaleX), (int)(200 * scaleY));
        
        // Khu vực làm thêm (Sân cỏ/Ghế đá bên trái)
        btnWork.setBounds((int)(50 * scaleX), (int)(400 * scaleY), (int)(350 * scaleX), (int)(250 * scaleY));
        
        // Tòa Shop (Cửa hàng) - Nằm phía trên bên phải
        btnShop.setBounds((int)(650 * scaleX), (int)(150 * scaleY), (int)(300 * scaleX), (int)(250 * scaleY));
        
        revalidate();
        repaint();
    }

    private JButton createTransparentButton(String text, String tooltip) {
        JButton btn = new JButton();
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); 
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        
        // Không vẽ viền kể cả khi hover để ẩn hoàn toàn vùng bấm
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        double scaleX = getWidth() / BASE_W;
        double scaleY = getHeight() / BASE_H;

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        
        // Vẽ nhân vật Vũ (theo hướng)
        if (playerSprites[currentDir] != null) {
            int drawX = (int)(playerX * scaleX) - 40;
            int drawY = (int)(playerY * scaleY) - 80;
            g.drawImage(playerSprites[currentDir], drawX, drawY, 80, 80, this);
        }
        
        // Vẽ đồng hồ Game
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String timeStr = "🕓 Giờ hệ thống: " + GameManager.getInstance().getGameHour() + ":00";
        g.drawString(timeStr, getWidth() - 250, 30);
    }
}
