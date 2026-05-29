package com.hust.game.maps.d9;

import com.hust.game.util.AssetLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * D9QuizManager - Quản lý quiz logic
 */
public class D9QuizManager {
    private final D9QuestionBank questionBank;
    private final D9Settings settings;

    public D9QuizManager(D9QuestionBank questionBank, D9Settings settings) {
        this.questionBank = questionBank;
        this.settings = settings;
    }

    public QuizResult conductQuiz(Component parent, D9Object door) {
        String questionSet = door.getProperty("questionSet");
        int floor = door.getFloor();
        int questionCount = door.getPropertyAsInt("questionCount", settings.getQuestionCount());
        int requiredCorrect = door.getPropertyAsInt("requiredCorrect",
                "FinalQuizDoor".equals(door.getClassType())
                        ? settings.getRequiredCorrectFinal()
                        : settings.getRequiredCorrectNormal());

        List<D9QuestionBank.Question> questions = questionBank.getQuestionsForSet(questionSet, questionCount);

        if (questions.isEmpty()) {
            System.err.println("[D9] Warning: no questions found for set " + questionSet);
            return new QuizResult(0, 0, requiredCorrect, false);
        }

        if (questions.size() < questionCount) {
            System.err.println("[D9] Warning: question set " + questionSet + " has only "
                    + questions.size() + " questions; requested " + questionCount + ".");
        }

        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        QuizDialog dialog = new QuizDialog(owner, floor, questions, requiredCorrect);
        dialog.setVisible(true);
        int correct = dialog.getCorrectCount();
        return new QuizResult(correct, questions.size(), requiredCorrect, correct >= requiredCorrect);
    }

    public static class QuizResult {
        public final int correct;
        public final int total;
        public final int requiredCorrect;
        public final boolean passed;

        public QuizResult(int correct, int total, int requiredCorrect, boolean passed) {
            this.correct = correct;
            this.total = total;
            this.requiredCorrect = requiredCorrect;
            this.passed = passed;
        }
    }

    private static class QuizDialog extends JDialog {
        private final int floor;
        private final List<D9QuestionBank.Question> questions;
        private final int requiredCorrect;
        private final JButton[] answerButtons = new JButton[4];
        private final JLabel titleLabel = new JLabel();
        private final JLabel progressLabel = new JLabel();
        private final JTextArea questionText = new JTextArea();
        private final JTextArea feedbackText = new JTextArea();
        private final JButton nextButton = new JButton("Tiếp tục");
        private final BufferedImage backgroundImage;
        private int currentIndex = 0;
        private int correctCount = 0;
        private boolean answered = false;

        QuizDialog(Window owner, int floor, List<D9QuestionBank.Question> questions, int requiredCorrect) {
            super(owner, "Thử thách OOP - Tầng " + floor, ModalityType.APPLICATION_MODAL);
            this.floor = floor;
            this.questions = questions;
            this.requiredCorrect = requiredCorrect;
            this.backgroundImage = AssetLoader.loadImage("assets/Map/D9/d9_quiz_panel.png");

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(700, 520);
            setLocationRelativeTo(owner);
            setContentPane(new BackgroundPanel());
            buildUi();
            showQuestion();
        }

        int getCorrectCount() {
            return correctCount;
        }

        private void buildUi() {
            JPanel root = (JPanel) getContentPane();
            root.setLayout(new BorderLayout(14, 14));
            root.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            progressLabel.setFont(new Font("Arial", Font.BOLD, 16));
            progressLabel.setForeground(new Color(230, 242, 255));

            JPanel header = transparentPanel(new BorderLayout());
            header.add(titleLabel, BorderLayout.WEST);
            header.add(progressLabel, BorderLayout.EAST);
            root.add(header, BorderLayout.NORTH);

            questionText.setOpaque(false);
            questionText.setEditable(false);
            questionText.setLineWrap(true);
            questionText.setWrapStyleWord(true);
            questionText.setForeground(Color.WHITE);
            questionText.setFont(new Font("Arial", Font.BOLD, 18));
            questionText.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JPanel center = transparentPanel(new BorderLayout(12, 12));
            center.add(questionText, BorderLayout.NORTH);

            JPanel answers = transparentPanel(new GridLayout(4, 1, 8, 8));
            for (int i = 0; i < answerButtons.length; i++) {
                final int selectedIndex = i;
                answerButtons[i] = new JButton();
                answerButtons[i].setFont(new Font("Arial", Font.PLAIN, 15));
                answerButtons[i].setFocusPainted(false);
                answerButtons[i].addActionListener(e -> answer(selectedIndex));
                answers.add(answerButtons[i]);
            }
            center.add(answers, BorderLayout.CENTER);

            feedbackText.setOpaque(false);
            feedbackText.setEditable(false);
            feedbackText.setLineWrap(true);
            feedbackText.setWrapStyleWord(true);
            feedbackText.setForeground(new Color(255, 244, 190));
            feedbackText.setFont(new Font("Arial", Font.PLAIN, 15));
            feedbackText.setRows(4);
            center.add(feedbackText, BorderLayout.SOUTH);
            root.add(center, BorderLayout.CENTER);

            nextButton.setEnabled(false);
            nextButton.setFont(new Font("Arial", Font.BOLD, 15));
            nextButton.addActionListener(e -> next());
            JPanel footer = transparentPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.add(nextButton);
            root.add(footer, BorderLayout.SOUTH);
        }

