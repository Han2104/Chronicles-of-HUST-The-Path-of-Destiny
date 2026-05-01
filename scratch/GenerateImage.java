import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GenerateImage {
    public static void main(String[] args) throws Exception {
        BufferedImage img = new BufferedImage(1000, 650, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        
        // Vẽ nền gạch đỏ (C2 HUST style)
        g2d.setColor(new Color(178, 34, 34)); 
        g2d.fillRect(0, 0, 1000, 650);
        
        // Vẽ sân trường
        g2d.setColor(new Color(211, 211, 211));
        g2d.fillRect(100, 100, 800, 450);
        
        // Vẽ chữ
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString("KÝ TÚC XÁ C2 - HUST", 300, 300);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.drawString("(Bản đồ placeholder)", 420, 350);
        
        g2d.dispose();
        ImageIO.write(img, "png", new File("assets/c2_map.png"));
        System.out.println("✅ Đã tạo assets/c2_map.png thành công!");
    }
}
