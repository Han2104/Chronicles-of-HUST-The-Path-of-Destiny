package com.hust.game.models.entities;

/**
 * Enemy - Lớp trừu tượng định nghĩa các thuộc tính chung cho kẻ địch.
 * Áp dụng Abstraction.
 */
public abstract class Enemy {
    protected String name;
    protected int hp;
    protected int attackPower;

    public Enemy(String name, int hp, int attackPower) {
        this.name = name;
        this.hp = hp;
        this.attackPower = attackPower;
    }

    /**
     * Phương thức tấn công trừu tượng mà mọi Boss/Enemy phải triển khai.
     */
    public abstract void executeSkill(Player player);

    public String getName() { return name; }
    public int getHp() { return hp; }
    public void takeDamage(int damage) {
        this.hp -= damage;
        System.out.println(name + " nhận " + damage + " sát thương. HP còn lại: " + hp);
    }
}
