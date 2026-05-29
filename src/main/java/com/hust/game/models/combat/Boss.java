package com.hust.game.models.combat;

import java.util.ArrayList;
import java.util.List;

public class Boss {
    public String name;
    public int hp;
    public int maxHp;
    public String description;
    public List<Skill> skills = new ArrayList<>();
    public String weakness;

    public static class Skill {
        public String name;
        public String type;
        public String effect;
        public String defense;
        public int damage;

        public Skill(String n, String t, String e, String d, int dmg) {
            this.name = n; this.type = t; this.effect = e; this.defense = d; this.damage = dmg;
        }
    }

    public Boss(String name, int hp, String desc, String weakness) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.description = desc;
        this.weakness = weakness;
    }

    public static Boss createBossTung() {
        Boss b = new Boss("Thầy Tạ Hải Tùng", 200, "Hiệu Trưởng uy nghi, chuyên gia Kỹ Thuật Điện Tử.", "Solution Skill > 70 + Thẻ B2");
        b.skills.add(new Skill("Camera Surveillance", "Kiểm soát", "Khóa 1 kỹ năng", "Sổ Chiến Lược", 10));
        b.skills.add(new Skill("Kỹ Thuật Lập Trình", "AOE", "-25 HP; -10 Skill", "Giáo trình OOP", 25));
        b.skills.add(new Skill("Trừ Điểm Rèn Luyện", "Trừng phạt", "-20 ĐRL", "Thẻ B2", 20));
        b.skills.add(new Skill("Kiểm Tra Đột Xuất", "Quiz", "3 câu OOP khó", "Solution Skill cao", 15));
        return b;
    }

    public static Boss createBossLuong() {
        Boss b = new Boss("GS. Nguyễn Cảnh Lương", 180, "Chuyên gia Cơ Học Ứng Dụng, nghiêm khắc.", "Willpower > 60");
        b.skills.add(new Skill("Định Luật Newton", "Vật lý", "-20 HP", "Học Vật Lý Đại Cương", 20));
        b.skills.add(new Skill("Xoáy Cơ Học", "AOE", "Giảm 10% chỉ số", "Phòng thủ (Defend)", 15));
        b.skills.add(new Skill("Báo Cáo Thí Nghiệm", "Kiểm tra", "Mini-game code", "Sổ Ghi Chép", 10));
        return b;
    }

    public static Boss createBossSon() {
        Boss b = new Boss("GS. Hoàng Minh Sơn", 300, "Boss cuối cùng - Kiểm tra toàn diện.", "Level 8+ và ĐRL >= 80");
        b.skills.add(new Skill("Quyết Định Toàn Diện", "Mega Attack", "-50 HP; 5 câu Quiz", "Áo Đồng Phục", 50));
        b.skills.add(new Skill("Phỏng Vấn Tuyển Dụng", "Event", "10 câu tình huống", "Willpower cao", 0));
        b.skills.add(new Skill("Chung Kết Nghề Nghiệp", "Phase cuối", "Tất cả skill cùng lúc", "Học Bổng KKHT", 40));
        return b;
    }
}
