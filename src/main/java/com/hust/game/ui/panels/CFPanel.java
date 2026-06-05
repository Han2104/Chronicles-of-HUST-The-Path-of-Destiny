package com.hust.game.ui.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import com.hust.game.ui.GameWindow;
import com.hust.game.util.AssetLoader;

public class CFPanel extends JPanel {

    // ── Constants ────────────────────────────────────────────────────────────
    private static final String CF_MAP_PATH  = "assets/Map/CF/mapcf.tmx";
    private static final int    PLAYER_W     = 80;
    private static final int    PLAYER_H     = 128;
    private static final int    INTERACT_RANGE = 80;
    private static final int    MAX_STOCK      = 6;
    private static final int    RESTOCK_AMOUNT = 1; // +1 mỗi lần bấm, tối đa 6
    private static final int    NUM_CUSTOMERS = 4;

    private static final Map<String, Integer> PRICE = new LinkedHashMap<>();
    static {
        PRICE.put("milo",     10000);
        PRICE.put("trachanh", 15000);
        PRICE.put("banhmi",   15000);
        PRICE.put("mitron",   20000);
    }

    private static final Map<String, String> DISPLAY_NAME = new LinkedHashMap<>();
    static {
        DISPLAY_NAME.put("milo",     "Sữa Milo");
        DISPLAY_NAME.put("trachanh", "Trà Chanh");
        DISPLAY_NAME.put("banhmi",   "Bánh Mì");
        DISPLAY_NAME.put("mitron",   "Mì Trộn");
    }

    // ── Game State ───────────────────────────────────────────────────────────
    private enum GameState {
        IDLE,             // waiting for player to click first customer
        SERVING,          // player serves ordered items
        WAITING_PAYMENT,  // all served, player clicks maythanhtoan
        TRANSFER_VERIFYING, // player clicks maythanhtoan to verify transfer
        CHAIR_SELECTING   // player selects a chair for dining-in customer
    }

    // ── Panel fields ─────────────────────────────────────────────────────────
    private final GameWindow   window;
    private final StatsPanel   statsPanel;
    private       TiledCFMap   tiledMap;

    private final BufferedImage standLeft, standRight, standFront, standBack;
    private final BufferedImage[] walkLeft  = new BufferedImage[4];
    private final BufferedImage[] walkRight = new BufferedImage[4];
    private final BufferedImage[] walkDown  = new BufferedImage[4];
    private final BufferedImage[] walkUp    = new BufferedImage[4];
    private final BufferedImage customerSprite;
    private final Timer animationTimer;

    private int  playerX = 824, playerY = 100;
    private int  cameraX = 0,   cameraY = 0;
    private int  walkFrame = 0;
    private long lastMoveAt = 0;
    private boolean walking = false;
    private Direction direction = Direction.DOWN;

    // Inventory
    private final Map<String, Integer> stock = new LinkedHashMap<>();

    // Per-customer order state
    private GameState gameState = GameState.IDLE;
    private final Map<String, Integer> customerOrder = new LinkedHashMap<>(); // what customer wants
    private final Map<String, Integer> orderServed   = new LinkedHashMap<>(); // what player served
    private int queueSize        = NUM_CUSTOMERS;
    private int transferAttempts = 0;
    private final Set<Integer>    occupiedChairs  = new HashSet<>();
    private final List<Rectangle> customerRects   = new ArrayList<>(); // updated each draw

    private String notification = "";
    private long   notifTime    = 0;

