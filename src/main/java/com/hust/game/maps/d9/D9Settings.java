package com.hust.game.maps.d9;

/**
 * D9Settings - Cài đặt cho map D9
 */
public class D9Settings {
    // Map paths
    public String mapPath = "assets/Map/D9/d9_map.tmx";
    public String questionBankPath = "assets/Data/questions/oop_questions.json";

    // Physics
    public int tileSize = 32;
    public double gravity = 0.45;
    public double moveSpeed = 3.2;
    public double maxFallSpeed = 12.0;
    public double minJumpPower = 6.0;
    public double maxJumpPower = 15.0;
    public double chargeRate = 0.22;

    // Player
    public int playerHitboxWidth = 34;
    public int playerHitboxHeight = 58;
    public double playerScale = 0.18;
    public boolean cameraFollow = true;

    // Quiz
    public int requiredCorrectNormal = 3;
    public int requiredCorrectFinal = 4;
    public int questionCount = 5;

    // Debug
    public boolean debugDraw = false;

    // Getters
    public String getMapPath() { return mapPath; }
    public String getQuestionBankPath() { return questionBankPath; }
    public int getTileSize() { return tileSize; }
    public double getGravity() { return gravity; }
    public double getMoveSpeed() { return moveSpeed; }
    public double getMaxFallSpeed() { return maxFallSpeed; }
    public double getMinJumpPower() { return minJumpPower; }
    public double getMaxJumpPower() { return maxJumpPower; }
    public double getChargeRate() { return chargeRate; }
    public int getPlayerHitboxWidth() { return playerHitboxWidth; }
    public int getPlayerHitboxHeight() { return playerHitboxHeight; }
    public double getPlayerScale() { return playerScale; }
    public boolean isCameraFollow() { return cameraFollow; }
    public int getRequiredCorrectNormal() { return requiredCorrectNormal; }
    public int getRequiredCorrectFinal() { return requiredCorrectFinal; }
    public int getQuestionCount() { return questionCount; }
    public boolean isDebugDraw() { return debugDraw; }
    public void setDebugDraw(boolean debugDraw) { this.debugDraw = debugDraw; }
    public void toggleDebugDraw() { this.debugDraw = !this.debugDraw; }
}
