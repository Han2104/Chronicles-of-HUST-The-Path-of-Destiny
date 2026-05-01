package com.hust.game.ui.panels;

import com.hust.game.core.GameManager;
import com.hust.game.models.entities.LazyNPC;
import com.hust.game.models.entities.Player;

import javax.swing.*;
import java.awt.*;

/**
 * LazyEncounterDialog - Giao diện đối đầu với NPC Lười Biếng.
 */
public class LazyEncounterDialog extends JDialog {
    private StatsPanel statsPanel;
    private LazyNPC npc;

    public LazyEncounterDialog(JFrame parent, StatsPanel statsPanel, LazyNPC npc) {
        super(parent, "⚠️ CẢNH BÁO: CÁM DỖ XUẤT HIỆN!", true);
        this.statsPanel = statsPanel;
        this.npc = npc;

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // 1. Tiêu đề và lời thoại NPC
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(new Color(255, 235, 235));
        JLabel lblTitle = new JLabel("Bạn gặp: " + npc.getName(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel lblCatchphrase = new JLabel("\"" + npc.getCatchphrase() + "\"", SwingConstants.CENTER);
        lblCatchphrase.setFont(new Font("Arial", Font.ITALIC, 16));
        headerPanel.add(lblTitle);
        headerPanel.add(lblCatchphrase);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Nội dung thử thách
        JPanel centerPanel = new JPanel(new GridLayout(3, 1));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        centerPanel.add(new JLabel("Ngưỡng Ý Chí yêu cầu: " + npc.getRequiredWillpower()));
        centerPanel.add(new JLabel("Hình phạt nếu thua: -" + npc.getDisciplinePenalty() + " Điểm Rèn Luyện"));
        if (npc.getEnergyPenalty() > 0) {
            centerPanel.add(new JLabel("Mất thêm: -" + npc.getEnergyPenalty() + " Energy"));
        }
        add(centerPanel, BorderLayout.CENTER);

        // 3. Các nút hành động
        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 20, 100));

        JButton btnFight = new JButton("👊 Đối đầu bằng Ý Chí");
        JButton btnEscape = new JButton("🏃 Chạy trốn (-2 Kỷ luật)");
        JButton btnItem = new JButton("🎒 Dùng vật phẩm");

        btnFight.addActionListener(e -> handleFight());
        btnEscape.addActionListener(e -> handleEscape());
        btnItem.addActionListener(e -> handleItem());

        actionPanel.add(btnFight);
        actionPanel.add(btnEscape);
        actionPanel.add(btnItem);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleFight() {
        Player p = GameManager.getInstance().getPlayer();
        if (p.getWillpower() >= npc.getRequiredWillpower()) {
            p.addDisciplineScore(5);
            p.addExp(10);
            p.addLazyWin();
            
            String msg = "✅ CHIẾN THẮNG!\nBạn đã vượt qua cám dỗ.\n+5 Điểm Rèn Luyện, +10 EXP";
            if (p.getLazyWinStreak() == 3) {
                p.addDisciplineScore(20);
                msg += "\n🔥 CHUỖI 3 THẮNG! +20 Điểm Rèn Luyện Bonus!";
            }
            if (p.getLazyWins() == 10) {
                p.addWillpower(20);
                msg += "\n🏆 ĐÃ THẮNG 10 LẦN! Nhận danh hiệu 'Vượt Cám Dỗ', +20 Willpower vĩnh viễn!";
            }
            JOptionPane.showMessageDialog(this, msg);
            finish();
        } else {
            p.addDisciplineScore(-npc.getDisciplinePenalty());
            p.addEnergy(-npc.getEnergyPenalty());
            p.resetLazyWinStreak();
            if (npc.getLockCheckInMinutes() > 0) {
                p.lockCheckIn(npc.getLockCheckInMinutes());
            }
            JOptionPane.showMessageDialog(this, "❌ THẤT BẠI!\nBạn đã bị khuất phục bởi sự lười biếng.\nHình phạt đã được áp dụng.");
            finish();
        }
    }

    private void handleEscape() {
        long now = System.currentTimeMillis();
        long lastEscape = GameManager.getInstance().getLastEscapeTime();
        if (now - lastEscape < 600000) { // 10 phút cooldown
            JOptionPane.showMessageDialog(this, "⚠️ Kỹ năng 'Chạy trốn' đang hồi chiêu (10 phút)!");
            return;
        }

        Player p = GameManager.getInstance().getPlayer();
        p.addDisciplineScore(-2);
        GameManager.getInstance().setLastEscapeTime(now);
        JOptionPane.showMessageDialog(this, "🏃 Bạn đã chạy trốn thành công nhưng bị mất 2 điểm Kỷ luật vì hèn nhát!");
        finish();
    }

    private void handleItem() {
        Player p = GameManager.getInstance().getPlayer();
        String[] options = {"Cà Phê Đen (+15 Ý Chí)", "Nước Tăng Lực (Thắng chắc dạng Thường/Nguy hiểm)", "Sổ Ghi Chép (Kháng Penalty)", "Hủy"};
        int choice = JOptionPane.showOptionDialog(this, "Chọn vật phẩm để sử dụng:", "Hành trang",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0: // Cà phê
                if (p.useItem("Cà Phê Đen Robusta")) {
                    p.addWillpower(15);
                    JOptionPane.showMessageDialog(this, "☕ Đã uống Cà phê! Ý chí của bạn tăng vọt.");
                    handleFight();
                } else JOptionPane.showMessageDialog(this, "Bạn không có Cà phê!");
                break;
            case 1: // Monster
                if (p.useItem("Nước Tăng Lực Monster")) {
                    if (npc != LazyNPC.TEA_LORD) {
                        JOptionPane.showMessageDialog(this, "⚡ Monster power! Bạn đánh bại NPC ngay lập tức.");
                        p.addWillpower(100); // Hack để thắng chắc
                        handleFight();
                    } else {
                        JOptionPane.showMessageDialog(this, "⚡ Monster chỉ giúp bạn tự tin hơn, nhưng chưa đủ để thắng Chúa Tể Trà Đá!");
                        p.addWillpower(25);
                        handleFight();
                    }
                } else JOptionPane.showMessageDialog(this, "Bạn không có Monster!");
                break;
            case 2: // Sổ ghi chép
                if (p.hasStrategyNotebook()) {
                    JOptionPane.showMessageDialog(this, "📘 Bạn dùng Sổ ghi chép để hóa giải mọi lời cám dỗ. Bạn an toàn!");
                    finish();
                } else JOptionPane.showMessageDialog(this, "Bạn chưa sở hữu Sổ Ghi Chép Chiến Lược!");
                break;
        }
    }

    private void finish() {
        statsPanel.updateStats();
        dispose();
    }
}
