package com.hust.game.models.entities;

/**
 * BossTung - Thầy Tạ Hải Tùng (Boss Map 4).
 * Áp dụng Inheritance kế thừa từ Enemy.
 */
public class BossTung extends Enemy {

    public BossTung() {
        super("Thầy Tạ Hải Tùng", 200, 25);
    }

    @Override
    public void executeSkill(Player player) {
        System.out.println("🚀 Boss " + name + " sử dụng kỹ năng: KỸ THUẬT LẬP TRÌNH!");
        // Theo GDD: -25 HP; -10 Solution Skill
        player.setEnergy(player.getEnergy() - 25);
        player.addSolutionSkill(-10);
    }
}
