package com.hust.game;

import com.hust.game.ui.GameWindow;
import javax.swing.SwingUtilities;

/**
 * Main - Điểm khởi đầu của chương trình (Khởi chạy GUI).
 */
public class Main {
    public static void main(String[] args) {
        // Khởi chạy giao diện trên Event Dispatch Thread để đảm bảo an toàn luồng
        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}
