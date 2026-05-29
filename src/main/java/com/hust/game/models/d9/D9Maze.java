package com.hust.game.models.d9;

import java.util.ArrayList;
import java.util.List;

public class D9Maze {
    public static class Question {
        public String questionText;
        public String[] options;
        public int correctIndex;
        public String rewardInfo;

        public Question(String q, String[] opts, int correct, String reward) {
            this.questionText = q;
            this.options = opts;
            this.correctIndex = correct;
            this.rewardInfo = reward;
        }
    }

    public List<Question> getQuestionsForFloor(int floor) {
        List<Question> list = new ArrayList<>();
        switch (floor) {
            case 1:
                list.add(new Question("Đạo hàm của sin(x) là gì?", new String[]{"cos(x)", "-cos(x)", "tan(x)", "cot(x)"}, 0, "Giải tích A1 - Tầng 1"));
                list.add(new Question("Giới hạn của (1/x) khi x tiến tới vô cùng?", new String[]{"1", "0", "Vô cùng", "-1"}, 1, "Cơ bản"));
                break;
            case 2:
                list.add(new Question("Nguyên hàm của e^x là?", new String[]{"e^x", "ln(x)", "x*e^x", "e^(x+1)"}, 0, "Tích phân - Tầng 2"));
                break;
            case 3:
                list.add(new Question("Ma trận đơn vị I nhân với ma trận A bằng?", new String[]{"0", "I", "A", "A^2"}, 2, "Ma trận - Tầng 3"));
                break;
            case 5:
                list.add(new Question("Trong Java, từ khóa nào dùng để kế thừa một lớp?", new String[]{"implements", "extends", "inherits", "using"}, 1, "OOP - Tầng 5"));
                break;
            // Các tầng khác sẽ được bổ sung thêm câu hỏi
            default:
                list.add(new Question("1 + 1 = ?", new String[]{"1", "2", "3", "0"}, 1, "Thử thách"));
                break;
        }
        return list;
    }
}
