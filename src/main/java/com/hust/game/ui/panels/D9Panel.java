package com.hust.game.ui.panels;

import com.hust.game.ui.GameWindow;
import com.hust.game.maps.d9.*;
import com.hust.game.util.AssetLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * D9Panel - Gameplay panel cho Map D9 với Jump King style
 */
public class D9Panel extends JPanel implements ActionListener, KeyListener {
    private static final String D9_MAP_PATH = "assets/Map/D9/d9_map.tmx";
    private static final String D9_QUESTION_BANK_PATH = "assets/Data/questions/oop_questions.json";

    private GameWindow window;
    private StatsPanel statsPanel;

    // Game components
    private D9Map map;
    private D9Player player;
    private D9CollisionManager collisionManager;
    private D9CheckpointManager checkpointManager;
    private D9QuestionBank questionBank;
    private D9QuizManager quizManager;

    // Rendering
    private final List<TilesetImage> tilesetImages = new ArrayList<>();
    private final Map<String, BufferedImage> sprites = new HashMap<>();
    private final Set<Integer> loggedUnresolvedGids = new HashSet<>();
    private final Set<Integer> loggedOutOfRangeGids = new HashSet<>();
    private final Set<Integer> loggedResolvedGids = new HashSet<>();
    private int cameraX = 0;
    private int cameraY = 0;
    private String loadError;

    // Input
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;
    private D9Object activeQuizDoor;
    private D9Object selectedSpawn;
    private D9CollisionManager.SnapResult selectedSpawnSnap;
    private boolean quizInProgress = false;
    private int debugFrameCounter = 0;

    // Game loop
    private Timer gameTimer;

    public D9Panel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;

        setBackground(Color.BLACK);
        setFocusable(true);
        setRequestFocusEnabled(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);

        initializeGame();