        private JPanel transparentPanel(LayoutManager layout) {
            JPanel panel = new JPanel(layout);
            panel.setOpaque(false);
            return panel;
        }

        private void showQuestion() {
            answered = false;
            D9QuestionBank.Question question = questions.get(currentIndex);
            titleLabel.setText("Thử thách OOP - Tầng " + floor);
            progressLabel.setText("Câu " + (currentIndex + 1) + "/" + questions.size());
            questionText.setText(question.question);
            feedbackText.setText("");
            nextButton.setEnabled(false);
            nextButton.setText(currentIndex == questions.size() - 1 ? "Xem kết quả" : "Tiếp tục");

            for (int i = 0; i < answerButtons.length; i++) {
                if (question.options != null && i < question.options.length) {
                    answerButtons[i].setText((i + 1) + ". " + question.options[i]);
                    answerButtons[i].setEnabled(true);
                } else {
                    answerButtons[i].setText((i + 1) + ". -");
                    answerButtons[i].setEnabled(false);
                }
            }
        }

        private void answer(int selectedIndex) {
            if (answered) {
                return;
            }
            answered = true;
            D9QuestionBank.Question question = questions.get(currentIndex);
            int answerIndex = question.answerIndex;
            boolean validAnswer = question.options != null
                    && answerIndex >= 0
                    && answerIndex < Math.min(question.options.length, answerButtons.length);
            boolean correct = validAnswer && selectedIndex == answerIndex;
            if (correct) {
                correctCount++;
            }

            for (JButton button : answerButtons) {
                button.setEnabled(false);
            }
            if (selectedIndex >= 0 && selectedIndex < answerButtons.length) {
                answerButtons[selectedIndex].setBackground(correct ? new Color(98, 180, 118) : new Color(190, 86, 86));
            }
            if (validAnswer && answerIndex < answerButtons.length) {
                answerButtons[answerIndex].setBackground(new Color(98, 180, 118));
            }

            String answerText = validAnswer ? question.options[answerIndex] : "Không xác định";
            String prefix = correct ? "Đúng." : "Sai. Đáp án đúng: " + answerText + ".";
            String explanation = question.explanation != null ? question.explanation : "";
            feedbackText.setText(prefix + "\n" + explanation);
            nextButton.setEnabled(true);
        }

        private void next() {
            resetButtonColors();
            if (currentIndex < questions.size() - 1) {
                currentIndex++;
                showQuestion();
                return;
            }
            showResult();
        }

        private void showResult() {
            boolean passed = correctCount >= requiredCorrect;
            titleLabel.setText("Kết quả thử thách");
            progressLabel.setText(correctCount + "/" + questions.size() + " đúng");
            questionText.setText(passed
                    ? "Bạn đã vượt qua thử thách OOP tầng " + floor + "."
                    : "Bạn chưa vượt qua thử thách OOP tầng " + floor + ".");
            feedbackText.setText("Yêu cầu: " + requiredCorrect + "/" + questions.size()
                    + " câu đúng. Kết quả: " + (passed ? "Đạt" : "Chưa đạt") + ".");
            for (JButton button : answerButtons) {
                button.setVisible(false);
            }
            nextButton.setText("Đóng");
            nextButton.setEnabled(true);
            nextButton.removeActionListener(nextButton.getActionListeners()[0]);
            nextButton.addActionListener(e -> dispose());
        }

        private void resetButtonColors() {
            Color defaultColor = UIManager.getColor("Button.background");
            for (JButton button : answerButtons) {
                button.setBackground(defaultColor);
            }
        }

        private class BackgroundPanel extends JPanel {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g2d.setColor(new Color(28, 34, 46));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                g2d.dispose();
            }
        }
    }
}
