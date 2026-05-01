package com.hust.game.models.items;

/**
 * Seed - Định nghĩa các loại hạt giống từ GDD.
 */
public enum Seed {
    CORN("Hạt giống Ngô", 10, 15, 5000, 5), // Giá 10 VNĐ, Thu hoạch +15 VNĐ, Chín sau 5 giây, +5 EXP
    PADDY("Hạt giống Lúa", 15, 25, 8000, 8); // +8 EXP (~1.5x Ngô)

    private final String name;
    private final double buyPrice;
    private final double harvestValue;
    private final long growthTimeMs;
    private final int expReward;

    Seed(String name, double buyPrice, double harvestValue, long growthTimeMs, int expReward) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.harvestValue = harvestValue;
        this.growthTimeMs = growthTimeMs;
        this.expReward = expReward;
    }

    public String getName() { return name; }
    public double getBuyPrice() { return buyPrice; }
    public double getHarvestValue() { return harvestValue; }
    public long getGrowthTimeMs() { return growthTimeMs; }
    public int getExpReward() { return expReward; }
}