        gameTimer = new Timer(1000/60, this);
    }

    private void initializeGame() {
        try {
            // Load map
            D9MapLoader loader = new D9MapLoader();
            System.out.println("[D9] TASK 2 - Loading TMX path: " + D9_MAP_PATH);
            map = loader.loadMap(D9_MAP_PATH);

            if (map == null) {
                loadError = "Could not load D9 map: " + D9_MAP_PATH;
                System.err.println("[D9] Warning: " + loadError);
                return;
            }

            // Initialize components
            player = new D9Player(map.getSettings());
            collisionManager = new D9CollisionManager(map);
            checkpointManager = new D9CheckpointManager(map);

            // Load question bank
            questionBank = new D9QuestionBank(D9_QUESTION_BANK_PATH);
            quizManager = new D9QuizManager(questionBank, map.getSettings());

            // Load tilesets and sprites
            loadAssets();

            // Set player spawn
            placePlayerAtSpawn(true);
            System.out.println("[D9] Floor-1 starting platform detected near spawn_floor_1: "
                    + floorOneStartingPlatformDetected());

        } catch (Exception e) {
            loadError = "Failed to initialize D9: " + e.getMessage();
            System.err.println("[D9] Warning: " + loadError);
            e.printStackTrace(System.err);
        }
    }

    private void loadAssets() {
        tilesetImages.clear();
        if (map != null) {
            for (D9Map.TilesetInfo tileset : map.getTilesets()) {
                String imageSource = tileset.getImageSource();
                if (imageSource == null || imageSource.isBlank()) {
                    System.err.println("[D9] Warning: tileset image source is missing for firstgid=" + tileset.getFirstGid());
                    continue;
                }
                BufferedImage image = AssetLoader.loadImage(imageSource);
                if (image != null) {
                    tilesetImages.add(new TilesetImage(tileset, image));
                } else {
                    System.err.println("[D9] Warning: missing tileset image for source " + imageSource + " (firstgid=" + tileset.getFirstGid() + ").");
                }
            }
        }

        sprites.clear();
        loadSprite("stand_left", "assets/Vu/character_stand_left (1).png");
        loadSprite("stand_right", "assets/Vu/character_stand_right (1).png");
        loadSprite("charge_left", "assets/Vu/character_charge_left.png");
        loadSprite("charge_right", "assets/Vu/character_charge_right.png");
        loadSprite("jump_left", "assets/Vu/character_jump_left.png");
        loadSprite("jump_right", "assets/Vu/character_jump_right.png");

        BufferedImage fallback = AssetLoader.loadImage("assets/Vu/character_icon.png");
        if (fallback != null) {
            String[] keys = {"stand_left", "stand_right", "charge_left", "charge_right", "jump_left", "jump_right"};
            for (String key : keys) {
                sprites.putIfAbsent(key, fallback);
            }
        }
    }

    private void loadSprite(String key, String path) {
        BufferedImage image = AssetLoader.loadImage(path);
        if (image != null) {
            sprites.put(key, image);
        } else {
            System.err.println("[D9] Warning: missing sprite for " + key + " at " + path);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (map == null || player == null) {
            renderFallbackScreen(g);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(-cameraX, -cameraY);

        int backgroundTiles = renderLayer(g2d, map.getBackgroundLayer(), "Background");
        int platformsTiles = renderLayer(g2d, map.getPlatformsLayer(), "Platforms");
        int decorTiles = renderLayer(g2d, map.getDecorLayer(), "Decor");
        int floorDividerTiles = renderLayer(g2d, map.getFloorDividerLayer(), "FloorDivider");
        int checkpointsTiles = renderLayer(g2d, map.getCheckpointsLayer(), "Checkpoints");
        int totalTiles = backgroundTiles + platformsTiles + decorTiles + floorDividerTiles + checkpointsTiles;

        System.out.println("[D9Render] Render order: Background, Platforms, Decor, FloorDivider, Checkpoints");
        System.out.println("[D9Render] Background rendered tiles: " + backgroundTiles);
        System.out.println("[D9Render] Platforms rendered tiles: " + platformsTiles);
        System.out.println("[D9Render] Decor rendered tiles: " + decorTiles);
        System.out.println("[D9Render] FloorDivider rendered tiles: " + floorDividerTiles);
        System.out.println("[D9Render] Checkpoints rendered tiles: " + checkpointsTiles);
        System.out.println("[D9Render] Total rendered tiles this frame: " + totalTiles);
        if (totalTiles == 0) {
            String reason = "";
            if (map.getBackgroundLayer() == null && map.getPlatformsLayer() == null && map.getDecorLayer() == null && map.getFloorDividerLayer() == null && map.getCheckpointsLayer() == null) {
                reason = "no layer data";
            } else if (tilesetImages.isEmpty()) {
                reason = "all tileset images missing";
            } else {
                reason = "all gids either zero, unresolved, or out of range";
            }
            System.err.println("[D9Render] No tiles rendered because " + reason + ".");
        }

        // Render player
        renderPlayer(g2d);

        // Debug draw
        if (map.getSettings().isDebugDraw()) {
            drawDebug(g2d);
        }

        g2d.translate(cameraX, cameraY);
    }

    private void renderFallbackScreen(Graphics g) {
        g.setColor(new Color(24, 28, 34));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("D9 Panel is open", 40, 60);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString(loadError != null ? loadError : "D9 is preparing assets.", 40, 95);
        g.drawString("Expected map: " + D9_MAP_PATH, 40, 125);
        g.drawString("Expected questions: " + D9_QUESTION_BANK_PATH, 40, 150);
        g.drawString("The game stayed running instead of crashing so navigation can be tested.", 40, 180);
    }

    private int renderLayer(Graphics2D g2d, int[][] layer, String layerName) {
        if (map == null) return 0;
        if (layer == null) {
            System.err.println("[D9Render] Layer '" + layerName + "' is missing.");
            return 0;
        }

        int renderedCount = 0;
        for (int y = 0; y < layer.length; y++) {
            for (int x = 0; x < layer[y].length; x++) {
                int gid = layer[y][x] & 0x1FFFFFFF;
                if (gid > 0 && drawTile(g2d, gid, x, y)) {
                    renderedCount++;
                }
            }
        }
        return renderedCount;
    }

    private boolean drawTile(Graphics2D g2d, int gid, int tileXOnMap, int tileYOnMap) {
        TilesetImage tilesetImage = findTilesetImage(gid);
        if (tilesetImage == null) {
            if (loggedUnresolvedGids.add(gid)) {
                System.err.println("[D9Render] unresolved gid skipped: " + gid);
            }
            return false;
        }

        int destX1 = tileXOnMap * map.getTileWidth();
        int destY1 = tileYOnMap * map.getTileHeight();
        int destX2 = destX1 + map.getTileWidth();
        int destY2 = destY1 + map.getTileHeight();

        D9Map.TilesetInfo info = tilesetImage.info;
        int localId = gid - info.getFirstGid();
        if (localId < 0 || localId >= info.getTileCount()) {
            if (loggedOutOfRangeGids.add(gid)) {
                System.err.println("[D9Render] gid out of range skipped: " + gid
                        + " -> tilesetFirstGid=" + info.getFirstGid()
                        + " tileCount=" + info.getTileCount());
            }
            return false;
        }

        int sourceX = (localId % info.getColumns()) * info.getTileWidth();
        int sourceY = (localId / info.getColumns()) * info.getTileHeight();
        if (tilesetImage.image == null
                || sourceX + info.getTileWidth() > tilesetImage.image.getWidth()
                || sourceY + info.getTileHeight() > tilesetImage.image.getHeight()) {
            if (loggedOutOfRangeGids.add(gid)) {
                System.err.println("[D9Render] gid out of range skipped: " + gid
                        + " -> tilesetFirstGid=" + info.getFirstGid()
                        + " sourceRect=(" + sourceX + "," + sourceY + "," + info.getTileWidth() + "," + info.getTileHeight() + ")"
                        + " imageSize=(" + (tilesetImage.image != null ? tilesetImage.image.getWidth() : 0)
                        + "," + (tilesetImage.image != null ? tilesetImage.image.getHeight() : 0) + ")");
            }
            return false;
        }

        if (loggedResolvedGids.add(gid)) {
            System.out.println("[D9Render] gid=" + gid + " -> tilesetFirstGid=" + info.getFirstGid()
                    + ", localId=" + localId + ", source=" + tilesetImage.info.getImageSource());
        }
        g2d.drawImage(tilesetImage.image,
                destX1, destY1, destX2, destY2,
                sourceX, sourceY, sourceX + info.getTileWidth(), sourceY + info.getTileHeight(), null);
        return true;
    }

    private TilesetImage findTilesetImage(int gid) {
        for (int i = tilesetImages.size() - 1; i >= 0; i--) {
            TilesetImage tilesetImage = tilesetImages.get(i);
            D9Map.TilesetInfo info = tilesetImage.info;
            if (gid >= info.getFirstGid() && gid < info.getFirstGid() + info.getTileCount()) {
                return tilesetImage;
            }
        }
        return null;
    }

    private void renderPlayer(Graphics2D g2d) {
        String spriteKey = getCurrentSpriteKey();
        BufferedImage sprite = sprites.get(spriteKey);

        if (sprite != null) {
            int drawWidth = (int) (sprite.getWidth() * map.getSettings().getPlayerScale());
            int drawHeight = (int) (sprite.getHeight() * map.getSettings().getPlayerScale());
            D9Map.Rectangle bounds = player.getBounds();
            int drawX = bounds.x + (bounds.width - drawWidth) / 2;
            int drawY = bounds.y + bounds.height - drawHeight;

            g2d.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, null);
        } else {
            // Fallback rectangle
            g2d.setColor(Color.BLUE);
            g2d.fillRect((int) player.getX(), (int) player.getY(), player.getWidth(), player.getHeight());
        }
    }

    private String getCurrentSpriteKey() {
        String direction = player.isFacingRight() ? "right" : "left";
        switch (player.getState()) {
            case CHARGE: return "charge_" + direction;
            case JUMP: return "jump_" + direction;
            default: return "stand_" + direction;
        }
    }

    private void drawDebug(Graphics2D g2d) {
        g2d.setColor(new Color(255, 70, 70));
        for (D9Map.Rectangle box : map.getPlatformCollisionBoxes()) {
            g2d.drawRect(box.x, box.y, box.width, box.height);
        }

        g2d.setColor(new Color(185, 80, 255));
        for (D9Map.Rectangle box : map.getExplicitBoundaryCollisionBoxes()) {
            g2d.drawRect(box.x, box.y, box.width, box.height);
        }

        g2d.setColor(new Color(255, 80, 200));
        for (D9Map.Rectangle box : map.getFallbackBoundaryCollisionBoxes()) {
            g2d.drawRect(box.x, box.y, box.width, box.height);
        }

        // Player bounds
        D9Map.Rectangle playerBounds = player.getBounds();
        g2d.setColor(Color.GREEN);
        g2d.drawRect(playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);

        for (D9Object spawn : map.getPlayerSpawns()) {
            if (!"spawn_floor_1".equals(spawn.getName())) {
                continue;
            }
            g2d.setColor(Color.CYAN);
            g2d.drawRect(spawn.getX(), spawn.getY(), spawn.getWidth(), spawn.getHeight());
        }

        if (selectedSpawn != null) {
            g2d.setColor(new Color(120, 255, 255));
            g2d.drawRect(selectedSpawn.getX(), selectedSpawn.getY(), selectedSpawn.getWidth(), selectedSpawn.getHeight());
            if (selectedSpawnSnap != null && selectedSpawnSnap.platform != null) {
                g2d.setColor(new Color(255, 220, 90));
                g2d.drawRect(selectedSpawnSnap.platform.x, selectedSpawnSnap.platform.y,
                        selectedSpawnSnap.platform.width, selectedSpawnSnap.platform.height);
            }
        }

        g2d.setColor(Color.ORANGE);
        for (D9Object door : map.getQuizDoors()) {
            g2d.drawRect(door.getX(), door.getY(), door.getWidth(), door.getHeight());
        }
    }

    private void updateCamera() {
        if (!map.getSettings().isCameraFollow()) return;

        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int mapWidth = map.getWidth() * map.getTileWidth();
        int mapHeight = map.getHeight() * map.getTileHeight();

        // Follow player vertically (for tower climbing)
        cameraY = (int) player.getY() - screenHeight / 2;
        cameraY = Math.max(0, Math.min(cameraY, mapHeight - screenHeight));

        // Keep horizontal centered or follow slightly
        cameraX = (int) player.getX() - screenWidth / 2;
        cameraX = Math.max(0, Math.min(cameraX, mapWidth - screenWidth));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (player != null && collisionManager != null && !quizInProgress) {
            // Handle input
            if (leftPressed) player.moveLeft();
            else if (rightPressed) player.moveRight();
            else player.stopMoving();

            if (jumpPressed) {
                if (player.getState() == D9Player.PlayerState.STAND && player.isOnGround()) {
                    if (player.startCharge(collisionManager)) {
                        System.out.println("[D9Input] start charge");
                    } else {
                        System.out.println("[D9Input] jump blocked reason=player not on ground");
                    }
                }
                if (player.getState() == D9Player.PlayerState.CHARGE) {
                    player.updateCharge();
                }
            }

            // Update physics
            player.update(collisionManager);

            // Update camera
            updateCamera();

            // Check interactions
            checkQuizDoors();

            if (map.getSettings().isDebugDraw()) {
                debugFrameCounter++;
                if (debugFrameCounter >= 60) {
                    debugFrameCounter = 0;
                    System.out.println("[D9] Debug player x=" + (int) player.getX()
                            + " y=" + (int) player.getY()
                            + " velocityY=" + String.format("%.2f", player.getVy())
                            + " onGround=" + player.isOnGround()
                            + " platforms=" + map.getPlatformCollisionBoxes().size()
                            + " boundaries=" + map.getBoundaryCollisionBoxes().size());
                }
            }

            repaint();
        } else {
            repaint();
        }
    }

    private void checkQuizDoors() {
        D9Map.Rectangle playerBounds = player.getBounds();
        boolean foundDoor = false;
        for (D9Object door : map.getQuizDoors()) {
            D9Map.Rectangle doorBounds = new D9Map.Rectangle(door.getX(), door.getY(), door.getWidth(), door.getHeight());
            if (playerBounds.intersects(doorBounds)) {
                foundDoor = true;
                if (activeQuizDoor == null || !door.getName().equals(activeQuizDoor.getName())) {
                    activeQuizDoor = door;
                    handleQuizDoor(door);
                }
                break;
            }
        }
        if (!foundDoor) {
            activeQuizDoor = null;
        }
    }

    private void handleQuizDoor(D9Object door) {
        if (quizManager == null || checkpointManager == null) {
            return;
        }

        String questionSet = door.getProperty("questionSet");
        if (questionSet == null) return;

        quizInProgress = true;
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        player.stopMoving();
        if (gameTimer != null) {
            gameTimer.stop();
        }

        try {
            D9QuizManager.QuizResult result = quizManager.conductQuiz(this, door);
            System.out.println("[D9] Quiz result for " + door.getName()
                    + ": " + result.correct + "/" + result.total
                    + " required=" + result.requiredCorrect
                    + " passed=" + result.passed);

            if (result.passed) {
                handleQuizSuccess(door);
            } else {
                handleQuizFailure(door);
            }
        } finally {
            quizInProgress = false;
            if (gameTimer != null && isShowing()) {
                gameTimer.start();
            }
            requestFocusInWindow();
        }
    }

    private void handleQuizSuccess(D9Object door) {
        if ("FinalQuizDoor".equals(door.getClassType())) {
            String victoryMessage = door.getPropertyOrDefault(
                    "victoryMessage",
                    "Chúc mừng! Bạn đã vượt qua D9 và phá đảo thử thách OOP!");
            JOptionPane.showMessageDialog(this, victoryMessage, "D9 Victory", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String successCheckpoint = door.getProperty("successCheckpoint");
        if (successCheckpoint != null) {
            checkpointManager.teleportToCheckpoint(player, successCheckpoint, collisionManager);
            checkpointManager.saveCheckpoint(player, successCheckpoint);
        } else {
            System.err.println("[D9] Warning: quiz door " + door.getName()
                    + " missing successCheckpoint; staying in place.");
        }
    }

    private void handleQuizFailure(D9Object door) {
        String punishment = door.getPropertyOrDefault("punishment", "FallToCheckpoint");
        int floor = door.getFloor();
        switch (punishment) {
            case "FallToFloorStart":
                placePlayerAtSpawn(false);
                break;
            case "FallOneFloor":
                if (!checkpointManager.teleportToFloorOrPrevious(player, floor - 1, collisionManager)) {
                    D9CollisionManager.SnapResult fallback = collisionManager.snapToNearestSafePlatform(
                            player.getX(), player.getY(), player.getWidth(), player.getHeight());
                    player.teleportTo(fallback.x, fallback.y, Math.max(1, floor - 1));
                }
                break;
            case "FallToCheckpoint":
            default:
                if (!checkpointManager.teleportToFloorOrPrevious(player, floor, collisionManager)) {
                    placePlayerAtSpawn(false);
                }
                break;
        }
    }

    private void placePlayerAtSpawn(boolean initial) {
        D9Object spawn = map != null ? selectSpawnForPlayer() : null;
        selectedSpawn = spawn;
        selectedSpawnSnap = null;
        if (spawn != null) {
            selectedSpawnSnap = collisionManager.snapObjectToNearestPlatform(
                    spawn, player.getWidth(), player.getHeight());
            System.out.println("[D9Spawn] selected spawn_floor_1 at x=" + spawn.getX()
                    + ", y=" + spawn.getY()
                    + ", w=" + spawn.getWidth()
                    + ", h=" + spawn.getHeight());
            System.out.println("[D9Spawn] nearest platform under spawn: " + formatRectangle(selectedSpawnSnap.platform));
            System.out.println("[D9Spawn] initial player center: " + (selectedSpawnSnap.x + player.getWidth() / 2.0));
            System.out.println("[D9Spawn] initial player feet/bottom: " + (selectedSpawnSnap.y + player.getHeight()));
            System.out.println("[D9Spawn] direction: " + spawn.getDirection());
            System.out.println("[D9Spawn] platforms near spawn: " + selectedSpawnSnap.platformsNearZone);
            if (selectedSpawnSnap.platform != null) {
                System.out.println("[D9] Player snapped to platform: true");
                System.out.println("[D9] Player snapped platform: " + formatRectangle(selectedSpawnSnap.platform));
                if (!selectedSpawnSnap.foundPlatformUnderZone) {
                    String message = "[D9] No platform directly below/overlapping spawn_floor_1; using nearest safe platform. "
                            + "spawn=" + formatObject(spawn)
                            + ", nearestPlatform=" + formatRectangle(selectedSpawnSnap.nearestPlatform)
                            + ", platformCollisionCount=" + map.getPlatformCollisionBoxes().size();
                    if (selectedSpawnSnap.platformsNearZone > 0) {
                        System.out.println(message);
                    } else {
                        System.err.println("[D9] Warning: " + message.substring("[D9] ".length()));
                    }
                }
                player.teleportTo(selectedSpawnSnap.x, selectedSpawnSnap.y, spawn.getFloor());
            } else {
                System.out.println("[D9] Player snapped to platform: false");
                System.err.println("[D9] Warning: no platform exists for spawn snapping; using spawn fallback. "
                        + "spawn=" + formatObject(spawn)
                        + ", nearestPlatform=" + formatRectangle(selectedSpawnSnap.nearestPlatform)
                        + ", platformCollisionCount=" + map.getPlatformCollisionBoxes().size());
                player.teleportTo(spawn.getX(), spawn.getY(), spawn.getFloor());
            }
            player.setFacingRight(!"left".equalsIgnoreCase(spawn.getDirection()));
            player.setLastCheckpointId(spawn.getSpawnId());
        } else {
            System.err.println("[D9] Warning: spawn_floor_1 missing; using nearest safe platform fallback.");
            int fallbackY = map != null ? Math.max(100, map.getPixelHeight() - 160) : 100;
            D9CollisionManager.SnapResult fallback = collisionManager.snapToNearestSafePlatform(
                    100, fallbackY, player.getWidth(), player.getHeight());
            player.teleportTo(fallback.x, fallback.y, 1);
        }

        if (initial) {
            System.out.println("[D9] Player initial world position: x=" + (int) player.getX()
                    + ", y=" + (int) player.getY());
            D9Map.Rectangle hitbox = player.getBounds();
            System.out.println("[D9] Player hitbox: x=" + hitbox.x
                    + ", y=" + hitbox.y
                    + ", w=" + hitbox.width
                    + ", h=" + hitbox.height);
        }
    }

    private D9Object selectSpawnForPlayer() {
        System.out.println("[D9Spawn] TASK 3 - Selecting spawn_floor_1 from " + map.getPlayerSpawns().size() + " total PlayerSpawn objects");
        List<D9Object> spawns = new ArrayList<>();
        for (D9Object spawn : map.getPlayerSpawns()) {
            if ("spawn_floor_1".equals(spawn.getName()) && "PlayerSpawn".equals(spawn.getClassType())) {
                spawns.add(spawn);
            }
        }
        if (spawns.isEmpty()) {
            for (D9Object spawn : map.getPlayerSpawns()) {
                if (!"map_bound_bottom".equals(spawn.getName()) && !"floor_1_start_platform".equals(spawn.getName())) {
                    spawns.add(spawn);
                }
            }
        }
        System.out.println("[D9Spawn] Found " + spawns.size() + " spawn_floor_1 candidates");
        if (spawns.isEmpty()) {
            return null;
        }
        if (spawns.size() > 1) {
            System.err.println("[D9Spawn] Warning: multiple PlayerSpawn objects named spawn_floor_1 found: "
                    + spawns.size() + ". Selecting the safest spawn zone.");
        }

        System.out.println("[D9Spawn] PlayerSpawn candidates: " + spawns.size());

        D9Object bestNearbySpawn = null;
        D9CollisionManager.SnapResult bestNearbySnap = null;
        for (D9Object spawn : spawns) {
            D9CollisionManager.SnapResult snap = collisionManager.snapObjectToNearestPlatform(
                    spawn, player.getWidth(), player.getHeight());
            System.out.println("[D9Spawn] candidate spawn x=" + spawn.getX()
                    + ", y=" + spawn.getY()
                    + ", w=" + spawn.getWidth()
                    + ", h=" + spawn.getHeight()
                    + ", platform=" + formatRectangle(snap.platform)
                    + ", foundUnder=" + snap.foundPlatformUnderZone
                    + ", nearCount=" + snap.platformsNearZone);
            if (bestNearbySpawn == null) {
                bestNearbySpawn = spawn;
                bestNearbySnap = snap;
                continue;
            }

            boolean currentBetter = false;
            if (snap.foundPlatformUnderZone != bestNearbySnap.foundPlatformUnderZone) {
                currentBetter = snap.foundPlatformUnderZone;
            } else if (snap.platform != null && bestNearbySnap.platform != null) {
                if (snap.platform.y != bestNearbySnap.platform.y) {
                    currentBetter = snap.platform.y < bestNearbySnap.platform.y;
                } else if (snap.platformsNearZone != bestNearbySnap.platformsNearZone) {
                    currentBetter = snap.platformsNearZone > bestNearbySnap.platformsNearZone;
                } else {
                    currentBetter = spawn.getY() < bestNearbySpawn.getY();
                }
            } else if (snap.platform != null != (bestNearbySnap.platform != null)) {
                currentBetter = snap.platform != null;
            } else if (snap.platformsNearZone != bestNearbySnap.platformsNearZone) {
                currentBetter = snap.platformsNearZone > bestNearbySnap.platformsNearZone;
            }

            if (currentBetter) {
                bestNearbySpawn = spawn;
                bestNearbySnap = snap;
            }
        }
        if (bestNearbySpawn != null) {
            return bestNearbySpawn;
        }

        for (D9Object spawn : spawns) {
            if (spawn.getHeight() >= player.getHeight()) {
                return spawn;
            }
        }
        return spawns.get(0);
    }

    private boolean floorOneStartingPlatformDetected() {
        if (map == null || collisionManager == null) {
            return false;
        }
        for (D9Object spawn : map.getPlayerSpawns()) {
            if (!"spawn_floor_1".equals(spawn.getName())) {
                continue;
            }
            D9CollisionManager.SnapResult snap = collisionManager.snapObjectToNearestPlatform(
                    spawn, player.getWidth(), player.getHeight());
            if (snap.foundPlatformUnderZone || snap.platformsNearZone > 0) {
                return true;
            }
        }
        return false;
    }

    private String formatObject(D9Object object) {
        if (object == null) {
            return "none";
        }
        return "x=" + object.getX()
                + ", y=" + object.getY()
                + ", w=" + object.getWidth()
                + ", h=" + object.getHeight();
    }

    private String formatRectangle(D9Map.Rectangle rectangle) {
        if (rectangle == null) {
            return "none";
        }
        return "x=" + rectangle.x
                + ", y=" + rectangle.y
                + ", w=" + rectangle.width
                + ", h=" + rectangle.height;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftPressed = true;
                System.out.println("[D9Input] keyPressed LEFT");
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightPressed = true;
                System.out.println("[D9Input] keyPressed RIGHT");
                break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                jumpPressed = true;
                System.out.println("[D9Input] keyPressed SPACE");
                break;
            case KeyEvent.VK_ESCAPE:
                com.hust.game.core.GameManager.getInstance().switchMap(0);
                window.showPanel("WORLD_MAP");
                break;
            case KeyEvent.VK_F3:
                if (map != null) {
                    map.getSettings().toggleDebugDraw();
                    System.out.println("[D9] Debug draw: " + map.getSettings().isDebugDraw());
                    repaint();
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftPressed = false;
                System.out.println("[D9Input] keyReleased LEFT");
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightPressed = false;
                System.out.println("[D9Input] keyReleased RIGHT");
                break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                jumpPressed = false;
                System.out.println("[D9Input] keyReleased SPACE");
                if (player != null) {
                    double power = player.getChargePower();
                    if (player.getState() == D9Player.PlayerState.CHARGE && power > 0) {
                        if (player.jump()) {
                            System.out.println("[D9Input] release jump with power=" + String.format("%.2f", power));
                        } else {
                            System.out.println("[D9Input] jump blocked reason=release did not transition");
                        }
                    } else {
                        String reason = player.getState() != D9Player.PlayerState.CHARGE
                                ? "not charging"
                                : "chargePower=" + String.format("%.2f", power);
                        System.out.println("[D9Input] jump blocked reason=" + reason);
                    }
                }
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public void onShown() {
        startGameLoop();
        setFocusable(true);
        setRequestFocusEnabled(true);
        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
            requestFocus();
        });
    }

    public void onHidden() {
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    private void startGameLoop() {
        if (gameTimer == null) {
            gameTimer = new Timer(1000 / 60, this);
        }
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    private static class TilesetImage {
        private final D9Map.TilesetInfo info;
        private final BufferedImage image;

        private TilesetImage(D9Map.TilesetInfo info, BufferedImage image) {
            this.info = info;
            this.image = image;
        }
    }
}
