package com.hust.game.maps.b1;

import com.hust.game.maps.d9.D9QuestionBank;
import com.hust.game.models.entities.B1Player;
import com.hust.game.models.entities.Player;

import java.util.Random;

/**
 * B1DebuffManager — Quản lý 3 loại debuff ngẫu nhiên từ Boss theo GDD V2.0
 * Section 6.4B.
 *
 * <pre>
 * 1. CAMERA_SURVEILLANCE  → Quiz Encapsulation → Sai: khóa bắn + ném item 6s
 * 2. XOAY_AOE             → Quiz Polymorphism  → Sai: giảm speed 50% trong 5s
 * 3. TRUC_XUAT            → Quiz OOP tích hợp  → Sai: -20 DisciplineScore
 * </pre>
 *
 * Thẻ B2 (passive): tự động trả lời đúng (tối đa 2 lần/trận).
 */
public class B1DebuffManager {

    // =========================================================
    // DEBUFF TYPES
    // =========================================================
    public enum DebuffType {
        NONE,
        CAMERA_SURVEILLANCE, // Khóa bắn 6s nếu trả lời sai
        XOAY_AOE, // Giảm speed 50% trong 5s nếu sai
        TRUC_XUAT // -20 DisciplineScore nếu sai
    }

    // =========================================================
    // DEBUFF STATE
    // =========================================================
    public static class DebuffState {
        public boolean shootLocked = false; // Camera: khóa bắn
        public long shootLockEnd = 0;

        public boolean slowed = false; // Xoáy: giảm speed
        public long slowEnd = 0;

        public boolean disciplinePenaltyApplied = false; // Trực Xuất: đã trừ điểm?

        public boolean hasNerfPermanent = false; // Khi Discipline < 50 → NERF -30%

        public void update(long now) {
            if (shootLocked && now >= shootLockEnd) {
                shootLocked = false;
                System.out.println("🔓 Khóa bắn đã được gỡ.");
            }
            if (slowed && now >= slowEnd) {
                slowed = false;
                System.out.println("💨 Tốc độ di chuyển đã phục hồi.");
            }
        }

        public double getSpeedMultiplier() {
            if (hasNerfPermanent && slowed)
                return 0.5 * 0.7; // -50% * -30%
            if (hasNerfPermanent)
                return 0.7; // -30%
            if (slowed)
                return 0.5; // -50%
            return 1.0;
        }

        public double getDamageMultiplier() {
            return hasNerfPermanent ? 0.7 : 1.0; // NERF: -30% damage
        }
    }

    // =========================================================
    // QUIZ PENDING STATE
    // =========================================================
    public static class PendingDebuffQuiz {
        public DebuffType type;
        public D9QuestionBank.Question question;

        public PendingDebuffQuiz(DebuffType type, D9QuestionBank.Question q) {
            this.type = type;
            this.question = q;
        }
    }

    // =========================================================
    // FIELDS
    // =========================================================
    private final QuizManager quizManager;
    private final DebuffState state = new DebuffState();
    private final Random rng = new Random();

    // Thẻ B2 passive: tự động trả lời đúng
    private int cardB2Charges = 0; // set từ Player.hasCardB2()

    // Cooldown giữa các debuff (ms)
    private static final long DEBUFF_INTERVAL_MS = 60_000; // 60s
    private long lastDebuffTime = 0;

    // Debuff quiz đang chờ xử lý
    private PendingDebuffQuiz pendingQuiz = null;
    private boolean debuffQuizActive = false;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public B1DebuffManager(QuizManager quizManager, boolean hasCardB2) {
        this.quizManager = quizManager;
        this.cardB2Charges = hasCardB2 ? 2 : 0;
    }

    // =========================================================
    // UPDATE (gọi mỗi frame)
    // =========================================================
    /**
     * @return PendingDebuffQuiz nếu boss vừa tung debuff cần quiz, null nếu không
     */
    public PendingDebuffQuiz update(long now) {
        state.update(now);

        // Kiểm tra trigger debuff theo interval
        if (!debuffQuizActive && now - lastDebuffTime >= DEBUFF_INTERVAL_MS) {
            lastDebuffTime = now;
            return triggerRandomDebuff();
        }
        return null;
    }

