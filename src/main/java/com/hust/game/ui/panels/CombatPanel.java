package com.hust.game.ui.panels;

import com.hust.game.ui.GameWindow;
import com.hust.game.models.entities.Player;
import com.hust.game.models.combat.Boss;
import com.hust.game.core.GameManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class CombatPanel extends JPanel {
    private GameWindow window;
    private StatsPanel statsPanel;
    private Boss currentBoss;
    private JProgressBar bossHPBar;
    private JProgressBar playerHPBar;
    private JTextArea combatLog;
    private BufferedImage bossImage;

    public CombatPanel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        // Load Boss Image
        try {
            bossImage = ImageIO.read(new File("assets/boss.png"));
        } catch (Exception e) {
            System.err.println("❌ Lỗi: Không tìm thấy assets/boss.png");
        }

        setupUI();
    }

    public void startCombat(Boss boss) {
        this.currentBoss = boss;
        bossHPBar.setMaximum(boss.maxHp);
        bossHPBar.setValue(boss.hp);
        playerHPBar.setMaximum(GameManager.getInstance().getPlayer().getMaxHpCombat());
        playerHPBar.setValue(GameManager.getInstance().getPlayer().getHp());
        combatLog.setText("--- TRẬN CHIẾN BẮT ĐẦU ---\nĐối mặt với: " + boss.name + "\n" + boss.description + "\n");
        repaint();
    }

    private void setupUI() {
        // Boss Area
        JPanel bossArea = new JPanel(new BorderLayout());
        bossArea.setOpaque(false);
        
        bossHPBar = new JProgressBar(0, 100);
        bossHPBar.setStringPainted(true);
        bossHPBar.setForeground(Color.RED);
        bossHPBar.setBackground(Color.DARK_GRAY);
        bossArea.add(bossHPBar, BorderLayout.NORTH);

        JLabel bossLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bossImage != null) {
                    // Vẽ Boss (Tạm thời vẽ toàn bộ ảnh, sau này có thể crop)
                    g.drawImage(bossImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        bossArea.add(bossLabel, BorderLayout.CENTER);
        add(bossArea, BorderLayout.CENTER);

        // Player Info and Actions
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.setPreferredSize(new Dimension(1000, 200));

        // Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Hành động"));
        
        JButton btnAnswer = createActionButton("📖 Trả lời (Tấn công)", Color.GREEN);
        JButton btnItem = createActionButton("🎒 Vật phẩm", Color.ORANGE);
        JButton btnDefend = createActionButton("🛡️ Phòng thủ", Color.BLUE);
        JButton btnHelp = createActionButton("🤝 Cầu cứu", Color.MAGENTA);

        actionPanel.add(btnAnswer);
        actionPanel.add(btnItem);
        actionPanel.add(btnDefend);
        actionPanel.add(btnHelp);
        
        // Log and Stats
        JPanel infoPanel = new JPanel(new BorderLayout());
        playerHPBar = new JProgressBar(0, 100);
        playerHPBar.setStringPainted(true);
        playerHPBar.setForeground(new Color(50, 205, 50));
        infoPanel.add(playerHPBar, BorderLayout.NORTH);

        combatLog = new JTextArea();
        combatLog.setEditable(false);
        combatLog.setBackground(Color.BLACK);
        combatLog.setForeground(Color.WHITE);
        JScrollPane scrollLog = new JScrollPane(combatLog);
        infoPanel.add(scrollLog, BorderLayout.CENTER);

        bottomPanel.add(actionPanel);
        bottomPanel.add(infoPanel);
        add(bottomPanel, BorderLayout.SOUTH);

        // Logic Actions
        btnAnswer.addActionListener(e -> playerTurn());
        btnDefend.addActionListener(e -> {
            log("Vũ đang phòng thủ! Giảm sát thương lượt sau.");
            bossTurn();
        });
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void playerTurn() {
        // Logic trả lời câu hỏi sẽ được tích hợp ở đây
        int damage = 30 + GameManager.getInstance().getPlayer().getSolutionSkill() / 2;
        currentBoss.hp -= damage;
        bossHPBar.setValue(currentBoss.hp);
        log("Vũ tung đòn 'Kiến thức là sức mạnh'! Gây " + damage + " sát thương.");

        if (currentBoss.hp <= 0) {
            log("CHIẾN THẮNG! " + currentBoss.name + " đã bị thuyết phục.");
            JOptionPane.showMessageDialog(this, "Bạn đã chiến thắng Boss!");
            window.showPanel("MAP_B1");
        } else {
            bossTurn();
        }
    }

    private void bossTurn() {
        Timer timer = new Timer(1000, e -> {
            Boss.Skill skill = currentBoss.skills.get((int)(Math.random() * currentBoss.skills.size()));
            int dmg = skill.damage;
            GameManager.getInstance().getPlayer().addHp(-dmg);
            playerHPBar.setValue(GameManager.getInstance().getPlayer().getHp());
            log(currentBoss.name + " dùng kỹ năng: [" + skill.name + "]! Gây " + dmg + " sát thương.");
            
            if (GameManager.getInstance().getPlayer().getHp() <= 0) {
                log("THẤT BẠI... Vũ đã kiệt sức.");
                JOptionPane.showMessageDialog(this, "Bạn đã thất bại trước Boss.");
                window.showPanel("WORLD_MAP");
            }
            statsPanel.updateStats();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void log(String msg) {
        combatLog.append(msg + "\n");
        combatLog.setCaretPosition(combatLog.getDocument().getLength());
    }
}
