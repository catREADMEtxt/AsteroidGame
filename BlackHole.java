import java.awt.*;
import java.awt.geom.Point2D;

public class BlackHole {
    private double x, y;
    private int size = 80;
    private int spawnTimer = 1 * 60;
    private float rotation = 0;
    private float pulse = 0;
    private final double pullStrength = 0.15;
    private final double pullRadius = 300;
    private final double eventHorizon = 40;
    private boolean fullySpawned = false;
    
    public BlackHole(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void update() {
        if (spawnTimer > 0) {
            spawnTimer--;
            if (spawnTimer == 0) fullySpawned = true;
        }
        rotation += 0.08f;
        pulse += 0.05f;
    }
    
    public Point2D.Double applyGravity(double shipX, double shipY) {
        if (!fullySpawned) return new Point2D.Double(0, 0);
        double dx = x - shipX;
        double dy = y - shipY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < pullRadius && distance > eventHorizon) {
            // Inverse square gravity
            double strength = pullStrength * (pullRadius - distance) / pullRadius;
            double forceX = (dx / distance) * strength;
            double forceY = (dy / distance) * strength;
            return new Point2D.Double(forceX, forceY);
        }
        
        return new Point2D.Double(0, 0);
    }
    
    public boolean isShipConsumed(double shipX, double shipY) {
        if (!fullySpawned) return false;
        double dx = x - shipX;
        double dy = y - shipY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < eventHorizon;
    }
    
    public void draw(Graphics2D g2d) {
        // Spawn Animation
        float spawnScale = fullySpawned ? 1.0f : (60 - spawnTimer) / 60.0f;
        // Scale effect
        if (!fullySpawned) {
            int alpha = (int)(255 * spawnScale);
            g2d.setColor(new Color(150, 100, 255, alpha));
            int spawnSize = (int)(size * 2 * spawnScale);
            g2d.fillOval((int)x - spawnSize, (int)y - spawnSize, spawnSize * 2, spawnSize * 2);
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, spawnScale));
        // Gravitational lensing effect (outer rings)
        for (int i = 5; i > 0; i--) {
            int radius = size + i * 30;
            int alpha = 30 - i * 5;
            g2d.setColor(new Color(100, 50, 200, alpha));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval((int)x - radius, (int)y - radius, radius * 2, radius * 2);
        }
        
        // Accretion disk (spinning)
        for (int layer = 0; layer < 3; layer++) {
            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0 * Math.PI * 2) + rotation + (layer * 0.3);
                int diskRadius = size + 20 + layer * 15;
                int px = (int)(x + diskRadius * Math.cos(angle));
                int py = (int)(y + diskRadius * Math.sin(angle) * 0.3); // Flattened
                
                int alpha = 150 - layer * 40;
                int particleSize = 4 - layer;
                
                // Color shifts from orange to blue
                Color diskColor = layer == 0 ? 
                    new Color(255, 150, 0, alpha) :
                    layer == 1 ?
                    new Color(200, 100, 255, alpha) :
                    new Color(100, 150, 255, alpha);
                
                g2d.setColor(diskColor);
                g2d.fillOval(px - particleSize/2, py - particleSize/2, particleSize, particleSize);
            }
        }
        
        // Event horizon
        int horizonAlpha = (int)(200 + 55 * Math.sin(pulse));
        g2d.setColor(new Color(0, 0, 0, horizonAlpha));
        g2d.fillOval((int)x - size/2, (int)y - size/2, size, size);
        
        // Hawking radiation (particles escaping)
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0 * Math.PI * 2) + pulse * 2;
            int escapeRadius = (int)(size/2 + 20 + 10 * Math.sin(pulse + i));
            int px = (int)(x + escapeRadius * Math.cos(angle));
            int py = (int)(y + escapeRadius * Math.sin(angle));
            
            g2d.setColor(new Color(150, 200, 255, 180));
            g2d.fillOval(px - 2, py - 2, 4, 4);
        }
        
        // Warning indicator
        g2d.setFont(new Font("Serif", Font.BOLD, 12));
        g2d.setColor(new Color(255, 100, 100));
        String warning = "⚠ BLACK HOLE";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(warning, (int)x - fm.stringWidth(warning)/2, (int)y - size);
        
        g2d.setStroke(new BasicStroke(1));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
}