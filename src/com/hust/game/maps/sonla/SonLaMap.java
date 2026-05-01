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
            case "till": // Cuốc đất: -2 Energy
                if (player.getEnergy() >= 2) {
                    plot.till();
                    player.setEnergy(player.getEnergy() - 2);
                    System.out.println("⚡ Năng lượng còn lại: " + player.getEnergy());
                } else {
                    System.out.println("❌ Vũ quá mệt để cuốc đất!");
                }
                break;

            case "plant": // Gieo hạt: Kiểm tra hành trang
                if (plot.getCurrentState() == com.hust.game.models.farming.FarmPlot.State.TILLED) {
                    if (player.useItem(seed.getName())) {
                        plot.plant(seed);
                    } else {
                        System.out.println("❌ Không có hạt giống trong hành trang!");
                    }
                }
                break;

            case "harvest":
                double reward = plot.harvest();
                if (reward > 0) {
                    player.addFinance(reward);
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
