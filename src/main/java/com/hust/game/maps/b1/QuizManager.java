package com.hust.game.maps.b1;

import com.hust.game.maps.d9.D9QuestionBank;
import java.util.List;
import java.util.Random;

public class QuizManager {
    private D9QuestionBank questionBank;
    private List<D9QuestionBank.Question> currentQuestions;
    private Random random;

    public QuizManager() {
        random = new Random();
        // Tái sử dụng ngân hàng câu hỏi của D9, KHÔNG duplicate dữ liệu JSON
        questionBank = new D9QuestionBank("assets/Data/questions/oop_questions.json");
        
        // Lấy câu hỏi từ set oop_final (hoặc set khác tùy ý)
        currentQuestions = questionBank.getQuestionsForSet("oop_final", 20);
        if (currentQuestions.isEmpty()) {
            currentQuestions = questionBank.getQuestionsForSet("oop_floor_1", 20);
        }
    }
    
    public D9QuestionBank.Question getRandomQuestion() {
        if (currentQuestions == null || currentQuestions.isEmpty()) {
            return null;
        }
        return currentQuestions.get(random.nextInt(currentQuestions.size()));
    }
    
    public boolean checkAnswer(D9QuestionBank.Question q, int selectedIndex) {
        return q != null && q.answerIndex == selectedIndex;
    }
}
