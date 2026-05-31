package com.hust.game.ui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hust.game.core.GameManager;
import com.hust.game.models.entities.Player;
import com.hust.game.ui.GameWindow;
import com.hust.game.util.AssetLoader;

/**
 * C2Panel - Map C2 dạng side-scrolling: bảng tin, cửa hàng, tượng đài và tòa C2.
 */
public class C2Panel extends JPanel {
    private static final String C2_MAP_PATH = "assets/Map/C2/mapc2.tmx";
    private static final int WORLD_WIDTH = 2300;
    private static final int WORLD_HEIGHT = 650;
    private static final int GROUND_Y = 520;
    private static final int PLAYER_W = 54;
    private static final int PLAYER_H = 86;

    private static final Rectangle SHOP_AREA = new Rectangle(200, 236, 272, 232);
    private static final Rectangle NOTICE_BOARD_AREA = new Rectangle(530, 288, 156, 150);
    private static final Rectangle C2_BUILDING_AREA = new Rectangle(1360, 280, 140, 220);

    private final GameWindow window;
    private final StatsPanel statsPanel;
    private TiledC2Map tiledMap;
    private final BufferedImage standLeft;
    private final BufferedImage standRight;
    private final BufferedImage standFront;
    private final BufferedImage standBack;
    private final BufferedImage[] walkLeft = new BufferedImage[4];
    private final BufferedImage[] walkRight = new BufferedImage[4];
    private final BufferedImage[] walkDown = new BufferedImage[4];
    private final BufferedImage[] walkUp = new BufferedImage[4];
    private final Timer animationTimer;

    private int playerX = 760;
    private int playerY = GROUND_Y - PLAYER_H;
    private int cameraX = 0;
    private int cameraY = 0;
    private int walkFrame = 0;
    private long lastMoveAt = 0;
    private boolean facingRight = true;
    private Direction direction = Direction.RIGHT;
    private boolean walking = false;
    private boolean introShown = false;
    private boolean noticeAccepted = false;
    private boolean citizenActivityCompleted = false;

    public C2Panel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;
        reloadC2Map(true);
        this.standLeft = AssetLoader.loadImage("assets/Vu/character_stand_left (1).png");
        this.standRight = AssetLoader.loadImage("assets/Vu/character_stand_right (1).png");
        this.standFront = AssetLoader.loadImage("assets/Vu/character_stand_front (1).png");
        this.standBack = AssetLoader.loadImage("assets/Vu/character_stand_back (1).png");
        loadWalkSprites();
        this.animationTimer = new Timer(120, e -> updateWalkAnimation());

        setFocusable(true);
        setLayout(null);
        setBackground(new Color(125, 192, 226));
        setupMovement();
        setupMouseInteraction();

        JButton backButton = new JButton("← Về World Map");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setFocusable(false);
        backButton.setBounds(10, 10, 160, 34);
        backButton.addActionListener(e -> {
            GameManager.getInstance().switchMap(0);
            window.showPanel("WORLD_MAP");
        });
        add(backButton);