    // ── Constructor ──────────────────────────────────────────────────────────
    public CFPanel(GameWindow window, StatsPanel statsPanel) {
        this.window     = window;
        this.statsPanel = statsPanel;

        for (String key : PRICE.keySet()) {
            stock.put(key, MAX_STOCK);
            customerOrder.put(key, 0);
            orderServed.put(key, 0);
        }

        tiledMap       = TiledCFMap.load(CF_MAP_PATH);
        standLeft      = AssetLoader.loadImage("assets/Vu/character_stand_left (1).png");
        standRight     = AssetLoader.loadImage("assets/Vu/character_stand_right (1).png");
        standFront     = AssetLoader.loadImage("assets/Vu/character_stand_front (1).png");
        standBack      = AssetLoader.loadImage("assets/Vu/character_stand_back (1).png");
        customerSprite = snapAlpha(AssetLoader.loadImage("assets/customer_npc.png"), 128);
        loadWalkSprites();

        animationTimer = new Timer(120, e -> updateWalkAnimation());

        setFocusable(true);
        setLayout(null);
        setBackground(new Color(60, 40, 20));

        setupMovement();
        setupMouseInteraction();
        animationTimer.start();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                requestFocusInWindow(); updateCamera();
            }
            @Override public void componentResized(java.awt.event.ComponentEvent e) { updateCamera(); }
        });
    }

    public void onShown() {
        tiledMap = TiledCFMap.load(CF_MAP_PATH);
        updateCamera(); repaint();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    private void goBack() {
        if (queueSize > 0 && gameState != GameState.IDLE || (queueSize > 0 && gameState == GameState.IDLE && queueSize < NUM_CUSTOMERS)) {
            // Đang trong ca làm việc dở chừng
            if (!cfConfirm("Thoát Ca Làm Việc",
                    "Bạn thoát giữa ca làm việc sẽ không nhận được lương!\nBạn có chắc muốn thoát không?")) {
                requestFocusInWindow(); return;
            }
        }
        resetWorkday();
        GameManager.getInstance().switchMap(0);
        window.showPanel("MAP_C2");
    }

    private void resetWorkday() {
        queueSize = NUM_CUSTOMERS;
        gameState = GameState.IDLE;
        customerOrder.replaceAll((k, v) -> 0);
        orderServed.replaceAll((k, v) -> 0);
        transferAttempts = 0;
        occupiedChairs.clear();
        customerRects.clear();
        for (String key : PRICE.keySet()) stock.put(key, MAX_STOCK);
    }

    // ── Sprites ──────────────────────────────────────────────────────────────
    /** Snap mọi pixel có alpha < threshold thành trong suốt hoàn toàn,
     *  loại bỏ semi-transparent halo khi scale sprite xuống nhỏ. */
    private static BufferedImage snapAlpha(BufferedImage src, int threshold) {
        if (src == null) return null;
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb  = src.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                out.setRGB(x, y, alpha < threshold ? 0 : argb | 0xFF000000);
            }
        }
        return out;
    }

    private void loadWalkSprites() {
        for (int i = 0; i < 4; i++) {
            int n = i + 1;
            walkLeft[i]  = AssetLoader.loadImage("assets/Vu/character_move_left ("  + n + ").png");
            walkRight[i] = AssetLoader.loadImage("assets/Vu/character_move_right (" + n + ").png");
            walkDown[i]  = AssetLoader.loadImage("assets/Vu/character_move_down ("  + n + ").png");
            walkUp[i]    = AssetLoader.loadImage("assets/Vu/character_move_up ("    + n + ").png");
        }
    }

    private BufferedImage currentSprite() {
        if (walking) {
            BufferedImage[] frames = switch (direction) {
                case LEFT  -> walkLeft;
                case RIGHT -> walkRight;
                case UP    -> walkUp;
                default    -> walkDown;
            };
            BufferedImage f = frames[walkFrame % frames.length];
            if (f != null) return f;
        }
        return switch (direction) {
            case LEFT  -> standLeft;
            case UP    -> standBack  != null ? standBack  : standLeft;
            case DOWN  -> standFront != null ? standFront : standRight;
            default    -> standRight;
        };
    }

    // ── Input ────────────────────────────────────────────────────────────────
    private void setupMovement() {
        InputMap  im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        String[]  dirs  = {"left","right","up","down"};
        String[]  keys1 = {"LEFT","RIGHT","UP","DOWN"};
        String[]  keys2 = {"A","D","W","S"};
        int[][]   deltas = {{-16,0},{16,0},{0,-16},{0,16}};
        for (int i = 0; i < 4; i++) {
            final int dx = deltas[i][0], dy = deltas[i][1];
            im.put(KeyStroke.getKeyStroke(keys1[i]), dirs[i]);
            im.put(KeyStroke.getKeyStroke(keys2[i]), dirs[i]);
            am.put(dirs[i], new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) { movePlayer(dx, dy); }
            });
        }
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "back");
        am.put("back", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { goBack(); }
        });
    }

    private void setupMouseInteraction() {
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
                handleClick(new Point(e.getX() + cameraX, e.getY() + cameraY));
            }
        });
    }

    // ── Player movement ──────────────────────────────────────────────────────
    private void movePlayer(int dx, int dy) {
        if (dx < 0) direction = Direction.LEFT;
        else if (dx > 0) direction = Direction.RIGHT;
        else if (dy < 0) direction = Direction.UP;
        else              direction = Direction.DOWN;

        walking = true; lastMoveAt = System.currentTimeMillis();

        int nx = clamp(playerX + dx, 16, worldWidth()  - PLAYER_W - 16);
        int ny = clamp(playerY + dy, 16, worldHeight() - PLAYER_H - 16);
        if (canMove(nx, playerY)) playerX = nx;
        if (canMove(playerX, ny)) playerY = ny;

        if (tiledMap != null) {
            Rectangle exit = tiledMap.groupBounds("cuathoat");
            if (exit != null && getPlayerFeet(playerX, playerY).intersects(exit)) { goBack(); return; }
        }
        updateCamera(); repaint();
    }

    private boolean canMove(int x, int y) {
        if (tiledMap == null) return true;
        Rectangle feet = getPlayerFeet(x, y);
        // Vật cản từ map
        for (Rectangle box : tiledMap.collisionBoxes) if (feet.intersects(box)) return false;
        // Khách hàng là vật cản
        for (Rectangle cr : customerRects) if (feet.intersects(cr)) return false;
        return true;
    }

    private Rectangle getPlayerFeet(int x, int y) {
    int feetW = 34;
    int feetH = 16;

    int feetX = x + (PLAYER_W - feetW) / 2;
    int feetY = y + PLAYER_H - 24;

    return new Rectangle(feetX, feetY, feetW, feetH);
}

    private void updateWalkAnimation() {
        if (walking && System.currentTimeMillis() - lastMoveAt > 160) { walking = false; repaint(); return; }
        if (walking) { walkFrame = (walkFrame + 1) % 4; repaint(); }
    }

    private void updateCamera() {
        int vw = Math.max(1, getWidth()), vh = Math.max(1, getHeight());
        cameraX = clamp(playerX + PLAYER_W / 2 - vw / 2, 0, Math.max(0, worldWidth()  - vw));
        cameraY = clamp(playerY + PLAYER_H / 2 - vh / 2, 0, Math.max(0, worldHeight() - vh));
    }

    private int worldWidth()  { return tiledMap != null ? tiledMap.pixelWidth  : 1648; }
    private int worldHeight() { return tiledMap != null ? tiledMap.pixelHeight : 944;  }
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    // ── Click routing ────────────────────────────────────────────────────────
    private void handleClick(Point world) {
        if (tiledMap == null) return;
        Point pc = new Point(playerX + PLAYER_W / 2, playerY + PLAYER_H / 2);

        // Exit door — always works
        Rectangle exit = tiledMap.groupBounds("cuathoat");
        if (exit != null && exit.contains(world)) { goBack(); return; }

        // Restock — always works regardless of state
        for (String food : PRICE.keySet()) {
            Rectangle area = tiledMap.groupBounds("them" + food);
            if (area != null && area.contains(world) && near(pc, area)) {
                handleRestock(food); return;
            }
        }

        switch (gameState) {
            case IDLE -> {
                // Click first customer in queue
                if (!customerRects.isEmpty() && customerRects.get(0).contains(world)) {
                    handleFirstCustomer();
                }
            }
            case SERVING -> {
                // Click food areas to serve
                for (String food : PRICE.keySet()) {
                    Rectangle area = tiledMap.groupBounds(food);
                    if (area != null && area.contains(world) && near(pc, area)) {
                        handleServeFood(food); return;
                    }
                }
                // Payment machine — remind player
                Rectangle pay = tiledMap.groupBounds("maythanhtoan");
                if (pay != null && pay.contains(world))
                    showNotif("Hãy lấy đủ món trước khi thanh toán!");
            }
            case WAITING_PAYMENT -> {
                Rectangle pay = tiledMap.groupBounds("maythanhtoan");
                if (pay != null && pay.contains(world)) handlePaymentMachine();
            }
            case TRANSFER_VERIFYING -> {
                Rectangle pay = tiledMap.groupBounds("maythanhtoan");
                if (pay != null && pay.contains(world)) handleVerifyTransfer();
            }
            case CHAIR_SELECTING -> {
                List<Rectangle> chairs = tiledMap.objectGroups.getOrDefault("ghe", new ArrayList<>());
                for (int i = 0; i < chairs.size(); i++) {
                    if (chairs.get(i).contains(world) && near(pc, chairs.get(i))) {
                        handleChairSelect(i); return;
                    }
                }
                showNotif("Di chuyển lại gần ghế để chọn.");
            }
        }
    }

    private boolean near(Point playerCenter, Rectangle area) {
        double cx = area.x + area.width  / 2.0;
        double cy = area.y + area.height / 2.0;
        double dist = playerCenter.distance(cx, cy);
        return dist < INTERACT_RANGE + Math.max(area.width, area.height) / 2.0;
    }

    // ── Game logic ───────────────────────────────────────────────────────────

    /** Click first customer → random order of 2 items → start serving */
    private void handleFirstCustomer() {
        // Random tổng số món từ 1 đến 10, phân phối ngẫu nhiên vào 4 loại
        customerOrder.replaceAll((k, v) -> 0);
        orderServed.replaceAll((k, v) -> 0);

        int total = 1 + (int)(Math.random() * 10); // 1–10
        List<String> foodKeys = new ArrayList<>(PRICE.keySet());
        for (int i = 0; i < total; i++) {
            String food = foodKeys.get((int)(Math.random() * foodKeys.size()));
            customerOrder.merge(food, 1, Integer::sum);
        }

        // Build menu dialog
        StringBuilder sb = new StringBuilder();
        sb.append("MENU CĂNG TIN\n");
        sb.append("─────────────────────────\n");
        for (Map.Entry<String, Integer> e : PRICE.entrySet())
            sb.append(String.format("  %-12s  %,dđ\n", DISPLAY_NAME.get(e.getKey()), e.getValue()));
        sb.append("\n─────────────────────────\n");
        sb.append("Khách gọi (" + total + " món):\n");
        for (Map.Entry<String, Integer> e : customerOrder.entrySet()) {
            if (e.getValue() > 0)
                sb.append(String.format("  • %-12s  x%d\n",
                        DISPLAY_NAME.get(e.getKey()), e.getValue()));
        }

        cfDialog("Khách Hàng Gọi Món", sb.toString());
        gameState = GameState.SERVING;

        // Thông báo tóm tắt
        StringBuilder notif = new StringBuilder("Cần lấy: ");
        customerOrder.forEach((k, v) -> {
            if (v > 0) notif.append(DISPLAY_NAME.get(k)).append(" x").append(v).append("  ");
        });
        showNotif(notif.toString().trim());
        requestFocusInWindow();
    }

    /** Player clicks food item to serve it */
    private void handleServeFood(String food) {
        int need   = customerOrder.getOrDefault(food, 0);
        int served = orderServed.getOrDefault(food, 0);
        if (need <= 0) {
            showNotif("Khách không gọi món " + DISPLAY_NAME.get(food) + "!");
            return;
        }
        if (served >= need) {
            showNotif("Khách chỉ yêu cầu " + need + " " + DISPLAY_NAME.get(food) + "!");
            return;
        }
        if (stock.getOrDefault(food, 0) <= 0) {
            showNotif(DISPLAY_NAME.get(food) + " đã hết hàng! Hãy nhập thêm.");
            return;
        }
        stock.merge(food, -1, Integer::sum);
        orderServed.put(food, served + 1);
        showNotif("Đã lấy " + DISPLAY_NAME.get(food) + ". Kho còn: " + stock.get(food));

        // Check fully served
        boolean done = true;
        for (String f : PRICE.keySet()) {
            if (orderServed.getOrDefault(f, 0) < customerOrder.getOrDefault(f, 0)) { done = false; break; }
        }
        if (done) {
            gameState = GameState.WAITING_PAYMENT;
            showNotif("Đã phục vụ xong! Mời đến quầy thanh toán.");
        }
        repaint();
    }

    /** Player clicks payment machine → bill → payment method choice */
    private void handlePaymentMachine() {
        // Build bill
        long total = 0;
        StringBuilder bill = new StringBuilder("╔══════════ HÓA ĐƠN ══════════╗\n\n");
        for (Map.Entry<String, Integer> e : customerOrder.entrySet()) {
            if (e.getValue() > 0) {
                long sub = (long) e.getValue() * PRICE.get(e.getKey());
                bill.append(String.format("  %-12s x%d  =  %,dđ\n",
                        DISPLAY_NAME.get(e.getKey()), e.getValue(), sub));
                total += sub;
            }
        }
        bill.append(String.format("\n╠══════════════════════════════╣\n  TỔNG CỘNG       =  %,dđ\n╚══════════════════════════════╝", total));
        cfDialog("Hóa Đơn", bill.toString());

        // Khách random chọn hình thức thanh toán
        boolean isCash = Math.random() < 0.5;
        String method = isCash ? "Tiền mặt" : "Chuyển khoản";
        cfDialog("Hình Thức Thanh Toán", "Khách hàng chọn thanh toán bằng: " + method);

        if (isCash) {
            cfDialog("Thanh Toán Thành Công", "Cảm ơn khách hàng đã sử dụng dịch vụ!");
            askDiningChoice();
        } else {
            transferAttempts = 0;
            gameState = GameState.TRANSFER_VERIFYING;
            showNotif("Bấm vào máy thanh toán để xác nhận chuyển khoản.");
        }
        requestFocusInWindow();
    }

    /** Player clicks payment machine again to verify bank transfer */
    private void handleVerifyTransfer() {
        transferAttempts++;
        // Lần đầu: 60% thành công ngay, lần 2+: 85% thành công
        double successRate = transferAttempts == 1 ? 0.60 : 0.85;
        boolean success = Math.random() < successRate;

        if (success) {
            cfDialog("Thanh Toán Thành Công",
                    "Xác nhận chuyển khoản thành công!\nCảm ơn khách hàng đã sử dụng dịch vụ!");
            askDiningChoice();
        } else {
            cfDialog("Chờ Xác Nhận",
                    "Chưa nhận được thanh toán.\nVui lòng bấm lại để kiểm tra.");
        }
        requestFocusInWindow();
    }

    /** Khách random chọn dùng ở đây hay mang về */
    private void askDiningChoice() {
        boolean dineIn = Math.random() < 0.5;
        String choice = dineIn ? "Dùng ở đây" : "Mang về";
        cfDialog("Hình Thức Sử Dụng", "Khách hàng chọn: " + choice);

        if (dineIn) {
            gameState = GameState.CHAIR_SELECTING;
            showNotif("Chọn ghế trống cho khách (click vào ghế).");
        } else {
            cfDialog("Mang Về", "Cảm ơn! Hẹn gặp lại!");
            nextCustomer();
        }
        requestFocusInWindow();
    }

    /** Player clicks a chair for the dining-in customer */
    private void handleChairSelect(int chairIndex) {
        if (occupiedChairs.contains(chairIndex)) {
            showNotif("Ghế này đã có người ngồi! Chọn ghế khác.");
            return;
        }
        occupiedChairs.add(chairIndex);
        cfDialog("Đã Sắp Xếp", "Khách đã được sắp xếp chỗ ngồi. Chúc ngon miệng!");
        nextCustomer();
        requestFocusInWindow();
    }

    /** Move to next customer in queue */
    private void nextCustomer() {
        queueSize = Math.max(0, queueSize - 1);
        customerOrder.replaceAll((k, v) -> 0);
        orderServed.replaceAll((k, v) -> 0);
        transferAttempts = 0;
        gameState = GameState.IDLE;

        if (queueSize > 0) {
            showNotif("Khách tiếp theo đang chờ! Còn " + queueSize + " khách.");
        } else {
            endWorkday();
        }
        repaint();
    }

    private void endWorkday() {
        double salary = 100_000;
        GameManager.getInstance().getPlayer().addFinance(salary);
        statsPanel.updateStats();
        cfDialog("Hết Ca Làm Việc",
                "Kết thúc một ngày làm việc!\n\n"
                + "Bạn đã phục vụ " + NUM_CUSTOMERS + " khách hàng.\n"
                + "Lương nhận được: 100,000 VNĐ\n\n"
                + "Tài chính đã được cập nhật!");
        resetWorkday();
        GameManager.getInstance().switchMap(0);
        window.showPanel("MAP_C2");
    }

    /** Restock a food item (capped at MAX_STOCK) */
    private void handleRestock(String food) {
        int cur = stock.getOrDefault(food, 0);
        if (cur >= MAX_STOCK) {
            showNotif(DISPLAY_NAME.get(food) + " đã đầy kho (" + MAX_STOCK + ")!");
            return;
        }
        int added = Math.min(RESTOCK_AMOUNT, MAX_STOCK - cur);
        stock.put(food, cur + added);
        showNotif("Thêm " + added + " " + DISPLAY_NAME.get(food) + ". Kho: " + stock.get(food) + "/" + MAX_STOCK);
        repaint();
    }

    private void showNotif(String msg) {
        notification = msg; notifTime = System.currentTimeMillis(); repaint();
    }

    /** Show a styled CF info dialog (replaces JOptionPane.showMessageDialog) */
    private void cfDialog(String title, String message) {
        new CFInfoDialog(SwingUtilities.getWindowAncestor(this), title, message).setVisible(true);
        requestFocusInWindow();
    }

    /** Show a styled CF confirm dialog; returns true if user confirms */
    private boolean cfConfirm(String title, String message) {
        CFConfirmDialogCF dlg = new CFConfirmDialogCF(SwingUtilities.getWindowAncestor(this), title, message);
        dlg.setVisible(true);
        requestFocusInWindow();
        return dlg.confirmed;
    }

    // ── Styled dialogs ────────────────────────────────────────────────────────
    private static final Color CF_BG      = new Color(10, 30, 50);
    private static final Color CF_CARD    = new Color(18, 55, 88);
    private static final Color CF_INNER   = new Color(6, 22, 38);
    private static final Color CF_BORDER  = new Color(80, 180, 230);
    private static final Color CF_TITLE   = new Color(255, 190, 60);
    private static final Color CF_BTN_OK  = new Color(30, 130, 200);
    private static final Color CF_BTN_NO  = new Color(170, 50, 40);

    private static JButton cfBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 15));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static class CFInfoDialog extends JDialog {
        private static final int W = 620, H = 420;
        CFInfoDialog(Window owner, String title, String message) {
            super(owner, "", ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            setSize(W, H);
            setLocationRelativeTo(owner);
            setContentPane(buildPanel(title, message));
        }

        private JPanel buildPanel(String title, String msg) {
            JPanel p = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(CF_BG);    g2.fillRect(0, 0, W, H);
                    g2.setColor(CF_CARD);  g2.fill(new RoundRectangle2D.Double(12, 12, W-24, H-24, 18, 18));
                    g2.setColor(CF_INNER); g2.fill(new RoundRectangle2D.Double(28, 62, W-56, H-130, 12, 12));
                    g2.setColor(CF_BORDER); g2.setStroke(new BasicStroke(2.5f));
                    g2.draw(new RoundRectangle2D.Double(12, 12, W-24, H-24, 18, 18));
                    g2.setColor(CF_TITLE); g2.setFont(new Font("Arial", Font.BOLD, 22));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(title.toUpperCase(), (W-fm.stringWidth(title.toUpperCase()))/2, 46);
                    g2.dispose();
                }
            };

            JTextArea ta = new JTextArea(msg);
            ta.setEditable(false); ta.setLineWrap(true); ta.setWrapStyleWord(true);
            ta.setOpaque(false); ta.setForeground(Color.WHITE);
            ta.setFont(new Font("Arial", Font.PLAIN, 15));

            JScrollPane scroll = new JScrollPane(ta,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.setBounds(38, 70, W-76, H-148);
            p.add(scroll);

            JButton ok = cfBtn("Đóng", CF_BTN_OK);
            ok.setBounds((W-120)/2, H-58, 120, 40);
            ok.addActionListener(e -> dispose());
            p.add(ok);
            return p;
        }
    }

    private static class CFConfirmDialogCF extends JDialog {
        boolean confirmed = false;
        CFConfirmDialogCF(Window owner, String title, String message) {
            super(owner, "", ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            setSize(600, 300);
            setLocationRelativeTo(owner);
            setContentPane(buildPanel(title, message));
        }

        private JPanel buildPanel(String title, String msg) {
            JPanel p = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(CF_BG);    g2.fillRect(0, 0, 600, 300);
                    g2.setColor(CF_CARD);  g2.fill(new RoundRectangle2D.Double(12, 12, 576, 276, 18, 18));
                    g2.setColor(CF_INNER); g2.fill(new RoundRectangle2D.Double(28, 62, 544, 148, 12, 12));
                    g2.setColor(CF_BORDER); g2.setStroke(new BasicStroke(2.5f));
                    g2.draw(new RoundRectangle2D.Double(12, 12, 576, 276, 18, 18));
                    g2.setColor(CF_TITLE); g2.setFont(new Font("Arial", Font.BOLD, 22));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(title.toUpperCase(), (600-fm.stringWidth(title.toUpperCase()))/2, 46);
                    g2.dispose();
                }
            };
            JTextArea ta = new JTextArea(msg);
            ta.setEditable(false); ta.setLineWrap(true); ta.setWrapStyleWord(true);
            ta.setOpaque(false); ta.setForeground(Color.WHITE);
            ta.setFont(new Font("Arial", Font.PLAIN, 15));
            ta.setBounds(38, 70, 524, 140); p.add(ta);

            JButton yes = cfBtn("Có", CF_BTN_OK);
            JButton no  = cfBtn("Không", CF_BTN_NO);
            yes.setBounds(120, 238, 150, 42); no.setBounds(330, 238, 150, 42);
            yes.addActionListener(e -> { confirmed = true;  dispose(); });
            no.addActionListener(e ->  { confirmed = false; dispose(); });
            p.add(yes); p.add(no);
            return p;
        }
    }

    // ── Painting ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.translate(-cameraX, -cameraY);
        if (tiledMap != null) tiledMap.render(g2d, this);
        drawOccupiedChairMarkers(g2d);
        drawCustomers(g2d);
        drawPlayer(g2d);
        g2d.translate(cameraX, cameraY);

        drawHud(g2d);
        drawNotification(g2d);
        g2d.dispose();
    }

    private void drawPlayer(Graphics2D g2d) {
        BufferedImage sp = currentSprite();
        if (sp != null) g2d.drawImage(sp, playerX - 10, playerY - 8, PLAYER_W + 20, PLAYER_H + 8, this);
        else { g2d.setColor(new Color(47, 91, 189)); g2d.fillRoundRect(playerX, playerY, PLAYER_W, PLAYER_H, 10, 10); }
    }

    private void drawCustomers(Graphics2D g2d) {
    customerRects.clear();
    if (queueSize <= 0 || customerSprite == null || tiledMap == null) return;
    Rectangle counter = tiledMap.groupBounds("maythanhtoan");
    if (counter == null) return;

    int spriteW = 80;
    int spriteH = 128;

    int spacingX = spriteW + 6;
    int rowGap = 55;
    int firstCustomerGap = 100;

    int baseY = counter.y + counter.height / 2 - spriteH / 2;

    List<Rectangle> obstacles = new ArrayList<>(tiledMap.objectGroups.getOrDefault("ghe", new ArrayList<>()));
    for (Rectangle vc : tiledMap.collisionBoxes) {
        if (vc.width * vc.height > 400) obstacles.add(vc);
    }

    List<Point> positions = new ArrayList<>();

    int startX = counter.x - spacingX - firstCustomerGap;

    for (int i = 0; i < queueSize; i++) {
        int row = i / 2;
        int col = i % 2;

        int x = startX - col * spacingX;
        int y = baseY + row * rowGap;

        // Check vật cản bằng toàn bộ thân khách
        Rectangle candHitbox = new Rectangle(x, y, spriteW, spriteH);

        boolean blocked = obstacles.stream().anyMatch(candHitbox::intersects);
        if (!blocked) {
            positions.add(new Point(x, y));
        }
    }

    for (Point p : positions) {
        g2d.drawImage(customerSprite, p.x, p.y, spriteW, spriteH, this);

        // Toàn bộ sprite khách là vật cản
        Rectangle hitbox = new Rectangle(p.x, p.y, spriteW, spriteH);
        customerRects.add(hitbox);
    }

    if (gameState == GameState.IDLE && !customerRects.isEmpty()) {
        Rectangle first = customerRects.get(0);

        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(new Color(255, 220, 50));
        g2d.drawString("Click", first.x - 2, first.y - 8);
    }
}
    private void drawOccupiedChairMarkers(Graphics2D g2d) {
        if (tiledMap == null || occupiedChairs.isEmpty()) return;
        List<Rectangle> chairs = tiledMap.objectGroups.getOrDefault("ghe", new ArrayList<>());
        g2d.setColor(new Color(220, 60, 60, 180));
        for (int idx : occupiedChairs) {
            if (idx < chairs.size()) {
                Rectangle r = chairs.get(idx);
                g2d.fillOval(r.x + r.width / 2 - 6, r.y + r.height / 2 - 6, 12, 12);
            }
        }
    }

    private void drawHud(Graphics2D g2d) {
        int x = getWidth() - 210, y = 55, lineH = 24;
        int rows = PRICE.size() + 2;
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fill(new RoundRectangle2D.Double(x - 8, y - 4, 206, rows * lineH + 20, 12, 12));

        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.setColor(new Color(255, 220, 80));
        g2d.drawString("📦 Kho hàng", x, y + 14); y += lineH;

        for (String food : PRICE.keySet()) {
            int qty = stock.getOrDefault(food, 0);
            g2d.setColor(qty > 0 ? Color.WHITE : new Color(255, 80, 80));
            g2d.drawString(DISPLAY_NAME.get(food) + ": " + qty + "/" + MAX_STOCK, x, y + 14);

            // Show ordered/served progress
            int need   = customerOrder.getOrDefault(food, 0);
            int served = orderServed.getOrDefault(food, 0);
            if (need > 0 && gameState == GameState.SERVING) {
                g2d.setColor(served >= need ? new Color(80, 220, 80) : new Color(255, 160, 50));
                g2d.drawString(served >= need ? " ✓" : " " + served + "/" + need,
                        x + 130, y + 14);
            }
            y += lineH;
        }

        // State hint
        g2d.setColor(new Color(180, 220, 255));
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        String hint = switch (gameState) {
            case IDLE                -> queueSize > 0 ? "▶ Click khách đầu hàng" : "✅ Hết khách";
            case SERVING             -> "▶ Lấy món cho khách";
            case WAITING_PAYMENT     -> "▶ Đến quầy thanh toán";
            case TRANSFER_VERIFYING  -> "▶ Xác nhận chuyển khoản";
            case CHAIR_SELECTING     -> "▶ Chọn ghế cho khách";
        };
        g2d.drawString(hint, x, y + 14);
    }

    private void drawNotification(Graphics2D g2d) {
        if (notification.isEmpty()) return;
        if (System.currentTimeMillis() - notifTime > 3000) { notification = ""; return; }
        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(notification);
        int nx = (getWidth() - tw - 32) / 2, ny = getHeight() - 80;
        g2d.setColor(new Color(0, 0, 0, 190));
        g2d.fill(new RoundRectangle2D.Double(nx - 4, ny - 4, tw + 40, 34, 12, 12));
        g2d.setColor(Color.WHITE);
        g2d.drawString(notification, nx + 16, ny + 18);
        repaint();
    }

    private enum Direction { LEFT, RIGHT, UP, DOWN }

    // ── Tiled map loader ─────────────────────────────────────────────────────
    static class TiledCFMap {
        final int pixelWidth, pixelHeight;
        private final int tileWidth, tileHeight, columns, tileCount;
        private final BufferedImage tilesetImage;
        private final int[][] layer;
        final Map<String, List<Rectangle>> objectGroups = new LinkedHashMap<>();
        final List<Rectangle> collisionBoxes = new ArrayList<>();

        private TiledCFMap(int w, int h, int tw, int th, int cols, int tc, BufferedImage img, int[][] layer) {
            pixelWidth = w * tw; pixelHeight = h * th;
            tileWidth = tw; tileHeight = th; columns = cols; tileCount = tc;
            tilesetImage = img; this.layer = layer;
        }

        static TiledCFMap load(String mapPath) {
            try {
                DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
                fac.setIgnoringComments(true);
                DocumentBuilder builder = fac.newDocumentBuilder();

                Document mapDoc = parse(builder, mapPath);
                Element  mapEl  = mapDoc.getDocumentElement();
                int w  = parseInt(mapEl.getAttribute("width"),      0);
                int h  = parseInt(mapEl.getAttribute("height"),     0);
                int tw = parseInt(mapEl.getAttribute("tilewidth"),  16);
                int th = parseInt(mapEl.getAttribute("tileheight"), 16);

                Element tilesetEl = (Element) mapEl.getElementsByTagName("tileset").item(0);
                String tsxSrc = tilesetEl.getAttribute("source");
                if (tsxSrc.matches("[A-Za-z]:/.*") || tsxSrc.startsWith("/"))
                    tsxSrc = tsxSrc.substring(tsxSrc.lastIndexOf('/') + 1);
                String tsxPath = AssetLoader.joinPath(AssetLoader.parentPath(mapPath), tsxSrc);
                Document tsxDoc = parse(builder, tsxPath);
                Element  tsxEl  = tsxDoc.getDocumentElement();
                int cols = parseInt(tsxEl.getAttribute("columns"),   1);
                int tc   = parseInt(tsxEl.getAttribute("tilecount"), 0);
                Element  imgEl   = (Element) tsxEl.getElementsByTagName("image").item(0);
                String   imgPath = AssetLoader.joinPath(AssetLoader.parentPath(tsxPath), imgEl.getAttribute("source"));
                BufferedImage tilesetImg = AssetLoader.loadImage(imgPath);
                if (tilesetImg == null) { System.err.println("[CFMap] Missing: " + imgPath); return null; }

                int[][] layerData = new int[h][w];
                NodeList layerNodes = mapEl.getElementsByTagName("layer");
                if (layerNodes.getLength() > 0)
                    layerData = parseLayer((Element) layerNodes.item(0), w, h);

                TiledCFMap map = new TiledCFMap(w, h, tw, th, cols, tc, tilesetImg, layerData);

                NodeList ogNodes = mapEl.getElementsByTagName("objectgroup");
                for (int i = 0; i < ogNodes.getLength(); i++) {
                    Element og   = (Element) ogNodes.item(i);
                    String  name = og.getAttribute("name");
                    List<Rectangle> boxes = new ArrayList<>();
                    NodeList objs = og.getElementsByTagName("object");
                    for (int j = 0; j < objs.getLength(); j++)
                        boxes.add(parseObjBounds((Element) objs.item(j), tw, th));
                    map.objectGroups.put(name, boxes);
                    if ("vatcan".equalsIgnoreCase(name)) map.collisionBoxes.addAll(boxes);
                }

                System.out.println("[CFMap] Loaded " + mapPath + " (" + map.pixelWidth + "x" + map.pixelHeight + ")");
                return map;
            } catch (Exception e) {
                System.err.println("[CFMap] Failed: " + e.getMessage()); return null;
            }
        }

        Rectangle groupBounds(String name) {
            List<Rectangle> list = objectGroups.get(name);
            if (list == null || list.isEmpty()) return null;
            int x1 = Integer.MAX_VALUE, y1 = Integer.MAX_VALUE, x2 = 0, y2 = 0;
            for (Rectangle r : list) { x1=Math.min(x1,r.x); y1=Math.min(y1,r.y); x2=Math.max(x2,r.x+r.width); y2=Math.max(y2,r.y+r.height); }
            return new Rectangle(x1, y1, x2-x1, y2-y1);
        }

        void render(Graphics2D g2d, ImageObserver obs) {
            for (int row = 0; row < layer.length; row++)
                for (int col = 0; col < layer[row].length; col++) {
                    int gid = layer[row][col] & 0x1FFFFFFF;
                    if (gid > 0) drawTile(g2d, gid, col, row, obs);
                }
        }

        private void drawTile(Graphics2D g2d, int gid, int col, int row, ImageObserver obs) {
            int lid = gid - 1;
            if (lid < 0 || (tileCount > 0 && lid >= tileCount)) return;
            int sx = (lid % columns) * tileWidth, sy = (lid / columns) * tileHeight;
            if (sx + tileWidth > tilesetImage.getWidth() || sy + tileHeight > tilesetImage.getHeight()) return;
            int dx = col * tileWidth, dy = row * tileHeight;
            g2d.drawImage(tilesetImage, dx, dy, dx+tileWidth, dy+tileHeight, sx, sy, sx+tileWidth, sy+tileHeight, obs);
        }

        private static Document parse(DocumentBuilder builder, String path) throws Exception {
            try (InputStream s = AssetLoader.openStream(path)) {
                Document doc = builder.parse(s); doc.getDocumentElement().normalize(); return doc;
            }
        }

        private static int[][] parseLayer(Element el, int w, int h) {
            int[][] data = new int[h][w];
            Element de = (Element) el.getElementsByTagName("data").item(0);
            if (de == null || !"csv".equals(de.getAttribute("encoding"))) return data;
            String[] tokens = de.getTextContent().replace("\r","").trim().split(",");
            int count = Math.min(tokens.length, w * h);
            for (int i = 0; i < count; i++) data[i/w][i%w] = parseInt(tokens[i].trim(), 0);
            return data;
        }

        private static Rectangle parseObjBounds(Element obj, int tw, int th) {
            int x = (int) Math.round(parseDouble(obj.getAttribute("x"), 0));
            int y = (int) Math.round(parseDouble(obj.getAttribute("y"), 0));
            int w = (int) Math.round(parseDouble(obj.getAttribute("width"),  0));
            int h = (int) Math.round(parseDouble(obj.getAttribute("height"), 0));
            if (w <= 0) w = tw; if (h <= 0) h = th;
            return new Rectangle(x, y, w, h);
        }

        private static int    parseInt(String v, int fb)    { try { return Integer.parseInt(v.trim()); } catch (Exception e) { return fb; } }
        private static double parseDouble(String v, double fb) { try { return Double.parseDouble(v.trim()); } catch (Exception e) { return fb; } }
    }
}
