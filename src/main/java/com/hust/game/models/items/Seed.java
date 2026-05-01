package com.hust.game.models.items;

/**
 * Seed - Định nghĩa các loại hạt giống từ GDD.
 */
public enum Seed {
    CORN("Hạt giống Ngô", 10, 15, 5000), // Giá 10 VNĐ, Thu hoạch +15 VNĐ, Chín sau 5 giây (demo)
    PADDY("Hạt giống Lúa", 15, 25, 8000);

    private final String name;
    private final double buyPrice;
    private final double harvestValue;
    private final long growthTimeMs;

    Seed(String name, double buyPrice, double harvestValue, long growthTimeMs) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.harvestValue = harvestValue;
        this.growthTimeMs = growthTimeMs;
    }

    public String getName() { return name; }
    public double getBuyPrice() { return buyPrice; }
    public double getHarvestValue() { return harvestValue; }
    public long getGrowthTimeMs() { return growthTimeMs; }
}
