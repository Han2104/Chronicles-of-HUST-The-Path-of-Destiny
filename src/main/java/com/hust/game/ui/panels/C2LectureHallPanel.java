package com.hust.game.ui.panels;

import com.hust.game.core.GameManager;
import com.hust.game.models.entities.Player;
import com.hust.game.ui.GameWindow;
import com.hust.game.util.AssetLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * C2LectureHallPanel - Man hinh hoi truong truoc khi lam quiz sinh hoat cong dan.
 */
public class C2LectureHallPanel extends JPanel {
    private final GameWindow window;
    private final StatsPanel statsPanel;
    private final BufferedImage backgroundImage;

    public C2LectureHallPanel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;
        this.backgroundImage = AssetLoader.loadImage("assets/c2_lecture_hall.jpg");

        setLayout(new BorderLayout());
        setFocusable(true);
        buildControls();
        setupKeys();
    }

    private void buildControls() {
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        bottomBar.setOpaque(false);

        JButton backButton = new JButton("Quay lại sân C2");
        backButton.setFont(new Font("Arial", Font.BOLD, 15));
        backButton.addActionListener(e -> window.showPanel("MAP_C2"));

        JButton quizButton = new JButton("Bắt đầu trắc nghiệm");
        quizButton.setFont(new Font("Arial", Font.BOLD, 16));
        quizButton.addActionListener(e -> startCitizenActivityQuiz());

        bottomBar.add(backButton);
        bottomBar.add(quizButton);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void setupKeys() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "startQuiz");
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "startQuiz");
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "backC2");

        actionMap.put("startQuiz", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startCitizenActivityQuiz();
            }
        });
        actionMap.put("backC2", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                window.showPanel("MAP_C2");
            }
        });
    }

    private void startCitizenActivityQuiz() {
        Player player = GameManager.getInstance().getPlayer();
        if (player.isCompletedMap2()) {
            JOptionPane.showMessageDialog(this,
                    "Bạn đã hoàn thành buổi sinh hoạt công dân và đã nhận điểm rèn luyện.",
                    "C2",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        C2QuizDialog dialog = new C2QuizDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isPassed()) {
            player.addDisciplineScore(90);
            player.setCompletedMap2(true);
            statsPanel.updateStats();
            JOptionPane.showMessageDialog(this,
                    "Chúc mừng bạn đã trả lời đúng 5/5 câu hỏi.\n\nBạn đã có 90 điểm rèn luyện.",
                    "Hoàn Thành",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Bạn cần trả lời đúng toàn bộ 5 câu hỏi để hoàn thành buổi sinh hoạt công dân. Hãy thử lại.",
                    "Chưa Hoàn Thành",
                    JOptionPane.WARNING_MESSAGE);
        }
        requestFocusInWindow();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (backgroundImage != null) {
            drawCoverImage(g2d);
        } else {
            g2d.setColor(new Color(53, 32, 30));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 22));
            g2d.drawString("Thiếu ảnh assets/c2_lecture_hall.jpg", 32, 52);
        }

        drawBottomShade(g2d);
        g2d.dispose();
    }

    private void drawCoverImage(Graphics2D g2d) {
        double panelRatio = getWidth() / Math.max(1.0, (double) getHeight());
        double imageRatio = backgroundImage.getWidth() / (double) backgroundImage.getHeight();

        int drawWidth;
        int drawHeight;
        if (panelRatio > imageRatio) {
            drawWidth = getWidth();
            drawHeight = (int) Math.round(drawWidth / imageRatio);
        } else {
            drawHeight = getHeight();
            drawWidth = (int) Math.round(drawHeight * imageRatio);
        }

        int x = (getWidth() - drawWidth) / 2;
        int y = (getHeight() - drawHeight) / 2;
        g2d.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
    }

    private void drawTopOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 130));
        g2d.fillRoundRect(22, 20, 430, 58, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("", 42, 55);
    }

    private void drawBottomShade(Graphics2D g2d) {
        GradientPaint shade = new GradientPaint(
                0, getHeight() - 130, new Color(0, 0, 0, 0),
                0, getHeight(), new Color(0, 0, 0, 145));
        g2d.setPaint(shade);
        g2d.fillRect(0, Math.max(0, getHeight() - 130), getWidth(), 130);
    }

    private static class C2QuizDialog extends JDialog {
        private final Question[] questions = {
                new Question("Trường Đại học Bách khoa Hà Nội được thành lập vào năm nào?",
                        new String[]{"1966", "1956", "1936", "1946"}, 1),
                new Question("Hiệu trưởng đầu tiên của Đại học Bách khoa Hà Nội là ai?",
                        new String[]{"GS. Trần Đại Nghĩa", "GS. Tạ Quang Bửu", "PGS.TS. Hoàng Minh Sơn", "PGS.TS. Huỳnh Quyết Thắng"}, 0),
                new Question("Thang điểm được sử dụng để tính điểm trung bình tích lũy (CPA) xét tốt nghiệp tại HUST là thang điểm mấy?",
                        new String[]{"Thang điểm 100", "Thang điểm 4", "Thang điểm 10", "Thang điểm 5"}, 1),
                new Question("Hành vi nào sau đây bị nghiêm cấm và xử lý kỷ luật nghiêm khắc nhất trong các kỳ thi tại Đại học Bách khoa Hà Nội?",
                        new String[]{"Dùng bút mực màu xanh để làm bài tự luận", "Gian lận, sử dụng tài liệu, thiết bị công nghệ cao hoặc thi hộ", "Xin thêm giấy nháp từ cán bộ coi thi", "Đến phòng thi muộn trước khi tính giờ làm bài 5 phút"}, 1),
                new Question("Thư viện nào sau đây được coi là thư viện lớn nhất Đông Nam Á?",
                        new String[]{"Thư viện Quốc gia Việt Nam", "Thư viện Tạ Quang Bửu", "Thư viện Trung tâm Văn hóa Hàn Quốc", "Thư viện Hà Nội"}, 1)
        };

        private final JTextArea questionText = new JTextArea();
        private final JLabel progressLabel = new JLabel();
        private final JButton[] answerButtons = new JButton[4];
        private final BufferedImage quizFrame;
        private int index = 0;
        private int correct = 0;
        private boolean passed = false;

        C2QuizDialog(Window owner) {
            super(owner, "Sinh Hoạt Công Dân C2", ModalityType.APPLICATION_MODAL);
            this.quizFrame = AssetLoader.loadImage("assets/c2_quiz_frame.jpg");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(980, 880);
            setResizable(false);
            setLocationRelativeTo(owner);
            buildQuizUi();
            showQuestion();
        }

        boolean isPassed() {
            return passed;
        }

        private void buildUi() {
            JPanel root = new JPanel(new BorderLayout(14, 14));
            root.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
            root.setBackground(new Color(15, 49, 78));
            setContentPane(root);

            JLabel title = new JLabel("SINH HOẠT CÔNG DÂN C2");
            title.setForeground(new Color(255, 184, 73));
            title.setFont(new Font("Arial", Font.BOLD, 28));
            progressLabel.setForeground(Color.WHITE);
            progressLabel.setFont(new Font("Arial", Font.BOLD, 16));
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.add(title, BorderLayout.WEST);
            header.add(progressLabel, BorderLayout.EAST);
            root.add(header, BorderLayout.NORTH);

            questionText.setEditable(false);
            questionText.setLineWrap(true);
            questionText.setWrapStyleWord(true);
            questionText.setFont(new Font("Arial", Font.BOLD, 18));
            questionText.setForeground(Color.WHITE);
            questionText.setBackground(new Color(8, 32, 56));
            questionText.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            root.add(questionText, BorderLayout.CENTER);

            JPanel answers = new JPanel(new GridLayout(4, 1, 8, 8));
            answers.setOpaque(false);
            for (int i = 0; i < answerButtons.length; i++) {
                final int selected = i;
                answerButtons[i] = new JButton();
                answerButtons[i].setFont(new Font("Arial", Font.BOLD, 15));
                answerButtons[i].addActionListener(e -> answer(selected));
                answers.add(answerButtons[i]);
            }
            root.add(answers, BorderLayout.SOUTH);
        }

        private void buildQuizUi() {
            QuizFramePanel root = new QuizFramePanel(quizFrame);
            root.setLayout(null);
            setContentPane(root);

            progressLabel.setForeground(new Color(83, 47, 31));
            progressLabel.setFont(new Font("Arial", Font.BOLD, 16));
            progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
            progressLabel.setOpaque(false);
            root.add(progressLabel);

            questionText.setEditable(false);
            questionText.setLineWrap(true);
            questionText.setWrapStyleWord(true);
            questionText.setFont(new Font("Arial", Font.BOLD, 24));
            questionText.setForeground(new Color(75, 43, 26));
            questionText.setOpaque(false);
            questionText.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            root.add(questionText);

            for (int i = 0; i < answerButtons.length; i++) {
                final int selected = i;
                answerButtons[i] = new JButton();
                answerButtons[i].setFont(new Font("Arial", Font.BOLD, 20));
                answerButtons[i].setForeground(new Color(75, 43, 26));
                answerButtons[i].setOpaque(false);
                answerButtons[i].setContentAreaFilled(false);
                answerButtons[i].setBorderPainted(false);
                answerButtons[i].setFocusPainted(false);
                answerButtons[i].setHorizontalAlignment(SwingConstants.LEFT);
                answerButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
                answerButtons[i].addActionListener(e -> answer(selected));
                root.add(answerButtons[i]);
            }

            root.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    layoutQuizComponents(root.getWidth(), root.getHeight());
                }
            });
            layoutQuizComponents(getWidth(), getHeight());
        }

        private void layoutQuizComponents(int width, int height) {
            int w = Math.max(1, width);
            int h = Math.max(1, height);
            progressLabel.setBounds((int) (w * 0.69), (int) (h * 0.045), (int) (w * 0.25), (int) (h * 0.06));
            questionText.setBounds((int) (w * 0.075), (int) (h * 0.16), (int) (w * 0.855), (int) (h * 0.27));

            int leftX = (int) (w * 0.075);
            int rightX = (int) (w * 0.52);
            int boxW = (int) (w * 0.405);
            int boxH = (int) (h * 0.105);
            int row1 = (int) (h * 0.58);
            int row2 = (int) (h * 0.80);
            answerButtons[0].setBounds(leftX, row1, boxW, boxH);
            answerButtons[1].setBounds(rightX, row1, boxW, boxH);
            answerButtons[2].setBounds(leftX, row2, boxW, boxH);
            answerButtons[3].setBounds(rightX, row2, boxW, boxH);
        }

        private void showQuestion() {
            Question question = questions[index];
            progressLabel.setText("Câu " + (index + 1) + "/" + questions.length);
            questionText.setText(question.text);
            for (int i = 0; i < answerButtons.length; i++) {
                answerButtons[i].setText((char) ('A' + i) + ". " + question.options[i]);
                answerButtons[i].setEnabled(true);
            }
        }

        private void answer(int selected) {
            Question question = questions[index];
            if (selected == question.correctIndex) {
                correct++;
            }

            if (index < questions.length - 1) {
                index++;
                showQuestion();
                return;
            }

            passed = correct == questions.length;
            JOptionPane.showMessageDialog(this,
                    "Bạn đã trả lời đúng " + correct + "/" + questions.length + " câu.",
                    "Kết quả",
                    passed ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            dispose();
        }

        private static class QuizFramePanel extends JPanel {
            private final BufferedImage image;

            QuizFramePanel(BufferedImage image) {
                this.image = image;
                setBackground(new Color(118, 65, 34));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                }
            }
        }

        private static class Question {
            final String text;
            final String[] options;
            final int correctIndex;

            Question(String text, String[] options, int correctIndex) {
                this.text = text;
                this.options = options;
                this.correctIndex = correctIndex;
            }
        }
    }
}
