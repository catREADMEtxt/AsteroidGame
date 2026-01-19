import java.awt.*;
import java.util.Random;

public class VoidCreature {
    private static final Random rand = new Random();
    
    public enum CreatureType { SERPENT, SPECTER, LEVIATHAN }
    
    private CreatureType type;
    private double x, y;
    private double vx, vy;
    private int size;
    private double speed;
    private int maxHP;
    private int currentHP;
    private float animPhase = 0;
    private float limbPhase = 0;
    private Color bodyColor;
    private boolean isInVoid;
    
    public VoidCreature(int screenWidth, int screenHeight, int level) {
        // Spawn from edges
        if (rand.nextBoolean()) {
            x = rand.nextBoolean() ? -100 : screenWidth + 100;
            y = rand.nextInt(screenHeight);
        } else {
            x = rand.nextInt(screenWidth);
            y = rand.nextBoolean() ? -100 : screenHeight + 100;
        }
        
        // Determine type
        type = CreatureType.values()[rand.nextInt(CreatureType.values().length)];
        
        // Size varies (30-100), larger = slower, smaller = faster
        size = 30 + rand.nextInt(71);
        
        // Speed inversely proportional to size (0.5-3.0)
        speed = 3.5 - (size / 100.0 * 3.0);
        speed = Math.max(0.5, Math.min(3.0, speed));
        
        // HP based on size (small: 50-100, large: 150-300)
        maxHP = (int)(50 + (size / 100.0) * 250);
        currentHP = maxHP;
        
        // Color based on size (blue for large, red for small)
        float sizeRatio = (size - 30) / 70.0f;
        int red = (int)(255 * (1 - sizeRatio)) + 50;
        int blue = (int)(255 * sizeRatio) + 100;
        red = Math.max(0, Math.min(255, red));
        blue = Math.max(0, Math.min(255, blue));
        bodyColor = new Color(red, 0, blue);
        
        updateVelocity(screenWidth / 2.0, screenHeight / 2.0);
    }
    
