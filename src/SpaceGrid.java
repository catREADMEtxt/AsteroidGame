import java.awt.*;
import java.util.List;

public class SpaceGrid {
    private static final int GRID_SIZE = 50;
    
    public void draw(Graphics2D g2d, int width, int height, List<Asteroid> asteroids, List<BlackHole> blackHoles) {
        g2d.setColor(new Color(100, 100, 150, 30));
        g2d.setStroke(new BasicStroke(1));
        
        // Vertical lines
        for (int x = 0; x < width; x += GRID_SIZE) {
            drawCurvedVerticalLine(g2d, x, height, asteroids, blackHoles);
        }
        
        // Horizontal lines
        for (int y = 0; y < height; y += GRID_SIZE) {
            drawCurvedHorizontalLine(g2d, y, width, asteroids, blackHoles);
        }
        
        g2d.setStroke(new BasicStroke(1));
    }
    
    private void drawCurvedVerticalLine(Graphics2D g2d, int baseX, int height, List<Asteroid> asteroids, List<BlackHole> blackHoles) {
        int segments = height / 5;
        int[] xPoints = new int[segments];
        int[] yPoints = new int[segments];
        
        for (int i = 0; i < segments; i++) {
            int y = i * 5;
            double displacement = 0;
            
            // Asteroid gravity (subtle)
            for (Asteroid a : asteroids) {
                Rectangle bounds = a.getBounds().getBounds();
                double dx = bounds.getCenterX() - baseX;
                double dy = bounds.getCenterY() - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist < 150) {
                    double strength = (150 - dist) / 150.0;
                    displacement += dx * strength * 0.3;
                }
            }
            
            // Black hole gravity (strong)
            for (BlackHole bh : blackHoles) {
                double dx = bh.getX() - baseX;
                double dy = bh.getY() - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist < 300) {
                    double strength = (300 - dist) / 300.0;
                    displacement += dx * strength * 2.0;
                }
            }
            
            xPoints[i] = baseX + (int)displacement;
            yPoints[i] = y;
        }
        
        g2d.drawPolyline(xPoints, yPoints, segments);
    }
    
    private void drawCurvedHorizontalLine(Graphics2D g2d, int baseY, int width, List<Asteroid> asteroids, List<BlackHole> blackHoles) {
        int segments = width / 5;
        int[] xPoints = new int[segments];
        int[] yPoints = new int[segments];
        
        for (int i = 0; i < segments; i++) {
            int x = i * 5;
            double displacement = 0;
            
            // Asteroid gravity (subtle)
            for (Asteroid a : asteroids) {
                Rectangle bounds = a.getBounds().getBounds();
                double dx = bounds.getCenterX() - x;
                double dy = bounds.getCenterY() - baseY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist < 150) {
                    double strength = (150 - dist) / 150.0;
                    displacement += dy * strength * 0.3;
                }
            }
            
            // Black hole gravity (strong)
            for (BlackHole bh : blackHoles) {
                double dx = bh.getX() - x;
                double dy = bh.getY() - baseY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist < 300) {
                    double strength = (300 - dist) / 300.0;
                    displacement += dy * strength * 2.0;
                }
            }
            
            xPoints[i] = x;
            yPoints[i] = baseY + (int)displacement;
        }
        
        g2d.drawPolyline(xPoints, yPoints, segments);
    }
}