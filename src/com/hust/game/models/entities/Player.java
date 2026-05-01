package com.hust.game.models.entities;

import java.util.HashMap;
import java.util.Map;

/**
 * Player - Đại diện cho nhân vật Vũ.
 */
public class Player {
    private String name;
    private Map<String, Integer> inventory = new HashMap<>();
    
    // Core Stats (Theo GDD 2.2)
    private int energy;          // Năng lượng (HP) - Max 20
    private double finance;      // Tài chính (VNĐ)
    private int willpower;       // Ý chí - Max 100
    private int disciplineScore; // Điểm rèn luyện - Max 100
    private int solutionSkill;   // Khả năng giải quyết vấn đề
    
    // Tiến trình (Progress Flags)
    private boolean completedMap1 = false;
    private boolean completedMap2 = false;
    private int currentD9Floor = 0;

    public Player(String name) {
        this.name = name;
        this.energy = 20;        // Khởi đầu 20/20
        this.finance = 50.0;     // Khởi đầu 50 VNĐ
        this.willpower = 30;     // Khởi đầu 30/100
        this.disciplineScore = 0;
        this.solutionSkill = 10;
    }

    // --- Getters & Setters với Encapsulation logic ---

    public String getName() { return name; }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(energy, 100));
        if (this.energy == 0) {
            System.out.println("⚠️ Vũ đã ngất do cạn năng lượng!");
        }
    }

    public double getFinance() { return finance; }
    public void addFinance(double amount) {
        this.finance += amount;
        System.out.println("💰 Tài chính hiện tại: " + this.finance + " VNĐ");
    }

    public int getDisciplineScore() { return disciplineScore; }
    public void addDiscipline(int points) {
        this.disciplineScore = Math.min(this.disciplineScore + points, 100);
    }

    public int getSolutionSkill() { return solutionSkill; }
    public void addSolutionSkill(int points) {
        this.solutionSkill = Math.min(this.solutionSkill + points, 100);
    }

    // --- Progress Management ---

    public boolean isCompletedMap1() { return completedMap1; }
    public void setCompletedMap1(boolean status) { this.completedMap1 = status; }

    public boolean isCompletedMap2() { return completedMap2; }
    public void setCompletedMap2(boolean status) { this.completedMap2 = status; }

    public int getCurrentD9Floor() { return currentD9Floor; }
    public void setCurrentD9Floor(int floor) { this.currentD9Floor = floor; }

    // --- Quản lý hành trang ---
    public void addItem(String itemName, int quantity) {
        inventory.put(itemName, inventory.getOrDefault(itemName, 0) + quantity);
        System.out.println("🎒 Đã thêm " + quantity + " " + itemName + " vào hành trang.");
    }

    public boolean useItem(String itemName) {
        int current = inventory.getOrDefault(itemName, 0);
        if (current > 0) {
            inventory.put(itemName, current - 1);
            return true;
        }
        return false;
    }

    public int getItemCount(String itemName) {
        return inventory.getOrDefault(itemName, 0);
    }
}
