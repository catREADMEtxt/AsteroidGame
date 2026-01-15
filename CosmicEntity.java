import java.awt.*;
import java.util.Random;

public class CosmicEntity {
    private static final Random rand = new Random();
    private double x, y;
    private double targetX, targetY;
    private double vx, vy;
    private final double speed = 2.5;
    private int size = 100;
    private float pulse = 0;
    private float tentaclePhase = 0;
    private int screenWidth, screenHeight;
    
    public CosmicEntity(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Spawn from random edge
        if (rand.nextBoolean()) {
            x = rand.nextBoolean() ? -100 : screenWidth + 100;
            y = rand.nextInt(screenHeight);
        } else {
            x = rand.nextInt(screenWidth);
            y = rand.nextBoolean() ? -100 : screenHeight + 100;
        }
        
        updateTarget(screenWidth / 2.0, screenHeight / 2.0);
    }
    
    public void updateTarget(double newTargetX, double newTargetY) {
        targetX = newTargetX;
        targetY = newTargetY;
        
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            vx = (dx / distance) * speed;
            vy = (dy / distance) * speed;
        }
    }
    
    public void update(double shipX, double shipY) {
        // Chase the ship
        updateTarget(shipX, shipY);
        
        x += vx;
        y += vy;
        pulse += 0.1f;
        tentaclePhase += 0.05f;
    }
    
    public boolean isNearShip(Polygon shipBounds) {
        for (int i = 0; i < shipBounds.npoints; i++) {
            double dx = shipBounds.xpoints[i] - x;
            double dy = shipBounds.ypoints[i] - y;
            if (Math.sqrt(dx * dx + dy * dy) < size) {
                return true;
            }
        }
        return false;
    }
    
    public void draw(Graphics2D g2d) {
        // Main body (eldritch sphere)
        int bodySize = (int)(size + 10 * Math.sin(pulse));
        
        // Outer aura
        for (int i = 3; i > 0; i--) {
            int auraSize = bodySize + i * 20;
            int alpha = 40 - i * 10;
            g2d.setColor(new Color(55, 0, 155, alpha));
            g2d.fillOval((int)x - auraSize/2, (int)y - auraSize/2, auraSize, auraSize);
        }
        
        // Multiple eyes
        for (int i = 0; i < 5; i++) {
            double eyeAngle = (i / 5.0 * Math.PI * 2) + pulse;
            int eyeRadius = 30;
            int eyeX = (int)(x + eyeRadius * Math.cos(eyeAngle));
            int eyeY = (int)(y + eyeRadius * Math.sin(eyeAngle));
            
            // Eye white
            g2d.setColor(new Color(255, 200, 200));
            g2d.fillOval(eyeX - 10, eyeY - 10, 20, 20);
            
            // Pupil (looking at center)
            g2d.setColor(new Color(150, 0, 0));
            g2d.fillOval(eyeX - 5, eyeY - 5, 10, 10);
            
            // Glint
            g2d.setColor(Color.WHITE);
            g2d.fillOval(eyeX - 2, eyeY - 2, 4, 4);
        }
        
        // Writhing tentacles
        int tentacleCount = 8;
        for (int i = 0; i < tentacleCount; i++) {
            drawTentacle(g2d, i, tentacleCount);
        }
        
        // Core body
        GradientPaint bodyGradient = new GradientPaint(
            (int)x - bodySize/2, (int)y - bodySize/2, new Color(150, 0, 100),
            (int)x + bodySize/2, (int)y + bodySize/2, new Color(80, 0, 50)
        );
        g2d.setPaint(bodyGradient);
        g2d.fillOval((int)x - bodySize/2, (int)y - bodySize/2, bodySize, bodySize);
        
        // Pulsing core
        int coreSize = (int)(30 + 10 * Math.sin(pulse * 2));
        int coreAlpha = (int)(200 + 55 * Math.sin(pulse * 3));
        g2d.setColor(new Color(100, 50, 200, coreAlpha));
        g2d.fillOval((int)x - coreSize/2, (int)y - coreSize/2, coreSize, coreSize);
    }
    
    private void drawTentacle(Graphics2D g2d, int index, int total) {
        double baseAngle = (index / (double)total) * Math.PI * 2;
        int segments = 8;
        
        g2d.setStroke(new BasicStroke(6));
        
        for (int seg = 0; seg < segments; seg++) {
            double segmentPhase = tentaclePhase + (index * 0.5) + (seg * 0.2);
            double angle = baseAngle + Math.sin(segmentPhase) * 0.5;
            double length = 15 + seg * 8;
            
            int x1 = seg == 0 ? (int)x : 
                (int)(x + (length - 8) * Math.cos(baseAngle + Math.sin(segmentPhase - 0.2) * 0.5));
            int y1 = seg == 0 ? (int)y :
                (int)(y + (length - 8) * Math.sin(baseAngle + Math.sin(segmentPhase - 0.2) * 0.5));
            
            int x2 = (int)(x + length * Math.cos(angle));
            int y2 = (int)(y + length * Math.sin(angle));
            
            int alpha = 200 - seg * 20;
            g2d.setColor(new Color(100, 0, 80, alpha));
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        g2d.setStroke(new BasicStroke(1));
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
}