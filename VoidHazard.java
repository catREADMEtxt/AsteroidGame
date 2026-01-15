import java.awt.*;
import java.util.Random;

public class VoidHazard {
    public enum Type { ORB, TENDRIL, VORTEX }
    
    private static final Random rand = new Random();
    private final Type type;
    private double x, y;
    private double vx, vy;
    private int lifetime = 300;
    private int size;
    private float pulse = 0;
    private float rotation = 0;
    
    public VoidHazard(int screenWidth, int screenHeight, Type type) {
        this.type = type;
        
        // Spawn from edges
        if (rand.nextBoolean()) {
            x = rand.nextBoolean() ? -50 : screenWidth + 50;
            y = rand.nextInt(screenHeight);
        } else {
            x = rand.nextInt(screenWidth);
            y = rand.nextBoolean() ? -50 : screenHeight + 50;
        }
        
        // Move toward center with variation based on type
        double targetX = screenWidth / 2.0;
        double targetY = screenHeight / 2.0;
        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        double speed = switch(type) {
            case ORB -> 1.5 + rand.nextDouble();
            case TENDRIL -> 0.8 + rand.nextDouble() * 0.5;
            case VORTEX -> 2.0 + rand.nextDouble();
        };
        
        vx = (dx / dist) * speed;
        vy = (dy / dist) * speed;
        
        size = switch(type) {
            case ORB -> 20 + rand.nextInt(20);
            case TENDRIL -> 30 + rand.nextInt(15);
            case VORTEX -> 35 + rand.nextInt(20);
        };
    }
    
    public void update() {
        x += vx;
        y += vy;
        pulse += 0.1f;
        rotation += 0.05f;
        lifetime--;
    }
    
    public boolean isAlive() {
        return lifetime > 0;
    }
    
    public boolean intersects(Polygon shipBounds) {
        int checkRadius = size;
        return shipBounds.contains(x, y) || 
               isNearShip(shipBounds, checkRadius);
    }
    
    private boolean isNearShip(Polygon shipBounds, int radius) {
        for (int i = 0; i < shipBounds.npoints; i++) {
            double dx = shipBounds.xpoints[i] - x;
            double dy = shipBounds.ypoints[i] - y;
            if (Math.sqrt(dx * dx + dy * dy) < radius) {
                return true;
            }
        }
        return false;
    }
    
    public void draw(Graphics2D g2d, boolean voidToggled) {
        switch(type) {
            case ORB -> drawOrb(g2d, voidToggled);
            case TENDRIL -> drawTendril(g2d, voidToggled);
            case VORTEX -> drawVortex(g2d, voidToggled);
        }
    }
    
    private void drawOrb(Graphics2D g2d, boolean voidToggled) {
        int pulseSize = (int)(size + 5 * Math.sin(pulse));
        
        // Outer glow
        int alpha =  voidToggled ? (int)(80 + 40 * Math.sin(pulse * 2)) : 40;
        g2d.setColor(new Color(150, 0, 255, alpha));
        g2d.fillOval((int)x - pulseSize, (int)y - pulseSize, pulseSize * 2, pulseSize * 2);
        
        // Inner core
        g2d.setColor(new Color(200, 50, 255, voidToggled ? 200 : 40));
        g2d.fillOval((int)x - size/2, (int)y - size/2, size, size);
        
        // Tendrils
        for (int i = 0; i < 6; i++) {
            double angle = (2 * Math.PI * i / 6) + pulse;
            int len = (int)(size + 10 * Math.sin(pulse + i));
            int ex = (int)(x + len * Math.cos(angle));
            int ey = (int)(y + len * Math.sin(angle));
            
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(new Color(180, 0, 255, voidToggled ? 150 : 40));
            g2d.drawLine((int)x, (int)y, ex, ey);
        }
        g2d.setStroke(new BasicStroke(1));
    }
    
    private void drawTendril(Graphics2D g2d, boolean voidToggled) {
        // Serpentine body
        int segments = 8;
        for (int i = 0; i < segments; i++) {
            double segmentAngle = rotation + (i * 0.3);
            double offsetX = 15 * Math.sin(segmentAngle);
            double offsetY = 10 * Math.cos(segmentAngle);
            
            int segX = (int)(x + offsetX - i * 5);
            int segY = (int)(y + offsetY - i * 5);
            
            int segSize = size - i * 3;
            int alpha = voidToggled ? 200 - i * 20 : 40;
            
            g2d.setColor(new Color(200, 0, 255, alpha));
            g2d.fillOval(segX - segSize/2, segY - segSize/2, segSize, segSize);
            
            // Connecting lines
            if (i > 0) {
                double prevAngle = rotation + ((i-1) * 0.3);
                double prevOffsetX = 15 * Math.sin(prevAngle);
                double prevOffsetY = 10 * Math.cos(prevAngle);
                int prevX = (int)(x + prevOffsetX - (i-1) * 5);
                int prevY = (int)(y + prevOffsetY - (i-1) * 5);
                
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(new Color(180, 0, 255, alpha));
                g2d.drawLine(prevX, prevY, segX, segY);
            }
        }
        
        // Head glow
        int headAlpha = voidToggled ? (int)(150 + 50 * Math.sin(pulse * 3)) : 40;
        g2d.setColor(new Color(255, 0, 200, headAlpha));
        g2d.fillOval((int)x - size, (int)y - size, size * 2, size * 2);
        
        g2d.setStroke(new BasicStroke(1));
    }
    
    private void drawVortex(Graphics2D g2d, boolean voidToggled) {
        // Spinning spiral
        int arms = 3;
        for (int arm = 0; arm < arms; arm++) {
            for (int i = 0; i < 20; i++) {
                double angle = rotation + (arm * 2 * Math.PI / arms) + (i * 0.3);
                double radius = i * 3;
                int px = (int)(x + radius * Math.cos(angle));
                int py = (int)(y + radius * Math.sin(angle));
                
                int particleSize = (int)(8 - i * 0.3);
                int alpha = voidToggled ? 255 - i * 10 : 40;
                
                g2d.setColor(new Color(150, 0, 255, alpha));
                g2d.fillOval(px - particleSize/2, py - particleSize/2, particleSize, particleSize);
            }
        }
        
        // Central eye
        int eyeAlpha = voidToggled ? (int)(200 + 55 * Math.sin(pulse * 4)) : 40;
        g2d.setColor(new Color(200, 0, 255, eyeAlpha));
        g2d.fillOval((int)x - size/3, (int)y - size/3, size*2/3, size*2/3);
        
        // Outer ring
        g2d.setColor(new Color(180, 0, 255, voidToggled ? 120 : 40));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval((int)x - size, (int)y - size, size * 2, size * 2);
        g2d.setStroke(new BasicStroke(1));
    }
    
    public Type getType() { return type; }
    public double getCenterX() { return x; }
    public double getCenterY() { return y; }
}