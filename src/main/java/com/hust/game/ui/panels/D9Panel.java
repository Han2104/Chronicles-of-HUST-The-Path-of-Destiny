package com.hust.game.ui.panels;

import com.hust.game.ui.GameWindow;
import com.hust.game.models.entities.Player;
import com.hust.game.models.d9.D9Maze;
import com.hust.game.core.GameManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * D9Panel - Giao diện Mê cung Đại cương 7 tầng.
 */
public class D9Panel extends JPanel {
    private GameWindow window;
    private StatsPanel statsPanel;
    private D9Maze mazeLogic = new D9Maze();
    private final double BASE_W = 1000.0;
    private final double BASE_H = 650.0;

    public D9Panel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;
        
        setLayout(new BorderLayout());
        setBackground(new Color(40, 44, 52));

        // Tiêu đề
        JLabel title = new JLabel("TÒA NHÀ D9: MÊ CUNG ĐẠI CƯƠNG", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(255, 215, 0));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Danh sách tầng
        JPanel floorsPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        floorsPanel.setOpaque(false);
        floorsPanel.setBorder(BorderFactory.createEmptyBorder(10, 150, 10, 150));

        for (int i = 7; i >= 1; i--) {
            final int floorNum = i;
            JButton btnFloor = createFloorButton(floorNum);
            btnFloor.addActionListener(e -> startFloorChallenge(floorNum));
            floorsPanel.add(btnFloor);
        }
        add(floorsPanel, BorderLayout.CENTER);

        // Nút quay lại
        JButton btnBack = new JButton("⬅ Về World Map");
        btnBack.addActionListener(e -> window.showPanel("WORLD_MAP"));
        add(btnBack, BorderLayout.SOUTH);
    }

    private JButton createFloorButton(int floor) {
        String text = "TẦNG " + floor + ": " + getFloorSubject(floor);
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 18));
        btn.setBackground(new Color(60, 63, 65));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Chỉ cho phép tầng hiện tại của người chơi
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(60, 63, 65));
            }
        });
        
        return btn;
    }

    private String getFloorSubject(int floor) {
        switch (floor) {
            case 1: return "Giải tích: Giới hạn & Đạo hàm";
            case 2: return "Giải tích: Tích phân";
            case 3: return "Đại số Tuyến tính: Ma trận";
            case 4: return "Đại số: Không gian Vector";
            case 5: return "OOP: Lý thuyết Class & Inheritance";
            case 6: return "OOP: Polymorphism & Interface";
            case 7: return "BOSS MINI: Trợ lý Giảng đường";
            default: return "Thử thách Tư duy";
        }
    }

    private void startFloorChallenge(int floor) {
        Player p = GameManager.getInstance().getPlayer();
        if (p.getCurrentD9Floor() < floor) {
            JOptionPane.showMessageDialog(this, "Bạn cần vượt qua Tầng " + p.getCurrentD9Floor() + " trước!");
            return;
        }

        List<D9Maze.Question> questions = mazeLogic.getQuestionsForFloor(floor);
        int score = 0;
        
        for (D9Maze.Question q : questions) {
            int choice = JOptionPane.showOptionDialog(this, q.questionText, 
                    "Thử thách Tầng " + floor, JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, null, q.options, q.options[0]);
            
            if (choice == q.correctIndex) {
                score++;
            } else {
                JOptionPane.showMessageDialog(this, "Sai rồi! Hãy ôn tập kỹ hơn.");
                return;
            }
        }

        // Vượt qua tầng thành công
        if (score == questions.size()) {
            grantFloorRewards(floor);
            if (p.getCurrentD9Floor() == floor) {
                p.setCurrentD9Floor(floor + 1);
            }
            JOptionPane.showMessageDialog(this, "Chúc mừng! Bạn đã chinh phục Tầng " + floor);
            statsPanel.updateStats();
        }
    }

    private void grantFloorRewards(int floor) {
        Player p = GameManager.getInstance().getPlayer();
        switch (floor) {
            case 1: p.addSolutionSkill(10); p.addFinance(20); break;
            case 2: p.addSolutionSkill(15); p.addFinance(30); break;
            case 3: p.addSolutionSkill(15); break;
            case 4: p.addSolutionSkill(20); p.addFinance(40); break;
            case 5: p.addSolutionSkill(20); p.setHasOOPBook(true); break;
            case 6: p.addSolutionSkill(25); p.addFinance(60); break;
            case 7: p.addSolutionSkill(30); p.setCompletedMap2(true); break;
        }
    }
}
