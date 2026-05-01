package com.hust.game.maps.sonla;

import com.hust.game.core.GameManager;
import com.hust.game.models.entities.Player;
import com.hust.game.models.farming.FarmPlot;
import com.hust.game.models.items.Seed;

import java.util.ArrayList;
import java.util.List;

/**
 * SonLaMap - Logic Map 1 (Nông trại, trồng trọt).
 */
public class SonLaMap {
    private List<FarmPlot> plots;
    private Player player;

    public SonLaMap() {
        this.player = GameManager.getInstance().getPlayer();
        this.plots = new ArrayList<>();
        // Khởi tạo 48 ô đất (Lưới 8x6)
        for (int i = 0; i < 48; i++) plots.add(new FarmPlot());
    }

    public void interactWithPlot(int plotIndex, String action, Seed seed) {
        if (plotIndex < 0 || plotIndex >= plots.size()) return;
        FarmPlot plot = plots.get(plotIndex);

        switch (action.toLowerCase()) {
            case "plant": // Gieo hạt trực tiếp lên đất trống: Tiêu hao 3 Energy
                if (plot.getCurrentState() == com.hust.game.models.farming.FarmPlot.State.EMPTY) {
                    if (player.getEnergy() >= 3) {
                        if (player.useItem(seed.getName())) {
                            plot.plant(seed);
                            player.setEnergy(player.getEnergy() - 3);
                            System.out.println("🌱 Đã cuốc đất và gieo " + seed.getName() + " (-3⚡)");
                        } else {
                            System.out.println("❌ Không có hạt giống trong hành trang!");
                        }
                    } else {
                        System.out.println("❌ Vũ không đủ năng lượng để gieo hạt!");
                    }
                } else {
                    System.out.println("ℹ️ Ô đất này hiện không thể gieo hạt.");
                }
                break;

            case "harvest":
                // Lấy thông tin hạt giống TRƯỚC KHI thu hoạch (vì thu hoạch xong ô đất sẽ bị xóa sạch)
                Seed harvestedSeed = plot.getPlantedSeed();
                double baseReward = plot.harvest();
                
                if (baseReward > 0 && harvestedSeed != null) {
                    double bonus = player.isIronHoeEquipped() ? 1.25 : 1.0;
                    double finalReward = baseReward * player.getHarvestMultiplier() * bonus;
                    player.addFinance(finalReward);
                    
                    // Nhận EXP dựa trên loại cây
                    int expGain = harvestedSeed.getExpReward();
                    player.addExp(expGain); 

                    // Loot ngẫu nhiên: Bình Nước Sơn La (10% cơ hội)
                    if (Math.random() < 0.10) {
                        player.addItem("Bình Nước Sơn La", 1);
                        System.out.println("🎁 Bạn tìm thấy 1 Bình Nước Sơn La trong khi thu hoạch!");
                    }
                } else {
                    System.out.println("⏳ Cây chưa chín hoặc đất trống.");
                }
                break;
        }
    }

    public com.hust.game.models.farming.FarmPlot getPlot(int index) {
        if (index >= 0 && index < plots.size()) return plots.get(index);
        return null;
    }
}