        animationTimer.start();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                reloadC2Map(false);
                requestFocusInWindow();
                showIntroOnce();
            }

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateCamera();
            }
        });
    }

    public void onShown() {
        reloadC2Map(false);
        updateCamera();
        repaint();
        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
            showIntroOnce();
        });
    }

    private void reloadC2Map(boolean resetPlayerToSpawn) {
        TiledC2Map loadedMap = TiledC2Map.load(C2_MAP_PATH);
        if (loadedMap == null) {
            return;
        }

        boolean hadMap = tiledMap != null;
        tiledMap = loadedMap;
        if (resetPlayerToSpawn || !hadMap) {
            applyPlayerSpawn();
        } else {
            playerX = Math.max(40, Math.min(getWorldWidth() - PLAYER_W - 40, playerX));
            playerY = Math.max(40, Math.min(getWorldHeight() - PLAYER_H - 20, playerY));
        }
        updateCamera();
    }

    private void loadWalkSprites() {
        for (int i = 0; i < 4; i++) {
            int frameNumber = i + 1;
            walkLeft[i] = AssetLoader.loadImage("assets/Vu/character_move_left (" + frameNumber + ").png");
            walkRight[i] = AssetLoader.loadImage("assets/Vu/character_move_right (" + frameNumber + ").png");
            walkDown[i] = AssetLoader.loadImage("assets/Vu/character_move_down (" + frameNumber + ").png");
            walkUp[i] = AssetLoader.loadImage("assets/Vu/character_move_up (" + frameNumber + ").png");
        }
    }

    private void applyPlayerSpawn() {
        if (tiledMap == null) {
            return;
        }
        Rectangle spawn = tiledMap.findObject("player", "Player");
        if (spawn != null) {
            playerX = spawn.x + spawn.width / 2 - PLAYER_W / 2;
            playerY = spawn.y + spawn.height - PLAYER_H;
        }
    }

    private void updateWalkAnimation() {
        if (walking && System.currentTimeMillis() - lastMoveAt > 160) {
            walking = false;
            repaint();
            return;
        }
        if (walking) {
            walkFrame = (walkFrame + 1) % walkRight.length;
            repaint();
        }
    }

    private void setupMovement() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        im.put(KeyStroke.getKeyStroke("A"), "moveLeft");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
        im.put(KeyStroke.getKeyStroke("D"), "moveRight");
        im.put(KeyStroke.getKeyStroke("UP"), "moveUp");
        im.put(KeyStroke.getKeyStroke("W"), "moveUp");
        im.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        im.put(KeyStroke.getKeyStroke("S"), "moveDown");
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "backWorld");

        am.put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                movePlayer(-16, 0);
            }
        });
        am.put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                movePlayer(16, 0);
            }
        });
        am.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                movePlayer(0, -16);
            }
        });
        am.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                movePlayer(0, 16);
            }
        });
        am.put("backWorld", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                GameManager.getInstance().switchMap(0);
                window.showPanel("WORLD_MAP");
            }
        });
    }

    private void setupMouseInteraction() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                Point worldPoint = toWorldPoint(e.getPoint());
                if (getNoticeArea().contains(worldPoint)) {
                    handleNoticeBoard();
                } else if (getC2BuildingArea().contains(worldPoint)) {
                    handleC2Building();
                } else if (getShopArea().contains(worldPoint)) {
                    handleShopEntrance();
                }
            }
        });
    }

    private Point toWorldPoint(Point screenPoint) {
        return new Point(screenPoint.x + cameraX, screenPoint.y + cameraY);
    }

    private void showIntroOnce() {
        if (introShown) {
            return;
        }
        introShown = true;
        SwingUtilities.invokeLater(() -> showNotice("Chào Mừng Đến C2",
                "Bạn đã đến khu C2. Hãy di chuyển tới bảng thông báo để nhận nhiệm vụ đầu tiên."));
    }

    private void movePlayer(int dx, int dy) {
        if (dx < 0) {
            direction = Direction.LEFT;
            facingRight = false;
        } else if (dx > 0) {
            direction = Direction.RIGHT;
            facingRight = true;
        } else if (dy < 0) {
            direction = Direction.UP;
        } else if (dy > 0) {
            direction = Direction.DOWN;
        }
        walking = true;
        lastMoveAt = System.currentTimeMillis();
        int nextX = Math.max(40, Math.min(getWorldWidth() - PLAYER_W - 40, playerX + dx));
        int nextY = Math.max(40, Math.min(getWorldHeight() - PLAYER_H - 20, playerY + dy));

        if (canMoveTo(nextX, playerY)) {
            playerX = nextX;
        }
        if (canMoveTo(playerX, nextY)) {
            playerY = nextY;
        }
        updateCamera();
        repaint();
    }

    private void updateCamera() {
        int viewportWidth = Math.max(1, getWidth());
        int viewportHeight = Math.max(1, getHeight());
        cameraX = playerX + PLAYER_W / 2 - viewportWidth / 2;
        cameraX = Math.max(0, Math.min(cameraX, Math.max(0, getWorldWidth() - viewportWidth)));
        cameraY = playerY + PLAYER_H / 2 - viewportHeight / 2;
        cameraY = Math.max(0, Math.min(cameraY, Math.max(0, getWorldHeight() - viewportHeight)));
    }

    private boolean isPlayerNear(Rectangle area) {
        Rectangle playerArea = new Rectangle(playerX - 70, playerY - 40, PLAYER_W + 140, PLAYER_H + 90);
        return playerArea.intersects(area);
    }

    private int getWorldWidth() {
        return tiledMap != null ? tiledMap.pixelWidth : WORLD_WIDTH;
    }

    private int getWorldHeight() {
        return tiledMap != null ? tiledMap.pixelHeight : WORLD_HEIGHT;
    }

    private boolean canMoveTo(int x, int y) {
        if (tiledMap == null) {
            return true;
        }
        Rectangle currentFeet = getPlayerFeet(playerX, playerY);
        Rectangle nextFeet = getPlayerFeet(x, y);
        int currentOverlap = tiledMap.collisionOverlapArea(currentFeet);
        int nextOverlap = tiledMap.collisionOverlapArea(nextFeet);
        return nextOverlap == 0 || nextOverlap < currentOverlap;
    }

    private Rectangle getPlayerFeet(int x, int y) {
        return new Rectangle(x + 12, y + PLAYER_H - 22, PLAYER_W - 24, 18);
    }

    private Rectangle getShopArea() {
        return getMapObjectArea("shop", "Shop", SHOP_AREA);
    }

    private Rectangle getNoticeArea() {
        return getMapObjectArea("notice", "NoticeBoard", NOTICE_BOARD_AREA);
    }

    private Rectangle getC2BuildingArea() {
        return getMapObjectArea("bachkhoa", "bachkhoa", C2_BUILDING_AREA);
    }

    private Rectangle getMapObjectArea(String name, String type, Rectangle fallback) {
        if (tiledMap == null) {
            return fallback;
        }
        Rectangle area = tiledMap.findObject(name, type);
        return area != null ? area : fallback;
    }

    private void handleNoticeBoard() {
        if (!isPlayerNear(getNoticeArea())) {
            showNotice("Bảng Thông Báo", "Bạn đang đứng quá xa. Hãy đưa Vũ tới gần bảng thông báo để đọc nhiệm vụ.");
            return;
        }

        noticeAccepted = true;
        showNotice("Thông Báo Cho Người Chơi",
                "Chào mừng bạn đến với Đại học Bách khoa Hà Nội.\n\n"
                        + "Việc đầu tiên bạn cần phải làm đó chính là đến tòa C2 và tham gia buổi sinh hoạt công dân do đại học tổ chức dành cho toàn bộ sinh viên năm nhất của Trường Công nghệ Thông tin và Truyền thông.\n\n"
                        + "Yêu cầu bạn di chuyển tới tòa C2 để tham gia hoạt động.");
    }

    private void handleC2Building() {
        if (!isPlayerNear(getC2BuildingArea())) {
            showNotice("Tòa C2", "Bạn đang đứng quá xa. Hãy di chuyển tới trước tòa C2 để tham gia hoạt động.");
            return;
        }
        if (!noticeAccepted) {
            showNotice("Tòa C2", "Bạn nên đọc bảng thông báo trước khi vào tòa C2.");
            return;
        }
        if (citizenActivityCompleted || GameManager.getInstance().getPlayer().isCompletedMap2()) {
            showNotice("Tòa C2", "Bạn đã hoàn thành buổi sinh hoạt công dân tại C2.");
            return;
        }

        ConfirmDialog confirm = new ConfirmDialog(SwingUtilities.getWindowAncestor(this), "Tòa C2",
                "Tòa C2 đang diễn ra buổi sinh hoạt công dân do đại học tổ chức\n"
                        + "dành cho toàn bộ sinh viên năm nhất của Trường CNTT & TT.\n\n"
                        + "Bạn có muốn tham gia không?");
        confirm.setVisible(true);
        if (confirm.isConfirmed()) {
            window.showPanel("MAP_C2_HALL");
        }
    }

    private void handleShopEntrance() {
        ConfirmDialog confirm = new ConfirmDialog(SwingUtilities.getWindowAncestor(this),
                "Quán Căn Tin CF",
                "Bạn có muốn làm việc tại quán CF không?\n\nMức lương: 100,000đ / ngày");
        confirm.setVisible(true);
        if (confirm.isConfirmed()) {
            window.showPanel("MAP_CF");
        }
    }

    private void startCitizenActivityQuiz() {
        C2QuizDialog dialog = new C2QuizDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isPassed()) {
            citizenActivityCompleted = true;
            Player player = GameManager.getInstance().getPlayer();
            player.addDisciplineScore(90);
            player.setCompletedMap2(true);
            statsPanel.updateStats();
            showNotice("Hoàn Thành",
                    "Chúc mừng bạn đã trả lời đúng 5/5 câu hỏi.\n\nBạn đã có 90 điểm rèn luyện.");
        } else {
            showNotice("Chưa Hoàn Thành",
                    "Bạn cần trả lời đúng toàn bộ 5 câu hỏi để hoàn thành buổi sinh hoạt công dân. Hãy thử lại tại tòa C2.");
        }
    }

    private void showNotice(String title, String message) {
        NoticeDialog dialog = new NoticeDialog(SwingUtilities.getWindowAncestor(this), title, message);
        dialog.setVisible(true);
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawSky(g2d);
        g2d.translate(-cameraX, -cameraY);
        drawWorld(g2d);
        drawPlayer(g2d);
        drawForeground(g2d);
        g2d.translate(cameraX, cameraY);
        g2d.dispose();
    }

    private void drawWorld(Graphics2D g2d) {
        if (tiledMap != null) {
            tiledMap.renderBackground(g2d, getPlayerDepthY(), this);
            return;
        }
        drawCampusGround(g2d);
        drawPaths(g2d);
        drawCampusDecor(g2d);
        drawShop(g2d);
        drawNoticeBoard(g2d);
        drawC2Building(g2d);
        drawGroundEdge(g2d);
    }

    private void drawForeground(Graphics2D g2d) {
        if (tiledMap != null) {
            tiledMap.renderForeground(g2d, getPlayerDepthY(), this);
        }
    }

    private int getPlayerDepthY() {
        return playerY + PLAYER_H - 4;
    }

    private void drawSky(Graphics2D g2d) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(174, 219, 220), 0, getHeight(), new Color(239, 247, 229));
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(new Color(255, 255, 255, 210));
        drawCloud(g2d, 60, 40);
        drawCloud(g2d, 650, 30);
        drawCloud(g2d, 1420, 54);
        drawCloud(g2d, 1980, 28);
    }

    private void drawCampusGround(Graphics2D g2d) {
        g2d.setColor(new Color(154, 188, 74));
        g2d.fillRect(0, 180, WORLD_WIDTH, WORLD_HEIGHT - 180);
        g2d.setColor(new Color(186, 114, 70));
        g2d.fillRect(0, GROUND_Y + 65, WORLD_WIDTH, WORLD_HEIGHT - GROUND_Y - 65);
        g2d.setColor(new Color(104, 67, 49));
        for (int x = 0; x < WORLD_WIDTH; x += 58) {
            g2d.drawLine(x, GROUND_Y + 96, x + 32, GROUND_Y + 126);
        }
    }

    private void drawPaths(Graphics2D g2d) {
        g2d.setColor(new Color(209, 184, 151));
        g2d.fillRoundRect(0, 448, WORLD_WIDTH, 82, 14, 14);
        g2d.fillRoundRect(286, 342, 96, 188, 14, 14);
        g2d.fillRoundRect(1130, 342, 96, 188, 14, 14);
        g2d.fillRoundRect(1825, 342, 96, 188, 14, 14);

        g2d.setColor(new Color(159, 126, 103));
        for (int x = 0; x < WORLD_WIDTH; x += 44) {
            g2d.drawLine(x, 448, x + 18, 530);
        }
        for (int y = 456; y < 530; y += 24) {
            g2d.drawLine(0, y, WORLD_WIDTH, y);
        }
    }

    private void drawCampusDecor(Graphics2D g2d) {
        drawTree(g2d, 555, 215, 1.15);
        drawTree(g2d, 1375, 244, 0.9);
        drawTree(g2d, 2210, 255, 1.05);
        drawFlowerPatch(g2d, 520, 420);
        drawFlowerPatch(g2d, 1320, 430);
        drawFlowerPatch(g2d, 2160, 438);
        drawFountain(g2d, 760, 380);
    }

    private void drawTree(Graphics2D g2d, int x, int y, double scale) {
        int trunkW = (int) (34 * scale);
        int trunkH = (int) (96 * scale);
        g2d.setColor(new Color(117, 73, 45));
        g2d.fillRoundRect(x - trunkW / 2, y + 58, trunkW, trunkH, 12, 12);
        g2d.setColor(new Color(80, 129, 59));
        int crown = (int) (92 * scale);
        g2d.fillOval(x - crown, y, crown + 40, crown);
        g2d.fillOval(x - 30, y - 34, crown + 38, crown + 24);
        g2d.fillOval(x - 8, y + 20, crown + 46, crown);
        g2d.setColor(new Color(124, 159, 68));
        g2d.fillOval(x - 54, y + 10, crown / 2, crown / 3);
    }

    private void drawFlowerPatch(Graphics2D g2d, int x, int y) {
        Color[] colors = {new Color(231, 134, 153), new Color(244, 208, 92), new Color(177, 104, 192), new Color(245, 244, 219)};
        g2d.setColor(new Color(76, 133, 62));
        g2d.fillOval(x - 58, y - 18, 125, 46);
        for (int i = 0; i < 18; i++) {
            g2d.setColor(colors[i % colors.length]);
            g2d.fillOval(x - 48 + (i * 13) % 98, y - 12 + (i * 9) % 28, 6, 6);
        }
    }

    private void drawCloudsAndMountains(Graphics2D g2d) {
        for (int x = -120; x < WORLD_WIDTH; x += 430) {
            drawCloud(g2d, x + 80, 70);
            drawMountain(g2d, x, 205, 260, 130, new Color(99, 142, 160));
            drawMountain(g2d, x + 150, 210, 330, 155, new Color(76, 121, 148));
        }
    }

    private void drawCloud(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillOval(x, y + 18, 78, 32);
        g2d.fillOval(x + 38, y, 86, 48);
        g2d.fillOval(x + 96, y + 20, 88, 34);
    }

    private void drawMountain(Graphics2D g2d, int x, int baseY, int width, int height, Color color) {
        Polygon mountain = new Polygon();
        mountain.addPoint(x, baseY);
        mountain.addPoint(x + width / 2, baseY - height);
        mountain.addPoint(x + width, baseY);
        g2d.setColor(color);
        g2d.fillPolygon(mountain);
        g2d.setColor(new Color(235, 244, 246, 180));
        Polygon snow = new Polygon();
        snow.addPoint(x + width / 2, baseY - height);
        snow.addPoint(x + width / 2 - 28, baseY - height + 45);
        snow.addPoint(x + width / 2 + 32, baseY - height + 52);
        g2d.fillPolygon(snow);
    }

    private void drawShop(Graphics2D g2d) {
        int x = SHOP_AREA.x;
        int y = SHOP_AREA.y;

        g2d.setColor(new Color(181, 110, 76));
        g2d.fillRect(x + 32, y + 82, 250, 188);
        g2d.setColor(new Color(222, 154, 94));
        g2d.fillRect(x + 44, y + 94, 226, 164);
        g2d.setColor(new Color(117, 73, 57));
        g2d.drawRect(x + 44, y + 94, 226, 164);

        Polygon roof = new Polygon();
        roof.addPoint(x + 4, y + 92);
        roof.addPoint(x + 154, y + 20);
        roof.addPoint(x + 314, y + 92);
        g2d.setColor(new Color(160, 78, 56));
        g2d.fillPolygon(roof);
        g2d.setColor(new Color(92, 58, 50));
        g2d.drawPolygon(roof);

        g2d.setColor(new Color(241, 203, 125));
        g2d.fillRoundRect(x + 95, y + 54, 128, 48, 8, 8);
        g2d.setColor(new Color(65, 65, 82));
        g2d.drawRoundRect(x + 95, y + 54, 128, 48, 8, 8);
        g2d.setFont(new Font("Arial", Font.BOLD, 29));
        g2d.drawString("SHOP", x + 123, y + 87);

        drawShopAwning(g2d, x + 38, y + 112);
        drawShopDisplay(g2d, x + 66, y + 156);
        drawShopDoor(g2d, x + 206, y + 150);
        drawShopKeeper(g2d, x + 224, GROUND_Y - 95);
        drawFlowerPatch(g2d, x + 282, GROUND_Y - 24);
    }

    private void drawShopAwning(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(245, 244, 219));
        g2d.fillRect(x, y, 226, 34);
        for (int i = 0; i < 8; i++) {
            g2d.setColor(i % 2 == 0 ? new Color(213, 61, 59) : new Color(255, 245, 226));
            g2d.fillRect(x + i * 29, y, 29, 42);
        }
        g2d.setColor(new Color(116, 72, 62));
        g2d.drawRect(x, y, 226, 34);
    }

    private void drawShopDisplay(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(122, 151, 153));
        g2d.fillRect(x, y, 112, 78);
        g2d.setColor(new Color(214, 237, 226, 180));
        g2d.fillRect(x + 7, y + 7, 98, 64);
        g2d.setColor(new Color(116, 72, 62));
        g2d.drawRect(x, y, 112, 78);
        g2d.drawLine(x + 56, y, x + 56, y + 78);
        g2d.setColor(new Color(239, 202, 118));
        g2d.fillOval(x + 18, y + 42, 24, 13);
        g2d.fillOval(x + 72, y + 36, 21, 15);
        g2d.setColor(new Color(201, 103, 74));
        g2d.fillRoundRect(x + 44, y + 48, 18, 16, 5, 5);
    }

    private void drawShopDoor(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(242, 226, 183));
        g2d.fillRect(x, y, 48, 94);
        g2d.setColor(new Color(116, 72, 62));
        g2d.drawRect(x, y, 48, 94);
        g2d.setColor(new Color(197, 58, 55));
        g2d.fillRect(x + 35, y + 16, 10, 50);
        g2d.setColor(new Color(52, 74, 88));
        g2d.fillOval(x + 8, y + 47, 5, 5);
    }

    private void drawShopKeeper(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(230, 174, 128));
        g2d.fillOval(x + 14, y, 28, 30);
        g2d.setColor(new Color(75, 47, 42));
        g2d.fillArc(x + 10, y - 4, 36, 26, 0, 180);
        g2d.setColor(new Color(168, 46, 46));
        g2d.fillRoundRect(x + 8, y + 30, 44, 54, 8, 8);
        g2d.setColor(new Color(246, 220, 130));
        g2d.fillRect(x + 25, y + 38, 10, 20);
        g2d.setColor(new Color(42, 45, 54));
        g2d.fillRect(x + 14, y + 82, 12, 30);
        g2d.fillRect(x + 36, y + 82, 12, 30);
    }

    private void drawNpc(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(244, 200, 150));
        g2d.fillOval(x + 18, y, 34, 34);
        g2d.setColor(new Color(52, 93, 168));
        g2d.fillRoundRect(x + 12, y + 32, 48, 50, 8, 8);
        g2d.setColor(new Color(35, 48, 70));
        g2d.fillRect(x + 18, y + 80, 12, 28);
        g2d.fillRect(x + 42, y + 80, 12, 28);
    }

    private void drawFountain(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(118, 139, 151));
        g2d.fillOval(x, y + 62, 260, 70);
        g2d.setColor(new Color(116, 190, 222));
        g2d.fillOval(x + 22, y + 68, 216, 46);
        g2d.setStroke(new BasicStroke(5f));
        g2d.setColor(new Color(208, 241, 255));
        g2d.drawArc(x + 58, y, 60, 120, 20, 140);
        g2d.drawArc(x + 138, y, 60, 120, 20, 140);
        g2d.drawLine(x + 130, y + 20, x + 130, y + 88);
        g2d.setStroke(new BasicStroke(1f));
    }

    private void drawNoticeBoard(Graphics2D g2d) {
        g2d.setColor(new Color(95, 59, 39));
        g2d.fillRect(NOTICE_BOARD_AREA.x + 18, NOTICE_BOARD_AREA.y + 102, 16, 68);
        g2d.fillRect(NOTICE_BOARD_AREA.x + 132, NOTICE_BOARD_AREA.y + 102, 16, 68);
        g2d.setColor(new Color(42, 87, 115));
        g2d.fillRoundRect(NOTICE_BOARD_AREA.x, NOTICE_BOARD_AREA.y, NOTICE_BOARD_AREA.width, 118, 10, 10);
        g2d.setColor(new Color(255, 219, 109));
        g2d.fillRoundRect(NOTICE_BOARD_AREA.x + 12, NOTICE_BOARD_AREA.y + 15, NOTICE_BOARD_AREA.width - 24, 82, 8, 8);
        g2d.setColor(new Color(45, 58, 75));
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("BANG TIN", NOTICE_BOARD_AREA.x + 38, NOTICE_BOARD_AREA.y + 62);
    }

    private void drawC2Building(Graphics2D g2d) {
        int x = C2_BUILDING_AREA.x;
        int y = C2_BUILDING_AREA.y;

        g2d.setColor(new Color(178, 139, 111));
        g2d.fillRect(x + 14, y + 74, 496, 28);
        g2d.setColor(new Color(226, 205, 178));
        g2d.fillRect(x + 32, y + 102, 470, 258);
        g2d.setColor(new Color(168, 132, 104));
        g2d.drawRect(x + 32, y + 102, 470, 258);

        Polygon roof = new Polygon();
        roof.addPoint(x + 20, y + 78);
        roof.addPoint(x + 250, y + 20);
        roof.addPoint(x + 520, y + 78);
        g2d.setColor(new Color(177, 92, 66));
        g2d.fillPolygon(roof);
        g2d.setColor(new Color(111, 70, 60));
        g2d.drawPolygon(roof);

        drawClockTower(g2d, x + 52, y - 10);
        drawBachKhoaSign(g2d, x + 120, y + 98);
        drawBachKhoaLogo(g2d, x + 300, y + 152);
        drawBuildingWindows(g2d, x, y);
        drawEntrance(g2d, x + 174, y + 246);
        drawSecurityGuard(g2d, x + 218, y + 318);
    }

    private void drawClockTower(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(214, 190, 162));
        g2d.fillRect(x, y + 78, 98, 144);
        g2d.setColor(new Color(148, 111, 91));
        g2d.drawRect(x, y + 78, 98, 144);

        Polygon roof = new Polygon();
        roof.addPoint(x - 8, y + 78);
        roof.addPoint(x + 49, y + 18);
        roof.addPoint(x + 108, y + 78);
        g2d.setColor(new Color(169, 67, 63));
        g2d.fillPolygon(roof);
        g2d.setColor(new Color(92, 62, 58));
        g2d.drawPolygon(roof);

        g2d.setColor(new Color(248, 243, 219));
        g2d.fillOval(x + 29, y + 112, 40, 40);
        g2d.setColor(new Color(67, 57, 54));
        g2d.drawOval(x + 29, y + 112, 40, 40);
        g2d.drawLine(x + 49, y + 132, x + 49, y + 119);
        g2d.drawLine(x + 49, y + 132, x + 60, y + 132);
    }

    private void drawBachKhoaSign(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(250, 242, 220));
        g2d.fillRect(x, y, 190, 70);
        g2d.setColor(new Color(139, 96, 79));
        g2d.drawRect(x, y, 190, 70);
        g2d.setColor(new Color(173, 55, 56));
        g2d.setFont(new Font("Arial", Font.BOLD, 29));
        g2d.drawString("BACH KHOA", x + 12, y + 45);
    }

    private void drawBachKhoaLogo(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(186, 44, 42));
        g2d.fillRect(x, y, 62, 92);
        g2d.setColor(new Color(240, 236, 205));
        g2d.fillRect(x + 11, y + 11, 40, 70);
        g2d.setColor(new Color(255, 229, 118));
        g2d.drawRect(x + 8, y + 8, 46, 76);
        g2d.setColor(new Color(186, 44, 42));
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("H", x + 24, y + 36);
        g2d.drawString("UST", x + 13, y + 64);
    }

    private void drawBuildingWindows(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(87, 110, 118));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int wx = x + 392 + col * 34;
                int wy = y + 140 + row * 46;
                g2d.fillRect(wx, wy, 24, 28);
                g2d.setColor(new Color(216, 232, 218));
                g2d.drawLine(wx + 12, wy, wx + 12, wy + 28);
                g2d.drawLine(wx, wy + 14, wx + 24, wy + 14);
                g2d.setColor(new Color(87, 110, 118));
            }
        }

        for (int col = 0; col < 5; col++) {
            int wx = x + 115 + col * 25;
            g2d.fillRect(wx, y + 186, 14, 88);
            g2d.setColor(new Color(238, 222, 179));
            g2d.drawLine(wx + 7, y + 186, wx + 7, y + 274);
            g2d.setColor(new Color(87, 110, 118));
        }
    }

    private void drawEntrance(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(214, 190, 162));
        g2d.fillRect(x, y, 130, 30);
        g2d.fillRect(x + 18, y + 30, 94, 96);
        g2d.setColor(new Color(125, 88, 75));
        g2d.drawRect(x, y, 130, 30);
        g2d.drawRect(x + 18, y + 30, 94, 96);
        g2d.setColor(new Color(92, 82, 76));
        g2d.fillRect(x + 44, y + 52, 44, 74);
        g2d.setColor(new Color(177, 151, 132));
        g2d.fillRect(x - 18, y + 126, 166, 18);
        g2d.fillRect(x - 34, y + 144, 198, 16);
    }

    private void drawSecurityGuard(Graphics2D g2d, int x, int y) {
        double scale = 0.82;
        Graphics2D guard = (Graphics2D) g2d.create();
        guard.translate(x, y);
        guard.scale(scale, scale);
        guard.setColor(new Color(236, 183, 134));
        guard.fillOval(16, 0, 30, 32);
        guard.setColor(new Color(36, 42, 58));
        guard.fillRoundRect(12, 30, 40, 58, 8, 8);
        guard.setColor(new Color(20, 25, 35));
        guard.fillRect(17, 86, 10, 34);
        guard.fillRect(39, 86, 10, 34);
        guard.setColor(new Color(241, 241, 221));
        guard.fillRect(27, 36, 9, 24);
        guard.setColor(new Color(24, 27, 34));
        guard.fillOval(12, -6, 38, 16);
        guard.dispose();
    }

    private void drawGroundEdge(Graphics2D g2d) {
        g2d.setColor(new Color(77, 117, 55));
        g2d.fillRect(0, GROUND_Y + 22, WORLD_WIDTH, 18);
        g2d.setColor(new Color(102, 67, 48));
        g2d.fillRect(0, GROUND_Y + 40, WORLD_WIDTH, 28);
    }

    private void drawPlayer(Graphics2D g2d) {
        BufferedImage sprite = getPlayerSprite();
        if (sprite != null) {
            g2d.drawImage(sprite, playerX - 10, playerY - 8, PLAYER_W + 20, PLAYER_H + 8, this);
            return;
        }
        g2d.setColor(new Color(47, 91, 189));
        g2d.fillRoundRect(playerX, playerY, PLAYER_W, PLAYER_H, 10, 10);
    }

    private BufferedImage getPlayerSprite() {
        if (walking) {
            BufferedImage[] frames = getWalkFrames();
            BufferedImage frame = frames[walkFrame % frames.length];
            if (frame != null) {
                return frame;
            }
        }
        switch (direction) {
            case LEFT:
                return standLeft;
            case UP:
                return standBack != null ? standBack : standLeft;
            case DOWN:
                return standFront != null ? standFront : standRight;
            case RIGHT:
            default:
                return standRight;
        }
    }

    private BufferedImage[] getWalkFrames() {
        switch (direction) {
            case LEFT:
                return walkLeft;
            case UP:
                return walkUp;
            case DOWN:
                return walkDown;
            case RIGHT:
            default:
                return walkRight;
        }
    }

    private enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    private static class TiledC2Map {
        private final int width;
        private final int height;
        private final int tileWidth;
        private final int tileHeight;
        private final int pixelWidth;
        private final int pixelHeight;
        private final int columns;
        private final int tileCount;
        private final BufferedImage tilesetImage;
        private final List<TileLayer> layers = new ArrayList<>();
        private final List<Rectangle> collisionBoxes = new ArrayList<>();
        private final List<MapObject> objects = new ArrayList<>();

        private TiledC2Map(int width, int height, int tileWidth, int tileHeight,
                           int columns, int tileCount, BufferedImage tilesetImage) {
            this.width = width;
            this.height = height;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.pixelWidth = width * tileWidth;
            this.pixelHeight = height * tileHeight;
            this.columns = columns;
            this.tileCount = tileCount;
            this.tilesetImage = tilesetImage;
        }

        static TiledC2Map load(String mapPath) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setIgnoringComments(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document mapDoc = parse(builder, mapPath);
                Element mapElement = mapDoc.getDocumentElement();
                int width = parseInt(mapElement.getAttribute("width"), 0);
                int height = parseInt(mapElement.getAttribute("height"), 0);
                int tileWidth = parseInt(mapElement.getAttribute("tilewidth"), 16);
                int tileHeight = parseInt(mapElement.getAttribute("tileheight"), 16);

                Element tilesetElement = (Element) mapElement.getElementsByTagName("tileset").item(0);
                String tsxSource = tilesetElement.getAttribute("source");
                // Handle absolute Windows paths (e.g., D:/...) by taking only the filename
                if (tsxSource.matches("[A-Za-z]:/.*") || tsxSource.startsWith("/")) {
                    tsxSource = tsxSource.substring(tsxSource.lastIndexOf('/') + 1);
                }
                String tsxPath = AssetLoader.joinPath(AssetLoader.parentPath(mapPath), tsxSource);
                Document tsxDoc = parse(builder, tsxPath);
                Element tsxElement = tsxDoc.getDocumentElement();
                int columns = parseInt(tsxElement.getAttribute("columns"), 1);
                int tileCount = parseInt(tsxElement.getAttribute("tilecount"), 0);
                Element imageElement = (Element) tsxElement.getElementsByTagName("image").item(0);
                String imagePath = AssetLoader.joinPath(AssetLoader.parentPath(tsxPath), imageElement.getAttribute("source"));
                BufferedImage tilesetImage = AssetLoader.loadImage(imagePath);
                if (tilesetImage == null) {
                    System.err.println("[C2Map] Missing tileset image: " + imagePath);
                    return null;
                }

                TiledC2Map map = new TiledC2Map(width, height, tileWidth, tileHeight, columns, tileCount, tilesetImage);
                NodeList layerNodes = mapElement.getElementsByTagName("layer");
                for (int i = 0; i < layerNodes.getLength(); i++) {
                    Element layerElement = (Element) layerNodes.item(i);
                    String layerName = layerElement.getAttribute("name");
                    int[][] layerData = parseLayer(layerElement, width, height);
                    boolean collisionLayer = isTileCollisionLayer(layerName);
                    boolean depthLayer = isDepthLayer(layerName);
                    map.layers.add(new TileLayer(layerName, layerData, collisionLayer, depthLayer));
                    if (collisionLayer) {
                        map.addCollisionBoxes(layerData);
                    }
                }

                NodeList objectGroupNodes = mapElement.getElementsByTagName("objectgroup");
                for (int i = 0; i < objectGroupNodes.getLength(); i++) {
                    Element objectGroupElement = (Element) objectGroupNodes.item(i);
                    String groupName = objectGroupElement.getAttribute("name");
                    boolean collisionGroup = isObjectCollisionGroup(groupName);
                    NodeList objectNodes = objectGroupElement.getElementsByTagName("object");
                    for (int j = 0; j < objectNodes.getLength(); j++) {
                        Element objectElement = (Element) objectNodes.item(j);
                        String name = objectElement.getAttribute("name");
                        String type = objectElement.getAttribute("type");
                        Rectangle bounds = parseObjectBounds(objectElement, tileWidth, tileHeight);
                        if (bounds.width > 0 && bounds.height > 0) {
                            String objectType = type == null || type.isBlank() ? groupName : type;
                            map.objects.add(new MapObject(name, objectType, bounds));
                            if (collisionGroup && !isPassThroughObject(groupName, name, type)) {
                                map.collisionBoxes.add(bounds);
                            }
                        }
                    }
                }

                System.out.println("[C2Map] Loaded " + mapPath + " (" + map.pixelWidth + "x" + map.pixelHeight + ")");
                return map;
            } catch (Exception e) {
                System.err.println("[C2Map] Failed to load " + mapPath + ": " + e.getMessage());
                return null;
            }
        }

        private static Document parse(DocumentBuilder builder, String path) throws Exception {
            try (InputStream stream = AssetLoader.openStream(path)) {
                Document doc = builder.parse(stream);
                doc.getDocumentElement().normalize();
                return doc;
            }
        }

        private static boolean isTileCollisionLayer(String layerName) {
            return "vatcan".equalsIgnoreCase(layerName)
                    || "collision".equalsIgnoreCase(layerName)
                    || "obstacle".equalsIgnoreCase(layerName);
        }

        private static boolean isDepthLayer(String layerName) {
            return "building".equalsIgnoreCase(layerName)
                    || "treelakecloud".equalsIgnoreCase(layerName)
                    || isTileCollisionLayer(layerName);
        }

        private static boolean isObjectCollisionGroup(String groupName) {
            return "vatcan".equalsIgnoreCase(groupName)
                    || "collision".equalsIgnoreCase(groupName)
                    || "obstacle".equalsIgnoreCase(groupName)
                    || "shop".equalsIgnoreCase(groupName)
                    || "notice".equalsIgnoreCase(groupName)
                    || "bachkhoa".equalsIgnoreCase(groupName)
                    || "c2".equalsIgnoreCase(groupName);
        }

        private static boolean isPassThroughObject(String groupName, String name, String type) {
            return "chodiqua".equalsIgnoreCase(groupName)
                    || "chodiqua".equalsIgnoreCase(name)
                    || "chodiqua".equalsIgnoreCase(type);
        }

        private static Rectangle parseObjectBounds(Element objectElement, int tileWidth, int tileHeight) {
            int x = (int) Math.round(parseDouble(objectElement.getAttribute("x"), 0));
            int y = (int) Math.round(parseDouble(objectElement.getAttribute("y"), 0));
            int objectWidth = (int) Math.round(parseDouble(objectElement.getAttribute("width"), 0));
            int objectHeight = (int) Math.round(parseDouble(objectElement.getAttribute("height"), 0));
            if (objectWidth <= 0) {
                objectWidth = tileWidth;
            }
            if (objectHeight <= 0) {
                objectHeight = tileHeight;
            }
            return new Rectangle(x, y, objectWidth, objectHeight);
        }

        private static int[][] parseLayer(Element layerElement, int width, int height) {
            int[][] data = new int[height][width];
            Element dataElement = (Element) layerElement.getElementsByTagName("data").item(0);
            if (dataElement == null || !"csv".equals(dataElement.getAttribute("encoding"))) {
                return data;
            }

            String[] tokens = dataElement.getTextContent().replace("\r", "").trim().split(",");
            int count = Math.min(tokens.length, width * height);
            for (int i = 0; i < count; i++) {
                data[i / width][i % width] = parseInt(tokens[i].trim(), 0);
            }
            return data;
        }

        void renderBackground(Graphics2D g2d, int playerDepthY, ImageObserver observer) {
            for (TileLayer layer : layers) {
                if (layer.depthLayer) {
                    renderDepthLayer(g2d, layer.data, playerDepthY, true, observer);
                } else {
                    renderLayer(g2d, layer.data, observer);
                }
            }
        }

        void renderForeground(Graphics2D g2d, int playerDepthY, ImageObserver observer) {
            for (TileLayer layer : layers) {
                if (layer.depthLayer) {
                    renderDepthLayer(g2d, layer.data, playerDepthY, false, observer);
                }
            }
        }

        private void addCollisionBoxes(int[][] layer) {
            for (int y = 0; y < layer.length; y++) {
                for (int x = 0; x < layer[y].length; x++) {
                    int gid = layer[y][x] & 0x1FFFFFFF;
                    if (gid > 0) {
                        collisionBoxes.add(new Rectangle(
                                x * tileWidth,
                                y * tileHeight,
                                tileWidth,
                                tileHeight));
                    }
                }
            }
        }

        int collisionOverlapArea(Rectangle bounds) {
            int area = 0;
            for (Rectangle box : collisionBoxes) {
                Rectangle overlap = bounds.intersection(box);
                if (!overlap.isEmpty()) {
                    area += overlap.width * overlap.height;
                }
            }
            return area;
        }

        private void renderLayer(Graphics2D g2d, int[][] layer, ImageObserver observer) {
            for (int y = 0; y < layer.length; y++) {
                for (int x = 0; x < layer[y].length; x++) {
                    int gid = layer[y][x] & 0x1FFFFFFF;
                    if (gid <= 0) {
                        continue;
                    }
                    drawTile(g2d, gid, x, y, observer);
                }
            }
        }

        private void renderDepthLayer(Graphics2D g2d, int[][] layer, int playerDepthY,
                                      boolean behindPlayer, ImageObserver observer) {
            for (int y = 0; y < layer.length; y++) {
                for (int x = 0; x < layer[y].length; x++) {
                    int gid = layer[y][x] & 0x1FFFFFFF;
                    if (gid <= 0) {
                        continue;
                    }
                    int tileBottom = (y + 1) * tileHeight;
                    if ((tileBottom <= playerDepthY) == behindPlayer) {
                        drawTile(g2d, gid, x, y, observer);
                    }
                }
            }
        }

        private void drawTile(Graphics2D g2d, int gid, int mapX, int mapY, ImageObserver observer) {
            int localId = gid - 1;
            if (localId < 0 || (tileCount > 0 && localId >= tileCount)) {
                return;
            }
            int sourceX = (localId % columns) * tileWidth;
            int sourceY = (localId / columns) * tileHeight;
            if (sourceX + tileWidth > tilesetImage.getWidth() || sourceY + tileHeight > tilesetImage.getHeight()) {
                return;
            }

            int destX = mapX * tileWidth;
            int destY = mapY * tileHeight;
            g2d.drawImage(tilesetImage,
                    destX, destY, destX + tileWidth, destY + tileHeight,
                    sourceX, sourceY, sourceX + tileWidth, sourceY + tileHeight,
                    observer);
        }

        Rectangle findObject(String name, String type) {
            Rectangle bestMatch = null;
            int bestArea = -1;
            for (MapObject object : objects) {
                if (name.equalsIgnoreCase(object.name) || type.equalsIgnoreCase(object.type)) {
                    int area = object.bounds.width * object.bounds.height;
                    if (area > bestArea) {
                        bestArea = area;
                        bestMatch = object.bounds;
                    }
                }
            }
            return bestMatch;
        }

        private static int parseInt(String value, int fallback) {
            try {
                return Integer.parseInt(value.trim());
            } catch (Exception ignored) {
                return fallback;
            }
        }

        private static double parseDouble(String value, double fallback) {
            try {
                return Double.parseDouble(value.trim());
            } catch (Exception ignored) {
                return fallback;
            }
        }

        private static class MapObject {
            private final String name;
            private final String type;
            private final Rectangle bounds;

            private MapObject(String name, String type, Rectangle bounds) {
                this.name = name != null ? name : "";
                this.type = type != null ? type : "";
                this.bounds = bounds;
            }
        }

        private static class TileLayer {
            private final String name;
            private final int[][] data;
            private final boolean collisionLayer;
            private final boolean depthLayer;

            private TileLayer(String name, int[][] data, boolean collisionLayer, boolean depthLayer) {
                this.name = name != null ? name : "";
                this.data = data;
                this.collisionLayer = collisionLayer;
                this.depthLayer = depthLayer;
            }
        }
    }

    private static class NoticeDialog extends JDialog {
        NoticeDialog(Window owner, String title, String message) {
            super(owner, title, ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(650, 430);
            setLocationRelativeTo(owner);
            setContentPane(new NoticePanel(title, message, this::dispose));
        }
    }

    private static class ConfirmDialog extends JDialog {
        private boolean confirmed = false;

        ConfirmDialog(Window owner, String title, String message) {
            super(owner, title, ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(620, 340);
            setLocationRelativeTo(owner);
            setContentPane(buildPanel(title, message));
        }

        boolean isConfirmed() { return confirmed; }

        private JPanel buildPanel(String title, String message) {
            JPanel panel = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(11, 44, 70));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(new Color(25, 99, 139));
                    g2d.fill(new RoundRectangle2D.Double(16, 14, getWidth() - 32, getHeight() - 28, 18, 18));
                    g2d.setColor(new Color(7, 31, 55));
                    g2d.fill(new RoundRectangle2D.Double(36, 58, getWidth() - 72, 140, 12, 12));
                    g2d.setColor(new Color(117, 205, 241));
                    g2d.setStroke(new BasicStroke(3f));
                    g2d.draw(new RoundRectangle2D.Double(16, 14, getWidth() - 32, getHeight() - 28, 18, 18));
                    g2d.setColor(new Color(255, 170, 66));
                    g2d.setFont(new Font("Arial", Font.BOLD, 26));
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(title.toUpperCase(), (getWidth() - fm.stringWidth(title.toUpperCase())) / 2, 46);
                    g2d.dispose();
                }
            };

            JTextArea text = new JTextArea(message);
            text.setEditable(false);
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
            text.setOpaque(false);
            text.setForeground(Color.WHITE);
            text.setFont(new Font("Arial", Font.BOLD, 15));
            text.setBounds(48, 66, getWidth() - 96, 128);
            panel.add(text);

            JButton yesBtn = makeConfirmBtn("Có", new Color(39, 174, 96));
            JButton noBtn  = makeConfirmBtn("Không", new Color(192, 57, 43));
            yesBtn.addActionListener(e -> { confirmed = true; dispose(); });
            noBtn.addActionListener(e -> dispose());
            yesBtn.setBounds(110, 214, 160, 42);
            noBtn.setBounds(330, 214, 160, 42);
            panel.add(yesBtn);
            panel.add(noBtn);
            return panel;
        }

        private static JButton makeConfirmBtn(String label, Color bg) {
            JButton btn = new JButton(label);
            btn.setFont(new Font("Arial", Font.BOLD, 15));
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            return btn;
        }
    }

    private static class NoticePanel extends JPanel {
        private final String title;
        private final JTextArea messageArea = new JTextArea();
        private final Runnable closeAction;

        NoticePanel(String title, String message, Runnable closeAction) {
            this.title = title;
            this.closeAction = closeAction;
            setLayout(null);
            messageArea.setText(message);
            messageArea.setOpaque(false);
            messageArea.setEditable(false);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setForeground(new Color(245, 250, 255));
            messageArea.setFont(new Font("Arial", Font.BOLD, 17));
            add(messageArea);

            JButton okButton = new JButton("Đóng");
            okButton.setFont(new Font("Arial", Font.BOLD, 15));
            okButton.setBackground(new Color(25, 99, 139));
            okButton.setForeground(Color.WHITE);
            okButton.setOpaque(true);
            okButton.setBorderPainted(false);
            okButton.setFocusPainted(false);
            okButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            okButton.addActionListener(e -> closeAction.run());
            add(okButton);
        }

        @Override
        public void doLayout() {
            messageArea.setBounds(66, 96, getWidth() - 132, getHeight() - 178);
            Component button = getComponent(1);
            button.setBounds(getWidth() / 2 - 48, getHeight() - 66, 96, 34);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(11, 44, 70));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(new Color(25, 99, 139));
            g2d.fill(new RoundRectangle2D.Double(24, 20, getWidth() - 48, getHeight() - 44, 22, 22));
            g2d.setColor(new Color(7, 31, 55));
            g2d.fill(new RoundRectangle2D.Double(46, 72, getWidth() - 92, getHeight() - 142, 14, 14));
            g2d.setColor(new Color(117, 205, 241));
            g2d.setStroke(new BasicStroke(4f));
            g2d.draw(new RoundRectangle2D.Double(24, 20, getWidth() - 48, getHeight() - 44, 22, 22));
            g2d.draw(new RoundRectangle2D.Double(46, 72, getWidth() - 92, getHeight() - 142, 14, 14));
            g2d.setColor(new Color(255, 170, 66));
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(title.toUpperCase(), (getWidth() - fm.stringWidth(title.toUpperCase())) / 2, 52);
            g2d.dispose();
        }
    }

    private static class C2QuizDialog extends JDialog {
        private final Question[] questions = {
                new Question("Trường Đại học Bách khoa Hà Nội được thành lập vào năm nào?",
                        new String[]{"1966", "1956", "1936", "1946"}, 1),
                new Question("Hiệu trưởng đầu tiên của Đại học Bách khoa Hà Nội là ai?",
                        new String[]{"GS. Trần Đại Nghĩa", "GS. Tạ Quang Bửu", "PGS.TS. Hoàng Minh Sơn", "PGS.TS. Huỳnh Quyết Thắng"}, 0),
                new Question("Thang điểm được sử dụng để tính điểm trung bình tích lũy (CPA) xét tốt nghiệp tại HUST là thang điểm mấy?",
                        new String[]{"Thang điểm 100", "Thang điểm 4", "Thang điểm 10", "Thang điểm 5"}, 1),
                new Question("Hành vi nào sau đây bị nghiêm cấm và xử lý kỷ luật nghiêm khắc nhất trong các kỳ thi tại Đại học Bách khoa Hà Nội?",
                        new String[]{"Dùng bút mực màu xanh để làm bài tự luận", "Gian lận, sử dụng tài liệu, thiết bị công nghệ cao hoặc thi hộ", "Xin thêm giấy nháp từ cán bộ coi thi", "Đến phòng thi muộn trước khi tính giờ làm bài 5 phút"}, 1),
                new Question("Thư viện nào sau đây được coi là thư viện lớn nhất Đông Nam Á?",
                        new String[]{"Thư viện Quốc gia Việt Nam", "Thư viện Tạ Quang Bửu", "Thư viện Trung tâm Văn hóa Hàn Quốc", "Thư viện Hà Nội"}, 1)
        };

        private final JTextArea questionText = new JTextArea();
        private final JLabel progressLabel = new JLabel();
        private final JButton[] answerButtons = new JButton[4];
        private int index = 0;
        private int correct = 0;
        private boolean passed = false;

        C2QuizDialog(Window owner) {
            super(owner, "Sinh Hoạt Công Dân C2", ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(760, 520);
            setLocationRelativeTo(owner);
            buildUi();
            showQuestion();
        }

        boolean isPassed() {
            return passed;
        }

        private void buildUi() {
            JPanel root = new JPanel(new BorderLayout(14, 14));
            root.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
            root.setBackground(new Color(15, 49, 78));
            setContentPane(root);

            JLabel title = new JLabel("SINH HOẠT CÔNG DÂN C2");
            title.setForeground(new Color(255, 184, 73));
            title.setFont(new Font("Arial", Font.BOLD, 28));
            progressLabel.setForeground(Color.WHITE);
            progressLabel.setFont(new Font("Arial", Font.BOLD, 16));
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.add(title, BorderLayout.WEST);
            header.add(progressLabel, BorderLayout.EAST);
            root.add(header, BorderLayout.NORTH);

            questionText.setEditable(false);
            questionText.setLineWrap(true);
            questionText.setWrapStyleWord(true);
            questionText.setFont(new Font("Arial", Font.BOLD, 18));
            questionText.setForeground(Color.WHITE);
            questionText.setBackground(new Color(8, 32, 56));
            questionText.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            root.add(questionText, BorderLayout.CENTER);

            JPanel answers = new JPanel(new GridLayout(4, 1, 6, 6));
            answers.setOpaque(false);
            for (int i = 0; i < answerButtons.length; i++) {
                final int selected = i;
                answerButtons[i] = new JButton();
                answerButtons[i].setFont(new Font("Arial", Font.BOLD, 14));
                answerButtons[i].setFocusPainted(false);
                answerButtons[i].addActionListener(e -> answer(selected));
                answers.add(answerButtons[i]);
            }
            root.add(answers, BorderLayout.SOUTH);
        }

        private void showQuestion() {
            Question question = questions[index];
            progressLabel.setText("Câu " + (index + 1) + "/" + questions.length);
            questionText.setText(question.text);
            for (int i = 0; i < answerButtons.length; i++) {
                answerButtons[i].setText((char) ('A' + i) + ". " + question.options[i]);
                answerButtons[i].setEnabled(true);
            }
        }

        private void answer(int selected) {
            Question question = questions[index];
            if (selected == question.correctIndex) {
                correct++;
            }

            if (index < questions.length - 1) {
                index++;
                showQuestion();
                return;
            }

            passed = correct == questions.length;
            new NoticeDialog(this, "Kết Quả",
                    "Bạn đã trả lời đúng " + correct + "/" + questions.length + " câu.").setVisible(true);
            dispose();
        }

        private static class Question {
            final String text;
            final String[] options;
            final int correctIndex;

            Question(String text, String[] options, int correctIndex) {
                this.text = text;
                this.options = options;
                this.correctIndex = correctIndex;
            }
        }
    }
}