    // =========================================================
    // TRIGGER RANDOM DEBUFF
    // =========================================================
    private PendingDebuffQuiz triggerRandomDebuff() {
        DebuffType[] types = DebuffType.values();
        // Chọn ngẫu nhiên 1 trong 3 debuff (bỏ qua NONE)
        DebuffType chosen = types[1 + rng.nextInt(3)];

        D9QuestionBank.Question q = getDebuffQuestion(chosen);
        if (q == null)
            return null;

        // Kiểm tra Thẻ B2 auto-answer
        if (cardB2Charges > 0) {
            cardB2Charges--;
            System.out.println("🃏 Thẻ B2 tự động kháng debuff: " + chosen + " (còn " + cardB2Charges + " lần)");
            // Auto-answer correct → không apply penalty
            return null;
        }

        debuffQuizActive = true;
        pendingQuiz = new PendingDebuffQuiz(chosen, q);
        System.out.println("⚠ Boss tung debuff: " + chosen);
        return pendingQuiz;
    }

    // =========================================================
    // ANSWER DEBUFF QUIZ
    // =========================================================
    /**
     * Gọi từ B1Engine sau khi người chơi chọn đáp án debuff quiz.
     * 
     * @param selectedIndex đáp án được chọn (-1 = timeout)
     * @param gPlayer       GameManager.Player để trừ DisciplineScore nếu cần
     * @param b1Player      B1Player để apply speed nerf
     */
    public void answerDebuffQuiz(int selectedIndex, Player gPlayer, B1Player b1Player) {
        if (pendingQuiz == null)
            return;

        boolean correct = quizManager.checkAnswer(pendingQuiz.question, selectedIndex);
        long now = System.currentTimeMillis();

        if (!correct) {
            applyDebuffPenalty(pendingQuiz.type, gPlayer, b1Player, now);
        } else {
            System.out.println("✅ Kháng debuff thành công: " + pendingQuiz.type);
        }

        pendingQuiz = null;
        debuffQuizActive = false;
    }

    private void applyDebuffPenalty(DebuffType type, Player gPlayer, B1Player b1Player, long now) {
        switch (type) {
            case CAMERA_SURVEILLANCE:
                // Khóa bắn 6s
                state.shootLocked = true;
                state.shootLockEnd = now + 6_000;
                System.out.println("📷 Bị Quét Camera! Khóa bắn 6 giây.");
                break;

            case XOAY_AOE:
                // Giảm speed 50% trong 5s
                state.slowed = true;
                state.slowEnd = now + 5_000;
                System.out.println("🌀 Bị Xoáy AOE! Tốc độ -50% trong 5 giây.");
                break;

            case TRUC_XUAT:
                // -20 DisciplineScore từ GameManager.Player
                // Sử dụng addDisciplineScore với giá trị âm
                gPlayer.addDisciplineScore(-20);
                System.out.println("📋 Bị Trực Xuất Điểm Rèn Luyện! -20 Điểm Rèn Luyện.");

                // Kiểm tra NERF vĩnh viễn nếu < 50
                if (gPlayer.getDisciplineScore() < 50 && !state.hasNerfPermanent) {
                    state.hasNerfPermanent = true;
                    System.out.println("💀 NERF kích hoạt! Tốc độ -30% và sát thương -30% cho đến hết trận.");
                }
                break;

            default:
                break;
        }
    }

    // =========================================================
    // GET DEBUFF QUESTION (theo topic tương ứng)
    // =========================================================
    private D9QuestionBank.Question getDebuffQuestion(DebuffType type) {
        // Tái sử dụng QuizManager lấy câu hỏi oop_final
        // Trong triển khai đầy đủ có thể lọc theo topic (Encapsulation, Polymorphism)
        return quizManager.getRandomQuestion();
    }

    // =========================================================
    // GETTERS
    // =========================================================
    public DebuffState getState() {
        return state;
    }

    public boolean isDebuffQuizActive() {
        return debuffQuizActive;
    }

    public PendingDebuffQuiz getPendingQuiz() {
        return pendingQuiz;
    }

    /** Tên debuff hiện tại để hiển thị trên HUD */
    public String getActiveDebuffLabel() {
        long now = System.currentTimeMillis();
        if (state.shootLocked && now < state.shootLockEnd) {
            long rem = (state.shootLockEnd - now) / 1000;
            return "📷 BỊ KHÓA BẮN: " + rem + "s";
        }
        if (state.slowed && now < state.slowEnd) {
            long rem = (state.slowEnd - now) / 1000;
            return "🌀 BỊ CHẬM 50%: " + rem + "s";
        }
        if (state.hasNerfPermanent) {
            return "💀 NERF: SPD-30% DMG-30%";
        }
        return "";
    }

    public int getCardB2Charges() {
        return cardB2Charges;
    }
}
