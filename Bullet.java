import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Bullet {
    private double x, y;      // Position
    private final double angle;     // Direction in radians
    private final double speed = 7; // Speed of the bullet
    private final int radius = 2; // Size of the bullet

    public Bullet(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public void update(int universeWidth, int universeHeight) {
        // Move bullet in direction of angle
        x += speed * Math.sin(angle);
        y -= speed * Math.cos(angle);

        // Loop universe boundaries
        if (x < 0) x += universeWidth;
        else if (x > universeWidth) x -= universeWidth;

        if (y < 0) y += universeHeight;
        else if (y > universeHeight) y -= universeHeight;
    }

    public Ellipse2D.Double getBounds() {
        return new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);
    }
}
