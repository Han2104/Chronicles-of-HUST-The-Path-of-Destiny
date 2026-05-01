package com.hust.game.models.farming;

import com.hust.game.models.items.Seed;

/**
 * FarmPlot - Quản lý trạng thái của một ô đất trồng trọt.
 */
public class FarmPlot {
    public enum State { EMPTY, TILLED, PLANTED, READY }

    private State currentState = State.EMPTY;
    private Seed plantedSeed = null;
    private long plantTime = 0;

    public State getCurrentState() {
        // Tự động cập nhật trạng thái nếu cây đã chín
        if (currentState == State.PLANTED && (System.currentTimeMillis() - plantTime >= plantedSeed.getGrowthTimeMs())) {
            currentState = State.READY;
        }
        return currentState;
    }

    public boolean till() {
        if (currentState == State.EMPTY) {
            currentState = State.TILLED;
            System.out.println("✅ Ô đất đã được cuốc sạch cỏ.");
            return true;
        }
        return false;
    }

    public void plant(Seed seed) {
        if (currentState == State.TILLED) {
            this.plantedSeed = seed;
            this.plantTime = System.currentTimeMillis();
            this.currentState = State.PLANTED;
            System.out.println("🌱 Đã gieo: " + seed.getName());
        }
    }

    public double harvest() {
        if (getCurrentState() == State.READY) {
            double value = plantedSeed.getHarvestValue();
            System.out.println("🌾 Thu hoạch thành công: " + plantedSeed.getName() + "! Nhận " + value + " VNĐ.");
            reset();
            return value;
        }
        return 0;
    }

    private void reset() {
        currentState = State.EMPTY;
        plantedSeed = null;
        plantTime = 0;
    }

    public String getStatusText() {
        switch (getCurrentState()) {
            case EMPTY: return "Đất trống (Cần cuốc)";
            case TILLED: return "Đã cuốc (Sẵn sàng gieo hạt)";
            case PLANTED: return "Đang trồng: " + (plantedSeed != null ? plantedSeed.getName() : "Unknown");
            case READY: return "Đã chín (Sẵn sàng thu hoạch)";
            default: return "Không xác định";
        }
    }

    public Seed getPlantedSeed() { return plantedSeed; }
}