    public void updateVelocity(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist > 0) {
            vx = (dx / dist) * speed;
            vy = (dy / dist) * speed;
        }
    }
    
    public void update(double shipX, double shipY) {
        updateVelocity(shipX, shipY);
        x += vx;
        y += vy;
        animPhase += 0.1f;
        limbPhase += 0.15f;
    }
    
    public void updateWithBoids(double shipX, double shipY, java.util.List<VoidCreature> neighbors) {
        // Tunables
        final double NEIGHBOR_R = 150.0;
        final double SEP_R = 50.0;
        final double NEIGHBOR_R2 = NEIGHBOR_R * NEIGHBOR_R;
        final double SEP_R2 = SEP_R * SEP_R;

        double separationX = 0, separationY = 0;
        double alignmentX = 0, alignmentY = 0;
        double cohesionX = 0, cohesionY = 0;
        int count = 0;

        // Optional: cap checks to reduce worst-case time
        final int MAX_CHECKS = 40;           // try 24–60
        int checked = 0;

        // Optional: stride sampling to avoid checking every neighbor every time
        // If neighbors is dense, this is a big win.
        int step = 1;
        int n = neighbors.size();
        if (n > 120) step = 2;
        if (n > 240) step = 3;

        for (int i = 0; i < n; i += step) {
            VoidCreature other = neighbors.get(i);
            if (other == this) continue;

            double dx = other.x - x;
            double dy = other.y - y;
            double dist2 = dx * dx + dy * dy;

            if (dist2 < NEIGHBOR_R2) {
                // Separation only if close; needs invDist
                if (dist2 < SEP_R2 && dist2 > 1e-9) {
                    double invDist = 1.0 / Math.sqrt(dist2);
                    separationX -= dx * invDist;
                    separationY -= dy * invDist;
                }

                alignmentX += other.vx;
                alignmentY += other.vy;

                cohesionX += other.x;
                cohesionY += other.y;

                count++;

                if (++checked >= MAX_CHECKS) break;
            }
        }

        if (count > 0) {
            double invCount = 1.0 / count;
            alignmentX *= invCount;
            alignmentY *= invCount;
            cohesionX = (cohesionX * invCount) - x;
            cohesionY = (cohesionY * invCount) - y;
        }

        // Prey on player (avoid sqrt if you can tolerate approximate normalization)
        double preyX = shipX - x;
        double preyY = shipY - y;
        double preyDist2 = preyX * preyX + preyY * preyY;
        if (preyDist2 > 1e-9) {
            double invPreyDist = 1.0 / Math.sqrt(preyDist2);
            preyX *= invPreyDist;
            preyY *= invPreyDist;
        }

        // Combine behaviors
        double ax = (separationX * 1.5 + alignmentX * 1.0 + cohesionX * 0.01 + preyX * 2.0);
        double ay = (separationY * 1.5 + alignmentY * 1.0 + cohesionY * 0.01 + preyY * 2.0);

        // Scale into desired speed band (keeps motion stable)
        vx = ax * speed / 4.0;
        vy = ay * speed / 4.0;

        // Limit speed using squared magnitude (one sqrt only if needed)
        double v2 = vx * vx + vy * vy;
        double s2 = speed * speed;
        if (v2 > s2 && v2 > 1e-12) {
            double invV = 1.0 / Math.sqrt(v2);
            vx *= speed * invV;
            vy *= speed * invV;
        }

        x += vx;
        y += vy;
        animPhase += 0.1f;
        limbPhase += 0.15f;
    }
    
    public void takeDamage(int damage) {
        currentHP -= damage;
        if (currentHP < 0) currentHP = 0;
    }
    
    public boolean isDead() {
        return currentHP <= 0;
    }
    
    public boolean intersects(Polygon shipBounds) {
        if (!isInVoid) return false;
        
        // Different collision radii based on type
        int collisionRadius = switch(type) {
            case SERPENT -> (int)(size * 1.2); // Longer body
            case SPECTER -> (int)(size * 0.7); // Just the bell/core
            case LEVIATHAN -> (int)(size * 1.5); // Massive body
        };
        
        for (int i = 0; i < shipBounds.npoints; i++) {
            double dx = shipBounds.xpoints[i] - x;
            double dy = shipBounds.ypoints[i] - y;
            if (Math.sqrt(dx * dx + dy * dy) < collisionRadius) {
                return true;
            }
        }
        return false;
    }
    
    public void draw(Graphics2D g2d, boolean voidMode) {
        isInVoid = voidMode;
        
        if (!voidMode) {
            // Background mode - ghostly, no threat
            drawGhostly(g2d);
        } else {
            // Active mode - threatening
            drawActive(g2d);
        }
    }
    
    private void drawGhostly(Graphics2D g2d) {
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        
        switch (type) {
            case SERPENT -> drawSerpent(g2d, 40);
            case SPECTER -> drawSpecter(g2d, 40);
            case LEVIATHAN -> drawLeviathan(g2d, 40);
        }
        
        g2d.setComposite(old);
    }
    
    private void drawActive(Graphics2D g2d) {
        switch (type) {
            case SERPENT -> drawSerpent(g2d, 255);
            case SPECTER -> drawSpecter(g2d, 255);
            case LEVIATHAN -> drawLeviathan(g2d, 255);
        }
        
        // HP bar
        drawHPBar(g2d);
    }

    private void drawSerpent(Graphics2D g2d, int baseAlpha) {
        // Serpentine eel-like creature - dark purple
        int segments = 6; // Reduced from 12
        double bodyLength = size * 2.0;
        
        for (int i = 0; i < segments; i++) {
            double segPhase = animPhase + i * 0.5;
            double t = i / (double)segments;
            
            // Sine wave swimming motion
            double offsetX = size * 0.3 * Math.sin(segPhase);
            double offsetY = size * 0.2 * Math.cos(segPhase * 0.8);
            
            int segX = (int)(x + offsetX - t * bodyLength);
            int segY = (int)(y + offsetY);
            
            // Tapered body
            double taper = Math.sin(t * Math.PI);
            int segSize = (int)(size * taper);
            if (segSize < 3) continue;
            
            int alpha = Math.max(0, Math.min(255, baseAlpha - i * (baseAlpha / 8)));
            
            // Dark purple gradient for serpent
            int red = Math.max(0, Math.min(255, (int)(80 + 50 * t)));
            int green = Math.max(0, Math.min(255, (int)(20 + 30 * (1-t))));
            int blue = Math.max(0, Math.min(255, (int)(120 + 60 * taper)));
            
            // Body segment
            g2d.setColor(new Color(red, green, blue, alpha));
            g2d.fillOval(segX - segSize/2, segY - segSize/2, segSize, segSize);
            
            // Simple fin every 2 segments
            if (i % 2 == 0 && i > 0 && i < segments - 1) {
                int finHeight = (int)(size * 0.4);
                int[] finX = {segX, segX - 6, segX + 6};
                int[] finY = {segY - segSize/2 - finHeight, segY - segSize/2, segY - segSize/2};
                
                int finAlpha = Math.max(0, Math.min(255, alpha / 2));
                g2d.setColor(new Color(red, green, blue, finAlpha));
                g2d.fillPolygon(finX, finY, 3);
            }
        }
        
        // Head with glowing eyes
        int headSize = (int)(size * 0.9);
        int headRed = Math.max(0, Math.min(255, 100));
        int headGreen = Math.max(0, Math.min(255, 30));
        int headBlue = Math.max(0, Math.min(255, 150));
        
        g2d.setColor(new Color(headRed, headGreen, headBlue, baseAlpha));
        g2d.fillOval((int)x - headSize/2, (int)y - headSize/2, headSize, headSize);
        
        // Glowing eyes
        int eyeOffset = size / 4;
        int eyeAlpha = Math.max(0, Math.min(255, (int)(baseAlpha * 0.9)));
        g2d.setColor(new Color(255, 100, 255, eyeAlpha));
        g2d.fillOval((int)x - eyeOffset - 3, (int)y - eyeOffset - 3, 6, 6);
        g2d.fillOval((int)x + eyeOffset - 3, (int)y - eyeOffset - 3, 6, 6);
    }

    private void drawSpecter(Graphics2D g2d, int baseAlpha) {
        // Jellyfish/ghost hybrid - magenta/pink
        int bodySize = (int)(size * 1.2);
        
        // Pulsing bell/dome
        double pulseAmount = 0.15 * Math.sin(animPhase * 2);
        int bellHeight = (int)(bodySize * (0.6 + pulseAmount));
        
        // Bell gradient - magenta to pink
        int bellRed = Math.max(0, Math.min(255, 200));
        int bellGreen = Math.max(0, Math.min(255, 50));
        int bellBlue = Math.max(0, Math.min(255, 180));
        int bellAlpha = Math.max(0, Math.min(255, baseAlpha / 3));
        
        g2d.setColor(new Color(bellRed, bellGreen, bellBlue, bellAlpha));
        g2d.fillArc((int)x - bodySize/2, (int)y - bellHeight/2, bodySize, bellHeight, 0, 180);
        
        // Bioluminescent spots
        for (int i = 0; i < 4; i++) { // Reduced from 6
            double angle = (2 * Math.PI * i / 4) + animPhase;
            int spotX = (int)(x + (bodySize/3) * Math.cos(angle));
            int spotY = (int)(y - bellHeight/4 + (bodySize/4) * Math.sin(angle));
            int spotAlpha = Math.max(0, Math.min(255, (int)(baseAlpha * (0.7 + 0.3 * Math.sin(animPhase * 2 + i)))));
            
            int spotRed = Math.max(0, Math.min(255, 255));
            int spotGreen = Math.max(0, Math.min(255, 100));
            int spotBlue = Math.max(0, Math.min(255, 230));
            
            g2d.setColor(new Color(spotRed, spotGreen, spotBlue, spotAlpha));
            g2d.fillOval(spotX - 4, spotY - 4, 8, 8);
        }
        
        // Flowing tentacles - reduced complexity
        int tentacles = 6; // Reduced from 8
        for (int i = 0; i < tentacles; i++) {
            double baseAngle = (2 * Math.PI * i / tentacles);
            
            // Only 3 segments instead of 6
            for (int seg = 0; seg < 3; seg++) {
                double segPhase = limbPhase + i * 0.4 + seg * 0.3;
                double sway = Math.sin(segPhase) * 0.4;
                double angle = baseAngle + sway;
                
                double baseRadius = bodySize * 0.4;
                double length = baseRadius + seg * (size / 5.0);
                
                int tx = (int)(x + length * Math.cos(angle));
                int ty = (int)(y + bellHeight/2 + length * Math.sin(angle) * 0.3);
                
                int segSize = Math.max(2, size / (6 + seg * 2));
                int alpha = Math.max(0, Math.min(255, baseAlpha - seg * (baseAlpha / 5)));
                
                int tentRed = Math.max(0, Math.min(255, 180));
                int tentGreen = Math.max(0, Math.min(255, 40));
                int tentBlue = Math.max(0, Math.min(255, 160));
                
                g2d.setColor(new Color(tentRed, tentGreen, tentBlue, alpha));
                g2d.fillOval(tx - segSize/2, ty - segSize/2, segSize, segSize);
            }
        }
        
        // Pulsing core
        int coreSize = (int)(size * 0.5 + size * 0.15 * Math.sin(animPhase * 3));
        int coreRed = Math.max(0, Math.min(255, 255));
        int coreGreen = Math.max(0, Math.min(255, 150));
        int coreBlue = Math.max(0, Math.min(255, 255));
        int coreAlpha = Math.max(0, Math.min(255, baseAlpha));
        
        g2d.setColor(new Color(coreRed, coreGreen, coreBlue, coreAlpha));
        g2d.fillOval((int)x - coreSize/2, (int)y - coreSize/2, coreSize, coreSize);
    }

    private void drawLeviathan(Graphics2D g2d, int baseAlpha) {
        // Massive dragon/manta ray hybrid - deep purple
        double neckCurve = Math.sin(animPhase) * 0.3;
        
        // Main body - deep purple
        int bodyWidth = size * 2;
        int bodyHeight = (int)(size * 1.2);
        int bodyRed = Math.max(0, Math.min(255, 60));
        int bodyGreen = Math.max(0, Math.min(255, 10));
        int bodyBlue = Math.max(0, Math.min(255, 100));
        int bodyAlpha = Math.max(0, Math.min(255, baseAlpha));
        
        g2d.setColor(new Color(bodyRed, bodyGreen, bodyBlue, bodyAlpha));
        g2d.fillOval((int)x - bodyWidth/2, (int)y - bodyHeight/2, bodyWidth, bodyHeight);
        
        // Simplified scales - 3x6 grid instead of 5x8
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 6; j++) {
                int scaleX = (int)(x - bodyWidth/2 + j * (bodyWidth/5.0));
                int scaleY = (int)(y - bodyHeight/2 + i * (bodyHeight/2.0));
                int scaleAlpha = Math.max(0, Math.min(255, baseAlpha / 2));
                
                int scaleRed = Math.max(0, Math.min(255, 90));
                int scaleGreen = Math.max(0, Math.min(255, 30));
                int scaleBlue = Math.max(0, Math.min(255, 130));
                
                g2d.setColor(new Color(scaleRed, scaleGreen, scaleBlue, scaleAlpha));
                g2d.fillOval(scaleX - 4, scaleY - 4, 8, 8);
            }
        }
        
        // Simplified neck - 3 segments instead of 5
        for (int i = 0; i < 3; i++) {
            double t = i / 2.0;
            int neckX = (int)(x + bodyWidth/2 + t * size * 0.6);
            int neckY = (int)(y - bodyHeight/4 + neckCurve * size * t);
            int neckSize = (int)(size * (0.8 - t * 0.3));
            
            g2d.setColor(new Color(bodyRed, bodyGreen, bodyBlue, bodyAlpha));
            g2d.fillOval(neckX - neckSize/2, neckY - neckSize/2, neckSize, neckSize);
        }
        
        // Head
        int headX = (int)(x + bodyWidth/2 + size * 0.6);
        int headY = (int)(y - bodyHeight/4 + neckCurve * size);
        int headSize = (int)(size * 0.8);
        
        int headRed = Math.max(0, Math.min(255, 80));
        int headGreen = Math.max(0, Math.min(255, 20));
        int headBlue = Math.max(0, Math.min(255, 120));
        int headAlpha = Math.max(0, Math.min(255, baseAlpha));
        
        g2d.setColor(new Color(headRed, headGreen, headBlue, headAlpha));
        g2d.fillOval(headX - headSize/2, headY - headSize/2, headSize, headSize);
        
        // Horns
        int[] hornX1 = {headX - headSize/4, headX - headSize/3, headX - headSize/4};
        int[] hornY1 = {headY - headSize/2, headY - headSize, headY - headSize/3};
        int[] hornX2 = {headX + headSize/4, headX + headSize/3, headX + headSize/4};
        int[] hornY2 = {headY - headSize/2, headY - headSize, headY - headSize/3};
        
        g2d.setColor(new Color(bodyRed, bodyGreen, bodyBlue, bodyAlpha));
        g2d.fillPolygon(hornX1, hornY1, 3);
        g2d.fillPolygon(hornX2, hornY2, 3);
        
        // Glowing eye
        int eyeAlpha = Math.max(0, Math.min(255, baseAlpha));
        g2d.setColor(new Color(255, 100, 255, eyeAlpha));
        g2d.fillOval(headX + headSize/6, headY - headSize/6, headSize/4, headSize/4);
        
        // Simplified tail - 3 segments instead of 4
        int tailX = (int)(x - bodyWidth/2);
        double tailSway = Math.sin(animPhase * 1.5) * size * 0.5;
        
        for (int i = 0; i < 3; i++) {
            double t = i / 2.0;
            int segX = (int)(tailX - i * (size/3.0));
            int segY = (int)(y + tailSway * t);
            int segSize = (int)(size * (0.8 - t * 0.5));
            
            g2d.setColor(new Color(bodyRed, bodyGreen, bodyBlue, bodyAlpha));
            g2d.fillOval(segX - segSize/2, segY - segSize/2, segSize, segSize);
            
            // Spine on tail
            if (i > 0) {
                int spineHeight = size/4;
                int[] spineX = {segX, segX - 4, segX + 4};
                int[] spineY = {segY - segSize/2 - spineHeight, segY - segSize/2, segY - segSize/2};
                g2d.fillPolygon(spineX, spineY, 3);
            }
        }
        
        // Simplified fins - 4 instead of 6
        for (int i = 0; i < 4; i++) {
            double finPhase = limbPhase + i * Math.PI / 2;
            int finY = (int)(y + (i < 2 ? -size/2 : size/2));
            int finLength = (int)(size * 0.5 + size * 0.2 * Math.sin(finPhase));
            
            int finX = (int)(x - bodyWidth/3 + i * (bodyWidth/3.0));
            int finEndX = finX + (i % 2 == 0 ? -finLength : finLength);
            int finEndY = (int)(finY + finLength * 0.3 * Math.sin(finPhase));
            
            int[] finXPoints = {finX, finEndX, finX};
            int[] finYPoints = {finY - size/12, finEndY, finY + size/12};
            
            int finAlpha = Math.max(0, Math.min(255, baseAlpha / 2));
            g2d.setColor(new Color(bodyRed, bodyGreen, bodyBlue, finAlpha));
            g2d.fillPolygon(finXPoints, finYPoints, 3);
        }
    }
    
    private void drawHPBar(Graphics2D g2d) {
        int barWidth = size;
        int barHeight = 5;
        int barX = (int)x - barWidth/2;
        int barY = (int)y - size/2 - 15;
        
        // Background
        g2d.setColor(new Color(50, 50, 50, 200));
        g2d.fillRect(barX, barY, barWidth, barHeight);
        
        // HP fill
        float hpPercent = (float)currentHP / maxHP;
        int fillWidth = (int)(barWidth * hpPercent);
        
        Color hpColor = hpPercent > 0.5f ? new Color(0, 255, 100) :
                       hpPercent > 0.25f ? new Color(255, 200, 0) :
                       new Color(255, 50, 50);
        
        g2d.setColor(hpColor);
        g2d.fillRect(barX, barY, fillWidth, barHeight);
        
        // Border
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }
    
    public int getSizeValue() {
        // Returns size value for spawn management (1-3 based on size)
        if (size < 50) return 1;
        if (size < 75) return 2;
        return 3;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
}