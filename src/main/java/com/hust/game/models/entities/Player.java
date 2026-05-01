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
    private int energy;          
    private int maxEnergy = 20;  // Mặc định ban đầu
    private double finance;      
    private double maxFinance = 500.0; 
    private int willpower;       
    private int maxWillpower = 100;
    private int disciplineScore; 
    private int solutionSkill;   
    private int maxSolutionSkill = 100;
    
    // Hệ thống cấp độ (Leveling System)
    private int level = 1;
    private int exp = 0;
    private final int[] levelThresholds = {50, 100, 180, 280, 400, 550, 750, 1000, 1300};
    
    // Kỹ năng mở khóa (Skills/Buffs)
    private double harvestMultiplier = 1.0;
    private boolean checkInComboEnabled = false;
    private boolean fastStudyEnabled = false; 
    private boolean resistDisciplinePenalty = false;
    private boolean hustLegendEnabled = false;
    
    // Tiến trình (Progress Flags) - Cần thiết cho các getter/setter phía dưới
    private boolean completedMap1 = false;
    private boolean completedMap2 = false;
    private int currentD9Floor = 0;

    public Player(String name) {
        this.name = name;
        this.energy = 20;        
        this.finance = 50.0;     
        this.willpower = 30;     
        this.disciplineScore = 0;
        this.solutionSkill = 10;
    }

    // --- Hệ thống thăng cấp ---
    
    public void addExp(int amount) {
        this.exp += amount;
        System.out.println("✨ +" + amount + " EXP (Tổng: " + this.exp + ")");
        
        while (level <= levelThresholds.length && exp >= levelThresholds[level - 1]) {
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        System.out.println("🎊 CHÚC MỪNG! BẠN ĐÃ LÊN CẤP " + level + " 🎊");
        
        switch (level) {
            case 2:
                maxEnergy += 1;
                energy = maxEnergy;
                System.out.println("🎁 Thưởng: +1 Energy tối đa!");
                break;
            case 3:
                harvestMultiplier = 1.2;
                System.out.println("🎁 Thưởng: Mở khóa [Canh Tác Nhanh] (+20% thu hoạch)!");
                break;
            case 4:
                maxFinance += 10;
                System.out.println("🎁 Thưởng: +10 Tài Chính tối đa!");
                break;
            case 5:
                checkInComboEnabled = true;
                System.out.println("🎁 Thưởng: Mở khóa [Check-in Combo]!");
                break;
            case 6:
                maxWillpower += 5;
                System.out.println("🎁 Thưởng: +5 Willpower tối đa!");
                break;
            case 7:
                fastStudyEnabled = true;
                System.out.println("🎁 Thưởng: Mở khóa [Ôn Thi Thần Tốc] (Boss D9 -20% HP)!");
                break;
            case 8:
                maxSolutionSkill += 10;
                System.out.println("🎁 Thưởng: +10 Solution Skill tối đa!");
                break;
            case 9:
                resistDisciplinePenalty = true;
                System.out.println("🎁 Thưởng: Kháng 1 đòn trừ Điểm Rèn Luyện/trận!");
                break;
            case 10:
                hustLegendEnabled = true;
                applyHustLegend();
                System.out.println("🎁 THƯỞNG TỐI THƯỢNG: [HUST LEGEND] (+30% toàn bộ chỉ số)!");
                break;
        }
    }

    private void applyHustLegend() {
        maxEnergy = (int)(maxEnergy * 1.3);
        maxWillpower = (int)(maxWillpower * 1.3);
        maxSolutionSkill = (int)(maxSolutionSkill * 1.3);
        energy = maxEnergy;
    }

    // --- Getters & Setters ---

    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getNextLevelExp() {
        if (level > levelThresholds.length) return -1;
        return levelThresholds[level - 1];
    }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(energy, maxEnergy));
        if (this.energy == 0) {
            System.out.println("⚠️ Vũ đã ngất do cạn năng lượng!");
        }
    }

    public double getFinance() { return finance; }
    public void addFinance(double amount) {
        this.finance = Math.min(this.finance + amount, maxFinance);
        System.out.println("💰 Tài chính hiện tại: " + String.format("%.1f", this.finance) + " VNĐ");
    }

    public double getHarvestMultiplier() { return harvestMultiplier; }

    public int getDisciplineScore() { return disciplineScore; }
    public void addDiscipline(int points) {
        this.disciplineScore = Math.min(this.disciplineScore + points, 100);
    }

    public int getWillpower() { return willpower; }
    public void setWillpower(int willpower) {
        this.willpower = Math.max(0, Math.min(willpower, maxWillpower));
    }

    public int getSolutionSkill() { return solutionSkill; }
    public void addSolutionSkill(int points) {
        this.solutionSkill = Math.min(this.solutionSkill + points, maxSolutionSkill);
    }
    
    public String getName() { return name; }

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
