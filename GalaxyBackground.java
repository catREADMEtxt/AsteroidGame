import java.awt.*;
import java.util.Random;

public class GalaxyBackground {
    private float colorShift = 0;
    private final Random rand = new Random();
    
    public void update() {
        colorShift += 0.001f; // Slow color transition
        if (colorShift > 1.0f) colorShift = 0;
    }
    
    public void draw(Graphics2D g2d, int width, int height) {
        // Calculate current color tone
        float hue1 = (colorShift) % 1.0f;
        float hue2 = (colorShift + 0.3f) % 1.0f;
        
        Color color1 = Color.getHSBColor(hue1, 0.6f, 0.2f);
        Color color2 = Color.getHSBColor(hue2, 0.5f, 0.3f);
        
        // Gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, color1,
            0, height, color2
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Nebula clouds
        for (int i = 0; i < 5; i++) {
            int x = (int)(width * (0.2 + i * 0.15));
            int y = height / 2 + rand.nextInt(200) - 100;
            int size = 300 + rand.nextInt(200);
            
            float nebulaHue = (colorShift + i * 0.1f) % 1.0f;
            Color nebulaColor = Color.getHSBColor(nebulaHue, 0.7f, 0.3f);
            
            RadialGradientPaint nebula = new RadialGradientPaint(
                x, y, size,
                new float[]{0f, 0.5f, 1f},
                new Color[]{
                    new Color(nebulaColor.getRed(), nebulaColor.getGreen(), nebulaColor.getBlue(), 80),
                    new Color(nebulaColor.getRed(), nebulaColor.getGreen(), nebulaColor.getBlue(), 30),
                    new Color(0, 0, 0, 0)
                }
            );
            g2d.setPaint(nebula);
            g2d.fillOval(x - size, y - size, size * 2, size * 2);
        }
    }
}