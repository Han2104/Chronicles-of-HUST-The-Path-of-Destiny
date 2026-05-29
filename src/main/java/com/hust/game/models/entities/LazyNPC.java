package com.hust.game.models.entities;

/**
 * LazyNPC - Định nghĩa các biến thể của NPC Lười Biếng và các thông số đối đầu.
 */
public enum LazyNPC {
    SLEEPY("Thằng Ngủ Nướng", 30, 3, 0, 0, "Ngủ thêm tí thôi mà..."),
    GAMER("Ma Game Online", 50, 8, 5, 120, "Làm một trận rank không? Đang chuỗi thắng!"),
    TEA_LORD("Chúa Tể Trà Đá", 70, 15, 10, 0, "Ra làm hơi trà đá đi, học hành gì tầm này.");

    private final String name;
    private final int requiredWillpower;
    private final int disciplinePenalty;
    private final int energyPenalty;
    private final int lockCheckInMinutes;
    private final String catchphrase;

    LazyNPC(String name, int requiredWillpower, int disciplinePenalty, int energyPenalty, int lockCheckInMinutes, String catchphrase) {
        this.name = name;
        this.requiredWillpower = requiredWillpower;
        this.disciplinePenalty = disciplinePenalty;
        this.energyPenalty = energyPenalty;
        this.lockCheckInMinutes = lockCheckInMinutes;
        this.catchphrase = catchphrase;
    }

    public String getName() { return name; }
    public int getRequiredWillpower() { return requiredWillpower; }
    public int getDisciplinePenalty() { return disciplinePenalty; }
    public int getEnergyPenalty() { return energyPenalty; }
    public int getLockCheckInMinutes() { return lockCheckInMinutes; }
    public String getCatchphrase() { return catchphrase; }
}
