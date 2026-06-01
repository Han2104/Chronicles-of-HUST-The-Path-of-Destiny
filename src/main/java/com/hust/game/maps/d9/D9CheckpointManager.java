package com.hust.game.maps.d9;

import java.util.HashMap;
import java.util.Map;

/**
 * D9CheckpointManager - Quản lý checkpoints và teleport player
 */
public class D9CheckpointManager {

    private final Map<String, D9Object> checkpoints;

    public D9CheckpointManager(D9Map map) {

        this.checkpoints = new HashMap<>();

        // Index checkpoints by checkpointId
        for (D9Object obj : map.getCheckpoints()) {
            String checkpointId = obj.getProperty("checkpointId");
            if (checkpointId != null) {
                checkpoints.put(checkpointId, obj);
                System.out.println("[D9] Checkpoint loaded: " + checkpointId
                        + " -> " + obj.getName() + " x=" + obj.getX() + " y=" + obj.getY());
            }
        }

        for (int floor = 2; floor <= 7; floor++) {
            String checkpointId = "cp_" + floor;
            if (!checkpoints.containsKey(checkpointId)) {
                System.err.println("[D9] Warning: checkpoint not loaded: " + checkpointId);
            }
        }
    }

    public D9Object getCheckpoint(String checkpointId) {
        return checkpoints.get(checkpointId);
    }

    public void teleportToCheckpoint(D9Player player, String checkpointId, D9CollisionManager collisionManager) {
        D9Object checkpoint = getCheckpoint(checkpointId);
        if (checkpoint != null) {
            teleportToObject(player, checkpoint, collisionManager);
            player.setLastCheckpointId(checkpointId);
            System.out.println("[D9] Teleported to checkpoint: " + checkpointId);
        } else {
            System.err.println("[D9] Warning: checkpoint not found: " + checkpointId);
        }
    }

    public void saveCheckpoint(D9Player player, String checkpointId) {
        player.setLastCheckpointId(checkpointId);
        System.out.println("[D9] Saved checkpoint: " + checkpointId);
    }

    public String getLastCheckpointId(D9Player player) {
        return player.getLastCheckpointId();
    }

    public boolean teleportToFloorOrPrevious(D9Player player, int floor, D9CollisionManager collisionManager) {
        for (int candidateFloor = floor; candidateFloor >= 2; candidateFloor--) {
            String checkpointId = "cp_" + candidateFloor;
            if (checkpoints.containsKey(checkpointId)) {
                teleportToCheckpoint(player, checkpointId, collisionManager);
                return true;
            }
        }
        return false;
    }

    public void teleportToObject(D9Player player, D9Object object, D9CollisionManager collisionManager) {
        D9CollisionManager.SnapResult snap = collisionManager.snapObjectToNearestPlatform(
                object, player.getWidth(), player.getHeight());
        if (snap.platform == null) {
            System.err.println("[D9] Warning: no platform found while teleporting to " + object.getName()
                    + "; using object position fallback.");
            player.teleportTo(object.getX(), object.getY(), object.getFloor());
            return;
        }
        player.teleportTo(snap.x, snap.y, object.getFloor());
    }
}
